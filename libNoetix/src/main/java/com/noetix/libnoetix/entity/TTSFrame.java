package com.noetix.libnoetix.entity;


public class TTSFrame {

    public byte [] audio;
    public int status;
    public long duration;

    public TTSFrame(byte[] audio, int status,long duration) {
        this.audio = audio;
        this.status = status;
        this.duration =duration;
    }
}
