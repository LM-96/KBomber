package kbomber.collections.values

/* SINGLE INIT ************************************************************************** */

/**
 * Create a new [LateSingleInit] object that is not initialized
 * @return a new and not initialized [LateSingleInit]
 */
fun <T> lateSingleInit() : LateSingleInit<T> {
    return LateSingleInit()
}

/**
 * Creates and initializes a new [LateSingleInit] object
 * @return the initialized [LateSingleInit] instance
 */
fun <T> lateSingleInit(obj : T) : LateSingleInit<T> {
    val res = lateSingleInit<T>()
    res.set(obj)
    return res
}

/* LOADABLE VALUES ********************************************************************** */
/**
 * Performs the *load* action passed as parameter and store the result
 * into a [LoadableValue], returning it
 * @param loader the action that loads the value
 * @return the result of the loader encapsulated into a [LoadableValue] object
 */
fun <T> load(loader : () -> T) : LoadableValue<T> {
    return MutableLoadableValue<T>().load(loader)
}

/**
 * Performs the *load* action passed as parameter and store the result
 * into a [MutableLoadableValue], returning it
 * @param loader the action that loads the value
 * @return the result of the loader encapsulated into a [LoadableValue] object
 */
fun <T> mutableLoad(loader: () -> T) : MutableLoadableValue<T> {
    return MutableLoadableValue<T>().load(loader)
}

/**
 * Encapsulates a value into a [LoadableValue] simulating a successful load
 * @param value the value
 * @return a [LoadableValue] with the `value` loaded
 */
fun <T> asLoaded(value : T) : LoadableValue<T> {
    return MutableLoadableValue<T>().load { value }
}

/**
 * Encapsulates a value into a [MutableLoadableValue] simulating a successful load
 * @param value the value
 * @return a [MutableLoadableValue] with the `value` loaded
 */
fun <T> asMutableLoaded(value : T) : MutableLoadableValue<T> {
    return MutableLoadableValue<T>().load { value }
}

/**
 * Creates and returns an empty [MutableLoadableValue]
 * @return an empty [MutableLoadableValue]
 */
fun <T> mutableLoadableValue() : MutableLoadableValue<T> {
    return MutableLoadableValue()
}

/**
 * Creates a copy of this [LoadableValue] that is also mutable.
 * Notice that all changes of the old value are not propagated to this
 * new copy and *vice-versa*
 * @return a [MutableLoadableValue] copy of this object
 */
fun <T> LoadableValue<T>.mutableCopy() : MutableLoadableValue<T> {
    return MutableLoadableValue(value, status, error)
}