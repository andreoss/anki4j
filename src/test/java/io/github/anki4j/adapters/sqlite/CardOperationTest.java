/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteTypeId;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the card operations of a collection.
 *
 * @since 0.1.0
 */
final class CardOperationTest {

    /**
     * The cards of a note are made from the templates of its note type.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void generatesCards() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("cards"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteId note = collection.editor().addNote(
                new NoteTypeId(Fixture.NOTE_TYPE_ID), List.of("hola", "hello")
            ).getId();
            collection.editor().generateCards(note, new DeckId(2L));
            Assertions.assertEquals(2, collection.cards().size());
            final Card card = collection.cards().get(1);
            Assertions.assertEquals(note.getValue(), card.getNote().getValue());
            Assertions.assertEquals(2L, card.getDeck().getValue());
            Assertions.assertEquals(0, card.getOrdinal());
            Assertions.assertEquals(0, card.getSchedule().getQueue());
        }
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertEquals(2, collection.cards().size());
            Assertions.assertEquals(
                2L, collection.cards().get(1).getDeck().getValue()
            );
        }
    }

    /**
     * The cards of a note are not made twice.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void generatesCardsOnce() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("cardsonce"));
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteId note = collection.editor().addNote(
                new NoteTypeId(Fixture.NOTE_TYPE_ID), List.of("hola", "hello")
            ).getId();
            collection.editor().generateCards(note, new DeckId(2L));
            collection.editor().generateCards(note, new DeckId(2L));
            Assertions.assertEquals(2, collection.cards().size());
        }
    }

}
