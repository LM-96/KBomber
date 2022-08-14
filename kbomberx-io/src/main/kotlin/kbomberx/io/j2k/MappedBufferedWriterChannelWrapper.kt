package kbomberx.io.j2k

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import java.io.*

/**
 * A wrapper class for [BufferedWriter] that *wraps* the standard *Java* buffered writer
 * to a new *Kotlin* [Channel]. The [sendChannel] can be used to send data that will be
 * written on the old writer. The object sent to the [sendChannel] are mapped into strings
 * using [mapper] function.
 * After the creation of the object, the old writer **must no longer be used** and
 * will be automatically closed with the new channel.
 * The default capacity of the new channel is [Channel.UNLIMITED] and the default scope
 * for the coroutine that listen from the channel and writes to the writer is [IoScope]
 * @param writer the writer to be wrapped
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @param mapper the transformation function from [T] to [String]
 */
class MappedBufferedWriterChannelWrapper<T>(
    private val writer: BufferedWriter,
    private val capacity : Int = Channel.UNLIMITED,
    private val scope : CoroutineScope = IoScope,
    private val mapper : (T) -> String
) : Closeable, AutoCloseable {

    constructor(outputStream: OutputStream, capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope,
                mapper: (T) -> String)
            : this(outputStream.bufferedWriter(), capacity, scope, mapper)

    private val channel = Channel<T>(capacity)

    /**
     * The new [Channel] that can be used instead of the old [BufferedWriter]
     */
    val sendChannel : SendChannel<T> = channel

    private val job = scope.launch(Dispatchers.IO) {
        var obj : T
        try {
            while(true) {
                obj = channel.receive()
                writer.write("${mapper(obj)}\n")
                writer.flush()
            }
        } catch (crce : ClosedReceiveChannelException) {
            //Channel has been closed -> writer must be closed
            writer.close()
        } catch (ioe : IOException) {
            //Writer has been closed -> channel must be closed
            channel.close(ioe)
        } catch (ce : CancellationException) {
            //Channel has been cancelled -> writer must be closed
            writer.close()
        }

        //Ensure channel is closed at the end
        if(!channel.isClosedForReceive) {
            channel.close()
        }
    }

    /**
     * Closes the internal channel and waits for the listening
     * job end. The closure of the channel also causes the closure
     * of the internal [BufferedWriter]
     */
    override fun close() {
        if(!channel.isClosedForReceive)
            channel.close()
        runBlocking { job.join() }
    }

}