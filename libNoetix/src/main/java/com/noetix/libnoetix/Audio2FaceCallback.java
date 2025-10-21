package com.noetix.libnoetix;

interface Audio2FaceCallback {
    void onResult(float[] outputParams, float[] audioData,int tag);
}
