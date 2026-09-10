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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * The cards of a collection.
 *
 * @since 0.1.0
 */
final class Cards {

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Cards of the collection.
     */
    private final List<Card> cardlist;

    /**
     * New cards of a collection.
     *
     * @param connection Connection to the database file
     * @param cards Cards of the collection
     */
    Cards(final Connection connection, final List<Card> cards) {
        this.connection = connection;
        this.cardlist = cards;
    }

    /**
     * Read the cards of the collection.
     *
     * @return Cards, in identifier order
     * @throws SQLException When the cards cannot be read
     */
    List<Card> read() throws SQLException {
        final List<Card> found = new ArrayList<>(0);
        try (
            Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(
                """
                select id, nid, did, ord, mod, usn, type, queue, due, ivl,
                    factor, reps, lapses, left, odue, odid, flags, data
                from cards
                order by id
                """
            )
        ) {
            while (rows.next()) {
                found.add(
                    new Card(
                        new CardId(rows.getLong("id")),
                        new NoteId(rows.getLong("nid")),
                        new DeckId(rows.getLong("did")),
                        rows.getInt("ord"),
                        rows.getLong("mod"),
                        rows.getLong("usn"),
                        new Schedule(
                            rows.getInt("type"),
                            rows.getInt("queue"),
                            rows.getLong("due"),
                            rows.getLong("ivl"),
                            rows.getInt("factor"),
                            rows.getInt("reps"),
                            rows.getInt("lapses"),
                            rows.getInt("left"),
                            rows.getLong("odue"),
                            rows.getLong("odid")
                        ),
                        rows.getInt("flags"),
                        rows.getString("data")
                    )
                );
            }
        }
        return found;
    }

    /**
     * Write a card.
     *
     * @param card Card to write
     */
    void write(final Card card) {
        try (
            PreparedStatement statement = this.connection.prepareStatement(
                """
                insert into cards (
                    id, nid, did, ord, mod, usn, type, queue, due, ivl, factor,
                    reps, lapses, left, odue, odid, flags, data
                ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """
            )
        ) {
            final Schedule schedule = card.getSchedule();
            statement.setLong(1, card.getId().getValue());
            statement.setLong(2, card.getNote().getValue());
            statement.setLong(3, card.getDeck().getValue());
            statement.setInt(4, card.getOrdinal());
            statement.setLong(5, card.getModified());
            statement.setLong(6, card.getUsn());
            statement.setInt(7, schedule.getType());
            statement.setInt(8, schedule.getQueue());
            statement.setLong(9, schedule.getDue());
            statement.setLong(10, schedule.getInterval());
            statement.setInt(11, schedule.getFactor());
            statement.setInt(12, schedule.getRepetitions());
            statement.setInt(13, schedule.getLapses());
            statement.setInt(14, schedule.getLeft());
            statement.setLong(15, schedule.getOriginaldue());
            statement.setLong(16, schedule.getOriginaldeck());
            statement.setInt(17, card.getFlags());
            statement.setString(18, card.getData());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException("cannot write the card", error);
        }
    }

    /**
     * Identifier of the card added next.
     *
     * @return Identifier, one above the highest in use
     */
    long nextid() {
        long next = 1L;
        for (final Card card : this.cardlist) {
            if (card.getId().getValue() >= next) {
                next = card.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * Position of a new card in the queue of the new cards.
     *
     * @return Position of the card
     */
    long position() {
        long position = 0L;
        for (final Card card : this.cardlist) {
            if (card.getSchedule().getQueue() == 0) {
                position += 1L;
            }
        }
        return position;
    }

    /**
     * The card of a note and a template.
     *
     * @param note Identifier of the note
     * @param ordinal Ordinal of the template
     * @return Card of the note, null when it is not there
     */
    Card find(final NoteId note, final int ordinal) {
        Card found = null;
        for (final Card card : this.cardlist) {
            if (card.getNote().getValue() == note.getValue()
                && card.getOrdinal() == ordinal) {
                found = card;
            }
        }
        return found;
    }
}
