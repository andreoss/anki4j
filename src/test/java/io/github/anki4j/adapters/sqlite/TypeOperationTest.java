/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Check;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the note type operations of a collection.
 *
 * @since 0.1.0
 */
final class TypeOperationTest {

    /**
     * A note type is added and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void addsANoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("addtype"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteType type = collection.types().addNoteType(
                TypeOperationTest.type("Spanish")
            );
            Assertions.assertEquals(2, collection.noteTypes().size());
            Assertions.assertEquals("Spanish", type.getName());
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(2, collection.noteTypes().size());
            final NoteType type = collection.noteTypes().get(1);
            Assertions.assertEquals("Spanish", type.getName());
            Assertions.assertEquals(2, type.getFields().size());
            Assertions.assertEquals(1, type.getTemplates().size());
            Assertions.assertEquals(".card {}", type.getCss());
        }
    }

    /**
     * The values of a note type are changed and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void updatesANoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("updatetype"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteType type = collection.noteTypes().get(0);
            collection.types().updateNoteType(
                NoteType.builder()
                    .id(type.getId())
                    .name("Basic renamed")
                    .fields(type.getFields())
                    .templates(type.getTemplates())
                    .checks(type.getChecks())
                    .css(type.getCss())
                    .sortfield(type.getSortfield())
                    .deck(type.getDeck())
                    .modified(type.getModified())
                    .usn(type.getUsn())
                    .build()
            );
            Assertions.assertEquals(
                "Basic renamed", collection.noteTypes().get(0).getName()
            );
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(
                "Basic renamed", collection.noteTypes().get(0).getName()
            );
        }
    }

    /**
     * A note type is deleted and kept out of the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void deletesANoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("deletetype"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.types().deleteNoteType(new NoteTypeId(Fixture.NOTE_TYPE_ID));
            Assertions.assertTrue(collection.noteTypes().isEmpty());
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertTrue(collection.noteTypes().isEmpty());
        }
    }

    /**
     * An operation on a note type that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAMissingNoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("missingtype"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.types().deleteNoteType(new NoteTypeId(99L))
            );
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.types().updateNoteType(
                    TypeOperationTest.type("None")
                )
            );
        }
    }

    /**
     * A note type with the values the tests need.
     *
     * @param name Name of the note type
     * @return Note type
     */
    private static NoteType type(final String name) {
        return NoteType.builder()
            .id(new NoteTypeId(1L))
            .name(name)
            .fields(
                List.of(
                    NoteField.builder().name("Front").ordinal(0).build(),
                    NoteField.builder().name("Back").ordinal(1).build()
                )
            )
            .templates(
                List.of(
                    NoteTemplate.builder()
                        .name("Card 1")
                        .ordinal(0)
                        .question("{{Front}}")
                        .answer("{{FrontSide}}")
                        .build()
                )
            )
            .checks(List.of(Check.builder().field(0).kind("any").values(List.of("0")).build()))
            .css(".card {}")
            .sortfield(0)
            .deck(new DeckId(1L))
            .modified(0L)
            .usn(-1L)
            .build();
    }
}
