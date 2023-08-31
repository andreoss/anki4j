/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A deck holding cards.
 *
 * @since 0.1.0
 */
@Value
@Builder
public final class Deck {

    /**
     * Identifier of the deck.
     */
    private DeckId id;

    /**
     * Full name of the deck.
     */
    private String name;

    /**
     * Modification time in seconds since the epoch.
     */
    private long modified;

    /**
     * Update sequence number.
     */
    private long usn;

    /**
     * Whether the deck is a filtered deck.
     */
    private boolean dynamic;

    /**
     * Copy of the deck under another name.
     *
     * @param title New name of the deck
     * @return Deck under the name
     */
    public Deck withName(final String title) {
        return new Deck(
            this.id, title, this.modified, this.usn, this.dynamic
        );
    }
}
