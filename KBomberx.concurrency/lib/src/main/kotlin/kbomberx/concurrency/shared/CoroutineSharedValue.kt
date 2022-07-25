package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.Closeable

class CoroutineSharedValue<T>(initialValue : T,
                              val scope : CoroutineScope = SharedScope) : Closeable, AutoCloseable {

    private enum class CmdType {
        SET, GET, ASYNC_SET, CLOSE
    }

    private data class Cmd<T>(
        val cmdType : CmdType,
        val param : T? = null,
        val responseChan : Channel<Result<*>> = Channel()
    )

    private val mainChannel = Channel<Cmd<T>>()

    private val job = scope.launch {
        var value : T = initialValue
        var cmd : Cmd<T>
        var working = true
        while (working) {
            try {
                cmd = mainChannel.receive()
                when(cmd.cmdType) {

                    CmdType.GET -> {
                        cmd.responseChan.send(Result.success(value))
                    }

                    CmdType.SET -> {
                        if(cmd.param != null) {
                            value = cmd.param!!
                            cmd.responseChan.send(Result.success(true))
                        } else {
                            cmd.responseChan.send(Result
                                .failure<Unit>(NullPointerException("Unable to set a null value")))
                        }
                    }

                    CmdType.ASYNC_SET -> {
                        if(cmd.param != null) {
                            value = cmd.param!!
                        }
                    }

                    CmdType.CLOSE -> {
                        mainChannel.close()
                        working = false
                    }
                }
            } catch (_: ClosedReceiveChannelException) {
                working = false
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
        val cmd = Cmd(CmdType.SET, newValue)
        mainChannel.send(cmd)
        cmd.responseChan.receive()
    }

    /**
     * Asynchronous sets the value of this object without making
     * sure that it has been set
     * @param newValue the value to be set
     */
    suspend fun asyncSet(newValue: T) {
        mainChannel.send(Cmd(CmdType.ASYNC_SET, newValue))
    }

    /**
     * Safely gets the value of this object
     */
    suspend fun get() : T {
        val cmd = Cmd<T>(CmdType.GET)
        mainChannel.send(cmd)
        return (cmd.responseChan.receive() as Result<T>).getOrThrow()
    }

    /**
     * Closes this object and waits until the job associated is terminated
     */
    override fun close() {
        runBlocking {
            mainChannel.send(Cmd(CmdType.CLOSE))
            job.join()
        }
    }



}