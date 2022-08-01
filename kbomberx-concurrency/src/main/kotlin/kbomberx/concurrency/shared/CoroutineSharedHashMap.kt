package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope
import java.util.*

/**
 * An implementation for [AbstractCoroutineSharedMap] that use an internal [HashMap]
 */
class CoroutineSharedHashMap<K : Any, V : Any>(scope : CoroutineScope = SharedScope) :
    AbstractCoroutineSharedMap<K, V>(hashMapOf(), scope) {
}