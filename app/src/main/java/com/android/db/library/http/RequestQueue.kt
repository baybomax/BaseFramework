package com.android.db.library.http

import android.content.Context
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

/**
 * Request Queue
 *
 * Created by DengBo on 27/02/2018.
 */

class RequestQueue private constructor(context: Context){

    private val requestQueue = Volley.newRequestQueue(context)

    init {
        requestQueue.start()
    }

    companion object {
        var requestQueue: RequestQueue? = null

        /**
         * Create instance
         *
         * @param context
         */
        fun create(context: Context) {
            if (null == requestQueue) {
                requestQueue = RequestQueue(context)
            }
        }
    }

    /**
     * Put request to requestQueue
     *
     * @param request
     */
    fun addRequest(request: StringRequest) {
        requestQueue.add(request)
    }
}
