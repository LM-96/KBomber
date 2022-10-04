package kbomberx.actors.model

/**
 * This class represents the *state* of an actor with a name that is the identifier of
 * this state.
 * A state maintains two list of functions:
 * * ```onEnter```: a list of function that have to be invoked when the actor
 * enters this state;
 * * ```onExit```: a list of function that have to be invoked when the actor
 * exits this state
 */
data class State (
    val name : String,
    val onEnter : Map<String, () -> Unit> = mapOf(),
    val onExit : Map<String, () -> Unit> = mapOf()
) {

    /**
     * Performs all the action which have to be invoked
     * when the actor **enters** this state.
     * This function also provide an optional argument ```externalExceptionHandler```
     * which is a function that is invoked when one ```onEnter``` function throws an
     * exception: this mechanism prevent actor blocks or unexpected errors
     *
     * @param externalExceptionHandler the function to be invoked every time a function
     * throws an exception
     */
    fun invokeOnEnter(externalExceptionHandler : String.(Exception) -> Unit = {it.printStackTrace()}) {
        onEnter.forEach { (n, f) ->
            try {
                f()
            } catch (e : Exception) { externalExceptionHandler(n, e) }
        }
    }

    /**
     * Performs all the action which have to be invoked
     * when the actor **exits** this state.
     * This function also provide an optional argument ```externalExceptionHandler```
     * which is a function that is invoked when one ```onEnter``` function throws an
     * exception: this mechanism prevent actor blocks or unexpected errors
     *
     * @param externalExceptionHandler the function to be invoked every time a function
     * throws an exception
     */
    fun invokeOnExit(externalExceptionHandler : String.(Exception) -> Unit = {it.printStackTrace()}) {
        onExit.forEach { (n, f) ->
            try {
                f()
            } catch (e : Exception) { externalExceptionHandler(n, e) }
        }
    }

}