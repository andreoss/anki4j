/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the deck operations of a collection.
 *
 * @since 0.1.0
 */
final class DeckOperationTest {

    /**
     * Name of the deck the tests create.
     */
    private static final String DECK = "French";

    /**
     * A deck is created and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void createsADeck() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("create"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Deck created = collection.createDeck(DeckOperationTest.DECK);
            Assertions.assertEquals(3L, created.getId().getValue());
            Assertions.assertEquals(DeckOperationTest.DECK, created.getName());
            Assertions.assertEquals(3, collection.decks().size());
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(3, collection.decks().size());
            Assertions.assertEquals(DeckOperationTest.DECK, collection.decks().get(2).getName());
        }
    }

    /**
     * A deck is renamed and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void renamesADeck() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("rename"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.renameDeck(new DeckId(2L), "Spanish renamed");
            Assertions.assertEquals(
                "Spanish renamed", collection.decks().get(1).getName()
            );
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(
                "Spanish renamed", collection.decks().get(1).getName()
            );
        }
    }

    /**
     * A deck is deleted and its cards move to the default deck.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void deletesADeck() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("delete"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.deleteDeck(new DeckId(2L));
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals(1L, collection.cards().get(0).getDeck().getValue());
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals(1L, collection.cards().get(0).getDeck().getValue());
        }
    }

    /**
     * An operation on something that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsMissingItems() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("missing"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.renameDeck(new DeckId(99L), "None")
            );
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.deleteDeck(new DeckId(99L))
            );
        }
    }

    /**
     * A deck is created in a collection that keeps its models in tables.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void createsADeckInTables() throws Exception {
        final Path path = Fixture.tabular(new Work().directory("tables"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Deck created = collection.createDeck(DeckOperationTest.DECK);
            Assertions.assertEquals(2L, created.getId().getValue());
            collection.renameDeck(created.getId(), "French renamed");
            Assertions.assertEquals(
                "French renamed", collection.decks().get(1).getName()
            );
            collection.deleteDeck(created.getId());
            Assertions.assertEquals(1, collection.decks().size());
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(1, collection.decks().size());
            Assertions.assertEquals("Default", collection.decks().get(0).getName());
        }
    }

    /**
     * A change on a closed file is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAClosedFile() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("closed"));
        final SqliteCollection collection = new SqliteCollection(path);
        collection.close();
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> collection.createDeck(DeckOperationTest.DECK)
        );
    }
}
