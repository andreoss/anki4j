/**
 * Conformance of the library with the format.
 *
 * @since 0.1.0
 */
package io.github.anki4j.conformance;

import io.github.anki4j.adapters.archive.Archive;
import io.github.anki4j.adapters.sqlite.SqliteCollection;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteTypeId;
import io.github.anki4j.domain.Schedule;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.sqlite.SQLiteDataSource;

/**
 * The cases of the format, kept as collections a reader outside this
 * repository can read.
 *
 * @since 0.1.0
 */
final class Cases {

    /**
     * Name of the collection file of a case.
     */
    private static final String FILE = "collection.anki2";

    /**
     * Utility class.
     */
    private Cases() {
    }

    /**
     * A collection without anything in it.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void empty(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertTrue(collection.decks().isEmpty());
            Assertions.assertTrue(collection.notes().isEmpty());
            Assertions.assertTrue(collection.cards().isEmpty());
            Assertions.assertTrue(collection.noteTypes().isEmpty());
        }
        Cases.keeps(directory);
    }

    /**
     * A collection with decks, nested decks included.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void decks(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.editor().createDeck("Default");
            collection.editor().createDeck("Spanish::Verbs");
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(
                List.of("Default", "Spanish::Verbs"),
                collection.decks().stream().map(Deck::getName).toList()
            );
        }
        Cases.keeps(directory);
    }

    /**
     * A collection with notes that carry tags and flags.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void notes(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
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
        Cases.keeps(directory);
    }

    /**
     * A collection with a note type that holds two templates.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void types(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.types().addNoteType(Fixtures.pair());
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.noteTypes().size());
            Assertions.assertEquals(
                2, collection.noteTypes().get(0).getTemplates().size()
            );
        }
        Cases.keeps(directory);
    }

    /**
     * A collection with cards that carry a scheduling state.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void cards(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.types().addNoteType(Fixtures.basic());
            final Note note = collection.editor().addNote(
                new NoteTypeId(1L), List.of("hola", "hello")
            );
            collection.editor().generateCards(note.getId(), new DeckId(1L));
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
        Cases.keeps(directory);
    }

    /**
     * A collection that keeps its models in tables.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be written
     */
    static void tables(final Path directory) throws Exception {
        Files.createDirectories(directory);
        final Path path = directory.resolve(Cases.FILE);
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", path));
        try (Connection connection = source.getConnection();
            Statement statement = connection.createStatement()) {
            for (final String command : Fixtures.tableSchema().split(";")) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(
                Fixtures.TABLE_VERSION, collection.schemaVersion()
            );
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals(1, collection.noteTypes().size());
            Assertions.assertEquals(
                2, collection.noteTypes().get(0).getTemplates().size()
            );
            Assertions.assertEquals(1, collection.notes().size());
            Assertions.assertEquals(1, collection.cards().size());
        }
        Cases.keeps(directory);
    }

    /**
     * The contents of a case survive a save and an export.
     *
     * @param directory Directory of the case
     * @throws Exception When the collection cannot be saved or exported
     */
    private static void keeps(final Path directory) throws Exception {
        final Path source = directory.resolve(Cases.FILE);
        final String report = Report.text(source);
        try (SqliteCollection collection = new SqliteCollection(source)) {
            collection.save(source);
        }
        Assertions.assertEquals(report, Report.text(source));
        final Path exported = Files.createDirectories(
            directory.resolve("export")
        );
        final Path archive = exported.resolve("case.apkg");
        try (SqliteCollection collection = new SqliteCollection(source)) {
            collection.exportArchive(archive, Map.of());
        }
        Assertions.assertEquals(
            report, Report.text(new Archive(archive).collection())
        );
    }
}
