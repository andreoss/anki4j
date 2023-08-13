/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.Schedule;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Writer of the cards of a collection file.
 *
 * @since 0.1.0
 */
final class CardOps {

    /**
     * Utility class.
     */
    private CardOps() {
    }

    /**
     * Identifier of the card added next.
     *
     * @param cards Cards of the collection
     * @return Identifier, one above the highest in use
     */
    static long nextid(final List<Card> cards) {
        long next = 1L;
        for (final Card card : cards) {
            if (card.getId().getValue() >= next) {
                next = card.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * Position of a new card in the queue of the new cards.
     *
     * @param cards Cards of the collection
     * @return Position of the card
     */
    static long position(final List<Card> cards) {
        long position = 0L;
        for (final Card card : cards) {
            if (card.getSchedule().getQueue() == 0) {
                position += 1L;
            }
        }
        return position;
    }

    /**
     * The card of a note and a template.
     *
     * @param cards Cards of the collection
     * @param note Identifier of the note
     * @param ordinal Ordinal of the template
     * @return Card of the note, null when it is not there
     */
    static Card find(
        final List<Card> cards,
        final NoteId note,
        final int ordinal
    ) {
        Card found = null;
        for (final Card card : cards) {
            if (card.getNote().getValue() == note.getValue()
                && card.getOrdinal() == ordinal) {
                found = card;
            }
        }
        return found;
    }

    /**
     * Write a card.
     *
     * @param connection Connection to the database file
     * @param card Card to write
     */
    static void write(final Connection connection, final Card card) {
        try (PreparedStatement statement = connection.prepareStatement(
            """
            insert into cards (
                id, nid, did, ord, mod, usn, type, queue, due, ivl, factor,
                reps, lapses, left, odue, odid, flags, data
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """
        )) {
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
}
