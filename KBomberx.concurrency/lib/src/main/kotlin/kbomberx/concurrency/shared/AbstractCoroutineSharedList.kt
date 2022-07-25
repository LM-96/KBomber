package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

abstract class AbstractCoroutineSharedList<T>(
    private val internalList : List<T>,
    private val scope: CoroutineScope = SharedScope
) {

    private val mainChannel = Channel<CmdRequest<T>>()

    private enum class CmdType {
        ADD, GET, REMOVE
    }

    private data class CmdRequest<T>(
        val reqType : CmdType,
        val reqParam : T? = null,
        val responseChan : Channel<Result<*>> = Channel()
    )

    private val job = scope.launch {
        var working = true
        var cmd : CmdRequest<T>

        while (working) {
            cmd = mainChannel.receive()
            when(cmd.reqType) {

                CmdType.ADD -> {

                }

                CmdType.GET -> {

                }

                CmdType.REMOVE -> {

                }
            }
        }
    }

}