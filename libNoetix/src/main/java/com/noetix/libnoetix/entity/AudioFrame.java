package com.noetix.libnoetix.entity;

import java.util.Arrays;
import java.util.Objects;

/**
 * 音频帧数据载体
 */
public class AudioFrame {
    private final int id;          // 唯一帧标识（用于去重）
    private final byte[] data;     // PCM数据
    private final long timestamp;  // 时间戳（毫秒）

    public AudioFrame(int id, byte[] data) {
        this.id = id;
        this.data = data.clone();  // 防御性拷贝
        this.timestamp = System.currentTimeMillis();
    }

    // region Getters
    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data.clone(); // 防止外部修改
    }

    public long getTimestamp() {
        return timestamp;
    }
    // endregion


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AudioFrame that = (AudioFrame) o;
        return id == that.id && timestamp == that.timestamp && Objects.deepEquals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Arrays.hashCode(data), timestamp);
    }
}
