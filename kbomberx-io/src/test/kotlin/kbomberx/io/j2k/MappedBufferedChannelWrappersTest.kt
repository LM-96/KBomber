package kbomberx.io.j2k

import kbomberx.io.TOLERANCE_MILLIS
import kbomberx.io.assertThrows
import kbomberx.io.assertTrueWithAttempts
import kbomberx.io.failsAfterMillis
import kotlinx.coroutines.*
import java.io.PipedInputStream
import java.io.PipedOutputStream
import kotlin.test.Test
import kotlin.test.assertEquals

class MappedBufferedChannelWrappersTest {

    @Test fun testMappedReaderWrapper() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)
        val writer = pipedOut.bufferedWriter()

        val wrappedChannel = pipedIn.mappedReceiveChannel {
            it.toInt()
        }
        for(i in 1..5) {
            writer.write("$i\n")
            writer.flush()
            failsAfterMillis(TOLERANCE_MILLIS , this) { assertEquals(i, wrappedChannel.receive()) }
        }
        pipedIn.close()
        pipedOut.close()
    }

    @Test fun testMappedWriterWrapper() = runBlocking(Dispatchers.IO) {
        val pipedIn = PipedInputStream()
        val pipedOut = PipedOutputStream(pipedIn)
        val reader = pipedIn.bufferedReader()

        val wrappedChannel = pipedOut.mappedStringSendChannel<Int>{ it.toString() }
        for(i in 1..5) {
            wrappedChannel.send(i)
            failsAfterMillis(TOLERANCE_MILLIS, this) { withContext(Dispatchers.IO) {
                assertEquals(i.toString(), reader.readLine())
            } }
        }
        pipedIn.close()
        pipedOut.close()
    }

}