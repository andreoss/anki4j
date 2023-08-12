/**
 * Port of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.ports;

import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;

/**
 * The note types of a collection.
 *
 * @since 0.1.0
 */
public interface NoteTypes {

    /**
     * Add a note type to the collection.
     *
     * @param type Note type to add
     * @return Note type added
     */
    NoteType addNoteType(NoteType type);

    /**
     * Write the fields, the checks, the templates and the styling of a
     * note type.
     *
     * @param type Note type with its new values
     */
    void updateNoteType(NoteType type);

    /**
     * Remove a note type.
     *
     * @param id Identifier of the note type
     */
    void deleteNoteType(NoteTypeId id);
}
