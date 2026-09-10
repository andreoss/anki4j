/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.archive;

import io.github.anki4j.Work;
import io.github.anki4j.adapters.sqlite.SqliteCollection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the packaged archive of a collection.
 *
 * @since 0.1.0
 */
final class ArchiveTest {

    /**
     * The collection and the media are read back from the archive.
     *
     * @throws Exception When the archive cannot be read
     */
    @Test
    void readsAnArchive() throws Exception {
        final Path directory = new Work().directory("readarchive");
        final Path path = directory.resolve("deck.apkg");
        final Path media = directory.resolve("image.txt");
        Files.writeString(media, "the media");
        try (
            SqliteCollection collection = new SqliteCollection(
                directory.resolve("collection.anki2"),
                true
            )
        ) {
            collection.editor().createDeck("French");
            collection.exportArchive(path, ArchiveTest.media(media));
        }
        final Archive archive = new Archive(path);
        Assertions.assertAll(
            () -> Assertions.assertEquals(
                "image.txt", archive.media().get("0")
            ),
            () -> {
                try (
                    SqliteCollection collection = new SqliteCollection(
                        archive.collection()
                    )
                ) {
                    Assertions.assertEquals(1, collection.decks().size());
                    Assertions.assertEquals(
                        "French", collection.decks().get(0).getName()
                    );
                }
            },
            () -> Assertions.assertEquals(
                "the media",
                Files.readString(archive.mediaDirectory().resolve("0"))
            )
        );
    }

    /**
     * An archive without a collection is reported.
     *
     * @throws Exception When the archive cannot be written
     */
    @Test
    void reportsAnArchiveWithoutACollection() throws Exception {
        final Path directory = new Work().directory("emptyarchive");
        final Path path = directory.resolve("empty.apkg");
        try (
            ZipOutputStream zip = new ZipOutputStream(
                Files.newOutputStream(path)
            )
        ) {
            zip.putNextEntry(new ZipEntry("media"));
            zip.closeEntry();
            zip.finish();
        }
        Assertions.assertThrows(
            IOException.class, new Archive(path)::collection
        );
    }

    private static Map<String, Path> media(final Path media) {
        final Map<String, Path> files = new LinkedHashMap<>();
        files.put("image.txt", media);
        return files;
    }
}
