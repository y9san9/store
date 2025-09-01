package me.y9san9.store

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
public fun interface StoreTransaction<in TValue, out TDiff> {
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
    public suspend fun update(value: TValue): StoreDiff<TDiff>
}
