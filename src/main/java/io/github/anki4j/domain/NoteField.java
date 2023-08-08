/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A field of a note type.
 *
 * @since 0.1.0
 */
@Value
@Builder
public class NoteField {

    /**
     * Name of the field.
     */
    private String name;

    /**
     * Ordinal of the field inside its note type.
     */
    private int ordinal;
}
