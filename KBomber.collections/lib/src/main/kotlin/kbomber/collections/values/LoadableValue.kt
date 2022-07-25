package kbomber.collections.values

/**
 * Represents a value that has been loaded.
 * If the value has correctly been loaded, then the variable [value] contains the
 * not null result. If the load operation fails due to an error, then [value] is null
 * and [error] contains the thrown error.
 * The variable [status] maintains the current status of this object (see [LoadStatus] for
 * details)
 */
abstract class LoadableValue<T> {

    /**
     * The value encapsulated into this object that is not `null` only if it has correctly
     * been loaded or reloaded. If this value is `null`, then the state of this object is
     * [LoadStatus.UNTOUCHED], [LoadStatus.UNLOADED] or [LoadStatus.NOT_FOUND]
     */
    var value : T? = null
        protected set

    /**
     * Maintains the status of this object
     */
    var status = LoadStatus.UNTOUCHED
        protected set

    /**
     * Maintains the error after a *load* or *reload* operation.
     * If the status is [LoadStatus.LOADED] or [LoadStatus.RELOADED] and this variable is `null`,
     * it means that the value has correctly been loaded or reloaded.
     * If the status is [LoadStatus.NOT_FOUND] and this variable is *not* `null`, then it
     * contains the error thrown during the load operation
     */
    var error : Exception? = null
        protected set

    /* CHECK VALUE/ERROR **************************************************************** */
    /**
     * Returns `true` if this object contains a value (so, if it has been *loaded*
     * or *reloaded*)
     */
    fun hasValue() : Boolean {
        return value != null
    }

    /**
     * Returns the *loaded* value. If the value has not been loaded,
     * this method throws an exception
     * @exception [IllegalStateException] if the value has not been loaded or injected
     * @return the *loaded* or *injected* value
     */
    @Throws(IllegalStateException::class)
    fun getOrThrows() : T {
        if(value == null) throw IllegalStateException("Value not present [status=$status]")
        return value!!
    }

    /**
     * Returns `true` if this object has no value, `false` otherwise.
     * Notice that a value can not be present if this object is [LoadStatus.UNTOUCHED]
     * or [LoadStatus.UNLOADED] or [LoadStatus.NOT_FOUND]
     * @return `true` if this object has no value, `false` otherwise
     */
    fun hasNoValue() : Boolean {
        return value == null
    }

    /**
     * Returns `true` if no error is thrown during the load of the value.
     * Notice that `true` is returned also if the value has not been loaded
     * @return `true` if no error is thrown during the load of the value,
     * `false` otherwise
     */
    fun hasError() : Boolean {
        return error != null
    }

    /* CHECK IS ************************************************************************* */
    /**
     * Checks the status of this object
     * @param status the desired status
     * @return `true` if this object has the desired status, `false` otherwise
     */
    fun isAtStatus(status : LoadStatus) : Boolean {
        return this.status == status
    }

    /**
     * Returns `true` if the value has been loaded, so the current status of this
     * object is [LoadStatus.LOADED]
     * @return `true` if the value has been loaded, `false` otherwise
     */
    fun isLoaded() : Boolean {
        return status == LoadStatus.LOADED
    }

    /**
     * Returns `true` if the value has been loaded and no error is thrown.
     * Notice that this method is different from [hasError] because this returns
     * `false` even if the value has not been loaded
     * @return `true` if the value has been loaded with no error, `false` otherwise
     */
    fun isLoadedWithNoError() : Boolean {
        return value != null && error != null
    }

    /**
     * Returns `true` if the value has been loaded at least once, so the current status of this
     * object is [LoadStatus.LOADED] or [LoadStatus.RELOADED]
     * @return `true` if the value has been loaded
     */
    fun isLoadedAtLeastOnce() : Boolean {
        return status == LoadStatus.LOADED || status == LoadStatus.RELOADED
    }

    /**
     * Returns `true` if the value has been reloaded so the current status of this
     * object is [LoadStatus.RELOADED]
     * @return `true` if the value has been loaded, `false` otherwise
     */
    fun isReloaded() : Boolean {
        return status == LoadStatus.RELOADED
    }

