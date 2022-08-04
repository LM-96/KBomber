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
 * a message, the message is *mapped* then sent to single output line if the passage of
 * the receiving route is opened.
 * The transformation function is passed as parameter and return an [Optional], absent
 * if the passage is denied by the same [mapper]
 * @param routerName the name of the router
 * @param outChan the output channel that receives data that come from the multiple input lines
 * after their mapping
 * @param scope the [CoroutineScope] of the job that realizes the routing functionality
 * @param mapper the function that transforms the values that outcome from the input lines
 * to that accepted by the single output line.
 * If this function returns an [Optional.empty], the no value is sent to the
 * output channel (means that the passage is denied by the same mapper)
 */
class MappedFanInChannelRouter<L, O>(
    val routerName : String,
    private val outChan : SendChannel<O>,
    scope : CoroutineScope = IoScope,
    /**
     * The function that transforms the values that outcome from the input lines
     * to that accepted by the single output line.
     * If this function returns an [Optional.empty], then no value is sent to the
     * output channel (means that the passage is denied by the same mapper)
     */
    private val mapper : (L) -> Optional<O>
) : AbstractChannelRouter<L>(scope){

    private lateinit var mapped : Optional<O>

    override fun onJobTermination() {
        outChan.close()
    }

    override fun SelectBuilder<Unit>.routerJob(routes: MutableMap<String, ChannelRoute<L>>) {
        routes.forEach { (_, route) ->
            try {
                route.channel.onReceive { msg ->
                    if(route.passage(msg)) {
                        mapped = mapper(msg)
                        if(mapped.isPresent) {
                            outChan.send(mapped.get())
                        }
                    }
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