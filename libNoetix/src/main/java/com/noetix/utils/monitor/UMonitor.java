package com.noetix.utils.monitor;

import android.content.Context;
import android.text.TextUtils;

import com.noetix.utils.AppGlobals;
import com.noetix.utils.KLog;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.commonsdk.listener.OnGetOaidListener;

public class UMonitor {

    private static final String TAG = "UMonitor";

    private static class Holder {
        private static final UMonitor instance = new UMonitor();
    }

    public static UMonitor getInstance() {
        return Holder.instance;
    }

    public void init(String appKey,String channel) {

        KLog.d(TAG,"UMonitor init appKey :"+appKey +"  channel "+channel);
        if (TextUtils.isEmpty(appKey)){
            KLog.d(TAG,"umeng appKey 为空...");
            return;
        }
        KLog.d(TAG,"初始化umeng ...");
        Context context = AppGlobals.getApplication();
        UMConfigure.setLogEnabled(true);
        UMConfigure.preInit(context,appKey,channel);
        UMConfigure.init(context,appKey,channel, UMConfigure.DEVICE_TYPE_BOX, "");
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public Boolean isInit(){
        return UMConfigure.isInit;
    }

    public String getUZid() {
        return UMConfigure.getUmengZID(AppGlobals.getApplication());
    }

    public String getUId() {
        String uid = UMConfigure.getUMIDString(AppGlobals.getApplication());
        return uid;
    }

    public String[] getTestDeviceInfo() {
        String[] info = UMConfigure.getTestDeviceInfo(AppGlobals.getApplication());
        return info;
    }

    public void initOAid() {
        UMConfigure.getOaid(AppGlobals.getApplication(), new OnGetOaidListener() {
            @Override
            public void onGetOaid(String s) {
                KLog.d(TAG,"oaid ->"+s);
//                PContext.get().setOAid(s);
            }
        });
    }

}
