package kbomberx.concurrency.shared

import kotlinx.coroutines.CoroutineScope
import java.util.LinkedList

/**
 * An implementation for [AbstractCoroutineSharedList] that use an internal [LinkedList]
 */
class CoroutineSharedLinkedList<T : Any>(scope : CoroutineScope = SharedScope) :
    AbstractCoroutineSharedList<T>(LinkedList<T>(), scope)