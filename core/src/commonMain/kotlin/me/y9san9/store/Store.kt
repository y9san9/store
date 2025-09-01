package me.y9san9.store

/**
 * Store is not an inheritor of Flow interface because calling `.first()` or
 * something like that is unsafe.
 */
public interface Store<out TValue, out TDiff> {
    /**
     * Store is considered 'stable' when there is no pending transactions at the
     * moment. That is useful to know if you want to drop/batch transactions
     * that are happening in-between.
     *
     * Null value of this property means that stability is impossible to
     * determine. This is usually due to filtering. If transactions are filtered
     * out, it's impossible to know in advance whether pending transactions
     * will be filtered or not.
     */
    public val isStable: Boolean?

    /**
     * [collector] is called whenever internal state is updated.
     *
     * **Note:** Collector needs to be fast! All writes are delayed until all
     *           the collectors are run. Consider to use [batchUnstable] or
     *           [dropUnstable] if collector is slow. Multiple collectors
     *           can run in parallel.
     *
     *  To remove collector, just cancel the coroutine.
     */
    public suspend fun collect(collector: StoreCollector<TValue, TDiff>)
}
