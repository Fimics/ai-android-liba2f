package com.noetix.libnoetix;

import java.util.Map;

public class NoetixJNI {
    static {
        // 先加载 c++_shared
        System.loadLibrary("c++_shared");

        System.loadLibrary("noetix_hobbs_sdk");
    }


    public static native float[] subtractVectors(float[] a, float[] b);
    // Native method declarations
    public static native void initRobotConfig(String configPath, String zeroAnglePath);
    public static native long createBlendshape2Action(String templateDataPath);
    public static native void deleteBlendshape2Action(long handle);
    public static native int[] mappingMotor(long handle, Map<String, Float> blendshape);

    public  static native Map<String, Float> decode2Blendshape(byte[] bytesData);

    public static native void initMotorController();
    public static native void initMotorControllerCustom(String facePort, String neckBus);
    public static native void shutdownMotorController();
    //    public static native void reconfigureFaceSerial();
    public static native void setFaceAngles(float[] angles);
    public static native void resetFaceAngles(int timeMs);
    public static native void setNeckAngles(float[] angles);
    public static native void setNeckRadios(float[] radios);
    public static native void setNeckRadiosDuration(float[] radios, float duratin_sec);

    public static native void setNeckAnglesParallel(float[] angles);
    public static native void setNeckRadiosParallel(float[] radios);
    public static native void setNeckRadiosDurationParallel(float[] radios, float duratin_sec);
    public static native void setNeckRadiosVersion(float[] radios, int version);
    public static native void setNeckRadiosDurationVersion(float[] radios, float duratin_sec,int version);
    public static native void autoSetNeckZero();
    public static native void setNeckZeroPosition();
    public static native float[] getNeckRadios();
    public static native float[] setNeckStop();

    // Overload with default parameter
    public static void resetFaceAngles() {
        resetFaceAngles(300);
    }

    public static native void initMyLogger(String path);
    public static native void flushMyLogger();
    public static native void deleteMyLogger();

    public interface Audio2FaceCallback {
        void onResult(float[] outputParams, float[] audioData, int tag);
    }

    public static native long createAudio2Face(String modelPath, Audio2FaceCallback callback);
    public static native void deleteAudio2Face(long handle);
    public static native void processStream(long handle, float[] audioData, int length, int tag);
    public  static native void resetAudio2Face(long handler);


}