package kbomberx.io.process

import kbomberx.io.utils.availableText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.select

/**
 * Represents the standard `IO` of a process or console in the
 * *Kotlin* world (with channels)
 */
data class KStdIO(
    val stdOut : SendChannel<String>,
    val stdErr : ReceiveChannel<String>,
    val stdIn : ReceiveChannel<String>
) {

    /**
     * Reads a string from the `stdIn` retuning it.
     * If [stdErr] receives some lines while it is waiting on [stdIn], a
     * [ProcessException] with the content coming from [stdErr] is thrown.
     * This function can be used to safely read the output of a process
     * @throws ProcessException if something is detected in [stdErr]
     * @return a line read from [stdIn]
     */
    @Throws(ProcessException::class)
    suspend fun readString() : String? {
        var line : String
        return try {
            select {
                stdIn.onReceive {
                    it
                }
                stdErr.onReceive {
                    line = it
                    line += stdErr.availableText()
                    throw ProcessException(line)
                }
            }
        }
        catch (_ : CancellationException) { null }
        catch (_ : ClosedReceiveChannelException) { null }
    }

    /**
     * Reads all the text coming from the `stdIn` until it has been closed.
     * If [stdErr] receives some lines while it is waiting on [stdIn], a
     * [ProcessException] with the content coming from [stdErr] is thrown.
     * This function can be used to safely read the output of a process until
     * it has been closed
     * @throws ProcessException if something is detected in [stdErr]
     * @return all the text read from [stdIn] or `null` if nothing is read and the
     * [stdIn] channel has been closed
     */
    @Throws(ProcessException::class)
    suspend fun readUntilExit() : String? {
        var inLines = ""
        var errLines = ""
        var terminated = false
        while(!terminated && errLines == "") {
            try {
                select {
                    stdIn.onReceive {
                        inLines += "$it/n"
                    }
                    stdErr.onReceive {
                        errLines += "$it/n"
                        errLines += stdErr.availableText()
                        throw ProcessException(errLines)
                    }
                }
            }
            catch (_ : CancellationException) { terminated = true }
            catch (_ : ClosedReceiveChannelException) { terminated = true }
        }

        if(inLines == "")
            return null

        return inLines
    }

}