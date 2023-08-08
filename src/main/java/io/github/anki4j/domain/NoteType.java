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
     * Modification time in seconds since the epoch.
     */
    private long modified;

    /**
     * Update sequence number.
     */
    private long usn;
}
