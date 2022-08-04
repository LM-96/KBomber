package kbomberx.io.j2k

import kbomberx.io.assertThrows
import kbomberx.io.assertTrueWithAttempts
import kbomberx.io.failsAfterMillis
import kotlinx.coroutines.*
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TOLERANCE_MILLIS = 5000L

class BufferedChannelWrappersTest {

    @Test fun testReaderWrapper() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)
        val writer = pipedOut.bufferedWriter()

        val wrappedChannel = pipedIn.stringReceiveChannel()
        var str : String
        for(i in 1..5) {
            str = "test_$i"
            writer.write("$str\n")
            writer.flush()
            failsAfterMillis(TOLERANCE_MILLIS , this) { assertEquals(str, wrappedChannel.receive()) }
        }
        pipedIn.close()
        pipedOut.close()
    }

    @Test fun testWriterWrapper() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)
        val reader = pipedIn.bufferedReader()

        val wrappedChannel = pipedOut.stringSendChannel()
        var str : String
        for(i in 1..5) {
            str = "test_$i"
            wrappedChannel.send(str)
            failsAfterMillis(TOLERANCE_MILLIS, this) { withContext(Dispatchers.IO) {
                assertEquals(str, reader.readLine())
            } }
        }
        pipedIn.close()
        pipedOut.close()
    }

    @Test fun testReaderWriterWrapper() = runBlocking {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)

        val inChan = pipedIn.stringReceiveChannel()
        val outChan = pipedOut.stringSendChannel()
        var str : String
        for(i in 1..3) {
            str = "test_$i"
            outChan.send(str)
            failsAfterMillis(TOLERANCE_MILLIS, this) { assertEquals(str, inChan.receive()) }
        }

        pipedIn.close()
        pipedOut.close()
    }

    @Test fun testWrappersClosure1() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)

        val inChan = pipedIn.stringReceiveChannel()
        val outChan = pipedOut.stringSendChannel()

        pipedOut.close()
        pipedIn.close()
        assertTrueWithAttempts(5, TOLERANCE_MILLIS) { inChan.isClosedForReceive  }

    }

    @Test fun testWrappersClosure2() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)

        val inChan = BufferedReaderChannelWrapper(pipedIn)
        val outChan = BufferedWriterChannelWrapper(pipedOut)

        inChan.close()
        outChan.close()

        assertThrows { pipedIn.read() }
        assertThrows { pipedOut.write(1) }

    }

}