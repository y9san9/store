package me.y9san9.store

/**
 * MutableStore is a safe way to work with mutable data.
 *
 * I am generally all in for immutability. But there are cases when your
 * structure is just very expensive to copy. For me I have this case for a
 * first time over the course of my 5 years development experience.
 *
 * This data structure offers:
 *
 * * Transactions to mutate a piece of mutable data (they are run sequentially)
 * * Collectors that are run in parallel as soon as mutation happened
 * * Diffs that allow to narrow down where the mutation happened
 */
public interface MutableStore<TValue, TDiff> : Store<TValue, TDiff> {
    /**
     * Store is considered 'stable' when there is no pending updates at the
     * moment. That is useful to know when you want to drop updates that are
     * happening in-between.
     */
    override val isStable: Boolean

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
    override suspend fun collect(collector: StoreCollector<TValue, TDiff>)

    /**
     * Transactions are executed sequentially and their speed heavily depends
     * on the speed of collectors. You might want to speed up collectors (for
     * example by using [dropUnstable]/[batchUnstable]) instead of batching
     * transactions.
     *
     * Transaction should NEVER fail or it is resposible for rollback of the
     * changes. If transaction fails with exception, collectors are not
     * receiving the update.
     */
    public suspend fun transaction(transaction: StoreTransaction<TValue, TDiff>)
}
