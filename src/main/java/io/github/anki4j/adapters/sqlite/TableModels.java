/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader of the models kept in tables of a collection file.
 *
 * @since 0.1.0
 */
final class TableModels {

    /**
     * Utility class.
     */
    private TableModels() {
    }

    /**
     * Read the decks of the collection.
     *
     * @param connection Connection to the database file
     * @return Decks, in identifier order
     * @throws SQLException When the decks cannot be read
     */
    static List<Deck> decks(final Connection connection) throws SQLException {
        final List<Deck> decks = new ArrayList<>(0);
        final String query = """
            select id, name, mtime_secs, usn
            from decks
            order by id
            """;
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                decks.add(
                    Deck.builder()
                        .id(new DeckId(rows.getLong("id")))
                        .name(rows.getString("name"))
                        .modified(rows.getLong("mtime_secs"))
                        .usn(rows.getLong("usn"))
                        .dynamic(false)
                        .build()
                );
            }
        }
        return decks;
    }

    /**
     * Read the note types of the collection.
     *
     * @param connection Connection to the database file
     * @return Note types, in identifier order
     * @throws SQLException When the note types cannot be read
     */
    static List<NoteType> noteTypes(final Connection connection)
        throws SQLException {
        final List<NoteType> types = new ArrayList<>(0);
        final String query = """
            select id, name, mtime_secs, usn
            from notetypes
            order by id
            """;
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                final NoteTypeId id = new NoteTypeId(rows.getLong("id"));
                types.add(
                    NoteType.builder()
                        .id(id)
                        .name(rows.getString("name"))
                        .fields(TableModels.fields(connection, id))
                        .templates(TableModels.templates(connection, id))
                        .modified(rows.getLong("mtime_secs"))
                        .usn(rows.getLong("usn"))
                        .build()
                );
            }
        }
        return types;
    }

    /**
     * Read the fields of a note type.
     *
     * @param connection Connection to the database file
     * @param id Identifier of the note type
     * @return Fields, in ordinal order
     * @throws SQLException When the fields cannot be read
     */
    private static List<NoteField> fields(
        final Connection connection,
        final NoteTypeId id
    ) throws SQLException {
        final List<NoteField> fields = new ArrayList<>(0);
        final String query = """
            select name, ord
            from fields
            where ntid = ?
            order by ord
            """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id.getValue());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    fields.add(
                        NoteField.builder()
                            .name(rows.getString("name"))
                            .ordinal(rows.getInt("ord"))
                            .build()
                    );
                }
            }
        }
        return fields;
    }

    /**
     * Read the card templates of a note type.
     *
     * @param connection Connection to the database file
     * @param id Identifier of the note type
     * @return Card templates, in ordinal order
     * @throws SQLException When the card templates cannot be read
     */
    private static List<NoteTemplate> templates(
        final Connection connection,
        final NoteTypeId id
    ) throws SQLException {
        final List<NoteTemplate> templates = new ArrayList<>(0);
        final String query = """
            select name, ord
            from templates
            where ntid = ?
            order by ord
            """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id.getValue());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    templates.add(
                        NoteTemplate.builder()
                            .name(rows.getString("name"))
                            .ordinal(rows.getInt("ord"))
                            .question("")
                            .answer("")
                            .build()
                    );
                }
            }
        }
        return templates;
    }
}
