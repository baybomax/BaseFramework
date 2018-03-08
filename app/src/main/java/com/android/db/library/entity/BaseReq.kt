package com.android.db.library.entity

/**
 * Base request
 *
 * Created by DengBo on 27/02/2018.
 */

abstract class BaseRequest<T: BaseResponse> {
    abstract val url: String
    abstract val resClass: Class<T>
}

abstract class BaseSessionRequest<T: BaseResponse>: BaseRequest<T>() {

}

abstract class BasePageRequest<T: BaseResponse>: BaseRequest<T>() {
    var start = 0
    var end   = 0
}

abstract class BaseSessionPageRequest<T: BaseResponse>: BaseSessionRequest<T>() {
    var start = 0
    var end   = 0
}
