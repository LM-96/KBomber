package kbomber.reflection.`class`

/**
 * Returns `true` if the given `clazz` has the superclass `superclazz` in its
 * hierarchy. Suppose to have this code:
 * ```
 * class A
 * class B : A()
 * class C : B()
 * ```
 * then, `hasSuperclass(C::class.java, A::class.java)` and
 * `hasSuperclass(B::class.java, A::class.java)` both return `true` while
 * `hasSuperclass(A::class.java, C::class.java)` or `hasSuperclass(A::class.java, B::class.java)`
 * both return `false`
 * @param clazz the class that should have the required [superclazz] in its hierarchy
 * @param superclazz the superclass
 * @return `true` if the [superclazz] is in the superclass hierarchy of [clazz], `false` otherwise
 */
fun hasSuperclass(clazz : Class<*>, superclazz : Class<*>) : Boolean {
    var sc = clazz.superclass
    while (sc != null) {
        if(sc == superclazz ) return true
        sc = sc.superclass
    }

    return false
}

/**
 * Returns `true` if the given interface has the [superinterfac3] in its superinterface
 * hierarchy. This method is the same as [hasSuperclass] but with interfaces.
 * @param interfac3 the interface that should have the required [superinterfac3] in its hierarchy
 * @param superinterfac3 the superinterface
 * @throws IllegalArgumentException if the passed classes are not interfaces
 * @return `true` if the [superinterfac3] is in the superinterface hierarchy of [interfac3],
 * `false` otherwise
 */
fun hasSuperInterface(interfac3: Class<*>, superinterfac3 : Class<*>) : Boolean {
    if(!interfac3.isInterface)
        throw IllegalArgumentException("$interfac3 is not an interface")
    if(!superinterfac3.isInterface)
        throw IllegalArgumentException("$superinterfac3 is not an interface")

    val si = interfac3.interfaces
    for(i in si) {
        if(i == superinterfac3 || hasSuperInterface(i, superinterfac3))
            return true
    }

    return false
}

/**
 * Returns `true` if the given class implements the given interface.
 * Notice that this method scan also all the hierarchy of the given class
 * returning `true` if one superclass implements the interface
 * @throws IllegalArgumentException if the passed [clazz] parameter is an interface
 * or if the [interfac3] is not an interface
 * @param clazz the class to be checked
 * @param interfac3 the interface
 * @return `true` if the class or one of its superclasses implements the interface,
 * `false` otherwise
 */
fun implements(clazz : Class<*>, interfac3 : Class<*>) : Boolean {
    if(clazz.isInterface)
        throw IllegalArgumentException("$clazz is not a class")

    if(!interfac3.isInterface)
        throw IllegalArgumentException("$interfac3 is not an interface")

    var c : Class<*>? = clazz
    while(c != null) {
        for(i in c.interfaces) {
            println("$clazz : $i")
            if(i == interfac3 || hasSuperInterface(i, interfac3))
                return true
        }
        c = c.superclass
    }

    return false
}