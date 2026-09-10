/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import java.util.List;

/**
 * A note type generating cards from templates.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
public final class NoteType {

    /**
     * Identifier of the note type.
     */
    private final NoteTypeId id;

    /**
     * Name of the note type.
     */
    private final String name;

    /**
     * Fields declared by the note type.
     */
    private final List<NoteField> fields;

    /**
     * Templates generating the cards of the note type.
     */
    private final List<NoteTemplate> templates;

    /**
     * Checks on the fields of the note type.
     */
    private final List<Check> checks;

    /**
     * Styling of the cards of the note type.
     */
    private final String css;

    /**
     * Ordinal of the field the notes of the type are sorted by.
     */
    private final int sortfield;

    /**
     * Identifier of the deck the cards go to.
     */
    private final DeckId deck;

    /**
     * Modification time in seconds since the epoch.
     */
    private final long modified;

    /**
     * Update sequence number.
     */
    private final long usn;

    /**
     * All fields of the note type.
     *
     * @param id Identifier of the note type
     * @param name Name of the note type
     * @param fields Fields declared by the note type
     * @param templates Templates generating the cards of the note type
     * @param checks Checks on the fields of the note type
     * @param css Styling of the cards of the note type
     * @param sortfield Ordinal of the sorting field
     * @param deck Identifier of the deck the cards go to
     * @param modified Modification time in seconds since the epoch
     * @param usn Update sequence number
     */
    public NoteType(
        final NoteTypeId id,
        final String name,
        final List<NoteField> fields,
        final List<NoteTemplate> templates,
        final List<Check> checks,
        final String css,
        final int sortfield,
        final DeckId deck,
        final long modified,
        final long usn
    ) {
        this.id = id;
        this.name = name;
        this.fields = fields;
        this.templates = templates;
        this.checks = checks;
        this.css = css;
        this.sortfield = sortfield;
        this.deck = deck;
        this.modified = modified;
        this.usn = usn;
    }

    /**
     * Identifier of the note type.
     *
     * @return Identifier of the note type
     */
    public NoteTypeId getId() {
        return this.id;
    }

    /**
     * Name of the note type.
     *
     * @return Name of the note type
     */
    public String getName() {
        return this.name;
    }

    /**
     * Fields declared by the note type.
     *
     * @return Fields of the note type
     */
    public List<NoteField> getFields() {
        return this.fields;
    }

    /**
     * Templates generating the cards of the note type.
     *
     * @return Templates of the note type
     */
    public List<NoteTemplate> getTemplates() {
        return this.templates;
    }

    /**
     * Checks on the fields of the note type.
     *
     * @return Checks of the note type
     */
    public List<Check> getChecks() {
        return this.checks;
    }

    /**
     * Styling of the cards of the note type.
     *
     * @return Css of the note type
     */
    public String getCss() {
        return this.css;
    }

    /**
     * Ordinal of the field the notes of the type are sorted by.
     *
     * @return Sorting field
     */
    public int getSortfield() {
        return this.sortfield;
    }

    /**
     * Identifier of the deck the cards go to.
     *
     * @return Identifier of the deck
     */
    public DeckId getDeck() {
        return this.deck;
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
}
