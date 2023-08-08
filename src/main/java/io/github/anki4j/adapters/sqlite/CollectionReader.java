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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Reader of the contents of a collection file.
 *
 * @since 0.1.0
 */
final class CollectionReader {

    /**
     * Schema version from which the tables hold the models.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Header row of the collection.
     */
    private final Row row;

    /**
     * New reader.
     *
     * @param connection Connection to the database file
     * @throws SQLException When the header row cannot be read
     */
    CollectionReader(final Connection connection) throws SQLException {
        this.connection = connection;
        this.row = Row.read(this.connection);
    }

    /**
     * Schema version of the collection.
     *
     * @return Version stored in the header row
     */
    int version() {
        return this.row.version();
    }

    /**
     * Decks of the collection.
     *
     * @return Decks, in identifier order
     * @throws SQLException When the decks cannot be read
     */
    List<Deck> decks() throws SQLException {
        final List<Deck> decks;
        if (this.row.version() >= CollectionReader.TABLE_VERSION) {
            decks = TableModels.decks(this.connection);
        } else {
            decks = LegacyModels.decks(this.row.decks());
        }
        return decks;
    }

    /**
     * Note types of the collection.
     *
     * @return Note types, in identifier order
     * @throws SQLException When the note types cannot be read
     */
    List<NoteType> noteTypes() throws SQLException {
        final List<NoteType> types;
        if (this.row.version() >= CollectionReader.TABLE_VERSION) {
            types = TableModels.noteTypes(this.connection);
        } else {
            types = LegacyModels.noteTypes(this.row.models());
        }
        return types;
    }

    /**
     * Notes of the collection.
     *
     * @return Notes, in identifier order
     * @throws SQLException When the notes cannot be read
     */
    List<Note> notes() throws SQLException {
        return NoteRows.read(this.connection);
    }

    /**
     * Cards of the collection.
     *
     * @return Cards, in identifier order
     * @throws SQLException When the cards cannot be read
     */
    List<Card> cards() throws SQLException {
        return CardRows.read(this.connection);
    }
}
