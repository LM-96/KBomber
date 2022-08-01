package kbomber.collections.parameters

/**
 * A class that represents a container of parameter, each associated with a unique name that
 * is the *key* of the related object.
 * This map is **readable** in the sense that no modification are possible over this container.
 */
abstract class ReadableParameterMap {

    protected abstract val params : Map<String, Any>

    /**
     * Returns the parameter associated with the given name
     * @param name the name of the element
     * @return the parameter associated with the given name or null
     * if no element with this name is foun
     */
    operator fun get(name : String) : Any? {
        return params[name]
    }

    /**
     * Checks if the element associated with the given name is of the type passed
     * as parameter
     * @exception NoSuchElementException if no element with the given name is present
     * @param name the name of the parameter
     * @param clazz the type to be checked
     * @return `true` if the element is of the passed type, `false` otherwise
     */
    fun checkTypeOf(name : String, clazz : Class<*>) : Boolean {
        if(!params.containsKey(name))
            throw NoSuchElementException("no parameter with name $name")

        if(clazz.isInstance(params[name]!!))
            return true


        return false
    }

    /**
     * Checks if the element associated with the given name is **not** of the type passed
     * as parameter
     * @exception NoSuchElementException if no element with the given name is present
     * @param name the name of the parameter
     * @param clazz the type to be checked
     * @return `true` if the element is not of the passed type, `false` otherwise
     */
    fun checkNotTypeOf(name : String, clazz : Class<*>) : Boolean {
        if(!params.containsKey(name))
            throw NoSuchElementException("no parameter with name $name")
        if(!clazz.isInstance(params[name]!!))
            return true

        return false
    }

    /**
     * Checks if the parameter associated with the given name is of the given type and if true
     * then call the passed `then` block
     *
     * @exception NoSuchElementException if no element with the given name is present
     * @param name the name of the parameter
     * @param clazz the type to be checked
     * @param then the function to be called
     * @return this map
     */
    fun ifIsTypeOf(name : String, clazz : Class<*>, then : (Any) -> Unit) : ReadableParameterMap {
        if(!params.containsKey(name))
            throw NoSuchElementException("no parameter with name $name")

        val param = params[name]!!
        if (clazz.isInstance(param))
            then.invoke(param)

        return this
    }

    /**
     * Checks if the parameter associated with the given name is **not** of the given type and if true
     * then call the passed `then` block
     *
     * @exception NoSuchElementException if no element with the given name is present
     * @param name the name of the parameter
     * @param clazz the type to be checked
     * @param then the function to be called
     * @return this map
     */
    fun ifIsNotTypeOf(name : String, clazz : Class<*>, then : (Any) -> Unit) : ReadableParameterMap {
        if(!params.containsKey(name))
            throw NoSuchElementException("no parameter with name $name")

        val param = params[name]!!
        if (!clazz.isInstance(param))
            then.invoke(param)

        return this
    }

    /**
     * Calls the specified `then` block only if the map contains a parameter associated with the
     * given `name` and if it is an instance of the [clazz] class. If not, so nothing is done
     * @param name the name of the parameter
     * @param clazz the type to be checked
     * @param then the function to be called
     * @return this map
     */
    fun ifIsPresentButNotTypeOf(name : String, clazz : Class<*>, then : (Any) -> Unit) : ReadableParameterMap {
        if(params.containsKey(name)) {
            val param = params[name]!!
            if (!clazz.isInstance(param))
                then.invoke(param)
        }

        return this
    }

    /**
     * Calls the passed `then` block only if it is **not** present a parameter associated with the
     * given `name`, passing it to the function
     * @param name the name of the parameter
     * @param then the function to be invoked
     * @return this map
     */
    fun ifNotPresent(name : String, then : () -> Unit) : ReadableParameterMap {
        if(!params.containsKey(name))
            then.invoke()

        return this
    }

    /**
     * Retrieves (but not removes) the element associated with the given `name`.
     * This method is different from [getParam] because
     * always returns a **not** `null` element and throws an exception if no parameter is associated with `name`
     * @exception NoSuchElementException if no element is associated with the given name
     * @param name the name of the searched parameter
     */
    @Throws(NoSuchElementException::class)
    fun peek(name : String) : Any {
        if(!params.containsKey(name))
            throw NoSuchElementException("No parameter with name \'$name\'")

        return params[name]!!
    }

    /**
     * Retrieves (but not removes) the element associated with the given `name`, casting it to the desired type
     * @exception NoSuchElementException if no element is associated with the given name
     * @exception ClassCastException if the cast of the parameter to the desired type fails
     * @param T the desired type of the parameter
     * @param name the name of the searched parameter
     */
    @Throws(NoSuchElementException::class, ClassCastException::class)
    fun <T> peekAs(name : String) : T {
        if(!params.containsKey(name))
            throw NoSuchElementException("No parameter with name \'$name\'")

        try {
            return params[name] as T
        } catch (e : Exception) {
            throw ClassCastException("Unable to get the parameter with name \'$name\' as desired type")
        }
    }

