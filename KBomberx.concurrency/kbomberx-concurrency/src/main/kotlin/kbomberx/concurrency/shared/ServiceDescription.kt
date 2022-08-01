package kbomberx.concurrency.shared

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class ServiceDescription(
    val description : String
)
