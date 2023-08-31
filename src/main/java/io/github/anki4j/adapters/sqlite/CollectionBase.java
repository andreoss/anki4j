/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.adapters.archive.ArchiveDraft;
import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.ports.Collection;
import io.github.anki4j.ports.Editor;
import io.github.anki4j.ports.NoteTypes;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Contents of a collection file, read once and held in memory.
 *
 * @since 0.1.0
 */
abstract class CollectionBase implements Collection {

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * New collection over the contents of a file.
     *
     * @param state Contents of the file
     */
    CollectionBase(final CollectionState state) {
        this.state = state;
    }

    @Override
    public int schemaVersion() {
        return this.state.getVersion();
    }

    @Override
    public List<Deck> decks() {
        return List.copyOf(this.state.getDecklist());
    }

    @Override
    public List<NoteType> noteTypes() {
        return List.copyOf(this.state.getNotetypelist());
    }

    @Override
    public List<Note> notes() {
        return List.copyOf(this.state.getNotelist());
    }

    @Override
    public List<Card> cards() {
        return List.copyOf(this.state.getCardlist());
    }

    @Override
    public Editor editor() {
        return new CollectionChanges(this.state);
    }

    @Override
    public NoteTypes types() {
        return new TypeChanges(this.state);
    }

    @Override
    public void save(final Path path) throws IOException, SQLException {
        new CollectionCopy(this.state, path).save();
    }

    @Override
    public void exportArchive(
        final Path archive,
        final Map<String, Path> media
    ) throws IOException, SQLException {
        final Path temp = archive.resolveSibling(
            String.format("%s.collection", archive.getFileName())
        );
        try {
            this.save(temp);
            new ArchiveDraft(archive).write(temp, media);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Close the file of the collection.
     *
     * @throws SQLException When the file cannot be closed
     */
    void closeFile() throws SQLException {
        this.state.getConnection().close();
    }
}
