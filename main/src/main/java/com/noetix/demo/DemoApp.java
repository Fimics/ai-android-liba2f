package com.noetix.demo;

import android.app.Application;
import android.util.Log;

import com.noetix.libnoetix.IRobotSDKManager;


public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("DemoApp","onCreate");
        CANShell.executeCanCommands();

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
