package kbomberx.concurrency.coroutineserver

import kotlinx.coroutines.channels.Channel

data class CmdServerRequest(
    val requestCode : Int,
    val responseChannel : Channel<ServerReply> = Channel(),
    val requestParams : Array<out Any> = arrayOf()
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CmdServerRequest

        if (requestCode != other.requestCode) return false
        if (responseChannel != other.responseChannel) return false
        if (!requestParams.contentEquals(other.requestParams)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = requestCode
        result = 31 * result + responseChannel.hashCode()
        result = 31 * result + requestParams.contentHashCode()
        return result
    }
}
