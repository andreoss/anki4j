/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * A field declared by a note type.
 *
 * @since 0.1.0
 */
public final class NoteField {

    /**
     * Name of the field.
     */
    private final String name;

    /**
     * Ordinal of the field inside its note type.
     */
    private final int ordinal;

    /**
     * All fields of the note field.
     *
     * @param name Name of the field
     * @param ordinal Ordinal of the field inside its note type
     */
    public NoteField(final String name, final int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Name of the field.
     *
     * @return Name of the field
     */
    public String getName() {
        return this.name;
    }

    /**
     * Ordinal of the field inside its note type.
     *
     * @return Ordinal of the field
     */
    public int getOrdinal() {
        return this.ordinal;
    }
}
