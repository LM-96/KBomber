package kbomberx.concurrency.shared

import kbomberx.concurrency.coroutineserver.*
import kotlinx.coroutines.CoroutineScope

/**
 * A list that can be safely shared between coroutine.
 * This type of list offered two type of operation:
 * - **synchronous**: functions that *require* an operation and waits for completion;
 * - **asynchronous**: functions that send the command to do an operation but without
 * waiting for its completion (so, they are *non-blocking*).
 * All of this type of function have a name that start with `async`.
 *
 * The main implementations of these classes are [CoroutineSharedArrayList] and [CoroutineSharedLinkedList]
 * that are different because of the internal list they use
 */
abstract class AbstractCoroutineSharedList<T : Any>(
    private val internalList : MutableList<T>,
    private val scope: CoroutineScope = SharedScope
) : CoroutineServer(scope) {

    companion object {
        private const val ADD_CODE = 1
        private const val ASYNC_ADD_CODE = 2
        private const val ADD_WITH_INDEX_CODE = 3
        private const val ASYNC_ADD_WITH_INDEX_CODE = 4
        private const val REMOVE_BY_INDEX_CODE = 5
        private const val ASYNC_REMOVE_BY_INDEX_CODE = 6
        private const val REMOVE_BY_OBJ_CODE = 7
        private const val ASYNC_REMOVE_BY_OBJ_CODE = 8
        private const val GET_CODE = 9
        private const val CLEAR_CODE = 10
        private const val ASYNC_CLEAR_CODE = 11
        private const val CONTAINS_CODE = 12
        private const val FILTER_CODE = 13
        private const val COPY_CODE = 14
        private const val INDEX_OF_CODE = 15
        private const val SIZE_CODE = 16
        private const val ADD_ALL_CODE = 17
        private const val ASYNC_ADD_ALL_CODE = 18
    }

    override suspend fun handleRequest(request: CmdServerRequest) {
        when(request.requestCode) {

            /* SYNCHRONOUS FUNCTIONALITY ************************************************ */
            ADD_CODE -> {
                val result = internalList.add(request.requestParams[0] as T)
                replyWithOk(request, result)
            }

            ADD_WITH_INDEX_CODE -> {
                internalList.add(request.requestParams[0] as Int, request.requestParams[1] as T)
                replyWithOk(request)
            }

            REMOVE_BY_INDEX_CODE -> {
                val removed = internalList.removeAt(request.requestParams[0] as Int)
                replyWithOk(request, removed)
            }

            REMOVE_BY_OBJ_CODE -> {
                val removed = internalList.remove(request.requestParams[0] as T)
                replyWithOk(request, removed)
            }

            GET_CODE -> {
                val value = internalList[request.requestParams[0] as Int]
                replyWithOk(request, value)
            }

            CLEAR_CODE -> {
                internalList.clear()
                replyWithOk(request)
            }

            CONTAINS_CODE -> {
                val result = internalList.contains(request.requestParams[0] as T)
                replyWithOk(request, result)
            }

            FILTER_CODE -> {
                val result = internalList.filter(request.requestParams[0] as (T) -> Boolean)
                replyWithOk(request, result)
            }

            COPY_CODE -> {
                val result = arrayListOf<T>().addAll(internalList)
                replyWithOk(request, result)
            }

            INDEX_OF_CODE -> {
                val index = internalList.indexOf(request.requestParams[0] as T)
                replyWithOk(request, index)
            }

            SIZE_CODE -> {
                val res = internalList.size
                replyWithOk(request, res)
            }

            ADD_ALL_CODE -> {
                val res = internalList.addAll(request.requestParams[0] as Collection<T>)
                replyWithOk(request, res)
            }

            /* ASYNCHRONOUS FUNCTIONALITY *********************************************** */
            ASYNC_ADD_CODE -> {
                try {
                    internalList.add(request.requestParams[0] as T)
                } catch (_ : Exception) {}
            }

            ASYNC_ADD_WITH_INDEX_CODE -> {
                try {
                    internalList.add(request.requestParams[0] as Int, request.requestParams[1] as T)
                } catch (_ : Exception) {}
            }

            ASYNC_REMOVE_BY_OBJ_CODE -> {
                try {
                    internalList.remove(request.requestParams[0] as T)
                } catch (_ : Exception) {}
            }

            ASYNC_REMOVE_BY_INDEX_CODE -> {
                try {
                    internalList.removeAt(request.requestParams[0] as Int)
                } catch (_ : Exception) {}
            }

            ASYNC_CLEAR_CODE -> {
                try {
                    internalList.clear()
                } catch (_ : Exception) {}
            }

        }
    }

    /**
     * Inserts an element into the list at the specified [index]
     * @param index the index
     * @param item the element to be added
     */
    suspend fun add(index : Int, item : T) {
        val request = requestWithParameters(ADD_WITH_INDEX_CODE, index, item)
        mainChannel.send(request)
        request.responseChannel.receive().throwErrorOrGetParameters()
    }

    /**
     * Adds the specified element to the end of this list
     * @param item the item to be added
     * @return `true` because the list is always modified as the result of this operation
     */
    suspend fun add(item : T) : Boolean {
        val request = requestWithParameter(ADD_CODE, item)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Boolean::class.java)
    }


    /**
     * Removes an element at the specified [index] from the list
     * @param index the index of the element to remove
     * @return the removed element
     */
    suspend fun removeAt(index : Int) : T {
        val request = requestWithParameter(REMOVE_BY_INDEX_CODE, index)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameter() as T
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present
     * @param item the item to be removed
     * @return `true` if the element has been successfully removed;
     * `false` if it was not present in the collection
     */
    suspend fun remove(item : T) : Boolean {
        val request = requestWithParameter(REMOVE_BY_OBJ_CODE, item)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Boolean::class.java)
    }

    /**
     * Returns the element at the specified index in the list
     * @throws IndexOutOfBoundsException if the index is not valid
     * @param index the index of the element
     * @return the element at the specified index in the list
     */
    suspend fun get(index: Int) : T {
        val request = requestWithParameter(GET_CODE, index)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameter() as T
    }

    /**
     * Safely gets the element at the specified index in the list
     * then invoke the given [block] with its value
     * @throws IndexOutOfBoundsException if the index is not valid
     * @param index the index of the element
     * @param block the function that will be executed with the element
     * @return the element at the specified index in the list that has been used
     * with [block]
     */
    suspend fun getAndInvoke(index: Int, block : (T) -> Unit) : T {
        val item = get(index)
        block(item)
        return item
    }

    /**
     * Safely gets and maps the element at the specified index in the list
     * @throws IndexOutOfBoundsException if the index is not valid
     * @param index the index of the element
     * @param mapper the function to apply for the transformation
     * @return the transformed value
     */
    suspend fun <R> getAndMap(index : Int, mapper : (T) -> R) : R {
        return mapper(get(index))
    }

    /**
     * Removes all elements from this collection
     */
    suspend fun clear() {
        val request = basicRequest(CLEAR_CODE)
        mainChannel.send(request)
        request.responseChannel.receive()
    }

    /**
     * Checks if the specified element is contained in this collection
     * @param item the element
     * @return `true` if the specified element is contained in this collection,
     * `false` otherwise
     */
    suspend fun contains(item : T) : Boolean {
        val request = requestWithParameter(CONTAINS_CODE, item)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Boolean::class.java)
    }

    /**
     * Returns a list containing only elements matching the given [predicate]
     * @param predicate the *predicate* to apply for filtering
     */
    suspend fun filter(predicate : (T) -> Boolean) : List<T> {
        val request = requestWithParameter(FILTER_CODE, predicate)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameter() as List<T>
    }

    /**
     * Returns a copy that contains all the elements of this list
     * @return a [List] that is a copy that contains all the elements of this list
     */
    suspend fun copy() : List<T> {
        val request = basicRequest(COPY_CODE)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameter() as List<T>
    }

    /**
     * Returns a list containing the results of applying the given [transform] function
     * to each element in the original array. This operation is done on a *copy* of the
     * current list
     * @param transform the function used for transformation
     * @return a list that contains the results of the transformation
     */
    suspend fun <R> map(transform : (T) -> R) : List<R> {
        return copy().map(transform)
    }

    /**
     * Returns the index of the first occurrence of the specified element in the list,
     * or -1 if the specified element is not contained in the list
     * @param item the element
     * @return the index of the first occurrence of the specified element in the list,
     * or -1 if the specified element is not contained in the list
     */
    suspend fun indexOf(item : T) : Int {
        val request = requestWithParameter(INDEX_OF_CODE, item)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Int::class.java)
    }

    /**
     * Returns the size of the collection
     * @return the size of the collection
     */
    suspend fun size() : Int {
        val request = basicRequest(SIZE_CODE)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Int::class.java)
    }

    /**
     * Adds all of the elements of the specified collection to the end of this list.
     * The elements are appended in the order they appear in the [items] collection.
     * @param items the [Collection] with the elements to be appended
     * @return `true` if the list was changed as the result of the operation
     */
    suspend fun addAll(items : Collection<T>) : Boolean {
        val request = requestWithParameter(ADD_ALL_CODE, items)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameterAs(Boolean::class.java)
    }

    /**
     * **Async** function for [add]
     * @param index the index
     * @param item the element to be added
     */
    suspend fun asyncAdd(index: Int, item: T) {
        mainChannel.send(requestWithParameters(ASYNC_ADD_WITH_INDEX_CODE, index, item))
    }

    /**
     * Async function for [add]
     * @param item the item to be added
     */
    suspend fun asyncAdd(item: T) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_ADD_CODE, item))
    }

    /**
     * **Async** function for [removeAt]
     * @param index the index of the element to remove
     */
    suspend fun asyncRemoveAt(index: Int) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_REMOVE_BY_INDEX_CODE, index))
    }

    /**
     * **Async** function for [remove]
     * @param item the item to be removed
     */
    suspend fun asyncRemove(item : T) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_REMOVE_BY_OBJ_CODE, item))
    }

    /**
     * **Async** function for [clear]
     */
    suspend fun asyncClear() {
        mainChannel.send(asyncBasicRequest(ASYNC_CLEAR_CODE))
    }

    /**
     * **Async** function for [addAll]
     * @param items the [Collection] with the elements to be appended
     */
    suspend fun asyncAddAll(items : Collection<T>) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_ADD_ALL_CODE, items))
    }



}