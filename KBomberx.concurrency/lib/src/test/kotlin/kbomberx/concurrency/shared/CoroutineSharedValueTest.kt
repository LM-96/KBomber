package kbomberx.concurrency.shared

import kotlinx.coroutines.channels.ClosedSendChannelException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CoroutineSharedValueTest {

    @Test fun testSharedValue() {
        val sharedValue = "test1".asCoroutineShared()
        runBlocking {
            assertEquals("test1", sharedValue.get())
            sharedValue.set("test2")
            assertEquals("test2", sharedValue.get())
            sharedValue.close()

            try {
                sharedValue.get()
                fail("Not throws the exception")
            } catch (e : Exception) {
                assertTrue(e is ClosedSendChannelException)
            }
        }
    }

}