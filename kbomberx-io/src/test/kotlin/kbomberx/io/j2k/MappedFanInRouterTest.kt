package kbomberx.io.j2k

import kbomberx.io.TOLERANCE_MILLIS
import kbomberx.io.failsAfterMillis
import kbomberx.io.routing.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ChannelResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.Optional
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappedFanInRouterTest {

    companion object {
        private const val DENY_ALL = 0
        private const val ROUTE_1 = 1
        private const val ROUTE_2 = 2
        private const val ROUTE_1_2 = 3
        private const val ROUTE_1_INT = 11
        private const val ROUTE_1_STRING = ROUTE_1_INT.toString()
        private const val ROUTE_2_INT = 12
        private const val ROUTE_2_STRING = ROUTE_2_INT.toString()
        private const val MAPPED_DENY_VALUE = 999
    }

    private var initialized = false
    private lateinit var outChan : Channel<String>
    private lateinit var router : ChannelRouter<Int>
    private lateinit var route1 : ChannelRoute<Int>
    private lateinit var route2 : ChannelRoute<Int>
    private var currentPassage = DENY_ALL

    @BeforeTest fun before() = runBlocking {
        if(!initialized) {
            outChan = Channel(Channel.UNLIMITED)
            router = outChan.createsFanInMappedRouter("test-router") {
                return@createsFanInMappedRouter try {
                    if(it == MAPPED_DENY_VALUE)
                        Optional.empty<String>()
                    else
                        Optional.of(it.toString())
                } catch (_ : Exception) {
                    Optional.empty<String>()
                }
            }
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
        route1.channel.send(ROUTE_1_INT)
        route2.channel.send(ROUTE_2_INT)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_1_STRING, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testOnlyRoute2() = runBlocking {
        currentPassage = ROUTE_2
        route1.channel.send(ROUTE_1_INT)
        route2.channel.send(ROUTE_2_INT)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_2_STRING, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testBothRoutes() = runBlocking {
        currentPassage = ROUTE_1_2
        route1.channel.send(ROUTE_1_INT)
        delay(200)
        route2.channel.send(ROUTE_2_INT)
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_1_STRING, outChan.receive())}
        failsAfterMillis(TOLERANCE_MILLIS, this) {assertEquals(ROUTE_2_STRING, outChan.receive())}
        assertTrue(outChan.isEmpty)
        assertTrue(outChan.tryReceive().isFailure)
    }

    @Test fun testAllRoutesDenied() = runBlocking {
        currentPassage = DENY_ALL
        route1.channel.send(ROUTE_1_INT)
        route2.channel.send(ROUTE_2_INT)

        for (i in 1..5) {
            assertTrue(outChan.tryReceive().isFailure)
            assertTrue(outChan.isEmpty)
            delay(200)
        }
    }

    @Test fun testMappedDeny() = runBlocking {
        currentPassage = ROUTE_1_2
        route1.send(MAPPED_DENY_VALUE)
        route2.send(MAPPED_DENY_VALUE)

        for (i in 1..5) {
            assertTrue(outChan.tryReceive().isFailure)
            assertTrue(outChan.isEmpty)
            delay(200)
        }

    }

}