package com.noetix.libnoetix;


import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.Observer;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.noetix.core.CloudSetting;
import com.noetix.core.downloader.EventConfig;
import com.noetix.utils.AdbCommandService;
import com.noetix.utils.AppGlobals;
import com.noetix.utils.ConfigChecker;
import com.noetix.utils.KLog;
import com.noetix.utils.KeysUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

class NativeApiImpl implements INativeApi {

    private static final String TAG = "SDKManagerImpl";
    private long mChatHandle;
    private long mTrackHandle;
    private long mAudio2FaceHandle;
    private boolean mIsChatMode;
    private String mSerialPort;
    private String mCan;
    private int mNeckStructure;
    private final boolean outputLog = false;
    private RobotConfig robotConfig;
    private static final String[] CONFIG_PATHS = {
            "/sdcard/robot_config/expression29_chat",
            "/sdcard/robot_config/expression29_track",
            "/sdcard/robot_config/29_servo_config.yaml",
            "/sdcard/robot_config/default_zero_angle.yaml",
            "/sdcard/robot_csv/default.csv",
            "/sdcard/robot_data/unitalker_960_simplified.rknn"
    };

    private NativeApiImpl() {
        Config mConfig = new Config();
        mConfig.tryCreateNativeDir();
        configEventObserver();
    }

    private static class Holder {
        public static final NativeApiImpl instance = new NativeApiImpl();
    }

    public static INativeApi get() {
        return Holder.instance;
    }

    @Override
    public float[] subtractVectors(float[] a, float[] b) {
        return NoetixJNI.subtractVectors(a, b);
    }

    /**
     * 在application 里初始化一次
     */
    @Override
    public void init(RobotConfig robotConfig) {
        this.robotConfig = robotConfig;
        this.mCan = robotConfig.getCan();
        this.mSerialPort = robotConfig.getSerialPort();
        this.mNeckStructure = robotConfig.getNeckType();
        AdbCommandService.Companion.start(AppGlobals.getApplication());

        KLog.d("sdk_version","1.0.09");
        KLog.d(TAG,"开始初始化sdk...serialPort "+mSerialPort +" can "+mCan);
        KLog.d(TAG,"CONFIG_PATHS size-> "+CONFIG_PATHS.length);

        CloudSetting.getInstance().updateApk(robotConfig.getActivity());

        boolean isCheckCompleted = ConfigChecker.checkAllPathsExistAndNotEmpty(CONFIG_PATHS);
        if (!isCheckCompleted){
            KLog.d(TAG,"配置文件完整性校验不通过 开始更新配置文件");
            KeysUtils.clearTimestamp();
            CloudSetting.getInstance().updateConfig();
        }else {
            KLog.d(TAG,"配置文件完整性校验通过 初始化native sdk");
            initNativeSDK();
        }

    }

    private void configEventObserver(){
        LiveEventBus.get(EventConfig.class).observeForever(new Observer<EventConfig>() {
            @Override
            public void onChanged(EventConfig eventConfig) {
                KLog.d(TAG,"配置文件更新完成 初始化sdk");
                initNativeSDK();
            }
        });
    }

