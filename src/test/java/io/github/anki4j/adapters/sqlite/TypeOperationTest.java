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
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    final NoteType type = collection.types().addNoteType(
                        TypeOperationTest.type("Spanish")
                    );
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            2, collection.noteTypes().size()
                        ),
                        () -> Assertions.assertEquals("Spanish", type.getName())
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertEquals(
                        2, collection.noteTypes().size()
                    );
                    final NoteType type = collection.noteTypes().get(1);
                    Assertions.assertAll(
                        () -> Assertions.assertEquals("Spanish", type.getName()),
                        () -> Assertions.assertEquals(
                            2, type.getFields().size()
                        ),
                        () -> Assertions.assertEquals(
                            1, type.getTemplates().size()
                        ),
                        () -> Assertions.assertEquals(".card {}", type.getCss())
                    );
                }
            }
        );
    }

    /**
     * The values of a note type are changed and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void updatesANoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("updatetype"));
        Assertions.assertAll(
            () -> TypeOperationTest.writeType(path),
            () -> TypeOperationTest.readType(path)
        );
    }

    /**
     * A note type is deleted and kept out of the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void deletesANoteType() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("deletetype"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.types().deleteNoteType(
                        new NoteTypeId(Fixture.NOTE_TYPE_ID)
                    );
                    Assertions.assertTrue(collection.noteTypes().isEmpty());
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertTrue(collection.noteTypes().isEmpty());
                }
            }
        );
    }

    /**
     * An operation on a note type that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAMissingNoteType() throws Exception {
        try (
            SqliteCollection collection = new SqliteCollection(
                Fixture.legacy(new Work().directory("missingtype"))
            )
        ) {
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

    private static NoteType type(final String name) {
        return new NoteType(
            new NoteTypeId(1L),
            name,
            List.of(
                new NoteField("Front", 0),
                new NoteField("Back", 1)
            ),
            List.of(
                new NoteTemplate(
                    "Card 1", 0, "{{Front}}", "{{FrontSide}}"
                )
            ),
            List.of(new Check(0, "any", List.of("0"))),
            ".card {}",
            0,
            new DeckId(1L),
            0L,
            -1L
        );
    }

    private static void writeType(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteType type = collection.noteTypes().get(0);
            collection.types().updateNoteType(
                new NoteType(
                    type.getId(), "Basic renamed", type.getFields(),
                    type.getTemplates(), type.getChecks(), type.getCss(),
                    type.getSortfield(), type.getDeck(),
                    type.getModified(), type.getUsn()
                )
            );
            Assertions.assertEquals(
                "Basic renamed", collection.noteTypes().get(0).getName()
            );
            collection.save(path);
        }
    }

    private static void readType(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(
                "Basic renamed", collection.noteTypes().get(0).getName()
            );
        }
    }
}
