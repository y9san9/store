# Mutable Store

> Make mutable objects safe!

I don't like mutability. Whenever it's possible I use immutable objects and JVM
is pretty damn good optimized to handle a lot of object allocations. But there
are rare cases when you just can't copy objects to mutate them.

Here is my case:

* Just a single class instance is around 20 MB
* Update rates are very fast (up to 8 times per millisecond on peaks)
* Working machine has 272 GB (not MB) of ORM, still can't handle copies

This library allows me to encapsulate Shared Mutable State and work with it
transactionally with safe reads. No Mutexes or Semaphores for you to manage.

## Example

Simple usage scenario looks like this:

```kotlin
suspend fun main() = coroutineScope {
    val store = createStore(scope = this)
    store.collect { counter ->
        // This IS thread-safe, while collector is
        // working it hangs all pending transactions
        if (counter > 0) {
            println(counter)
        }
    }
}

// Expose Read-only type
fun createStore(scope: CoroutineScope): Store<Counter> {
    val store = MutableStore(MutableCounter(i = 0))
    // launch a coroutine that modifies counter
    scope.launch {
        while (true) {
            store.transaction { counter -> counter.i++ }
        }
    }
    return store
}

interface Counter {
    val i: Int
}

class MutableCounter(
    override var i: Int
) : Counter
```

**But this example will not compile**. Usually when you have this usecase it's
some enormous amount of data that is stored in memory. And it's a lot of
different data as well. Modifications could happen anywhere, how to know –
where? For that matter there is builtin support for diffs. Here is an updated
example that will compile:

```kotlin
suspend fun main() = coroutineScope {
    createStore(scope = this)
        // batch or drop is supported
        .batchUnstable()
         // filter by diffs (generic filter function is also present)
        .filterDiff<_, Counter.Diff.Increment>()
        .collect { counter, _ ->
            // This IS thread-safe, while collectors are
            // working it hangs all pending transactions.
            // Multiple collectors can run in parallel.
            if (counter.i > 0) {
                println(counter)
            }
        }
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

data class MutableCounter(
    override var i: Int
) : Counter
```

Please remember that this library is not for simple cases. You still should
prefer immutable objects whenever it's possible. But for that usecases where
you will end up using this library the amount of boilerplate is acceptable.

## Install

Add this to your `build.gradle` to install libary:

```gradle
implementation("me.y9san9.store:core:$version")
```

Or this to your `libs.versions.toml`:

```toml
[versions]
y9san9-store = "$version"

[libraries]
y9san9-store = { module = "me.y9san9.store", version = "y9san9-store" }
```

`$version` should be the last from GitHub Releases.

## Performance is Experimental

At this point of time I present the API of this library to public judge. It's
implementation is still very scratchy and I know the exact moments where I
could've done better. But it works for my very data-intensive case, so PRs are
welcome.
