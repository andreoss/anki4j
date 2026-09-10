/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteTypeId;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the note operations of a collection.
 *
 * @since 0.1.0
 */
final class NoteOperationTest {

    /**
     * First field of the notes the tests write.
     */
    private static final String FRONT = "bonjour";

    /**
     * A note is added and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void addsANote() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("addnote"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    final Note note = collection.editor().addNote(
                        new NoteTypeId(Fixture.NOTE_TYPE_ID),
                        List.of(NoteOperationTest.FRONT, "hello")
                    );
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            2, collection.notes().size()
                        ),
                        () -> Assertions.assertEquals(
                            NoteOperationTest.FRONT, note.getFields().get(0)
                        ),
                        () -> Assertions.assertFalse(note.getGuid().isEmpty())
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertEquals(2, collection.notes().size());
                    final Note note = collection.notes().get(1);
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            List.of(NoteOperationTest.FRONT, "hello"),
                            note.getFields()
                        ),
                        () -> Assertions.assertTrue(note.getChecksum() > 0L)
                    );
                }
            }
        );
    }

    /**
     * The values of a note are changed and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void updatesANote() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("updatenote"));
        Assertions.assertAll(
            () -> NoteOperationTest.writeNote(path),
            () -> NoteOperationTest.readNote(path)
        );
    }

    /**
     * A note is deleted with its cards.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void deletesANote() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("deletenote"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.editor().deleteNote(new NoteId(Fixture.NOTE_ID));
                    Assertions.assertAll(
                        () -> Assertions.assertTrue(collection.notes().isEmpty()),
                        () -> Assertions.assertTrue(collection.cards().isEmpty())
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertAll(
                        () -> Assertions.assertTrue(collection.notes().isEmpty()),
                        () -> Assertions.assertTrue(collection.cards().isEmpty())
                    );
                }
            }
        );
    }

    /**
     * An operation on a note that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAMissingNote() throws Exception {
        try (
            SqliteCollection collection = new SqliteCollection(
                Fixture.legacy(new Work().directory("missingnote"))
            )
        ) {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().deleteNote(new NoteId(99L))
            );
            final Note note = collection.notes().get(0);
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().updateNote(
                    new Note(
                        new NoteId(99L), note.getGuid(),
                        note.getNotetype(), note.getModified(),
                        note.getUsn(), note.getTags(), note.getFields(),
                        note.getChecksum(), note.getFlags(),
                        note.getData()
                    )
                )
            );
        }
    }

    /**
     * A change on a closed file is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAClosedFile() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("closednote"));
        final SqliteCollection collection = new SqliteCollection(path);
        collection.close();
        Assertions.assertThrows(
            IOException.class,
            () -> collection.save(path.resolve("absent").resolve("new.anki2"))
        );
    }

    private static void writeNote(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Note note = collection.notes().get(0);
            collection.editor().updateNote(
                new Note(
                    note.getId(), note.getGuid(), note.getNotetype(),
                    note.getModified(), note.getUsn(), List.of("salut"),
                    List.of(NoteOperationTest.FRONT, "hello"),
                    note.getChecksum(), 3, note.getData()
                )
            );
            Assertions.assertAll(
                () -> Assertions.assertEquals(
                    List.of("salut"), collection.notes().get(0).getTags()
                ),
                () -> Assertions.assertEquals(
                    3, collection.notes().get(0).getFlags()
                )
            );
            collection.save(path);
        }
    }

    private static void readNote(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Note note = collection.notes().get(0);
            Assertions.assertAll(
                () -> Assertions.assertEquals(List.of("salut"), note.getTags()),
                () -> Assertions.assertEquals(
                    List.of(NoteOperationTest.FRONT, "hello"), note.getFields()
                ),
                () -> Assertions.assertEquals(3, note.getFlags())
            );
        }
    }
}
