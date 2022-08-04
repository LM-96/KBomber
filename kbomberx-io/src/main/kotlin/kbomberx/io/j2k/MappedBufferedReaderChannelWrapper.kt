package kbomberx.io.j2k

import kbomberx.io.IoScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.BufferedReader
import java.io.Closeable
import java.io.IOException
import java.io.InputStream

/**
 * A wrapper class for [BufferedReader] that *wraps* the standard *Java* buffered reader
 * to a new *Kotlin* [Channel]. The strings received from the reader are *mapped* using
 * the transformation function passed to the constructor of this class.
 * After the creation of the object, the old reader **must no longer be used** and
 * will be automatically closed with the new channel.
 * The default capacity of the new channel is [Channel.UNLIMITED] and the default scope
 * for the coroutine that listen from the reader and writes to the channel is [IoScope]
 * @param reader the reader
 * @param capacity the capacity of the channel (default [Channel.UNLIMITED])
 * @param scope the scope of the internal listening job (default [IoScope])
 * @param mapper the transformation function
 */
class MappedBufferedReaderChannelWrapper<O>(
    private val reader : BufferedReader,
    private val capacity : Int = Channel.UNLIMITED,
    private val scope : CoroutineScope = IoScope,
    private val mapper : (String) -> O
) : Closeable, AutoCloseable {

    constructor(inputStream: InputStream,
                capacity: Int = Channel.UNLIMITED,
                scope : CoroutineScope = IoScope,
                mapper : (String) -> O)
            : this(inputStream.bufferedReader(), capacity, scope, mapper)

    private val channel = Channel<O>(capacity)

    /**
     * The new [Channel] that can be used instead of the old [BufferedReader].
     * The data received from the original reader are *mapped* and then sent
     * to this channel
     */
    val receiveChannel : ReceiveChannel<O> = channel

    private val job = scope.launch(Dispatchers.IO) {
        var line : String? = null
        try {
            do{
                line = reader.readLine()
                //IO_LOG.info("readed line from BufferedReader: $line")
                if(line != null) {
                    channel.send(mapper(line))
                    //IO_LOG.info("line [$line] sent to channel")
                }
            } while (line != null)
        } catch (ioe : IOException) {
            //Buffered Reader is closed
            channel.close(ioe)
        } catch (ce: CancellationException) {
            //Channel scope is cancelled
            reader.close()
        } catch (csce : ClosedSendChannelException) {
            //Channel is closed for send
            reader.close()
        }

        //Needed in case of reaching EOF in the stream
        if(!channel.isClosedForSend) {
            channel.close()
        }
    }

    /**
     * Closes the internal channel and waits for the listening
     * job end. The closure of the channel also causes the closure
     * of the internal [BufferedReader]
     */
    override fun close() {
        if(!channel.isClosedForSend)
            channel.close()
        runBlocking { job.join() }
    }
}