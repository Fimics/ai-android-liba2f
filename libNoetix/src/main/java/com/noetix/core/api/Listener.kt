package com.noetix.core.api

import okhttp3.Call
import okhttp3.Response
import okio.IOException

interface Listener {

    fun onResponse(call: Call, response: Response)
    fun onFailure(call: Call, e: IOException)
}