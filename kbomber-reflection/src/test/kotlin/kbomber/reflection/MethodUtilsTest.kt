package kbomber.reflection

import kbomber.reflection.method.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.coroutines.coroutineContext
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure
import kotlin.test.*

class MethodUtilsTest {

    companion object {
        private const val COROUTINE_TEST_NAME = "coroutine-test-name"
    }

    class TestClass {
        fun eyeStr(str : String) : String {
            return str
        }

        fun eyeStrInt(str : String, int : Int) : Pair<String, Int> {
            return str to int
        }

        suspend fun getCurrentCtxName() : String? {
            return coroutineContext[CoroutineName.Key]?.name
        }

        suspend fun sEyeStrInt(str : String, int : Int) : Pair<String, Int> {
            return str to int
        }

        fun nothing() {}

        suspend fun sNothing() {}
    }

    val testInstance = TestClass()
    val eyeStrMethod = TestClass::class.java.methods.find { it.name == "eyeStr" }!!
    val eyeStrIntMethod = TestClass::class.java.methods.find { it.name == "eyeStrInt" }!!
    val getCurrentCtxNameMethod = TestClass::class.java.methods.find { it.name == "getCurrentCtxName" }!!
    val sEyeStrIntMethod = TestClass::class.java.methods.find { it.name == "sEyeStrInt" }!!
    val nothingMethod = TestClass::class.java.methods.find { it.name == "nothing" }!!
    val sNothingMethod = TestClass::class.java.methods.find { it.name == "sNothing" }!!

    @Test fun testCountParameters() {
        assertEquals(1, eyeStrMethod.countParams())
        assertEquals(2, eyeStrIntMethod.countParams())
        assertEquals(0, getCurrentCtxNameMethod.countParams())
        assertEquals(2, sEyeStrIntMethod.countParams())
        assertEquals(0, nothingMethod.countParams())
        assertEquals(0, sNothingMethod.countParams())
    }

    @Test fun testHasParameter() {
        assertTrue(eyeStrMethod.hasParameters())
        assertTrue(eyeStrIntMethod.hasParameters())
        assertFalse(getCurrentCtxNameMethod.hasParameters())
        assertTrue(sEyeStrIntMethod.hasParameters())
        assertFalse(nothingMethod.hasParameters())
        assertFalse(sNothingMethod.hasParameters())
    }

    @Test fun testGetKParameter() {
        val eyeStrParams = eyeStrMethod.getKotlinParameters()
        assertNotNull(eyeStrParams)
        eyeStrParams as List<KParameter>
        assertEquals(1, eyeStrParams.size)
        assertEquals(String::class, eyeStrParams[0].type.jvmErasure)

        val eyeStrIntParams = eyeStrIntMethod.getKotlinParameters()
        assertNotNull(eyeStrIntParams)
        eyeStrIntParams as List<KParameter>
        assertEquals(2, eyeStrIntParams.size)
        assertEquals(String::class, eyeStrIntParams[0].type.jvmErasure)
        assertEquals(Int::class, eyeStrIntParams[1].type.jvmErasure)

        val getCurrentContextNameParams = getCurrentCtxNameMethod.getKotlinParameters()
        assertNotNull(getCurrentContextNameParams)
        getCurrentContextNameParams as List<KParameter>
        assertEquals(0, getCurrentContextNameParams.size)

        val sEyeStrIntParams = eyeStrIntMethod.getKotlinParameters()
        assertNotNull(sEyeStrIntParams)
        sEyeStrIntParams as List<KParameter>
        assertEquals(2, sEyeStrIntParams.size)
        assertEquals(String::class, sEyeStrIntParams[0].type.jvmErasure)
        assertEquals(Int::class, sEyeStrIntParams[1].type.jvmErasure)

        val nothingParams = nothingMethod.getKotlinParameters()
        assertNotNull(nothingParams)
        nothingParams as List<KParameter>
        assertEquals(0, nothingParams.size)

        val sNothingParams = sNothingMethod.getKotlinParameters()
        assertNotNull(sNothingParams)
        sNothingParams as List<KParameter>
        assertEquals(0, sNothingParams.size)
    }

