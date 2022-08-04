package kbomberx.concurrency.atomic

class ReadWriteAtomicVar<T>(private var value : T) : AbstractReadWriteAtomicVar<T>() {

    private val nonConcurrentVar = NonConcurrentVar(this)

    override suspend fun atomicSet(value: T) {
        super.writeOperation {
            this.value = value
        }
    }

    override suspend fun atomicGet(): T {
        try {
            beginReadOperation()
            return value
        } finally {
            endReadOperation()
        }
    }

    override suspend fun atomicUseValue(block: suspend (T) -> Unit) {
        super.readOperation {
            block(value)
        }
    }

    override suspend fun <R> atomicMap(mapper: (T) -> R): R {
        try {
            beginReadOperation()
            return mapper(value)
        } finally {
            endReadOperation()
        }
    }

    /**
     * An internal class that can be used in some special methods
     * of [SimpleAtomicVar]. Its constructor is internal
     */
    class NonConcurrentVar<T> internal constructor(private val atomicVar: ReadWriteAtomicVar<T>) {

        /**
         * Sets the value of this [ReadWriteAtomicVar] without caring about concurrency.
         * This method can only be used in special methods of this atomic value
         * @param newValue the new value to be set
         */
        fun set(newValue : T) {
            atomicVar.value = newValue
        }

        /**
         * Returns the current value of this [ReadWriteAtomicVar] without caring about concurrency.
         * This method can only be used in special methods of this atomic value
         * @return the current value of this object
         */
        fun get() : T {
            return atomicVar.value
        }
    }

    /**
     * Atomically performs an operation with the value associated with
     * this object. The internal value is accessible thanks to the receiver
     * [NonConcurrentVar] that exposes the [NonConcurrentVar.set] and [NonConcurrentVar.get]
     * that are non-concurrent operations safe to be used in this block
     * @param block the action that will be atomically invoked
     */
    suspend fun atomicWithValue(block : suspend NonConcurrentVar<T>.() -> Unit) {
        writeOperation {
            nonConcurrentVar.block()
        }
    }
}