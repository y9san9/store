package me.y9san9.store

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

internal class MutableStoreImpl<TValue, TDiff>(private val value: TValue) :
    MutableStore<TValue, TDiff> {
    // TODO: Atomics?
    private var pendingTransactions = 0
    private val pendingMutex = Mutex()

    private val transactionMutex = Mutex()

    // It's fine to call transaction/collect of
    // some other instance of MutableStore.
    private val deadlockCheck = DeadlockCheck()

    private val collectors = mutableSetOf<Collector>()

    override val isStable: Boolean get() = pendingTransactions == 0

    override suspend fun collect(
        collector: StoreCollector<TValue, TDiff>,
    ): Nothing {
        if (coroutineContext[deadlockCheck.key] != null) {
            error(
                "You can't call collect/transaction from within another collect/transaction. This is a deadlock",
            )
        }
        withContext(deadlockCheck) {
            coroutineScope {
                val scope = this
                // Never completes normally. Responsible for failing call
                // site when exception occurs
                val completion = CompletableDeferred<Nothing>()
                val collector = Collector(
                    realCollector = collector,
                    scope = scope,
                    completion = completion,
                )

                transactionMutex.withLock {
                    collectors += collector
                    // TODO: optimize initial collectors
                    try {
                        collector.realCollector.hang(value, StoreDiff.Empty)
                    } catch (throwable: Throwable) {
                        withContext(NonCancellable) {
                            collectors -= collector
                        }
                        throw throwable
                    }
                }
                completion.await()
            }
        }
    }

    override suspend fun transaction(
        transaction: StoreTransaction<TValue, TDiff>,
    ) {
        if (coroutineContext[deadlockCheck.key] != null) {
            error(
                "You can't call collect/transaction from within collect/transaction. This is a deadlock",
            )
        }
        withContext(deadlockCheck) {
            pendingMutex.withLock { pendingTransactions++ }
            transactionMutex.withLock {
                // This transaction is not pending anymore, but active.
                pendingMutex.withLock { pendingTransactions-- }

                // Intentionally do not catch errors.
                // Transaction must not fail or is responsible
                // for state rollback
                val diff = transaction.update(value)
                val jobs = collectors.map { collector ->
                    collector.scope.launch(deadlockCheck) {
                        runCatching {
                            collector.realCollector.hang(value, diff)
                        }.onFailure { throwable ->
                            withContext(NonCancellable) {
                                collectors -= collector
                                val completion = collector.completion
                                completion.completeExceptionally(throwable)
                            }
                        }
                    }
                }
                jobs.joinAll()
            }
        }
    }

    private inner class Collector(
        val realCollector: StoreCollector<TValue, TDiff>,
        val scope: CoroutineScope,
        val completion: CompletableDeferred<Nothing>,
    )

    private class DeadlockCheck : CoroutineContext.Element {
        override val key: CoroutineContext.Key<DeadlockCheck> =
            object : CoroutineContext.Key<DeadlockCheck> {}
    }
}
