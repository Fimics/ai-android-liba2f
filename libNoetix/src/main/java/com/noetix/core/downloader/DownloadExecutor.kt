package com.noetix.core.downloader

import DownloadableEntity
import android.net.Uri
import com.jeremyliao.liveeventbus.LiveEventBus
import com.noetix.utils.AppGlobals
import com.noetix.utils.KLog
import com.noetix.utils.P
import com.noetix.utils.TaskExecutors
import com.noetix.utils.ZipExtractor
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import java.io.File
import kotlin.collections.mutableListOf

class DownloadExecutor {

    private val groupId = 1000

    private val downloadList = mutableListOf<DownloadableEntity>()
    private val downloadManager by lazy {
        NDownloadManager.getInstance(AppGlobals.getApplication())
    }

    companion object {
        private const val TAG = "DownloadExecutor"
    }

    init {
        downloadManager.registerGroupCallback(groupId, object : GroupCallback {
            override fun onAllCompleted(groupId: Int) {

                TaskExecutors.get().onIOTask {
                    KLog.d(TAG, "所有下载完成 groupId: $groupId")
                    // 1. 先替换目录（data + config）
                    swapDir("robot_data")
                    swapDir("robot_config")
                    swapDir("robot_csv")

                    // 2. 再解压（此时文件已在最终位置 /sdcard/robot_data 下）
                    unZipFiles()
                    KLog.d(TAG,"配置准备完毕 发送Event 初始化sdk... ")
                    LiveEventBus.get<EventConfig>(EventConfig::class.java).post(EventConfig())
                }
            }

            override fun onGroupFailed(groupId: Int, failedDownloads: Map<Int, Error>) {
                KLog.d(TAG, "分组下载失败 groupId: $groupId, 失败项: $failedDownloads")
            }
        })
    }

    /**
     * 把 /sdcard/{dirName}_temp 重命名为 /sdcard/{dirName}
     * 如果临时目录不存在就跳过
     */
    private fun swapDir(dirName: String) {
        val sd = android.os.Environment.getExternalStorageDirectory()
        val oldDir = File(sd, dirName)
        val tempDir = File(sd, "${dirName}_temp")

        if (!tempDir.exists()) {
            KLog.d(TAG, "$dirName 临时目录不存在，跳过")
            return
        }

        // 1. 删除旧目录
        val deleteOk = oldDir.deleteRecursively()
        KLog.d(TAG, "删除旧 $dirName 结果：$deleteOk")

        // 2. 重命名
        val renameOk = tempDir.renameTo(oldDir)
        KLog.d(TAG, "重命名 ${dirName}_temp → $dirName 结果：$renameOk")
    }


    private fun unZipFiles(){
        val downloadSize = downloadList.size
        KLog.d(TAG,"unZipFiles  downloadSize ->$downloadSize")
        downloadList.forEach { it->
            if (it.isUnZip){
                KLog.d(TAG,"解压文件  ${it.toString()}")
                doUnZipFiles(it)
            }
        }
    }

    private fun doUnZipFiles(unZipEntry:DownloadableEntity){

            val zipFile = File(unZipEntry.unZipDir,unZipEntry.fileName)
            val targetDir = File(unZipEntry.unZipDir)
            ZipExtractor.unzip(
                zipFile = zipFile,
                targetDir = targetDir,
                onSuccess = {
                    KLog.d(TAG,"解压成功 "+ unZipEntry.fileName)
                    val eventProgress = DownloadEvent(null,100, DownloadEvent.EVENT_ALL_COMPLETED,requestIndex,requestListSize)
                    postEvent(eventProgress)
                },
                onError={e->
                    KLog.d(TAG,"解压失败 ->"+unZipEntry.fileName + e.message)
                }
            )
    }

    private val requestList = mutableListOf<Request>()
    private var requestIndex=0
    private var requestListSize=0

    fun execute(list: List<DownloadableEntity?>) {
        requestList.clear()
        downloadList.clear()
        KLog.d(TAG,"execute 0")
        DownloadActivity.start(AppGlobals.getApplication())
        KLog.d(TAG,"execute 1")
        list.forEach { it->
            KLog.d(TAG,it.toString())
            if (it!=null && it.shouldDownload){
                downloadList.add(it)
                val downloadRequest = buildDownloadRequest(it)
                requestList.add(downloadRequest)
            }
        }

        val  fetch =downloadManager.getFetch()
        /**
         *全局清空
         * removeAll() / deleteAll() 会把 整个 Fetch 数据库 清掉，影响范围不限于当前这组任务。
         * 如果 App 里还有别的业务模块也在用 Fetch，它们的下载记录会被一起干掉。
         */
        fetch.removeAll()
        fetch.deleteAll()
        requestListSize = requestList.size
        doDownload()
    }


    private fun doDownload(){
        KLog.d(TAG, "requestListSize ->$requestListSize  requestIndex ->$requestIndex")

        if (requestIndex<requestListSize){
            val firstRequest = requestList[requestIndex]
            downloadSingleFile(firstRequest)
        }
    }

    private fun downloadSingleFile(request: Request) {

        KLog.d(TAG, "downloadSingleFile..-------------.")
        //先删除可能的旧记录
        KLog.d(TAG, "groupdId ${request.groupId}  id ->${request.id}")

        val callback = object : DownloadCallback {
            override fun onProgress(request: Request?, downloadId: Int, downloaded: Long, total: Long, progress: Int) {
                KLog.d(TAG, "下载进度: $progress% ($downloaded/$total)")
                KLog.d(TAG, "onProgress requestListSize ->$requestListSize  requestIndex ->$requestIndex")
                val eventProgress = DownloadEvent(request!!,progress, DownloadEvent.EVENT_ITEM_PROGRESS,requestIndex,requestListSize)
                postEvent(eventProgress)
            }

            override fun onCompleted(request: Request?, downloadId: Int) {
                val tags = request?.tag?.split("-")
                val key = tags?.first()
                val value = tags?.last()
                P.get().putString(key,value)
                KLog.d(TAG, "下载完成 ID: $downloadId  tag -> ${request?.tag}")
                KLog.d(TAG, "onCompleted requestListSize ->$requestListSize  requestIndex ->$requestIndex")
                val eventCompleted = DownloadEvent(request!!,100, DownloadEvent.EVENT_ITEM_COMPLETED,requestIndex,requestListSize)
                postEvent(eventCompleted)
                requestIndex++
                doDownload()
            }

            override fun onFailed(request:Request?,downloadId: Int, error: Error) {
                KLog.d(TAG, "下载失败 ID: $downloadId, 错误: ${error.name}")
                KLog.d(TAG, "onFailed requestListSize ->$requestListSize  requestIndex ->$requestIndex")
                val eventFailed = DownloadEvent(request!!,0, DownloadEvent.EVENT_ITEM_FAILED,requestIndex,requestListSize)
                postEvent(eventFailed)
            }
        }

        KLog.d(TAG, "注册回调: ${callback.hashCode()}")
        downloadManager.enqueueDownload(request, groupId, callback)
    }

    private fun postEvent(event: DownloadEvent){
        LiveEventBus.get<DownloadEvent>(DownloadEvent::class.java).post(event)
    }

    private fun buildDownloadRequest(entity: DownloadableEntity): Request{
        val tag = entity.pKey+"-"+entity.timestamp
        val fileName = entity.fileName
        val dir = entity.fileDir
        val file = File(dir, fileName)
        val fileUri = Uri.fromFile(file)
        val request = Request(entity.originalUrl, fileUri)
        request.priority = Priority.HIGH
        request.networkType = NetworkType.ALL
        request.tag=tag
        return request
    }
}