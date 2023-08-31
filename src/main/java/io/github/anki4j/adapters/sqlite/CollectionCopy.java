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
 * Writer of the contents of a collection file.
 *
 * @since 0.1.0
 */
final class CollectionCopy {

    /**
     * Schema version from which the models are kept in tables.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * Suffix of the file the collection is written to first.
     */
    private static final String SUFFIX = ".tmp";

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * Path of the file the collection is written to.
     */
    private final Path target;

    /**
     * New saver of a collection.
     *
     * @param state Contents of the collection
     * @param target Path of the file to write
     */
    CollectionCopy(final CollectionState state, final Path target) {
        this.state = state;
        this.target = target;
    }

    /**
     * Write the collection to a file and move it over the target.
     *
     * @throws IOException When the file cannot be written
     * @throws SQLException When the file cannot be filled
     */
    void save() throws IOException, SQLException {
        final Path temp = this.temporary();
        try {
            this.prepare(temp);
            this.fill(temp);
            Files.move(
                temp, this.target,
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
     * @return Path of the file
     */
    private Path temporary() {
        return this.target.resolveSibling(
            this.target.getFileName() + CollectionCopy.SUFFIX
        );
    }

    /**
     * Make the file the collection is written to first, with the tables of
     * the file it was read from.
     *
     * @param temp Path of the file
     * @throws IOException When the file cannot be copied
     */
    private void prepare(final Path temp) throws IOException {
        Files.deleteIfExists(temp);
        if (Files.exists(this.target)) {
            Files.copy(
                this.target, temp, StandardCopyOption.COPY_ATTRIBUTES
            );
        } else {
            Files.copy(
                this.state.getPath(), temp, StandardCopyOption.COPY_ATTRIBUTES
            );
        }
    }

    /**
     * Fill the file with the contents of the collection.
     *
     * @param temp Path of the file
     * @throws SQLException When the file cannot be filled
     */
    private void fill(final Path temp) throws SQLException {
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", temp));
        try (Connection connection = source.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("delete from notes");
                statement.executeUpdate("delete from cards");
            }
            new Decks(
                connection,
                this.state.getDecklist(),
                this.state.getHeader().decks()
            ).write(this.state.getVersion());
            if (this.state.getVersion() < CollectionCopy.TABLE_VERSION) {
                new Models(
                    connection,
                    this.state.getNotetypelist(),
                    this.state.getHeader().models()
                ).write(this.state.getVersion());
            }
            final Notes notes = new Notes(
                connection, this.state.getNotelist()
            );
            for (final Note note : this.state.getNotelist()) {
                notes.write(note);
            }
            final Cards cards = new Cards(
                connection, this.state.getCardlist()
            );
            for (final Card card : this.state.getCardlist()) {
                cards.write(card);
            }
        }
    }
}
