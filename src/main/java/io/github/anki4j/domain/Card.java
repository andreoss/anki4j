/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * A card generated from a note by one card template.
 *
 * @since 0.1.0
 */
@SuppressWarnings({"PMD.DataClass", "PMD.TooManyMethods"})
public final class Card {

    /**
     * Identifier of the card.
     */
    private final CardId id;

    /**
     * Identifier of the note the card belongs to.
     */
    private final NoteId note;

    /**
     * Identifier of the deck holding the card.
     */
    private final DeckId deck;

    /**
     * Ordinal of the card template inside its note type.
     */
    private final int ordinal;

    /**
     * Modification time in seconds since the epoch.
     */
    private final long modified;

    /**
     * Update sequence number.
     */
    private final long usn;

    /**
     * Scheduling state of the card.
     */
    private final Schedule schedule;

    /**
     * Flags of the card.
     */
    private final int flags;

    /**
     * Opaque data of the card.
     */
    private final String data;

    /**
     * All fields of the card.
     *
     * @param id Identifier of the card
     * @param note Identifier of the note the card belongs to
     * @param deck Identifier of the deck holding the card
     * @param ordinal Ordinal of the card template inside its note type
     * @param modified Modification time in seconds since the epoch
     * @param usn Update sequence number
     * @param schedule Scheduling state of the card
     * @param flags Flags of the card
     * @param data Opaque data of the card
     */
    public Card(
        final CardId id,
        final NoteId note,
        final DeckId deck,
        final int ordinal,
        final long modified,
        final long usn,
        final Schedule schedule,
        final int flags,
        final String data
    ) {
        this.id = id;
        this.note = note;
        this.deck = deck;
        this.ordinal = ordinal;
        this.modified = modified;
        this.usn = usn;
        this.schedule = schedule;
        this.flags = flags;
        this.data = data;
    }

    /**
     * Identifier of the card.
     *
     * @return Identifier of the card
     */
    public CardId getId() {
        return this.id;
    }

    /**
     * Identifier of the note the card belongs to.
     *
     * @return Identifier of the note
     */
    public NoteId getNote() {
        return this.note;
    }

    /**
     * Identifier of the deck holding the card.
     *
     * @return Identifier of the deck
     */
    public DeckId getDeck() {
        return this.deck;
    }

    /**
     * Ordinal of the card template inside its note type.
     *
     * @return Ordinal of the template
     */
    public int getOrdinal() {
        return this.ordinal;
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
     * Scheduling state of the card.
     *
     * @return Schedule of the card
     */
    public Schedule getSchedule() {
        return this.schedule;
    }

    /**
     * Flags of the card.
     *
     * @return Flags of the card
     */
    public int getFlags() {
        return this.flags;
    }

    /**
     * Opaque data of the card.
     *
     * @return Data of the card
     */
    public String getData() {
        return this.data;
    }

    /**
     * Copy of the card with another scheduling state.
     *
     * @param state Scheduling state
     * @return Card with the scheduling state
     */
    public Card withSchedule(final Schedule state) {
        return new Card(
            this.id, this.note, this.deck, this.ordinal, this.modified,
            this.usn, state, this.flags, this.data
        );
    }

    /**
     * Copy of the card with another deck.
     *
     * @param target Identifier of the deck
     * @return Card with the deck
     */
    public Card withDeck(final DeckId target) {
        return new Card(
            this.id, this.note, target, this.ordinal, this.modified,
            this.usn, this.schedule, this.flags, this.data
        );
    }
}
