package com.noetix.libnoetix;

import android.annotation.SuppressLint;

import com.noetix.libnoetix.entity.TTSFrame;
import com.noetix.utils.FileUtil;
import com.noetix.utils.KLog;
//import com.noetix.libnoetix.utils.GlobalContext;

import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.Queue;

/**
 * 音频转面部表情处理器
 * 主要功能：处理流式TTS数据，确保首个数据包满足时长要求
 */
class Audio2FaceHandler {
    private static final String TAG = "doubaoplugin-Audio2FaceHandler";

    private static final int THRESHOLD_TIME = 500;  // 首个数据包最小时间阈值（毫秒）

    // 缓存队列，用于累积未达到阈值的帧数据
    private final Queue<TTSFrame> buffer = new LinkedList<>();
    private long accumulatedTime = 0;        // 当前累积时间（毫秒）
    private volatile boolean firstFrameSent = false;  // 首帧是否已发送标志// 播放处理器

    private  long firstFrameSentTime = 0;
    private Audio2FaceCallback audio2FaceCallback;

    public Audio2FaceHandler() {
        KLog.d(TAG, "Audio2FaceHandler init ---->");
        Audio2FaceListener audio2FaceListener = new Audio2FaceListener();
        INativeApi iNativeApi = INativeApi.get();
        iNativeApi.createAudio2Face(audio2FaceListener);
    }

    public void setAudio2FaceCallback(Audio2FaceCallback audio2FaceCallback) {
        this.audio2FaceCallback = audio2FaceCallback;
    }


    public void doProcessFrame(TTSFrame ttsFrame) {
        if (ttsFrame == null) return;
        if (!SDKContext.isExternalTts){
//            audioStreamHandler.processFrame(ttsFrame.audio, ttsFrame.status);
            processFrame(ttsFrame);
        }else {
            float[] floatAudio = TTSHelper.byteArrayToFloat(ttsFrame.audio, ByteOrder.LITTLE_ENDIAN);
            int length = floatAudio.length;
            int status = ttsFrame.status;
//            KLog.d(TAG, " 外部 length ->" + length + "status ->" + status);
            INativeApi.get().processStream(floatAudio, length, status);
        }

    }

    /**
     * 核心处理方法 - 接收并处理每个TTS帧
     *
     * @param frame 传入的TTS数据帧
     */
    private void processFrame(TTSFrame frame) {

        if (frame.status == 0) {
            firstFrameSentTime = System.currentTimeMillis();
            //reset
            firstFrameSent = false;
            buffer.clear();
        }
        // 遇到结束帧需要特殊处理
        if (frame.status == 2) {
            KLog.d(TAG, "2遇到结束帧需要特殊处理");
            handleEndFrame(frame);
            return;
        }

        //
        if (frame.status == 3) {
            KLog.d(TAG, "3遇到结束帧需要特殊处理 只有一个Frame");
            frame.status = 3;
            handleEndFrame(frame);
            return;
        }

        // 根据首帧发送状态选择处理方式
        if (!firstFrameSent) {
            KLog.d(TAG,"未发送首帧时进行缓存处理");
            handleBuffering(frame);  // 未发送首帧时进行缓存处理
        } else {
            KLog.d(TAG, "已发送首帧时直接透传");
            sendDirectly(frame);     // 已发送首帧时直接透传
        }
    }

    /**
     * 处理结束帧的特殊情况
     *
     * @param endFrame 带有结束标记的帧
     */
    private void handleEndFrame(TTSFrame endFrame) {
        sendDirectly(endFrame);
    }

    /**
     * 处理缓存逻辑
     *
     * @param frame 当前收到的帧
     */
    private void handleBuffering(TTSFrame frame) {
        buffer.add(frame);
        accumulatedTime += frame.duration;
        KLog.d(TAG,"handleBuffering accumulatedTime ->accumulatedTime: " + accumulatedTime);

        // 当累积时间达到阈值时触发发送
        if (accumulatedTime >= THRESHOLD_TIME) {
            KLog.d(TAG, "够1S 数据用时：" + (System.currentTimeMillis() - firstFrameSentTime));
            sendCombinedFrame(false);  // 正常阈值触发发送
        }
    }

