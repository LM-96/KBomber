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

fun <T> simpleAtomicVar(value : T) : AtomicVar<T> {
    return SimpleAtomicVar(value)
}

/* READ WRITE ATOMIC VAR **************************************************************** */
fun <T> T.asReadWriteAtomic() : ReadWriteAtomicVar<T> {
    return ReadWriteAtomicVar(this)
}

fun <T> readWriteAtomicVar(value : T) : ReadWriteAtomicVar<T> {
    return ReadWriteAtomicVar(value)
}