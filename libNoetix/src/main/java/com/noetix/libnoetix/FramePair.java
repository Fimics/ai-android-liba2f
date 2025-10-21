package com.noetix.libnoetix;

import java.util.Map;

public class FramePair {

    private Map<String, Float> blendShapeFrame;
    private float[] audioFrame;
    private int index;
    private int dts;

    public FramePair(Map<String, Float> blendShapeFrame, float[] audioFrame, int index,int dts) {
        this.blendShapeFrame = blendShapeFrame;
        this.audioFrame = audioFrame;
        this.index = index;
        this.dts = dts;
    }

    public Map<String, Float> getBlendShapeFrame() {
        return blendShapeFrame;
    }

    @SuppressWarnings("unused")
    public void setBlendShapeFrame(Map<String, Float> blendShapeFrame) {
        this.blendShapeFrame = blendShapeFrame;
    }

    public float[] getAudioFrame() {
        return audioFrame;
    }


    @SuppressWarnings("unused")
    public void setAudioFrame(float[] audioFrame) {
        this.audioFrame = audioFrame;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getDts() {
        return dts;
    }

    public void setDts(int dts) {
        this.dts = dts;
    }
}
