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
            Assertions.assertAll(
                () -> Assertions.assertEquals(1, types.size()),
                () -> Assertions.assertEquals("Basic", types.get(0).getName()),
                () -> Assertions.assertEquals(
                    Fixture.NOTE_TYPE_ID, types.get(0).getId().getValue()
                ),
                () -> Assertions.assertEquals(
                    2, types.get(0).getFields().size()
                ),
                () -> Assertions.assertEquals(
                    1, types.get(0).getTemplates().size()
                )
            );
        }
    }

    /**
     * A document that holds no note type holds none.
     */
    @Test
    void readsEmptyInput() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            Assertions.assertAll(
                () -> Assertions.assertTrue(
                    new Models(
                        connection, new ArrayList<>(0), new Document((String) null)
                    ).read(11).isEmpty()
                ),
                () -> Assertions.assertTrue(
                    new Models(
                        connection, new ArrayList<>(0), new Document("")
                    ).read(11).isEmpty()
                )
            );
        }
    }

    /**
     * A document that is not an object holds no note type.
     */
    @Test
    void readsNonObjectInput() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            Assertions.assertTrue(
                new Models(
                    connection, new ArrayList<>(0), new Document("[]")
                ).read(11).isEmpty()
            );
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
            Assertions.assertAll(
                () -> Assertions.assertEquals("", types.get(0).getName()),
                () -> Assertions.assertTrue(
                    types.get(0).getFields().isEmpty()
                ),
                () -> Assertions.assertTrue(
                    types.get(0).getTemplates().isEmpty()
                )
            );
        }
    }

    /**
     * The identifier of a note type is the key of its entry.
     */
    @Test
    void readsIdentifierFromKey() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection,
                new ArrayList<>(0),
                new Document(
                    """
                    {
                        "1581236385343": {
                            "id": "1581236385343",
                            "name": "Cloze",
                            "flds": [{"name": "Text", "ord": 0}],
                            "tmpls": [{"name": "Cloze", "ord": 0}]
                        }
                    }
                    """
                )
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(1, types.size()),
                () -> Assertions.assertEquals(
                    1_581_236_385_343L, types.get(0).getId().getValue()
                ),
                () -> Assertions.assertEquals("Cloze", types.get(0).getName())
            );
        }
    }

    /**
     * Every note type of a document keeps its own identifier.
     */
    @Test
    void keepsIdentifierOfEveryNoteType() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection,
                new ArrayList<>(0),
                new Document(
                    """
                    {
                        "1581236385343": {"id": "1581236385343", "name": "Cloze"},
                        "1581236385344": {"id": "1581236385344", "name": "Basic"}
                    }
                    """
                )
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(2, types.size()),
                () -> Assertions.assertEquals(
                    1_581_236_385_343L, types.get(0).getId().getValue()
                ),
                () -> Assertions.assertEquals(
                    1_581_236_385_344L, types.get(1).getId().getValue()
                )
            );
        }
    }

    /**
     * A key that holds no number falls back to the member of the entry.
     */
    @Test
    void fallsBackToTheMemberForTheIdentifier() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection,
                new ArrayList<>(0),
                new Document(
                    """
                    {
                        "first": {"id": 1581236385343, "name": "Cloze"}
                    }
                    """
                )
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(1, types.size()),
                () -> Assertions.assertEquals(
                    1_581_236_385_343L, types.get(0).getId().getValue()
                )
            );
        }
    }

    /**
     * A number the format keeps as text is read as a number.
     */
    @Test
    void readsNumberKeptAsText() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection,
                new ArrayList<>(0),
                new Document(
                    """
                    {
                        "1581236385343": {
                            "id": "1581236385343",
                            "name": "Cloze",
                            "mod": "1581236385",
                            "usn": "-1"
                        }
                    }
                    """
                )
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(1, types.size()),
                () -> Assertions.assertEquals(
                    1_581_236_385L, types.get(0).getModified()
                ),
                () -> Assertions.assertEquals(-1L, types.get(0).getUsn())
            );
        }
    }

    /**
     * A member that holds no number falls back to the value given.
     */
    @Test
    void fallsBackForAMemberThatHoldsNoNumber() throws SQLException {
        try (Connection connection = ModelsTest.connect()) {
            final List<NoteType> types = new Models(
                connection,
                new ArrayList<>(0),
                new Document(
                    """
                    {
                        "1581236385343": {
                            "id": "1581236385343", "name": "Cloze", "mod": "x"
                        }
                    }
                    """
                )
            ).read(11);
            Assertions.assertAll(
                () -> Assertions.assertEquals(1, types.size()),
                () -> Assertions.assertEquals(
                    0L, types.get(0).getModified()
                )
            );
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite::memory:");
    }
}
