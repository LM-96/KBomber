package kbomberx.concurrency.coroutineserver

/**
 * A class that encapsulate an error that is thrown by executing the command requested by
 * a [CmdServerRequest].
 */
class ReplyException (
    /**
     * The original [Exception] that is thrown during the execution of a requested command
     */
    val causeException : Exception
    ) : Exception(causeException)