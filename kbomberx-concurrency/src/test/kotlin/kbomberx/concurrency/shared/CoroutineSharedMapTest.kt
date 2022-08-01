package kbomberx.concurrency.shared

import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoroutineSharedMapTest {

    val sharedMap = newSharedMap<String, String>()

    @Before fun clear() = runBlocking {
        sharedMap.clear()
    }

    @Test fun putAndGetTest() = runBlocking{
        val entry = "key1" to "value1"
        sharedMap.put(entry.first, entry.second)
        val res = sharedMap.get(entry.first)
        assertTrue(res.isPresent)
        assertEquals(entry.second, res.get())
        assertEquals(entry.second, sharedMap.forcedGet(entry.first))
    }

    @Test fun putAllAndSizeTest() = runBlocking {
        assertEquals(0, sharedMap.size())
        val entries = mutableMapOf<String, String>()
        for(i in 1..10) {
            entries["key$i"] = "value$i"
        }
        sharedMap.putAll(entries)

        assertEquals(entries.size, sharedMap.size())
        for(key in entries.keys) {
            assertEquals(entries[key], sharedMap.forcedGet(key))
        }
    }

    @Test fun containsKeyTest() = runBlocking {
        val entry = "key1" to "value1"
        sharedMap.put(entry.first, entry.second)

        assertTrue(sharedMap.containsKey(entry.first))
    }

}