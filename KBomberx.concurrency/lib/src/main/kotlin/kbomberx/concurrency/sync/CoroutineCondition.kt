package kbomberx.concurrency.sync

import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A condition for *Kotlin* coroutines that is similar
 * to *Java* [java.util.concurrent.locks.Condition]
 */
class CoroutineCondition internal constructor(private val mutex : Mutex) {

    private val continuations = ArrayDeque<Continuation<Unit>>()

    /**
     * Causes the current coroutine to wait until it is signalled.
     * The mutex associated with this Condition is atomically released and the
     * current coroutine becomes disabled for coroutine scheduling purposes and lies dormant until
     * some other coroutine invokes the [signal] or [signalAll] method on this condition
     */
    suspend fun await() {
        if(!mutex.isLocked)
            throw IllegalStateException("mutex is not locked")

        suspendCoroutine {
            continuations.add(it)
            mutex.unlock()
        }
        mutex.lock()
    }

    /**
     * Wakes up one waiting thread.
     * If any coroutines are waiting on this condition then one is selected for waking up.
     * That coroutine must then re-acquire the mutex before returning from `await`.
     */
    suspend fun signal() {
        if (!mutex.isLocked)
            throw IllegalStateException("mutex is not locked")
        resumeFirst()

    }

    /**
     * Wakes up all waiting coroutines.
     * If any coroutines are waiting on this condition then they are all woken up.
     * Each coroutine must re-acquire the lock before it can return from await.
     */
    suspend fun signalAll() {
        if (!mutex.isLocked)
            throw IllegalStateException("mutex is not locked")

        while(continuations.isNotEmpty())
            resumeFirst()
    }

    private fun resumeFirst() {
        val continuation = continuations.removeFirstOrNull()
        if(continuation != null) {
            if(continuation.context.isActive) {
                continuation.resume(Unit)
            }
        }
    }

}