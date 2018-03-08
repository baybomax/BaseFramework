package com.android.db.library.http

import android.util.Log
import com.android.db.library.entity.BaseRequest
import com.android.db.library.entity.BaseResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson

/**
 * Http Request
 *
 * Created by DengBo on 27/02/2018.
 */

class HttpRequest {

    companion object {
        private val TAG = HttpSessionRequest.TAG

        /**
         * request
         *
         * @param request  request entity
         * @param listener success block
         * @param error    error block
         * @param host     host string
         * @param aesKey   aes key
         * @param encrypt  T/F
         */
        fun <T: BaseRequest<S>, S: BaseResponse> request(request: T,
                                                         host: String,
                                                         aesKey: String,
                                                         listener: (S) -> Unit,
                                                         error: () -> Unit,
                                                         encrypt: Boolean = false) {
            //
            Log.v(TAG, "url is ${host + request.url}")
            Log.v(TAG, "request is ${Gson().toJson(request)}")

            val reqJson = if (encrypt)
                AESCipher(aesKey).encrypt(Gson().toJson(request))
            else
                Gson().toJson(request)

            RequestQueue.requestQueue?.addRequest(
                    object : StringRequest(
                            Request.Method.POST,
                            host + request.url,
                            Response.Listener {
                                val resJson = if (encrypt) AESCipher(aesKey).decipher(it) else it
                                Log.v(TAG, "response is $resJson")
                                val response = Gson().fromJson(resJson, request.resClass)
                                listener(response)
                            },
                            Response.ErrorListener {
                                Log.v(TAG, "error is $it")
                                error()
                            }
                    ) {
                        override fun getBody(): ByteArray {
                            return reqJson.toByteArray()
                        }
                    }
            )
        }
    }
}
