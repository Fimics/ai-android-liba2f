package com.noetix.core.entity

import kotlinx.serialization.Serializable

// 外层响应数据结构
@Serializable
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val timestamp: String,
    val data: T?
)