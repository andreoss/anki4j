/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import lombok.Builder;
import lombok.Value;

/**
 * A card template of a note type.
 *
 * @since 0.1.0
 */
@Value
@Builder
public final class NoteTemplate {

    /**
     * Name of the card template.
     */
    private String name;

    /**
     * Ordinal of the card template inside its note type.
     */
    private int ordinal;

    /**
     * Question format of the card.
     */
    private String question;

    /**
     * Answer format of the card.
     */
    private String answer;
}