    @Test fun testCheckParmeterOfType() {
        assertTrue(eyeStrMethod.checkParameterOfType(0, String::class))
        assertFalse(eyeStrMethod.checkParameterOfType(0, Int::class))

        assertTrue(eyeStrIntMethod.checkParameterOfType(0, String::class))
        assertFalse(eyeStrIntMethod.checkParameterOfType(0, Int::class))
        assertTrue(eyeStrIntMethod.checkParameterOfType(1, Int::class))
        assertFalse(eyeStrIntMethod.checkParameterOfType(1, String::class))

        assertThrows { getCurrentCtxNameMethod.checkParameterOfType(0, String::class) }

        assertTrue(sEyeStrIntMethod.checkParameterOfType(0, String::class))
        assertFalse(sEyeStrIntMethod.checkParameterOfType(0, Int::class))
        assertTrue(sEyeStrIntMethod.checkParameterOfType(1, Int::class))
        assertFalse(sEyeStrIntMethod.checkParameterOfType(1, String::class))

        assertThrows { nothingMethod.checkParameterOfType(0, String::class) }
        assertThrows { sNothingMethod.checkParameterOfType(0, String::class) }
    }

    @Test fun testIsSuspendFunction() {
        assertFalse(eyeStrMethod.isSuspendFunction())
        assertFalse(eyeStrIntMethod.isSuspendFunction())
        assertTrue(getCurrentCtxNameMethod.isSuspendFunction())
        assertTrue(sEyeStrIntMethod.isSuspendFunction())
        assertFalse(nothingMethod.isSuspendFunction())
        assertTrue(sNothingMethod.isSuspendFunction())
    }

    @Test fun testInvokeSuspend() = runBlocking(CoroutineName(COROUTINE_TEST_NAME)) {
        val str = "str"
        val int = 1

        assertSuspendThrows { eyeStrMethod.invokeSuspend(testInstance, str) }
        assertSuspendThrows { eyeStrIntMethod.invokeSuspend(testInstance, str, int) }

        var res = getCurrentCtxNameMethod.invokeSuspend(testInstance)
        assertTrue(res is String)
        assertEquals(COROUTINE_TEST_NAME, res)

        res = sEyeStrIntMethod.invokeSuspend(testInstance, str, int)
        assertTrue(res is Pair<*, *>)
        assertTrue(res.first is String)
        assertTrue(res.second is Int)
        assertEquals(str, res.first)
        assertEquals(int, res.second)

        assertSuspendThrows { nothingMethod.invokeSuspend(testInstance) }

        res = sNothingMethod.invokeSuspend(testInstance)
        assertTrue(res is Unit)
    }

    @Test fun testInvokeProperly() = runBlocking(CoroutineName(COROUTINE_TEST_NAME)) {
        val str = "str"
        val int = 1

        var res = eyeStrMethod.invokeProperly(testInstance, str)
        assertTrue(res is String)
        assertEquals(str, res)

        res = eyeStrIntMethod.invokeProperly(testInstance, str, int)
        assertTrue(res is Pair<*,*>)
        assertTrue(res.first is String); assertTrue(res.second is Int)
        assertEquals(str, res.first); assertEquals(int, res.second)

        res = getCurrentCtxNameMethod.invokeProperly(testInstance)
        assertTrue(res is String)
        assertEquals(COROUTINE_TEST_NAME, res)

        res = sEyeStrIntMethod.invokeProperly(testInstance, str, int)
        assertTrue(res is Pair<*, *>)
        assertTrue(res.first is String)
        assertTrue(res.second is Int)
        assertEquals(str, res.first)
        assertEquals(int, res.second)

        res = nothingMethod.invokeProperly(testInstance)
        assertNull(res)

        res = sNothingMethod.invokeProperly(testInstance)
        assertTrue(res is Unit)
    }

    @Test fun testCheckSignature() {
        assertTrue(eyeStrMethod.checkSignature("eyeStr", String::class.java, String::class.java))
        assertTrue(eyeStrIntMethod.checkSignature("eyeStrInt", Pair::class.java, String::class.java, Int::class.java))
        assertTrue(sEyeStrIntMethod.checkSignature("sEyeStrInt", Pair::class.java, String::class.java, Int::class.java))
        assertTrue(getCurrentCtxNameMethod.checkSignature("getCurrentCtxName", String::class.java))
        assertTrue(nothingMethod.checkSignature("nothing", Unit::class.java))
        assertTrue(sNothingMethod.checkSignature("sNothing", Unit::class.java))
    }


}