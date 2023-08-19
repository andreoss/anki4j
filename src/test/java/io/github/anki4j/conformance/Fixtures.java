/**
 * Conformance of the library with the format.
 *
 * @since 0.1.0
 */
package io.github.anki4j.conformance;

import io.github.anki4j.domain.DeckId;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import java.util.List;

/**
 * The contents of the cases of the format.
 *
 * @since 0.1.0
 */
final class Fixtures {

    /**
     * Schema version from which the models are kept in tables.
     */
    static final int TABLE_VERSION = 15;

    /**
     * Utility class.
     */
    private Fixtures() {
    }

    /**
     * A note type with one template.
     *
     * @return Note type
     */
    static NoteType basic() {
        return NoteType.builder()
            .id(new NoteTypeId(1L))
            .name("Basic")
            .fields(
                List.of(
                    NoteField.builder().name("Front").ordinal(0).build(),
                    NoteField.builder().name("Back").ordinal(1).build()
                )
            )
            .templates(List.of(Fixtures.template("Card 1", 0)))
            .checks(List.of())
            .css(".card {}")
            .sortfield(0)
            .deck(new DeckId(1L))
            .modified(0L)
            .usn(-1L)
            .build();
    }

    /**
     * A note type with two templates.
     *
     * @return Note type
     */
    static NoteType pair() {
        return NoteType.builder()
            .id(new NoteTypeId(1L))
            .name("Basic")
            .fields(
                List.of(
                    NoteField.builder().name("Front").ordinal(0).build(),
                    NoteField.builder().name("Back").ordinal(1).build()
                )
            )
            .templates(
                List.of(
                    Fixtures.template("Card 1", 0),
                    NoteTemplate.builder()
                        .name("Card 2").ordinal(1)
                        .question("{{Back}}")
                        .answer("{{Back}}")
                        .build()
                )
            )
            .checks(List.of())
            .css(".card {}")
            .sortfield(0)
            .deck(new DeckId(1L))
            .modified(0L)
            .usn(-1L)
            .build();
    }

    /**
     * Tables of a collection that keeps its models in tables.
     *
     * @return Commands creating the collection
     */
    static String tableSchema() {
        return """
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
            create table decks (
                id integer primary key,
                name text not null,
                mtime_secs integer not null,
                usn integer not null,
                common blob not null,
                kind blob not null
            );
            create table notetypes (
                id integer primary key,
                name text not null,
                mtime_secs integer not null,
                usn integer not null,
                config blob not null
            );
            create table fields (
                ntid integer not null,
                ord integer not null,
                name text not null,
                config blob not null,
                primary key (ntid, ord)
            );
            create table templates (
                ntid integer not null,
                ord integer not null,
                name text not null,
                mtime_secs integer not null,
                usn integer not null,
                config blob not null,
                primary key (ntid, ord)
            );
            insert into col values (
                1, 0, 0, 0, 15, 0, 0, 0, '{}', '{}', '{}', '{}', '{}'
            );
            insert into decks values (1, 'Default', 5, -1, x'00', x'00');
            insert into notetypes values (1, 'Basic', 5, -1, x'00');
            insert into fields values (1, 0, 'Front', x'00');
            insert into fields values (1, 1, 'Back', x'00');
            insert into templates values (1, 0, 'Card 1', 5, -1, x'00');
            insert into templates values (1, 1, 'Card 2', 5, -1, x'00');
            insert into notes values (
                1, 'tables-guid', 1, 5, -1, 'greeting',
                'hola' || char(31) || 'hello', 0, 1, 0, ''
            );
            insert into cards values (
                1, 1, 1, 0, 5, -1, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, ''
            );
            """;
    }

    /**
     * A card template of a note type.
     *
     * @param name Name of the template
     * @param ordinal Ordinal of the template
     * @return Card template
     */
    private static NoteTemplate template(final String name, final int ordinal) {
        return NoteTemplate.builder()
            .name(name)
            .ordinal(ordinal)
            .question("{{Front}}")
            .answer("{{FrontSide}}")
            .build();
    }
}
