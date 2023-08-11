/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the writer of the decks.
 *
 * @since 0.1.0
 */
final class DeckOpsTest {

    /**
     * The document of the decks holds every deck.
     */
    @Test
    void writesTheDocument() {
        final List<Deck> decks = List.of(
            Deck.builder()
                .id(new DeckId(1L))
                .name("Default")
                .modified(3L)
                .usn(-1L)
                .dynamic(false)
                .build(),
            Deck.builder()
                .id(new DeckId(2L))
                .name("Filtered")
                .modified(4L)
                .usn(-1L)
                .dynamic(true)
                .build()
        );
        final String document = DeckOps.document(decks);
        Assertions.assertTrue(document.contains("\"Default\""));
        Assertions.assertTrue(document.contains("\"Filtered\""));
        Assertions.assertTrue(document.contains("\"dyn\":1"));
        Assertions.assertTrue(document.contains("\"dyn\":0"));
    }
}
