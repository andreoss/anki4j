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

/**
 * Work area for the tests, inside the build directory of the repository.
 *
 * @since 0.1.0
 */
public final class Work {

    /**
     * Root of the work area.
     */
    private final Path root;

    /**
     * New work area.
     */
    public Work() {
        this.root = Work.workRoot();
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

    /**
     * Root of the work area.
     *
     * @return Directory holding the work areas
     */
    private static Path workRoot() {
        try {
            final Path classes = Path.of(
                Work.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            );
            return classes.getParent().resolve("test-work");
        } catch (final URISyntaxException error) {
            throw new IllegalStateException(
                "cannot locate the build directory", error
            );
        }
    }

    /**
     * Delete a directory and its content.
     *
     * @param directory Directory to delete
     * @throws IOException When the directory cannot be deleted
     */
    private static void delete(final Path directory) throws IOException {
        final List<Path> paths = new ArrayList<>(0);
        try (var walk = Files.walk(directory)) {
            walk.forEach(paths::add);
        }
        paths.sort(Comparator.reverseOrder());
        for (final Path path : paths) {
            Files.deleteIfExists(path);
        }
    }
}