    /**
     * 合并帧并发送
     */
    private void sendCombinedFrame(boolean isEnd) {


        TTSFrame combinedFrame = combineFrames();

        // 如果是结束帧，修正为结束帧类型
        if (isEnd) {
            combinedFrame = new TTSFrame(
                    combinedFrame.audio,
                    2,  // 强制设为END类型
                    combinedFrame.duration
            );
        }

        // 发送合并帧并输出日志
        processStream(combinedFrame, "combined");

        // 设置首帧已发送并清空缓存
        firstFrameSent = true;
        buffer.clear();
        accumulatedTime = 0;
    }

    /**
     * 合并缓冲区中的多个帧为一个帧
     */
    private TTSFrame combineFrames() {
        long totalDuration = 0;
        byte[] combinedData = new byte[0];

        // 遍历缓冲区合并数据和计算总时长
        for (TTSFrame frame : buffer) {
            totalDuration += frame.duration;
            combinedData = combineByteArrays(combinedData, frame.audio);
        }

        // 创建合并后的新帧，标记为开始帧
        return new TTSFrame(combinedData, 0, totalDuration);  // 设置为开始帧
    }

    /**
     * 直接透传帧数据（用于首帧之后的常规处理）
     */
    private void sendDirectly(TTSFrame frame) {
        // 如果首帧已发送且是开始帧，设置为中间帧
        if (firstFrameSent && frame.status == 0) {
            KLog.d(TAG, "sendDirectly if");
            frame = new TTSFrame(frame.audio, 1, frame.duration);  // 将开始帧类型修改为中间帧
        }

        // 透传并输出日志
        processStream(frame, "direct");
    }

    /**
     * 字节数组合并工具方法
     */
    private byte[] combineByteArrays(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    /**
     * 最终输出方法（模拟实际发送逻辑）
     */
    @SuppressLint("SdCardPath")
    private void processStream(TTSFrame frame, String source) {
        KLog.d(TAG, "发送帧 - 时长: " + frame.duration + "ms, 字节大小:" + frame.audio.length
                + " 类型: " + typeToString(frame.status)
                + ", 来源: " + source);
        byte[] audio = frame.audio;
        if (SDKContext.isSaveAudioData) {
            if (audio != null && audio.length > 0) {
                FileUtil.writeFile(audio, "/sdcard/tts_send_data.pcm");
            }
        }
        float[] floatAudio = TTSHelper.byteArrayToFloat(audio, ByteOrder.LITTLE_ENDIAN);
        int length = floatAudio.length;
        int status = frame.status;
//        KLog.d(TAG, "length ->" + length + "status ->" + status);

        KLog.d(TAG, "开始调用算法进行推理时间：" + System.currentTimeMillis());
        INativeApi.get().processStream(floatAudio, length, status);


    }

    private class Audio2FaceListener implements NoetixJNI.Audio2FaceCallback {
        @Override
        public void onResult(float[] outputParams, float[] audioData,int tag) {

            KLog.d(TAG, "onResult");
            if (SDKContext.isSaveAudioData) {
                byte[] audio = TTSHelper.floatArrayToByte(audioData, ByteOrder.LITTLE_ENDIAN);
                if (audio != null && audio.length > 0) {
                    FileUtil.writeFile(audio, "/sdcard/tts_result_data.pcm");
                }
            }

            if (audio2FaceCallback != null) {
                KLog.d(TAG, "audio2FaceCallback");
                audio2FaceCallback.onResult(outputParams, audioData,tag);
            }
//            KLog.d(TAG, "Output params length: " + outputParams.length +
//                    ", Audio data length: " + audioData.length);
//            KLog.d(TAG,"outputParams length "+outputParams.length+"audioData length->"+audioData.length);
//
//            for (int i = 0; i < outputParams.length; i++) {
//                KLog.d(TAG, "Output param " + i + ": " + outputParams[i]);
//            }
//
//            // print time
//            KLog.d(TAG, "run time" + System.currentTimeMillis());
            //TODO
        }
    }

    /**
     * 帧类型转换工具方法
     */
    private String typeToString(int index) {
        switch (index) {
            case 0:
                return "开始帧";
            case 1:
                return "中间帧";
            case 2:
                return "结束帧";
            default:
                return "未知类型";
        }
    }

}