    /**
     * Returns `true` if the value is never been loaded, so the status of this object
     * is [LoadStatus.UNTOUCHED]. Notice that a value that is *untouched* is only been created
     * but never loaded (then, an *unloaded* value make this function returning `false`)
     * @return `true` if the value is untouched, `false` otherwise
     */
    fun isUntouched() : Boolean {
        return status == LoadStatus.UNTOUCHED
    }

    /**
     * Returns `true` if the value has previously been loaded and after unloaded
     * without reloading (so, the status of this object is [LoadStatus.UNLOADED])
     * @return`true` if the value is *unloaded* without reloading, `false` otherwise
     */
    fun isUnLoaded() : Boolean {
        return status == LoadStatus.UNLOADED
    }

    /**
     * Returns `true` if the value has not been found during its loading
     * (so, the status of this object is [LoadStatus.NOT_FOUND])
     * @return `true` if the value has not been found, `false` otherwise
     */
    fun isNotFound() : Boolean {
        return status == LoadStatus.NOT_FOUND
    }

    /**
     * Returns `true` if the value has not been found during its loading
     * (so, the status of this object is [LoadStatus.NOT_FOUND]) and an
     * error has been thrown during its load
     * @return `true` if the value has not been found with some errors
     * during its load, `false` otherwise
     */
    fun isNotFoundWithError() : Boolean {
        return status == LoadStatus.NOT_FOUND && error != null
    }

    /* PERFORMS IF ********************************************************************** */
    /**
     * Performs the given action on the value encapsulated in this object
     * **only if its status is the one desired**.
     * Notice that in the case the status is different from the desired,
     * nothing will be done
     * @param action the action to be performed with the value
     */
    fun ifStatus(status: LoadStatus, action : (T?) -> Unit) : LoadableValue<T> {
        if(isAtStatus(status))
            action.invoke(value)
        return this
    }

    /**
     * Performs an action if the value has previously been loaded
     * @param action the action to be performed with the value of this object
     * @return this object
     */
    fun ifLoaded(action: (T) -> Unit) : LoadableValue<T> {
        if(isLoaded()) action.invoke(value!!)
        return this
    }

    /**
     * Returns the value of this object if it has been loaded,
     * otherwise it returns the given element
     * @param otherValue the return value if this object is not loaded
     * @return the value of this object or `otherValue` if not loaded
     */
    fun getIfLoadedOrElse(otherValue : () -> T) : T {
        return if(isLoaded()) value!!
        else otherValue.invoke()
    }

    /**
     * Performs an action if the value has previously been reloaded
     * @param action the action to be performed with the value of this object
     * @return this object
     */
    fun ifReloaded(action: (T) -> Unit) : LoadableValue<T> {
        if(isReloaded()) action.invoke(value!!)
        return this
    }

    /**
     * Performs an action if the value is not loaded or reloaded
     * @param action the action to be performed
     * @return this object
     */
    fun ifNotLoaded(action : LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(!isLoaded() && !isReloaded()) action.invoke(this)
        return this
    }

    /**
     * Performs an action if the value is untouched (then it is not *loaded* or *reloaded*
     * or *unloaded*)
     * @param action the action to be performed on this object
     * @return this object
     */
    fun ifUntouched(action: LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(isUntouched()) action(this)
        return this
    }

    /**
     * Performs an action if the value is unloaded
     * @param action the action to be performed on this object
     * @return this object
     */
    fun ifUnloaded(action: LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(isUnLoaded()) action.invoke(this)
        return this
    }

    /**
     * Performs an action if the value has **not** been unloaded.
     * Notice that the action is not performed if the value is *untouched* or
     * *reloaded*
     * @param action the action to be performed on this object
     * @return this object
     */
    fun ifNotUnloaded(action : LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(!isUnLoaded()) action.invoke(this)
        return this
    }

    /**
     * Performs an action if the value has **not** been found during its loading
     * @param action the action to be performed on this object
     * @return this object
     */
    fun ifNotFound(action: LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(isNotFound()) action(this)
        return this
    }

