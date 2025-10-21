package com.noetix.core.downloader

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.noetix.utils.KLog
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import com.tonyodev.fetch2.Error as FetchError
import java.util.concurrent.ConcurrentHashMap

@Suppress("unused")
class NDownloadManager private constructor(context: Context) {
    companion object {
        private const val TAG = "DownloadManager"
        @Volatile
        private var instance: NDownloadManager? = null

        fun getInstance(context: Context): NDownloadManager {
            return instance ?: synchronized(this) {
                instance ?: NDownloadManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val fetch: Fetch
    private val groupCallbacks = ConcurrentHashMap<Int, GroupCallback>()
    private val downloadCallbacks = ConcurrentHashMap<Int, DownloadCallback>()
    private val downloadIdToRequestMap = ConcurrentHashMap<Int, Request>()
    private var isListening = false
    private val pendingGroupChecks = mutableSetOf<Int>()
    private val downloadIdToGroupIdMap = ConcurrentHashMap<Int, Int>()
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        val config = FetchConfiguration.Builder(context)
            .setDownloadConcurrentLimit(4)
            .setHttpDownloader(HttpUrlConnectionDownloader())
            .setAutoRetryMaxAttempts(3)
            .build()
        fetch = Fetch.getInstance(config)
    }

    fun enqueueDownload(request: Request, groupId: Int = 0, callback: DownloadCallback? = null) {
        request.groupId = groupId
        downloadIdToGroupIdMap[request.id] = groupId
        // 1. 缓存 request
        downloadIdToRequestMap[request.id] = request

        KLog.d(TAG, "开始下载 ID: ${request.id} URL: ${request.url}")

        callback?.let { cb ->
            downloadCallbacks[request.id] = cb
            KLog.d(TAG, "成功添加回调 ID: ${request.id} 回调: ${cb.hashCode()}")
        }

        fetch.enqueue(request,
            { result ->
                KLog.d(TAG, "下载已加入队列 ID: ${request.id}")
                startListening()
            },
            { error ->
                downloadCallbacks.remove(request.id)
                KLog.e(TAG, "下载失败 ID: ${request.id} 错误: ${error.name}")
                notifyDownloadFailed(request.id, error)
            }
        )
    }

    fun pauseDownload(downloadId: Int) {
        fetch.pause(downloadId)
        KLog.d(TAG, "pauseDownload downloadId ->$downloadId")
    }

    fun resumeDownload(downloadId: Int) {
        fetch.resume(downloadId)
        KLog.d(TAG, "resumeDownload downloadId ->$downloadId")
    }

    fun cancelDownload(downloadId: Int) {
        fetch.cancel(downloadId)
        downloadIdToGroupIdMap.remove(downloadId)
        downloadCallbacks.remove(downloadId)
        KLog.d(TAG, "cancelDownload downloadId ->$downloadId")
    }

    fun removeDownload(downloadId: Int) {
        fetch.remove(downloadId)
        downloadIdToGroupIdMap.remove(downloadId)
        downloadCallbacks.remove(downloadId)
        KLog.d(TAG, "removeDownload downloadId ->$downloadId")
    }

    fun getDownloadsInGroup(groupId: Int, func: Func<List<Download>>): Fetch {
        return fetch.getDownloadsInGroup(groupId, func)
    }

    fun registerGroupCallback(groupId: Int, callback: GroupCallback) {
        groupCallbacks[groupId] = callback
        startListening()
    }

    fun unregisterGroupCallback(groupId: Int, callback: GroupCallback) {
        if (groupCallbacks[groupId] == callback) {
            groupCallbacks.remove(groupId)
        }
    }

    fun registerDownloadCallback(downloadId: Int, callback: DownloadCallback) {
        downloadCallbacks[downloadId] = callback
        startListening()
    }

    fun unregisterDownloadCallback(downloadId: Int, callback: DownloadCallback) {
        if (downloadCallbacks[downloadId] == callback) {
            downloadCallbacks.remove(downloadId)
        }
    }

    fun getDownloadStatus(downloadId: Int, callback: (Download?) -> Unit) {
        fetch.getDownload(downloadId, callback)
    }

    fun getGroupDownloads(groupId: Int, callback: (List<Download>) -> Unit) {
        fetch.getDownloadsInGroup(groupId, callback)
    }

    fun getFetch(): Fetch {
        return fetch
    }

    fun shutdown() {
        stopListening()
        fetch.close()
        groupCallbacks.clear()
        downloadCallbacks.clear()
        pendingGroupChecks.clear()
        downloadIdToGroupIdMap.clear()
        instance = null
        KLog.d(TAG, "shutdown")
    }

    private fun startListening() {
        if (!isListening) {
            fetch.addListener(fetchListener)
            isListening = true
            KLog.d(TAG, "startListening")
        }
    }

    private fun stopListening() {
        if (isListening) {
            fetch.removeListener(fetchListener)
            isListening = false
            KLog.d(TAG, "stopListening")
        }
    }

    private fun checkGroupStatus(groupId: Int) {
        if (pendingGroupChecks.contains(groupId)) return

        pendingGroupChecks.add(groupId)

        mainHandler.postDelayed({
            pendingGroupChecks.remove(groupId)
            performGroupStatusCheck(groupId)
        }, 300)
    }

    private fun performGroupStatusCheck(groupId: Int) {
        fetch.getDownloadsInGroup(groupId) { downloads ->
            val total = downloads.size
            if (total == 0) return@getDownloadsInGroup

            val completed = downloads.count { it.status == Status.COMPLETED }
            val hasFailed = downloads.any { it.status == Status.FAILED }

            when {
                completed == total -> notifyGroupSuccess(groupId)
                hasFailed -> notifyGroupFailure(groupId, downloads)
            }
        }
    }

    private fun notifyGroupSuccess(groupId: Int) {
        groupCallbacks[groupId]?.onAllCompleted(groupId)
    }

    private fun notifyGroupFailure(groupId: Int, downloads: List<Download>) {
        val failedDownloads = downloads
            .filter { it.status == Status.FAILED }
            .associate { it.id to it.error }

        groupCallbacks[groupId]?.onGroupFailed(groupId, failedDownloads)
    }

    private fun notifyDownloadProgress(download: Download) {
        val callback = downloadCallbacks[download.id]
        val request  = downloadIdToRequestMap[download.id]
        KLog.d(TAG,"notifyDownloadProgress - callback ->$callback")
        if (callback != null) {
            KLog.d(TAG, "找到回调 ID: ${download.id} 回调: ${callback.hashCode()}")
            callback.onProgress(
                request,
                download.id,
                download.downloaded,
                download.total,
                download.progress
            )
        } else {
            KLog.w(TAG, "未找到回调 ID: ${download.id}")
        }
    }

    private fun notifyDownloadCompleted(download: Download) {
        val callback =downloadCallbacks[download.id]
//        KLog.d(TAG,"notifyDownloadCompleted callback->$callback")
        val request  = downloadIdToRequestMap[download.id]
        callback?.onCompleted(request,download.id)
        // 延迟清理回调
        mainHandler.postDelayed({
            downloadCallbacks.remove(download.id)
            KLog.d(TAG, "已清理回调 ID: ${download.id}")
        }, 1000) // 1秒延迟确保所有通知完成
    }

    private fun notifyDownloadFailed(downloadId: Int, error: FetchError) {
        val request  = downloadIdToRequestMap[downloadId]
        downloadCallbacks[downloadId]?.onFailed(request,downloadId, error)
        downloadCallbacks.remove(downloadId)
    }

    private val fetchListener = object : AbstractFetchListener() {
        override fun onCompleted(download: Download) {
            KLog.d(TAG, "onCompleted called for download ID: ${download.id}")
            notifyDownloadCompleted(download)
            handleStatusChange(download)
        }

        override fun onError(download: Download, error: FetchError, throwable: Throwable?) {
            KLog.d(TAG, "onError called for download ID: ${download.id}, error: $error")
            notifyDownloadFailed(download.id, error)
            handleStatusChange(download)
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            KLog.d(TAG, "onProgress called for download ID: ${download.id}, progress: ${download.progress}")
            notifyDownloadProgress(download)
        }

        private fun handleStatusChange(download: Download) {
            val groupId = downloadIdToGroupIdMap[download.id] ?: return
            checkGroupStatus(groupId)
            KLog.d(TAG, "handleStatusChange called for download ID: ${download.id}")
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            KLog.d(TAG, "onQueued called for download ID: ${download.id}, waitingOnNetwork: $waitingOnNetwork")
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)
            KLog.d(TAG, "onPaused called for download ID: ${download.id}")
        }

        override fun onResumed(download: Download) {
            super.onResumed(download)
            KLog.d(TAG, "onResumed called for download ID: ${download.id}")
        }

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            super.onStarted(download, downloadBlocks, totalBlocks)
            KLog.d(TAG, "onStarted called for download ID: ${download.id}, blockSize ->${downloadBlocks.size} totalBlocks ->$totalBlocks")
        }

        override fun onWaitingNetwork(download: Download) {
            super.onWaitingNetwork(download)
            KLog.d(TAG, "onWaitingNetwork called for download ID: ${download.id}")
        }

        override fun onAdded(download: Download) {
            super.onAdded(download)
            KLog.d(TAG, "onAdded called for download ID: ${download.id}")
        }

        override fun onRemoved(download: Download) {
            super.onRemoved(download)
            KLog.d(TAG, "onRemoved called for download ID: ${download.id}")
        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)
            KLog.d(TAG, "onDeleted called for download ID: ${download.id}")
        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)
            KLog.d(TAG, "onCancelled called for download ID: ${download.id}")
        }

        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
            super.onDownloadBlockUpdated(download, downloadBlock, totalBlocks)
        }
    }

    private abstract inner class AbstractFetchListener : FetchListener {
        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {}
        override fun onPaused(download: Download) {}
        override fun onResumed(download: Download) {}
        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {}
        override fun onWaitingNetwork(download: Download) {}
        override fun onAdded(download: Download) {}
        override fun onRemoved(download: Download) {
//            downloadIdToGroupIdMap.remove(download.id)
//            downloadCallbacks.remove(download.id)
        }
        override fun onDeleted(download: Download) {
//            downloadIdToGroupIdMap.remove(download.id)
//            downloadCallbacks.remove(download.id)
        }
        override fun onCancelled(download: Download) {
//            downloadIdToGroupIdMap.remove(download.id)
//            downloadCallbacks.remove(download.id)
        }
        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {}
    }
}