package com.noetix.libnoetix;


import com.noetix.libnoetix.csv.ICSVReader;
import com.noetix.libnoetix.entity.TTSFrame;
import com.noetix.utils.KLog;
import com.noetix.utils.monitor.UMonitor;

import java.util.Map;

class RobotSDKManagerImpl implements IRobotSDKManager {

    private static final String TAG = "RobotSDKManagerImpl";

    private final INativeApi mNativeApi;
    private  Audio2FaceHandler mAudio2FaceHandler;
    private  BlendShapeHandler mBlendShapeHandler;
    private  FaceAnglesHelper mFaceAnglesHelper;

    private RobotSDKManagerImpl() {
        mNativeApi = INativeApi.get();
    }

    private static final class Holder {
        private static final RobotSDKManagerImpl instance = new RobotSDKManagerImpl();
    }

    public static IRobotSDKManager getInstance() {

        //添加 tag 之后测试
        return Holder.instance;
    }


    @Override
    public void init(RobotConfig robotConfig){
        KLog.d(TAG,"robotConfig ->"+robotConfig.toString());
        SDKContext.isExternalTts = robotConfig.isExternalTts();
        SDKContext.isEnableLog = robotConfig.isEnableLog();
        SDKContext.isSaveAudioData = robotConfig.isSaveAudio();

        UMonitor.getInstance().init(robotConfig.getUAppKey(), robotConfig.getUChannel());
        ICSVReader.getInstance().parseCSVFiles();
        NativeApiImpl.get().init(robotConfig);
        mBlendShapeHandler = new BlendShapeHandler();
        mBlendShapeHandler.init();
        mAudio2FaceHandler = new Audio2FaceHandler();
        mAudio2FaceHandler.setAudio2FaceCallback(mBlendShapeHandler.getAudio2FaceListener());

        mFaceAnglesHelper = new FaceAnglesHelper();
    }

    @Override
    public void unInit() {
        NativeApiImpl.get().unInit();
    }

    @Override
    public void processAudioStream(byte[] audio, int status) {
        if (audio== null){
            KLog.d(TAG," processAudioStream 传入的audio 参数为 null");
            return;
        }
        long duration =TTSHelper.calculateAudioDuration(audio);
        TTSFrame ttsFrame = new TTSFrame(audio,status,duration);
        KLog.d(TAG,"duration "+duration +"ttsFrame "+ttsFrame.audio.length);
        mAudio2FaceHandler.doProcessFrame(ttsFrame);
    }

    @Override
    public void registerResultListener(Callback callback) {
       mBlendShapeHandler.setCallback(callback);
    }

    @Override
    public void unRegisterResultListener(Callback callback) {
      mBlendShapeHandler.setCallback(null);
    }

    @Override
    public void setFaceAngles(float[] angles) {
        mNativeApi.setFaceAngles(angles);
    }

    @Override
    public void setNeckRadios(float[] radios) {
       mNativeApi.setNeckRadios(radios);
    }

    @Override
    public void delaySecondAngles(int second) {
        SDKContext.delaySecondRunAngles =second;
    }

    @Override
    public void setNecksWithBS(Map<String, Float> blendShape) {
        mNativeApi.setNecksWithBS(blendShape);
    }

    @Override
    public int[] mappingMotor(Map<String, Float> blendShape) {
        return mNativeApi.mappingMotor(blendShape);
    }

    @Override
    public void setNeckRadiosDuration(float[] radios, float duratin_sec) {
        mNativeApi.setNeckRadiosDuration(radios,duratin_sec);
    }

    @Override
    public void resetFaceAngles() {
        mFaceAnglesHelper.resetFaceAngles();
    }

    @Override
    public void autoSetNeckZero() {
        mNativeApi.autoSetNeckZero();
    }

    @Override
    public void manualSetNeckZero() {
       mNativeApi.setNeckZeroPosition();
    }

    @Override
    public float[] getNeckRadios() {
        return mNativeApi.getNeckRadios();
    }

    @Override
    public void chatMode(boolean isChatMode) {
        mNativeApi.setChatMode(isChatMode);
    }

    @Override
    public void shutdownMotorController() {
         mNativeApi.shutdownMotorController();
    }

    @Override
    public float[] setNeckStop() {
        return mNativeApi.setNeckStop();
    }

    @Override
    public void stopAudio2Face() {
        mNativeApi.stopAudio2Face();
    }
    @Override
    public void interrupt() {
        mBlendShapeHandler.interrupt();
    }


    @Override
    public void pause(boolean isPause) {
        SDKContext.isPause = isPause;
    }

    @Override
    public void lockDevice(boolean isLock) {
          mNativeApi.lockDevice(isLock);
    }

}
