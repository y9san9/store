package me.y9san9.store

/**
 * Filters transactions and only invokes collectors after [block] returned
 * true.
 *
 * It's impossible to determine stability after that operation.
 */
public fun <TValue, TDiff> Store<TValue, TDiff>.filter(
    block: (TValue, StoreDiff<TDiff>) -> Boolean,
): Store<TValue, TDiff> = FilteringStore(upstream = this, block)

private class FilteringStore<TValue, TDiff>(
    private val upstream: Store<TValue, TDiff>,
    private val block: (TValue, StoreDiff<TDiff>) -> Boolean,
) : Store<TValue, TDiff> {
    override val isStable: Nothing? get() = null

    override suspend fun collect(collector: StoreCollector<TValue, TDiff>) {
        upstream.collect { value, diff ->
            if (block(value, diff)) {
                collector.hang(value, diff)
            }
        }
    }
}
