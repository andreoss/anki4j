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
        this.state = state;
        this.notes = new Notes(state.getConnection(), state.getNotelist());
        this.cards = new Cards(state.getConnection(), state.getCardlist());
        this.decks = new Decks(
            state.getConnection(), state.getDecklist(), state.getHeader().decks()
        );
        this.models = new Models(
            state.getConnection(),
            state.getNotetypelist(),
            state.getHeader().models()
        );
        this.clock = new Clock(System::currentTimeMillis);
    }

    /**
     * Create a deck.
     *
     * @param name Name of the deck
     * @return Deck created
     */
    public Deck createDeck(final String name) {
        final Deck deck = Deck.builder()
            .id(new DeckId(this.decks.nextid()))
            .name(name)
            .modified(this.clock.now())
            .usn(-1L)
            .dynamic(false)
            .build();
        this.state.getDecklist().add(deck);
        return deck;
    }

    /**
     * Rename a deck.
     *
     * @param id Identifier of the deck
     * @param name New name of the deck
     */
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

    /**
     * Delete a deck; its cards move to the default deck.
     *
     * @param id Identifier of the deck
     */
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

    /**
     * Move the cards of a note to a deck.
     *
     * @param id Identifier of the note
     * @param deck Identifier of the deck
     */
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

    /**
     * Move a card to a deck.
     *
     * @param id Identifier of the card
     * @param deck Identifier of the deck
     */
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

    /**
     * Add a note to the collection.
     *
     * @param type Identifier of the note type
     * @param fields Field values of the note
     * @return Note added
     */
    public Note addNote(final NoteTypeId type, final List<String> fields) {
        final Note note = Note.builder()
            .id(new NoteId(this.notes.nextid()))
            .guid(this.notes.guid())
            .notetype(type)
            .modified(this.clock.now())
            .usn(-1L)
            .tags(List.of())
            .fields(fields)
            .checksum(
                new FieldValues(
                    String.join(FieldValues.SEPARATOR, fields)
                ).checksum()
            )
            .flags(0)
            .data("")
            .build();
        this.state.getNotelist().add(note);
        return note;
    }

    /**
     * Write the field values, the tags and the flags of a note.
     *
     * @param note Note with its new values
     */
    public void updateNote(final Note note) {
        final Note changed = Note.builder()
            .id(note.getId())
            .guid(note.getGuid())
            .notetype(note.getNotetype())
            .modified(this.clock.now())
            .usn(note.getUsn())
            .tags(note.getTags())
            .fields(note.getFields())
            .checksum(
                new FieldValues(
                    String.join(FieldValues.SEPARATOR, note.getFields())
                ).checksum()
            )
            .flags(note.getFlags())
            .data(note.getData())
            .build();
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

    /**
     * Delete a note with its cards.
     *
     * @param id Identifier of the note
     */
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
        final Note note = this.notes.find(id);
        final NoteType type = this.models.find(note.getNotetype());
        final long due = this.cards.position();
        for (final NoteTemplate template : type.getTemplates()) {
            final Card found = this.cards.find(id, template.getOrdinal());
            if (found == null) {
                final Card card = Card.builder()
                    .id(new CardId(this.cards.nextid()))
                    .note(id)
                    .deck(deck)
                    .ordinal(template.getOrdinal())
                    .modified(this.clock.now())
                    .usn(-1L)
                    .schedule(
                        Schedule.builder()
                            .type(0)
                            .queue(0)
                            .due(due)
                            .interval(0L)
                            .factor(0)
                            .repetitions(0)
                            .lapses(0)
                            .left(0)
                            .originaldue(0L)
                            .originaldeck(0L)
                            .build()
                    )
                    .flags(0)
                    .data("")
                    .build();
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
