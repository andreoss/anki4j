/**
 * Port of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.ports;

import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteTypeId;
import java.util.List;

/**
 * The changes of a collection.
 *
 * @since 0.1.0
 */
public interface Editor {

    /**
     * Create a deck.
     *
     * @param name Name of the deck
     * @return Deck created
     */
    Deck createDeck(String name);

    /**
     * Rename a deck.
     *
     * @param id Identifier of the deck
     * @param name New name of the deck
     */
    void renameDeck(DeckId id, String name);

    /**
     * Delete a deck; its notes and cards move to the default deck.
     *
     * @param id Identifier of the deck
     */
    void deleteDeck(DeckId id);

    /**
     * Move a note to a deck.
     *
     * @param id Identifier of the note
     * @param deck Identifier of the deck
     */
    void moveNote(NoteId id, DeckId deck);

    /**
     * Move a card to a deck.
     *
     * @param id Identifier of the card
     * @param deck Identifier of the deck
     */
    void moveCard(CardId id, DeckId deck);

    /**
     * Add a note to the collection.
     *
     * @param type Identifier of the note type
     * @param fields Field values of the note
     * @return Note added
     */
    Note addNote(NoteTypeId type, List<String> fields);

    /**
     * Write the field values, the tags and the flags of a note.
     *
     * @param note Note with its new values
     */
    void updateNote(Note note);

    /**
     * Delete a note with its cards.
     *
     * @param id Identifier of the note
     */
    void deleteNote(NoteId id);
}
