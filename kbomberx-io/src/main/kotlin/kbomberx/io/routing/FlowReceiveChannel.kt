package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import java.io.Closeable

/**
 * A worker that collects the internal flow and propagates its values to
 * a channel
 */
class FlowReceiveChannel<T>(
    flow : Flow<T>,
    private val capacity : Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope
) : Closeable, AutoCloseable {

    private val channel = Channel<T>(capacity)

    /**
     * The channel that receives the result of the collect of the internal
     * flow. Every update is sent to this channel
     */
    val receiveChannel : ReceiveChannel<T> = channel

    private val job = scope.launch {
        try {
            flow.collect {
                channel.send(it)
            }
        }
        catch (_ : ClosedSendChannelException) {}
        catch (_ : CancellationException){}
    }

    /**
     * Request to close the internal channel and force the internal
     * job to be closed, waiting for it
     */
    override fun close() {
        try {
            channel.close()
            runBlocking {
                job.cancelAndJoin()
            }
        } catch (_ : CancellationException) {}
    }

}