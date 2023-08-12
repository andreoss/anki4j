/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

/**
 * Writer of the notes of a collection file.
 *
 * @since 0.1.0
 */
final class NoteOps {

    /**
     * Characters of the identifiers of the notes.
     */
    private static final String ALPHABET = """
        abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ\
        0123456789!#$%&()*+,-./:;<=>?@[]^_`{|}~\
        """;

    /**
     * Source of the identifiers of the notes.
     */
    private static final Random RANDOM = new Random();

    /**
     * Utility class.
     */
    private NoteOps() {
    }

    /**
     * Identifier of the note added next.
     *
     * @param notes Notes of the collection
     * @return Identifier, one above the highest in use
     */
    static long nextid(final List<Note> notes) {
        long next = 1L;
        for (final Note note : notes) {
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
    static String guid() {
        long value = NoteOps.RANDOM.nextLong() & Long.MAX_VALUE;
        final StringBuilder builder = new StringBuilder();
        while (value > 0L) {
            builder.append(NoteOps.ALPHABET.charAt((int) (value % 91L)));
            value /= 91L;
        }
        if (builder.isEmpty()) {
            builder.append(NoteOps.ALPHABET.charAt(0));
        }
        return builder.reverse().toString();
    }

    /**
     * Join the field values of a note as the format keeps them.
     *
     * @param fields Field values of the note
     * @return Field values joined
     */
    static String join(final List<String> fields) {
        return String.join(String.valueOf(NoteRows.SEPARATOR), fields);
    }

    /**
     * Value the format sorts the notes by.
     *
     * @param fields Field values of the note
     * @return Sort field of the note
     */
    static String sortfield(final List<String> fields) {
        String sortfield = "";
        if (!fields.isEmpty()) {
            sortfield = fields.get(0);
        }
        return sortfield;
    }

    /**
     * Checksum the format keeps for the sort field of a note.
     *
     * @param text Sort field of the note
     * @return Checksum of the field
     */
    static long checksum(final String text) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException error) {
            throw new IllegalStateException("no sha-1 in this runtime", error);
        }
        final byte[] hash = digest.digest(text.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long checksum = 0L;
        for (int index = 0; index < 4; index += 1) {
            checksum = (checksum << 8) | (hash[index] & 0xFFL);
        }
        return checksum;
    }

    /**
     * The note of a collection.
     *
     * @param notes Notes of the collection
     * @param id Identifier of the note
     * @return Note of the collection
     */
    static Note find(final List<Note> notes, final NoteId id) {
        for (final Note note : notes) {
            if (note.getId().getValue() == id.getValue()) {
                return note;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note with id %d", id.getValue())
        );
    }

    /**
     * Write a note.
     *
     * @param connection Connection to the database file
     * @param note Note to write
     */
    static void write(final Connection connection, final Note note) {
        try (PreparedStatement statement = connection.prepareStatement(
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
            statement.setString(6, String.join(" ", note.getTags()));
            statement.setString(7, NoteOps.join(note.getFields()));
            statement.setString(8, NoteOps.sortfield(note.getFields()));
            statement.setLong(9, note.getChecksum());
            statement.setInt(10, note.getFlags());
            statement.setString(11, note.getData());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the note", error);
        }
    }

    /**
     * Write the values of a note.
     *
     * @param connection Connection to the database file
     * @param note Note with its new values
     */
    static void update(final Connection connection, final Note note) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            update notes
            set mod = ?, tags = ?, flds = ?, sfld = ?, csum = ?, flags = ?
            where id = ?
            """
        )) {
            statement.setLong(1, note.getModified());
            statement.setString(2, String.join(" ", note.getTags()));
            statement.setString(3, NoteOps.join(note.getFields()));
            statement.setString(4, NoteOps.sortfield(note.getFields()));
            statement.setLong(5, note.getChecksum());
            statement.setInt(6, note.getFlags());
            statement.setLong(7, note.getId().getValue());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the note", error);
        }
    }

    /**
     * Remove a note, its cards and their review log.
     *
     * @param connection Connection to the database file
     * @param id Identifier of the note
     */
    static void remove(final Connection connection, final NoteId id) {
        try (PreparedStatement statement = connection.prepareStatement(
            "delete from cards where nid = ?"
        )) {
            statement.setLong(1, id.getValue());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot remove the cards", error);
        }
        try (PreparedStatement statement = connection.prepareStatement(
            "delete from revlog where cid in (select id from cards where nid = ?)"
        )) {
            statement.setLong(1, id.getValue());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot remove the review log", error);
        }
        try (PreparedStatement statement = connection.prepareStatement(
            "delete from notes where id = ?"
        )) {
            statement.setLong(1, id.getValue());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot remove the note", error);
        }
    }
}
