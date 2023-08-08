/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.ports.Collection;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.sqlite.SQLiteDataSource;

/**
 * A collection kept in a database file.
 *
 * @since 0.1.0
 */
public final class SqliteCollection implements Collection, AutoCloseable {

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Schema version of the collection.
     */
    private final int version;

    /**
     * Decks of the collection.
     */
    private final List<Deck> decklist;

    /**
     * Note types of the collection.
     */
    private final List<NoteType> notetypelist;

    /**
     * Notes of the collection.
     */
    private final List<Note> notelist;

    /**
     * Cards of the collection.
     */
    private final List<Card> cardlist;

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
        this.connection = state.getConnection();
        this.version = state.getVersion();
        this.decklist = state.getDecklist();
        this.notetypelist = state.getNotetypelist();
        this.notelist = state.getNotelist();
        this.cardlist = state.getCardlist();
    }

    @Override
    public int schemaVersion() {
        return this.version;
    }

    @Override
    public List<Deck> decks() {
        return this.decklist;
    }

    @Override
    public List<NoteType> noteTypes() {
        return this.notetypelist;
    }

    @Override
    public List<Note> notes() {
        return this.notelist;
    }

    @Override
    public List<Card> cards() {
        return this.cardlist;
    }

    @Override
    public void close() throws SQLException {
        this.connection.close();
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
