/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteId;
import java.sql.SQLException;

/**
 * Changes of a collection file.
 *
 * @since 0.1.0
 */
final class CollectionEditor {

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * New editor over the contents of a file.
     *
     * @param state Contents of the file
     */
    CollectionEditor(final CollectionState state) {
        this.state = state;
    }

    /**
     * Create a deck.
     *
     * @param name Name of the deck
     * @return Deck created
     */
    Deck createDeck(final String name) {
        final Deck deck = Deck.builder()
            .id(new DeckId(DeckOps.nextid(this.state.getDecklist())))
            .name(name)
            .modified(DeckOps.now())
            .usn(-1L)
            .dynamic(false)
            .build();
        this.state.getDecklist().add(deck);
        DeckOps.write(
            this.state.getConnection(),
            this.state.getVersion(),
            this.state.getDecklist()
        );
        return deck;
    }

    /**
     * Rename a deck.
     *
     * @param id Identifier of the deck
     * @param name New name of the deck
     */
    void renameDeck(final DeckId id, final String name) {
        for (int index = 0; index < this.state.getDecklist().size(); index += 1) {
            final Deck deck = this.state.getDecklist().get(index);
            if (deck.getId().getValue() == id.getValue()) {
                this.state.getDecklist().set(index, deck.withName(name));
                DeckOps.write(
                    this.state.getConnection(),
                    this.state.getVersion(),
                    this.state.getDecklist()
                );
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no deck with id %d", id.getValue())
        );
    }

    /**
     * Delete a deck; its cards move to the default deck.
     *
     * @param id Identifier of the deck
     */
    void deleteDeck(final DeckId id) {
        boolean found = false;
        for (int index = 0; index < this.state.getDecklist().size(); index += 1) {
            final long current = this.state.getDecklist()
                .get(index).getId().getValue();
            if (current == id.getValue()) {
                this.state.getDecklist().remove(index);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                String.format("no deck with id %d", id.getValue())
            );
        }
        final DeckId target = new DeckId(
            DeckOps.defaultid(this.state.getDecklist())
        );
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getDeck().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withDeck(target));
                MoveStore.movecard(
                    this.state.getConnection(), card.getId(), target
                );
            }
        }
        DeckOps.write(
            this.state.getConnection(),
            this.state.getVersion(),
            this.state.getDecklist()
        );
    }

    /**
     * Move the cards of a note to a deck.
     *
     * @param id Identifier of the note
     * @param deck Identifier of the deck
     */
    void moveNote(final NoteId id, final DeckId deck) {
        boolean found = false;
        for (final Note note : this.state.getNotelist()) {
            if (note.getId().getValue() == id.getValue()) {
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                String.format("no note with id %d", id.getValue())
            );
        }
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getNote().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withDeck(deck));
                MoveStore.movecard(
                    this.state.getConnection(), card.getId(), deck
                );
            }
        }
    }

    /**
     * Move a card to a deck.
     *
     * @param id Identifier of the card
     * @param deck Identifier of the deck
     */
    void moveCard(final CardId id, final DeckId deck) {
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getId().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withDeck(deck));
                MoveStore.movecard(this.state.getConnection(), id, deck);
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no card with id %d", id.getValue())
        );
    }

    /**
     * Close the file of the collection.
     *
     * @throws SQLException When the file cannot be closed
     */
    void close() throws SQLException {
        this.state.getConnection().close();
    }
}
