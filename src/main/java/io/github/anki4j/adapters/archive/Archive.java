/**
 * Archive of a collection.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.archive;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A packaged collection: the collection file, the media and the map that
 * holds the names of the media.
 *
 * @since 0.1.0
 */
public final class Archive {

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.builder()
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
     * Path of the archive.
     */
    private final Path file;

    /**
     * New archive.
     *
     * @param file Path of the archive
     */
    public Archive(final Path file) {
        this.file = file;
    }

    /**
     * The collection of the archive, taken out of it.
     *
     * @return Path of the collection file
     * @throws IOException When the archive cannot be read
     */
    public Path collection() throws IOException {
        final Path target = this.file.resolveSibling(Archive.FILE);
        try (ZipFile zip = new ZipFile(this.file.toFile())) {
            final ZipEntry entry = zip.getEntry(Archive.FILE);
            if (entry == null) {
                throw new IOException("the archive holds no collection");
            }
            try (InputStream stream = zip.getInputStream(entry)) {
                Files.copy(stream, target, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        return target;
    }

    /**
     * The names of the media of the archive.
     *
     * @return Names of the media, by their number in the archive
     * @throws IOException When the archive cannot be read
     */
    public Map<String, String> media() throws IOException {
        final Map<String, String> names = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(this.file.toFile())) {
            final ZipEntry entry = zip.getEntry(Archive.MAP);
            if (entry != null) {
                try (InputStream stream = zip.getInputStream(entry)) {
                    final String document = new String(
                        stream.readAllBytes(), StandardCharsets.UTF_8
                    );
                    final Object map = Archive.TYPE.fromJson(document);
                    if (map instanceof Map) {
                        final Map<?, ?> entries = (Map<?, ?>) map;
                        for (final Map.Entry<?, ?> member : entries.entrySet()) {
                            names.put(
                                String.valueOf(member.getKey()),
                                String.valueOf(member.getValue())
                            );
                        }
                    }
                }
            }
        }
        return names;
    }

    /**
     * The media of the archive, taken out of it.
     *
     * @return Directory holding the media files, by their name
     * @throws IOException When the archive cannot be read
     */
    public Path mediaDirectory() throws IOException {
        final Path directory = this.file.resolveSibling("media");
        Files.createDirectories(directory);
        try (ZipFile zip = new ZipFile(this.file.toFile())) {
            final var entries = zip.entries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()
                    || Archive.FILE.equals(entry.getName())
                    || Archive.MAP.equals(entry.getName())) {
                    continue;
                }
                try (InputStream stream = zip.getInputStream(entry)) {
                    Files.copy(
                        stream,
                        directory.resolve(entry.getName()),
                        StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        }
        return directory;
    }
}
