/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.util.Arrays;
import java.util.List;

/**
 * The tags of a note, as the format keeps them.
 *
 * @since 0.1.0
 */
final class Tags {

    /**
     * Character between the tags of a note.
     */
    static final String SEPARATOR = " ";

    /**
     * Text of the tags.
     */
    private final String text;

    /**
     * New tags, as the format keeps them.
     *
     * @param text Text of the tags
     */
    Tags(final String text) {
        this.text = text;
    }

    /**
     * Values of the tags.
     *
     * @return Tags, in the order they were written
     */
    List<String> values() {
        List<String> values = List.of();
        if (this.text != null && !this.text.isBlank()) {
            values = Arrays.asList(this.text.split(Tags.SEPARATOR));
        }
        return values;
    }

}
