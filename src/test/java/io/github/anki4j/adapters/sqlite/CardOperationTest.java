/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.CardId;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteId;
import io.github.anki4j.domain.NoteTypeId;
import io.github.anki4j.domain.Schedule;
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
        Assertions.assertAll(
            () -> CardOperationTest.writeCards(path),
            () -> CardOperationTest.readCards(path)
        );
    }

    /**
     * The cards of a note are not made twice.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void generatesCardsOnce() throws Exception {
        try (
            SqliteCollection collection = new SqliteCollection(
                Fixture.legacy(new Work().directory("cardsonce"))
            )
        ) {
            final NoteId note = collection.editor().addNote(
                new NoteTypeId(Fixture.NOTE_TYPE_ID), List.of("hola", "hello")
            ).getId();
            collection.editor().generateCards(note, new DeckId(2L));
            collection.editor().generateCards(note, new DeckId(2L));
            Assertions.assertEquals(2, collection.cards().size());
        }
    }

    /**
     * The scheduling state of a card is written and kept in the file.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void writesTheSchedule() throws Exception {
        final Path path = Fixture.legacy(new Work().directory("schedule"));
        Assertions.assertAll(
            () -> CardOperationTest.writeSchedule(path),
            () -> CardOperationTest.readSchedule(path)
        );
    }

    /**
     * An operation on a card that is not there is reported.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void reportsAMissingCard() throws Exception {
        try (
            SqliteCollection collection = new SqliteCollection(
                Fixture.legacy(new Work().directory("missingcard"))
            )
        ) {
            Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> collection.editor().setSchedule(
                    new CardId(99L),
                    new Schedule(
                        0, 0, 0L, 0L, 0, 0, 0, 0, 0L, 0L
                    )
                )
            );
        }
    }

    private static void writeCards(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final NoteId note = collection.editor().addNote(
                new NoteTypeId(Fixture.NOTE_TYPE_ID),
                List.of("hola", "hello")
            ).getId();
            collection.editor().generateCards(note, new DeckId(2L));
            final Card card = collection.cards().get(1);
            Assertions.assertAll(
                () -> Assertions.assertEquals(2, collection.cards().size()),
                () -> Assertions.assertEquals(
                    note.getValue(), card.getNote().getValue()
                ),
                () -> Assertions.assertEquals(2L, card.getDeck().getValue()),
                () -> Assertions.assertEquals(0, card.getOrdinal()),
                () -> Assertions.assertEquals(
                    0, card.getSchedule().getQueue()
                )
            );
            collection.save(path);
        }
    }

    private static void readCards(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            Assertions.assertAll(
                () -> Assertions.assertEquals(2, collection.cards().size()),
                () -> Assertions.assertEquals(
                    2L, collection.cards().get(1).getDeck().getValue()
                )
            );
        }
    }

    private static void writeSchedule(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            collection.editor().setSchedule(
                new CardId(Fixture.CARD_ID),
                new Schedule(
                    2, 2, 100L, 10L, 2500, 3, 1, 0, 90L, 1L
                )
            );
            Assertions.assertEquals(
                2, collection.cards().get(0).getSchedule().getQueue()
            );
            collection.save(path);
        }
    }

    private static void readSchedule(final Path path) throws Exception {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final Schedule schedule = collection.cards().get(0).getSchedule();
            Assertions.assertAll(
                () -> Assertions.assertEquals(2, schedule.getType()),
                () -> Assertions.assertEquals(2, schedule.getQueue()),
                () -> Assertions.assertEquals(100L, schedule.getDue()),
                () -> Assertions.assertEquals(10L, schedule.getInterval()),
                () -> Assertions.assertEquals(2500, schedule.getFactor()),
                () -> Assertions.assertEquals(3, schedule.getRepetitions()),
                () -> Assertions.assertEquals(1, schedule.getLapses()),
                () -> Assertions.assertEquals(90L, schedule.getOriginaldue())
            );
        }
    }
}
