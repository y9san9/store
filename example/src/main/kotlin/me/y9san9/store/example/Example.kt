package me.y9san9.store.example

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.y9san9.store.MutableStore
import me.y9san9.store.Store
import me.y9san9.store.StoreDiff
import me.y9san9.store.read

suspend fun main() = coroutineScope {
    val store = createStore(scope = this)
    // store
    //     .batchUnstable()
    //     .filterDiff<_, Counter.Diff.Increment>()
    //     .collect { counter, _ ->
    //         // This IS thread-safe, while collector is
    //         // working is hangs all pending transactions
    //         if (counter.i > 0) {
    //             println(counter)
    //         }
    //     }

    try {
        val job = launch {
            store.collect { counter, _ ->
            }
        }
        delay(500)
        job.cancelAndJoin()
    } catch (throwable: Throwable) { }
    store.read { counter ->
        println(counter)
    }
    cancel()
}

// Expose Read-only type
fun createStore(scope: CoroutineScope): Store<Counter, Counter.Diff> {
    val store = MutableStore(MutableCounter(i = 0)).diff<Counter.Diff>()
    // launch a coroutine that modifies counter
    scope.launch {
        while (true) {
            store.transaction { counter ->
                counter.i++
                StoreDiff.of(Counter.Diff.Increment)
            }
        }
    }
    return store
}

interface Counter {
    val i: Int

    sealed interface Diff {
        data object Increment : Diff
    }
}

data class MutableCounter(override var i: Int) : Counter
