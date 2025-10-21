package com.noetix.libnoetix

import com.noetix.utils.KLog
import java.io.ByteArrayOutputStream
import java.nio.ByteOrder

class AudioStreamHandler {
    private val mByteBuffer = ByteArrayOutputStream()
    private val lock = Any()
    private val BUFFER_THRESHOLD = 32000 // 1秒数据阈值

    private fun cacheAudioData(data: ByteArray) {
        synchronized(lock) {
            mByteBuffer.write(data, 0, data.size)
        }
    }

    private fun clearBufferedData() {
        synchronized(lock) {
            mByteBuffer.reset()
        }
    }

    private fun sendBufferedData(dts: Int) {
        synchronized(lock) {
            if (mByteBuffer.size() > 0) {
                sendFloatData(mByteBuffer.toByteArray(), dts)
                if (dts == 2 || dts == 3) {
                    clearBufferedData()
                }
            }
        }
    }

    private fun sendData(data: ByteArray, dts: Int) {
        synchronized(lock) {
            sendFloatData(data, dts)
        }
    }

    private fun sendFloatData(data: ByteArray?, dts: Int) {
        val floatAudio = TTSHelper.byteArrayToFloat(data, ByteOrder.LITTLE_ENDIAN)
//        KLog.d(TAG, "外部 length -> ${floatAudio.size} status -> $dts")
        INativeApi.get().processStream(floatAudio, floatAudio.size, dts)
    }

    @Synchronized
    fun processFrame(data: ByteArray, dts: Int) {
        KLog.d(TAG, "data size ${data.size}")
        when (dts) {
            3 -> handleDirectSend(data)
            0 -> handleStartFrame(data)
            1 -> handleMiddleFrame(data)
            2 -> handleEndFrame(data)
        }
    }

    private fun handleDirectSend(data: ByteArray) {
        sendData(data, 3)
    }

    private fun handleStartFrame(data: ByteArray) {
        clearBufferedData()
        cacheAudioData(data)
        if (mByteBuffer.size() >= BUFFER_THRESHOLD) {
            sendBufferedData(0)
        }
    }

    private fun handleMiddleFrame(data: ByteArray) {
        if (mByteBuffer.size() < BUFFER_THRESHOLD) {
            cacheAudioData(data)
            if (mByteBuffer.size() >= BUFFER_THRESHOLD) {
                sendBufferedData(0)
            }
        } else {
            sendData(data, 1)
        }
    }

    private fun handleEndFrame(data: ByteArray) {
        if (mByteBuffer.size() < BUFFER_THRESHOLD) {
            cacheAudioData(data)
            sendBufferedData(3)
        } else {
            sendData(data, 2)
        }
    }

    companion object {
        private const val TAG = "PcmAudioPlayer"
    }
}
