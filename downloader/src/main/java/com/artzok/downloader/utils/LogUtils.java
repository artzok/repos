package com.artzok.downloader.utils;

import android.util.Log;

/**
 * name：赵坤 on 2018/12/17 17:13
 * email：zhaokun@ziipin.com
 */
public class LogUtils {
    public static final boolean debug = true; // BuildConfig.DEBUG;

    public static void d(String tag, String msg) {
        if (debug) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (debug) Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (debug) Log.w(tag, msg);
    }
}
