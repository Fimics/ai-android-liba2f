package com.noetix.core;

import android.app.Activity;
import android.content.Context;

import com.noetix.core.upgrade.NAppUpdater;
import com.noetix.core.api.ApiManager;
import com.noetix.core.device.HeartBeat;

public class CloudSetting {
    private static final String TAG="CoreManager";
    private final HeartBeat heartBeat = new HeartBeat();

    private static final class Holder{
        private static final CloudSetting instance = new CloudSetting();
    }

    public static CloudSetting getInstance(){
        return Holder.instance;
    }

    public void updateApk(Activity activity){
        ApiManager.getInstance().initialize();
        NAppUpdater.createInstance(activity);
    }

    public void updateConfig(){
      ApiManager.getInstance().fetchDeviceInfo();
    }

    public void sendHeartBeat(){
        heartBeat.init();
    }

    public void unInit(){
      //TODO
    }
}
