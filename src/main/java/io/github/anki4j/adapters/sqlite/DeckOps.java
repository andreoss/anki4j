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
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writer of the decks of a collection file.
 *
 * @since 0.1.0
 */
final class DeckOps {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Schema version from which the decks are kept in tables.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * Utility class.
     */
    private DeckOps() {
    }

    /**
     * Identifier of the deck created next.
     *
     * @param decks Decks of the collection
     * @return Identifier, one above the highest in use
     */
    static long nextid(final List<Deck> decks) {
        long next = 1L;
        for (final Deck deck : decks) {
            if (deck.getId().getValue() >= next) {
                next = deck.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * Identifier of the deck the notes and cards of a deleted deck move to.
     *
     * @param decks Decks of the collection
     * @return Identifier of the default deck
     */
    static long defaultid(final List<Deck> decks) {
        long found = 1L;
        for (final Deck deck : decks) {
            if (deck.getId().getValue() == 1L) {
                found = 1L;
            }
        }
        return found;
    }

    /**
     * Current time in seconds since the epoch.
     *
     * @return Seconds since the epoch
     */
    static long now() {
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Write the decks of a collection.
     *
     * @param connection Connection to the database file
     * @param version Schema version of the collection
     * @param decks Decks of the collection
     */
    static void write(
        final Connection connection,
        final int version,
        final List<Deck> decks
    ) {
        try {
            if (version >= DeckOps.TABLE_VERSION) {
                DeckOps.writeTables(connection, decks);
            } else {
                DeckOps.writeDocument(connection, decks);
            }
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the decks", error);
        }
    }

    /**
     * Build the document of the decks of a collection.
     *
     * @param decks Decks of the collection
     * @return Document of the decks
     */
    static String document(final List<Deck> decks) {
        final Map<String, Object> document = new LinkedHashMap<>();
        for (final Deck deck : decks) {
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
            document.put(String.valueOf(deck.getId().getValue()), entry);
        }
        return DeckOps.TYPE.toJson(document);
    }

    /**
     * Write the decks of a collection that keeps its models as documents.
     *
     * @param connection Connection to the database file
     * @param decks Decks of the collection
     * @throws SQLException When the decks cannot be written
     */
    private static void writeDocument(
        final Connection connection,
        final List<Deck> decks
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            "update col set decks = ?"
        )) {
            statement.setString(1, DeckOps.document(decks));
            statement.executeUpdate();
        }
    }

    /**
     * Write the decks of a collection that keeps its models in tables.
     *
     * @param connection Connection to the database file
     * @param decks Decks of the collection
     * @throws SQLException When the decks cannot be written
     */
    private static void writeTables(
        final Connection connection,
        final List<Deck> decks
    ) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            insert into decks (id, name, mtime_secs, usn, common, kind)
            values (?, ?, ?, ?, x'00', x'00')
            on conflict(id) do update set
                name = excluded.name,
                mtime_secs = excluded.mtime_secs,
                usn = excluded.usn
            """
        )) {
            for (final Deck deck : decks) {
                statement.setLong(1, deck.getId().getValue());
                statement.setString(2, deck.getName());
                statement.setLong(3, deck.getModified());
                statement.setLong(4, deck.getUsn());
                statement.executeUpdate();
            }
        }
        for (final Deck deck : DeckOps.removed(connection, decks)) {
            try (PreparedStatement statement = connection.prepareStatement(
                "delete from decks where id = ?"
            )) {
                statement.setLong(1, deck.getId().getValue());
                statement.executeUpdate();
            }
        }
    }

    /**
     * Decks of a collection that are not wanted any more.
     *
     * @param connection Connection to the database file
     * @param decks Decks of the collection
     * @return Decks to remove
     * @throws SQLException When the decks cannot be read
     */
    private static List<Deck> removed(
        final Connection connection,
        final List<Deck> decks
    ) throws SQLException {
        final List<Deck> removed = new java.util.ArrayList<>(0);
        try (var statement = connection.createStatement();
            var rows = statement.executeQuery(
                "select id, name, mtime_secs, usn from decks order by id"
            )) {
            while (rows.next()) {
                boolean wanted = false;
                for (final Deck deck : decks) {
                    if (deck.getId().getValue() == rows.getLong("id")) {
                        wanted = true;
                    }
                }
                if (!wanted) {
                    final DeckId id = new DeckId(rows.getLong("id"));
                    removed.add(
                        Deck.builder()
                            .id(id)
                            .name(rows.getString("name"))
                            .modified(rows.getLong("mtime_secs"))
                            .usn(rows.getLong("usn"))
                            .dynamic(false)
                            .build()
                    );
                }
            }
        }
        return removed;
    }
}
