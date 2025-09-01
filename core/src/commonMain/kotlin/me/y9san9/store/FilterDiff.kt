@file:Suppress("ktlint")

package me.y9san9.store

/**
 * Filters transactions and only invokes collectors with diffs of specified
 * type.
 *
 * Uses [typeClass] to infer types. Use other variant of this function of diff
 * is not implementing it's typeClass.
 *
 * It's impossible to determine stability after that operation.
 */
public inline fun <TValue, reified TDiff> Store<TValue, *>.filterDiff(
    @Suppress("unused")
    typeClass: StoreDiff.EntryTypeClass<TDiff>,
): Store<TValue, TDiff> = filterDiff()

/**
 * Filters transactions and only invokes collectors with diffs of specified
 * type.
 *
 * It's impossible to determine stability after that operation.
 */
public inline fun <TValue, reified TDiff> Store<TValue, *>.filterDiff(): Store<TValue, TDiff> =
    DiffFilteringStore(
        upstream = this,
        filter = { diff -> diff.filterIsInstance<TDiff>() },
    )

@PublishedApi
internal class DiffFilteringStore<TValue, TDiff>(
    private val upstream: Store<TValue, *>,
    private val filter: (List<Any?>) -> List<TDiff>,
) : Store<TValue, TDiff> {
    override val isStable: Nothing? get() = null

    override suspend fun collect(collector: StoreCollector<TValue, TDiff>) {
        upstream.collect { value, diff ->
            val filteredDiff = filter(diff.entries)
            if (filteredDiff.isNotEmpty()) {
                collector.hang(value, StoreDiff(filteredDiff))
            }
        }
    }
}
