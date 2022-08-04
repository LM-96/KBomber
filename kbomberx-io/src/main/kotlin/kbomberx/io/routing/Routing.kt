package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/* FLOW - CHANNELS ******************************************************************************** */
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

/* FAN-IN ROUTERS ********************************************************************************* */
/**
 * Creates and returns a new *FAN-IN* [ChannelRouter] that has this channel as the unique
 * output line. The resulting router is not started.
 * **After this call this channel MUST NO longer be used for send operation** in order to avoid
 * conflicts
 * @param routerName the name of the router
 * @param scope the [CoroutineScope] of the routing job (default [IoScope])
 * @return the new router
 */
fun <T> Channel<T>.createsFanInRouter(routerName : String, scope : CoroutineScope = IoScope) : ChannelRouter<T> {
    return FanInChannelRouter(routerName, this, scope)
}

/**
 * Creates and returns a new *mapped FAN-IN* [ChannelRouter] that has this channel as the unique
 * output line and the [mapper] function to transform the values that exit from the input lines.
 * The resulting router is not started.
 * **After this call this channel MUST NO longer be used for send operation** in order to avoid
 * conflicts
 * @param routerName the name of the router
 * @param scope the [CoroutineScope] of the routing job (default [IoScope])
 * @param mapper the function that transforms the values that outcome from the input lines
 * to that accepted by the single output line. If this function returns an [Optional.empty],
 * then no value is sent to the output channel (means that the passage is denied by the same mapper)
 * @return the new router
 */
fun <L, T> Channel<T>.createsFanInMappedRouter(routerName : String,
                                               scope : CoroutineScope = IoScope,
                                               mapper : (L) -> Optional<T>
) : ChannelRouter<L> {
    return MappedFanInChannelRouter(routerName, this, scope, mapper)
}

/* FAN-OUT ROUTERS ******************************************************************************** */
/**
 * Creates and returns a new *FAN-OUT* [ChannelRouter] that has this channel as the unique
 * input line. The resulting router is not started.
 * **After this call this channel MUST NO longer be used for receive operation** in order to avoid
 * conflicts
 * @param routerName the name of the router
 * @param scope the [CoroutineScope] of the routing job (default [IoScope])
 * @return the new router
 */
fun <T> Channel<T>.createsFanOutRouter(routerName : String, scope : CoroutineScope = IoScope) : ChannelRouter<T> {
    return FanOutChannelRouter(routerName, this, scope)
}

/**
 * Creates and returns a new *mapped FAN-OUT* [ChannelRouter] that has this channel as the unique
 * input line and the [mapper] function to transform the values that exit from this channel.
 * The resulting router is not started.
 * **After this call this channel MUST NO longer be used for receive operation** in order to avoid
 * conflicts
 * @param routerName the name of the router
 * @param scope the [CoroutineScope] of the routing job (default [IoScope])
 * @param mapper the function that transforms the values that outcome from the input line
 * to that accepted by all the output lines. If this function returns an [Optional.empty],
 * then no value is sent to the output lines (means that the passage is denied by the same mapper)
 * @return the new router
 */
fun <L, T> Channel<T>.createsFanOutMappedRouter(routerName : String,
                                               scope : CoroutineScope = IoScope,
                                               mapper : (T) -> Optional<L>
) : ChannelRouter<L> {
    return MappedFanOutChannelRouter<T, L>(routerName, this, scope, mapper)
}

