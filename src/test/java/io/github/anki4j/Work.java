/**
 * Test helpers of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Work area for the tests, inside the build directory of the repository.
 *
 * @since 0.1.0
 */
public final class Work {

    /**
     * Root of the work area, for all the instances of this class.
     */
    private static final Path ROOT = Work.workRoot();

    /**
     * Root of the work area, taken from {@link #ROOT}.
     */
    private final Path root;

    /**
     * New work area.
     */
    public Work() {
        this.root = Work.ROOT;
    }

    /**
     * Empty directory for one test.
     *
     * @param name Name of the directory
     * @return Directory created
     * @throws IOException When the directory cannot be created
     */
    public Path directory(final String name) throws IOException {
        final Path directory = this.root.resolve(name);
        if (Files.exists(directory)) {
            Work.delete(directory);
        }
        Files.createDirectories(directory);
        return directory;
    }

    private static Path workRoot() {
        try {
            return Path.of(
                Work.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            ).getParent().resolve("test-work");
        } catch (final URISyntaxException error) {
            throw new IllegalStateException(
                "cannot locate the build directory", error
            );
        }
    }

    private static void delete(final Path directory) throws IOException {
        final List<Path> paths = new ArrayList<>(0);
        try (Stream<Path> walk = Files.walk(directory)) {
            walk.forEach(paths::add);
        }
        paths.sort(Comparator.reverseOrder());
        for (final Path path : paths) {
            Files.deleteIfExists(path);
        }
    }
}
