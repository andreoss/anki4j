/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.nio.file.Path;
import java.sql.SQLException;

/**
 * A collection kept in a database file.
 *
 * @since 0.1.0
 */
public final class SqliteCollection extends CollectionBase implements AutoCloseable {

    /**
     * New collection over an existing file.
     *
     * @param path Path of the database file
     * @throws SQLException When the file cannot be read
     */
    public SqliteCollection(final Path path) throws SQLException {
        this(new CollectionFile(path, false).open());
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
        this(new CollectionFile(path, empty).open());
    }

    /**
     * New collection over the contents of a file.
     *
     * @param state Contents of the file
     */
    private SqliteCollection(final CollectionState state) {
        super(state);
    }

    @Override
    public void close() throws SQLException {
        this.closeFile();
    }
}
