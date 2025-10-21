package com.noetix.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * ZIP 解压工具类
 * 使用示例：
 * ZipExtractor.unzip(
 *     zipFile = File("/sdcard/template.zip"),
 *     targetDir = File("/sdcard/unzipped"),
 *     onSuccess = { Log.d("ZipExtractor", "解压成功") },
 *     onError = { e -> Log.e("ZipExtractor", "解压失败", e) }
 * )
 */
object ZipExtractor {

    /**
     * 解压 ZIP 文件
     * @param zipFile ZIP 文件对象
     * @param targetDir 目标目录
     * @param onSuccess 成功回调
     * @param onError 失败回调（携带异常）
     */
    fun unzip(
        zipFile: File,
        targetDir: File,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = { _ -> }
    ) {
        try {
            validateInput(zipFile, targetDir)
            performUnzip(zipFile, targetDir)
            onSuccess()
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * 解压 ZIP 文件（路径形式）
     */
    fun unzip(
        zipPath: String,
        targetPath: String,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = { _ -> }
    ) {
        unzip(File(zipPath), File(targetPath), onSuccess, onError)
    }

    // region Private Methods
    private fun validateInput(zipFile: File, targetDir: File) {
        if (!zipFile.exists()) {
            throw FileNotFoundException("ZIP file not found: ${zipFile.absolutePath}")
        }
        if (zipFile.isDirectory) {
            throw IllegalArgumentException("Target path must be a directory: ${zipFile.absolutePath}")
        }
        if (targetDir.isFile) {
            throw IllegalArgumentException("Target path must be a directory: ${targetDir.absolutePath}")
        }
    }

    private fun performUnzip(zipFile: File, targetDir: File) {
        targetDir.takeIf { !it.exists() }?.mkdirs()

        ZipInputStream(BufferedInputStream(zipFile.inputStream())).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                val entryFile = File(targetDir, entry.name).normalizeFile()
                
                validateSafePath(targetDir, entryFile)
                
                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    extractEntry(zis, entryFile)
                }
                zis.closeEntry()
            }
        }
    }

    private fun extractEntry(zis: ZipInputStream, destFile: File) {
        destFile.parentFile?.mkdirs()
        BufferedOutputStream(FileOutputStream(destFile)).use { bos ->
            zis.copyTo(bos, bufferSize = 1024)
        }
    }

    /**
     * 防止 ZIP 路径穿越攻击（如包含 ../ 的恶意路径）
     */
    private fun validateSafePath(baseDir: File, destFile: File) {
        if (!destFile.canonicalPath.startsWith(baseDir.canonicalPath + File.separator)) {
            throw SecurityException("ZIP entry attempted to traverse outside target directory")
        }
    }

    private fun File.normalizeFile(): File {
        return File(canonicalPath)
    }
    // endregion
}