/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import java.util.List;

/**
 * A check on the field of a note type.
 *
 * @since 0.1.0
 */
public final class Check {

    /**
     * Ordinal of the field the check holds.
     */
    private final int field;

    /**
     * Kind of the check: any, all or none.
     */
    private final String kind;

    /**
     * Values the field may hold.
     */
    private final List<String> values;

    /**
     * All fields of the check.
     *
     * @param field Ordinal of the field the check holds
     * @param kind Kind of the check: any, all or none
     * @param values Values the field may hold
     */
    public Check(
        final int field, final String kind, final List<String> values
    ) {
        this.field = field;
        this.kind = kind;
        this.values = values;
    }

    /**
     * Ordinal of the field the check holds.
     *
     * @return Ordinal of the field
     */
    public int getField() {
        return this.field;
    }

    /**
     * Kind of the check.
     *
     * @return Kind of the check
     */
    public String getKind() {
        return this.kind;
    }

    /**
     * Values the field may hold.
     *
     * @return Values of the check
     */
    public List<String> getValues() {
        return this.values;
    }
}
