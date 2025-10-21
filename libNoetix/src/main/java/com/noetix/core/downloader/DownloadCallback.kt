package com.noetix.core.downloader

import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2.Error as FetchError

interface DownloadCallback {
        fun onProgress(
            request: Request?,
            downloadId: Int, downloaded: Long, total: Long, progress: Int
        )
        fun onCompleted(request: Request?, downloadId: Int)
        fun onFailed(request: Request?,downloadId: Int, error: FetchError)
    }