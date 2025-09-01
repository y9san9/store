package me.y9san9.store

/**
 * Usually when you need to use this library it's some enormous amount of data
 * that is stored in memory. And it's a lot of different data as well.
 * Modifications could happen anywhere, how to know – where? For that matter
 * there is builtin support for diffs.
 */
public data class StoreDiff<out TEntry>(val entries: List<TEntry>) {

    /**
     * Used as a helper to infer type info about diffs. See [filterDiff] for
     * example.
     */
    public interface EntryTypeClass<TEntry>

    public companion object {
        /**
         * Empty diff. It is always emitted immediately after `collect`
         * invocation.
         */
        public val Empty: StoreDiff<Nothing> = StoreDiff(emptyList())

        public fun <T> of(vararg entries: T): StoreDiff<T> =
            StoreDiff(entries.toList())
    }
}
