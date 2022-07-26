package kbomberx.concurrency.coroutineserver

/**
 * A class that represents a reply of a server.
 * Typically, the [replyCode] shows a basic description ([OK_CODE] or [ERR_CODE]) and
 * the [params] maintains a list of results.
 * Notice that **if this reply contains an error** (so [replyCode] is [ERR_CODE]) **then
 * the exception is maintained into the first position of [params]** array.
 */
data class ServerReply(
    val replyCode : Int,
    val params : Array<out Any> = arrayOf()
) {

    companion object {
        /**
         * A code of a reply that means *all is go well*
         */
        const val OK_CODE = 0

        /**
         * A code of a reply that contains an error.
         * The exception should be stored into the position 0 of the
         * [ServerReply.params] array
         */
        const val ERR_CODE = 1
    }

    /**
     * Returns `true` if this response means that all is gone well
     * @return `true` if this response means that all is gone well,
     * `false` otherwise
     */
    fun isOk() : Boolean {
        return replyCode == OK_CODE
    }

    /**
     * Returns `true` if this response means that an error is thrown
     * @return `true` if this response means that an error is thrown,
     * `false` otherwise
     */
    fun isError() : Boolean {
        return replyCode == ERR_CODE
    }

    /**
     * Returns the error maintained into the first parameter
     * of this reply
     * @throws IllegalStateException if this reply does not contains
     * the error at the first parameter
     * @return the thrown error
     */
    fun getError() : ReplyException {
        if(params.isNotEmpty())
            if(params[0] is Exception)
                return ReplyException(params[0] as Exception)

        throw IllegalStateException("no error is found inside params")
    }

    /**
     * Throws the error maintained into the first parameter.
     * If no error is found, this function returns `false`
     * @throws ReplyException the error in the first parameter
     * @return `false` if no error is found
     */
    fun throwError() : Boolean {
        if(params.isNotEmpty())
            if(params[0] is Exception)
                throw ReplyException(params[0] as Exception)

        return false
    }

    /**
     * Scan the parameter array throwing an exception if present in
     * one of the parameters
     * @throws ReplyException if a parameter is an exception
     * @return the parameter array
     */
    fun throwsFirstExceptionOrGetParams() : Array<out Any> {
        for (param in params) {
            if(param is Exception)
                throw ReplyException(param)
        }

        return params
    }

    /**
     * Checks if this reply contains an error and eventually throws it.
     * If no error is present, then this method returns the parameter array
     * @throws ReplyException the error contained in the first parameter, if present
     * @return the array of parameter
     */
    fun throwErrorOrGetParameters() : Array<out Any> {
        if(params.isNotEmpty())
            if(params[0] is Exception)
                throw ReplyException(params[0] as Exception)

        return params
    }

    /**
     * Returns the first parameter or `null` if not present.
     * If the parameter is an exception, then throws it instead of returning it
     * @throws ReplyException the error contained in the first parameter, if present
     * @return the first parameter or `null` if not present
     */
    fun throwErrorOrTryGetFirstParameter() : Any? {
        if(params.isNotEmpty()){
            if(params[0] is Exception)
                throw ReplyException(params[0] as Exception)

            return params[0]
        }

        return null
    }

    /**
     * Returns the first parameter throwing an exception if not present.
     * If the parameter is an exception, then throws it instead of returning it
     * @throws NoSuchElementException if the parameter array is empty
     * @throws ReplyException the error contained in the first parameter, if present
     * @return the first parameter or `null` if not present
     */
    fun throwErrorOrGetFirstParameter() : Any {
        if(params.isEmpty())
            throw NoSuchElementException("no parameters in this reply")

        if(params[0] is Exception)
            throw ReplyException(params[0] as Exception)

        return params[0]

    }

    /**
     * Returns the first parameter throwing an exception if not present.
     * If the parameter is an exception, then throws it instead of returning it
     * otherwise this function returns it after casting it to the expected type.
     * **The cast is done without check**
     * @throws NoSuchElementException if the parameter array is empty
     * @throws ReplyException the error contained in the first parameter, if present
     * @return the first parameter or `null` if not present
     */
    fun <T> throwErrorOrGetFirstParameterAs(clazz : Class<T>) : T {
        if(params.isEmpty())
            throw NoSuchElementException("no parameters in this reply")

        if(params[0] is Exception)
            throw ReplyException(params[0] as Exception)

        return params[0] as T

    }
    /**
     * Tries to cast and return the first parameter throwing an exception if not present.
     * If the parameter is an exception, then throws it instead of returning it
     * otherwise this function returns it after casting it to the expected type.
     * **If the cast fails an exception is thrown**
     * @throws NoSuchElementException if the parameter array is empty or if the
     * cast fails
     * @throws ReplyException the error contained in the first parameter, if present
     * @return the first parameter or `null` if not present
     */
    inline fun <reified T> throwErrorOrTryCastFirstParameter(clazz : Class<T>) : T {
        if(params.isEmpty())
            throw NoSuchElementException("no parameters in this reply")

        if(params[0] is Exception)
            throw ReplyException(params[0] as Exception)

        if(params[0] is T)
            return params[0] as T
        else
            throw NoSuchElementException("parameter is not of the desired type")

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerReply

        if (replyCode != other.replyCode) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = replyCode
        result = 31 * result + params.contentHashCode()
        return result
    }
}