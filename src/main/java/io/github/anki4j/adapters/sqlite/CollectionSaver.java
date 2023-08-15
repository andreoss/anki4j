/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Note;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteDataSource;

/**
 * Writer of the file of a collection.
 *
 * @since 0.1.0
 */
final class CollectionSaver {

    /**
     * Schema version from which the note types are kept in tables.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * Suffix of the file the collection is written to first.
     */
    private static final String SUFFIX = ".tmp";

    /**
     * Utility class.
     */
    private CollectionSaver() {
    }

    /**
     * Write the collection to a file and move it over the target.
     *
     * @param state Contents of the collection
     * @param target Path of the file to write
     * @throws IOException When the file cannot be written
     * @throws SQLException When the file cannot be filled
     */
    static void save(final CollectionState state, final Path target)
        throws IOException, SQLException {
        final Path temp = CollectionSaver.temporary(target);
        try {
            CollectionSaver.prepare(temp, target);
            CollectionSaver.fill(state, temp);
            Files.move(
                temp, target,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (final IOException | SQLException error) {
            Files.deleteIfExists(temp);
            throw error;
        }
    }

    /**
     * Path of the file the collection is written to first.
     *
     * @param target Path of the file to write
     * @return Path of the file
     */
    private static Path temporary(final Path target) {
        return target.resolveSibling(target.getFileName() + CollectionSaver.SUFFIX);
    }

    /**
     * Make the file the collection is written to first.
     *
     * @param temp Path of the file
     * @param target Path of the file to write
     * @throws IOException When the file cannot be copied
     * @throws SQLException When the file cannot be created
     */
    private static void prepare(
        final Path temp,
        final Path target
    ) throws IOException, SQLException {
        Files.deleteIfExists(temp);
        if (Files.exists(target)) {
            Files.copy(target, temp, StandardCopyOption.COPY_ATTRIBUTES);
        } else {
            final SQLiteDataSource source = new SQLiteDataSource();
            source.setUrl(
                String.format("jdbc:sqlite:%s", temp)
            );
            try (Connection connection = source.getConnection();
                Statement statement = connection.createStatement()) {
                for (final String command
                    : SqliteSchema.commands().split(";")) {
                    if (!command.isBlank()) {
                        statement.execute(command);
                    }
                }
            }
        }
    }

    /**
     * Fill the file with the contents of the collection.
     *
     * @param state Contents of the collection
     * @param temp Path of the file
     * @throws SQLException When the file cannot be filled
     */
    private static void fill(final CollectionState state, final Path temp)
        throws SQLException {
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", temp));
        try (Connection connection = source.getConnection()) {
            CollectionSaver.clear(connection);
            DeckOps.write(
                connection, state.getVersion(), state.getDecklist()
            );
            if (state.getVersion() < CollectionSaver.TABLE_VERSION) {
                TypeOps.write(
                    connection, state.getVersion(), state.getNotetypelist()
                );
            }
            for (final Note note : state.getNotelist()) {
                NoteOps.write(connection, note);
            }
            for (final Card card : state.getCardlist()) {
                CardOps.write(connection, card);
            }
        }
    }

    /**
     * Empty the tables the collection keeps its contents in.
     *
     * @param connection Connection to the database file
     * @throws SQLException When the tables cannot be emptied
     */
    private static void clear(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("delete from notes");
            statement.executeUpdate("delete from cards");
        }
    }
}
