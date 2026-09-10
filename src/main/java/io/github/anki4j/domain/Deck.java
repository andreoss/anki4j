/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * A deck holding cards.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class Deck {

    /**
     * Identifier of the deck.
     */
    private final DeckId id;

    /**
     * Name of the deck.
     */
    private final String name;

    /**
     * Modification time in seconds since the epoch.
     */
    private final long modified;

    /**
     * Update sequence number.
     */
    private final long usn;

    /**
     * Whether the deck is filtered.
     */
    private final boolean dynamic;

    /**
     * All fields of the deck.
     *
     * @param id Identifier of the deck
     * @param name Name of the deck
     * @param modified Modification time in seconds since the epoch
     * @param usn Update sequence number
     * @param dynamic Whether the deck is filtered
     */
    public Deck(
        final DeckId id, final String name, final long modified,
        final long usn, final boolean dynamic
    ) {
        this.id = id;
        this.name = name;
        this.modified = modified;
        this.usn = usn;
        this.dynamic = dynamic;
    }

    /**
     * Identifier of the deck.
     *
     * @return Identifier of the deck
     */
    public DeckId getId() {
        return this.id;
    }

    /**
     * Name of the deck.
     *
     * @return Name of the deck
     */
    public String getName() {
        return this.name;
    }

    /**
     * Modification time in seconds since the epoch.
     *
     * @return Modification time
     */
    public long getModified() {
        return this.modified;
    }

    /**
     * Update sequence number.
     *
     * @return Update sequence number
     */
    public long getUsn() {
        return this.usn;
    }

    /**
     * Whether the deck is filtered.
     *
     * @return True when the deck is filtered
     */
    public boolean isDynamic() {
        return this.dynamic;
    }

    /**
     * Copy of the deck with another name.
     *
     * @param title Name of the deck
     * @return Deck with the name
     */
    public Deck withName(final String title) {
        return new Deck(
            this.id, title, this.modified, this.usn, this.dynamic
        );
    }
}
