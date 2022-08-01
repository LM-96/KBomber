package kbomber.reflection.method

import java.lang.reflect.Method
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.kotlinFunction

/**
 * Counts the parameter of this method considering if it is
 * a *Kotlin* function or a standard *Java* method.
 * So, this method should return the real number of parameter
 * @return the number of the parameter of this method
 */
fun Method.countParams() : Int {
    return kotlinFunction?.valueParameters?.size ?: parameterCount
}

/**
 * Return `true` if this method admits at least one parameter considering if it is
 * a *Kotlin* function or a standard *Java* method.
 * @return `true` if this method admits at least one parameter
 */
fun Method.hasParameters() : Boolean {
    return kotlinFunction?.valueParameters?.isNotEmpty() ?: parameters.isNotEmpty()
}

/**
 * Returns a [List] with the *Kotlin* parameter of this function.
 * The result is `null` if this method cannot be represented by a Kotlin function
 * @return a [List] with the *Kotlin* parameter of this function or `null`
 * if this method cannot be represented by a Kotlin function
 */
fun Method.getKotlinParameters() : List<KParameter>? {
    return kotlinFunction?.valueParameters
}

/**
 * Returns `true` if the parameter at the specified position is of the type
 * represented by the given *Kotlin* `clazz`. If this method cannot be represented
 * as a *Kotlin* function, then the check is done at *Java* level, using the normal
 * [Class] instance of the `clazz` parameter
 * @param param the number of the parameter
 * @param clazz the *Kotlin* class
 * @return `true` if the parameter at the specified position is of the type
 * represented by the given *Kotlin* `clazz`
 */
fun Method.checkParameterOfType(param : Int, clazz : KClass<*>) : Boolean {
    val kParam = kotlinFunction?.valueParameters
    return if(kParam != null)
        kParam[param].type.jvmErasure == clazz
    else
        parameterTypes[param] == clazz.java
}

/**
 * Returns `true` if this method can be represented as a *Kotlin* function and
 * it also is *suspend*
 * @return `true` if this method is a **suspend** function
 */
fun Method.isSuspendFunction() : Boolean {
    return kotlinFunction?.isSuspend == true
}

/**
 * Invokes this method using the current coroutine.
 * The invocation is done using reflection (see [invoke]).
 * Notice that **an exception is thrown if this [Method] object does not
 * represent a `suspend` *Kotlin* function**.
 * Use [invokeProperly] to be safer
 * @param obj the object the underlying method is invoked from
 * @param params the arguments used for the method call
 */
suspend fun Method.invokeSuspend(obj : Any, vararg params : Any?) : Any =
    suspendCoroutineUninterceptedOrReturn { continuation -> invoke(obj, *params, continuation) }

/**
 * Invokes this method considering its nature.
 * If this method is a **suspend** function, then it is invoked using the
 * current coroutine (see [invokeSuspend]); otherwise, it is regularly invoked
 * as a normal method (see [invoke])
 */
suspend fun Method.invokeProperly(obj: Any, vararg params: Any?) : Any {
    if(isSuspendFunction()) {
        return invokeSuspend(obj, params)
    }
    return invoke(obj, params)
}

/**
 * Returns `true` if the signature of this method is that required
 * (so, this method has the given name, with the given return type and
 * the given parameter types).
 * Notice that this method works with both *Kotlin* function
 * (also suspend) and *Java* methods.
 * @param name the desired name of the method
 * @param returnType the desired return type of the method
 * @param paramTypes the types of the desired params of the method
 * @return `true` if the signature of this method is that required, `false` otherwise
 */
fun Method.checkSignature(name : String, returnType : Class<*>, vararg paramTypes : Class<*>) : Boolean {
    if(this.name == name) {
        val kotlinFunction = this.kotlinFunction
        if(kotlinFunction != null) { //Kotlin Function
            if(kotlinFunction.returnType.jvmErasure.java == returnType) {
                val params = kotlinFunction.valueParameters.map { it.type.jvmErasure.java }
                if(params.size == paramTypes.size) {
                    for (p in paramTypes)
                        if (!params.contains(p))
                            return false
                    return true
                }
            }

        } else { //Java Method
            if(this.returnType == returnType) {
                val params = this.parameterTypes
                if(params.size == paramTypes.size) {
                    for(p in paramTypes)
                        if(!params.contains(p))
                            return false
                    return true
                }
            }
        }
    }

    return false
}
