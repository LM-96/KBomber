package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Creates and returns a new channel in which the value of this flow are collected and
 * sent in. An internal coroutine is dedicated to *collect* the value emitted
 * with this flow and writes them to the result channel
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the [CoroutineScope] used by the internal job (default [IoScope])
 * @return a new [ReceiveChannel]
 */
fun <T> Flow<T>.newReceiveChannel(capacity : Int = Channel.UNLIMITED,
                                  scope : CoroutineScope = IoScope) : ReceiveChannel<T> {
    return FlowReceiveChannel(this, capacity).receiveChannel
}

/**
 * Creates and returns a new [StateFlow] that emits all the values written to
 * this channel. **After this call, this channel MUST BE USED ONLY FOR SEND operation**
 * in order to avoid collision in receiving
 * @param initialValue the initial value for the [StateFlow]
 * @param scope the [CoroutineScope] used by the internal job (default [IoScope])
 * @param mapper the transformation function that maps the values from the channel
 * to those emitted by the flow
 * @return the new [StateFlow] that emits the mapped value once received from the channel
 */
fun <T, O> ReceiveChannel<T>.asStateFlow(initialValue : O, scope : CoroutineScope = IoScope,
                                         mapper : (T) -> O) : StateFlow<O>
= ChannelToStateFlowWrapper(this, initialValue, scope, mapper).stateFlow