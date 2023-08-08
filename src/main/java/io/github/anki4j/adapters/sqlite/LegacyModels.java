/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Reader of the models kept as documents in a collection file.
 *
 * @since 0.1.0
 */
final class LegacyModels {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Utility class.
     */
    private LegacyModels() {
    }

    /**
     * Read the decks of a document.
     *
     * @param document Document of the decks
     * @return Decks, in the order of the document
     */
    static List<Deck> decks(final String document) {
        final List<Deck> decks = new ArrayList<>(0);
        final Object parsed = LegacyModels.parse(document);
        if (parsed instanceof Map) {
            final Map<?, ?> entries = (Map<?, ?>) parsed;
            for (final Map.Entry<?, ?> entry : entries.entrySet()) {
                final Map<?, ?> deck = (Map<?, ?>) entry.getValue();
                decks.add(
                    Deck.builder()
                        .id(new DeckId(LegacyModels.number(deck, "id", 0L)))
                        .name(LegacyModels.name(deck))
                        .modified(LegacyModels.number(deck, "mod", 0L))
                        .usn(LegacyModels.number(deck, "usn", 0L))
                        .dynamic(LegacyModels.number(deck, "dyn", 0L) != 0L)
                        .build()
                );
            }
        }
        return decks;
    }

    /**
     * Read the note types of a document.
     *
     * @param document Document of the note types
     * @return Note types, in the order of the document
     */
    static List<NoteType> noteTypes(final String document) {
        final List<NoteType> types = new ArrayList<>(0);
        final Object parsed = LegacyModels.parse(document);
        if (parsed instanceof Map) {
            final Map<?, ?> entries = (Map<?, ?>) parsed;
            for (final Map.Entry<?, ?> entry : entries.entrySet()) {
                final Map<?, ?> type = (Map<?, ?>) entry.getValue();
                types.add(
                    NoteType.builder()
                        .id(new NoteTypeId(LegacyModels.number(type, "id", 0L)))
                        .name(LegacyModels.name(type))
                        .fields(LegacyModels.fields(type))
                        .templates(LegacyModels.templates(type))
                        .modified(LegacyModels.number(type, "mod", 0L))
                        .usn(LegacyModels.number(type, "usn", 0L))
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
     * @return Fields, in ordinal order
     */
    private static List<NoteField> fields(final Map<?, ?> type) {
        final List<NoteField> fields = new ArrayList<>(0);
        for (final Object field : LegacyModels.items(type, "flds")) {
            final Map<?, ?> item = (Map<?, ?>) field;
            fields.add(
                NoteField.builder()
                    .name(LegacyModels.name(item))
                    .ordinal((int) LegacyModels.number(item, "ord", 0L))
                    .build()
            );
        }
        return fields;
    }

    /**
     * Read the card templates of a note type.
     *
     * @param type Note type
     * @return Card templates, in ordinal order
     */
    private static List<NoteTemplate> templates(final Map<?, ?> type) {
        final List<NoteTemplate> templates = new ArrayList<>(0);
        for (final Object template : LegacyModels.items(type, "tmpls")) {
            final Map<?, ?> item = (Map<?, ?>) template;
            templates.add(
                NoteTemplate.builder()
                    .name(LegacyModels.name(item))
                    .ordinal((int) LegacyModels.number(item, "ord", 0L))
                    .question(LegacyModels.text(item, "qfmt"))
                    .answer(LegacyModels.text(item, "afmt"))
                    .build()
            );
        }
        return templates;
    }

    /**
     * Read the name of a model.
     *
     * @param model Model being read
     * @return Name of the model, empty when it has none
     */
    private static String name(final Map<?, ?> model) {
        return LegacyModels.text(model, "name");
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
     * Read the number of a member.
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
            value = LegacyModels.TYPE.fromJson(document);
        }
        return value;
    }
}
