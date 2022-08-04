package kbomberx.concurrency.atomic

/* DEFAULT ATOMIC VAR ******************************************************************* */
fun <T> T.asAtomic() : AtomicVar<T> {
    return SimpleAtomicVar(this)
}

fun <T> atomicVar(value : T) : AtomicVar<T> {
    return SimpleAtomicVar(value)
}

/* SIMPLE ATOMIC VAR ******************************************************************** */
fun <T> T.asSimpleAtomic() : SimpleAtomicVar<T> {
    return SimpleAtomicVar(this)
}

fun <T> simpleAtomicVar(value : T) : SimpleAtomicVar<T> {
    return SimpleAtomicVar(value)
}

/* READ WRITE ATOMIC VAR **************************************************************** */
fun <T> T.asReadWriteAtomic() : ReadWriteAtomicVar<T> {
    return ReadWriteAtomicVar(this)
}

fun <T> readWriteAtomicVar(value : T) : ReadWriteAtomicVar<T> {
    return ReadWriteAtomicVar(value)
}

/* MULTIPLE ATOMIC ********************************************************************** */
/**
 * Executes the given [block] locking both atomic variables [atomic1] and [atomic2]
 * @param atomic1 the first [AtomicVar]
 * @param atomic2 the second [AtomicVar]
 * @param block the function to be executed in mutual exclusion
 */
suspend fun <T1, T2> withBothLocked(atomic1 : AtomicVar<T1>, atomic2: AtomicVar<T2>, block : (T1, T2) -> Unit) {
    atomic1.atomicUseValue {  v1 ->
        atomic2.atomicUseValue { v2 ->
            block(v1, v2)
        }
    }
}