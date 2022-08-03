package kbomberx.io

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.EmptyCoroutineContext

/**
 * The default scope for all I/O objects
 */
val IoScope = CoroutineScope(EmptyCoroutineContext + CoroutineName("IO-SCOPE"))