/**
 * Identifier of a model value.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * Identifier of a Card.
 *
 * @since 0.1.0
 */
public final class CardId {

    /**
     * Raw identifier.
     */
    private final long value;

    /**
     * All fields of the identifier.
     *
     * @param value Raw identifier
     */
    public CardId(final long value) {
        this.value = value;
    }

    /**
     * Raw identifier.
     *
     * @return Raw identifier
     */
    public long getValue() {
        return this.value;
    }
}
