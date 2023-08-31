/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The note types of a collection.
 *
 * @since 0.1.0
 */
final class Models {

    /**
     * Schema version from which the models are kept in tables.
     */
    private static final int TABLE_VERSION = 15;

    /**
     * Name of the member the models keep.
     */
    private static final String NAME = "name";

    /**
     * The type of the documents.
     */
    private static final JsonType<Object> TYPE = Jsonb.newBuilder()
        .build()
        .type(Object.class);

    /**
     * Connection to the database file.
     */
    private final Connection connection;

    /**
     * Note types of the collection.
     */
    private final List<NoteType> notetypelist;

    /**
     * Document the note types are kept in.
     */
    private final Document document;

    /**
     * Tables of the note types.
     */
    private final TypeTables tables;

    /**
     * New models of a collection.
     *
     * @param connection Connection to the database file
     * @param types Note types of the collection
     * @param document Document the note types are kept in
     */
    Models(
        final Connection connection,
        final List<NoteType> types,
        final Document document
    ) {
        this.connection = connection;
        this.notetypelist = types;
        this.document = document;
        this.tables = new TypeTables(connection);
    }

    /**
     * Read the note types of the collection.
     *
     * @param version Schema version of the collection
     * @return Note types, in identifier order
     * @throws SQLException When the note types cannot be read
     */
    List<NoteType> read(final int version) throws SQLException {
        final List<NoteType> found;
        if (version >= Models.TABLE_VERSION) {
            found = this.readtables();
        } else {
            found = this.readdocument();
        }
        return found;
    }

    /**
     * Write the note types of a collection.
     *
     * @param version Schema version of the collection
     */
    void write(final int version) {
        try (PreparedStatement statement = this.connection.prepareStatement(
            "update col set models = ?"
        )) {
            statement.setString(1, this.text());
            statement.executeUpdate();
        } catch (final SQLException error) {
            throw new IllegalStateException(
                "cannot write the note types", error
            );
        }
    }

    /**
     * Identifier of the note type added next.
     *
     * @return Identifier, one above the highest in use
     */
    long nextid() {
        long next = 1L;
        for (final NoteType type : this.notetypelist) {
            if (type.getId().getValue() >= next) {
                next = type.getId().getValue() + 1L;
            }
        }
        return next;
    }

    /**
     * The note type of a collection.
     *
     * @param id Identifier of the note type
     * @return Note type of the collection
     */
    NoteType find(final NoteTypeId id) {
        for (final NoteType type : this.notetypelist) {
            if (type.getId().getValue() == id.getValue()) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note type with id %d", id.getValue())
        );
    }

    /**
     * Build the document of the note types of a collection.
     *
     * @return Document of the note types
     */
    String text() {
        final Map<String, Object> entries = new LinkedHashMap<>();
        for (final NoteType type : this.notetypelist) {
            entries.put(
                String.valueOf(type.getId().getValue()),
                new TypeEntry(type).entry()
            );
        }
        return Models.TYPE.toJson(entries);
    }

    /**
     * Read the note types of a collection that keeps its models in tables.
     *
     * @return Note types, in identifier order
     * @throws SQLException When the note types cannot be read
     */
    private List<NoteType> readtables() throws SQLException {
        final List<NoteType> found = new ArrayList<>(0);
        final String query = """
            select id, name, mtime_secs, usn from notetypes order by id
            """;
        try (Statement statement = this.connection.createStatement();
            ResultSet rows = statement.executeQuery(query)) {
            while (rows.next()) {
                final NoteTypeId id = new NoteTypeId(rows.getLong("id"));
                found.add(
                    NoteType.builder()
                        .id(id)
                        .name(rows.getString("name"))
                        .fields(this.tables.fields(id))
                        .templates(this.tables.templates(id))
                        .checks(List.of())
                        .css("")
                        .sortfield(0)
                        .deck(new DeckId(1L))
                        .modified(rows.getLong("mtime_secs"))
                        .usn(rows.getLong("usn"))
                        .build()
                );
            }
        }
        return found;
    }

    /**
     * Read the note types of a collection that keeps its models in a
     * document.
     *
     * @return Note types, in the order of the document
     */
    private List<NoteType> readdocument() {
        final List<NoteType> found = new ArrayList<>(0);
        for (final Entry entry : this.document.entries()) {
            final long identifier = entry.number("id", 0L);
            found.add(
                NoteType.builder()
                    .id(new NoteTypeId(identifier))
                    .name(entry.text(Models.NAME))
                    .fields(this.readfields(entry.name()))
                    .templates(this.readtemplates(entry.name()))
                    .checks(List.of())
                    .css(entry.text("css"))
                    .sortfield((int) entry.number("sortf", 0L))
                    .deck(new DeckId(entry.number("did", 1L)))
                    .modified(entry.number("mod", 0L))
                    .usn(entry.number("usn", 0L))
                    .build()
            );
        }
        return found;
    }

    /**
     * Read the fields of a note type kept in a document.
     *
     * @param key Key of the entry in the document
     * @return Fields of the note type
     */
    private List<NoteField> readfields(final String key) {
        final List<NoteField> fields = new ArrayList<>(0);
        for (final Entry field : this.document.entry(key).items("flds")) {
            fields.add(
                NoteField.builder()
                    .name(field.text(Models.NAME))
                    .ordinal((int) field.number("ord", 0L))
                    .build()
            );
        }
        return fields;
    }

    /**
     * Read the card templates of a note type kept in a document.
     *
     * @param key Key of the entry in the document
     * @return Card templates of the note type
     */
    private List<NoteTemplate> readtemplates(final String key) {
        final List<NoteTemplate> templates = new ArrayList<>(0);
        for (final Entry template : this.document.entry(key).items("tmpls")) {
            templates.add(
                NoteTemplate.builder()
                    .name(template.text(Models.NAME))
                    .ordinal((int) template.number("ord", 0L))
                    .question(template.text("qfmt"))
                    .answer(template.text("afmt"))
                    .build()
            );
        }
        return templates;
    }

}
