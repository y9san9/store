@file:Suppress("ktlint")

package me.y9san9.store

/**
 * Creates a new Store. Collectors on that Store will not receive all updates
 * in case if Collectors are slower than transactions (i.e. too many updates).
 *
 * Consider the following example:
 *
 * * Transaction1 executed
 *
 * * Collector started (takes 5000ms to finish)
 *
 * * Transaction2 pending
 * * Transaction3 pending
 * * Transaction4 pending
 *
 * * Collector finished
 *
 * * Transaction2 executed
 * * Transaction3 executed
 * * Transaction4 executed
 *
 * * Collector started (only for transaction 4)
 */
public fun <TValue, TDiff> Store<TValue, TDiff>.dropUnstable(): Store<TValue, TDiff> =
    DroppingStore(upstream = this)

private class DroppingStore<TValue, TDiff>(
    private val upstream: Store<TValue, TDiff>,
) : Store<TValue, TDiff> {
    // This Store can't have pending transactions
    override val isStable: Boolean? get() = true

    override suspend fun collect(collector: StoreCollector<TValue, TDiff>) {
        upstream.collect { value, diff ->
            val stable = upstream.isStable
                ?: error(
                    "Upstream stability is impossible to determine. Please consider to move all filtering operations afterwards",
                )

            if (stable) {
                collector.hang(value, diff)
            }
        }
    }
}
