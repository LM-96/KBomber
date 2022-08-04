package kbomberx.concurrency.atomic

import kotlinx.coroutines.sync.Mutex

/**
 * A variable shared between coroutines that can be used atomically in a safe way.
 * The concurrent policies are decided by the implementing class
 */
interface AtomicVar<T> {

    /**
     * Atomically sets this object
     * @param value the value to be set
     */
    suspend fun atomicSet(value: T)

    /**
     * Atomically get this object
     * @return the current value of this object
     */
    suspend fun atomicGet() : T

    /**
     * Atomically performs an operation with the value associated with
     * this object
     * @param block the action that will be atomically invoked
     */
    suspend fun atomicUseValue(block : suspend (T) -> Unit)

    /**
     * Atomically map this object
     * @param mapper the function that transform the value of this object
     * @return the mapped object
     */
    suspend fun <R> atomicMap(mapper : (T) -> R) : R

    /**
     * Locks the mutex associated with this object.
     * After this invocation only the coroutine that hold the lock
     * can perform the correspondent unlock operation.
     * **Notice that if a coroutine locks this object then no one can
     * call methods over this, not even itself** (except for the unlock method as said)
     * This method can be used to block other coroutines until the owner has need.
     * If the coroutine that calls this method has to perform action on this object,
     * this method should not be used: use [atomicUseValue] instead.
     *
     * See [Mutex.lock] for additional details
     */
    suspend fun lock()

    /**
     * The dual method of the [lock].
     * This method unlock the mutex associated with this object allowing other
     * coroutines to invoke the other operations.
     * See [Mutex.unlock] for additional details
     */
    suspend fun unlock()


}