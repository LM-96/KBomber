package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.selects.SelectBuilder
import java.util.*

/**
 * A router with one input channel and multiple output lines.
 * This router listen on the channel and when one message is received,
 * the message is replicated and sent on each route that has the passage opened
 * for the current message
 * @param routerName the name of the router
 * @param sourceChan the source channel that receives data that has to be
 * replicated to the output routes
 * @param scope the [CoroutineScope] of the job that realizes the routing functionality
 */
class FanOutChannelRouter<L>(
    val routerName : String,
    private val sourceChan : ReceiveChannel<L>,
    private val scope : CoroutineScope = IoScope
) : AbstractChannelRouter<L>(scope) {

    private lateinit var iterator : MutableIterator<MutableMap.MutableEntry<String, ChannelRoute<L>>>
    private lateinit var current : MutableMap.MutableEntry<String, ChannelRoute<L>>

    override fun onJobTermination() {
        //Nothing to do (routes are closed by superclass job) :)
    }

    override fun SelectBuilder<Unit>.routerJob(routes: MutableMap<String, ChannelRoute<L>>) {
        try {
            sourceChan.onReceive { msg ->
                iterator = routes.iterator()
                while (iterator.hasNext()) {
                    try {
                        current = iterator.next()
                        current.value.verifyAndTrySend(msg)
                    } catch (csce: ClosedSendChannelException) {
                        //Closed route -> remove
                        iterator.remove()
                    } catch (ce: CancellationException) {
                        iterator.remove()
                    }
                }
            }
        } catch (crce : ClosedReceiveChannelException) {
            terminated = true
        } catch (ce : CancellationException) {
            terminated = true
        }
    }
}