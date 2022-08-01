package kbomberx.concurrency.shared

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoroutineSharedServerTest {

    class Counter {

        companion object {
            const val INCREMENT_NAME = "increment"
            const val ADD_NAME = "add"
            const val GET_NAME = "get"
            const val INCREMENT_DESC = "increments the value of the counter"
            const val ADD_DESC = "add the given value to the counter"
            const val GET_DESC = "get the current value of the counter"
        }

        var value = 0
        private set

        @ServiceDescription(INCREMENT_DESC)
        fun increment() {
            value++
        }

        @ServiceDescription(ADD_DESC)
        fun add(toAdd : Int) {
            value+=toAdd
        }

        @ServiceDescription(GET_DESC)
        fun get() : Int {
            return value
        }

    }

    private val counter = Counter()
    private val sharedServer = CoroutineSharedServer(counter)

    @Test fun testDiscoveryService() = runBlocking {
        val svcs = sharedServer.discoverServices()
        assertTrue(svcs.size >= 3)
        assertTrue(svcs.find { it.serviceName == Counter.INCREMENT_NAME } != null)
        assertTrue(svcs.find { it.serviceName == Counter.ADD_NAME } != null)
        assertTrue(svcs.find { it.serviceName == Counter.GET_NAME } != null)
        assertEquals(Counter.INCREMENT_DESC, svcs.find { it.serviceName == Counter.INCREMENT_NAME }!!.serviceDescription)
        assertEquals(Counter.ADD_DESC, svcs.find { it.serviceName == Counter.ADD_NAME }!!.serviceDescription)
        assertEquals(Counter.GET_DESC, svcs.find { it.serviceName == Counter.GET_NAME }!!.serviceDescription)
    }

    @Test fun testExecuteIncrementAndGet() = runBlocking {
        val currValue = counter.value
        val incRes = sharedServer.executeService(Counter.INCREMENT_NAME)
        assertEquals(Void.TYPE, incRes.resultClass)

        val getRes = sharedServer.executeService(Counter.GET_NAME)
        assertEquals(Int::class.java, getRes.resultClass)
        assertEquals(currValue + 1, getRes.result!! as Int)
    }

    @Test fun testExecuteAddAndGet() = runBlocking {
        val currValue = counter.value
        val toAdd = 2
        val addRes = sharedServer.executeService(Counter.ADD_NAME)
        assertEquals(Void.TYPE, addRes.resultClass)

        val getRes = sharedServer.executeService(Counter.GET_NAME)
        assertEquals(Int::class.java, getRes.resultClass)
        assertEquals(currValue + toAdd, getRes.result!! as Int)
    }


}