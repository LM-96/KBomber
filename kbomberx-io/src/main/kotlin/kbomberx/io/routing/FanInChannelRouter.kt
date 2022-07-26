package kbomberx.io.routing

import kbomberx.io.IoScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.selects.SelectBuilder
import java.util.*

/**
 * A router with multiple input lines and only one output channel.
 * This router listen all the input lines and when one of this receives
 * a message, the message is sent to single output line if the passage of
 * the receiving route is opened
 * @param routerName the name of the router
 * @param outChan the output channel that receives data that come from the multiple input lines
 * @param scope the [CoroutineScope] of the job that realizes the routing functionality
 */
class FanInChannelRouter<L>(
    val routerName : String,
    private val outChan : SendChannel<L>,
    scope : CoroutineScope = IoScope
) : AbstractChannelRouter<L>(scope){

    override fun onJobTermination() {
        outChan.close()
    }

    override fun SelectBuilder<Unit>.routerJob(routes: MutableMap<String, ChannelRoute<L>>) {
        routes.forEach { (_, route) ->
            try {
                route.channel.onReceive { msg ->
                    route.verifyAndTrySendTo(outChan, msg)
                }
            } catch (crce : ClosedReceiveChannelException) {
                //One input line is closed
                routes.remove(route.name)
            } catch (csce : ClosedSendChannelException) {
                //Out route is closed
                terminated = true
            } catch (ce : CancellationException) {
                routes.values.forEach { it.channel.close(ce) }
                routes.clear()
                terminated = true
            }
        }
    }
}