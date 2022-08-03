package kbomberx.io.utils

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ReceiveChannel

/**
 * Receives all the lines currently available in this channel, until
 * it is empty
 * @return all the lines currently available in this channel or `null`
 * if the channel is empty
 */
suspend fun ReceiveChannel<String>.availableText() : String? {
    var res = ""
    var received = tryReceive()
    while(received.isSuccess) {
        res += received.getOrThrow()
        received = tryReceive()
    }

    if(res == "") {
        return null
    }
    return res
}

/**
 * Receives all the lines written in this channel until it is closed
 * @return all the lines written in this channel until it is closed or
 * `null` if this channel is empty and it has been closed
 */
suspend fun ReceiveChannel<String>.receiveTextUntilClosed() : String? {
    var text = ""
    try {
        while(true)
            text += (receive() + "\n")
    } catch (_ : ClosedReceiveChannelException) {
        return if(text.isBlank())
            null
        else text.trim()
    }

}