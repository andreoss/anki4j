/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteType;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the database adapter.
 *
 * @since 0.1.0
 */
final class SqliteCollectionTest {

    /**
     * A collection keeping its models as documents is read.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void readsLegacyLayout() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("legacy"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(11, collection.schemaVersion());
            final Deck firstdeck = collection.decks().get(0);
            Assertions.assertEquals(1L, firstdeck.getId().getValue());
            Assertions.assertEquals("Default", firstdeck.getName());
            Assertions.assertEquals(3L, firstdeck.getModified());
            Assertions.assertEquals(-1L, firstdeck.getUsn());
            Assertions.assertFalse(firstdeck.isDynamic());
            Assertions.assertEquals(
                "Spanish", collection.decks().get(1).getName()
            );
            final NoteType type = collection.noteTypes().get(0);
            Assertions.assertEquals("Basic", type.getName());
            Assertions.assertEquals(2, type.getFields().size());
            Assertions.assertEquals("Front", type.getFields().get(0).getName());
            Assertions.assertEquals("Back", type.getFields().get(1).getName());
            Assertions.assertEquals(1, type.getTemplates().size());
            Assertions.assertEquals(
                "{{Front}}", type.getTemplates().get(0).getQuestion()
            );
            Assertions.assertEquals(
                "{{FrontSide}}", type.getTemplates().get(0).getAnswer()
            );
            final Note note = collection.notes().get(0);
            Assertions.assertEquals("guid-1", note.getGuid());
            Assertions.assertEquals(7L, note.getModified());
            Assertions.assertEquals(
                List.of("greeting", "spanish"), note.getTags()
            );
            Assertions.assertEquals(List.of("hola", "hello"), note.getFields());
            Assertions.assertEquals(12_345L, note.getChecksum());
            final Card card = collection.cards().get(0);
            Assertions.assertEquals(2L, card.getDeck().getValue());
            Assertions.assertEquals(2500, card.getSchedule().getFactor());
            Assertions.assertEquals(
                1, card.getSchedule().getRepetitions()
            );
        }
    }

    /**
     * A collection keeping its models in tables is read.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void readsTabularLayout() throws Exception {
        final Path path = Fixture.tabular(new Work().directory("tabular"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(15, collection.schemaVersion());
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals(
                "Default", collection.decks().get(0).getName()
            );
            Assertions.assertEquals(1, collection.noteTypes().size());
            final NoteType type = collection.noteTypes().get(0);
            Assertions.assertEquals("Basic", type.getName());
            Assertions.assertEquals(2, type.getFields().size());
            Assertions.assertEquals(
                "Card 1", type.getTemplates().get(0).getName()
            );
            Assertions.assertEquals(1, collection.notes().size());
            Assertions.assertEquals(1, collection.cards().size());
        }
    }

    /**
     * An empty collection is created and read.
     *
     * @throws Exception When the collection cannot be created
     */
    @Test
    void createsEmptyCollection() throws Exception {
        final Path path = new Work().directory("empty").resolve("new.anki2");
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            Assertions.assertEquals(11, collection.schemaVersion());
            Assertions.assertTrue(collection.decks().isEmpty());
            Assertions.assertTrue(collection.noteTypes().isEmpty());
            Assertions.assertTrue(collection.notes().isEmpty());
            Assertions.assertTrue(collection.cards().isEmpty());
        }
    }

    /**
     * The tests work inside the build area of the repository.
     *
     * @throws Exception When the work area cannot be created
     */
    @Test
    void worksInsideRepositoryBuildArea() throws Exception {
        final Path directory = new Work().directory("area");
        Assertions.assertTrue(
            directory.toString().contains("target"),
            String.format(
                "test work must stay inside the repository: %s", directory
            )
        );
    }

    /**
     * A file that is not there is reported.
     *
     * @throws Exception When the work area cannot be created
     */
    @Test
    void reportsMissingFile() throws Exception {
        final Path path = new Work().directory("absent").resolve("absent.anki2");
        Assertions.assertThrows(
            Exception.class, () -> new SqliteCollection(path)
        );
    }

    /**
     * A directory that is not there is reported.
     *
     * @throws Exception When the work area cannot be created
     */
    @Test
    void reportsMissingDirectory() throws Exception {
        final Path path = new Work().directory("gone").resolve("absent/new.anki2");
        Assertions.assertThrows(
            Exception.class, () -> new SqliteCollection(path, true)
        );
    }

    /**
     * A file that is not a collection is reported.
     *
     * @throws Exception When the file cannot be written
     */
    @Test
    void reportsFileWithoutCollectionRow() throws Exception {
        final Path path = new Work().directory("plain").resolve("plain.anki2");
        try (Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("create table notes (id integer)");
        }
        Assertions.assertThrows(
            Exception.class, () -> new SqliteCollection(path)
        );
    }

    /**
     * A collection without a header row is reported.
     *
     * @throws Exception When the collection cannot be written
     */
    @Test
    void reportsEmptyCollectionRow() throws Exception {
        final Path path = new Work().directory("emptyrow").resolve("empty.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertEquals(11, created.schemaVersion());
        }
        try (Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from col");
        }
        Assertions.assertThrows(
            Exception.class, () -> new SqliteCollection(path)
        );
    }

    /**
     * A collection without models is read.
     *
     * @throws Exception When the collection cannot be written
     */
    @Test
    void readsCollectionRowWithoutModels() throws Exception {
        final Path path = new Work().directory("bare").resolve("bare.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertEquals(11, created.schemaVersion());
        }
        try (Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                """
                create table col_new (
                    id integer, ver integer, models text, decks text
                )
                """
            );
            statement.executeUpdate(
                "insert into col_new values (1, 11, null, null)"
            );
            statement.executeUpdate("drop table col");
            statement.executeUpdate("alter table col_new rename to col");
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(11, collection.schemaVersion());
            Assertions.assertTrue(collection.decks().isEmpty());
            Assertions.assertTrue(collection.noteTypes().isEmpty());
        }
    }
}
