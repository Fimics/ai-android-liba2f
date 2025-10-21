package com.azhon.appupdate.util

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.noetix.utils.KLog
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream

object SystemPackageInstaller {

    private val tag ="SystemPackageInstaller"
    fun installApk(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists()) return false

        // 从 APK 文件中解析 packageName
        val packageName = getPackageNameFromApk(context, apkFile) ?: return false

        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            sessionParams.setInstallReason(PackageManager.INSTALL_REASON_USER)
        }

        try {
            val sessionId = packageInstaller.createSession(sessionParams)
            val session = packageInstaller.openSession(sessionId)

            val inputStream = FileInputStream(apkFile)
            val outputStream: OutputStream = session.openWrite("package", 0, -1)
            inputStream.copyTo(outputStream)
            session.fsync(outputStream)
            outputStream.close()
            inputStream.close()

            session.commit(createIntentSender(context, sessionId))

            // 延迟 5 秒检查是否安装成功
            Handler(Looper.getMainLooper()).postDelayed({
                if (isPackageInstalled(context, packageName)) {
                    launchApp(context, packageName)
                } else {
                    KLog.e(tag, "安装未完成，尝试再次检查")
                }
            }, 5000)

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    private fun getPackageNameFromApk(context: Context, apkFile: File): String? {
        return try {
            val packageInfo = context.packageManager.getPackageArchiveInfo(apkFile.path, 0)
            packageInfo?.packageName
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createIntentSender(context: Context, sessionId: Int): IntentSender {
        val intent = Intent(context, InstallResultReceiver::class.java).apply {
            action = "com.noetix.sdk.INSTALL_COMPLETE"
            putExtra("session_id", sessionId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent.intentSender
    }


    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}