/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reader of the note types a collection keeps as a document.
 *
 * @since 0.1.0
 */
final class TypeReader {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Name of the member the models keep.
     */
    private static final String NAME = "name";

    /**
     * Utility class.
     */
    private TypeReader() {
    }

    /**
     * Read the note types of a collection that keeps its models as
     * documents.
     *
     * @param document Document of the note types
     * @return Note types of the collection
     */
    static List<NoteType> read(final String document) {
        final List<NoteType> types = new ArrayList<>(0);
        final Object models = TypeReader.parse(document);
        if (models instanceof Map) {
            final Map<?, ?> entries = (Map<?, ?>) models;
            for (final Map.Entry<?, ?> entry : entries.entrySet()) {
                final String key = String.valueOf(entry.getKey());
                final Map<?, ?> type = (Map<?, ?>) entry.getValue();
                types.add(
                    NoteType.builder()
                        .id(new NoteTypeId(TypeReader.number(type, "id", 0L)))
                        .name(TypeReader.text(type, TypeReader.NAME))
                        .fields(TypeReader.readfields(type))
                        .templates(TypeReader.readtemplates(type))
                        .checks(List.of())
                        .css(TypeReader.text(type, "css"))
                        .sortfield((int) TypeReader.number(type, "sortf", 0L))
                        .deck(new DeckId(TypeReader.number(type, "did", 1L)))
                        .modified(TypeReader.number(type, "mod", 0L))
                        .usn(TypeReader.number(type, "usn", 0L))
                        .build()
                );
            }
        }
        return types;
    }

    /**
     * Read the fields of a note type.
     *
     * @param type Note type
     * @return Fields of the note type
     */
    private static List<NoteField> readfields(final Map<?, ?> type) {
        final List<NoteField> fields = new ArrayList<>(0);
        for (final Object field : TypeReader.items(type, "flds")) {
            final Map<?, ?> item = (Map<?, ?>) field;
            fields.add(
                NoteField.builder()
                    .name(TypeReader.text(item, TypeReader.NAME))
                    .ordinal((int) TypeReader.number(item, "ord", 0L))
                    .build()
            );
        }
        return fields;
    }

    /**
     * Read the card templates of a note type.
     *
     * @param type Note type
     * @return Card templates of the note type
     */
    private static List<NoteTemplate> readtemplates(final Map<?, ?> type) {
        final List<NoteTemplate> templates = new ArrayList<>(0);
        for (final Object template : TypeReader.items(type, "tmpls")) {
            final Map<?, ?> item = (Map<?, ?>) template;
            templates.add(
                NoteTemplate.builder()
                    .name(TypeReader.text(item, TypeReader.NAME))
                    .ordinal((int) TypeReader.number(item, "ord", 0L))
                    .question(TypeReader.text(item, "qfmt"))
                    .answer(TypeReader.text(item, "afmt"))
                    .build()
            );
        }
        return templates;
    }

    /**
     * Read the text of a member.
     *
     * @param value Value holding the members
     * @param key Name of the member
     * @return Text of the member, empty when it is absent
     */
    private static String text(final Map<?, ?> value, final String key) {
        final Object found = value.get(key);
        String text = "";
        if (found instanceof String) {
            text = (String) found;
        }
        return text;
    }

    /**
     * Read the number of a member; the format keeps some numbers as text.
     *
     * @param value Value holding the members
     * @param key Name of the member
     * @param fallback Value used when the member is absent
     * @return Number of the member
     */
    private static long number(
        final Map<?, ?> value,
        final String key,
        final long fallback
    ) {
        final Object found = value.get(key);
        long number = fallback;
        if (found instanceof Number) {
            number = ((Number) found).longValue();
        }
        return number;
    }

    /**
     * Read the items of a member.
     *
     * @param value Value holding the members
     * @param key Name of the member
     * @return Items of the member, empty when it holds none
     */
    private static List<Object> items(final Map<?, ?> value, final String key) {
        final Object found = value.get(key);
        final List<Object> items = new ArrayList<>(0);
        if (found instanceof List) {
            items.addAll((List<?>) found);
        }
        return items;
    }

    /**
     * Read a document.
     *
     * @param document Text of the document
     * @return Value of the document
     */
    private static Object parse(final String document) {
        Object value = Map.of();
        if (document != null && !document.isBlank()) {
            value = TypeReader.TYPE.fromJson(document);
        }
        return value;
    }
}
