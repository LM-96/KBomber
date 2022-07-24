package kbomber.collections.parameters

/**
 * This class represents the *mutable* version of the [ReadableParameterMap], so it is possible
 * to add or remove parameters. For security reasons, the internal map is not accessible or modificable
 */
class MutableParameterMap(initParams : Map<String, Any>? = null) : ReadableParameterMap() {

    override val params = mutableMapOf<String, Any>()

    init {
        if(initParams != null)
            params.putAll(initParams)
    }

    /**
     * Associate the specified `obj` to the given `name`. Notice that if a parameter is already
     * associated to the name, it will be overridden
     * @param name the name to associate to the parameter
     * @param obj the object that represents the parameter
     */
    operator fun set(name : String, obj : Any) {
        params[name] = obj
    }

    /**
     * Associate the specified `obj` to the given `name`. Notice that if a parameter is already
     * associated to the name, it will be overridden
     * @param name the name to associate to the parameter
     * @param obj the object that represents the parameter
     * @return this map
     */
    fun addParam(name : String, value : Any) : MutableParameterMap {
        params[name] = value
        return this
    }

    /**
     * Adds the passed list of association *name-parameter* to this map.
     * Notice that if an association is already present, then it will be overridden
     * @param name the name to associate to the parameter
     * @param obj the object that represents the parameter
     * @return this map
     */
    fun addParams(vararg params : Pair<String, Any>) : MutableParameterMap {
        for(p in params)
            this[p.first] = p.second

        return this
    }

    /**
     * Removes the parameter associated with the given `name`, returning it if present.
     * If not present then the map is not modified and `null` is returned
     * @param name the name associated to element to be removed
     * @return the parameter associated with the name or `null` if not present
     */
    fun removeParam(name : String) : Any? {
        return params.remove(name)
    }

    /**
     * Returns this map as an [ImmutableParameterMap]
     */
    fun asImmutable() : ImmutableParameterMap {
        return ImmutableParameterMap(params.toMap())
    }

    /**
     * Deletes all entries already present into this map. After this invokation, the
     * map will be totally *cleared*
     */
    fun clear() {
        params.clear()
    }

}