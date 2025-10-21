package com.noetix.utils.http

import com.noetix.utils.KLog
import com.noetix.utils.TaskExecutors
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

object HttpLoader {
    private const val TAG = "HttpLoader"
    @JvmStatic
    fun sendPost(request: Request?, dataListener: DataListener?) :Call{
        val call = OKHttpManager.get().newCall(request!!)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TaskExecutors.get().onMainTask { dataListener?.onFailure(e as Exception) }
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                if (dataListener != null) {
                    try {
                        dataListener.onSuccess(call, response)
                    } catch (e: IOException) {
                        KLog.d(TAG, e.message)
                        //                        throw new RuntimeException(e);
                    }
                }
            }
        })
        return call
    }

}
