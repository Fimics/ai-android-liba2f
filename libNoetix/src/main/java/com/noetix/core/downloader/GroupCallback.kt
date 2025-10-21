package com.noetix.core.downloader
import com.tonyodev.fetch2.Error as FetchError

interface GroupCallback {
        fun onAllCompleted(groupId: Int)
        fun onGroupFailed(groupId: Int, failedDownloads: Map<Int, FetchError>)
    }