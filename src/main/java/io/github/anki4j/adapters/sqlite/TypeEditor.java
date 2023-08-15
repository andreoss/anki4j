/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

import io.github.anki4j.domain.NoteType;
import io.github.anki4j.domain.NoteTypeId;
import io.github.anki4j.ports.NoteTypes;
import java.util.List;

/**
 * The note types of a collection, open for change.
 *
 * @since 0.1.0
 */
final class TypeEditor implements NoteTypes {

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * New editor of the note types.
     *
     * @param state Contents of the file
     */
    TypeEditor(final CollectionState state) {
        this.state = state;
    }

    @Override
    public NoteType addNoteType(final NoteType type) {
        final NoteType added = NoteType.builder()
            .id(new NoteTypeId(TypeOps.nextid(this.state.getNotetypelist())))
            .name(type.getName())
            .fields(type.getFields())
            .templates(type.getTemplates())
            .checks(type.getChecks())
            .css(type.getCss())
            .sortfield(type.getSortfield())
            .deck(type.getDeck())
            .modified(DeckOps.now())
            .usn(type.getUsn())
            .build();
        this.state.getNotetypelist().add(added);
        return added;
    }

    @Override
    public void updateNoteType(final NoteType type) {
        final List<NoteType> types = this.state.getNotetypelist();
        for (int index = 0; index < types.size(); index += 1) {
            final NoteType current = types.get(index);
            if (current.getId().getValue() == type.getId().getValue()) {
                types.set(
                    index,
                    NoteType.builder()
                        .id(type.getId())
                        .name(type.getName())
                        .fields(type.getFields())
                        .templates(type.getTemplates())
                        .checks(type.getChecks())
                        .css(type.getCss())
                        .sortfield(type.getSortfield())
                        .deck(type.getDeck())
                        .modified(DeckOps.now())
                        .usn(type.getUsn())
                        .build()
                );
                return;
            }
        }
        throw new IllegalArgumentException(
            String.format("no note type with id %d", type.getId().getValue())
        );
    }

    @Override
    public void deleteNoteType(final NoteTypeId id) {
        boolean found = false;
        for (int index = 0; index < this.state.getNotetypelist().size();
            index += 1) {
            final long current = this.state.getNotetypelist()
                .get(index).getId().getValue();
            if (current == id.getValue()) {
                this.state.getNotetypelist().remove(index);
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException(
                String.format("no note type with id %d", id.getValue())
            );
        }
    }
}
