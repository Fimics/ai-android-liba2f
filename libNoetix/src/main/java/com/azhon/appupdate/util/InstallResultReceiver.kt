package com.azhon.appupdate.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import android.widget.Toast
import com.noetix.utils.KLog

class InstallResultReceiver : BroadcastReceiver() {

    private val tag="InstallResultReceiver"
    override fun onReceive(context: Context, intent: Intent) {
        Log.e("INSTALL_RESULT", ">>> onReceive action=${intent.action} extras=${intent.extras}")

        KLog.d(tag, "收到广播: ${intent.action}")

        // 1. 处理系统广播（ACTION_PACKAGE_ADDED）
        if (intent.action == Intent.ACTION_PACKAGE_ADDED || intent.action == Intent.ACTION_PACKAGE_REPLACED) {
            val packageName = intent.data?.schemeSpecificPart
            if (packageName != null) {
                KLog.d(tag, "系统广播：安装成功，启动应用: $packageName")
                launchApp(context, packageName)
            }
            return
        }

        // 2. 处理自定义广播（com.noetix.sdk.INSTALL_COMPLETE）
        if (intent.action != "com.noetix.sdk.INSTALL_COMPLETE") return

        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val packageName = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                KLog.d(tag, "PackageInstaller 广播：安装成功: $packageName")
                if (packageName != null) launchApp(context, packageName)
            }
            else -> {
                KLog.e(tag, "安装失败: status=$status")
                Toast.makeText(context, "安装失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            } else {
                KLog.e(tag, "无法获取启动 Intent: $packageName")
                Toast.makeText(context, "无法启动应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            KLog.e(tag, "启动应用失败: ${e.message}")
        }
    }
}