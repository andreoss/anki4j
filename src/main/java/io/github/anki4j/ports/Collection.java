/**
 * Port of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.ports;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteType;
import java.util.List;

/**
 * A collection of decks, notes, cards and note types.
 *
 * @since 0.1.0
 */
public interface Collection {

    /**
     * Schema version of the collection.
     *
     * @return Version stored in the collection
     */
    int schemaVersion();

    /**
     * Decks of the collection.
     *
     * @return Decks, in identifier order
     */
    List<Deck> decks();

    /**
     * Note types of the collection.
     *
     * @return Note types, in identifier order
     */
    List<NoteType> noteTypes();

    /**
     * Notes of the collection.
     *
     * @return Notes, in identifier order
     */
    List<Note> notes();

    /**
     * Cards of the collection.
     *
     * @return Cards, in identifier order
     */
    List<Card> cards();

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
}
