/**
 * Tests of the packaged archive of a collection.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.archive;

import io.github.anki4j.Work;
import io.github.anki4j.adapters.sqlite.SqliteCollection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the writing of a packaged archive.
 *
 * @since 0.1.0
 */
final class ArchiveWriterTest {

    /**
     * The archive holds the collection, the map of the media and the
     * media.
     *
     * @throws Exception When the archive cannot be written
     */
    @Test
    void writesAnArchive() throws Exception {
        final Path directory = new Work().directory("archive");
        final Path path = directory.resolve("deck.apkg");
        final Path media = directory.resolve("image.txt");
        Files.writeString(media, "the media");
        try (SqliteCollection collection = new SqliteCollection(
            directory.resolve("collection.anki2"), true
        )) {
            collection.editor().createDeck("French");
            collection.exportArchive(path, ArchiveWriterTest.media(media));
        }
        try (ZipFile zip = new ZipFile(path.toFile())) {
            Assertions.assertNotNull(zip.getEntry("collection.anki2"));
            Assertions.assertNotNull(zip.getEntry("media"));
            Assertions.assertNotNull(zip.getEntry("0"));
        }
    }

    /**
     * The media of a collection, by name.
     *
     * @param media Path of the media file
     * @return Media of the collection
     */
    private static Map<String, Path> media(final Path media) {
        final Map<String, Path> files = new LinkedHashMap<>();
        files.put("image.txt", media);
        return files;
    }
}
