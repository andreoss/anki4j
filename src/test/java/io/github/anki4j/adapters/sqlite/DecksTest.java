/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the decks of a collection.
 *
 * @since 0.1.0
 */
final class DecksTest {

    /**
     * The document of the decks holds every deck.
     */
    @Test
    void writesTheDocument() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            final String document = new Decks(
                connection,
                List.of(
                    new Deck(new DeckId(1L), "Default", 3L, -1L, false),
                    new Deck(new DeckId(2L), "Filtered", 4L, -1L, true)
                ),
                new Document("{}")
            ).text();
            Assertions.assertAll(
                () -> Assertions.assertTrue(document.contains("\"Default\"")),
                () -> Assertions.assertTrue(document.contains("\"Filtered\"")),
                () -> Assertions.assertTrue(document.contains("\"dyn\":1")),
                () -> Assertions.assertTrue(document.contains("\"dyn\":0"))
            );
        }
    }

    /**
     * The decks of a document are read.
     */
    @Test
    void readsDecks() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            final List<Deck> decks = new Decks(
                connection, new ArrayList<>(0), new Document(Fixture.DECKS)
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(2, decks.size()),
                () -> Assertions.assertEquals("Default", decks.get(0).getName()),
                () -> Assertions.assertEquals(2L, decks.get(1).getId().getValue())
            );
        }
    }

    /**
     * A document that holds no deck holds none.
     */
    @Test
    void readsEmptyInput() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            Assertions.assertAll(
                () -> Assertions.assertTrue(
                    new Decks(
                        connection, new ArrayList<>(0), new Document((String) null)
                    ).read(11).isEmpty()
                ),
                () -> Assertions.assertTrue(
                    new Decks(
                        connection, new ArrayList<>(0), new Document("  ")
                    ).read(11).isEmpty()
                )
            );
        }
    }

    /**
     * A document that is not an object holds no deck.
     */
    @Test
    void readsNonObjectInput() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            Assertions.assertTrue(
                new Decks(
                    connection,
                    new ArrayList<>(0),
                    new Document("[]")
                ).read(11).isEmpty()
            );
        }
    }

    /**
     * A deck without values falls back to the empty values.
     */
    @Test
    void defaultsMissingValues() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            final List<Deck> decks = new Decks(
                connection, new ArrayList<>(0), new Document("{\"1\": {}}")
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals("", decks.get(0).getName()),
                () -> Assertions.assertEquals(0L, decks.get(0).getModified())
            );
        }
    }

    /**
     * A filtered deck is marked as such.
     */
    @Test
    void readsDynamicDeck() throws SQLException {
        try (Connection connection = DecksTest.connect()) {
            Assertions.assertTrue(
                new Decks(
                    connection,
                    new ArrayList<>(0),
                    new Document("{\"1\": {\"id\": 1, \"name\": \"Filtered\", \"dyn\": 1}}")
                ).read(11).get(0).isDynamic()
            );
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite::memory:");
    }
}
