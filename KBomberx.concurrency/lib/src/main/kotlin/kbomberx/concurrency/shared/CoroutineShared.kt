package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

/**
 * A scope for all the shared values
 */
val SharedScope = CoroutineScope(EmptyCoroutineContext + CoroutineName("SharedScope"))

/* SHARED VALUES ************************************************************************ */
/**
 * Creates and returns a new value that can safely be shared between coroutines
 * @param initial the initial value of the object
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the new [CoroutineSharedValue] instance
 */
fun <T : Any> newSharedValue(initial : T, scope : CoroutineScope = SharedScope) : CoroutineSharedValue<T> {
    return CoroutineSharedValue(initial, scope)
}

/**
 * Creates and returns a new value starting from this object that can safely be shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the new [CoroutineSharedValue] instance
 */
fun <T : Any> T.asCoroutineShared(scope: CoroutineScope = SharedScope) : CoroutineSharedValue<T> {
    return CoroutineSharedValue(this, scope)
}

/* SHARED LIST ************************************************************************** */
/**
 * Creates and returns a new [AbstractCoroutineSharedList] using the default implementation.
 * The result list can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [AbstractCoroutineSharedList] instance
 */
fun <T : Any> newSharedList(scope : CoroutineScope = SharedScope) : AbstractCoroutineSharedList<T> {
    return CoroutineSharedArrayList(scope)
}

/**
 * Creates and returns a new [AbstractCoroutineSharedList] using the default implementation starting
 * from this list.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [AbstractCoroutineSharedList] instance that is a copy of this list
 */
suspend fun <T : Any> List<T>.newSharedCopy(scope: CoroutineScope = SharedScope) : AbstractCoroutineSharedList<T> {
    val res = CoroutineSharedArrayList<T>(scope)
    res.addAll(this)
    return res
}

/**
 * Creates and returns a new [CoroutineSharedArrayList].
 * The result list can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedArrayList] instance
 */
fun <T : Any> newSharedArrayList(scope : CoroutineScope = SharedScope) : CoroutineSharedArrayList<T> {
    return CoroutineSharedArrayList(scope)
}

/**
 * Creates and returns a new [CoroutineSharedArrayList] starting
 * from this list.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedArrayList] instance that is a copy of this list
 */
suspend fun <T : Any> List<T>.newSharedArrayListCopy(scope: CoroutineScope = SharedScope) : CoroutineSharedArrayList<T> {
    val res = CoroutineSharedArrayList<T>(scope)
    res.addAll(this)
    return res
}

/**
 * Creates and returns a new [CoroutineSharedLinkedList].
 * The result list can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedLinkedList] instance
 */
fun <T : Any> newSharedLinkedList(scope : CoroutineScope = SharedScope) : CoroutineSharedLinkedList<T> {
    return CoroutineSharedLinkedList(scope)
}

/**
 * Creates and returns a new [CoroutineSharedArrayList] starting
 * from this list.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedLinkedList] instance that is a copy of this list
 */
suspend fun <T : Any> List<T>.newSharedLinkedListCopy(scope: CoroutineScope = SharedScope) : CoroutineSharedLinkedList<T> {
    val res = CoroutineSharedLinkedList<T>(scope)
    res.addAll(this)
    return res
}

/* SHARED MAP *************************************************************************** */
/**
 * Creates and returns a new [AbstractCoroutineSharedMap] using the default implementation.
 * The result map can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [AbstractCoroutineSharedMap] instance
 */
fun <K : Any, V : Any> newSharedMap(scope : CoroutineScope = SharedScope) : AbstractCoroutineSharedMap<K, V> {
    return CoroutineSharedHashMap(scope)
}

/**
 * Creates and returns a new [AbstractCoroutineSharedMap] using the default implementation starting
 * from this map.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [AbstractCoroutineSharedList] instance that is a copy of this list
 */
suspend fun <K: Any, V: Any> Map<K, V>.newSharedCopy(scope: CoroutineScope = SharedScope) :
        AbstractCoroutineSharedMap<K, V> {
    val res = CoroutineSharedHashMap<K, V>(scope)
    res.putAll(this)
    return res
}

/**
 * Creates and returns a new [CoroutineSharedHashMap].
 * The result map can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedHashMap] instance
 */
fun <K : Any, V : Any> newSharedHashMap(scope : CoroutineScope = SharedScope) : CoroutineSharedHashMap<K, V> {
    return CoroutineSharedHashMap(scope)
}

/**
 * Creates and returns a new [CoroutineSharedArrayList] starting
 * from this map.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedArrayList] instance that is a copy of this list
 */
suspend fun <K : Any, V : Any> Map<K, V>.newSharedHashMapCopy(scope: CoroutineScope = SharedScope) : CoroutineSharedHashMap<K, V> {
    val res = CoroutineSharedHashMap<K, V>(scope)
    res.putAll(this)
    return res
}

/**
 * Creates and returns a new [CoroutineSharedTreeMap].
 * The result map can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedTreeMap] instance
 */
fun <K : Any, V : Any> newSharedTreeMap(scope : CoroutineScope = SharedScope) : CoroutineSharedTreeMap<K, V> {
    return CoroutineSharedTreeMap(scope)
}

/**
 * Creates and returns a new [CoroutineSharedTreeMap] starting
 * from this map.
 * The result is a copy of this list that can be safely shared between coroutines
 * @param scope the scope of the coroutine server under this object; by default it is
 * used the [SharedScope]
 * @return the [CoroutineSharedTreeMap] instance that is a copy of this list
 */
suspend fun <K : Any, V : Any> Map<K, V>.newSharedTreeMapCopy(scope: CoroutineScope = SharedScope) : CoroutineSharedTreeMap<K, V> {
    val res = CoroutineSharedTreeMap<K, V>(scope)
    res.putAll(this)
    return res
}