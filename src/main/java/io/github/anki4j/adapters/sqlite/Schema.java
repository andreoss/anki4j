/**
 * Adapter over the collection database.
 *
 * @since 0.1.0
 */
package io.github.anki4j.adapters.sqlite;

/**
 * The schema of a collection, which the reference implementation opens or
 * refuses.
 *
 * <p>The reference implementation refuses a collection below the schema
 * eleven, of the schema twelve or thirteen and above the schema
 * eighteen. The schema twelve and the schema thirteen are refused because
 * the definition of the schema changed and the collection must be shut
 * down cleanly to return to the schema eleven first.</p>
 *
 * @since 0.1.0
 */
final class Schema {

    /**
     * The oldest schema the reference implementation opens.
     */
    private static final int OLDEST = 11;

    /**
     * The newest schema the reference implementation opens.
     */
    private static final int NEWEST = 18;

    /**
     * The first schema the reference implementation refuses between the
     * oldest and the newest.
     */
    private static final int REFUSED_FIRST = 12;

    /**
     * The last schema the reference implementation refuses between the
     * oldest and the newest.
     */
    private static final int REFUSED_LAST = 13;

    /**
     * Version of the schema.
     */
    private final int version;

    /**
     * New schema of a collection.
     *
     * @param version Version of the schema
     */
    Schema(final int version) {
        this.version = version;
    }

    /**
     * Whether the reference implementation opens a collection of this schema.
     *
     * @return True when the schema is opened
     */
    boolean supported() {
        final boolean refused = this.version >= Schema.REFUSED_FIRST
            && this.version <= Schema.REFUSED_LAST;
        return !refused && this.version >= Schema.OLDEST
            && this.version <= Schema.NEWEST;
    }

    /**
     * The report of this schema the reference implementation refuses.
     *
     * @return Report of the schema
     */
    String report() {
        return String.format(
            "the schema %d of the collection is not supported by the reference",
            this.version
        );
    }
}
