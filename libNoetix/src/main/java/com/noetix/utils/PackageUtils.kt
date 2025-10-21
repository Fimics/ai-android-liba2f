package com.noetix.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build

/**
 * Android 包管理工具类
 * 功能包含：
 * 1. 获取当前/其他应用包名
 * 2. 获取应用信息/版本
 * 3. 检查应用安装状态
 * 4. 获取应用图标/名称
 * 5. 解析APK文件
 * 6. 兼容Android 11+权限限制
 */
object PackageUtils {

    // ========================
    // 获取包名相关
    // ========================

    /**
     * 获取当前应用的包名
     */
    fun getCurrentPackageName(context: Context): String {
        return context.packageName
    }

    /**
     * 获取指定应用的包名（通过ComponentName）
     * @param className 完整类名（如 "com.example.MainActivity"）
     */
    fun getPackageNameByClass(context: Context, className: String): String? {
        return try {
            val component = context.packageManager.getLaunchIntentForPackage(className)?.component
            component?.packageName
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从APK文件中提取包名
     */
    fun getPackageNameFromApk(context: Context, apkPath: String): String? {
        return getApkInfo(context, apkPath)?.packageName
    }

    // ========================
    // 基础信息获取
    // ========================

    /**
     * 获取当前应用的PackageInfo
     */
    fun getCurrentPackageInfo(context: Context): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_META_DATA)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * 获取当前应用版本号（兼容API 28+）
     */
    fun getCurrentVersionCode(context: Context): Long {
        return getCurrentPackageInfo(context)?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) it.longVersionCode else it.versionCode.toLong()
        } ?: 0
    }

    /**
     * 获取当前应用版本名称
     */
    fun getCurrentVersionName(context: Context): String {
        return getCurrentPackageInfo(context)?.versionName ?: ""
    }

    // ========================
    // 其他应用操作
    // ========================

    /**
     * 获取指定包名的应用信息
     * @param flags 可选：PackageManager.GET_ACTIVITIES等
     */
    fun getPackageInfo(
        context: Context,
        packageName: String,
        flags: Int = 0
    ): PackageInfo? {
        return try {
            context.packageManager.getPackageInfo(packageName, flags)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /**
     * 获取应用图标
     */
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取应用名称
     */
    fun getAppName(context: Context, packageName: String): String? {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            null
        }
    }

    // ========================
    // 应用状态检查
    // ========================

    /**
     * 检查应用是否安装
     */
    fun isAppInstalled(context: Context, packageName: String): Boolean {
        return getPackageInfo(context, packageName) != null
    }

    /**
     * 检查应用是否启用
     */
    fun isAppEnabled(context: Context, packageName: String): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            appInfo.enabled
        } catch (e: Exception) {
            false
        }
    }

    // ========================
    // APK文件解析
    // ========================

    /**
     * 解析APK文件信息（无需安装）
     */
    fun getApkInfo(context: Context, apkPath: String): PackageInfo? {
        return context.packageManager.getPackageArchiveInfo(
            apkPath,
            PackageManager.GET_ACTIVITIES or PackageManager.GET_META_DATA
        )
    }

    // ========================
    // 批量操作
    // ========================

    /**
     * 获取所有已安装应用包名（受Android 11+限制）
     */
    fun getAllInstalledPackages(context: Context): List<String> {
        return context.packageManager.getInstalledPackages(0).map { it.packageName }
    }

    /**
     * 获取所有已安装应用信息（受Android 11+限制）
     */
    fun getAllInstalledPackageInfo(context: Context): List<PackageInfo> {
        return context.packageManager.getInstalledPackages(
            PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_META_DATA
        )
    }
}