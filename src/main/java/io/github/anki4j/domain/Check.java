/**
 * Value of the domain model.
 *
 * @since 0.1.0
 */
package io.github.anki4j.domain;

import java.util.List;
import lombok.Builder;
import lombok.Value;

/**
 * A check on the field of a note type.
 *
 * @since 0.1.0
 */
@Value
@Builder
public final class Check {

    /**
     * Ordinal of the field the check holds.
     */
    private int field;

    /**
     * Kind of the check: any, all or none.
     */
    private String kind;

    /**
     * Values the field may hold.
     */
    private List<String> values;
}