    /**
     * Return true if the value has been found during is load.
     * Notice that the action is performed only **if a load attempt has been done** and
     * this **includes also the case in which the value has been unloaded**.
     * @return `true` if the value has not been found, `false` otherwise
     */
    fun ifFound(action : LoadableValue<T>.() -> Unit) : LoadableValue<T> {
        if(!isNotFound() && !isUntouched()) action.invoke(this)
        return this
    }

    /* PERFORM WITH VALUE *************************************************************** */

    /**
     * Performs the given action on the value encapsulated in this object.
     * Notice that this method throws an exception in case the value is absent
     * because it has not been loaded, or it has been unloaded
     * @throws [IllegalStateException] if this object has no value
     * @param action the action to be performed with the value
     */
    fun withValue(action : (T) -> Unit) : LoadableValue<T> {
        if(value == null) throw IllegalStateException("Value not present [status=$status]")
        action.invoke(value!!)
        return this
    }

    /**
     * Performs the given action on the value encapsulated in this object considering
     * its state by passing as parameter of the action.
     * Notice that this method throws an exception in case the value is absent
     * because it has not been loaded, or it has been unloaded
     * @throws [IllegalStateException] if this object has no value
     * @param action the action to be performed with the value and its state
     */
    fun withValue(action : (LoadStatus, T) -> Unit) : LoadableValue<T> {
        if(value == null) throw IllegalStateException("Value not present [status=$status]")
        action.invoke(status, value!!)
        return this
    }

    /**
     * Tries to perform the given action **only if it is present**. If the value
     * is not present, nothing will be done
     * @param action the action to be performed with the value
     */
    fun tryWithValue(action : (T) -> Unit) : LoadableValue<T> {
        if(value != null) action.invoke(value!!)
        return this
    }

    /**
     * Tries to perform the given action on the value encapsulated in this object considering
     * its state by passing as parameter of the action.
     * If the value
     * is not present, nothing will be done
     * @throws [IllegalStateException] if this object has no value
     * @param action the action to be performed with the value and its state
     */
    fun tryWithValue(action : (LoadStatus, T) -> Unit) : LoadableValue<T> {
        if(value != null) action.invoke(status, value!!)
        return this
    }

    /**
     * Performs the given action on the value encapsulated in this object.
     * This method is different from [withValue] because it does not
     * throw any exception but simply pass a `null` object to the action
     * @param action the action to be performed with the value that can
     * be `null` if absent
     */
    fun withNullableValue(action : (T?) -> Unit) : LoadableValue<T> {
        action.invoke(value)
        return this
    }

    /**
     * Performs the given action on the value encapsulated in this object considering
     * its state by passing as parameter of the action.
     * This method is different from [withValue] because it does not
     * throw any exception but simply pass a `null` object to the action
     * (however, the [LoadStatus] parameter of the action can never be `null`)
     * @param action the action to be performed with the value that can
     * be `null` if absent and its state
     */
    fun withNullableValue(action : (LoadStatus, T?) -> Unit) : LoadableValue<T> {
        action.invoke(status, value)
        return this
    }

    /* MAP ****************************************************************************** */

    /**
     * Transforms the value encapsulated into this object to a new type
     * @param mapper the mapper function
     * @return the result of the transformation
     */
    fun <R> map(mapper : (T?) -> R?) : R? {
        return mapper.invoke(value)
    }

    /**
     * Transforms the value encapsulated into this object to a new type.
     * This method is different from [map] because the result of the transformation
     * can not be null
     * @param mapper the mapper function
     * @return the result of the transformation
     */
    fun <R> aMap(mapper : (T?) -> R) : R {
        return mapper(value)
    }

    /**
     * Transforms the value encapsulated into this object to a new type.
     * This method is different from [map] because no nullable values are
     * allowed and throws an exception if the value of this object is absent
     * @param mapper the mapper function with no nullable parameters or result
     * @return the result of the transformation
     */
    @Throws(IllegalStateException::class)
    fun <R> mapOrThrows(mapper : (T) -> R) : R {
        if (value == null) throw IllegalStateException("Unable to map with a $status status")
        return mapper(value!!)
    }

}