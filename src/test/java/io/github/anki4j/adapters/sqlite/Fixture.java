/**
 * Tests of the library.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Assertions;

/**
 * Collections used by the tests.
 *
 * @since 0.1.0
 */
final class Fixture {

    /**
     * Identifier of the note type.
     */
    static final long NOTE_TYPE_ID = 1_600_000_000_000L;

    /**
     * Identifier of the note.
     */
    static final long NOTE_ID = 1_700_000_000_000L;

    /**
     * Identifier of the card.
     */
    static final long CARD_ID = 1_800_000_000_000L;

    /**
     * Note types kept as a document.
     */
    static final String MODELS = """
        {
            "1600000000000": {
                "id": 1600000000000,
                "name": "Basic",
                "mod": 5,
                "usn": -1,
                "flds": [
                    {"name": "Front", "ord": 0},
                    {"name": "Back", "ord": 1}
                ],
                "tmpls": [
                    {
                        "name": "Card 1",
                        "ord": 0,
                        "qfmt": "{{Front}}",
                        "afmt": "{{FrontSide}}"
                    }
                ]
            }
        }
        """;

    /**
     * Decks kept as a document.
     */
    static final String DECKS = """
        {
            "1": {"id": 1, "name": "Default", "mod": 3, "usn": -1, "dyn": 0},
            "2": {"id": 2, "name": "Spanish", "mod": 4, "usn": -1, "dyn": 0}
        }
        """;

    /**
     * Utility class.
     */
    private Fixture() {
    }

    /**
     * Collection keeping its models as documents.
     *
     * @param directory Directory of the collection
     * @return Path of the collection
     * @throws SQLException When the collection cannot be written
     * @throws IOException When the collection cannot be stored
     */
    static Path legacy(final Path directory) throws SQLException, IOException {
        final Path path = directory.resolve("legacy.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertTrue(created.schemaVersion() > 0);
        }
        try (Connection connection = Fixture.connect(path)) {
            try (
                PreparedStatement statement = connection.prepareStatement(
                    "update col set ver = 11, models = ?, decks = ?"
                )
            ) {
                statement.setString(1, Fixture.MODELS);
                statement.setString(2, Fixture.DECKS);
                statement.executeUpdate();
            }
            try (Statement statement = connection.createStatement()) {
                Fixture.insertNote(statement);
                Fixture.insertCard(statement);
            }
        }
        return path;
    }

    /**
     * Collection keeping its models in tables.
     *
     * @param directory Directory of the collection
     * @return Path of the collection
     * @throws SQLException When the collection cannot be written
     * @throws IOException When the collection cannot be stored
     */
    static Path tabular(final Path directory) throws SQLException, IOException {
        final Path path = directory.resolve("tabular.anki2");
        try (SqliteCollection created = new SqliteCollection(path, true)) {
            Assertions.assertTrue(created.schemaVersion() > 0);
        }
        try (
            Connection connection = Fixture.connect(path);
            Statement statement = connection.createStatement()
        ) {
            statement.executeUpdate(
                "update col set ver = 15, models = '{}', decks = '{}'"
            );
            statement.executeUpdate(
                """
                create table notetypes (
                    id integer not null primary key,
                    name text not null,
                    mtime_secs integer not null,
                    usn integer not null,
                    config blob not null
                )
                """
            );
            statement.executeUpdate(
                """
                create table fields (
                    ntid integer not null,
                    ord integer not null,
                    name text not null,
                    config blob not null
                )
                """
            );
            statement.executeUpdate(
                """
                create table templates (
                    ntid integer not null,
                    ord integer not null,
                    name text not null,
                    mtime_secs integer not null,
                    usn integer not null,
                    config blob not null
                )
                """
            );
            statement.executeUpdate(
                """
                create table decks (
                    id integer primary key not null,
                    name text not null,
                    mtime_secs integer not null,
                    usn integer not null,
                    common blob not null,
                    kind blob not null
                )
                """
            );
            statement.executeUpdate(
                """
                insert into notetypes values (
                    1600000000000, 'Basic', 5, -1, x'00'
                )
                """
            );
            statement.executeUpdate(
                "insert into fields values (1600000000000, 0, 'Front', x'00')"
            );
            statement.executeUpdate(
                "insert into fields values (1600000000000, 1, 'Back', x'00')"
            );
            statement.executeUpdate(
                """
                insert into templates values (
                    1600000000000, 0, 'Card 1', 5, -1, x'00'
                )
                """
            );
            statement.executeUpdate(
                "insert into decks values (1, 'Default', 3, -1, x'00', x'00')"
            );
            Fixture.insertNote(statement);
            Fixture.insertCard(statement);
        }
        return path;
    }

    /**
     * Open a collection file.
     *
     * @param path Path of the collection
     * @return Connection to the file
     * @throws SQLException When the file cannot be opened
     */
    static Connection connect(final Path path) throws SQLException {
        return DriverManager.getConnection(
            String.format("jdbc:sqlite:%s", path)
        );
    }

    /**
     * Write a note.
     *
     * @param statement Statement writing the note
     * @throws SQLException When the note cannot be written
     */
    static void insertNote(final Statement statement) throws SQLException {
        statement.executeUpdate(
            """
            insert into notes values (
                1700000000000, 'guid-1', 1600000000000, 7, -1,
                'greeting spanish', 'holahello', 0, 12345, 0, ''
            )
            """
        );
    }

    /**
     * Write a card.
     *
     * @param statement Statement writing the card
     * @throws SQLException When the card cannot be written
     */
    static void insertCard(final Statement statement) throws SQLException {
        statement.executeUpdate(
            """
            insert into cards values (
                1800000000000, 1700000000000, 2, 0, 9, -1, 0, 0, 5, 10, 2500,
                1, 0, 0, 0, 0, 0, ''
            )
            """
        );
    }
}
