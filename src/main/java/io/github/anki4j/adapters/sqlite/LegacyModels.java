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
import io.github.anki4j.domain.NoteType;
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
        return TypeReader.read(document);
    }

    /**
     * Read the name of a model.
     *
     * @param model Model being read
     * @return Name of the model, empty when it has none
     */
    private static String name(final Map<?, ?> model) {
        final Object found = model.get("name");
        String name = "";
        if (found instanceof String) {
            name = (String) found;
        }
        return name;
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
