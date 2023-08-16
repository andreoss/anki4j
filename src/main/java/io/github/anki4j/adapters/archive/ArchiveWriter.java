/**
 * Archive of a collection.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.archive;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writer of a packaged collection.
 *
 * @since 0.1.0
 */
public final class ArchiveWriter {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Name of the collection inside the archive.
     */
    private static final String FILE = "collection.anki2";

    /**
     * Name of the map of the media inside the archive.
     */
    private static final String MAP = "media";

    /**
     * Path of the archive to write.
     */
    private final Path archive;

    /**
     * New writer.
     *
     * @param archive Path of the archive to write
     */
    public ArchiveWriter(final Path archive) {
        this.archive = archive;
    }

    /**
     * Write the collection and the media to the archive.
     *
     * @param collection Path of the collection file
     * @param media Media files, by name
     * @throws IOException When the archive cannot be written
     */
    public void write(
        final Path collection,
        final Map<String, Path> media
    ) throws IOException {
        final Path temp = this.archive.resolveSibling(
            String.format("%s.tmp", this.archive.getFileName())
        );
        Files.deleteIfExists(temp);
        try (ZipOutputStream zip = new ZipOutputStream(
            Files.newOutputStream(temp)
        )) {
            ArchiveWriter.add(zip, ArchiveWriter.FILE, collection);
            final Map<String, Object> names = new LinkedHashMap<>();
            int number = 0;
            for (final Map.Entry<String, Path> entry : media.entrySet()) {
                names.put(String.valueOf(number), entry.getKey());
                ArchiveWriter.add(zip, String.valueOf(number), entry.getValue());
                number += 1;
            }
            zip.putNextEntry(new ZipEntry(ArchiveWriter.MAP));
            zip.write(
                ArchiveWriter.TYPE.toJson(names)
                    .getBytes(StandardCharsets.UTF_8)
            );
            zip.closeEntry();
            zip.finish();
        } catch (final IOException error) {
            Files.deleteIfExists(temp);
            throw error;
        }
        Files.move(
            temp, this.archive,
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE
        );
    }

    /**
     * Add a file to the archive.
     *
     * @param zip Archive being written
     * @param name Name of the entry
     * @param path Path of the file
     * @throws IOException When the file cannot be added
     */
    private static void add(
        final ZipOutputStream zip,
        final String name,
        final Path path
    ) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        Files.copy(path, zip);
        zip.closeEntry();
    }
}
