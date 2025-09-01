@file:Suppress("ktlint")

package me.y9san9.store

/**
 * Creates a new Store. Collectors on that Store will not receive all updates
 * in case if Collectors are slower than transactions (e.g. too many updates).
 *
 * Just like [dropUnstable], but instead of dropping updates they are
 * stored in a separate list and then received at once.
 *
 * Important notice: diffs might be outdated! Keep in mind that there is only
 *                   a single instance of state. For example: Diff.Add(element)
 *                   doesn't mean that element **was** at state with that
 *                   parameters when added. This element could me mutated by
 *                   that point of time. Ideally when using this function
 *                   Stores should be created with that in mind and never
 *                   provide diffs with mutable references inside.
 */
public fun <TValue, TDiff> Store<TValue, TDiff>.batchUnstable(): Store<TValue, TDiff> =
    BatchStore(upstream = this)

private class BatchStore<TValue, TDiff>(
    private val upstream: Store<TValue, TDiff>,
) : Store<TValue, TDiff> {
    // This Store can't have pending transactions
    override val isStable: Boolean? get() = true

    override suspend fun collect(collector: StoreCollector<TValue, TDiff>) {
        val diffs = mutableListOf<TDiff>()
        upstream.collect { value, diff ->
            val stable = upstream.isStable
                ?: error(
                    "Upstream stability is impossible to determine. Please consider to move all filtering operations afterwards",
                )
            diffs += diff.entries
            if (stable) {
                collector.hang(value, StoreDiff(diffs.toList()))
                diffs.clear()
            }
        }
    }
}
