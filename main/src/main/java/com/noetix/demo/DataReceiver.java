package com.noetix.demo;

import android.util.Log;

import com.noetix.libnoetix.Callback;
import com.noetix.libnoetix.FramePair;
import com.noetix.libnoetix.IRobotSDKManager;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class DataReceiver implements Callback {
    private static final String TAG = "DataReceiver";
    @Override
    public void onOriginResult(float[] outputParams, float[] audioData) {
        Log.d(TAG,"onOriginResult");
    }

    @Override
    public void onSynchronousResult(FramePair framePair, Map<String, Float> csvBlendShapes, LinkedList<FramePair> linkedList) {

        //1.执行脖子动作
        IRobotSDKManager.getInstance().setNecksWithBS(csvBlendShapes);

        if (framePair == null) {
            //2.只通过csv 执行
            Log.d(TAG,"只通过csv 执行");
            sendOnlyCsvCommand(csvBlendShapes);
        } else {
            //handle tts
            float[] floatsAudio =framePair.getAudioFrame();
             //3. 播放tts
//           AudioPlayer.addToTrack(floatsAudio);

            //4 .执行脸部动作
            Map<String, Float> blendShapes = framePair.getBlendShapeFrame();
            Log.d(TAG,"执行 tss 生成的bs指令");
//            Log.d(TAG, "blendShapes " + blendShapes.get("JawOpen"));
            float[] motorCommands = bs2MotorCommands(blendShapes);
            float[] csvCommands = bs2MotorCommands(csvBlendShapes);
            float[] mergedCommands = createUpdatedBlendShapes(csvCommands, motorCommands);
            IRobotSDKManager.getInstance().setFaceAngles(mergedCommands);
        }
    }

    private void sendOnlyCsvCommand(Map<String, Float> blendShapes) {
//        float JawOpen =blendShapes.get("JawOpen");
//        KLog.d(TAG,"JawOpen "+JawOpen);
        if (blendShapes == null || blendShapes.isEmpty()) {
//            KLog.d(TAG, "当前无数据可处理");
            return;
        }
        try {
            float[] motorCommands = bs2MotorCommands(blendShapes);
            IRobotSDKManager.getInstance().setFaceAngles(motorCommands);
        } catch (Exception e) {
            Log.e(TAG, "执行电机指令时发生异常", e);
        }
    }

    private float[] bs2MotorCommands(Map<String, Float> blendShapes) {
        int[] motorAngles = IRobotSDKManager.getInstance().mappingMotor(blendShapes);
        float[] motorCommands = new float[motorAngles.length];
        for (int i = 0; i < motorAngles.length; i++) {
            motorCommands[i] = motorAngles[i];
        }
        return motorCommands;
    }

    public float[] createUpdatedBlendShapes(float[] csvMotorCommands, float[] motorCommands) {
        int newLength = csvMotorCommands.length;
        float[] updated = Arrays.copyOf(motorCommands, newLength);

        for (int i = 0; i < newLength; i++) {
            if (i <= 14) {
                updated[i] = csvMotorCommands[i];
            } else {
                updated[i] = motorCommands[i];
            }
        }
        return updated;
    }
}
