package com.noetix.core.upgrade

import android.text.TextUtils
import android.util.Patterns
import com.noetix.core.entity.AppVersionInfo
import com.noetix.utils.AppGlobals
import com.noetix.utils.KLog
import com.noetix.utils.PackageUtils


class UpgradeProcessor {


    companion object{
        private const val TAG="UpgradeProcessor"
    }

    fun process(info: AppVersionInfo){
        val version = info.appVersion
//        val url =info.apkUrl
        val url = "http://114.251.228.195/apps/demo_2.0.0.5_202509281958_debug.apk"
        val packageName =info.packageName
        val isForceUpgrade =info.isForceUpgrade

        KLog.d(TAG," remote version ->$version url ->$url packageName ->$packageName isForceUpgrade ->$isForceUpgrade")

        val localPackageName = PackageUtils.getCurrentPackageName(AppGlobals.getApplication())
        val versionCode =PackageUtils.getCurrentVersionCode(AppGlobals.getApplication())
        val versionName =PackageUtils.getCurrentVersionName(AppGlobals.getApplication())

        KLog.d(TAG,"local packageName ->$localPackageName versionCode ->$versionCode versionName ->$versionName")

        val versionResult  = compareVersions(version,versionName)
        val packageNameResult = comparePackageName(packageName,localPackageName)
        val urlResult = isValidHttpUrl(url)

        KLog.d(TAG,"compare -> versionResult ->$versionResult  packageNameResult ->$packageNameResult  urlResult->  $urlResult")

        if (versionResult==1 && packageNameResult && urlResult){
            KLog.d(TAG,"noetixAppUpdater.upgrade(info)")
            NAppUpdater.getInstance()?.upgrade(info)
        }
    }

    /**
     * 比较两个版本号字符串的大小
     * @param version1 版本号1 (如 "1.0.0.6")
     * @param version2 版本号2 (如 "1.0.0.5")
     * @return 返回比较结果：
     *         -1 -> version1 < version2
     *          0 -> version1 == version2
     *          1 -> version1 > version2
     */
    private fun compareVersions(version1: String, version2: String): Int {

        if (TextUtils.isEmpty(version1)|| TextUtils.isEmpty(version2)) return -1

        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(v1Parts.size, v2Parts.size)

        for (i in 0 until maxLength) {
            val v1 = v1Parts.getOrElse(i) { 0 }
            val v2 = v2Parts.getOrElse(i) { 0 }

            when {
                v1 < v2 -> return -1
                v1 > v2 -> return 1
            }
        }

        return 0 // 所有部分均相等
    }

    private fun comparePackageName(remotePackageName: String,localPackageName: String ): Boolean{
       if (TextUtils.isEmpty(remotePackageName)|| TextUtils.isEmpty(localPackageName)) return false
        return remotePackageName == localPackageName
    }

    private fun isValidHttpUrl(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches() &&
                (url.startsWith("http://", ignoreCase = true) ||
                        url.startsWith("https://", ignoreCase = true))
    }

}