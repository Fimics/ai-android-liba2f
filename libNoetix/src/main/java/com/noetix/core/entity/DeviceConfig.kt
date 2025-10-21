package com.noetix.core.entity
import kotlinx.serialization.Serializable

@Serializable
data class DeviceConfig(
    val cppSdkConfig: String,
    val createBy: String,
    val createTime: String,
    val defaultActionUrl: String,
    val isLock: Int,
    val onlineStatus: Int,
    val onlineTimestamp: Long,
    val productType: Int,
    val remark: String,
    val rknnUrl: String,
    val sn: String,
    val templateUrl: String,
    val updateBy: String,
    val updateTime: String,
    val zeroConfigUrl: String
) {
    @Serializable
    data class CppSdkConfig(
        val nickStructureType: Int
    )

    override fun toString(): String {
        return """
        |DeviceConfig {
        |  cppSdkConfig: '$cppSdkConfig',
        |  createBy: '$createBy',
        |  createTime: '$createTime',
        |  defaultActionUrl: '$defaultActionUrl',
        |  isLock: $isLock,
        |  onlineStatus: $onlineStatus,
        |  onlineTimestamp: $onlineTimestamp,
        |  productType: $productType,
        |  remark: '$remark',
        |  rknnUrl: '$rknnUrl',
        |  sn: '$sn',
        |  templateUrl: '$templateUrl',
        |  updateBy: '$updateBy',
        |  updateTime: '$updateTime',
        |  zeroConfigUrl: '$zeroConfigUrl'
        |}
        """.trimMargin()
    }
}