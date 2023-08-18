/**
 * Conformance of the library with the format.
 *
 * @since 0.1.0
 */
package io.github.anki4j.conformance;

import io.github.anki4j.Work;
import io.github.anki4j.adapters.sqlite.SqliteCollection;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import io.github.anki4j.domain.Schedule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The cases of the format, kept as collections a reader outside this
 * repository can read.
 *
 * @since 0.1.0
 */
final class ConformanceTest {

    /**
     * The cases are written where a reader outside this repository can
     * find them.
     *
     * @throws Exception When the collections cannot be written
     */
    @Test
    void writesTheCases() throws Exception {
        final Path directory = new Work().directory("conformance");
        ConformanceTest.empty(directory.resolve("empty.anki2"));
        ConformanceTest.decks(directory.resolve("decks.anki2"));
        ConformanceTest.notes(directory.resolve("notes.anki2"));
        ConformanceTest.types(directory.resolve("types.anki2"));
        ConformanceTest.cards(directory.resolve("cards.anki2"));
        Assertions.assertTrue(Files.exists(directory));
    }

    /**
     * A collection without anything in it.
     *
     * @param path Path of the collection
     * @throws Exception When the collection cannot be written
     */
    private static void empty(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.save(path);
        }
    }

    /**
     * A collection with decks.
     *
     * @param path Path of the collection
     * @throws Exception When the collection cannot be written
     */
    private static void decks(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.editor().createDeck("Default");
            collection.editor().createDeck("Spanish::Verbs");
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(2, collection.decks().size());
        }
    }

    /**
     * A collection with notes that carry tags and flags.
     *
     * @param path Path of the collection
     * @throws Exception When the collection cannot be written
     */
    private static void notes(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            final Note note = collection.editor().addNote(
                new NoteTypeId(1L), List.of("hola", "hello")
            );
            collection.editor().updateNote(
                Note.builder()
                    .id(note.getId())
                    .guid(note.getGuid())
                    .notetype(note.getNotetype())
                    .modified(note.getModified())
                    .usn(note.getUsn())
                    .tags(List.of("greeting", "spanish"))
                    .fields(note.getFields())
                    .checksum(note.getChecksum())
                    .flags(3)
                    .data(note.getData())
                    .build()
            );
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Note note = collection.notes().get(0);
            Assertions.assertEquals(
                List.of("greeting", "spanish"), note.getTags()
            );
            Assertions.assertEquals(3, note.getFlags());
        }
    }

    /**
     * A collection with a note type that holds two templates.
     *
     * @param path Path of the collection
     * @throws Exception When the collection cannot be written
     */
    private static void types(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.types().addNoteType(
                NoteType.builder()
                    .id(new NoteTypeId(1L))
                    .name("Basic")
                    .fields(
                        List.of(
                            NoteField.builder().name("Front").ordinal(0)
                                .build(),
                            NoteField.builder().name("Back").ordinal(1)
                                .build()
                        )
                    )
                    .templates(
                        List.of(
                            NoteTemplate.builder()
                                .name("Card 1").ordinal(0)
                                .question("{{Front}}")
                                .answer("{{FrontSide}}")
                                .build(),
                            NoteTemplate.builder()
                                .name("Card 2").ordinal(1)
                                .question("{{Back}}")
                                .answer("{{Back}}")
                                .build()
                        )
                    )
                    .checks(List.of())
                    .css(".card {}")
                    .sortfield(0)
                    .deck(new DeckId(1L))
                    .modified(0L)
                    .usn(-1L)
                    .build()
            );
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.noteTypes().size());
            Assertions.assertEquals(
                2, collection.noteTypes().get(0).getTemplates().size()
            );
        }
    }

    /**
     * A note type with one template.
     *
     * @return Note type
     */
    private static NoteType basic() {
        return NoteType.builder()
            .id(new NoteTypeId(1L))
            .name("Basic")
            .fields(
                List.of(
                    NoteField.builder().name("Front").ordinal(0).build(),
                    NoteField.builder().name("Back").ordinal(1).build()
                )
            )
            .templates(
                List.of(
                    NoteTemplate.builder()
                        .name("Card 1").ordinal(0)
                        .question("{{Front}}")
                        .answer("{{FrontSide}}")
                        .build()
                )
            )
            .checks(List.of())
            .css(".card {}")
            .sortfield(0)
            .deck(new DeckId(1L))
            .modified(0L)
            .usn(-1L)
            .build();
    }

    /**
     * A collection with cards that carry a scheduling state.
     *
     * @param path Path of the collection
     * @throws Exception When the collection cannot be written
     */
    private static void cards(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.types().addNoteType(ConformanceTest.basic());
            final Note note = collection.editor().addNote(
                new NoteTypeId(1L), List.of("hola", "hello")
            );
            collection.editor().generateCards(
                note.getId(), new DeckId(1L)
            );
            collection.editor().setSchedule(
                collection.cards().get(0).getId(),
                Schedule.builder()
                    .type(2)
                    .queue(2)
                    .due(100L)
                    .interval(10L)
                    .factor(2500)
                    .repetitions(3)
                    .lapses(1)
                    .left(0)
                    .originaldue(90L)
                    .originaldeck(1L)
                    .build()
            );
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.cards().size());
            Assertions.assertEquals(
                2, collection.cards().get(0).getSchedule().getQueue()
            );
        }
    }
}
