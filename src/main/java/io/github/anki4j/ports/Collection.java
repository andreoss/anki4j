/**
 * Port of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.ports;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
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
     * Editor of the changes of the collection.
     *
     * @return Editor of the collection
     */
    Editor editor();

    /**
     * The note types of the collection.
     *
     * @return Note types of the collection
     */
    NoteTypes types();
}
