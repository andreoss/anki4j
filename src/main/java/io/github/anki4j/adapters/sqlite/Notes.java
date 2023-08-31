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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The notes of a collection.
 *
 * @since 0.1.0
 */
final class Notes {

    /**
     * Characters of the identifiers of the notes.
     */
    private static final String ALPHABET = """
        abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\
        0123456789!#$%&()*+,-./:;<=>?@[]^_`{|}~\
        """;

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Notes of the collection.
     */
    private final List<Note> notelist;

    /**
     * Source of the identifiers of the notes.
     */
    private final Random random;

    /**
     * New notes of a collection.
     *
     * @param connection Connection to the database file
     * @param notes Notes of the collection
     */
    Notes(final Connection connection, final List<Note> notes) {
        this.connection = connection;
        this.notelist = notes;
        this.random = new Random();
    }

    /**
     * Read the notes of the collection.
     *
     * @return Notes, in identifier order
     * @throws SQLException When the notes cannot be read
     */
    List<Note> read() throws SQLException {
        final List<Note> found = new ArrayList<>(0);
        final String query = """
            select id, guid, mid, mod, usn, tags, flds, csum, flags, data
            from notes
            order by id
            """;
        try (Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                found.add(
                    Note.builder()
                        .id(new NoteId(rows.getLong("id")))
                        .guid(rows.getString("guid"))
                        .notetype(new NoteTypeId(rows.getLong("mid")))
                        .modified(rows.getLong("mod"))
                        .usn(rows.getLong("usn"))
                        .tags(new Tags(rows.getString("tags")).values())
                        .fields(new FieldValues(rows.getString("flds")).values())
                        .checksum(rows.getLong("csum"))
                        .flags(rows.getInt("flags"))
                        .data(rows.getString("data"))
                        .build()
                );
            }
        }
        return found;
    }

    /**
     * Write a note.
     *
     * @param note Note to write
     */
    void write(final Note note) {
        final String fields = String.join(
            FieldValues.SEPARATOR, note.getFields()
        );
        final String tags = String.join(Tags.SEPARATOR, note.getTags());
        try (PreparedStatement statement = this.connection.prepareStatement(
            """
            insert into notes (
                id, guid, mid, mod, usn, tags, flds, sfld, csum, flags, data
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
        )) {
            statement.setLong(1, note.getId().getValue());
            statement.setString(2, note.getGuid());
            statement.setLong(3, note.getNotetype().getValue());
            statement.setLong(4, note.getModified());
            statement.setLong(5, note.getUsn());
            statement.setString(6, tags);
            statement.setString(7, fields);
            statement.setString(8, new FieldValues(fields).sortfield());
            statement.setLong(9, note.getChecksum());
            statement.setInt(10, note.getFlags());
            statement.setString(11, note.getData());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the note", error);
        }
    }

    /**
     * Identifier of the note added next.
     *
     * @return Identifier, one above the highest in use
     */
    long nextid() {
        long next = 1L;
        for (final Note note : this.notelist) {
            if (note.getId().getValue() >= next) {
                next = note.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * Identifier of a note, unique in the collection.
     *
     * @return Identifier of the note
     */
    String guid() {
        long value = (this.random.nextLong() & Long.MAX_VALUE) | 1L;
        final StringBuilder builder = new StringBuilder();
        while (value > 0L) {
            builder.append(Notes.ALPHABET.charAt((int) (value % 91L)));
            value /= 91L;
        }
        return builder.reverse().toString();
    }

    /**
     * The note of a collection.
     *
     * @param id Identifier of the note
     * @return Note of the collection
     */
    Note find(final NoteId id) {
        for (final Note note : this.notelist) {
            if (note.getId().getValue() == id.getValue()) {
                return note;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note with id %d", id.getValue())
        );
    }
}
