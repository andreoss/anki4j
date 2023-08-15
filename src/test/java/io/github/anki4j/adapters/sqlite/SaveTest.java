/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the save of a collection.
 *
 * @since 0.1.0
 */
final class SaveTest {

    /**
     * Name of the deck the tests create.
     */
    private static final String DECK = "French";

    /**
     * Suffix of the file the collection is written to first.
     */
    private static final String SUFFIX = ".tmp";

    /**
     * The saved file holds the changes of the collection.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void savesTheCollection() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("save"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.editor().createDeck(SaveTest.DECK);
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(3, collection.decks().size());
            Assertions.assertEquals(SaveTest.DECK, collection.decks().get(2).getName());
        }
    }

    /**
     * The collection that keeps its models in tables is saved as well.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void savesATabularCollection() throws Exception {
        final Path path = Fixture.tabular(new Work().directory("tabularsave"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.editor().createDeck(SaveTest.DECK);
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(2, collection.decks().size());
            Assertions.assertEquals(SaveTest.DECK, collection.decks().get(1).getName());
            Assertions.assertEquals(1, collection.noteTypes().size());
        }
    }

    /**
     * A collection that is not there yet is written.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void savesANewFile() throws Exception {
        final Path path = new Work().directory("newfile").resolve("new.anki2");
        try (SqliteCollection collection = new SqliteCollection(path, true)) {
            collection.editor().createDeck(SaveTest.DECK);
            collection.save(path);
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals(SaveTest.DECK, collection.decks().get(0).getName());
        }
    }

    /**
     * The review log of the collection survives the save.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void keepsTheReviewLog() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("revlog"));
        try (java.sql.Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate(
                """
                insert into revlog values (
                    1, 1800000000000, -1, 3, 10, 0, 2500, 100, 0
                )
                """
            );
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.save(path);
        }
        try (java.sql.Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement();
            var rows = statement.executeQuery(
                "select count(*) from revlog"
            )) {
            Assertions.assertTrue(rows.next());
            Assertions.assertEquals(1L, rows.getLong(1));
        }
    }

    /**
     * The file the collection is written to first is gone after the save.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void removesTheTemporaryFile() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("temporary"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.save(path);
            Assertions.assertFalse(
                Files.exists(
                    path.resolveSibling(
                        path.getFileName() + SaveTest.SUFFIX
                    )
                )
            );
        }
    }

    /**
     * A save that cannot be written leaves the file as it was.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void keepsTheFileWhenTheSaveFails() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("failed"));
        final byte[] before = Files.readAllBytes(path);
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.editor().createDeck(SaveTest.DECK);
            Assertions.assertThrows(
                IOException.class,
                () -> collection.save(
                    path.resolve("absent").resolve("new.anki2")
                )
            );
        }
        Assertions.assertArrayEquals(before, Files.readAllBytes(path));
    }
}
