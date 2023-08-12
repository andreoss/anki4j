/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * A note type describing the fields and the card templates of its notes.
 *
 * @since 0.1.0
 */
@Value
@Builder
public class NoteType {

    /**
     * Identifier of the note type.
     */
    private NoteTypeId id;

    /**
     * Name of the note type.
     */
    private String name;

    /**
     * Fields in ordinal order.
     */
    private List<NoteField> fields;

    /**
     * Card templates in ordinal order.
     */
    private List<NoteTemplate> templates;

    /**
     * Checks on the fields, in the order the format keeps them.
     */
    private List<Check> checks;

    /**
     * Styling of the cards.
     */
    private String css;

    /**
     * Ordinal of the field the notes are sorted by.
     */
    private int sortfield;

    /**
     * Identifier of the deck the new cards go to.
     */
    private DeckId deck;

    /**
     * Modification time in seconds since the epoch.
     */
    private long modified;

    /**
     * Update sequence number.
     */
    private long usn;
}
