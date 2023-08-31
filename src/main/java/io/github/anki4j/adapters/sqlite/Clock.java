/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.util.function.LongSupplier;

/**
 * The clock of a collection, which tells the time in seconds.
 *
 * @since 0.1.0
 */
final class Clock {

    /**
     * Source of the time.
     */
    private final LongSupplier source;

    /**
     * New clock.
     *
     * @param source Source of the time, in milliseconds
     */
    Clock(final LongSupplier source) {
        this.source = source;
    }

    /**
     * The time of the clock now.
     *
     * @return Seconds since the epoch
     */
    long now() {
        return this.source.getAsLong() / 1000L;
    }
}
