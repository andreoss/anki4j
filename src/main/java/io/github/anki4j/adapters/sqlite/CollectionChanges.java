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
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import io.github.anki4j.domain.Schedule;
import io.github.anki4j.ports.Editor;
import java.util.List;

/**
 * Changes of a collection file.
 *
 * @since 0.1.0
 */
final class CollectionChanges implements Editor {

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * The notes of the collection.
     */
    private final Notes notes;

    /**
     * The cards of the collection.
     */
    private final Cards cards;

    /**
     * The decks of the collection.
     */
    private final Decks decks;

    /**
     * The models of the collection.
     */
    private final Models models;

    /**
     * The clock of the collection.
     */
    private final Clock clock;

    /**
     * New editor over the contents of a file.
     *
     * @param state Contents of the file
     */
    CollectionChanges(final CollectionState state) {
        this(
            state,
            new Notes(state.getConnection(), state.getNotelist()),
            new Cards(state.getConnection(), state.getCardlist()),
            new Decks(
                state.getConnection(),
                state.getDecklist(),
                state.getHeader().decks()
            ),
            new Models(
                state.getConnection(),
                state.getNotetypelist(),
                state.getHeader().models()
            ),
            new Clock(System::currentTimeMillis)
        );
    }

    /**
     * New editor over the contents of a file.
     *
     * @param state Contents of the file
     * @param notes The notes of the collection
     * @param cards The cards of the collection
     * @param decks The decks of the collection
     * @param models The models of the collection
     * @param clock The clock of the collection
     */
    CollectionChanges(
        final CollectionState state,
        final Notes notes,
        final Cards cards,
        final Decks decks,
        final Models models,
        final Clock clock
    ) {
        this.state = state;
        this.notes = notes;
        this.cards = cards;
        this.decks = decks;
        this.models = models;
        this.clock = clock;
    }

    @Override
    public Deck createDeck(final String name) {
        final Deck deck = new Deck(
            new DeckId(this.decks.nextid()),
            name,
            this.clock.now(),
            -1L,
            false
        );
        this.state.getDecklist().add(deck);
        return deck;
    }

    @Override
    public void renameDeck(final DeckId id, final String name) {
        for (int index = 0; index < this.state.getDecklist().size(); index += 1) {
            final Deck deck = this.state.getDecklist().get(index);
            if (deck.getId().getValue() == id.getValue()) {
                this.state.getDecklist().set(index, deck.withName(name));
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no deck with id %d", id.getValue())
        );
    }

    @Override
    public void deleteDeck(final DeckId id) {
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
            this.decks.defaultid()
        );
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getDeck().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withDeck(target));
            }
        }
    }

    @Override
    public void moveNote(final NoteId id, final DeckId deck) {
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
            }
        }
    }

    @Override
    public void moveCard(final CardId id, final DeckId deck) {
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getId().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withDeck(deck));
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no card with id %d", id.getValue())
        );
    }

    @Override
    public Note addNote(final NoteTypeId type, final List<String> fields) {
        final Note note = new Note(
            new NoteId(this.notes.nextid()),
            this.notes.guid(),
            type,
            this.clock.now(),
            -1L,
            List.of(),
            fields,
            new FieldValues(
                String.join(FieldValues.SEPARATOR, fields)
            ).checksum(),
            0,
            ""
        );
        this.state.getNotelist().add(note);
        return note;
    }

    @Override
    public void updateNote(final Note note) {
        final Note changed = new Note(
            note.getId(),
            note.getGuid(),
            note.getNotetype(),
            this.clock.now(),
            note.getUsn(),
            note.getTags(),
            note.getFields(),
            new FieldValues(
                String.join(FieldValues.SEPARATOR, note.getFields())
            ).checksum(),
            note.getFlags(),
            note.getData()
        );
        for (int index = 0; index < this.state.getNotelist().size(); index += 1) {
            final long current = this.state.getNotelist()
                .get(index).getId().getValue();
            if (current == note.getId().getValue()) {
                this.state.getNotelist().set(index, changed);
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note with id %d", note.getId().getValue())
        );
    }

    @Override
    public void deleteNote(final NoteId id) {
        boolean found = false;
        for (int index = 0; index < this.state.getNotelist().size(); index += 1) {
            final long current = this.state.getNotelist()
                .get(index).getId().getValue();
            if (current == id.getValue()) {
                this.state.getNotelist().remove(index);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                String.format("no note with id %d", id.getValue())
            );
        }
        for (int index = this.state.getCardlist().size() - 1; index >= 0; index -= 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getNote().getValue() == id.getValue()) {
                this.state.getCardlist().remove(index);
            }
        }
    }

    @Override
    public void generateCards(final NoteId id, final DeckId deck) {
        final NoteType type = this.models.find(this.notes.find(id).getNotetype());
        final long due = this.cards.position();
        for (final NoteTemplate template : type.getTemplates()) {
            final Card found = this.cards.find(id, template.getOrdinal());
            if (found == null) {
                final Card card = new Card(
                    new CardId(this.cards.nextid()),
                    id,
                    deck,
                    template.getOrdinal(),
                    this.clock.now(),
                    -1L,
                    new Schedule(
                        0,
                        0,
                        due,
                        0L,
                        0,
                        0,
                        0,
                        0,
                        0L,
                        0L
                    ),
                    0,
                    ""
                );
                this.state.getCardlist().add(card);
            }
        }
    }

    @Override
    public void setSchedule(final CardId id, final Schedule schedule) {
        for (int index = 0; index < this.state.getCardlist().size(); index += 1) {
            final Card card = this.state.getCardlist().get(index);
            if (card.getId().getValue() == id.getValue()) {
                this.state.getCardlist().set(index, card.withSchedule(schedule));
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no card with id %d", id.getValue())
        );
    }
}
