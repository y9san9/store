@file:Suppress("ktlint")

package me.y9san9.store

/**
 * Auto-infer generic. With that solution:
 *
 * val store = MutableStore(value).diff<MyDiff>()
 *
 * Without that solution:
 *
 * val store: MutableStore<MyValue, MyDiff> = MutableStore(value)
 *
 * Or (in my subjective opinion looks worse):
 *
 * val store = MutableStore<_, MyDiff>(value)
 */
public class MutableStoreValue<TValue>(public val value: TValue) {
    /**
     * Specify a second generic for MutableStore
     */
    public fun <TDiff> diff(): MutableStore<TValue, TDiff> = MutableStoreImpl(value)
}

/**
 * Auto-infer generic. With that solution:
 *
 * val store = MutableStore(value).diff<MyDiff>()
 *
 * Without that solution:
 *
 * val store: MutableStore<MyValue, MyDiff> = MutableStore(value)
 *
 * Or (in my subjective opinion looks worse):
 *
 * val store = MutableStore<_, MyDiff>(value)
 */
public fun <TValue> MutableStore(value: TValue): MutableStoreValue<TValue> =
    MutableStoreValue(value)
