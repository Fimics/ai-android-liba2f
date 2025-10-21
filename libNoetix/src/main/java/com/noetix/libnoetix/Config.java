package com.noetix.libnoetix;

import android.os.Environment;

import com.noetix.utils.KLog;

import java.io.File;

class Config {

    public static final String ROBOT_CONFIG_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath() + "/robot_config";
    public static final String CONFIG_PATH = ROBOT_CONFIG_FOLDER + "/29_servo_config.yaml";
    public static final String ZERO_ANGLE_PATH = ROBOT_CONFIG_FOLDER + "/default_zero_angle.yaml";
    public static final String PATH_CHAT = ROBOT_CONFIG_FOLDER + "/expression29_chat";
    public static final String PATH_TRACK = ROBOT_CONFIG_FOLDER + "/expression29_track";
    public static final String LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/logs/";
    //robot_data/unitalker_960_simplified.rknn";
     public static final String modelPath = Environment.getExternalStorageDirectory() + "/robot_data/unitalker_960_simplified.rknn";
    public static final String MODEL_PAtH = Environment.getExternalStorageDirectory() + "/robot_data";

    public void tryCreateNativeDir() {
        doTryCreateTargetDir();
    }

    private void doTryCreateTargetDir() {
        tryCreateTargetDir(ROBOT_CONFIG_FOLDER);
        tryCreateTargetDir(LOG_PATH);
        tryCreateTargetDir(MODEL_PAtH);

//        TaskExecutors.get().onIOTask(() -> {
//            tryCreateTargetDir(ROBOT_CONFIG_FOLDER);
//            tryCreateTargetDir(LOG_PATH);
//        });

    }

    public void tryCreateTargetDir(String path) {
        File dir = new File(path);

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                KLog.d("robot_config directory created successfully at: " + dir.getAbsolutePath());
            } else {
                KLog.e("Failed to create robot_config directory at: " + dir.getAbsolutePath());
            }
        } else {
            KLog.d("robot_config directory already exists at: " + dir.getAbsolutePath());
        }
    }

}
