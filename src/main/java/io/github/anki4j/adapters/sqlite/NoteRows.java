/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteTypeId;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Reader of the notes of a collection file.
 *
 * @since 0.1.0
 */
final class NoteRows {

    /**
     * Character between the field values of a note.
     */
    private static final char FIELD_SEPARATOR = '\u001F';

    /**
     * Character between the tags of a note.
     */
    private static final String TAG_SEPARATOR = " ";

    /**
     * Utility class.
     */
    private NoteRows() {
    }

    /**
     * Read the notes of the collection.
     *
     * @param connection Connection to the database file
     * @return Notes, in identifier order
     * @throws SQLException When the notes cannot be read
     */
    static List<Note> read(final Connection connection) throws SQLException {
        final List<Note> notes = new ArrayList<>(0);
        final String query = """
            select id, guid, mid, mod, usn, tags, flds, csum, flags, data
            from notes
            order by id
            """;
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                notes.add(
                    Note.builder()
                        .id(new NoteId(rows.getLong("id")))
                        .guid(rows.getString("guid"))
                        .notetype(new NoteTypeId(rows.getLong("mid")))
                        .modified(rows.getLong("mod"))
                        .usn(rows.getLong("usn"))
                        .tags(NoteRows.tags(rows.getString("tags")))
                        .fields(NoteRows.fields(rows.getString("flds")))
                        .checksum(rows.getLong("csum"))
                        .flags(rows.getInt("flags"))
                        .data(rows.getString("data"))
                        .build()
                );
            }
        }
        return notes;
    }

    /**
     * Split the field values of a note.
     *
     * @param joined Field values as stored
     * @return Field values, empty when there are none
     */
    static List<String> fields(final String joined) {
        List<String> fields = List.of();
        if (joined != null && !joined.isEmpty()) {
            fields = Arrays.asList(
                joined.split(String.valueOf(NoteRows.FIELD_SEPARATOR), -1)
            );
        }
        return fields;
    }

    /**
     * Split the tags of a note.
     *
     * @param joined Tags as stored
     * @return Tags, empty when there are none
     */
    static List<String> tags(final String joined) {
        List<String> tags = List.of();
        if (joined != null && !joined.isBlank()) {
            tags = Arrays.asList(joined.split(NoteRows.TAG_SEPARATOR));
        }
        return tags;
    }
}
