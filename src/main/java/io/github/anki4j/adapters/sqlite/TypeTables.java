/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteTypeId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The tables of the note types of a collection.
 *
 * @since 0.1.0
 */
final class TypeTables {

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * New tables of the note types.
     *
     * @param connection Connection to the database file
     */
    TypeTables(final Connection connection) {
        this.connection = connection;
    }

    /**
     * Fields of a note type.
     *
     * @param id Identifier of the note type
     * @return Fields of the note type
     * @throws SQLException When the fields cannot be read
     */
    List<NoteField> fields(final NoteTypeId id) throws SQLException {
        final List<NoteField> fields = new ArrayList<>(0);
        try (
            PreparedStatement statement = this.connection.prepareStatement(
                """
                select ord, name from fields where ntid = ? order by ord
                """
            )
        ) {
            statement.setLong(1, id.getValue());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    fields.add(
                        new NoteField(
                            rows.getString("name"),
                            rows.getInt("ord")
                        )
                    );
                }
            }
        }
        return fields;
    }

    /**
     * Card templates of a note type.
     *
     * @param id Identifier of the note type
     * @return Card templates of the note type
     * @throws SQLException When the templates cannot be read
     */
    List<NoteTemplate> templates(final NoteTypeId id) throws SQLException {
        final List<NoteTemplate> templates = new ArrayList<>(0);
        try (
            PreparedStatement statement = this.connection.prepareStatement(
                """
                select ord, name from templates where ntid = ? order by ord
                """
            )
        ) {
            statement.setLong(1, id.getValue());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    templates.add(
                        new NoteTemplate(
                            rows.getString("name"),
                            rows.getInt("ord"),
                            "",
                            ""
                        )
                    );
                }
            }
        }
        return templates;
    }
}
