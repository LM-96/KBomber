package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancel
import java.io.Closeable

/**
 * A class that creates a [StateFlow] starting from a channel.
 * The values that are sent to the channel will be received from an internal job
 * that emits them to a *Kotlin* [StateFlow]
 * **Notice that after the creation of an instance of this class, the channel passed
 * to the constructor MUST NO longer be used** in order to avoid conflicts in receive
 * In addition to this, the values received from the channel are transformed thanks to
 * the transformation function passed as parameter to the constructor
 */
class ChannelToStateFlowWrapper<I, O>(
    private val channel : ReceiveChannel<I>,
    initialValue : O,
    scope: CoroutineScope = IoScope,
    private val mapper : (I) -> O
) : Closeable, AutoCloseable {

    private val internalStateFlow = MutableStateFlow(initialValue)
    val stateFlow = internalStateFlow.asStateFlow()

    private val job = scope.launch {
        var received : I
        var terminated = false
        while(isActive && !terminated) {
            try {
                received = channel.receive()
                internalStateFlow.emit(mapper(received))
            } catch (_ : ClosedReceiveChannelException) {
                terminated = true
            } catch (_ : CancellationException) {
                terminated = true
            } catch (e : Exception) {
                e.printStackTrace()
                //Cathces the exception of the mapper
            }
        }
    }

    /**
     * Closes the internal channel and cancel the internal job,
     * waiting for its termination
     */
    override fun close() {
        runBlocking {
            channel.cancel()
            job.cancelAndJoin()
        }
    }

}