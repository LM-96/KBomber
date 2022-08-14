package kbomberx.io

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlin.test.assertTrue
import kotlin.test.fail

private const val JOB_END = 0
private const val TIMER_END = 1

const val TOLERANCE_MILLIS = 5000L

suspend fun failsAfterMillis(millis : Long, scope : CoroutineScope,
                             block : suspend () -> Unit) {
    val channel = Channel<Int>()

    val job = scope.launch {
        block()
        channel.send(JOB_END)
    }

    val timer = scope.launch {
        delay(millis)
        channel.send(TIMER_END)
    }

    when(channel.receive()) {
        JOB_END -> timer.cancelAndJoin()
        TIMER_END -> {
            job.cancel()
            fail("timer end without job completion")
        }
    }
    channel.close()
}

suspend fun assertTrueWithAttempts(attempts : Int, millisBeetwenAttempts : Long, block : suspend () -> Boolean) {
    for(i in 1..attempts) {
        if(block()){
            assertTrue(true)
            return
        }
        delay(millisBeetwenAttempts)
    }
    fail("attempts end")
}

suspend fun assertSuspendThrows(message : String = "expected exception is not thrown",
                                block : suspend () -> Unit) {
    try {
        block()
        fail("")
    } catch (_ : Exception){}
}

fun assertThrows(message : String = "expected exception is not thrown", block : () -> Unit) {
    try {
        block()
        fail("")
    } catch (_ : Exception){}
}

class FlowChangeListener<T>(
    private val flow : Flow<T>,
    private val scope : CoroutineScope = IoScope
) {

    private var internalChannel : Channel<T>? = null
    var channel : ReceiveChannel<T>? = null

    private var job : Job? = null

    fun start() {
        internalChannel = Channel()
        channel = internalChannel
        job = scope.launch {
            flow.collect {
                internalChannel?.send(it)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        internalChannel = null
        channel = null
    }
}