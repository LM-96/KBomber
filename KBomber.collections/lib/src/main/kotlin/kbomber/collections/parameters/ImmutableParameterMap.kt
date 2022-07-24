package kbomber.collections.parameters

/**
 * This class is a complete implementation of the [ReadableParameterMap] and represents
 * a container of *name-parameter* entries that cannot be modified
 * @see [MutableParameterMap]
 */
class ImmutableParameterMap(
    override val params: Map<String, Any>
) : ReadableParameterMap() {

    /**
     * Returns a mutable copy of this map.
     * Notice that all changes performed over the new [MutableParameterMap] *are not propagated* into
     * the original map
     * @return a copy of this parameter map as a [MutableParameterMap] instance
     */
    fun mutableCopy() : MutableParameterMap {
        return MutableParameterMap(params)
    }

}