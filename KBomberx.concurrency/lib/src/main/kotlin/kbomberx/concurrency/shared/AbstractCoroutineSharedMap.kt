package kbomberx.concurrency.shared

import kbomberx.concurrency.coroutineserver.*
import kotlinx.coroutines.CoroutineScope
import java.util.*
import kotlin.NoSuchElementException

/**
 * A map that can be safely shared between coroutine.
 * This type of list offered two type of operation:
 * - **synchronous**: functions that *require* an operation and waits for completion;
 * - **asynchronous**: functions that send the command to do an operation but without
 * waiting for its completion (so, they are *non-blocking*).
 * All of this type of function have a name that start with `async`.
 *
 * The main implementations of these classes are [CoroutineSharedHashMap] and [CoroutineSharedTreeMap]
 * that are different because of the internal map they use
 */
abstract class AbstractCoroutineSharedMap<K : Any, V : Any>(
    private val internalMap: MutableMap<K, V>,
    private val scope : CoroutineScope = SharedScope
) : CoroutineServer(scope) {

    companion object {
        private const val PUT_CODE = 1
        private const val ASYNC_PUT_CODE = 2
        private const val GET_CODE = 3
        private const val CONTAINS_KEY_CODE = 4
        private const val KEY_SET_CODE = 5
        private const val VALUES_CODE = 6
        private const val SNAPSHOT_CODE = 7
        private const val CLEAR_CODE = 8
        private const val ASYNC_CLEAR_CODE = 9
        private const val ENTRIES_CODE = 10
        private const val SIZE_CODE = 11
        private const val PUT_ALL_CODE  = 12
        private const val ASYNC_PUT_ALL_CODE = 13
    }

    override suspend fun handleRequest(request: CmdServerRequest) {
        when(request.requestCode) {

            PUT_CODE -> {
                val res = Optional.ofNullable(
                    internalMap.put(request.requestParams[0] as K, request.requestParams[1] as V))
                replyWithOk(request, res)
            }

            ASYNC_PUT_CODE -> {
                internalMap[request.requestParams[0] as K] = request.requestParams[1] as V
            }

            GET_CODE -> {
                val res = Optional.ofNullable(
                    internalMap[request.requestParams[0] as K]
                )
                replyWithOk(request, res)
            }

            CONTAINS_KEY_CODE -> {
                val res = internalMap.containsKey(request.requestParams[0] as K)
                replyWithOk(request, res)
            }

            KEY_SET_CODE -> {
                val res = internalMap.keys.toSet()
                replyWithOk(request, res)
            }

            VALUES_CODE -> {
                val res = internalMap.values.toList()
                replyWithOk(request, res)
            }

            SNAPSHOT_CODE -> {
                val res = internalMap.toMap()
                replyWithOk(request, res)
            }

            CLEAR_CODE -> {
                internalMap.clear()
                replyWithOk(request)
            }

            ASYNC_CLEAR_CODE -> {
                internalMap.clear()
            }

            ENTRIES_CODE -> {
                val res = internalMap.entries.toList()
                replyWithOk(request, res)
            }

            SIZE_CODE -> {
                val res = internalMap.size
                replyWithOk(request, res)
            }

            PUT_ALL_CODE -> {
                internalMap.putAll(request.requestParams[0] as Map<K, V>)
                replyWithOk(request)
            }

            ASYNC_PUT_ALL_CODE -> {
                internalMap.putAll(request.requestParams[0] as Map<K, V>)
            }

        }
    }

    /**
     * Associates the specified [value] with the specified [key] in the map.
     * @param key the key
     * @param value the value
     * @return an [Optional] that contains the previous value associated with the key,
     * or that is empty if the key was not present in the map
     */
    suspend fun put(key : K, value : V) : Optional<V> {
        val req = requestWithParameters(PUT_CODE, key, value)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Optional<V>
    }

    /**
     * Returns an [Optional] that contains the value corresponding to the given key,
     * or [Optional.empty] if such a key is not present in the map.
     * @param key the key of the element
     * @return an [Optional] that contains the value corresponding to the given key or
     * that is empty if no value is associated with the given [key]
     */
    suspend fun get(key : K) : Optional<V> {
        val req = requestWithParameters(GET_CODE, key)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Optional<V>
    }

    /**
     * Safely gets the element associated with the given [key]
     * then invoke the given [block] with its value
     * @throws NoSuchElementException if no element associated with [key] is present
     * @param key the key associated to the element
     * @param block the function that will be executed with the element
     * @return the element at the specified index in the list that has been used
     * with [block]
     */
    suspend fun getAndInvoke(key : K, block : (Pair<K, V>) -> Unit) : V {
        val item = get(key)
        if(item.isEmpty)
            throw NoSuchElementException("no element associated with the key \'$key\'")
        block(Pair(key, item.get()))
        return item.get()
    }

    /**
     * Safely gets and maps the element associated with the given [key]
     * @throws NoSuchElementException if no element associated with [key] is present
     * @param key the key associated to the element
     * @param mapper the function to apply for the transformation
     * @return the transformed value
     */
    suspend fun <R> getAndMap(key : K, mapper : (V) -> R) : R {
        val item = get(key)
        if(item.isEmpty)
            throw NoSuchElementException("no element associated with the key \'$key\'")
        return mapper(item.get())
    }

    /**
     * Returns the value corresponding to the given key,
     * or throws an exception if such a key is not present in the map.
     * @throws NoSuchElementException if the key is not present in the map
     * @param key the key of the element
     * @return the element associated with the key
     */
    suspend fun forcedGet(key : K) : V {
        val res = get(key)
        if(res.isEmpty)
            throw NoSuchElementException("no element with key \'$key\'")

        return res.get()
    }

    /**
     * Returns `true` if the map contains the specified key
     * @param key the key that can be contained into the map
     * @return `true` if the map contains the specified key, `false` otherwise
     */
    suspend fun containsKey(key : K) : Boolean {
        val req = requestWithParameters(CONTAINS_KEY_CODE, key)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameterAs(Boolean::class.java)
    }

    /**
     * Returns a read-only [Set] of all keys in this map
     * @return a read-only Set of all keys in this map
     */
    suspend fun keySet() : Set<K> {
        val req = basicRequest(KEY_SET_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Set<K>
    }

    /**
     * Returns a read-only [List] of all values in this map.
     * Note that this collection may contain duplicate values
     * @return a read-only [List] of all values in this map
     */
    suspend fun values() : List<V> {
        val req = basicRequest(VALUES_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as List<V>
    }

    /**
     * Returns a *read-only* copy of this [Map]
     * @return a *read-only* copy of this [Map]
     */
    suspend fun snapshot() : Map<K, V> {
        val req = basicRequest(SNAPSHOT_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Map<K, V>
    }

    /**
     * Removes all elements from this map
     */
    suspend fun clear() {
        val req = basicRequest(CLEAR_CODE)
        mainChannel.send(req)
        req.responseChannel.receive()
    }

    /**
     * Returns a read-only [List] of all key/value pairs in this map
     * @return a read-only [List] of all key/value pairs in this map
     */
    suspend fun entries() : List<MutableMap.MutableEntry<K, V>> {
        val req = basicRequest(ENTRIES_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive()
            .throwErrorOrGetFirstParameter() as List<MutableMap.MutableEntry<K, V>>
    }

    /**
     * Returns the number of key/value pairs in the map
     * @return the number of key/value pairs in the map
     */
    suspend fun size() : Int {
        val req = basicRequest(SIZE_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameterAs(Int::class.java)
    }

    /**
     * Updates this map with key/value pairs from the specified map [from]
     * @param from the map that contains the element to put
     */
    suspend fun putAll(from : Map<K, V>) {
        val req = requestWithParameter(PUT_ALL_CODE, from)
        mainChannel.send(req)
        req.responseChannel.receive()
    }

    /**
     * **Async** function for [put]
     * @param key the key
     * @param value the value
     */
    suspend fun asyncPut(key : K, value : V) {
        mainChannel.send(asyncRequestWithParameters(ASYNC_PUT_CODE, key, value))
    }

    /**
     * **Async** function for [putAll]
     * @param from the map that contains the element to put
     */
    suspend fun asyncPutAll(from : Map<K, V>) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_PUT_ALL_CODE, from))
    }

    /**
     * **Async** function for [clear]
     */
    suspend fun asyncClear() {
        mainChannel.send(asyncBasicRequest(ASYNC_CLEAR_CODE))
    }


}