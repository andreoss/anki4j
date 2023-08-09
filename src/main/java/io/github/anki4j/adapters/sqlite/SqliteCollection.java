/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.DeckId;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.SQLiteDataSource;

/**
 * A collection kept in a database file.
 *
 * @since 0.1.0
 */
public final class SqliteCollection extends CollectionBase implements AutoCloseable {

    /**
     * Editor of the contents of the collection.
     */
    private final CollectionEditor editor;

    /**
     * New collection over an existing file.
     *
     * @param path Path of the database file
     * @throws SQLException When the file cannot be read
     */
    public SqliteCollection(final Path path) throws SQLException {
        this(SqliteCollection.read(path, false));
    }

    /**
     * New collection, empty when asked for.
     *
     * @param path Path of the database file
     * @param empty Whether the file is created with an empty collection
     * @throws SQLException When the file cannot be read or written
     */
    public SqliteCollection(final Path path, final boolean empty)
        throws SQLException {
        this(SqliteCollection.read(path, empty));
    }

    /**
     * New collection over the contents of a file.
     *
     * @param state Contents of the file
     */
    private SqliteCollection(final CollectionState state) {
        super(state);
        this.editor = new CollectionEditor(state);
    }

    @Override
    public Deck createDeck(final String name) {
        return this.editor.createDeck(name);
    }

    @Override
    public void renameDeck(final DeckId id, final String name) {
        this.editor.renameDeck(id, name);
    }

    @Override
    public void deleteDeck(final DeckId id) {
        this.editor.deleteDeck(id);
    }

    @Override
    public void close() throws SQLException {
        this.editor.close();
    }

    /**
     * Read the contents of a database file.
     *
     * @param path Path of the database file
     * @param empty Whether the file is created with an empty collection
     * @return Contents of the file
     * @throws SQLException When the file cannot be read or written
     */
    private static CollectionState read(final Path path, final boolean empty)
        throws SQLException {
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", path));
        final Connection connection = source.getConnection();
        if (empty) {
            SqliteCollection.createSchema(connection);
        }
        final CollectionReader reader = new CollectionReader(connection);
        return new CollectionState(
            connection,
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
