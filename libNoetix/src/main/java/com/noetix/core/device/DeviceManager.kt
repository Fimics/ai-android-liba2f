package com.noetix.core.device

import com.noetix.libnoetix.IRobotSDKManager
import com.noetix.utils.KLog
import com.noetix.utils.KeysUtils

class DeviceManager (){

    private val tag ="DeviceManager"

    fun initConfig(config: String){
        KLog.d(tag,"config ->$config")
        KeysUtils.putNativeConfig(config)
    }

    fun lock(isLock:Boolean){
        KLog.d(tag,"lock... $isLock")
        IRobotSDKManager.getInstance().lockDevice(isLock)
    }

}