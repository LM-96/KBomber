package kbomberx.io.j2k

import kbomberx.io.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream

/* READERS TO CHANNEL ***************************************************************************** */

/**
 * Wraps this [InputStream] to a *Kotlin* [Channel] using a [BufferedReaderChannelWrapper]
 * instance.
 * The data received from this stream are read as **strings**.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun InputStream.stringReceiveChannel(
    capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope) : ReceiveChannel<String>
= BufferedReaderChannelWrapper(this, capacity, scope).receiveChannel

/**
 * Wraps this [BufferedReader] to a *Kotlin* [Channel] using a [BufferedReaderChannelWrapper]
 * instance.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun BufferedReader.stringReceiveChannel(
    capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope) : ReceiveChannel<String>
        = BufferedReaderChannelWrapper(this, capacity, scope).receiveChannel

/* READERS TO CHANNEL WITH MAP ******************************************************************** */

/**
 * Wraps this [InputStream] to a *Kotlin* [Channel] using a
 * [MappedBufferedReaderChannelWrapper] instance.
 * The data received from this stream are read as **strings** and then mapped using the
 * transformation function passed as param.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @param mapper the transformation function
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun <O> InputStream.mappedReceiveChannel(
    capacity: Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope,
    mapper : (String) -> O
) : ReceiveChannel<O>
        = MappedBufferedReaderChannelWrapper(this, capacity, scope, mapper).receiveChannel

/**
 * Wraps this [BufferedReader] to a *Kotlin* [Channel] using a [MappedBufferedReaderChannelWrapper]
 * instance. The strings received from this reader are mapped using the
 * transformation function passed as param.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @param mapper the transformation function
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun <O> BufferedReader.mappedReceiveChannel(
    capacity: Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope,
    mapper : (String) -> O
) : ReceiveChannel<O>
        = MappedBufferedReaderChannelWrapper(this, capacity, scope, mapper).receiveChannel

/* READERS TO FLOW ******************************************************************************** */

/**
 * Wraps this [BufferedReader] to a *Kotlin* [SharedFlow] using a [BufferedReaderChannelWrapper]
 * instance.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param reply the number of values replayed to new subscribers (cannot be negative,
 * defaults to zero)
 * @param extraBufferCapacity the number of values buffered in addition to replay.
 * [MutableSharedFlow.emit] does not suspend while there is a buffer space remaining (optional,
 * cannot be negative, defaults to zero)
 * @param onBufferOverflow configures an [MutableSharedFlow.emit] action on buffer overflow.
 * Optional, defaults to suspending attempts to emit a value.
 * Values other than [BufferOverflow.SUSPEND] are supported only when replay > 0 or extraBufferCapacity > 0.
 * **Buffer overflow can happen only when there is at least one subscriber that is not ready to accept
 * the new value**. In the absence of subscribers only the most recent [reply] values are stored and
 * the buffer overflow behavior is never triggered and has no effect.
 * @return the [SharedFlow] that can be used instead of this reader
 */
fun BufferedReader.stringSharedFlow(reply : Int = 0,
                                    extraBufferCapacity : Int = 0,
                                    onBufferOverflow: BufferOverflow,
                                    scope : CoroutineScope = GlobalScope) : SharedFlow<String> {
    return BufferedReaderFlowWrapper(this, reply, extraBufferCapacity, onBufferOverflow, scope).sharedFlow
}

/**
 * Wraps this [InputStream] to a *Kotlin* [SharedFlow] using a [BufferedReaderChannelWrapper]
 * instance. The data received from this stream are read as **strings**.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param reply the number of values replayed to new subscribers (cannot be negative,
 * defaults to zero)
 * @param extraBufferCapacity the number of values buffered in addition to replay.
 * [MutableSharedFlow.emit] does not suspend while there is a buffer space remaining (optional,
 * cannot be negative, defaults to zero)
 * @param onBufferOverflow configures an [MutableSharedFlow.emit] action on buffer overflow.
 * Optional, defaults to suspending attempts to emit a value.
 * Values other than [BufferOverflow.SUSPEND] are supported only when replay > 0 or extraBufferCapacity > 0.
 * **Buffer overflow can happen only when there is at least one subscriber that is not ready to accept
 * the new value**. In the absence of subscribers only the most recent [reply] values are stored and
 * the buffer overflow behavior is never triggered and has no effect.
 * @return the [SharedFlow] that can be used instead of this reader
 */
