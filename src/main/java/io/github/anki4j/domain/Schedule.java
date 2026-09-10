/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * Scheduling state of a card.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class Schedule {

    /**
     * Type of the card.
     */
    private final int type;

    /**
     * Queue the card is in.
     */
    private final int queue;

    /**
     * Due date or position of the card.
     */
    private final long due;

    /**
     * Interval in days between reviews.
     */
    private final long interval;

    /**
     * Scheduling factor of the card.
     */
    private final int factor;

    /**
     * Number of reviews.
     */
    private final int repetitions;

    /**
     * Number of lapses.
     */
    private final int lapses;

    /**
     * Number of reviews left in the current day.
     */
    private final int left;

    /**
     * Original due date of the card.
     */
    private final long originaldue;

    /**
     * Identifier of the original deck of the card.
     */
    private final long originaldeck;

    /**
     * All fields of the scheduling state.
     *
     * @param type Type of the card
     * @param queue Queue the card is in
     * @param due Due date or position of the card
     * @param interval Interval in days between reviews
     * @param factor Scheduling factor of the card
     * @param repetitions Number of reviews
     * @param lapses Number of lapses
     * @param left Number of reviews left in the current day
     * @param originaldue Original due date of the card
     * @param originaldeck Identifier of the original deck of the card
     */
    public Schedule(
        final int type, final int queue, final long due,
        final long interval, final int factor, final int repetitions,
        final int lapses, final int left, final long originaldue,
        final long originaldeck
    ) {
        this.type = type;
        this.queue = queue;
        this.due = due;
        this.interval = interval;
        this.factor = factor;
        this.repetitions = repetitions;
        this.lapses = lapses;
        this.left = left;
        this.originaldue = originaldue;
        this.originaldeck = originaldeck;
    }

    /**
     * Type of the card.
     *
     * @return Type of the card
     */
    public int getType() {
        return this.type;
    }

    /**
     * Queue the card is in.
     *
     * @return Queue of the card
     */
    public int getQueue() {
        return this.queue;
    }

    /**
     * Due date or position of the card.
     *
     * @return Due of the card
     */
    public long getDue() {
        return this.due;
    }

    /**
     * Interval in days between reviews.
     *
     * @return Interval of the card
     */
    public long getInterval() {
        return this.interval;
    }

    /**
     * Scheduling factor of the card.
     *
     * @return Factor of the card
     */
    public int getFactor() {
        return this.factor;
    }

    /**
     * Number of reviews.
     *
     * @return Repetitions of the card
     */
    public int getRepetitions() {
        return this.repetitions;
    }

    /**
     * Number of lapses.
     *
     * @return Lapses of the card
     */
    public int getLapses() {
        return this.lapses;
    }

    /**
     * Number of reviews left in the current day.
     *
     * @return Reviews left of the card
     */
    public int getLeft() {
        return this.left;
    }

    /**
     * Original due date of the card.
     *
     * @return Original due of the card
     */
    public long getOriginaldue() {
        return this.originaldue;
    }

    /**
     * Identifier of the original deck of the card.
     *
     * @return Original deck of the card
     */
    public long getOriginaldeck() {
        return this.originaldeck;
    }
}
