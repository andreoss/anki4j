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

/**
 * Contents of a collection file, read in one go.
 *
 * @since 0.1.0
 */
@SuppressWarnings("PMD.DataClass")
class CollectionState {

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Path of the file the collection was read from.
     */
    private final Path path;

    /**
     * Schema version of the collection.
     */
    private final int version;

    /**
     * Header row of the collection.
     */
    private final Header header;

    /**
     * Decks of the collection.
     */
    private final List<Deck> decklist;

    /**
     * Note types of the collection.
     */
    private final List<NoteType> notetypelist;

    /**
     * Notes of the collection.
     */
    private final List<Note> notelist;

    /**
     * Cards of the collection.
     */
    private final List<Card> cardlist;

    /**
     * All contents of the collection.
     *
     * @param connection Connection to the database file
     * @param path Path of the file the collection was read from
     * @param version Schema version of the collection
     * @param header Header row of the collection
     * @param decklist Decks of the collection
     * @param notetypelist Note types of the collection
     * @param notelist Notes of the collection
     * @param cardlist Cards of the collection
     */
    CollectionState(
        final Connection connection,
        final Path path,
        final int version,
        final Header header,
        final List<Deck> decklist,
        final List<NoteType> notetypelist,
        final List<Note> notelist,
        final List<Card> cardlist
    ) {
        this.connection = connection;
        this.path = path;
        this.version = version;
        this.header = header;
        this.decklist = decklist;
        this.notetypelist = notetypelist;
        this.notelist = notelist;
        this.cardlist = cardlist;
    }

    /**
     * Connection to the database file.
     *
     * @return Connection
     */
    Connection getConnection() {
        return this.connection;
    }

    /**
     * Path of the file the collection was read from.
     *
     * @return Path of the file
     */
    Path getPath() {
        return this.path;
    }

    /**
     * Schema version of the collection.
     *
     * @return Version of the schema
     */
    int getVersion() {
        return this.version;
    }

    /**
     * Header row of the collection.
     *
     * @return Header of the collection
     */
    Header getHeader() {
        return this.header;
    }

    /**
     * Decks of the collection.
     *
     * @return Decks
     */
    List<Deck> getDecklist() {
        return this.decklist;
    }

    /**
     * Note types of the collection.
     *
     * @return Note types
     */
    List<NoteType> getNotetypelist() {
        return this.notetypelist;
    }

    /**
     * Notes of the collection.
     *
     * @return Notes
     */
    List<Note> getNotelist() {
        return this.notelist;
    }

    /**
     * Cards of the collection.
     *
     * @return Cards
     */
    List<Card> getCardlist() {
        return this.cardlist;
    }
}
