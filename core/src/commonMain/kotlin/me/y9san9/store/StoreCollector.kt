package me.y9san9.store

/**
 * Collector needs to be fast! All writes are delayed until all
 * the collectors are run. Consider to use [batchUnstable] or
 * [dropUnstable] if collector is slow. Multiple collectors
 * can run in parallel.
 */
public fun interface StoreCollector<in TValue, in TDiff> {
    /**
     * Collector needs to be fast! All writes are delayed until all
     * the collectors are run. Consider to use [batchUnstable] or
     * [dropUnstable] if collector is slow. Multiple collectors
     * can run in parallel.
     */
    public suspend fun hang(value: TValue, diff: StoreDiff<TDiff>)
}
