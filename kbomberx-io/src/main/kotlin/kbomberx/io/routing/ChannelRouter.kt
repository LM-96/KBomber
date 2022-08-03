package kbomberx.io.routing

import kotlinx.coroutines.channels.Channel

/**
 * A *one-to-many* or *many-to-one* router that *replicates* and *dispatches* message from
 * the input line(s) to the output line(s)
 * @param L the type of the routes (on the multiple lines side)
 */
abstract class ChannelRouter<L> {

    companion object {
        private var routeNum = 0
    }

    /**
     * Start the job of the router.
     * After this call the router is able to receive message from the input line(s)
     * and forward it to the output line(s), replicating it if needed
     */
    abstract suspend fun start()

    /**
     * Creates a new route that is automatically added to this router
     * @throws Exception if a route with the given [name] is already present
     * @param name the name of the route
     * @param routeChannelCapacity the capacity of the channel associated to the new route
     * @param passage the function that let to evaluate if the route is opened
     * @return the new [ChannelRoute] instance
     */
    abstract suspend fun newRoute(name : String = "route:${routeNum++}",
                                              routeChannelCapacity : Int = Channel.UNLIMITED,
                                              passage : (L) -> Boolean = {true}) : ChannelRoute<L>

    /**
     * Register the given [route] to this router
     * @throws IllegalArgumentException if a route with this name already exists
     * @param route the route to register
     */
    abstract suspend fun registerRoute(route : ChannelRoute<L>)

    /**
     * Returns the route with the given [name] that is registered to this router
     * @param name the name of the route
     * @return the route with the given [name] that is registered to this router
     * or `null` if no route with this name has previously been registered
     */
    abstract suspend fun getRoute(name : String) : ChannelRoute<L>?

    /**
     * Removes and returns the route with the given [name].
     * If no route with this name has previously been registered,
     * then nothing is done and `null` is returned
     * @param name the name of the route
     * @return the removed route
     * or `null` if no route with this name has previously been registered
     */
    abstract suspend fun removeRoute(name : String) : ChannelRoute<L>?

    /**
     * Starts this router and returns it
     * @return this router
     */
    suspend fun started() : ChannelRouter<L> {
        start()
        return this
    }

    /**
     * Creates a new route starting from the given [channel] and automatically add it
     * to this router
     * @throws IllegalArgumentException if a route with this name already exists
     * @param name the name of the route
     * @param channel the channel that will be used for the route
     * @param passage the function that let to evaluate if the route is opened
     */
    suspend fun registerRoute(name : String = "route:${routeNum++}",
                              channel: Channel<L>,
                              passage : (L) -> Boolean = {true}) : ChannelRoute<L> {
        val route = ChannelRoute(name, channel, passage)
        registerRoute(route)
        return route
    }

}