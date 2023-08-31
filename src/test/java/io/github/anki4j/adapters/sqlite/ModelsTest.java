/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.NoteType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of the note types of a collection.
 *
 * @since 0.1.0
 */
final class ModelsTest {

    /**
     * The note types of a document are read.
     */
    @Test
    void readsNoteTypes() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection, new ArrayList<>(0), new Document(Fixture.MODELS)
            ).read(11);
            Assertions.assertEquals(1, types.size());
            Assertions.assertEquals("Basic", types.get(0).getName());
            Assertions.assertEquals(
                Fixture.NOTE_TYPE_ID, types.get(0).getId().getValue()
            );
            Assertions.assertEquals(2, types.get(0).getFields().size());
            Assertions.assertEquals(1, types.get(0).getTemplates().size());
        }
    }

    /**
     * A document that holds no note type holds none.
     */
    @Test
    void readsEmptyInput() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            Assertions.assertTrue(
                new Models(
                    connection, new ArrayList<>(0), new Document((String) null)
                ).read(11).isEmpty()
            );
            Assertions.assertTrue(
                new Models(
                    connection, new ArrayList<>(0), new Document("")
                ).read(11).isEmpty()
            );
        }
    }

    /**
     * A document that is not an object holds no note type.
     */
    @Test
    void readsNonObjectInput() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection, new ArrayList<>(0), new Document("[]")
            ).read(11);
            Assertions.assertTrue(types.isEmpty());
        }
    }

    /**
     * A note type without values falls back to the empty values.
     */
    @Test
    void defaultsMissingValues() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection, new ArrayList<>(0), new Document("{\"1\": {}}")
            ).read(11);
            Assertions.assertEquals("", types.get(0).getName());
            Assertions.assertTrue(types.get(0).getFields().isEmpty());
            Assertions.assertTrue(types.get(0).getTemplates().isEmpty());
        }
    }

    /**
     * Connect to an empty database.
     *
     * @return Connection to the database
     * @throws SQLException When the connection cannot be opened
     */
    private static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite::memory:");
    }
}
