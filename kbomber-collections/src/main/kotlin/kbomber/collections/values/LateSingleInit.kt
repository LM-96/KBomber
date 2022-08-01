package kbomber.collections.values

/**
 * Represents an object that can be initialized only once
 */
class LateSingleInit<T> {

    private var value : T? = null

    /**
     * Sets the value of the object. This method is different from [setIfNotAlready]
     * because it throws an exception if value is already set
     * @exception [IllegalStateException] if the object is already set
     * @param initValue the value to be
     */
    @Throws(IllegalStateException::class)
    fun set(initValue : T) {
        if(this.value != null)
            throw IllegalStateException("Value already initialized")

        this.value = initValue
    }

    /**
     * Sets the value of this object only if it is not already been set.
     * This method is different from [set] because it does not throw any exception
     * @param initValue the value to be set
     * @return `true` if the value has been set, `false` otherwise (if already set)
     */
    fun setIfNotAlready(initValue: T) : Boolean {
        if(this.value == null) {
            this.value = initValue
            return true
        }

        return false
    }

    /**
     * Returns the value of this object. It is different from [getOrNull] method because it
     * throw an exception if the value has not been set
     * @exception [IllegalStateException] if the value has not been set
     * @return the value
     */
    @Throws(IllegalStateException::class)
    fun get() : T {
        if(value == null)
            throw IllegalStateException("Value not initialized")

        return value!!
    }

    /**
     * Returns the value of this object or `null` if the value has not been set.
     * This method is different from [get] because it does not throw any exception
     * @return the value of this object or `null` if the value has not been set
     */
    fun getOrNull() : T? {
        return value
    }

    /**
     * Returns the value of this object or the `elseObj` if it was not set
     * @return the value of this object or the `elseObj` if it was not set
     */
    fun orElse(elseObj : T) : T {
        if(value != null)
            return value!!

        return elseObj
    }

    /**
     * Returns `true` if the value of this object has been set, `false` if not.
     * This method is the dual of [isNotInitialized]
     * @return `true` if the value of this object has been set, `false` if not
     */
    fun isInitialized() : Boolean {
        return value != null
    }

    /**
     * Returns `true` if the value of this object has **not** been set, `false` if yes.
     * This method is the dual of [isInitialized]
     * @return `true` if the value of this object has **not** been set, `false` if yes
     */
    fun isNotInitialized() : Boolean {
        return value == null
    }

}
