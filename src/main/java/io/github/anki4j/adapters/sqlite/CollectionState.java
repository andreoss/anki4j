/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteType;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import lombok.Value;

/**
 * Contents of a collection file, read in one go.
 *
 * @since 0.1.0
 */
@Value
class CollectionState {

    /**
     * Connection to the database file.
     */
    private Connection connection;

    /**
     * Path of the file the collection was read from.
     */
    private Path path;

    /**
     * Schema version of the collection.
     */
    private int version;

    /**
     * Decks of the collection.
     */
    private List<Deck> decklist;

    /**
     * Note types of the collection.
     */
    private List<NoteType> notetypelist;

    /**
     * Notes of the collection.
     */
    private List<Note> notelist;

    /**
     * Cards of the collection.
     */
    private List<Card> cardlist;
}
