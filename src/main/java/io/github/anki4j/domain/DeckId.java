/**
 * Identifier of a model value.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Value;

/**
 * Identifier of a Deck.
 *
 * @since 0.1.0
 */
@Value
public final class DeckId {

    /**
     * Raw identifier.
     */
    private long value;
}
