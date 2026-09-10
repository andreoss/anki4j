/**
 * Conformance of the library with the format.
 *
 * @since 0.1.0
 */
package io.github.anki4j.conformance;

import io.github.anki4j.adapters.sqlite.SqliteCollection;
import io.github.anki4j.domain.Card;
import io.github.anki4j.domain.Deck;
import io.github.anki4j.domain.Note;
import io.github.anki4j.domain.NoteField;
import io.github.anki4j.domain.NoteTemplate;
import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.Schedule;
import java.nio.file.Path;
import java.sql.SQLException;

/**
 * The contents of a collection, as a report another reader can build too.
 *
 * @since 0.1.0
 */
final class Report {

    /**
     * Utility class.
     */
    private Report() {
    }

    /**
     * The contents of a collection.
     *
     * @param path Path of the collection
     * @return Report of the contents
     * @throws SQLException When the collection cannot be read
     */
    static String text(final Path path) throws SQLException {
        try (SqliteCollection collection = new SqliteCollection(path)) {
            final StringBuilder report = new StringBuilder(1024)
                .append(collection.schemaVersion()).append('\n');
            for (final Deck deck : collection.decks()) {
                report.append("deck ").append(deck.getId().getValue())
                    .append('|').append(deck.getName())
                    .append('|').append(deck.getModified())
                    .append('|').append(deck.getUsn()).append('\n');
            }
            for (final NoteType type : collection.noteTypes()) {
                report.append("type ").append(type.getId().getValue())
                    .append('|').append(type.getName());
                for (final NoteField field : type.getFields()) {
                    report.append('|').append(field.getName())
                        .append(',').append(field.getOrdinal());
                }
                for (final NoteTemplate template : type.getTemplates()) {
                    report.append("|t ").append(template.getName())
                        .append(',').append(template.getOrdinal())
                        .append(',').append(template.getQuestion())
                        .append(',').append(template.getAnswer());
                }
                report.append('\n');
            }
            for (final Note note : collection.notes()) {
                report.append("note ").append(note.getId().getValue())
                    .append('|').append(note.getGuid())
                    .append('|').append(note.getNotetype().getValue())
                    .append('|').append(String.join(" ", note.getTags()))
                    .append('|').append(String.join("", note.getFields()))
                    .append('|').append(note.getChecksum())
                    .append('|').append(note.getFlags()).append('\n');
            }
            for (final Card card : collection.cards()) {
                final Schedule state = card.getSchedule();
                report.append("card ").append(card.getId().getValue())
                    .append('|').append(card.getNote().getValue())
                    .append('|').append(card.getDeck().getValue())
                    .append('|').append(card.getOrdinal())
                    .append('|').append(state.getType())
                    .append('|').append(state.getQueue())
                    .append('|').append(state.getDue())
                    .append('|').append(state.getInterval())
                    .append('|').append(state.getFactor())
                    .append('|').append(state.getRepetitions())
                    .append('|').append(state.getLapses())
                    .append('|').append(state.getLeft())
                    .append('|').append(state.getOriginaldue())
                    .append('|').append(state.getOriginaldeck()).append('\n');
            }
            return report.toString();
        }
    }
}
