package kbomberx.io.j2k

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.cancel
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * A wrapper class for [BufferedReader] that *wraps* the standard *Java* buffered reader
 * to a new *Kotlin* [SharedFlow].
 * The strings received from the reader are *mapped* using
 * the transformation function passed to the constructor of this class.
 * After the creation of the object, the old reader **must no longer be used** and
 * will be automatically closed with the new channel.
 * The default capacity of the new channel is [Channel.UNLIMITED] and the default scope
 * for the coroutine that listen from the reader and writes to the channel is [IoScope]
 * @param reader the reader
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
 * @param scope the scope of the internal listening job (default [IoScope])
 * @param mapper the transformation function
 */
class MappedBufferedReaderFlowWrapper<O>(
    private val reader : BufferedReader,
    reply : Int = 0,
    extraBufferCapacity : Int = 0,
    onBufferOverflow: BufferOverflow,
    private val scope : CoroutineScope = IoScope,
    private val mapper : (String) -> O
) : Closeable, AutoCloseable {

    constructor(inputStream: InputStream, reply : Int = 0,
                extraBufferCapacity : Int = 0,
                onBufferOverflow: BufferOverflow,
                scope : CoroutineScope = IoScope,
                mapper : (String) -> O)
            : this(inputStream.bufferedReader(), reply, extraBufferCapacity, onBufferOverflow, scope, mapper)

    private val internalFlow = MutableSharedFlow<O>(reply, extraBufferCapacity, onBufferOverflow)

    /**
     * The new [SharedFlow] that can be used instead of the old [BufferedReader]
     * The data received from the original reader are *mapped* and then emitted
     * to this flow
     */
    val sharedFlow : SharedFlow<O> = internalFlow.asSharedFlow()

    private val job = scope.launch(Dispatchers.IO) {
        var line : String? = null
        try {
            do{
                line = reader.readLine()
                //IO_LOG.info("readed line from BufferedReader: $line")
                if(line != null) {
                    internalFlow.emit(mapper(line))
                    //IO_LOG.info("line [$line] sent to channel")
                }
            } while (line != null)
        } catch (ioe : IOException) {
            //Buffered Reader is closed -> exit
        }
    }

    /**
     * Closes the reader and waits for the listening
     * job end
     */
    override fun close() {
        reader.close()
        runBlocking { job.join() }
    }
}