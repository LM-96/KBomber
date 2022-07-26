package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope

/**
 * An implementation for [AbstractCoroutineSharedList] that use an internal [ArrayList]
 */
class CoroutineSharedArrayList<T : Any>(scope : CoroutineScope = SharedScope) : AbstractCoroutineSharedList<T>(
    arrayListOf(), scope) {
}