package kbomberx.io.j2k

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.SendChannel
import java.io.*

/**
 * A wrapper class for [BufferedWriter] that *wraps* the standard *Java* buffered writer
 * to a new *Kotlin* [Channel].
 * After the creation of the object, the old writerr **must no longer be used** and
 * will be automatically closed with the new channel.
 * The default capacity of the new channel is [Channel.UNLIMITED] and the default scope
 * for the coroutine that listen from the channel and writes to the writer is [IoScope]
 */
class BufferedWriterChannelWrapper(
    private val writer: BufferedWriter,
    private val capacity : Int = Channel.UNLIMITED,
    private val scope : CoroutineScope = IoScope
) : Closeable, AutoCloseable {

    constructor(outputStream: OutputStream, capacity: Int = Channel.UNLIMITED, scope : CoroutineScope = IoScope)
            : this(outputStream.bufferedWriter(), capacity, scope)

    private val channel = Channel<String>(capacity)

    /**
     * The new [Channel] that can be used instead of the old [BufferedWriter]
     */
    val sendChannel : SendChannel<String> = channel

    private val job = scope.launch(Dispatchers.IO) {
        var line : String = ""
        try {
            while(true) {
                line = channel.receive()
                writer.write("${line.trim()}\n")
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