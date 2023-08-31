/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

/**
 * The header of a collection, which holds its schema version and the
 * documents of its models.
 *
 * @since 0.1.0
 */
final class Header {

    /**
     * Schema version of the collection.
     */
    private final int ver;

    /**
     * Note types kept as a document.
     */
    private final Document modelsdoc;

    /**
     * Decks kept as a document.
     */
    private final Document decksdoc;

    /**
     * New header of a collection.
     *
     * @param version Schema version of the collection
     * @param models Text of the document of the note types
     * @param decks Text of the document of the decks
     */
    Header(final int version, final String models, final String decks) {
        this.ver = version;
        this.modelsdoc = new Document(models);
        this.decksdoc = new Document(decks);
    }

    /**
     * Schema version of the collection.
     *
     * @return Version stored in the header
     */
    int version() {
        return this.ver;
    }

    /**
     * Note types kept as a document.
     *
     * @return Document of the note types
     */
    Document models() {
        return this.modelsdoc;
    }

    /**
     * Decks kept as a document.
     *
     * @return Document of the decks
     */
    Document decks() {
        return this.decksdoc;
    }
}
