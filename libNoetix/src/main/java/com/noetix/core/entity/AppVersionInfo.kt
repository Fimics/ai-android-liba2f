package com.noetix.core.entity

import kotlinx.serialization.Serializable

@Serializable
data class AppVersionInfo(
    val apkUrl: String,
    val appVersion: String,
    val isForceUpgrade: Boolean,
    val packageName: String
)