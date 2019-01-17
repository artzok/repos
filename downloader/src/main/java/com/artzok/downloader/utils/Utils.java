package com.artzok.downloader.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * name：赵坤 on 2018/12/17 18:13
 * email：zhaokun@ziipin.com
 */
public class Utils {
    private static final String TAG = "badam.download.utils";

    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        if (manager == null) return null;
        List<ActivityManager.RunningAppProcessInfo> info = manager.getRunningAppProcesses();
        if (info != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : info) {
                if (processInfo.pid == pid) {
                    return processInfo.processName;
                }
            }
        }
        return null;
    }

    public static boolean isUiProcess(Context context) {
        String name = Utils.getProcessName(context);
        LogUtils.d("process", name);
        return !TextUtils.isEmpty(name) && name.equals(context.getPackageName());
    }

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2)
                stringBuilder.append(0);
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
