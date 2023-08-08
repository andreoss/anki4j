/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Header row of a collection file.
 *
 * @since 0.1.0
 */
final class Row {

    /**
     * Schema version of the collection.
     */
    private final int ver;

    /**
     * Note types kept as a document.
     */
    private final String modelsdoc;

    /**
     * Decks kept as a document.
     */
    private final String decksdoc;

    /**
     * New header row.
     *
     * @param version Schema version
     * @param models Note types kept as a document
     * @param decks Decks kept as a document
     */
    private Row(final int version, final String models, final String decks) {
        this.ver = version;
        this.modelsdoc = models;
        this.decksdoc = decks;
    }

    /**
     * Read the header row.
     *
     * @param connection Connection to the database file
     * @return Header row
     * @throws SQLException When the row cannot be read
     */
    static Row read(final Connection connection) throws SQLException {
        final String query = "select ver, models, decks from col";
        try (Statement statement = connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            if (rows.next()) {
                return new Row(
                    rows.getInt("ver"),
                    Row.value(rows, "models"),
                    Row.value(rows, "decks")
                );
            }
        }
        throw new IllegalStateException("collection has no col row");
    }

    /**
     * Schema version of the collection.
     *
     * @return Version stored in the header row
     */
    int version() {
        return this.ver;
    }

    /**
     * Note types kept as a document.
     *
     * @return Document of the note types
     */
    String models() {
        return this.modelsdoc;
    }

    /**
     * Decks kept as a document.
     *
     * @return Document of the decks
     */
    String decks() {
        return this.decksdoc;
    }

    /**
     * Read a column of the row.
     *
     * @param rows Row being read
     * @param name Name of the column
     * @return Value of the column, empty when it is null
     * @throws SQLException When the column cannot be read
     */
    private static String value(final ResultSet rows, final String name)
        throws SQLException {
        final String found = rows.getString(name);
        String value = "";
        if (found != null) {
            value = found;
        }
        return value;
    }
}
