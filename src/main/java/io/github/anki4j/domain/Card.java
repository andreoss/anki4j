/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A card generated from a note by one card template.
 *
 * @since 0.1.0
 */
@Value
@Builder
public class Card {

    /**
     * Identifier of the card.
     */
    private CardId id;

    /**
     * Identifier of the note the card belongs to.
     */
    private NoteId note;

    /**
     * Identifier of the deck holding the card.
     */
    private DeckId deck;

    /**
     * Ordinal of the card template inside its note type.
     */
    private int ordinal;

    /**
     * Modification time in seconds since the epoch.
     */
    private long modified;

    /**
     * Update sequence number.
     */
    private long usn;

    /**
     * Scheduling state of the card.
     */
    private Schedule schedule;

    /**
     * Flags of the card.
     */
    private int flags;

    /**
     * Opaque data of the card.
     */
    private String data;
}
