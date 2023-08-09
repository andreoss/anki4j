/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.DeckId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Writer of the deck of a card.
 *
 * @since 0.1.0
 */
final class MoveStore {

    /**
     * Utility class.
     */
    private MoveStore() {
    }

    /**
     * Write the deck of a card.
     *
     * @param connection Connection to the database file
     * @param id Identifier of the card
     * @param deck Identifier of the deck
     */
    static void movecard(
        final Connection connection,
        final CardId id,
        final DeckId deck
    ) {
        try (PreparedStatement statement = connection.prepareStatement(
            "update cards set did = ? where id = ?"
        )) {
            statement.setLong(1, deck.getValue());
            statement.setLong(2, id.getValue());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot move the card", error);
        }
    }
}
