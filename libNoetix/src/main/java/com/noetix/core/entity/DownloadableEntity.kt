import android.annotation.SuppressLint
import java.util.*
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat

/**
 * 可下载资源实体类
 * @param originalUrl 原始URL
 * @param timestamp 从URL中提取的时间戳
 * @param formattedTime 格式化后的时间字符串
 * @param fileType 文件类型（根据URL后缀判断）
 * @param resourceType 资源类型（根据URL路径判断）
 */
@Serializable
data class DownloadableEntity(
    val originalUrl: String,
    val timestamp: String,
    val lastTimestamp: String?,
    val formattedTime: String,
    val fileType: String,
    val resourceType: String,
    val domain: String,
    val pathSegments: List<String>,
    val isTimestampValid: Boolean,
    val shouldDownload: Boolean,
    val isUnZip : Boolean = false,
    val fileName: String,
    val fileDir: String,
    val unZipDir: String,
    val pKey:String,
    val lastModified: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * 从URL创建DownloadableEntity
         */
        fun fromUrl(url: String,lastTimestamp: String?,pKey:String,fileName: String,fileDir: String,isUnZip: Boolean,unZipDir: String): DownloadableEntity? {
            val timestamp = extractTimestampFromUrl(url) ?: return null
            val fileType = extractFileType(url)
            val resourceType = extractResourceType(url)
            val domain = extractDomain(url)
            val pathSegments = extractPathSegments(url)
            val shouldDownload =shouldDownload(timestamp,lastTimestamp)

            return DownloadableEntity(
                originalUrl = url,
                timestamp = timestamp,
                lastTimestamp = lastTimestamp,
                formattedTime = formatTimestamp(timestamp),
                fileType = fileType,
                resourceType = resourceType,
                domain = domain,
                pathSegments = pathSegments,
                shouldDownload =shouldDownload ,
                isUnZip = isUnZip,
                fileName = fileName,
                fileDir = fileDir,
                unZipDir = unZipDir,
                pKey = pKey,
                isTimestampValid = timestamp.length in listOf(10, 13) // 10位秒或13位毫秒
            )
        }

        private fun extractTimestampFromUrl(url: String): String? {
            val pattern = Regex("""_(\d{10}|\d{13})(?:\.\w+|/|$)""")
            return pattern.find(url)?.groupValues?.get(1)
        }

        @SuppressLint("SimpleDateFormat")
        private fun formatTimestamp(timestamp: String): String {
            return try {
                val milliseconds = timestamp.toLong().let {
                    if (it.toString().length == 10) it * 1000 else it
                }
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Date(milliseconds))
            } catch (e: Exception) {
                "Invalid timestamp"
            }
        }

        private fun extractFileType(url: String): String {
//            url.substringAfterLast('.').takeIf { it != url } ?: "unknown"
             return url.substringAfterLast('.').split("?").get(0).takeIf { it!=url }?:"unknown"
        }

        private fun extractResourceType(url: String): String {
            return url.substringAfterLast('/')
                .substringBefore('_')
                .takeIf { it.isNotEmpty() } ?: "unknown"
        }

        private fun extractDomain(url: String): String {
            return url.removePrefix("http://")
                .removePrefix("https://")
                .substringBefore('/')
        }

        private fun extractPathSegments(url: String): List<String> {
            return url.split('/')
                .dropWhile { !it.contains('.') } // 跳过协议和域名部分
                .dropLastWhile { it.contains('_') || it.contains('.') } // 跳过文件名
        }

        fun shouldDownload(timestamp:String?,lastTimestamp: String?): Boolean{
            return compareTimestamps(timestamp,lastTimestamp)==1
        }

        private fun compareTimestamps(timestamp:String?,lastTimestamp: String?): Int? {
            return try {
                val current = timestamp?.toLongOrNull()
                val last = lastTimestamp?.toLongOrNull()

                when {
                    current == null || last == null -> null
                    current < last -> -1
                    current > last -> 1
                    else -> 0
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * 获取文件名（不含路径）
     */
    fun getSimpleFileName(): String {
        return originalUrl.substringAfterLast('/')
    }

    /**
     * 获取纯文件名（不含扩展名和时间戳）
     */
    fun getBaseFileName(): String {
        return getSimpleFileName()
            .substringBeforeLast('_')
            .substringBeforeLast('.')
    }

    /**
     * 获取时间戳对应的Date对象
     */
    fun getTimestampAsDate(): Date? {
        return try {
            val milliseconds = timestamp.toLong().let {
                if (it.toString().length == 10) it * 1000 else it
            }
            Date(milliseconds)
        } catch (e: Exception) {
            null
        }
    }

    override fun toString(): String {
        return """
        |DownloadableEntity {
        |   originalUrl: '$originalUrl'
        |   currentTimestamp: '$timestamp' (valid: $isTimestampValid)
        |   oldTimestamp :'$lastTimestamp'
        |   formattedTime: '$formattedTime'
        |   fileType: '$fileType'
        |   fileName: '$fileName'
        |   fileDir:  '$fileDir'
        |   resourceType: '$resourceType'
        |   domain: '$domain'
        |   pathSegments: ${pathSegments.joinToString()}
        |   lastModified: ${Date(lastModified)}
        |   simpleFileName: '${getSimpleFileName()}'
        |   shouldDownload: '$shouldDownload'
        |   isUnZip: ‘${isUnZip}’
        |   unZipDir: '${unZipDir}'
        |   pKey: '${pKey}'
        |   baseFileName: '${getBaseFileName()}'
        |}
        """.trimMargin()
    }
}