package com.noetix.sample

import android.app.Application
import com.noetix.libnoetix.IRobotSDKManager

class NoeTixApp:Application() {

    private val tag= "NoeTixApp"
    override fun onCreate() {
        super.onCreate()
        CANShell.executeCanCommands()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
    }

}