/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An entry of a document of a collection, which holds the members of one
 * object of the collection.
 *
 * @since 0.1.0
 */
final class Entry {

    /**
     * Name of the entry in the document.
     */
    private final String key;

    /**
     * Members of the entry.
     */
    private final Map<?, ?> members;

    /**
     * New entry of a document.
     *
     * @param name Name of the entry in the document
     * @param members Members of the entry
     */
    Entry(final String name, final Map<?, ?> members) {
        this.key = name;
        this.members = members;
    }

    /**
     * Name of the entry in the document.
     *
     * @return Name of the entry
     */
    String name() {
        return this.key;
    }

    /**
     * The text of a member of the entry.
     *
     * @param member Name of the member
     * @return Text of the member, empty when it is absent
     */
    String text(final String member) {
        final Object found = this.members.get(member);
        String text = "";
        if (found instanceof final String word) {
            text = word;
        }
        return text;
    }

    /**
     * The number of a member of the entry.
     *
     * @param member Name of the member
     * @param fallback Value used when the member is absent
     * @return Number of the member
     */
    long number(final String member, final long fallback) {
        final Object found = this.members.get(member);
        long number = fallback;
        if (found instanceof final Number count) {
            number = count.longValue();
        } else if (found instanceof final String word) {
            try {
                number = Long.parseLong(word.trim());
            } catch (final NumberFormatException error) {
                number = fallback;
            }
        }
        return number;
    }

    /**
     * The items of a member of the entry.
     *
     * @param member Name of the member
     * @return Items of the member, empty when it holds none
     */
    List<Entry> items(final String member) {
        final List<Entry> items = new ArrayList<>(0);
        final Object found = this.members.get(member);
        if (found instanceof List) {
            int ordinal = 0;
            for (final Object item : (List<?>) found) {
                if (item instanceof Map) {
                    items.add(new Entry(String.valueOf(ordinal), (Map<?, ?>) item));
                }
                ordinal += 1;
            }
        }
        return items;
    }
}