    /**
     * Returns the parameter associated with the given `name` or `null` if no parameter
     * is found.
     * @param name the name associated with the parameter
     * @return  the parameter associated with the given `name` or `null` if no parameter
     * is found
     */
    fun getParam(name : String) : Any? {
        return params[name]
    }

    /**
     * Returns the parameter associated with the given `name` or, if no element is associated with it,
     * the `elseObj`. This method does not consider the type of the elements
     * @param name the name associated with the searched parameter
     * @param elseObj the object to return in case of no element is associated with the given `name`
     * @return the parameter associated with the given `name` or the `elseObj` if not present
     */
    fun getOrElse(name : String, elseObj : Any) : Any {
        return if(params.containsKey(name))
            params[name]!!
        else
            elseObj
    }

    /**
     * Tries to return the element associated with the given name, casting it to the desired `T` type.
     * If no element is associated with the passed `name` or if the cast fails, then it returns the
     * given `elseObj` object
     * @param T the desired type of the searched element
     * @param name the name of the searched parameter
     * @param elseObj the object to return in case of no element is associated with the given `name` or
     * the cast fails
     * @return the parameter associated with the key, cast to the desired type or the `elseObj` if no
     * element with the given `name` is found or the cast fails
     */
    inline fun <reified T> tryCastOrElse(name : String, elseObj : T) : T {
        if(this.hasParam(name)) {
            val param = this[name]
            if(param is T)
                return param
        }

        return elseObj
    }

    /**
     * Tries to return the element associated with the given name, casting it to the desired `T` type.
     * If no element is associated with the passed `name` then it returns the
     * given `elseObj` object. This method is different from [tryCastOrElse] because it throws an exception
     * if the cast fails
     * @throws [ClassCastException] if the cast fails
     * @param T the desired type of the searched element
     * @param name the name of the searched parameter
     * @param elseObj the object to return in case of no element is associated with the given `name`
     * @return the parameter associated with the given `name` cast as `T` object
     * or the `elseObj` if not present
     */
    fun <T> castOrElse(name : String, elseObj : T) : T {
        if(params.containsKey(name))
            return params[name] as T

        return elseObj
    }

    /**
     * Returns `true` if this map contains a parameter associated with the given `name`
     * @param name the name to be checked
     * @return `true` if this map contains a parameter associated with the given `name`,
     * `false` otherwise
     */
    fun hasParam(name : String) : Boolean {
        return params.containsKey(name)
    }

    /**
     * Returns `true` if this contains **all** the parameter associated with the passed list of names
     * @param names the list of the names to be checked
     * @return `true` if this map contains **all** the parameters associated with the given list of names,
     * `false` otherwise
     */
    fun hasParams(vararg names : String) : Boolean {
        for(name in names)
            if(!params.containsKey(name))
                return false
        return true
    }

    /**
     * Returns this [ReadableParameterMap] as a normal read-only [Map]. Notice that the returned
     * map **is a copy** of the internal map
     * @return a copy of this map as a regular [Map]
     */
    fun asMap() : Map<String, Any> {
        return params.toMap()
    }

    /**
     * Returns this [ReadableParameterMap] as a normal read-only [MutableMap]. Notice that the returned
     * map **is a copy** of the internal map, so changes to the returned map are not propagated into
     * this parameter map
     * @return a copy of this map as a regular [MutableMap]
     */
    fun asMutableMap() : MutableMap<String, Any> {
        return params.toMutableMap()
    }

    /**
     * Returns the element associated with the given `name`, casting it to the desired `T` type, or `null`
     * if no element is associated to the name
     * @param name the name of the searched parameter
     * @return the element associated with the given `name`, cast to the desired `T` type, or `null`
     * if no element is associated to the name
     */
    fun <T> getAs(name : String) : T? {
        if(params.containsKey(name))
            return params[name]!! as T

        return null
    }

    /**
     * Tries to return the element associated with the given name, casting it to the desired `T` type.
     * If no element is associated with the passed `name` then `null` is returned
     * @param T the desired type of the searched element
     * @param name the name of the searched parameter
     * @return the parameter associated with the key, cast to the desired type or `null` if no element
     * is present
     */
    inline fun <reified T> tryGetAs(name : String) : T? {
        if(hasParam(name)) {
            val param = this[name]
            if(param is T)
                return param
        }

        return null
    }

    /**
     * Returns the element associated with the given `name` mapped as a `T` object. If no parameter is
     * associated with the `name`, then it returns `null`
     * @param name the name of the searched parameter
     * @param mapper the mapper function
     * @return the mapped element or `null` is no parameter is associated with the given name
     */
    fun <T> map(name : String, mapper : (Any) -> T) : T? {
        if(params.containsKey(name))
            return mapper.invoke(params[name]!!)

        return null
    }

