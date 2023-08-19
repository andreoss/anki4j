/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteDataSource;

/**
 * The file of a collection.
 *
 * @since 0.1.0
 */
final class CollectionFile {

    /**
     * Utility class.
     */
    private CollectionFile() {
    }

    /**
     * Open the file of a collection and read it.
     *
     * @param path Path of the database file
     * @param empty Whether the file is created with an empty collection
     * @return Contents of the file
     * @throws SQLException When the file cannot be read or written
     */
    static CollectionState open(final Path path, final boolean empty)
        throws SQLException {
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", path));
        final Connection connection = source.getConnection();
        if (empty) {
            CollectionFile.createSchema(connection);
        }
        final CollectionReader reader = new CollectionReader(connection);
        return new CollectionState(
            connection,
            path,
            reader.version(),
            reader.decks(),
            reader.noteTypes(),
            reader.notes(),
            reader.cards()
        );
    }

    /**
     * Create the tables of an empty collection.
     *
     * @param connection Connection to the database file
     * @throws SQLException When the tables cannot be created
     */
    private static void createSchema(final Connection connection)
        throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (final String command : SqliteSchema.commands().split(";")) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        }
    }
}
