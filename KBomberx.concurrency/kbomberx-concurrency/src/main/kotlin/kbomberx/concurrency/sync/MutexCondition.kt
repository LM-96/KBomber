package kbomberx.concurrency.sync

import kotlinx.coroutines.sync.Mutex

fun Mutex.newCondition() : CoroutineCondition {
    return CoroutineCondition(this)
}