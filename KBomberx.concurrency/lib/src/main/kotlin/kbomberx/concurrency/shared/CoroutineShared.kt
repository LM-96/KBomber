package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

val SharedScope = CoroutineScope(EmptyCoroutineContext + CoroutineName("SharedScope"))

fun <T> newSharedValue(initial : T, scope : CoroutineScope = SharedScope) : CoroutineSharedValue<T> {
    return CoroutineSharedValue(initial, scope)
}

fun <T> T.asCoroutineShared(scope: CoroutineScope = SharedScope) : CoroutineSharedValue<T> {
    return CoroutineSharedValue(this, scope)
}