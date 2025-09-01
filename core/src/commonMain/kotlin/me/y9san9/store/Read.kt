package me.y9san9.store

/**
 * Single-time read request that allows to extract current value from the
 * store. Diff is not returned, since for the first value it's always
 * StoreDiff.Empty.
 */
public suspend fun <TValue, R> Store<TValue, *>.read(block: (TValue) -> R): R {
    val owner = Any()
    var result: R? = null
    try {
        collect { element, _ ->
            result = block(element)
            throw ReadAbortException(owner)
        }
    } catch (exception: ReadAbortException) {
        if (exception.owner !== owner) {
            throw exception
        }
    }
    @Suppress("UNCHECKED_CAST")
    return result as R
}

private class ReadAbortException(val owner: Any) : Throwable()
