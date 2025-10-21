package com.noetix.core.downloader

import DownloadableEntity
import android.annotation.SuppressLint
import com.noetix.core.entity.DeviceConfig
import com.noetix.utils.KLog
import com.noetix.utils.KeysUtils
import com.noetix.utils.P

class DownloadProcessor {

    private val downloadExecutor by lazy {
        DownloadExecutor()
    }
    companion object{
        private const val TAG ="DownloadProcessor"

        private const val FILE_NAME_CSV="default.csv"
        private const val FILE_NAME_RKNN="unitalker_960_simplified.rknn"
        private const val FILE_NAME_TEMPLATE="template.zip"
        private const val FILE_NAME_ZERO="29_servo_config.yaml"

        @SuppressLint("SdCardPath")
        private const val FILE_CONFIG_DIR="/sdcard/robot_config_temp"
        @SuppressLint("SdCardPath")
        private const val FILE_DATA_DIR="/sdcard/robot_data_temp"
        @SuppressLint("SdCardPath")
        private const val FILE_CSV_DIR="/sdcard/robot_csv_temp"

        @SuppressLint("SdCardPath")
        private const val FILE_UNZIP_DIR="/sdcard/robot_config"
    }

    init {
//        KeysUtils.clearTimestamp()
    }

    fun process(config: DeviceConfig){
        KLog.d(TAG,"process invoke...")

        val localCsvTimestamp = P.get().getString(KeysUtils.TIME_CSV,"0")
        val localRknnTimestamp = P.get().getString(KeysUtils.TIME_RKNN,"0")
        val localTemplateTimestamp = P.get().getString(KeysUtils.TIME_TEMPLATE,"0")
        val localZeroTimestamp = P.get().getString(KeysUtils.TIME_ZERO,"0")

        val csvDownloadable = DownloadableEntity.fromUrl(config.defaultActionUrl,localCsvTimestamp,KeysUtils.TIME_CSV,FILE_NAME_CSV,FILE_CSV_DIR,false,FILE_UNZIP_DIR)
        val templateDownloadable = DownloadableEntity.fromUrl(config.templateUrl,localTemplateTimestamp,KeysUtils.TIME_TEMPLATE,FILE_NAME_TEMPLATE,FILE_CONFIG_DIR,true,FILE_UNZIP_DIR)
        val zeroDownloadable = DownloadableEntity.fromUrl(config.zeroConfigUrl,localZeroTimestamp,KeysUtils.TIME_ZERO,FILE_NAME_ZERO,FILE_CONFIG_DIR,false,FILE_UNZIP_DIR)
        val rknnDownloadable = DownloadableEntity.fromUrl(config.rknnUrl,localRknnTimestamp,KeysUtils.TIME_RKNN,FILE_NAME_RKNN,FILE_DATA_DIR,false,FILE_UNZIP_DIR)

        val downloadList = listOf(csvDownloadable,rknnDownloadable,templateDownloadable,zeroDownloadable)
        downloadExecutor.execute(downloadList)
    }

}