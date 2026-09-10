/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the field values and the tags of a note.
 *
 * @since 0.1.0
 */
final class NotesTest {

    /**
     * The field values of a note are split.
     */
    @Test
    void splitsFields() {
        Assertions.assertEquals(
            List.of("bonjour", "hello"),
            new FieldValues("bonjourhello").values()
        );
    }

    /**
     * Empty field values are kept.
     */
    @Test
    void keepsEmptyFieldValues() {
        Assertions.assertEquals(
            List.of("bonjour", "", "hello"),
            new FieldValues("bonjourhello").values()
        );
    }

    /**
     * A note without field values holds none.
     */
    @Test
    void readsEmptyFields() {
        Assertions.assertAll(
            () -> Assertions.assertTrue(
                new FieldValues((String) null).values().isEmpty()
            ),
            () -> Assertions.assertTrue(new FieldValues("").values().isEmpty())
        );
    }

    /**
     * The tags of a note are split.
     */
    @Test
    void splitsTags() {
        Assertions.assertEquals(
            List.of("salut", "spanish"),
            new Tags("salut spanish").values()
        );
    }

    /**
     * A note without tags holds none.
     */
    @Test
    void readsEmptyTags() {
        Assertions.assertAll(
            () -> Assertions.assertTrue(
                new Tags((String) null).values().isEmpty()
            ),
            () -> Assertions.assertTrue(new Tags("   ").values().isEmpty())
        );
    }

    /**
     * The field values of a note are joined as the format keeps them.
     */
    @Test
    void joinsFields() {
        Assertions.assertEquals(
            "bonjourhello",
            String.join(
                FieldValues.SEPARATOR, List.of("bonjour", "hello")
            )
        );
    }

    /**
     * The format sorts the notes by their first field.
     */
    @Test
    void readsSortField() {
        Assertions.assertAll(
            () -> Assertions.assertEquals(
                "bonjour",
                new FieldValues("bonjourhello").sortfield()
            ),
            () -> Assertions.assertEquals("", new FieldValues("").sortfield())
        );
    }

    /**
     * The checksum of a note is the one of its sort field.
     */
    @Test
    void readsChecksum() {
        Assertions.assertEquals(
            new FieldValues("bonjourhello").checksum(),
            new FieldValues("bonjourbye").checksum()
        );
    }
}