    private void initNativeSDK(){

        String nativeConfig = KeysUtils.getNativeConfig();
        // TODO 合并代码后直接把nativeConfig 传到cpp层
        if (!TextUtils.isEmpty(nativeConfig)){
            try {
                JSONObject object = new JSONObject(nativeConfig);
                mNeckStructure = object.getInt("nickStructureType");
            } catch (JSONException e) {
                KLog.d(TAG,e.getMessage());
            }
        }

        KLog.d(TAG,"nativeConfig ->"+nativeConfig +" mNeckStructure ->"+mNeckStructure );

        NoetixJNI.initMyLogger(Config.LOG_PATH);
        NoetixJNI.initRobotConfig(Config.CONFIG_PATH, Config.ZERO_ANGLE_PATH);

        boolean hasParams = !TextUtils.isEmpty(mSerialPort) || !TextUtils.isEmpty(mCan);
        if (hasParams){
            KLog.d(TAG,"initMotorControllerCustom");
            NoetixJNI.initMotorControllerCustom(mSerialPort,mCan);
        }else {
            KLog.d(TAG,"initMotorController");
            NoetixJNI.initMotorController();
        }

        mChatHandle = NoetixJNI.createBlendshape2Action(Config.PATH_CHAT);
        mTrackHandle = NoetixJNI.createBlendshape2Action(Config.PATH_TRACK);

        if (mChatHandle==0){
            KLog.d(TAG,"mChatHandle  ->"+mChatHandle);
        }

        if (mTrackHandle==0){
            KLog.d(TAG,"mTrackHandle  ->"+mTrackHandle);
        }


        KLog.d(TAG, "Called initRobotConfig with CONFIG_PATH: " + Config.CONFIG_PATH +
                " and ZERO_ANGLE_PATH: " + Config.ZERO_ANGLE_PATH);

        setNeckRadiosDuration(new float[]{0.0f, 0.0f, 0.0f}, robotConfig.getZeroDuration());
    }

    @Override
    public void unInit() {
        NoetixJNI.deleteBlendshape2Action(mChatHandle);
        NoetixJNI.deleteBlendshape2Action(mTrackHandle);
        NoetixJNI.deleteMyLogger();
    }

    @Override
    public void shutdownMotorController() {
        KLog.d(TAG,"shutdownMotorController");
        NoetixJNI.shutdownMotorController();
    }

    @Override
    public float[] setNeckStop() {
        KLog.d(TAG,"setNeckStop");
        return NoetixJNI.setNeckStop();
    }

    @Override
    public void setNeckZeroPosition() {
        KLog.d(TAG,"setNeckZeroPosition");
        NoetixJNI.setNeckZeroPosition();
    }

    @Override
    public void setChatMode(boolean isChatMode) {
        this.mIsChatMode=isChatMode;
        KLog.d(TAG,"setChatMode ->"+isChatMode);
    }


    @Override
    public int[] mappingMotor( Map<String, Float> blendshape) {
        if (outputLog){
            KLog.d(TAG,"mappingMotor mIsChatMode"+mIsChatMode+" blendshape size"+blendshape);
        }

        if (mIsChatMode){
            return NoetixJNI.mappingMotor(mChatHandle, blendshape);
        }else {
            return NoetixJNI.mappingMotor(mTrackHandle, blendshape);
        }
    }

    @Override
    public void setFaceAngles(float[] angles) {
        if (outputLog){
            StringBuilder stringBuffer = new StringBuilder();
            int len = angles.length;
            for (int i=0;i<len;i++){
                stringBuffer.append("angle[").append(i).append("]").append(" = ").append(angles[i]);
            }
            KLog.d(TAG,"setFaceAngles ->angles "+stringBuffer.toString());
        }
        NoetixJNI.setFaceAngles(angles);
    }

    @Override
    public void setNeckAngles(float[] angles) {
        if (outputLog){
            StringBuilder stringBuffer = new StringBuilder();
            int len = angles.length;
            for (int i=0;i<len;i++){
                stringBuffer.append("angle[").append(i).append("]").append(" = ").append(angles[i]);
            }
            KLog.d(TAG,"setNeckAngles ->angles "+stringBuffer.toString());
        }
        if (this.mNeckStructure==NeckStructure.LING_HU){
            NoetixJNI.setNeckAnglesParallel(angles);
        }else {
            NoetixJNI.setNeckAngles(angles);
        }
    }

    @Override
    public void setNeckRadiosDuration(float[] radios, float duratin_sec) {
        NoetixJNI.setNeckRadiosDurationVersion(radios,duratin_sec,mNeckStructure);
    }

    @Override
    public void setNeckRadios(float[] radios) {
        NoetixJNI.setNeckRadiosVersion(radios,mNeckStructure);
    }

