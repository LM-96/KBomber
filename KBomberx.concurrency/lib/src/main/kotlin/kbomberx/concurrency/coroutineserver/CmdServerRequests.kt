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