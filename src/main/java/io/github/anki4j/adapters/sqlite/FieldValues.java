/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * The field values of a note, as the format keeps them.
 *
 * @since 0.1.0
 */
final class FieldValues {

    /**
     * Character between the field values of a note.
     */
    static final String SEPARATOR = "\u001F";

    /**
     * Text of the field values.
     */
    private final String text;

    /**
     * New field values, as the format keeps them.
     *
     * @param text Text of the field values
     */
    FieldValues(final String text) {
        this.text = text;
    }

    /**
     * Values of the fields.
     *
     * @return Values, in the order of the fields
     */
    List<String> values() {
        List<String> values = List.of();
        if (this.text != null && !this.text.isEmpty()) {
            values = Arrays.asList(
                this.text.split(FieldValues.SEPARATOR, -1)
            );
        }
        return values;
    }

    /**
     * Value the format sorts the notes by.
     *
     * @return Sort field of the note
     */
    String sortfield() {
        String sortfield = "";
        final List<String> values = this.values();
        if (!values.isEmpty()) {
            sortfield = values.get(0);
        }
        return sortfield;
    }

    /**
     * Checksum the format keeps for the sort field of a note.
     *
     * @return Checksum of the sort field
     */
    long checksum() {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException error) {
            throw new IllegalStateException("no sha-1 in this runtime", error);
        }
        final byte[] hash = digest.digest(
            this.sortfield().getBytes(StandardCharsets.UTF_8)
        );
        long checksum = 0L;
        for (int index = 0; index < 4; index += 1) {
            checksum = (checksum << 8) | (hash[index] & 0xFFL);
        }
        return checksum;
    }
}