    @Override
    public void autoSetNeckZero() {
        NoetixJNI.autoSetNeckZero();
    }

    @Override
    public float[] getNeckRadios() {
        KLog.d(TAG,"getNeckRadios");
        return NoetixJNI.getNeckRadios();
    }

    @Override
    public void flushMyLogger() {
         NoetixJNI.flushMyLogger();
    }

    @Override
    public void setNecksWithBS(Map<String, Float> blendShape) {
        try {
            if (blendShape == null || blendShape.isEmpty()) return;

            // 安全获取值并处理null情况
            Float pitchVal = blendShape.get("HeadPitch");
            Float rollVal = blendShape.get("HeadRoll");
            Float yawVal = blendShape.get("HeadYaw");

            // 使用三元运算符处理可能的null值
            float pitch = (pitchVal != null) ? pitchVal : 0.0f;
            float roll = (rollVal != null) ? -rollVal : 0.0f; // 注意负号处理
            float yaw = (yawVal != null) ? yawVal : 0.0f;

            float[] neckArray = new float[3];
            neckArray[0] = pitch / 0.5f;
            neckArray[1] = roll / 0.8f;
            neckArray[2] = yaw / 0.8f;


            if (neckArray[0]>0.6){
                SDKLog.d(TAG+"p","pitch ->"+neckArray[0]);
            }
            setNeckRadios(neckArray);
        } catch (Exception e) {
            KLog.d(TAG, e.getMessage());
        }
    }

    public void setNecksWithArray(float [] array) {
        setNeckRadios(array);
    }

    @Override
    public void resetMotorAndAngles() {
        NoetixJNI.resetFaceAngles(2000);
        setNeckRadiosDuration(new float[]{0.0f, 0.0f, 0.0f},1.5f);
    }

    @Override
    public void setNeckAnglesParallel(float[] angles) {
         NoetixJNI.setNeckAnglesParallel(angles);
    }

    @Override
    public void setNeckRadiosParallel(float[] radios) {
        NoetixJNI.setNeckRadiosParallel(radios);
    }


    @Override
    public void stopAudio2Face() {
        NoetixJNI.resetAudio2Face(mAudio2FaceHandle);
    }

    @Override
    public void lockDevice(boolean isLock) {
      //TODO c++层实现设备锁定
//        NoetixJNI.lockDevice(isLock)
    }

    @Override
    public void setNeckRadiosDurationParallel(float[] radios, float duratin_sec) {
        NoetixJNI.setNeckRadiosDurationParallel(radios,duratin_sec);
    }

    @Override
    public Map<String, Float> decode2Blendshape(byte[] bytesData) {
        return NoetixJNI.decode2Blendshape(bytesData);
    }

    /**
     * //TODO 是否可以放在内部调用
     */
    @Override
    public void createAudio2Face( NoetixJNI.Audio2FaceCallback callback) {
       KLog.d(TAG,"modelPath ->"+Config.modelPath);
       mAudio2FaceHandle=NoetixJNI.createAudio2Face(Config.modelPath,callback);
       Log.d(TAG, "createAudio2Face mAudio2FaceHandle:" + mAudio2FaceHandle);
       if (mAudio2FaceHandle==0){
           KLog.d(TAG,"Audio2Face 引擎创建失败...");
       }else {
           KLog.d(TAG,"Audio2Face 引擎创建成功...");
       }
    }

    @Override
    public void deleteAudio2Face() {
        try {
            NoetixJNI.deleteAudio2Face(mAudio2FaceHandle);
        }catch (Exception e){
            KLog.e(TAG,"deleteAudio2Face exception");
            KLog.e(TAG,e.getMessage());
        }
    }

    @Override
    public void processStream(float[] audioData, int length, int status) {
       NoetixJNI.processStream(mAudio2FaceHandle,audioData,length,status);
    }

}
