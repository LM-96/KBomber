package kbomberx.concurrency.shared

import kbomberx.concurrency.coroutineserver.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable

/**
 * A value that can be safely shared between coroutines.
 * Notice that [get] and [set] operation are synchronous and block the calling
 * coroutine until the operation is completed, instead [asyncSet] is **asynchronous**
 * and asks to set the value of this object without waiting for completion
 */
class CoroutineSharedValue<T : Any>(initialValue : T,
                                    private val scope : CoroutineScope = SharedScope) :
    CoroutineServer(scope) {

    companion object {
        private const val SET_CODE = 1
        private const val GET_CODE = 2
        private const val ASYNC_SET_CODE = 3
    }

    private var value = initialValue

    override suspend fun handleRequest(request: CmdServerRequest) {
        when(request.requestCode) {

            GET_CODE -> {
                replyWithOk(request, value)
            }

            SET_CODE -> {
                value = request.requestParams[0] as T
                replyWithOk(request)
            }

            ASYNC_SET_CODE -> {
                try {
                    value = request.requestParams[0] as T
                } catch (_ : ClassCastException) {
                    //No response sent (method is async)
                }
            }
        }
    }


    /**
     * Safely sets the value of this object.
     * This method is synchronous, so at the end of its execution
     * the value is correctly been set
     * @param newValue the value to be set
     */
    suspend fun set(newValue : T) {
        val request = requestWithParameter(SET_CODE, newValue)
        mainChannel.send(request)
        request.responseChannel.receive()
    }

    /**
     * Asynchronous sets the value of this object without making
     * sure that it has been set
     * @param newValue the value to be set
     */
    suspend fun asyncSet(newValue: T) {
        mainChannel.send(asyncRequestWithParameter(ASYNC_SET_CODE, newValue))
    }

    /**
     * Safely gets the value of this object
     */
    suspend fun get() : T {
        val request = basicRequest(GET_CODE)
        mainChannel.send(request)
        return request.responseChannel.receive().throwErrorOrGetFirstParameter() as T
    }

    /**
     * Safely gets the value of this object and then executes
     * the given [block] passing it
     * @param block the function that will be executed with this value
     * @return the get value that has been used with [block]
     */
    suspend fun getAndInvoke(block : (T) -> Unit) : T {
        val item = get()
        block(item)
        return item
    }

    /**
     * Safely gets and maps the value of this object
     * @param mapper the function to apply for the transformation
     * @return the transformed value
     */
    suspend fun <R> getAndMap(mapper : (T) -> R) : R {
        return mapper(get())
    }

}