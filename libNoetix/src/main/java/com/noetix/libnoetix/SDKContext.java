package com.noetix.libnoetix;

public class SDKContext {

    public static final String TAG = "ROBOT_SDK";
    public static boolean isEnableLog = true;
    public static boolean isSaveAudioData = false;

    public static final int ORIGINAL_FPS=30;
    public static final int TARGET_FPS=10;
    public static final int RATIO_FPS =ORIGINAL_FPS/TARGET_FPS;

    public volatile static boolean isPause = false;

    public static boolean isExternalTts = false;
    public static int delaySecondRunAngles = 0;
}
