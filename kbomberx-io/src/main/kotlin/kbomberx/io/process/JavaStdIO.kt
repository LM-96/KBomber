package kbomberx.io.process

import kbomberx.io.IoScope
import kbomberx.io.j2k.stringReceiveChannel
import kbomberx.io.j2k.stringSendChannel
import kbomberx.io.j2k.BufferedReaderChannelWrapper
import kbomberx.io.j2k.BufferedWriterChannelWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import java.io.InputStream
import java.io.OutputStream

/**
 * Represents the standard `IO` of a process or console in the
 * *Java* world (with stream)
 */
data class JavaStdIO(
    val stdOut : OutputStream,
    val stdErr : InputStream,
    val stdIn : InputStream
) {

    /**
     * Converts this standard `IO` from the old *Java* streams to the new
     * *Kotlin* [KStdIO] that uses channel. The conversion uses [BufferedReaderChannelWrapper]
     * and [BufferedWriterChannelWrapper] so, after this call, this class **MUST NO longer
     * be used** (use the result [KStdIO] instead)
     * @param capacity the capacity of all the channel associated
     * to the streams (default [Channel.UNLIMITED])
     * @param scope the scope of all the internal jobs (default [IoScope])
     */
    fun toKotlinStdIO(capacity : Int = Channel.UNLIMITED,
                      scope : CoroutineScope = IoScope) : KStdIO =
        KStdIO(stdOut.stringSendChannel(capacity, scope),
            stdErr.stringReceiveChannel(capacity, scope),
            stdIn.stringReceiveChannel(capacity, scope)
        )

    /**
     * Converts this standard `IO` from the old *Java* streams to the new
     * *Kotlin* [KStdIO] that uses channel. The conversion uses [BufferedReaderChannelWrapper]
     * and [BufferedWriterChannelWrapper] so, after this call, this class **MUST NO longer
     * be used** (use the result [KStdIO] instead)
     * @param stdOutCapacity the capacity of the `stdOut` channel associated
     * to the streams (default [Channel.UNLIMITED])
     * @param stdErrCapacity the capacity of the `stdErr` channel associated
     * to the streams (default [Channel.UNLIMITED])
     * @param stdInCapacity the capacity of the `stdIn` channel associated
     * to the streams (default [Channel.UNLIMITED])
     * @param stdOutScope the scope of the internal job of the `stdOut` worker (default [IoScope])
     * @param stdErrScope the scope of the internal job of the `stdErr` worker (default [IoScope])
     * @param stdInScope the scope of the internal job of the `stdIn` worker (default [IoScope])
     */
    fun toKotlinStdIO(stdOutCapacity : Int = Channel.UNLIMITED,
                      stdErrCapacity : Int = Channel.UNLIMITED,
                      stdInCapacity : Int = Channel.UNLIMITED,
                      stdOutScope : CoroutineScope = IoScope,
                      stdErrScope : CoroutineScope = IoScope,
                      stdInScope : CoroutineScope = IoScope
    ) : KStdIO = KStdIO(stdOut.stringSendChannel(stdOutCapacity, stdOutScope),
            stdErr.stringReceiveChannel(stdErrCapacity, stdErrScope),
            stdIn.stringReceiveChannel(stdInCapacity, stdInScope)
        )

}
