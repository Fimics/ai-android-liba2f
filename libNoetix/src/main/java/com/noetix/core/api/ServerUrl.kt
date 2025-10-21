package com.noetix.core.api

object ServerUrl {
    //测试设备号  NTX_HOBBS_v1_BJ01_250925_001
//    private const val HOST_RELEASE = "http://192.168.100.7:8080"
//    private const val HOST_DEBUG = "http://192.168.100.7:8080"

    private const val HOST_RELEASE = "http://192.168.102.94:8080"
    private const val HOST_DEBUG = "http://192.168.102.94:8080"
    const val HOST = HOST_DEBUG

    const val URL_APP_INFO="/app/getAPPInfo"
    const val URL_DEVICE_INFO="/app/searchDeviceInfo"
    const val URL_HEARTBEAT="/app/setDeviceOnline"
}