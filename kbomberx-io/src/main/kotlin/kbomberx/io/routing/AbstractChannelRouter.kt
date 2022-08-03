package kbomberx.io.routing

import kbomberx.concurrency.coroutineserver.CmdServerRequest
import kbomberx.concurrency.coroutineserver.ServerReply
import kbomberx.concurrency.coroutineserver.requestWithParameter
import kbomberx.io.IoScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.SelectBuilder
import kotlinx.coroutines.selects.select
import java.io.Closeable
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * Abstract class for routers that implements the part of adding
 * and managing routes and creates and start the job associated
 * with the router. The default [CoroutineScope] is [IoScope]
 */
abstract class AbstractChannelRouter<L>(
    private val scope : CoroutineScope = IoScope
) : ChannelRouter<L>(), Closeable, AutoCloseable {

    companion object {
        private const val START_ROUTER_CODE = 0
        private const val ADD_ROUTE_CODE = 1
        private const val REMOVE_ROUTE_CODE = 2
        private const val GET_ROUTE_CODE = 3
        private const val TERMINATE_ROUTER_CODE = 4
    }

    private val cmdChannel = Channel<CmdServerRequest>()
    protected var terminated = false

    protected abstract fun onJobTermination()
    abstract fun SelectBuilder<Unit>.routerJob(routes : MutableMap<String, ChannelRoute<L>>)

    private val job = scope.launch {
        var started = false

        val routes = mutableMapOf<String, ChannelRoute<L>>()

        while(isActive && !terminated) {

            select {

                cmdChannel.onReceive { request ->

                    try {
                        when(request.requestCode) {

                            START_ROUTER_CODE -> {
                                started = true
                                replyWithOk(request)
                            }

                            ADD_ROUTE_CODE -> {
                                val route = request.requestParams[0] as ChannelRoute<L>
                                routes[route.name] = route
                                replyWithOk(request)
                            }

                            REMOVE_ROUTE_CODE -> {
                                val removed = routes.remove(request.requestParams[0] as String)
                                replyWithRoute(request, removed)
                            }

                            GET_ROUTE_CODE -> {
                                val route = routes[request.requestParams[0] as String]
                                replyWithRoute(request, route)
                            }

                            TERMINATE_ROUTER_CODE -> {
                                terminated = true
                                replyWithOk(request)
                            }
                        }
                    } catch (e : Exception) {
                        replyWithException(request, e)
                    }
                }//On receive of command channel

                if(started)
                    this.routerJob(routes)

            }

        }
        //Termination
        routes.forEach { it.value.channel.close() }
        routes.clear()

        onJobTermination()
    }

    protected fun terminate() {

    }

    private suspend fun performRequest(code : Int, vararg params : Any) : ServerReply {
        val req = requestWithParameter(code, params)
        cmdChannel.send(req)
        return req.responseChannel.receive()
    }

    private suspend fun replyWithOk(request : CmdServerRequest) {
        try {
            request.responseChannel.send(ServerReply(ServerReply.OK_CODE))
            request.responseChannel.close()
        } catch (_ : Exception) {/*Send fails -> ignoring*/}
    }

    private suspend fun replyWithRoute(request : CmdServerRequest, route : ChannelRoute<L>?) {
        try {
            request.responseChannel.send(ServerReply(ServerReply.OK_CODE,
                arrayOf(Optional.ofNullable(route))))
            request.responseChannel.close()
        } catch (_ : Exception) {/*Send fails -> ignoring*/}
    }

    private suspend fun replyWithException(request: CmdServerRequest, exception : Exception) {
        try {
            request.responseChannel.send(ServerReply(ServerReply.ERR_CODE, arrayOf(exception)))
            request.responseChannel.close()
        } catch (_ : Exception) {/* Send fails -> ignoring */}
    }

    override suspend fun start() {
        performRequest(START_ROUTER_CODE)
    }

    override suspend fun newRoute(name: String, routeChannelCapacity: Int,
                                  passage: (L) -> Boolean): ChannelRoute<L> {
        val route = ChannelRoute(name, Channel<L>(routeChannelCapacity), passage)
        val res = performRequest(ADD_ROUTE_CODE, route)
        res.throwError()

        return route
    }

    override suspend fun registerRoute(route: ChannelRoute<L>) {
        performRequest(ADD_ROUTE_CODE, route)
    }

    override suspend fun getRoute(name: String): ChannelRoute<L>? {
        val res = performRequest(GET_ROUTE_CODE, name)
        val route = res.throwErrorOrGetFirstParameter() as Optional<ChannelRoute<L>>

        if(route.isEmpty)
            return null
        return route.get()
    }

    override suspend fun removeRoute(name: String): ChannelRoute<L>? {
        val res = performRequest(REMOVE_ROUTE_CODE, name)
        val route = res.throwErrorOrGetFirstParameter() as Optional<ChannelRoute<L>>

        if(route.isEmpty)
            return null
        return route.get()
    }

    override fun close() {
        runBlocking {
            val res = performRequest(TERMINATE_ROUTER_CODE)
            res.throwError()
            job.join()
        }
    }

}