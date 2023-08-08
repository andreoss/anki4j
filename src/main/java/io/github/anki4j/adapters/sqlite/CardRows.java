/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.Schedule;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader of the cards of a collection file.
 *
 * @since 0.1.0
 */
final class CardRows {

    /**
     * Utility class.
     */
    private CardRows() {
    }

    /**
     * Read the cards of the collection.
     *
     * @param connection Connection to the database file
     * @return Cards, in identifier order
     * @throws SQLException When the cards cannot be read
     */
    static List<Card> read(final Connection connection) throws SQLException {
        final List<Card> cards = new ArrayList<>(0);
        final String query = """
            select id, nid, did, ord, mod, usn, type, queue, due, ivl,
                factor, reps, lapses, left, odue, odid, flags, data
            from cards
            order by id
            """;
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                cards.add(
                    Card.builder()
                        .id(new CardId(rows.getLong("id")))
                        .note(new NoteId(rows.getLong("nid")))
                        .deck(new DeckId(rows.getLong("did")))
                        .ordinal(rows.getInt("ord"))
                        .modified(rows.getLong("mod"))
                        .usn(rows.getLong("usn"))
                        .schedule(
                            Schedule.builder()
                                .type(rows.getInt("type"))
                                .queue(rows.getInt("queue"))
                                .due(rows.getLong("due"))
                                .interval(rows.getLong("ivl"))
                                .factor(rows.getInt("factor"))
                                .repetitions(rows.getInt("reps"))
                                .lapses(rows.getInt("lapses"))
                                .left(rows.getInt("left"))
                                .originaldue(rows.getLong("odue"))
                                .originaldeck(rows.getLong("odid"))
                                .build()
                        )
                        .flags(rows.getInt("flags"))
                        .data(rows.getString("data"))
                        .build()
                );
            }
        }
        return cards;
    }
}
