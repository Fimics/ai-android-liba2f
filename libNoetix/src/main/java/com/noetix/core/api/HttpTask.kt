package com.noetix.core.api

import com.noetix.utils.AppGlobals
import com.noetix.utils.KLog
import com.noetix.utils.PackageUtils
import com.noetix.utils.SerialNumber
import com.noetix.utils.http.OKHttpManager
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class HttpTask {

    private val tag = "ApiFetcher"

    companion object{
        val packageName by lazy { PackageUtils.getCurrentPackageName(AppGlobals.getApplication()) }
        val sn by lazy { SerialNumber.getSN() }
        private val httpTask = HttpTask()

        @JvmStatic
        fun getInstance(): HttpTask{
            return httpTask
        }
    }

    fun post(url: String,params: Map<String, Any>,listener:Listener?){

        KLog.d(tag,"apiTask post  url ->$url")

        try {
            // 1. 构建请求参数
            val formBody = FormBody.Builder().apply {
                params.forEach { (key, value) ->
                    add(key, value.toString())
                }
            }.build()

            // 2. 创建请求
            val request = Request.Builder()
                .url(url)
                .post(formBody)
                .build()

            val client = OKHttpManager.get()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    KLog.d(tag,"onFailure url->$url  e:${e.message} ")
                    listener?.onFailure(call,e)
                }

                override fun onResponse(call: Call, response: Response) {
                    KLog.d(tag,"onResponse url->$url")
                    listener?.onResponse(call,response)
                }
            })

        }catch (e: Exception) {
            KLog.d(tag,"Unexpected error: ${e.message}")
        }
    }

    fun get(url: String, listener: Listener?) {
        KLog.d(tag, "apiTask get url -> $url")

        try {
            // 1. 构建带参数的URL（如果有参数）
            val finalUrl = url

            // 2. 创建GET请求
            val request = Request.Builder()
                .url(finalUrl)
                .get() // 明确指定GET方法
                .build()

            // 3. 使用相同的OkHttpClient实例
            val client = OKHttpManager.get()
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    KLog.d(tag, "GET onFailure url->$finalUrl e:${e.message}")
                    listener?.onFailure(call, e)
                }

                override fun onResponse(call: Call, response: Response) {
                    KLog.d(tag, "GET onResponse url->$finalUrl")
                    listener?.onResponse(call, response)
                }
            })

        } catch (e: Exception) {
            KLog.d(tag, "GET请求异常: ${e.message}")
        }
    }
}