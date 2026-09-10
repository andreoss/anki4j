/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import java.util.List;

/**
 * A note holding the field values of one card template.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class Note {

    /**
     * Identifier of the note.
     */
    private final NoteId id;

    /**
     * Globally unique identifier of the note.
     */
    private final String guid;

    /**
     * Identifier of the note type.
     */
    private final NoteTypeId notetype;

    /**
     * Modification time in seconds since the epoch.
     */
    private final long modified;

    /**
     * Update sequence number.
     */
    private final long usn;

    /**
     * Tags of the note.
     */
    private final List<String> tags;

    /**
     * Field values in the order of the note type fields.
     */
    private final List<String> fields;

    /**
     * Checksum of the first field.
     */
    private final long checksum;

    /**
     * Flags of the note.
     */
    private final int flags;

    /**
     * Opaque data of the note.
     */
    private final String data;

    /**
     * All fields of the note.
     *
     * @param id Identifier of the note
     * @param guid Globally unique identifier of the note
     * @param notetype Identifier of the note type
     * @param modified Modification time in seconds since the epoch
     * @param usn Update sequence number
     * @param tags Tags of the note
     * @param fields Field values in the order of the note type fields
     * @param checksum Checksum of the first field
     * @param flags Flags of the note
     * @param data Opaque data of the note
     */
    public Note(
        final NoteId id,
        final String guid,
        final NoteTypeId notetype,
        final long modified,
        final long usn,
        final List<String> tags,
        final List<String> fields,
        final long checksum,
        final int flags,
        final String data
    ) {
        this.id = id;
        this.guid = guid;
        this.notetype = notetype;
        this.modified = modified;
        this.usn = usn;
        this.tags = tags;
        this.fields = fields;
        this.checksum = checksum;
        this.flags = flags;
        this.data = data;
    }

    /**
     * Identifier of the note.
     *
     * @return Identifier of the note
     */
    public NoteId getId() {
        return this.id;
    }

    /**
     * Globally unique identifier of the note.
     *
     * @return Guid of the note
     */
    public String getGuid() {
        return this.guid;
    }

    /**
     * Identifier of the note type.
     *
     * @return Identifier of the note type
     */
    public NoteTypeId getNotetype() {
        return this.notetype;
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
     * Tags of the note.
     *
     * @return Tags of the note
     */
    public List<String> getTags() {
        return this.tags;
    }

    /**
     * Field values in the order of the note type fields.
     *
     * @return Fields of the note
     */
    public List<String> getFields() {
        return this.fields;
    }

    /**
     * Checksum of the first field.
     *
     * @return Checksum of the note
     */
    public long getChecksum() {
        return this.checksum;
    }

    /**
     * Flags of the note.
     *
     * @return Flags of the note
     */
    public int getFlags() {
        return this.flags;
    }

    /**
     * Opaque data of the note.
     *
     * @return Data of the note
     */
    public String getData() {
        return this.data;
    }
}
