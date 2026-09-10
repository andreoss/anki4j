/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

/**
 * A card template of a note type.
 *
 * @param name Name of the template
 * @param ordinal Ordinal of the template inside its note type
 * @param question Question text of the template
 * @param answer Answer text of the template
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
public record NoteTemplate(
    String name,
    int ordinal,
    String question,
    String answer
) {

    /**
     * Name of the template.
     *
     * @return Name of the template
     */
    public String getName() {
        return this.name;
    }

    /**
     * Ordinal of the template inside its note type.
     *
     * @return Ordinal of the template
     */
    public int getOrdinal() {
        return this.ordinal;
    }

    /**
     * Question text of the template.
     *
     * @return Question text
     */
    public String getQuestion() {
        return this.question;
    }

    /**
     * Answer text of the template.
     *
     * @return Answer text
     */
    public String getAnswer() {
        return this.answer;
    }
}
