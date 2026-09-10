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
final class TypeChanges implements NoteTypes {

    /**
     * Contents of the collection.
     */
    private final CollectionState state;

    /**
     * The models of the collection.
     */
    private final Models models;

    /**
     * The clock of the collection.
     */
    private final Clock clock;

    /**
     * New editor of the note types.
     *
     * @param state Contents of the file
     */
    TypeChanges(final CollectionState state) {
        this(
            state,
            new Models(
                state.getConnection(),
                state.getNotetypelist(),
                state.getHeader().models()
            ),
            new Clock(System::currentTimeMillis)
        );
    }

    /**
     * New editor of the note types.
     *
     * @param state Contents of the file
     * @param models The models of the collection
     * @param clock The clock of the collection
     */
    TypeChanges(
        final CollectionState state,
        final Models models,
        final Clock clock
    ) {
        this.state = state;
        this.models = models;
        this.clock = clock;
    }

    @Override
    public NoteType addNoteType(final NoteType type) {
        final NoteType added = new NoteType(
            new NoteTypeId(this.models.nextid()),
            type.getName(),
            type.getFields(),
            type.getTemplates(),
            type.getChecks(),
            type.getCss(),
            type.getSortfield(),
            type.getDeck(),
            this.clock.now(),
            type.getUsn()
        );
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
                    new NoteType(
                        type.getId(),
                        type.getName(),
                        type.getFields(),
                        type.getTemplates(),
                        type.getChecks(),
                        type.getCss(),
                        type.getSortfield(),
                        type.getDeck(),
                        this.clock.now(),
                        type.getUsn()
                    )
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
