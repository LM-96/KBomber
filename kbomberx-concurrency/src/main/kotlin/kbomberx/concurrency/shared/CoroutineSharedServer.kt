package kbomberx.concurrency.shared

import kbomber.reflection.method.invokeProperly
import kbomberx.concurrency.coroutineserver.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.lang.reflect.Method
import java.time.LocalDateTime
import java.util.*
import kotlin.NoSuchElementException
import kotlin.reflect.jvm.kotlinFunction
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class CoroutineSharedServer<T : Any>(
    private val server : T,
    scope : CoroutineScope = SharedScope) : CoroutineServer(scope)
{

    companion object {
        const val SERVICES_DISCOVERY_CODE = 1
        const val SERVICES_NAME_LIST_CODE = 2
        const val SERVICE_INFO_CODE = 3
        const val EXECUTE_SERVICE_CODE = 4
        const val ASYNC_EXECUTE_SERVICE_CODE = 5
    }

    data class ServiceInfo(
        val serviceName : String,
        val serviceDescription : String,
        val inputParameterTypes : List<Class<*>>,
        val resultType : Class<*>
    )

    data class ServiceResult(
        val result : Any?,
        val resultClass : Class<*>,
        val errorsDuringExecution : Exception?,
        val startTime : LocalDateTime,
        val endTime : LocalDateTime
    )

    private val serviceClass = server::class.java
    private val offeredServiceDescriptions : Map<String, ServiceInfo>
    private val service2method : Map<String, Method>
    init {
        val methods = serviceClass.methods
        val descriptions = mutableMapOf<String, ServiceInfo>()
        val service2methodMut = mutableMapOf<String, Method>()
        var desc : String
        for(method in methods) {

            desc = if(method.isAnnotationPresent(ServiceDescription::class.java)) {
                method.getAnnotation(ServiceDescription::class.java).description
            } else ""

            descriptions[method.name] = ServiceInfo(
                    method.name, desc, method.parameterTypes.toList(), method.returnType)
            service2methodMut[method.name] = method
        }
        offeredServiceDescriptions = descriptions.toMap()
        service2method = service2methodMut.toMap()
    }

    override suspend fun handleRequest(request: CmdServerRequest) {
        when(request.requestCode) {

            SERVICES_DISCOVERY_CODE -> {
                replyWithOk(request, offeredServiceDescriptions.values)
            }

            SERVICES_NAME_LIST_CODE -> {
                replyWithOk(request, service2method.keys)
            }

            SERVICE_INFO_CODE -> {
                val serviceName = request.requestParams[0] as String
                if(!offeredServiceDescriptions.containsKey(serviceName))
                    replyWithError(request, Optional.empty<ServiceInfo>())
                else
                    replyWithOk(request, offeredServiceDescriptions[serviceName]!!)
            }

            EXECUTE_SERVICE_CODE -> {
                val serviceName = request.requestParams[0] as String
                val resultClass = offeredServiceDescriptions[serviceName]!!.resultType
                var serviceResult : ServiceResult
                val startTime = LocalDateTime.now()
                var endTime : LocalDateTime
                try {
                    val res = invokeService( serviceName,
                        request.requestParams.copyOfRange(1, request.requestParams.size)
                    )
                    endTime = LocalDateTime.now()
                    serviceResult = ServiceResult(res, resultClass, null, startTime, endTime)
                } catch (e : Exception) {
                    endTime = LocalDateTime.now()
                    serviceResult = ServiceResult(null, resultClass, e, startTime, endTime)
                }
                replyWithOk(request, serviceResult)
            }

            ASYNC_EXECUTE_SERVICE_CODE -> {
                val serviceName = request.requestParams[0] as String
                val resultClass = offeredServiceDescriptions[serviceName]!!.resultType
                var serviceResult : ServiceResult
                val startTime = LocalDateTime.now()
                var endTime : LocalDateTime
                try {
                    val res = invokeService( serviceName,
                        request.requestParams.copyOfRange(2, request.requestParams.size)
                    )
                    endTime = LocalDateTime.now()
                    serviceResult = ServiceResult(res, resultClass, null, startTime, endTime)
                } catch (e : Exception) {
                    endTime = LocalDateTime.now()
                    serviceResult = ServiceResult(null, resultClass, e, startTime, endTime)
                }

                val svcResultChannel = request.requestParams[1] as Channel<ServiceResult>
                svcResultChannel.send(serviceResult)
                svcResultChannel.close()
            }

        }
    }

    private suspend fun invokeService(serviceName: String, params : Array<out Any>) : Any? {
        params.forEach { p -> println("invokeService($serviceName), param: $p, param.toString: ${p.toString()}") }
        if(!service2method.containsKey(serviceName))
            throw NoSuchElementException("no offered service with name \'$serviceName\'")

        return service2method[serviceName]!!.invokeProperly(server, *params)
    }

    /**
     * Discovers all the *services* offered by this server returning
     * a [Set] that contains all the information about all the services
     * @return a [Set] that contains one [ServiceInfo] for each service offered by
     * this server
     */
    suspend fun discoverServices() : Set<ServiceInfo> {
        val req = basicRequest(SERVICES_DISCOVERY_CODE)
        mainChannel.send(req)
        return (req.responseChannel.receive().throwErrorOrGetFirstParameter() as Collection<ServiceInfo>).toSet()
    }

    /**
     * Discovers the name of all the *services* offered by this server returning
     * a [Set] that contains the names
     * @return a [Set] that contains all the names of the services offered by this server
     */
    suspend fun discoverServiceNames() : Set<String> {
        val req = basicRequest(SERVICES_NAME_LIST_CODE)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Set<String>
    }

    /**
     * Retrieves the [ServiceInfo] of the service that has the given [serviceName]
     * @param serviceName the name of the servie
     * @return an [Optional] that contains the [ServiceInfo] instance or that is empty
     * if no service with the given [serviceName] is offered by this
     */
    suspend fun getServiceInfo(serviceName : String) : Optional<ServiceInfo> {
        val req = requestWithParameter(SERVICE_INFO_CODE, serviceName)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as Optional<ServiceInfo>
    }

    /**
     * Requests the execution of a service and waits for its completion in order to
     * retrieve the result
     * @throws ReplyException if no service with [serviceName] exists
     * @param serviceName the name of the service to execute
     * @return a [ServiceResult] instance that maintains the result
     */
    suspend fun executeService(serviceName: String, vararg param : Any) : ServiceResult {
        val req = requestWithParameters(EXECUTE_SERVICE_CODE, serviceName, *param)
        mainChannel.send(req)
        return req.responseChannel.receive().throwErrorOrGetFirstParameter() as ServiceResult
    }

    /**
     * Requests the execution of a service and returns immediately without waiting
     * for the results.
     * The result will be sent into the channel that is returned by this function.
     * Notice that, after send of the result, the channel will be closed.
     * @throws ReplyException if no service with [serviceName] exists
     * @param serviceName the name of the service to execute
     * @return a [ReceiveChannel] that receives the result at and of the execution
     */
    suspend fun asyncExecuteService(serviceName: String, vararg params : Any) : ReceiveChannel<ServiceResult> {
        val svcResultChan = Channel<ServiceResult>()
        val req = requestWithParameters(ASYNC_EXECUTE_SERVICE_CODE, serviceName, svcResultChan, *params)
        req.responseChannel.close()
        mainChannel.send(req)
        return svcResultChan
    }


}