/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import org.sqlite.SQLiteDataSource;

/**
 * The file of a collection.
 *
 * @since 0.1.0
 */
final class CollectionFile {

    /**
     * Tables and indexes of an empty collection.
     */
    private final String schema;

    /**
     * Path of the file of the collection.
     */
    private final Path path;

    /**
     * Whether the file is created with an empty collection.
     */
    private final boolean empty;

    /**
     * New file of a collection.
     *
     * @param path Path of the database file
     * @param empty Whether the file is created with an empty collection
     */
    CollectionFile(final Path path, final boolean empty) {
        this.schema = """
            create table col (
                id integer primary key,
                crt integer not null,
                mod integer not null,
                scm integer not null,
                ver integer not null,
                dty integer not null,
                usn integer not null,
                ls integer not null,
                conf text not null,
                models text not null,
                decks text not null,
                dconf text not null,
                tags text not null
            );
            create table notes (
                id integer primary key,
                guid text not null,
                mid integer not null,
                mod integer not null,
                usn integer not null,
                tags text not null,
                flds text not null,
                sfld integer not null,
                csum integer not null,
                flags integer not null,
                data text not null
            );
            create table cards (
                id integer primary key,
                nid integer not null,
                did integer not null,
                ord integer not null,
                mod integer not null,
                usn integer not null,
                type integer not null,
                queue integer not null,
                due integer not null,
                ivl integer not null,
                factor integer not null,
                reps integer not null,
                lapses integer not null,
                left integer not null,
                odue integer not null,
                odid integer not null,
                flags integer not null,
                data text not null
            );
            create table revlog (
                id integer primary key,
                cid integer not null,
                usn integer not null,
                ease integer not null,
                ivl integer not null,
                lastIvl integer not null,
                factor integer not null,
                time integer not null,
                type integer not null
            );
            create table graves (
                usn integer not null,
                oid integer not null,
                type integer not null
            );
            create index ix_notes_usn on notes (usn);
            create index ix_cards_usn on cards (usn);
            create index ix_revlog_usn on revlog (usn);
            create index ix_cards_nid on cards (nid);
            create index ix_cards_sched on cards (did, queue, due);
            create index ix_revlog_cid on revlog (cid);
            create index ix_notes_csum on notes (csum);
            insert into col values (
                1, 0, 0, 0, 11, 0, 0, 0, '{}', '{}', '{}', '{}', '{}'
            );
            """;
        this.path = path;
        this.empty = empty;
    }

    /**
     * Open the file of a collection and read it.
     *
     * @return Contents of the file
     * @throws SQLException When the file cannot be read or written
     */
    CollectionState open() throws SQLException {
        final SQLiteDataSource source = new SQLiteDataSource();
        source.setUrl(String.format("jdbc:sqlite:%s", this.path));
        final Connection connection = source.getConnection();
        if (this.empty) {
            this.createschema(connection);
        }
        final Header header;
        try (
            Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(
                "select ver, models, decks from col"
            )
        ) {
            if (rows.next()) {
                header = new Header(
                    rows.getInt("ver"),
                    rows.getString("models"),
                    rows.getString("decks")
                );
            } else {
                throw new IllegalStateException("collection has no col row");
            }
        }
        final int version = header.version();
        final Schema current = new Schema(version);
        if (!current.supported()) {
            connection.close();
            throw new SQLException(current.report());
        }
        return new CollectionState(
            connection,
            this.path,
            version,
            header,
            new Decks(connection, new ArrayList<>(0), header.decks())
                .read(version),
            new Models(connection, new ArrayList<>(0), header.models())
                .read(version),
            new Notes(connection, new ArrayList<>(0)).read(),
            new Cards(connection, new ArrayList<>(0)).read()
        );
    }

    private void createschema(final Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            for (final String command : this.schema.split(";", -1)) {
                if (!command.isBlank()) {
                    statement.execute(command);
                }
            }
        }
    }
}
