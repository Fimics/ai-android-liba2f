package com.noetix.core.upgrade

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import com.azhon.appupdate.listener.OnDownloadListener
import com.azhon.appupdate.manager.DownloadManager
import com.azhon.appupdate.util.ApkUtil
import com.noetix.R
import com.noetix.core.entity.AppVersionInfo
import com.noetix.utils.AppGlobals
import com.noetix.utils.KLog
import com.noetix.utils.PackageUtils
import java.io.File
import java.lang.ref.WeakReference

class NAppUpdater private constructor(activity: Activity) {



    // 使用 WeakReference 防止内存泄漏
    private val activityRef = WeakReference<Activity>(activity)
    private var downloadApkName = "appupdate.apk"
    private var manager: DownloadManager? = null

    companion object {
        private const val TAG = "NoetixAppUpdater"
        @SuppressLint("StaticFieldLeak")
        private var instance: NAppUpdater? = null

        // 静态方法获取实例
        @JvmStatic
        fun createInstance(activity: Activity): NAppUpdater {
            return instance ?: synchronized(this) {
                instance ?: NAppUpdater(activity).also { instance = it }
            }
        }

        @JvmStatic
        fun getInstance(): NAppUpdater?{
           return instance
        }
    }


    fun upgrade(appInfo: AppVersionInfo) {
        val packageName =PackageUtils.getCurrentPackageName(AppGlobals.getApplication())
        downloadApkName =packageName.split(".").last()+".apk"
        KLog.d(TAG, "upgrade downloadApkName ->$downloadApkName")
        if (appInfo.isForceUpgrade) {
            forceUpgrade(appInfo)
        }
    }

    private fun forceUpgrade(appInfo: AppVersionInfo) {
        val activity = activityRef.get()
        //delete downloaded old Apk
        val result = ApkUtil.deleteOldApk(AppGlobals.getApplication(), "${activity?.externalCacheDir?.path}/$downloadApkName")
        startUpdate(appInfo)
    }


    private fun startUpdate(appInfo: AppVersionInfo) {
        KLog.d(TAG,"startUpdate")
        val versionCode = appInfo.appVersion.replace(".", "").toInt()
        KLog.d(TAG,"new versionCode->$versionCode")
        val activity = activityRef.get()
        manager = DownloadManager.Builder(activity!!).run {
            apkUrl(appInfo.apkUrl)
            apkName(downloadApkName)
            apkVersionCode(versionCode)
            smallIcon(R.mipmap.ic_launcher)
            onDownloadListener(downloadListener)
            apkDescription("发现新版本 ${appInfo.appVersion}")
            build()
        }
        manager?.download()
    }

    private val downloadListener = DownloadListener()

    private class DownloadListener: OnDownloadListener{
        override fun start() {
            KLog.d(TAG,"start")
        }

        override fun downloading(max: Int, progress: Int) {
            KLog.d(TAG,"downloading progress->$progress")
        }

        override fun done(apk: File) {
            apk.let {
                KLog.d(TAG,"done  path -> ${it.path}")
            }
        }

        override fun cancel() {
            KLog.d(TAG,"cancel")
        }

        override fun error(e: Throwable) {
            KLog.d(TAG,"error ->${e.message}")
        }

    }

    private fun hasInstallPermission() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppGlobals.getApplication().packageManager.canRequestPackageInstalls()
        } else {
            true
        }

}