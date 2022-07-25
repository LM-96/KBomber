package kbomber.reflection.`class`

import java.lang.reflect.Field

/**
 * A set of [Field] that contains all the delegates in this class
 */
val Class<*>.delegates : Set<Field>
    get() {
        return declaredFields.filter { it.name.contains("\$\$delegate_") }.toSet()
    }

/**
 * Returns a set of [Field] that contains all the delegates in this class with
 * the given type
 * @param type the desired type
 * @return a set of [Field] that contains all the delegates in this class with
 * the given type
 */
fun Class<*>.getDelegateFieldsWithType(type : Class<*>) : Set<Field> {
    return declaredFields.filter { it.name.contains("\$\$delegate_") &&
            it.type == type }.toSet()
}

/**
 * Returns a set that contains all the delegate with the specified type owned by the given
 * instance. The delegates are cast to the passed type (cast is safe thanks to a check)
 * @param instance the instance that owns the delegates
 * @param type the type of the delegates
 * @return the set with the found cast delegates
 */
fun <T, R> Class<T>.getDelegateObjectsWithType(instance : T, type : Class<R>) : Set<R> {
    return declaredFields.filter { it.name.contains("\$\$delegate_") &&
            it.type == type }.map { it.get(instance) as R }.toSet()
}