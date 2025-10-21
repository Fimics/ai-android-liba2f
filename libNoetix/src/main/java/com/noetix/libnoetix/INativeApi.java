package com.noetix.libnoetix;

import android.app.Activity;

import java.util.Map;

interface INativeApi {


    @Deprecated
    float[] subtractVectors(float[] a, float[] b);

    /**
     * 初始化
     */
   void init(RobotConfig robotConfig);

    /**
     * 逆初始化
     */
    void unInit();

    void shutdownMotorController();

    /**
     * 停掉脖子电机
     * @return
     */
    float[] setNeckStop();

    /**
     * 脖子复位
     */
    void setNeckZeroPosition();

    void setChatMode(boolean isChatMode);

    int[] mappingMotor(Map<String, Float> blendshape);

    /**
     * 设置舵机控制(写串口)
     */
    void setFaceAngles(float [] angles);

    /**
     * 电机控制(写CAN)
     */
    @SuppressWarnings("unused")
    void setNeckAngles(float [] angles);

    void setNeckRadiosDuration(float[] radios, float duratin_sec);

    void autoSetNeckZero();


    @SuppressWarnings("unused")
    float[] getNeckRadios();

    @SuppressWarnings("unused")
    void flushMyLogger();


    void setNeckRadios(float[] radios);

    void setNecksWithBS(Map<String, Float> blendShape);

    @SuppressWarnings("unused")
    void setNecksWithArray(float [] array);

    @SuppressWarnings("unused")
    void resetMotorAndAngles();

    void setNeckAnglesParallel(float[] angles);
    void setNeckRadiosParallel(float[] radios);
    void setNeckRadiosDurationParallel(float[] radios, float duratin_sec);

    Map<String, Float> decode2Blendshape(byte[] bytesData);


    void createAudio2Face(NoetixJNI.Audio2FaceCallback callback);

    @SuppressWarnings("unused")
    void deleteAudio2Face();

    void processStream(float[] audioData, int length, int status);

   void stopAudio2Face();

   void lockDevice(boolean isLock);

   static INativeApi get() {
        return NativeApiImpl.get();
    }

}
