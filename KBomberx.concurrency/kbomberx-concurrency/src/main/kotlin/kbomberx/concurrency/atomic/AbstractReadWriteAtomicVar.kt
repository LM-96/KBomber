package kbomberx.concurrency.atomic

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * An abstract class that realizes the *Read-Write* concurrent model: *read* operations
 * can perform concurrently but *write* operations are atomic.
 * This classes use two mutex to realize this model.
 */
abstract class AbstractReadWriteAtomicVar<T> : AtomicVar<T> {

    /**
     * The mutex for the *read* operations
     */
    private val readMutex = Mutex()

    /**
     * The mutex for the *write* operations
     */
    private val writeMutex = Mutex()

    /**
     * The number of the coroutines that are currently reading
     */
    private var countl = 0

    /**
     * Starts a *read* operation in a safe way *registering* the current coroutine
     * that is performing the read work. Multiple *read* operations can be started concurrently.
     * The first coroutine that calls this method also block every *write* operation until
     * the last registered coroutine ends its *read* work by calling [endReadOperation]
     */
    suspend fun beginReadOperation() {
        readMutex.withLock {
            countl++
            if(countl == 1) writeMutex.lock()
        }
    }

    /**
     * Ends the *read* operation in a safe way *de-registering* the current coroutine.
     * If no registered coroutine are present and this is the last that calls this method,
     * the *write* operations will be allowed at the end of the execution of this function
     */
    suspend fun endReadOperation() {
        readMutex.withLock {
            countl--
            if(countl == 0) writeMutex.unlock()
        }
    }

    /**
     * Atomically performs a write work on this var.
     * Notice that during the write operation, no other works (both *read* or *write*) are
     * allowed on this object.
     * @param action the write action that is atomically performed
     */
    suspend fun writeOperation(action : () -> Unit) {
        writeMutex.withLock {
            action.invoke()
        }
    }

    /**
     * Starts a *write* operation on this object. The coroutine that start the work also
     * prevent any other operation from other coroutines (both *read* and *write*) on this
     * object until it calls the [endWriteOperation] method.
     */
    suspend fun beginWriteOperation() {
        writeMutex.lock()
    }

    /**
     * Ends the *write* operation in a safe way.
     * At the end of this functions, all the operation on this object
     * are allowed following the concurrent model of this object
     */
    suspend fun endWriteOperation() {
        writeMutex.unlock()
    }

    suspend fun readOperation(action : () -> Unit) {
        try {
            beginReadOperation()
            action.invoke()
        } finally {
            endReadOperation()
        }
    }

    override suspend fun lock() {
        beginWriteOperation()
    }

    override suspend fun unlock() {
        endWriteOperation()
    }

}