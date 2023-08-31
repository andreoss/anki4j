/**
 * Adapter over the archive of a collection.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.archive;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The archive of a collection, which holds the collection file and the media
 * files.
 *
 * @since 0.1.0
 */
final class Zip {

    /**
     * Archive being written.
     */
    private final ZipOutputStream stream;

    /**
     * New archive.
     *
     * @param stream Archive being written
     */
    Zip(final ZipOutputStream stream) {
        this.stream = stream;
    }

    /**
     * Add a file to the archive.
     *
     * @param name Name of the entry
     * @param path Path of the file
     * @throws IOException When the file cannot be added
     */
    void add(final String name, final Path path) throws IOException {
        this.stream.putNextEntry(new ZipEntry(name));
        Files.copy(path, this.stream);
        this.stream.closeEntry();
    }

    /**
     * Add a text to the archive.
     *
     * @param name Name of the entry
     * @param text Text of the entry
     * @throws IOException When the text cannot be added
     */
    void text(final String name, final String text) throws IOException {
        this.stream.putNextEntry(new ZipEntry(name));
        this.stream.write(text.getBytes(StandardCharsets.UTF_8));
        this.stream.closeEntry();
    }

    /**
     * Finish the archive.
     *
     * @throws IOException When the archive cannot be finished
     */
    void finish() throws IOException {
        this.stream.finish();
    }
}
