/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteId;
import java.io.IOException;
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
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    final Deck created = collection.editor().createDeck(
                        DeckOperationTest.DECK
                    );
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            3L, created.getId().getValue()
                        ),
                        () -> Assertions.assertEquals(
                            DeckOperationTest.DECK, created.getName()
                        ),
                        () -> Assertions.assertEquals(
                            3, collection.decks().size()
                        )
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            3, collection.decks().size()
                        ),
                        () -> Assertions.assertEquals(
                            DeckOperationTest.DECK,
                            collection.decks().get(2).getName()
                        )
                    );
                }
            }
        );
    }

    /**
     * A deck is renamed and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void renamesADeck() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("rename"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.editor().renameDeck(
                        new DeckId(2L), "Spanish renamed"
                    );
                    Assertions.assertEquals(
                        "Spanish renamed", collection.decks().get(1).getName()
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertEquals(
                        "Spanish renamed", collection.decks().get(1).getName()
                    );
                }
            }
        );
    }

    /**
     * A deck is deleted and its cards move to the default deck.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void deletesADeck() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("delete"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.editor().deleteDeck(new DeckId(2L));
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            1, collection.decks().size()
                        ),
                        () -> Assertions.assertEquals(
                            1L, collection.cards().get(0).getDeck().getValue()
                        )
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            1, collection.decks().size()
                        ),
                        () -> Assertions.assertEquals(
                            1L, collection.cards().get(0).getDeck().getValue()
                        )
                    );
                }
            }
        );
    }

    /**
     * The cards of a note move to another deck.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void movesANote() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("movenote"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.editor().moveNote(
                        new NoteId(Fixture.NOTE_ID), new DeckId(1L)
                    );
                    Assertions.assertEquals(
                        1L, collection.cards().get(0).getDeck().getValue()
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertEquals(
                        1L, collection.cards().get(0).getDeck().getValue()
                    );
                }
            }
        );
    }

    /**
     * A card moves to another deck.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void movesACard() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("movecard"));
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    collection.editor().moveCard(
                        new CardId(Fixture.CARD_ID), new DeckId(1L)
                    );
                    Assertions.assertEquals(
                        1L, collection.cards().get(0).getDeck().getValue()
                    );
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertEquals(
                        1L, collection.cards().get(0).getDeck().getValue()
                    );
                }
            }
        );
    }

    /**
     * An operation on something that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsMissingItems() throws Exception {
        try (
            SqliteCollection collection = new SqliteCollection(
                Fixture.legacy(new Work().directory("missing"))
            )
        ) {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().renameDeck(new DeckId(99L), "None")
            );
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().deleteDeck(new DeckId(99L))
            );
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().moveNote(new NoteId(99L), new DeckId(1L))
            );
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().moveCard(new CardId(99L), new DeckId(1L))
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
        Assertions.assertAll(
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    final Deck created = collection.editor().createDeck(
                        DeckOperationTest.DECK
                    );
                    Assertions.assertEquals(2L, created.getId().getValue());
                    collection.editor().renameDeck(
                        created.getId(), "French renamed"
                    );
                    Assertions.assertEquals(
                        "French renamed",
                        collection.decks().get(1).getName()
                    );
                    collection.editor().deleteDeck(created.getId());
                    Assertions.assertEquals(1, collection.decks().size());
                    collection.save(path);
                }
            },
            () -> {
                try (SqliteCollection collection = new SqliteCollection(path)) {
                    Assertions.assertAll(
                        () -> Assertions.assertEquals(
                            1, collection.decks().size()
                        ),
                        () -> Assertions.assertEquals(
                            "Default", collection.decks().get(0).getName()
                        )
                    );
                }
            }
        );
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
            IOException.class,
            () -> collection.save(path.resolve("absent").resolve("new.anki2"))
        );
    }
}
