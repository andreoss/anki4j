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
 * A note holding the field values of one card template.
 *
 * @since 0.1.0
 */
@Value
@Builder
public final class Note {

    /**
     * Identifier of the note.
     */
    private NoteId id;

    /**
     * Globally unique identifier of the note.
     */
    private String guid;

    /**
     * Identifier of the note type.
     */
    private NoteTypeId notetype;

    /**
     * Modification time in seconds since the epoch.
     */
    private long modified;

    /**
     * Update sequence number.
     */
    private long usn;

    /**
     * Tags of the note.
     */
    private List<String> tags;

    /**
     * Field values in the order of the note type fields.
     */
    private List<String> fields;

    /**
     * Checksum of the first field.
     */
    private long checksum;

    /**
     * Flags of the note.
     */
    private int flags;

    /**
     * Opaque data of the note.
     */
    private String data;
}
