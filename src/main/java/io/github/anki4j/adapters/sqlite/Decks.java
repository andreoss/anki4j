/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The decks of a collection.
 *
 * @since 0.1.0
 */
final class Decks {

    /**
     * Schema version from which the decks are kept in tables.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.builder()
        .build()
        .type(Object.class);

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Decks of the collection.
     */
    private final List<Deck> decklist;

    /**
     * Document the decks are kept in.
     */
    private final Document document;

    /**
     * New decks of a collection.
     *
     * @param connection Connection to the database file
     * @param decks Decks of the collection
     * @param document Document the decks are kept in
     */
    Decks(
        final Connection connection,
        final List<Deck> decks,
        final Document document
    ) {
        this.connection = connection;
        this.decklist = decks;
        this.document = document;
    }

    /**
     * Read the decks of the collection.
     *
     * @param version Schema version of the collection
     * @return Decks, in identifier order
     * @throws SQLException When the decks cannot be read
     */
    List<Deck> read(final int version) throws SQLException {
        final List<Deck> found;
        if (version >= Decks.TABLE_VERSION) {
            found = this.readtables();
        } else {
            found = this.readdocument();
        }
        return found;
    }

    /**
     * Identifier of the deck added next.
     *
     * @return Identifier, one above the highest in use
     */
    long nextid() {
        long next = 1L;
        for (final Deck deck : this.decklist) {
            if (deck.getId().getValue() >= next) {
                next = deck.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * Identifier of the deck the notes and cards of a deleted deck move to.
     *
     * @return Identifier of the default deck
     */
    long defaultid() {
        long found = 1L;
        for (final Deck deck : this.decklist) {
            if (deck.getId().getValue() == 1L) {
                found = 1L;
            }
        }
        return found;
    }

    /**
     * Write the decks of a collection.
     *
     * @param version Schema version of the collection
     */
    void write(final int version) {
        try {
            if (version >= Decks.TABLE_VERSION) {
                this.writetables();
            } else {
                try (
                    PreparedStatement statement = this.connection.prepareStatement(
                        "update col set decks = ?"
                    )
                ) {
                    statement.setString(1, this.text());
                    statement.executeUpdate();
                }
            }
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the decks", error);
        }
    }

    /**
     * Build the document of the decks of a collection.
     *
     * @return Document of the decks
     */
    String text() {
        final Map<String, Object> entries = new LinkedHashMap<>();
        for (final Deck deck : this.decklist) {
            final Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", deck.getId().getValue());
            entry.put("name", deck.getName());
            entry.put("mod", deck.getModified());
            entry.put("usn", deck.getUsn());
            final int dynamic;
            if (deck.isDynamic()) {
                dynamic = 1;
            } else {
                dynamic = 0;
            }
            entry.put("dyn", dynamic);
            entries.put(String.valueOf(deck.getId().getValue()), entry);
        }
        return Decks.TYPE.toJson(entries);
    }

    private List<Deck> readtables() throws SQLException {
        final List<Deck> found = new ArrayList<>(0);
        try (
            Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(
                "select id, name, mtime_secs, usn from decks order by id"
            )
        ) {
            while (rows.next()) {
                found.add(
                    new Deck(
                        new DeckId(rows.getLong("id")),
                        rows.getString("name"),
                        rows.getLong("mtime_secs"),
                        rows.getLong("usn"),
                        false
                    )
                );
            }
        }
        return found;
    }

    private List<Deck> readdocument() {
        final List<Deck> found = new ArrayList<>(0);
        for (final Entry entry : this.document.entries()) {
            found.add(
                new Deck(
                    new DeckId(entry.number("id", 0L)),
                    entry.text("name"),
                    entry.number("mod", 0L),
                    entry.number("usn", 0L),
                    entry.number("dyn", 0L) != 0L
                )
            );
        }
        return found;
    }

    private void writetables() throws SQLException {
        try (
            PreparedStatement statement = this.connection.prepareStatement(
                """
                insert into decks (id, name, mtime_secs, usn, common, kind)
                values (?, ?, ?, ?, x'00', x'00')
                on conflict(id) do update set
                    name = excluded.name,
                    mtime_secs = excluded.mtime_secs,
                    usn = excluded.usn
                """
            )
        ) {
            for (final Deck deck : this.decklist) {
                statement.setLong(1, deck.getId().getValue());
                statement.setString(2, deck.getName());
                statement.setLong(3, deck.getModified());
                statement.setLong(4, deck.getUsn());
                statement.executeUpdate();
            }
        }
        for (final Deck deck : this.removed()) {
            try (
                PreparedStatement statement = this.connection.prepareStatement(
                    "delete from decks where id = ?"
                )
            ) {
                statement.setLong(1, deck.getId().getValue());
                statement.executeUpdate();
            }
        }
    }

    private List<Deck> removed() throws SQLException {
        final List<Deck> removed = new ArrayList<>(0);
        try (
            Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(
                "select id, name, mtime_secs, usn from decks order by id"
            )
        ) {
            while (rows.next()) {
                boolean wanted = false;
                for (final Deck deck : this.decklist) {
                    if (deck.getId().getValue() == rows.getLong("id")) {
                        wanted = true;
                    }
                }
                if (!wanted) {
                    final DeckId id = new DeckId(rows.getLong("id"));
                    removed.add(
                        new Deck(
                            id,
                            rows.getString("name"),
                            rows.getLong("mtime_secs"),
                            rows.getLong("usn"),
                            false
                        )
                    );
                }
            }
        }
        return removed;
    }
}
