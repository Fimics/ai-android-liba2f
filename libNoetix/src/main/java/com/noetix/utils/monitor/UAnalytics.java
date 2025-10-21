package com.noetix.utils.monitor;

import com.noetix.utils.AppGlobals;
import com.umeng.analytics.MobclickAgent;

import java.util.Map;

public class UAnalytics {

    public void setCatchUncaughtExceptions(){
        MobclickAgent.setCatchUncaughtExceptions(true);
    }

    public void onPageStart(String pageName){
        MobclickAgent.onPageStart(pageName);
    }

    public void onPageEnd(String pageName){
        MobclickAgent.onPageEnd(pageName);
    }

    public void onError(String message){
        MobclickAgent.reportError(AppGlobals.getApplication(),message);
    }

    public void onEvent(String eventName){
        MobclickAgent.onEvent(AppGlobals.getApplication(),eventName);
    }

    public void onEvent(String eventName,String eventValue){
          MobclickAgent.onEvent(AppGlobals.getApplication(),eventName,eventName);
    }

    public void onEvent(String eventName, Map<String,String> map){
        MobclickAgent.onEvent(AppGlobals.getApplication(),eventName,map);
    }
}
