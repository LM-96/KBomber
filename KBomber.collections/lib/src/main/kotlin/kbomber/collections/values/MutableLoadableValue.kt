package kbomber.collections.values

/**
 * Represents a value that **could** be loaded.
 * This class extends [LoadableValue] exposing methods that allow
 * to load, reload or unload the value.
 */
class MutableLoadableValue<T>() : LoadableValue<T>() {

    internal constructor(value : T?, status : LoadStatus, error : Exception?) : this() {
        this.value = value
        this.status = status
        this.error = error
    }

    /**
     * Applies the loader passed as parameter putting its result
     * into this object. Notice that **if the loader throws an exception,
     * it will be stored into [error] var** of this object and the
     * status will be [LoadStatus.NOT_FOUND].
     * Otherwise, if all goes good, the result is stored into [value] and the
     * status will be [LoadStatus.LOADED].
     * **If the values has previously been loaded, this method throws an exception** because
     * it is not possible to use it to reload the value (see [loadOrReload] or [reload])
     * @throws IllegalStateException if the value is already loaded
     * @return this object after the `loader` was applied
     */
    fun load(loader : () -> T) : MutableLoadableValue<T> {
        if(status != LoadStatus.UNTOUCHED)
            throw IllegalStateException("Value is already loaded")
        return loadOrReload(loader)
    }

    /**
     * Applies the loader passed as parameter putting its result
     * into this object. Notice that **if the loader throws an exception,
     * it will be stored into [error] var** of this object and the
     * status will be [LoadStatus.NOT_FOUND].
     * Otherwise, if all goes good, the result is stored into [value] and the
     * status will be [LoadStatus.LOADED] if it was untouched or [LoadStatus.RELOADED] in
     * the other cases
     * @return this object after the `loader` is applied
     */
    fun loadOrReload(loader: () -> T) : MutableLoadableValue<T> {
        try {
            value = loader()
            status = when(status) {
                LoadStatus.UNTOUCHED -> LoadStatus.LOADED
                else -> LoadStatus.RELOADED
            }
            if(error != null)
                error = null
        } catch (e : Exception) {
            this.status = LoadStatus.NOT_FOUND
            this.error = e
        }
        return this
    }

    /**
     * Applies the loader passed as parameter putting its result
     * into this object. Notice that **if the loader throws an exception,
     * it will be stored into [error] var** of this object and the
     * status will be [LoadStatus.NOT_FOUND].
     * Otherwise, if all goes good, the result is stored into [value] and the
     * status will be [LoadStatus.RELOADED].
     * **If the values has not previously been loaded, this method throws an exception** because
     * it is not possible to use it to load the value for the first time (see [loadOrReload] or [load])
     * @throws IllegalStateException if the value has never been loaded for the first time
     * @return this object after the `loader` was applied
     */
    fun reload(loader: () -> T) : MutableLoadableValue<T> {
        if(status == LoadStatus.UNTOUCHED)
            throw IllegalStateException("Value is never been loaded: unable to reload")
        return loadOrReload(loader)
    }

    /**
     * Unloads the value encapsulated into this object.
     * After the invocation of this method, the [value] and [error] variables
     * are set to `null` and the status is [LoadStatus.UNLOADED]
     * @return this object after unload
     */
    fun unload() : MutableLoadableValue<T> {
        return when(status) {
            LoadStatus.UNTOUCHED ->
                throw IllegalStateException("Value is never been loaded: unable to unload")
            LoadStatus.NOT_FOUND ->
                throw IllegalStateException("Values has not been found: unable to unload")
            else -> {
                this.value = null
                this.error = null
                this.status = LoadStatus.UNLOADED
                this
            }
        }
    }


    /**
     * Performs the given action on this object if the current status is the desired, otherwise
     * nothing will be done
     * @param status the desired status
     * @param action the action to invoke on this object
     * @return this object
     */
    fun ifMutableStatus(status : LoadStatus, action : MutableLoadableValue<T>.() -> Unit): MutableLoadableValue<T> {
        if(this.status == status)
            action(this)

        return this
    }

    /**
     * Performs the given action on this object if the current status is [LoadStatus.LOADED].
     * If the status is different, nothing will be done
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableLoaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isLoaded()) this.action()
        return this
    }

    /**
     * Performs the given action on this object if the current status is [LoadStatus.RELOADED].
     * If the status is different, nothing will be done
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableReloaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isReloaded()) this.action()
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.LOADED].
     * So, the action will be invoked **only** if the value of this object is not loaded;
     * notice that the action will be however performed if the object has been *reloaded*
     * (use [ifMutableNotLoadedOrReloaded] if you don't want this)
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableNotLoaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isLoaded()) this.action()
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.LOADED]
     * and not [LoadStatus.RELOADED].
     * So, the action will be invoked if the value of this object is not loaded or reloaded
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableNotLoadedOrReloaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isLoaded() && !isReloaded()) this.action()
        return this
    }

    /**
     * Performs the given action on this object if the current status is [LoadStatus.UNTOUCHED].
     * If the status is different, nothing will be done
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableUntouched(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isUntouched()) this.action()
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.UNTOUCHED].
     * So, the action will be invoked if at least one operation on this object has been performed
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableNotUntouched(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isUntouched()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the current status is [LoadStatus.UNLOADED].
     * If the status is different, nothing will be done
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableUnloaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isUnLoaded()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.UNLOADED].
     * Notice that the action will be invoked even if this object is untouched
     * (use [ifMutableLoadedButNotUnloaded] if you don't want this)
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableNotUnloaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isUnLoaded()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.UNLOADED]
     * but it has been previously loaded, reloaded or if it is not found
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableLoadedButNotUnloaded(action: MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isUnLoaded() && !isUntouched()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the current status is [LoadStatus.NOT_FOUND]
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableNotFound(action : MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isNotFound()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the current status is **not** [LoadStatus.NOT_FOUND].
     * Notice that this includes that the action is performed even if this object is untouched
     * (use [ifMutableLoadedAndFound] if you don't want this)
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableFound(action : MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(!isNotFound()) action.invoke(this)
        return this
    }

    /**
     * Performs the given action on this object if the value has been correct loaded or
     * reloaded, so a value is present
     *
     * @param the action to invoke on this object
     * @return this object
     */
    fun ifMutableLoadedOrReloaded(action : MutableLoadableValue<T>.() -> Unit) : MutableLoadableValue<T> {
        if(isLoaded() || isReloaded()) action.invoke(this)
        return this
    }

    /**
     * Returns this object as [LoadableValue]
     * @return this object as [LoadableValue]
     */
    fun asLoadableValue() : LoadableValue<T> {
        return this
    }

}