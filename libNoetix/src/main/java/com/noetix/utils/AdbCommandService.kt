package com.noetix.utils

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import com.noetix.R
import com.noetix.libnoetix.SDKContext
import com.noetix.libnoetix.SDKLog

class AdbCommandService : Service() {

    //adb shell am start-foreground-service -n com.noetix.robotics/com.noetix.utils.AdbCommandService --es command "enable_log" --es args "true" |"false"
    companion object {
        private const val TAG = "AdbCommandService"
        const val NOTIFICATION_CHANNEL_ID = "adb_command_channel"
        const val NOTIFICATION_ID = 1001

        // 启动服务的便捷方法
        fun start(context: Context) {
            val intent = Intent(context, AdbCommandService::class.java)
            SDKLog.d(TAG,"start")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground()
        Log.d(TAG, "ADB command service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SDKLog.d(TAG,"onStartCommand")
        intent?.let { handleCommand(it) }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "ADB Command Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Service for processing ADB commands"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("NewApi")
    private fun startForeground() {
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("ADB Command Processor")
            .setContentText("Ready to receive commands")
            .setSmallIcon(R.drawable.ic_launcher)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun handleCommand(intent: Intent) {
        val command = intent.getStringExtra("command")
        val args = intent.getStringExtra("args")

        Log.d(TAG, "Processing ADB command: $command, args: $args")

        when (command) {
            "enable_log" -> handleEnableLog(args)
            "set_config" -> handleSetConfig(args)
            "reset" -> handleReset()
            else -> Log.w(TAG, "Unknown ADB command: $command")
        }
    }

    private fun handleEnableLog(args: String?) {
        val enable = args.equals("true", ignoreCase = true)
        Log.d(TAG, "Logging ${if (enable) "enabled" else "disabled"}")
        SDKContext.isEnableLog = enable
    }

    private fun handleSetConfig(args: String?) {
        Log.d(TAG, "Updating config with: $args")
        // 实现配置更新逻辑
    }

    private fun handleReset() {
        Log.d(TAG, "Resetting application state")
        // 实现重置逻辑
    }
}