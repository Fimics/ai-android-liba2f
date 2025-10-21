package com.noetix.core.api

import com.noetix.core.device.DeviceProcessor
import com.noetix.core.entity.ApiResponse
import com.noetix.core.entity.AppVersionInfo
import com.noetix.core.entity.DeviceConfig
import com.noetix.core.entity.HeartBeat
import com.noetix.core.upgrade.UpgradeProcessor
import com.noetix.utils.KLog
import com.noetix.utils.SerialNumber
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Response
import okio.IOException
import org.json.JSONObject

class ApiManager private constructor() {

    private val tag = "ApiManager"
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private val upgradeProcessor = UpgradeProcessor()
    private val deviceProcessor = DeviceProcessor()

    companion object {
        @JvmStatic
        val instance by lazy { ApiManager() }
    }

    fun initialize() {
        KLog.d(tag, "ApiManager initializing...")
        fetchAppInfo()
    }

    private fun fetchAppInfo() {
        KLog.d(tag,"执行 fetchAppInfo")
        executeApiGetRequest<AppVersionInfo>(
            url = ServerUrl.HOST + ServerUrl.URL_APP_INFO,
            requestBody = JSONObject().apply { put("packageName", HttpTask.packageName) },
            successHandler = { data: AppVersionInfo ->
                KLog.d(tag,"fetchAppInfo  数据获取成功 准备更新 app")
                upgradeProcessor.process(data)
            },
            onError = {e ->
                KLog.d(tag,"fetchAppInfo  数据获取失败 准备更新 app")
//                DeviceInfoRequestMonitor.getInstance().monitorRequestApi("获取appInfo 数据失败")
            },
            apiName = "AppInfo"
        )
    }

    fun fetchDeviceInfo() {
        KLog.d(tag,"执行 fetchDeviceInfo 拉取配置信息...")
        executeApiGetRequest<DeviceConfig>(
            url = ServerUrl.HOST + ServerUrl.URL_DEVICE_INFO,
            requestBody = JSONObject().apply { put("sn", SerialNumber.getSN()) },
            successHandler = { data: DeviceConfig -> deviceProcessor.process(data) },
            onError = {e ->},
            apiName = "DeviceInfo"
        )
    }

    fun sendHeartbeat() {
        executeApiPostRequest<HeartBeat>(
            url = ServerUrl.HOST + ServerUrl.URL_HEARTBEAT,
            params = mapOf<String, String>("sn" to HttpTask.sn),
            successHandler = { _: HeartBeat -> }, // 显式指定类型
            onError = {e ->},
            apiName = "Heartbeat"
        )
    }

    private inline fun <reified T : Any> executeApiGetRequest(
        url: String,
        requestBody: JSONObject,
        crossinline successHandler: (T) -> Unit,
        noinline onError: (Exception) -> Unit,
        apiName: String
    ) {
        KLog.d(tag, "Request body -> $requestBody")
        val queryParams = buildQueryParams(requestBody)
        val requestUrl = if (queryParams.isNotEmpty()) "$url?$queryParams" else url

        HttpTask.getInstance().get(requestUrl, object : Listener {
            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, successHandler, onError,apiName)
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }
        })
    }

    private inline fun <reified T : Any> executeApiPostRequest(
        url: String,
        params: Map<String, Any>,
        crossinline successHandler: (T) -> Unit,
        noinline onError: (Exception) -> Unit,
        apiName: String
    ) {
        HttpTask.getInstance().post(url, params, object : Listener {
            override fun onResponse(call: Call, response: Response) {
                handleResponse(response, successHandler,onError, apiName)
            }

            override fun onFailure(call: Call, e: IOException) {
                onError(e)
            }
        })
    }

    private fun buildQueryParams(requestBody: JSONObject): String {
        val queryParams = StringBuilder()
        requestBody.keys().forEach { key ->
            if (queryParams.isNotEmpty()) queryParams.append("&")
            queryParams.append("$key=${requestBody.optString(key)}")
        }
        return queryParams.toString()
    }

    private inline fun <reified T : Any> handleResponse(
        response: Response,
        successHandler: (T) -> Unit,
        onError: (Exception) -> Unit,
        apiName: String
    ) {

        val code = response.code
        KLog.d(tag," responseCode ->$code")
        if (!response.isSuccessful) {
            KLog.e(tag, "$apiName request failed with code: ${response.code}")
            onError(Exception("$apiName request failed with code: ${response.code}"))
            return
        }

        response.body?.string()?.let { responseBody ->
            processApiResponse(responseBody, successHandler, onError,apiName)
        } ?: run {
            KLog.e(tag, "$apiName response body is null")
        }
    }

    private inline fun <reified T : Any> processApiResponse(
        response: String,
        successHandler: (T) -> Unit,
        onError: (Exception) -> Unit,
        apiName: String
    ) {
        try {
            KLog.d(tag,"response -> $response")
            val apiResponse = jsonParser.decodeFromString<ApiResponse<T>>(response)

            if (apiResponse.status == 0) {
                apiResponse.data?.let(successHandler) ?: KLog.e(tag,
                    "$apiName response data is null"
                )
                logApiResponseDetails(apiResponse, apiName)
            } else {
                onError(Exception("apiResponse.status != 0"))
                KLog.e(tag, "$apiName request failed: ${apiResponse.message}")
            }
        } catch (e: Exception) {
            onError(Exception("Failed to parse $apiName response: ${e.message}"))
            KLog.e(tag, "Failed to parse $apiName response: ${e.message}")
        }
    }

    private fun <T> logApiResponseDetails(apiResponse: ApiResponse<T>, apiName: String) {
        KLog.d(tag, "$apiName request status: ${apiResponse.status}")
        KLog.d(tag, "$apiName request message: ${apiResponse.message}")
        KLog.d(tag, "$apiName timestamp: ${apiResponse.timestamp}")
    }
}
