package kbomberx.io.routing

import kotlinx.coroutines.channels.*
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

/**
 * A route with a [routeName], a [passage] that *indicates* if the route
 * is opened and a [channel] that *relizes* the route
 */
data class ChannelRoute<T>(
    /**
     * The name of this route
     */
    val name : String,

    /**
     * The channel that represents the route: if the passage is
     * opened, so it is possible to write to this channel
     */
    val channel : Channel<T>,

    /**
     * A function that can be used to evaluate if the route
     * is actually opened or if the passage of the data is denied.
     * This function also consider the data that has to be sent
     */
    val passage : (T) -> Boolean
) {

    /**
     * Verifies if the passage of this route is opened then send
     * [data] to the channel of this route returning `true`.
     * If the passage is denied then nothing is sent and this function
     * returns `false`
     * @throws CancellationException if thrown by [Channel.send]
     * @throws ClosedSendChannelException if thrown by [Channel.send]
     * @param data the data to be sent
     * @return `true` if the passage is open and [data] is sent,
     * `false` if the passage is denied and nothing is sent
     */
    suspend fun verifyAndSend(data : T) : Boolean {
        if(!passage(data)) {
            return false
        }
        channel.send(data)
        return true
    }

    /**
     * Verifies if the passage of this route is opened then try to send
     * [data] to the channel of this route returning the [ChannelResult].
     * If the passage is denied then nothing is sent and this function
     * returns `null`.
     * See [Channel.trySend] for additional details about the semantic for send
     * @throws CancellationException if thrown by [Channel.send]
     * @throws ClosedSendChannelException if thrown by [Channel.send]
     * @param data the data to be sent
     * @return `true` if the passage is open and [data] is sent,
     * `false` if the passage is denied and nothing is sent
     */
    suspend fun verifyAndTrySend(data : T) : ChannelResult<Unit>? {
        if(!passage(data)) {
            return null
        }
        return channel.trySend(data)
    }

    /**
     * Verifies if the passage of this route is opened then send
     * [data] to the [otherChannel] returning `true`.
     * If the passage is denied then nothing is sent and this function
     * returns `false`
     * @throws CancellationException if thrown by [Channel.send]
     * @throws ClosedSendChannelException if thrown by [Channel.send]
     * @param otherChannel the channel used to send [data]
     * @param data the data to be sent
     * @return `true` if the passage is open and [data] is sent,
     * `false` if the passage is denied and nothing is sent
     */
    suspend fun verifyAndSendTo(otherChannel : SendChannel<T>, data : T) : Boolean {
        if(!passage(data)) {
            return false
        }
        otherChannel.send(data)
        return true
    }

    /**
     * Verifies if the passage of this route is opened then try to send
     * [data] to the [otherChannel] returning the [ChannelResult].
     * If the passage is denied then nothing is sent and this function
     * returns `null`.
     * See [Channel.trySend] for additional details about the semantic for send
     * @throws CancellationException if thrown by [Channel.send]
     * @throws ClosedSendChannelException if thrown by [Channel.send]
     * @param otherChannel the channel used to send [data]
     * @param data the data to be sent
     * @return `true` if the passage is open and [data] is sent,
     * `false` if the passage is denied and nothing is sent
     */
    suspend fun verifyAndTrySendTo(otherChannel: SendChannel<T>, data : T) : ChannelResult<Unit>? {
        if(!passage(data)) {
            return null
        }
        return otherChannel.trySend(data)
    }

    /**
     * Sends [data] to the channel of this route throwing an exception
     * if the passage is denied
     * @throws IllegalStateException if the passage is denied
     * @throws CancellationException if thrown by [Channel.send]
     * @throws ClosedSendChannelException if thrown by [Channel.send]
     * @param data data to be sent
     */
    suspend fun send(data : T) {
        if(!passage(data)) {
            throw IllegalStateException("passage is denied")
        }
        channel.send(data)
    }

    /**
     * Executes the given [block] if the [passage] is allowed for [msg]
     * @param msg the message to use for checking passage
     * @param block the function to be executed if the passage is opened
     * @return `true` if the passage were opened and [block] has been executed,
     * `false` otherwise
     */
    suspend fun ifPassageAllowed(msg : T, block : (Channel<T>) -> Unit) : Boolean {
        if(passage(msg)) {
            block(channel)
            return true
        }

        return false
    }



}