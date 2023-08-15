/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.github.anki4j.domain.Check;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Writer of the note types of a collection file.
 *
 * @since 0.1.0
 */
final class TypeOps {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Utility class.
     */
    private TypeOps() {
    }

    /**
     * Write the note types of a collection.
     *
     * @param connection Connection to the database file
     * @param version Schema version of the collection
     * @param types Note types of the collection
     */
    static void write(
        final Connection connection,
        final int version,
        final List<NoteType> types
    ) {
        try (PreparedStatement statement = connection.prepareStatement(
            "update col set models = ?"
        )) {
            statement.setString(1, TypeOps.document(types));
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException(
                "cannot write the note types", error
            );
        }
    }

    /**
     * Identifier of the note type added next.
     *
     * @param types Note types of the collection
     * @return Identifier, one above the highest in use
     */
    static long nextid(final List<NoteType> types) {
        long next = 1L;
        for (final NoteType type : types) {
            if (type.getId().getValue() >= next) {
                next = type.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * The note type of a collection.
     *
     * @param types Note types of the collection
     * @param id Identifier of the note type
     * @return Note type of the collection
     */
    static NoteType find(final List<NoteType> types, final NoteTypeId id) {
        for (final NoteType type : types) {
            if (type.getId().getValue() == id.getValue()) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note type with id %d", id.getValue())
        );
    }

    /**
     * Build the document of the note types of a collection.
     *
     * @param types Note types of the collection
     * @return Document of the note types
     */
    static String document(final List<NoteType> types) {
        final Map<String, Object> document = new LinkedHashMap<>();
        for (final NoteType type : types) {
            document.put(
                String.valueOf(type.getId().getValue()),
                TypeOps.entry(type)
            );
        }
        return TypeOps.TYPE.toJson(document);
    }

    /**
     * Build the entry of one note type.
     *
     * @param type Note type
     * @return Entry of the note type
     */
    private static Map<String, Object> entry(final NoteType type) {
        final Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("id", type.getId().getValue());
        entry.put("name", type.getName());
        entry.put("type", 0);
        entry.put("mod", type.getModified());
        entry.put("usn", type.getUsn());
        entry.put("sortf", type.getSortfield());
        entry.put("did", TypeOps.deck(type));
        entry.put("css", type.getCss());
        entry.put("flds", TypeOps.fields(type));
        entry.put("tmpls", TypeOps.templates(type));
        entry.put("req", TypeOps.checks(type));
        entry.put("originalStockKind", 1);
        return entry;
    }

    /**
     * Build the fields of a note type.
     *
     * @param type Note type
     * @return Fields of the note type
     */
    private static List<Object> fields(final NoteType type) {
        final List<Object> fields = new ArrayList<>(0);
        for (final NoteField field : type.getFields()) {
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
     * Build the card templates of a note type.
     *
     * @param type Note type
     * @return Card templates of the note type
     */
    private static List<Object> templates(final NoteType type) {
        final List<Object> templates = new ArrayList<>(0);
        for (final NoteTemplate template : type.getTemplates()) {
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
     * Build the checks of a note type.
     *
     * @param type Note type
     * @return Checks of the note type
     */
    private static List<Object> checks(final NoteType type) {
        final List<Object> checks = new ArrayList<>(0);
        for (final Check check : type.getChecks()) {
            final List<Object> entry = new ArrayList<>(0);
            entry.add(check.getField());
            entry.add(check.getKind());
            entry.add(check.getValues());
            checks.add(entry);
        }
        return checks;
    }

    /**
     * Deck the new cards of a note type go to.
     *
     * @param type Note type
     * @return Identifier of the deck
     */
    private static long deck(final NoteType type) {
        long deck = 1L;
        if (type.getDeck() != null) {
            deck = type.getDeck().getValue();
        }
        return deck;
    }

}
