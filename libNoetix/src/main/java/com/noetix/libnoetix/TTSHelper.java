package com.noetix.libnoetix;

import android.media.AudioFormat;
import java.nio.ByteOrder;

public class TTSHelper {
    // 设置PCM参数
    public static int SAMPLE_RATE = 16000; // 采样率 讯飞
    public static int CHANNEL = AudioFormat.CHANNEL_OUT_MONO; // 单声道
    public static int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT; // 16位PCM
    public static int CHANNEL_COUNT = 1;

    public static int FPS = 10;

    // 计算音频时长（单位：毫秒），返回值类型为 long
    public static long calculateAudioDuration(byte[] audioData) {
        long bytePerSample = (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) ? 2 : 1; // 每个样本的字节数
        long samplesPerSecond = SAMPLE_RATE * CHANNEL_COUNT * bytePerSample; // 每秒的字节数
        long audioLength = audioData.length; // 音频数据的字节数
        // 计算音频时长（毫秒）
        return (audioLength * 1000L) / samplesPerSecond; // 使用 1000L 来确保计算的精度
    }

    // 计算每秒音频数据量（字节）
    public static int calculateBytesPerSecond() {
        int bytePerSample = (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) ? 2 : 1; // 每个样本的字节数
        return SAMPLE_RATE * CHANNEL_COUNT * bytePerSample; // 每秒的字节数
    }

    /**
     * 计算每帧的字节数
     */
    @SuppressWarnings("unused")
    public static int calculateBytesPerFrame() {
        int bytesPerSecond = Math.toIntExact(calculateBytesPerSecond()); // 每秒数据量（字节）
        return bytesPerSecond / FPS;
    }

    /**
     * 计算每帧的时长（毫秒）
     */
    @SuppressWarnings("unused")
    public static int calculateDurationPerFrame() {
        return 1000 / FPS; // 每帧时长（秒）
    }


    public static float[] byteArrayToFloat(byte[] byteArray, ByteOrder byteOrder) {
        int length = byteArray.length / 2;
        float[] floatArray = new float[length];

        for (int i = 0; i < length; i++) {
            int highByte, lowByte;

            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                highByte = byteArray[2*i];
                lowByte  = byteArray[2*i+1];
            } else {
                highByte = byteArray[2*i+1]; // 小端序：低位在前
                lowByte  = byteArray[2*i];   // 高位在后
            }

            short int16Value = (short) ((highByte << 8) | (lowByte & 0xFF));
            floatArray[i] = int16Value;
        }

        return floatArray;
    }

    public static byte[] floatArrayToByte(float[] floatArray, ByteOrder byteOrder) {
        byte[] byteArray = new byte[floatArray.length * 2];

        for (int i = 0; i < floatArray.length; i++) {
            short int16Value = (short) floatArray[i];

            if (byteOrder == ByteOrder.BIG_ENDIAN) {
                byteArray[2*i]   = (byte) (int16Value >> 8);
                byteArray[2*i+1] = (byte) (int16Value & 0xFF);
            } else {
                byteArray[2*i]   = (byte) (int16Value & 0xFF);  // 小端序：低位在前
                byteArray[2*i+1] = (byte) (int16Value >> 8);    // 高位在后
            }
        }

        return byteArray;
    }

}
