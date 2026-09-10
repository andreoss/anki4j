/**
 * Conformance of the library with the format.
 *
 * @since 0.1.0
 */
package io.github.anki4j.conformance;

import io.github.anki4j.Work;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * The cases of the format, kept as collections a reader outside this
 * repository can read.
 *
 * @since 0.1.0
 */
final class ConformanceTest {

    /**
     * The cases are written where a reader outside this repository can
     * find them, and each keeps its contents over a save and an export.
     *
     * @throws Exception When the collections cannot be written
     */
    @Test
    void keepsTheCases() throws Exception {
        final Path root = new Work().directory("conformance");
        Assertions.assertAll(
            () -> Cases.empty(root.resolve("empty")),
            () -> Cases.decks(root.resolve("decks")),
            () -> Cases.notes(root.resolve("notes")),
            () -> Cases.types(root.resolve("types")),
            () -> Cases.cards(root.resolve("cards")),
            () -> Cases.tables(root.resolve("tables"))
        );
    }
}
