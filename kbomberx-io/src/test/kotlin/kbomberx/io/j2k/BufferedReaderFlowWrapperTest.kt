package kbomberx.io.j2k

import kbomberx.io.FlowChangeListener
import kbomberx.io.TOLERANCE_MILLIS
import kbomberx.io.failsAfterMillis
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class BufferedReaderFlowWrapperTest {

    @Test fun testBufferedReaderFlowWrapper() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)
        val flow = pipedIn.stringSharedFlow()
        val flowChangeListener = FlowChangeListener(flow, this)
        flowChangeListener.start()

        val writer = pipedOut.bufferedWriter()
        var str : String

        for(i in 1..3) {
            str = "test_$i"
            writer.write("$str\n")
            writer.flush()
            failsAfterMillis(TOLERANCE_MILLIS, this) { assertEquals(str, flowChangeListener.channel!!.receive()) }
        }

        flowChangeListener.stop()
        pipedIn.close()
        pipedOut.close()
    }

}