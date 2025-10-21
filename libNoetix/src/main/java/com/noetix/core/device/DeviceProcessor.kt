package com.noetix.core.device

import com.noetix.core.downloader.DownloadProcessor
import com.noetix.core.entity.DeviceConfig
import com.noetix.utils.KLog

class DeviceProcessor {

    private val deviceManager = DeviceManager()
    private val downloadProcessor = DownloadProcessor()

    companion object{
        private const val TAG ="DeviceProcessor"
    }

    fun process(config: DeviceConfig){
        KLog.d(TAG,"deviceConfig ->${config.toString()}")

        val nativeConfig = config.cppSdkConfig
        val isLock = config.isLock==1

        deviceManager.initConfig(nativeConfig)
        deviceManager.lock(isLock)
        downloadProcessor.process(config)
    }
}