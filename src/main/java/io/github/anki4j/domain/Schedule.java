/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Builder;
import lombok.Value;

/**
 * Scheduling state of a card.
 *
 * @since 0.1.0
 */
@Value
@Builder
public class Schedule {

    /**
     * Type of the card.
     */
    private int type;

    /**
     * Queue the card is in.
     */
    private int queue;

    /**
     * Due date or position of the card.
     */
    private long due;

    /**
     * Interval in days between reviews.
     */
    private long interval;

    /**
     * Scheduling factor of the card.
     */
    private int factor;

    /**
     * Number of reviews.
     */
    private int repetitions;

    /**
     * Number of lapses.
     */
    private int lapses;

    /**
     * Number of reviews left in the current day.
     */
    private int left;

    /**
     * Original due date of the card.
     */
    private long originaldue;

    /**
     * Identifier of the original deck of the card.
     */
    private long originaldeck;
}
