package kbomber.reflection

import kbomber.reflection.`class`.hasSuperInterface
import kbomber.reflection.`class`.implements
import kbomber.reflection.`class`.hasSuperclass
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassUtilsTest {

    interface IA
    interface IB : IA
    interface IC : IB

    open class A : IA
    open class B : A(), IB
    class C : B(), IC

    private val intA = IA::class.java
    private val intB = IB::class.java
    private val intC = IC::class.java

    private val classA = A::class.java
    private val classB = B::class.java
    private val classC = C::class.java


    @Test fun testHasSuperclass() {
        assertTrue(hasSuperclass(classC, classA))
        assertTrue(hasSuperclass(classC, classB))
        assertTrue(hasSuperclass(classB, classA))

        assertFalse(hasSuperclass(classB, classC))
        assertFalse(hasSuperclass(classA, classB))
        assertFalse(hasSuperclass(classA, classC))

        assertFalse(hasSuperclass(classA, classA))
        assertFalse(hasSuperclass(classB, classB))
        assertFalse(hasSuperclass(classC, classC))
    }

    @Test fun testHasSuperinterface() {
        assertTrue(hasSuperInterface(intC, intB))
        assertTrue(hasSuperInterface(intC, intA))
        assertTrue(hasSuperInterface(intB, intA))

        assertFalse(hasSuperInterface(intB, intC))
        assertFalse(hasSuperInterface(intA, intB))
        assertFalse(hasSuperInterface(intA, intC))

        assertThrows { hasSuperInterface(classC, intC) }
        assertThrows { hasSuperInterface(intC, classC) }
    }

    @Test fun testImplements() {
        assertTrue(implements(classC, intC))
        assertTrue(implements(classC, intB))
        assertTrue(implements(classC, intA))

        assertTrue(implements(classB, intB))
        assertTrue(implements(classB, intA))

        assertTrue(implements(classA, intA))

        assertFalse(implements(classB, intC))
        assertFalse(implements(classA, intB))
        assertFalse(implements(classA, intC))
    }

}