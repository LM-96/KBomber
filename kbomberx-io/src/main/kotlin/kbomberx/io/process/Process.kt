package kbomberx.io.process

import kbomberx.io.IoScope
import kbomberx.io.j2k.stringReceiveChannel
import kbomberx.io.j2k.stringSendChannel
import kbomberx.io.j2k.BufferedWriterChannelWrapper
import kbomberx.io.j2k.BufferedReaderChannelWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel

/**
 * Opens a new channel in which is possible to receive the content that
 * came from the standard input. This function uses a [BufferedReaderChannelWrapper]
 * @param capacity the capacity of the new channel  (default [Channel.UNLIMITED])
 * @param scope the scope of the internal jobs (default [IoScope])
 * @return a new channel that receives data from `stdIn`
 */
fun Process.stdInChannel(
    capacity : Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope) : ReceiveChannel<String> {
    return inputStream.stringReceiveChannel(capacity, scope)
}

/**
 * Opens a new channel in which is possible to write the content that
 * has to finish to the `stdOut`. This function uses a [BufferedWriterChannelWrapper]
 * @param capacity the capacity of the new channel  (default [Channel.UNLIMITED])
 * @param scope the scope of the internal jobs (default [IoScope])
 * @return a new channel that receives data from `stdIn`
 */
fun Process.stdOutChannel(
    capacity : Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope) : SendChannel<String> {
    return outputStream.stringSendChannel(capacity, scope)
}

/**
 * Opens a new channel in which is possible to receive the content that
 * came from the standard error. This function uses a [BufferedReaderChannelWrapper]
 * @param capacity the capacity of the new channel  (default [Channel.UNLIMITED])
 * @param scope the scope of the internal jobs (default [IoScope])
 * @return a new channel that receives data from `stdErr`
 */
fun Process.stdErrChannel(
    capacity : Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope) : ReceiveChannel<String> {
    return errorStream.stringReceiveChannel(capacity, scope)
}

/**
 * Returns a [JavaStdIO] instance that allow to access all the `std` streams
 * @return a [JavaStdIO] instance that allow to access all the `std` streams
 */
fun Process.getStdIO() : JavaStdIO {
    return JavaStdIO(outputStream, errorStream, inputStream)
}

/**
 * Return a [KStdIO] instance that allow to access all the `std` streams of
 * the process as *Kotlin* channels.
 * It's important that **after this call the normal *Java* streams MUST NO longer
 * be used on this process** in order to avoid conflicts.
 * This function internally use [BufferedReaderChannelWrapper] and [BufferedWriterChannelWrapper]
 * @param capacity the capacity of all the channel associated
 * to the streams (default [Channel.UNLIMITED])
 * @param scope the scope of all the internal jobs (default [IoScope])
 * @return a [KStdIO] instance that allow to access all the `std` streams of
 * the process as *Kotlin* channels
 */
fun Process.getKotlinStdIO(
    capacity: Int = Channel.UNLIMITED,
    scope : CoroutineScope = IoScope) : KStdIO {
    return getStdIO().toKotlinStdIO(capacity, scope)
}

/**
 * Reads all the text coming from the process `stdIn` until process is exited.
 * If `stdErr` receives some lines while it is waiting on `stdIn`, a
 * [ProcessException] with the content coming from error stream is thrown.
 * This function can be used to safely read the output of a process until
 * it has been closed
 * @throws ProcessException if something is detected in `stdErr` of the process
 * @return all the text read from `stdIn` or `null` if nothing is read and the
 * standard input of the process has been closed
 */
@Throws(ProcessException::class)
suspend fun Process.readUntilExited(scope : CoroutineScope = IoScope) : String? {
    return getKotlinStdIO(scope = scope).readUntilExit()
}