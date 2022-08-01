package kbomberx.concurrency.shared

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Before
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CoroutineSharedListTest {

    private val coroutineSharedList = CoroutineSharedArrayList<String>()

    @Before fun clear() {
        runBlocking {
            coroutineSharedList.clear()
        }
    }

    @Test fun testAddAndGet() = runBlocking {
        val str = "str"
        coroutineSharedList.add(0, str)
        val res = coroutineSharedList.get(0)
        assertEquals(str, res)
    }

    @Test fun testContains() = runBlocking {
        val str = "str"
        coroutineSharedList.add(str)
        assertTrue(coroutineSharedList.contains(str))
    }

    @Test fun testRemoveAt() = runBlocking {
        val str = "str"
        coroutineSharedList.add(0, str)
        val removed = coroutineSharedList.removeAt(0)
        assertEquals(str, removed)
        assertFalse(coroutineSharedList.contains(str))
    }

    @Test fun testContainsAndRemove() = runBlocking {
        val str = "str"
        coroutineSharedList.add(str)
        assertTrue(coroutineSharedList.contains(str))
        coroutineSharedList.remove(str)
        assertFalse(coroutineSharedList.contains(str))
    }

    @Test fun testSize() = runBlocking {
        val strs = arrayListOf("str1", "str2", "str3")
        for(str in strs) {
            coroutineSharedList.add(str)
        }
        assertEquals(strs.size, coroutineSharedList.size())
    }

    @Test fun testAsyncAdd() = runBlocking {
        val str = "str"
        coroutineSharedList.asyncAdd(0, str)
        delay(200)
        assertEquals(str, coroutineSharedList.get(0))
    }

}