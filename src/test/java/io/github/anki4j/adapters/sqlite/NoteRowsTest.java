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
 * Tests of the reader of the notes.
 *
 * @since 0.1.0
 */
final class NoteRowsTest {

    /**
     * The field values of a note are split.
     */
    @Test
    void splitsFields() {
        Assertions.assertEquals(
            List.of("hola", "hello"), NoteRows.fields("hola\u001Fhello")
        );
    }

    /**
     * Empty field values are kept.
     */
    @Test
    void keepsEmptyFieldValues() {
        Assertions.assertEquals(
            List.of("hola", "", "hello"), NoteRows.fields("hola\u001F\u001Fhello")
        );
    }

    /**
     * A note without field values holds none.
     */
    @Test
    void readsEmptyFields() {
        Assertions.assertTrue(NoteRows.fields(null).isEmpty());
        Assertions.assertTrue(NoteRows.fields("").isEmpty());
    }

    /**
     * The tags of a note are split.
     */
    @Test
    void splitsTags() {
        Assertions.assertEquals(
            List.of("greeting", "spanish"), NoteRows.tags("greeting spanish")
        );
    }

    /**
     * A note without tags holds none.
     */
    @Test
    void readsEmptyTags() {
        Assertions.assertTrue(NoteRows.tags(null).isEmpty());
        Assertions.assertTrue(NoteRows.tags("   ").isEmpty());
    }
}
