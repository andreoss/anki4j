/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Check;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The entry of a note type, as the document of the collection keeps it.
 *
 * @since 0.1.0
 */
final class TypeEntry {

    /**
     * The note type of the entry.
     */
    private final NoteType type;

    /**
     * New entry of a note type.
     *
     * @param type The note type of the entry
     */
    TypeEntry(final NoteType type) {
        this.type = type;
    }

    /**
     * Build the entry of the note type.
     *
     * @return Entry of the note type
     */
    Map<String, Object> entry() {
        final Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", this.type.getId().getValue());
        entry.put("name", this.type.getName());
        entry.put("type", 0);
        entry.put("mod", this.type.getModified());
        entry.put("usn", this.type.getUsn());
        entry.put("sortf", this.type.getSortfield());
        entry.put("did", this.deck());
        entry.put("css", this.type.getCss());
        entry.put("flds", this.fields());
        entry.put("tmpls", this.templates());
        entry.put("req", this.checks());
        entry.put("originalStockKind", 1);
        return entry;
    }

    /**
     * Build the fields of the note type.
     *
     * @return Fields of the note type
     */
    private List<Object> fields() {
        final List<Object> fields = new ArrayList<>(0);
        for (final NoteField field : this.type.getFields()) {
            final Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", field.getName());
            entry.put("ord", field.getOrdinal());
            entry.put("sticky", false);
            entry.put("rtl", false);
            entry.put("font", "Arial");
            entry.put("size", 20);
            entry.put("description", "");
            entry.put("plainText", false);
            entry.put("collapsed", false);
            entry.put("excludeFromSearch", false);
            fields.add(entry);
        }
        return fields;
    }

    /**
     * Build the card templates of the note type.
     *
     * @return Card templates of the note type
     */
    private List<Object> templates() {
        final List<Object> templates = new ArrayList<>(0);
        for (final NoteTemplate template : this.type.getTemplates()) {
            final Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", template.getName());
            entry.put("ord", template.getOrdinal());
            entry.put("qfmt", template.getQuestion());
            entry.put("afmt", template.getAnswer());
            entry.put("did", null);
            entry.put("bqfmt", "");
            entry.put("bafmt", "");
            entry.put("bfont", "");
            entry.put("bsize", 0);
            entry.put("bcol", null);
            entry.put("browserCollapsed", false);
            entry.put("id", template.getOrdinal() + 1);
            templates.add(entry);
        }
        return templates;
    }

    /**
     * Build the checks of the note type.
     *
     * @return Checks of the note type
     */
    private List<Object> checks() {
        final List<Object> checks = new ArrayList<>(0);
        for (final Check check : this.type.getChecks()) {
            final List<Object> entry = new ArrayList<>(0);
            entry.add(check.getField());
            entry.add(check.getKind());
            entry.add(check.getValues());
            checks.add(entry);
        }
        return checks;
    }

    /**
     * Deck the new cards of the note type go to.
     *
     * @return Identifier of the deck
     */
    private long deck() {
        long deck = 1L;
        if (this.type.getDeck() != null) {
            deck = this.type.getDeck().getValue();
        }
        return deck;
    }
}
