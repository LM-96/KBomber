package kbomberx.io.j2k

import kbomberx.io.TOLERANCE_MILLIS
import kbomberx.io.failsAfterMillis
import kbomberx.io.routing.ChannelRoute
import kbomberx.io.routing.ChannelRouter
import kbomberx.io.routing.FanInChannelRouter
import kbomberx.io.routing.createsFanInRouter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FanInRouterTest {

    companion object {
        private const val DENY_ALL = 0
        private const val ROUTE_1 = 1
        private const val ROUTE_2 = 2
        private const val ROUTE_1_2 = 3
        private const val ROUTE_1_HELLO = "hello by route 1"
        private const val ROUTE_2_HELLO = "hello by route 2"
    }

    private var initialized = false
    private lateinit var outChan : Channel<String>
    private lateinit var router : ChannelRouter<String>
    private lateinit var route1 : ChannelRoute<String>
    private lateinit var route2 : ChannelRoute<String>
    private var currentPassage = DENY_ALL

    @BeforeTest fun before() = runBlocking {
        if(!initialized) {
            outChan = Channel(Channel.UNLIMITED)
            router = outChan.createsFanInRouter("test-router").started()
            route1 = router.newRoute { currentPassage == ROUTE_1 || currentPassage == ROUTE_1_2 }
            route2 = router.newRoute { currentPassage == ROUTE_2 || currentPassage == ROUTE_1_2 }
            initialized = true
        }

        //Clear the channel
        var channelResult : ChannelResult<String>
        do {
            channelResult = outChan.tryReceive()
        } while (channelResult.isSuccess)
    }



    @Test fun testOnlyRoute1() = runBlocking {
        currentPassage = ROUTE_1
        route1.channel.send(ROUTE_1_HELLO)
        route2.channel.send(ROUTE_2_HELLO)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_1_HELLO, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testOnlyRoute2() = runBlocking {
        currentPassage = ROUTE_2
        route1.channel.send(ROUTE_1_HELLO)
        route2.channel.send(ROUTE_2_HELLO)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_2_HELLO, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testBothRoutes() = runBlocking {
        currentPassage = ROUTE_1_2
        route1.channel.send(ROUTE_1_HELLO)
        delay(200)
        route2.channel.send(ROUTE_2_HELLO)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_1_HELLO, outChan.receive())}
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_2_HELLO, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testAllRoutesDenied() = runBlocking {
        currentPassage = DENY_ALL
        route1.channel.send(ROUTE_1_HELLO)
        route2.channel.send(ROUTE_2_HELLO)

        for (i in 1..5) {
            assertTrue(outChan.tryReceive().isFailure)
            assertTrue(outChan.isEmpty)
            delay(200)
        }
    }

}