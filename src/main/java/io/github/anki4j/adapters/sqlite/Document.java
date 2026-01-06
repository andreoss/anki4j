/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A document of a collection, which holds the members as they were stored
 * in the header row.
 *
 * @since 0.1.0
 */
final class Document {

    /**
     * The type of a document.
     */
    private static final JsonType<Object> TYPE = Jsonb.builder()
        .build()
        .type(Object.class);

    /**
     * Text of the document.
     */
    private final String text;

    /**
     * New document of a collection.
     *
     * @param text Text of the document
     */
    Document(final String text) {
        this.text = text;
    }

    /**
     * Entries of the document.
     *
     * @return Entries, in the order of the document
     */
    List<Entry> entries() {
        final List<Entry> entries = new ArrayList<>(0);
        Object parsed = Map.of();
        if (this.text != null && !this.text.isBlank()) {
            parsed = Document.TYPE.fromJson(this.text);
        }
        if (parsed instanceof Map) {
            for (final Map.Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
                final String key = String.valueOf(entry.getKey());
                if (entry.getValue() instanceof Map) {
                    entries.add(
                        new Entry(key, (Map<?, ?>) entry.getValue())
                    );
                }
            }
        }
        return entries;
    }

    /**
     * The entry of a member of the document.
     *
     * @param key Name of the member
     * @return Entry of the member, empty when it is absent
     */
    Entry entry(final String key) {
        Entry found = new Entry(key, Map.of());
        for (final Entry entry : this.entries()) {
            if (entry.name().equals(key)) {
                found = entry;
            }
        }
        return found;
    }
}
