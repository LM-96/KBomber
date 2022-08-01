package kbomberx.concurrency.coroutineserver

fun requestWithParameter(requestCode : Int, param : Any) : CmdServerRequest {
    return CmdServerRequest(requestCode, requestParams = arrayOf(param))
}

fun basicRequest(requestCode: Int) : CmdServerRequest {
    return CmdServerRequest(requestCode)
}

fun requestWithParameters(requestCode: Int, vararg params : Any) : CmdServerRequest {
    return CmdServerRequest(requestCode, requestParams = params)
}

fun asyncRequestWithParameters(requestCode: Int, vararg params : Any) : CmdServerRequest {
    val req = CmdServerRequest(requestCode, requestParams = params)
    req.responseChannel.close()
    return req
}

fun asyncRequestWithParameter(requestCode: Int, param : Any) : CmdServerRequest {
    val req = CmdServerRequest(requestCode, requestParams = arrayOf(param))
    req.responseChannel.close()
    return req
}

fun asyncBasicRequest(requestCode: Int) : CmdServerRequest {
    val req = CmdServerRequest(requestCode)
    req.responseChannel.close()
    return req
}