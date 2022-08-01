package kbomber.collections.parameters

/**
 * Returns a new cleared [MutableParameterMap] ready to be used
 * @return a new cleared [MutableParameterMap] ready to be used
 */
fun mutableParameterMap() : MutableParameterMap {
    return MutableParameterMap()
}

/**
 * Returns a new [MutableParameterMap] that contains all the parameter that are already
 * present in the given [initialParams] argument. Notice that this method **copies** all the
 * elements that are in the given map to an internal map. So, all the modification done to the
 * new map are *not* propagated into the original and *vice-versa*
 * @param initialParams the initial parameters that has to be copied into the new map
 * @return a new [MutableMap] that contains all the entries of the passed map
 */
fun mutableParameterMap(initialParams : Map<String, Any>) : MutableParameterMap {
    return MutableParameterMap(initialParams)
}

/**
 * Returns a new [ImmutableParameterMap] starting from the given map.
 * Notice that the original map is directly used internally to the new map,
 * so changes over the [params] map **are propagated** into the new
 * @param params the map that has to be transformed into an [ImmutableParameterMap]
 * @return the new [ImmutableParameterMap]
 */
fun immutableParameterMap(params : Map<String, Any>) : ImmutableParameterMap {
    return ImmutableParameterMap(params)
}

/**
 * Returns a new cleared [ImmutableParameterMap] that cannot be modified
 * @return an empty [ImmutableParameterMap]
 */
fun immutableParameterMap() : ImmutableParameterMap {
    return ImmutableParameterMap(mapOf())
}

/**
 * Returns a new [ImmutableParameterMap] that contains all the passed [params] entries
 * @param params a list of association *name-parameter*
 * @return the new [ImmutableParameterMap] with all the entries
 */
fun immutableParameterMapOf(vararg params : Pair<String, Any>) : ImmutableParameterMap {
    return immutableParameterMap(params.toMap())
}

/**
 * Returns a new [MutableParameterMap] that contains all the passed [params] entries
 * @param params a list of association *name-parameter*
 * @return the new [MutableParameterMap] with all the entries
 */
fun mutableParameterMapOf(vararg params : Pair<String, Any>) : MutableParameterMap {
    return mutableParameterMap(params.toMap())
}

/**
 * Create a new [MutableParameterMap] starting from this map. The new map internally contains a **copy**
 * of the original map, so, all the modification done to the
 * new map are *not* propagated into the original and *vice-versa*
 * @return the new [MutableParameterMap] that contains all the entries already present on this map
 */
fun Map<String, Any>.toParameterMap() : MutableParameterMap {
    return MutableParameterMap(this)
}

/**
 * Create a new [ImmutableParameterMap] starting from this map. The new map internally contains
 * the original map, so, all the modification done to it are propagated into the new map
 * @return the new [ImmutableParameterMap] that incapsulate this map
 */
fun Map<String, Any>.toImmutableParameterMap() : ImmutableParameterMap {
    return ImmutableParameterMap(this.toMap())
}

/**
 * Returns an association *name-parameter* as a [Pair] with a [String] and an [Any] object
 * @return the [Pair] that represents the assiciation *name-parameter*
 */
infix fun String.asNameOf(obj : Any) : Pair<String, Any> {
    return Pair(this, obj)
}