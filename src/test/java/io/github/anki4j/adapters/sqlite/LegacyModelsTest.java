/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.NoteType;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the reader of the models kept as documents.
 *
 * @since 0.1.0
 */
final class LegacyModelsTest {

    /**
     * The decks of a document are read.
     */
    @Test
    void readsDecks() {
        final List<Deck> decks = LegacyModels.decks(Fixture.DECKS);
        Assertions.assertEquals(2, decks.size());
        Assertions.assertEquals("Default", decks.get(0).getName());
        Assertions.assertEquals(2L, decks.get(1).getId().getValue());
    }

    /**
     * The note types of a document are read.
     */
    @Test
    void readsNoteTypes() {
        final List<NoteType> types = LegacyModels.noteTypes(Fixture.MODELS);
        Assertions.assertEquals(1, types.size());
        Assertions.assertEquals("Basic", types.get(0).getName());
        Assertions.assertEquals(
            Fixture.NOTE_TYPE_ID, types.get(0).getId().getValue()
        );
        Assertions.assertEquals(2, types.get(0).getFields().size());
        Assertions.assertEquals(1, types.get(0).getTemplates().size());
    }

    /**
     * An empty document holds no model.
     */
    @Test
    void readsEmptyInput() {
        Assertions.assertTrue(LegacyModels.decks(null).isEmpty());
        Assertions.assertTrue(LegacyModels.decks("  ").isEmpty());
        Assertions.assertTrue(LegacyModels.noteTypes(null).isEmpty());
        Assertions.assertTrue(LegacyModels.noteTypes("").isEmpty());
    }

    /**
     * A document that is not an object holds no model.
     */
    @Test
    void readsNonObjectInput() {
        Assertions.assertTrue(LegacyModels.decks("[]").isEmpty());
        Assertions.assertTrue(LegacyModels.noteTypes("[]").isEmpty());
    }

    /**
     * A model without values falls back to the empty values.
     */
    @Test
    void defaultsMissingValues() {
        final List<Deck> decks = LegacyModels.decks("{\"1\": {}}");
        Assertions.assertEquals("", decks.get(0).getName());
        Assertions.assertEquals(0L, decks.get(0).getModified());
        final List<NoteType> types = LegacyModels.noteTypes("{\"1\": {}}");
        Assertions.assertEquals("", types.get(0).getName());
        Assertions.assertTrue(types.get(0).getFields().isEmpty());
        Assertions.assertTrue(types.get(0).getTemplates().isEmpty());
    }

    /**
     * A filtered deck is marked as such.
     */
    @Test
    void readsDynamicDeck() {
        final List<Deck> decks = LegacyModels.decks(
            "{\"1\": {\"id\": 1, \"name\": \"Filtered\", \"dyn\": 1}}"
        );
        Assertions.assertTrue(decks.get(0).isDynamic());
    }
}
