package com.android.db.library.entity

/**
 * Base response
 *
 * Created by DengBo on 27/02/2018.
 */

abstract class BaseResponse {
    var code = 0
    var msg  = ""
}

abstract class BasePageResponse: BaseResponse() {
    var start = 0
    var count = 0
    var total = 0
}
