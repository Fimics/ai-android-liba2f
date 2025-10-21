package com.noetix.libnoetix;

import java.util.LinkedList;
import java.util.Map;

public interface Callback {

    /**
     * 此方法为 传入audio 后，native 层直接生成的原始数据
     * @param outputParams  blendShape 数据
     * @param audioData  audio 数据
     */
    void onOriginResult(float[] outputParams, float[] audioData);


    /**
     *  此方法封装native 层onOriginResult 方法后，以固定频率回调数据
     * @param framePair  包含一帧bs和一帧audio,此bs数据不包括脖子电机数据，
     * @param csvBlendShapes   带有脖子数据的BS
     * @param list  暂时用不到这个参数
     */
    void onSynchronousResult(FramePair framePair, Map<String, Float> csvBlendShapes, LinkedList<FramePair> list);
}
