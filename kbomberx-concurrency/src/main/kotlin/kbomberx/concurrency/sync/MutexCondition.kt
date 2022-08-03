package kbomberx.concurrency.sync

import kotlinx.coroutines.sync.Mutex

/**
 * Creates a new condition for this [Mutex]
 * @return the new [CoroutineCondition] instance
 */
fun Mutex.newCondition() : CoroutineCondition {
    return CoroutineCondition(this)
}