fun InputStream.stringSharedFlow(reply : Int = 0,
                                    extraBufferCapacity : Int = 0,
                                    onBufferOverflow: BufferOverflow,
                                    scope : CoroutineScope = GlobalScope) : SharedFlow<String> {
    return BufferedReaderFlowWrapper(this, reply, extraBufferCapacity, onBufferOverflow, scope).sharedFlow
}

/* READERS TO FLOW WITH MAP *********************************************************************** */

/**
 * Wraps this [BufferedReader] to a *Kotlin* [SharedFlow] using a [BufferedReaderChannelWrapper]
 * instance. The strings received from this reader are mapped using the
 * transformation function passed as param and then emitted to result flow.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param reply the number of values replayed to new subscribers (cannot be negative,
 * defaults to zero)
 * @param extraBufferCapacity the number of values buffered in addition to replay.
 * [MutableSharedFlow.emit] does not suspend while there is a buffer space remaining (optional,
 * cannot be negative, defaults to zero)
 * @param onBufferOverflow configures an [MutableSharedFlow.emit] action on buffer overflow.
 * Optional, defaults to suspending attempts to emit a value.
 * Values other than [BufferOverflow.SUSPEND] are supported only when replay > 0 or extraBufferCapacity > 0.
 * **Buffer overflow can happen only when there is at least one subscriber that is not ready to accept
 * the new value**. In the absence of subscribers only the most recent [reply] values are stored and
 * the buffer overflow behavior is never triggered and has no effect
 * @param mapper the transformation function
 * @return the [SharedFlow] that can be used instead of this reader
 */
fun <O> BufferedReader.mappedSharedFlow(reply : Int = 0,
                                        extraBufferCapacity : Int = 0,
                                        onBufferOverflow: BufferOverflow,
                                        scope : CoroutineScope = IoScope,
                                        mapper: (String) -> O) : SharedFlow<O> {
    return MappedBufferedReaderFlowWrapper(this,
        reply, extraBufferCapacity, onBufferOverflow, scope, mapper).sharedFlow
}

/**
 * Wraps this [InputStream] to a *Kotlin* [SharedFlow] using a [BufferedReaderChannelWrapper]
 * instance. The data received from this stream are read as **strings** and then mapped using the
 * transformation function passed as param.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param reply the number of values replayed to new subscribers (cannot be negative,
 * defaults to zero)
 * @param extraBufferCapacity the number of values buffered in addition to replay.
 * [MutableSharedFlow.emit] does not suspend while there is a buffer space remaining (optional,
 * cannot be negative, defaults to zero)
 * @param onBufferOverflow configures an [MutableSharedFlow.emit] action on buffer overflow.
 * Optional, defaults to suspending attempts to emit a value.
 * Values other than [BufferOverflow.SUSPEND] are supported only when replay > 0 or extraBufferCapacity > 0.
 * **Buffer overflow can happen only when there is at least one subscriber that is not ready to accept
 * the new value**. In the absence of subscribers only the most recent [reply] values are stored and
 * the buffer overflow behavior is never triggered and has no effect
 * @param mapper the transformation function
 * @return the [SharedFlow] that can be used instead of this reader
 */
fun <O> InputStream.mappedSharedFlow(reply : Int = 0,
                                     extraBufferCapacity : Int = 0,
                                     onBufferOverflow: BufferOverflow,
                                     scope : CoroutineScope = IoScope,
                                     mapper: (String) -> O
) : SharedFlow<O> {
    return MappedBufferedReaderFlowWrapper(this,
        reply, extraBufferCapacity, onBufferOverflow, scope, mapper).sharedFlow
}

/* WRITERS TO CHANNEL ***************************************************************************** */
/**
 * Wraps this [OutputStream] to a *Kotlin* [Channel] using a [BufferedWriterChannelWrapper]
 * instance.
 * **Notice that after this call the original stream MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun OutputStream.stringSendChannel(
    capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope) : SendChannel<String>
        = BufferedWriterChannelWrapper(this, capacity, scope).sendChannel

/**
 * Wraps this [BufferedWriter] to a *Kotlin* [Channel] using a [BufferedWriterChannelWrapper]
 * instance.
 * **Notice that after this call the original writer MUST NO longer be used**
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @return the [ReceiveChannel] that can be used instead of this stream
 */
fun BufferedWriter.stringSendChannel(
    capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope) : SendChannel<String>
        = BufferedWriterChannelWrapper(this, capacity, scope).sendChannel