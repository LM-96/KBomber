package kbomber.reflection

import kotlin.test.fail

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