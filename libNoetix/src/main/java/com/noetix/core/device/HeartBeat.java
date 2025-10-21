package com.noetix.core.device;

import com.noetix.core.api.ApiManager;
import com.noetix.utils.KLog;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartBeat {
    private static final String TAG ="HeartBeat";
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void init(){
        scheduler.scheduleAtFixedRate(this::sendHeartBeatMessage, 1000 ,3*60*1000, TimeUnit.MILLISECONDS);
    }

    private void sendHeartBeatMessage(){
        KLog.d(TAG,"sendHeartBeatMessage");
        ApiManager.getInstance().sendHeartbeat();
    }

    public void unInit(){
        if (scheduler!=null && !scheduler.isShutdown()){
            scheduler.shutdown();
        }
    }
}
