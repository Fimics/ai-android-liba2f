package com.noetix.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.reflect.Method

class SerialNumber {
    /**
     * 临时属性
     * 保存sn到系统属性
     * adb shell setprop noetix_sn  "NTX_HOBBS_v1_BJ01_250925_001"
     * 获取sn 属性
     * adb shell getprop noetix_sn
     *
     *
     * Android 提供了 persist.前缀，可以自动持久化属性值到 /data/property/
     *
     * adb shell setprop persist.noetix_sn "NTX_HOBBS_v1_BJ01_250925_001"
     * adb shell getprop persist.noetix_sn
     */

    companion object {
        private val snKey = "noetix_sn"
        private var sn = ""

        @JvmStatic
        fun getSN(): String {
                sn = readPropViaShell()
            return sn
        }

        fun readPropViaShell(): String {
            return try {
                val process = Runtime.getRuntime().exec("getprop persist.noetix_sn")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                reader.readLine().trim() ?: "default_value_here"
            } catch (e: Exception) {
                e.printStackTrace()
                "error_value"
            }
        }

    }

}