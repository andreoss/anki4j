/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.Work;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the schema a collection may hold.
 *
 * @since 0.1.0
 */
final class SchemaTest {

    /**
     * A collection of a schema the reference implementation refuses is
     * reported.
     *
     * @throws Exception When the collection cannot be written
     */
    @Test
    void reportsSchemaThatIsNotSupported() throws Exception {
        final Path path = new Work().directory("old").resolve("old.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertEquals(11, created.schemaVersion());
        }
        try (Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("update col set ver = 1");
        }
        final SQLException error = Assertions.assertThrows(
            SQLException.class, () -> new SqliteCollection(path)
        );
        Assertions.assertTrue(
            error.getMessage().contains("the schema 1"),
            error.getMessage()
        );
    }

    /**
     * A collection of a schema the reference implementation refuses
     * between the oldest and the newest is reported.
     *
     * @throws Exception When the collection cannot be written
     */
    @Test
    void reportsSchemaOfTwelveAndThirteen() throws Exception {
        for (final int version : new int[] {12, 13}) {
            final Path path = new Work().directory("middle")
                .resolve(String.format("middle-%d.anki2", version));
            try (SqliteCollection created = new SqliteCollection(path, true)) {
                Assertions.assertEquals(11, created.schemaVersion());
            }
            try (Connection connection = Fixture.connect(path);
                Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    String.format("update col set ver = %d", version)
                );
            }
            final SQLException error = Assertions.assertThrows(
                SQLException.class, () -> new SqliteCollection(path)
            );
            Assertions.assertTrue(
                error.getMessage().contains(
                    String.format("the schema %d", version)
                ),
                error.getMessage()
            );
        }
    }

    /**
     * A collection of a schema above the newest the reference
     * implementation opens is reported.
     *
     * @throws Exception When the collection cannot be written
     */
    @Test
    void reportsSchemaThatIsTooNew() throws Exception {
        final Path path = new Work().directory("new").resolve("new.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertEquals(11, created.schemaVersion());
        }
        try (Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()) {
            statement.executeUpdate("update col set ver = 19");
        }
        final SQLException error = Assertions.assertThrows(
            SQLException.class, () -> new SqliteCollection(path)
        );
        Assertions.assertTrue(
            error.getMessage().contains("the schema 19"), error.getMessage()
        );
    }

    /**
     * A collection of a schema the reference implementation opens is read.
     *
     * @throws Exception When the collection cannot be read
     */
    @Test
    void readsEverySupportedSchema() throws Exception {
        final Work work = new Work();
        for (final int version : new int[] {11, 14}) {
            final Path path = Fixture.legacy(work.directory("supported-legacy"));
            try (Connection connection = Fixture.connect(path);
                Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    String.format("update col set ver = %d", version)
                );
            }
            try (SqliteCollection collection = new SqliteCollection(path)) {
                Assertions.assertEquals(version, collection.schemaVersion());
                Assertions.assertEquals(1, collection.noteTypes().size());
            }
        }
        for (final int version : new int[] {15, 16, 17, 18}) {
            final Path path = Fixture.tabular(work.directory("supported-table"));
            try (Connection connection = Fixture.connect(path);
                Statement statement = connection.createStatement()) {
                statement.executeUpdate(
                    String.format("update col set ver = %d", version)
                );
            }
            try (SqliteCollection collection = new SqliteCollection(path)) {
                Assertions.assertEquals(version, collection.schemaVersion());
                Assertions.assertEquals(1, collection.noteTypes().size());
            }
        }
    }
}
