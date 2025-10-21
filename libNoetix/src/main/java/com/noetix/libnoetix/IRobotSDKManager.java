package com.noetix.libnoetix;

import android.app.Activity;

import java.util.Map;

public interface IRobotSDKManager {
    /**
     * 初始化SDK  neckStructure  0 是老结构  1，是新结构
     */
  void init(RobotConfig robotConfig);

    /**
     * 销毁SDK
     */
    void unInit();

    /**
     * @param audio   tts 音视内容
     * @param status  帧序 0(开始), 1(中间) 2(结束)
     */
    void processAudioStream(byte[] audio, int status);

    /**
     * 注册 BS，audio 回调函数
     * @param callback
     */
    void registerResultListener(Callback callback);

    /**
     * 反注册BS ,audio 回调函数
     * @param callback
     */
    void unRegisterResultListener(Callback callback);

    /**
     * 传入 float 数组数据，驱动舵机
     * @param angles
     */
    void setFaceAngles(float[] angles);

    /**
     * 设置脖子radios
     * @param radios
     */
    void setNeckRadios(float[] radios);

    void delaySecondAngles(int second);

    /**
     * 传入BS 驱动CAN 电机
     * @param blendShape
     */
    void setNecksWithBS(Map<String, Float> blendShape);

    /**
     * blendShape 转换为舵机数组
     * @param blendShape
     * @return
     */
    int[] mappingMotor(Map<String, Float> blendShape);

    void setNeckRadiosDuration(float[] radios, float duratin_sec);

    void shutdownMotorController();

    /**
     * 舵机复位
     */
    void resetFaceAngles();

    /**
     * 脖子电机自动标零，
     */
    void autoSetNeckZero();

    /**
     * 脖子电机手动标零位
     */
    void manualSetNeckZero();

    /**
     * 获取脖子电机角度
     */
    float[] getNeckRadios();

    /**
     * 是否为交互模式，外部调用 isChatMode 应该为true
     * @param isChatMode
     */
    void chatMode(boolean isChatMode);


    float[] setNeckStop();

  /**
   *   停止生成bs
   */
    void stopAudio2Face();
    /*
     * 打断
     */
    void interrupt();

    void pause(boolean isPause);

    void lockDevice(boolean isLock);

    static IRobotSDKManager getInstance() {
        return RobotSDKManagerImpl.getInstance();
    }

}
