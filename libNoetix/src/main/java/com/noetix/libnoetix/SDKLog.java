package com.noetix.libnoetix;

import android.text.TextUtils;
import android.util.Log;

import com.noetix.BuildConfig;

public class SDKLog {

    public static void i(String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.i(SDKContext.TAG, message);
    }

    public static void v(String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.v(SDKContext.TAG, message);
    }


    public static void d(String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.d(SDKContext.TAG, message);
    }


    public static void w(String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.w(SDKContext.TAG, message);
    }

    public static void e(String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }
        Log.e(SDKContext.TAG, message);
    }

    public static void i(String tag, String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.i(tag, message);
    }

    public static void v(String tag, String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.v(tag, message);
    }


    public static void d(String tag, String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }
        Log.d(tag, message);
    }


    public static void w(String tag, String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }

        Log.w(tag, message);
    }

    public static void e(String tag, String message) {
        if (TextUtils.isEmpty(message) || !SDKContext.isEnableLog) {
            return;
        }
        Log.e(tag, message);
    }

}
