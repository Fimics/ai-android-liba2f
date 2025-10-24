package com.noetix.libnoetix;

import android.app.Activity;

public class RobotConfig {
    private Activity activity;
    private String serialPort;
    private String can;
    private boolean externalTts;
    private boolean enableLog;
    private boolean saveAudio;
    private int delaySeconds;
    private String ext;
    private String uAppKey;    // 新增字段
    private String uChannel;   // 新增字段
    private float zeroDuration; // 新增字段
    private int neckType;

    private RobotConfig(Builder builder) {
        this.activity = builder.activity;
        this.serialPort = builder.serialPort;
        this.can = builder.can;
        this.externalTts = builder.externalTts;
        this.enableLog = builder.enableLog;
        this.saveAudio = builder.saveAudio;
        this.delaySeconds = builder.delaySeconds;
        this.ext = builder.ext;
        this.uAppKey = builder.uAppKey;
        this.uChannel = builder.uChannel;
        this.zeroDuration = builder.zeroDuration;
        this.neckType = builder.neckType;
    }

    public static class Builder {
        private final Activity activity;
        private String serialPort = "";
        private String can = "";
        private boolean externalTts = false;
        private boolean enableLog = false;
        private boolean saveAudio = false;
        private int delaySeconds = 0;
        private String ext = "";
        private String uAppKey = "";    // 默认值为空字符串
        private String uChannel = "";   // 默认值为空字符串
        private float zeroDuration = 5f; // 默认值为0
        private int neckType;

        public Builder(Activity activity) {
            this.activity = activity;
        }

        public Builder setSerialPort(String serialPort) {
            this.serialPort = serialPort;
            return this;
        }

        public Builder setCan(String can) {
            this.can = can;
            return this;
        }

        public Builder setExternalTts(boolean externalTts) {
            this.externalTts = externalTts;
            return this;
        }

        public Builder enableLog(boolean enableLog) {
            this.enableLog = enableLog;
            return this;
        }

        public Builder saveAudio(boolean saveAudio) {
            this.saveAudio = saveAudio;
            return this;
        }

        public Builder setDelaySeconds(int delaySeconds) {
            this.delaySeconds = delaySeconds;
            return this;
        }

        public Builder setExt(String ext) {
            this.ext = ext;
            return this;
        }

        // 新增的三个字段的设置方法
        public Builder setUAppKey(String uAppKey) {
            this.uAppKey = uAppKey;
            return this;
        }

        public Builder setUChannel(String uChannel) {
            this.uChannel = uChannel;
            return this;
        }

        public Builder setZeroDuration(float zeroDuration) {
            this.zeroDuration = zeroDuration;
            return this;
        }

        public Builder setNeckType(int neckType) {
            this.neckType = neckType;
            return this;
        }

        public RobotConfig build() {
            return new RobotConfig(this);
        }
    }

    // Getters
    public Activity getActivity() { return activity; }
    public String getSerialPort() { return serialPort; }
    public String getCan() { return can; }
    public boolean isExternalTts() { return externalTts; }
    public boolean isEnableLog() { return enableLog; }
    public boolean isSaveAudio() { return saveAudio; }
    public int getDelaySeconds() { return delaySeconds; }
    public String getExt() { return ext; }
    public String getUAppKey() { return uAppKey; }      // 新增getter
    public String getUChannel() { return uChannel; }     // 新增getter
    public float getZeroDuration() { return zeroDuration; } // 新增getter

    public int getNeckType() {
        return neckType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("RobotConfig {\n");
        sb.append("  activity = ").append(activity != null ? activity.getClass().getSimpleName() : "null").append("\n");
        sb.append("  serialPort = '").append(serialPort).append("'\n");
        sb.append("  can = '").append(can).append("'\n");
        sb.append("  externalTts = ").append(externalTts).append("\n");
        sb.append("  enableLog = ").append(enableLog).append("\n");
        sb.append("  saveAudio = ").append(saveAudio).append("\n");
        sb.append("  delaySeconds = ").append(delaySeconds).append("\n");
        sb.append("  ext = '").append(ext).append("'\n");
        sb.append("  uAppKey = '").append(uAppKey).append("'\n");
        sb.append("  uChannel = '").append(uChannel).append("'\n");
        sb.append("  zeroDuration = ").append(zeroDuration).append("\n");
        sb.append("  neckType = '").append(neckType).append("'\n");
        sb.append("}");
        return sb.toString();
    }
}