package kbomberx.concurrency.coroutineserver

import kbomberx.concurrency.coroutineserver.ServerReply.Companion.ERR_CODE
import kbomberx.concurrency.coroutineserver.ServerReply.Companion.OK_CODE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable

/**
 * An abstract coroutine server that hold a coroutine that listens on a channel
 * waiting for [CmdServerRequest]. When a request is received, the coroutine calls
 * [handleRequest] function to handle the request.
 * All subclasses of this class must implement [handleRequest] function in order to
 * define the behavior of the server.
 * The developer can define custom services offered by his server by defining new
 * [CmdServerRequest.requestCode], handling it into the [handleRequest] method.
 * The closure of the coroutine is automatically handled by this class so the developer
 * does not care about it
 */
abstract class CoroutineServer(private val scope : CoroutineScope) :
    Closeable, AutoCloseable {

    companion object {
        private const val CLOSE_CODE = -1
    }

    /**
     * The channel internally used by the server
     */
    private val serverChannel = Channel<CmdServerRequest>()

    /**
     * The *main* channel that can be used by all the subclasses in order to
     * send the request to the server coroutine
     */
    protected val mainChannel : SendChannel<CmdServerRequest> = serverChannel

    /**
     * This method is implemented in the subclass in order to define the
     * behavior of the server. The request received by the server coroutine
     * is passed as parameter to this function, so the subclass can handle it
     * Notice that **all exception thrown by this method are automatically
     * caught and sent as reply**. The developer can avoid this behavior by defining an
     * internal `try-catch` inside the [handleRequest]
     */
    protected abstract suspend fun handleRequest(request: CmdServerRequest)

    /**
     * The job that listens on the main channel and invokes [handleRequest].
     * Notice that **all exception thrown by the [handleRequest] are automatically
     * caught and sent as reply**. The developer can avoid this behavior by defining an
     * internal `try-catch` inside the [handleRequest]
     */
    private val job = scope.launch {
        var working = true
        var cmd : CmdServerRequest

        while(working) {
            try {
                cmd = serverChannel.receive()
                when(cmd.requestCode) {
                    CLOSE_CODE -> {
                        serverChannel.close()
                        working = false
                        cmd.responseChannel.send(ServerReply(OK_CODE))
                    }

                    else -> {
                        try {
                            handleRequest(cmd)
                        } catch (e : Exception) {
                            replyWithError(cmd, e)
                        }
                    }
                }
            } catch (_ : ClosedReceiveChannelException) {
                working = false
            }
        }

    }

    /**
     * Closes this object and waits until the job associated is terminated
     */
    override fun close() {
        runBlocking {
            val req = CmdServerRequest(CLOSE_CODE)
            mainChannel.send(req)
            req.responseChannel.receive()
        }
    }

    /**
     * Replies to the given request and closes the request channel
     * @param request the request to reply
     * @param replyCode the [ServerReply.replyCode] of the reply
     * @param params the parameters of the reply
     */
    protected suspend fun reply(request: CmdServerRequest, replyCode : Int,
                                  vararg params : Any) {
        request.responseChannel.send(ServerReply(replyCode, params))
        request.responseChannel.close()
    }

    /**
     * Replies to the given request with a reply that maintains an error
     * and closes the request channel
     * @param request the request to reply
     * @param exception the exception that represents the error
     */
    protected suspend fun replyWithError(request: CmdServerRequest, exception: Exception) {
        request.responseChannel.send(ServerReply(ERR_CODE, arrayOf(exception)))
        request.responseChannel.close()
    }

    /**
     * Replies to the given request with a simple ok and closes the request
     * channel
     * @param request the request to reply
     */
    protected suspend fun replyWithOk(request: CmdServerRequest) {
        request.responseChannel.send(ServerReply(OK_CODE))
        request.responseChannel.close()
    }

    /**
     * Replies to the given request with a simple ok and closes the request channel.
     * This method is different from the other [replyWithOk] because this allow
     * to pass parameters inside the reply (for example to pass a result)
     * @param request the request to reply
     */
    protected suspend fun replyWithOk(request: CmdServerRequest, vararg params: Any) {
        request.responseChannel.send(ServerReply(OK_CODE, params))
        request.responseChannel.close()
    }


}