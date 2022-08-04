package kbomberx.concurrency.atomic

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A variable that is coroutine safe.
 * Notice that this class uses an internal [Mutex] to manage the concurrency
 */
class SimpleAtomicVar<T>(private var value : T) : AtomicVar<T> {
    
    private val mutex = Mutex()
    private val nonConcurrentVar = NonConcurrentVar(this)

    /**
     * An internal class that can be used in some special methods
     * of [SimpleAtomicVar]. Its constructor is internal
     */
    class NonConcurrentVar<T> internal constructor(private val atomicVar: SimpleAtomicVar<T>) {

        /**
         * Sets the value of this [SimpleAtomicVar] without caring about concurrency.
         * This method can only be used in special methods of this atomic value
         * @param newValue the new value to be set
         */
        fun set(newValue : T) {
            atomicVar.value = newValue
        }

        /**
         * Returns the current value of this [SimpleAtomicVar] without caring about concurrency.
         * This method can only be used in special methods of this atomic value
         * @return the current value of this object
         */
        fun get() : T {
            return atomicVar.value
        }
    }

    override suspend fun atomicSet(value: T) {
        mutex.withLock {
            this.value = value
        }
    }

    override suspend fun atomicGet() : T {
        return mutex.withLock {
            value
        }
    }

    override suspend fun lock() {
        mutex.lock()
    }

    override suspend fun unlock() {
        mutex.unlock()
    }

    /**
     * Atomically performs an operation with the value associated with
     * this object. The internal value is accessible thanks to the receiver
     * [NonConcurrentVar] that exposes the [NonConcurrentVar.set] and [NonConcurrentVar.get]
     * that are non-concurrent operations safe to be used in this block
     * @param block the action that will be atomically invoked
     */
    suspend fun atomicWithValue(block : suspend NonConcurrentVar<T>.() -> Unit) {
        mutex.withLock {
            nonConcurrentVar.block()
        }
    }

    override suspend fun atomicUseValue(block : suspend (T) -> Unit) {
        mutex.withLock {
            block(value)
        }
    }

    override suspend fun <R> atomicMap(mapper : (T) -> R) : R {
        return mutex.withLock {
            mapper(value)
        }
    }
}