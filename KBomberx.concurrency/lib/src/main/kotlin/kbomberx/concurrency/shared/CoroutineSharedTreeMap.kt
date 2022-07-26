package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope
import java.util.*

/**
 * An implementation for [AbstractCoroutineSharedMap] that use an internal [TreeMap]
 */
class CoroutineSharedTreeMap<K : Any, V : Any>(scope : CoroutineScope = SharedScope) :
    AbstractCoroutineSharedMap<K, V>(TreeMap(), scope) {
}