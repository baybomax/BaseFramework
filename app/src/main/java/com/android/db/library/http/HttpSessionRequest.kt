package com.android.db.library.http

import com.android.db.library.entity.BaseResponse
import com.android.db.library.entity.BaseSessionRequest

/**
 * Http session request
 *
 * Created by DengBo on 01/03/2018.
 */

abstract class HttpSessionRequest {

    companion object {
        val TAG = "HttpSessionRequest"
    }

    open var aesKey = "PEUPNxKsDRDzLxPQ"

    abstract var host: String

    abstract fun <T: BaseResponse> setSession(request: BaseSessionRequest<T>)

    abstract fun <T: BaseResponse> accessSession(response: T, access: () -> Unit)

    fun <T: BaseSessionRequest<S>, S: BaseResponse> sessionRequest(request: T,
                                                                   listener: (S) -> Unit,
                                                                   error: () -> Unit,
                                                                   _host: String = host,
                                                                   _aesKey: String = aesKey,
                                                                   encrypt: Boolean = false) {
        setSession(request)
        HttpRequest.request(request, _host, _aesKey, { res->
            accessSession(res) {
                listener(res)
            }
        }, {
            error()
        }, encrypt)
    }

}