    /**
     * Searches for a parameter associated with the given `name` casting it to an `I` object
     * and then, if present, maps the found
     * element to an object of type `O`, by applying the `mapper` function. Returns `null` is no element
     * is associated with the given name and throws an exception if the cast to the `I` type fails.
     * This method is differen from [tryCastAndMap] because it throws an exception if the cast of the
     * parameter to the `I` type fails (but it however returns null if no element with the given name
     * is present)
     * @exception ClassCastException if the cast to the `I` type of the searched parameter fails
     * @param I the desired type of the searched parameter
     * @param O the return type
     * @param name the name associated with the searched parameter
     * @param mapper the mapper function
     * @return the mapped object or `null` of this map does not contain an element associated with the given
     * name
     */
    fun <I,O> castAndMap(name : String, mapper : (I) -> O) : O? {
        if(params.containsKey(name))
            return mapper.invoke(params[name]!! as I)

        return null
    }

    /**
     * Searches for a parameter associated with the given `name` casting it to an `I` object  and then,
     * if present, maps the found element to an object of type `O`, by applying the `mapper` function.
     * This method is different from [tryCastAndMap] and [castAndMap] because it throws an exception
     * if no element is associated with the passed `name` but also if the cast to an `I` object fails
     * @exception NoSuchElementException if no element is associated with the given `name`
     * @exception ClassCastException if the cast to the `I` type of the searched parameter fails
     * @param I the desired type of the searched parameter
     * @param O the return type
     * @param name the name associated with the searched parameter
     * @param mapper the mapper function
     * @return the mapped object
     */
    fun <I,O> forceCastAndMap(name : String, mapper : (I) -> O) : O {
        if(!params.containsKey(name))
            throw NoSuchElementException("no parameter with name $name")
        return mapper.invoke(params[name]!! as I)
    }

    /**
     * Tries to cast the element associated with the given `name` to a `I` object and then
     * maps it to a `O` object using the given `mapper`. If no element is associated with the
     * given `name` or if the cast of the parameter to the `I` type fails, then `null` is returned.
     * This method is different from [castAndMap] because it does not throw exceptions but returns `null`
     * in case of failure
     */
    inline fun <reified I, O> tryCastAndMap(name : String, mapper : (I) -> O) : O? {
        if(hasParam(name)) {
            val param = this[name]
            if(param is I)
                return mapper.invoke(param)
        }

        return null
    }

    /**
     * Tries to apply the given action to the parameter associated with the given `name`.
     * If no object is associated to the name or if is present but is not of the `T` type, then
     * the action is **not** applied.
     * @param name the name of the searched parameter
     * @param action to action to be performed if the element exists and if it is a `T` object
     * @return this map
     */
    inline fun <reified T> tryWithParam(name : String, action : (T) -> Unit) : ReadableParameterMap {
        if(this.hasParam(name)) {
            val param = this[name]
            if(param != null)
                if(param is T)
                    action.invoke(param)
        }

        return this
    }

    /**
     * Executes the action represented by `then` only if the map contains a parameter associated with the given
     * `name` that is equals to [expected], the returns the result of the comparison.
     * Notice that the equality includes the type of the two objects
     * @param name the name of the searched parameter
     * @param expected the expected value of the parameter
     * @param then the action that will be invoked
     * @return `true` if the parameter associated to the given name is equals to the [expected]
     */
    inline fun <reified T> ifIsEqualsTo(name : String, expected : T, then : (T) -> Unit) :
            Boolean {
        if(hasParam(name)) {
            val param = this[name]!!
            if(param is T)
                if(param == expected) {
                    then(param)
                    return true
                }
        }

        return false
    }

    /**
     * Returns `true` if the map contains a parameter associated with the given
     * `name` is equals to [expected]. Notice that the equality includes the type of the two objects
     * @param name the name of the searched parameter
     * @param expected the expected value of the parameter
     * @param then the action that will be invoked
     * @return `true` if the parameter associated to the given name is equals to the [expected]
     */
    inline fun <reified T> isEqualsTo(name : String, expected : T, then : (T) -> Unit) :
            Boolean {
        if(hasParam(name)) {
            val param = this[name]!!
            if(param is T)
                if(param == expected) {
                    then(param)
                    return true
                }
        }

        return false
    }

    operator fun iterator() : Iterator<Map.Entry<String, Any>> {
        return params.iterator()
    }

    /**
     * Calls the normal [Map.toString] to the internal map
     * @return the [Map.toString] of the internal map
     */
    override fun toString(): String {
        return params.toString()
    }

    /**
     * Performs the given `action` on each parameter of this map
     */
    fun forEach(action : (Map.Entry<String, Any>) -> Unit) {
        params.forEach(action)
    }

    /**
     * Returns a new [ReadableParameterMap] that contains all the parameters that are
     * already present **plus** the new one
     * @return the new [ReadableParameterMap]
     */
    operator fun plus(params : Pair<String, Any>) : ReadableParameterMap {
        val newMap = mutableMapOf<String, Any>()
        newMap.putAll(this.params)
        newMap[params.first] = params.second
        return ImmutableParameterMap(newMap)
    }

    /**
     * Returns a new map that is the result of the merge of this and the given map.
     * So, the new map contains all the element of this map and of that passed as parameter
     * to this methos
     * @return the new map
     */
    operator fun plus(parameterMap : ReadableParameterMap) : ReadableParameterMap {
        val resultMap = params.toMutableMap()
        resultMap.putAll(parameterMap.asMap())
        return ImmutableParameterMap(resultMap)
    }
}