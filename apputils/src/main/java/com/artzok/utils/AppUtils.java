package com.artzok.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.Context.ACTIVITY_SERVICE;

public class AppUtils {


    /**
     * network type
     */
    public enum NET_TYPE {
        NET_2G, NET_3G, NET_4G, NET_WIFI, NET_UNKNOWN, NONE
    }

    /**
     * Operators
     */
    public enum OPERATORS {
        UNKNOWN, MOBILE, UNICOM, TELECOM, NONE
    }

    /**
     * delete service
     */
    private static ExecutorService sDeleteService;

    /**
     * only one Toast instance
     */
    private static Toast sToast;

    /**
     * get package name by apk file
     */
    public static String getPackageName(Context context, File apk) {
        PackageInfo info = AppUtils.getPackageInfo(context, apk);
        if (info != null)
            return info.packageName;
        return "";
    }

    /**
     * get package info by installed app's package name
     */
    public static PackageInfo getPackageInfo(final Context cxt, final String packageName) {
        final PackageManager pm = cxt.getPackageManager();
        try {
            return pm.getPackageInfo(packageName, 0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get my app package info
     */
    public static PackageInfo getPackageInfo(Context context) {
        try {
            return getPackageInfo(context, context.getPackageName());
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * get package info with meta data additional
     */
    public static PackageInfo getPackageInfoWithMeta(final Context cxt, final String packageName) {
        final PackageManager pm = cxt.getPackageManager();
        try {
            return pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * get package info from apk file
     */
    public static PackageInfo getPackageInfo(final Context cxt, final File apk) {
        final PackageManager pm = cxt.getPackageManager();
        try {
            return pm.getPackageArchiveInfo(apk.getAbsolutePath(), 0);
        } catch (final Exception e) {
            return null;
        }
    }

    /**
     * get all app package info of this device
     */
    public static List<PackageInfo> getLocalPackageInfo(final Context cxt) {
        ArrayList<PackageInfo> allInfo = new ArrayList<>();
        try {
            PackageManager pm = cxt.getPackageManager();
            List<PackageInfo> temp = pm.getInstalledPackages(0);
            if (temp != null && temp.size() > 0) {
                allInfo.addAll(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allInfo;
    }

    /**
     * get all user installed app package info of this device
     */
    public static List<PackageInfo> getUserInstalledAppsPackageInfo(final Context cxt) {
        ArrayList<PackageInfo> allInfo = new ArrayList<>();
        try {
            List<PackageInfo> installedInfo = getLocalPackageInfo(cxt);
            if (installedInfo != null && installedInfo.size() > 0) {
                for (PackageInfo info : installedInfo) {
                    if ((info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        allInfo.add(info);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allInfo;
    }

    /**
     * install apk
     */
    public static void install(final Context cxt, final File apk) {
        Intent intent = getInstallIntent(cxt, apk);
        cxt.startActivity(intent);
    }

    /**
     * uninstall app
     */
    public static void uninstall(final Context cxt, final String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        cxt.startActivity(uninstallIntent);
    }

    /**
     * get intent that will use to install apk
     */
    public static Intent getInstallIntent(final Context context, final File apk) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apk), "application/vnd.android.package-archive");
        if (!(context instanceof Activity))
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * determine if the app is installed
     */
    public static boolean isInstalled(final Context cxt, final String packageName) {
        return getPackageInfo(cxt, packageName) != null;
    }

    /**
     * determine if the apk is valid apk package file
     */
    public static boolean isValidApk(final Context cxt, final File apk) {
        return getPackageInfo(cxt, apk) != null;
    }

    /**
     * startup app
     */
    public static void launcher(final Context cxt, final String packageName) {
        try {
            Intent intent = getLauncherIntent(cxt, packageName);
            if (intent != null) cxt.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * get intent which will use to launcher app
     */
    public static Intent getLauncherIntent(Context cxt, String packageName) {
        final PackageManager pm = cxt.getPackageManager();
        final Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent == null) return null;
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (!(cxt instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * get current process name
     */
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

    /**
     * create scaled bitmap by drawable resource
     */
    public static Bitmap createScaledBitmap(Context context, int resId, int width, int height) {
        try {
            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), resId);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, width, height, true);
            bmp.recycle();
            return thumbBmp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * create scaled bitmap by file
     */
    public static Bitmap createScaledBitmap(String path, int width, int height) {
        if (TextUtils.isEmpty(path) || !new File(path).exists()) return null;
        try {
            Bitmap bmp = BitmapFactory.decodeFile(path);
            Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, width, height, true);
            bmp.recycle();
            return thumbBmp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * save bitmap to file
     */
    public static void saveBitmap(Bitmap bmp, File file, boolean needRecycle) {
        try {
            if (file.exists()) file.delete();
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (needRecycle && bmp != null) {
                bmp.recycle();
            }
        }

    }

    /**
     * convert bitmap to byte array
     */
    public static byte[] bitmapToBytes(final Bitmap bmp, final boolean needRecycle) {
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            if (bmp.isRecycled()) {
                return new byte[0];
            }
            bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
            if (needRecycle) {
                bmp.recycle();
            }

            byte[] result = output.toByteArray();
            try {
                output.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        } catch (Exception e) {
            return new byte[0];
        }

    }

    @SuppressLint("HardwareIds")
    public static String getUUID(Context context) {
        try {
            final TelephonyManager tm = getTM(context);
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                return null;
            String tmDevice = tm.getDeviceId();
            String tmSerial = tm.getSimSerialNumber();
            String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            UUID uid = new UUID(androidId.hashCode(),
                    ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
            return uid.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("HardwareIds")
    public static String getSimSerialNumber(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
                return null;
            String simSerialNumber = getTM(context).getSimSerialNumber();
            return simSerialNumber != null ? simSerialNumber : "";
        } catch (Exception e) {
            return "";
        }
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(Context context) {
        try {
            String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
            return androidId != null ? androidId : "";
        } catch (Exception e) {
            return "";
        }
    }

    public static OPERATORS getOperator(Context context) {
        String IMSI = getIMSI(context);
        if (TextUtils.isEmpty(IMSI))
            IMSI = getTM(context).getSimOperator();
        OPERATORS operator = OPERATORS.UNKNOWN;
        if (IMSI != null) {
            if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {
                operator = OPERATORS.MOBILE;
            } else if (IMSI.startsWith("46001") || IMSI.startsWith("46006")) {
                operator = OPERATORS.UNICOM;
            } else if (IMSI.startsWith("46003") || IMSI.startsWith("46005")) {
                operator = OPERATORS.TELECOM;
            }
        }

        if (operator == OPERATORS.UNKNOWN) {
            String name = getTM(context).getSimOperatorName();
            if (name != null) {
                name = name.toLowerCase();
                if ("cmcc".equals(name) || "china mobile".equals(name)) {
                    operator = OPERATORS.MOBILE;
                } else if ("cucc".equals(name) || "china union".equals(name)) {
                    operator = OPERATORS.UNKNOWN;
                } else if ("ctcc".equals(name) || "china telecom".equals(name) || "中国电信".equals(name)) {
                    operator = OPERATORS.TELECOM;
                }
            }
        }

        if (operator == OPERATORS.UNKNOWN && TextUtils.isEmpty(IMSI))
            return OPERATORS.NONE;
        return operator;
    }

    private static TelephonyManager getTM(Context context) {
        return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }


    @SuppressLint("HardwareIds")
    public static String getPhoneNumber(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return getTM(context).getLine1Number();
        } catch (Exception e) {
            return "UNKNOW";
        }
    }

    @SuppressLint("HardwareIds")
    public static String getIMSI(Context context) {
        try {
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return getTM(context).getSubscriberId();
        } catch (Exception e) {
            return "UNKNOW";
        }
    }

    public static String getVersionName(Context context) {
        PackageInfo packageInfo = AppUtils.getPackageInfo(context);
        if (packageInfo != null)
            return packageInfo.versionName;
        else
            return "unknown";
    }

    public static int getVersionCode(Context context, String packageName) {
        try {
            PackageInfo pi = context.getApplicationContext()
                    .getPackageManager().getPackageInfo(packageName, 0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getVersionCode(Context context) {
        PackageInfo packageInfo = AppUtils.getPackageInfo(context);
        if (packageInfo != null)
            return packageInfo.versionCode;
        else
            return -1;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) return wm.getDefaultDisplay().getWidth();
        return 1;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) return wm.getDefaultDisplay().getHeight();
        return 1;
    }

    private static NET_TYPE getNetworkType(final Context cxt) {
        try {
            if (isWifiConnected(cxt)) return NET_TYPE.NET_WIFI;
            final TelephonyManager tm = (TelephonyManager) cxt
                    .getSystemService(Context.TELEPHONY_SERVICE);
            assert tm != null;
            final int networkType = tm.getNetworkType();

            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return NET_TYPE.NET_2G;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return NET_TYPE.NET_3G;
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return NET_TYPE.NET_4G;
                default:
                    return NET_TYPE.NET_UNKNOWN;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return NET_TYPE.NONE;
        }
    }

    public static boolean is2G(final Context cxt) {
        return getNetworkType(cxt) == NET_TYPE.NET_2G;
    }

    public static boolean is3G(final Context cxt) {
        return getNetworkType(cxt) == NET_TYPE.NET_3G;
    }

    public static boolean is4G(final Context cxt) {
        return getNetworkType(cxt) == NET_TYPE.NET_4G;
    }

    public static boolean isAvailable(final Context cxt) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) cxt
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            final NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isConnected(final Context cxt, final int networkType) {
        try {
            final ConnectivityManager cm = (ConnectivityManager) cxt
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            assert cm != null;
            final NetworkInfo ni = cm.getNetworkInfo(networkType);
            return ni != null && ni.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isWifiConnected(final Context cxt) {
        return isConnected(cxt, ConnectivityManager.TYPE_WIFI);
    }

    public static void showLongToast(final Context context, final String text) {
        show(context, text, Toast.LENGTH_LONG);
    }

    public static void showLongToast(final Context context, final int resId) {
        show(context, context.getString(resId), Toast.LENGTH_LONG);
    }

    public static void showShortToast(final Context context, final String text) {
        show(context, text, Toast.LENGTH_SHORT);
    }

    public static void showShortToast(final Context context, final int resId) {
        show(context, context.getString(resId), Toast.LENGTH_SHORT);
    }

    @SuppressLint("ShowToast")
    public static void show(final Context context, final String text, int duration) {
        if (sToast == null) {
            sToast = Toast.makeText(context.getApplicationContext(), text, duration);
        } else {
            sToast.setText(text);
            sToast.setDuration(duration);
        }
        sToast.show();
    }

    public static void installShortcut(final Context context, String title, final int iconResource,
                                       final Bitmap iconBitmap, Intent shortcutIntent) {
        Intent intent = uninstallShortcut(context, title, iconResource, iconBitmap, shortcutIntent);
        intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(intent);
    }

    public static Intent uninstallShortcut(final Context context, String title, final int iconResource,
                                           final Bitmap iconBitmap, Intent shortcutIntent) {
        Intent intent = getShortcutIntent(context, title, iconResource, iconBitmap, shortcutIntent);
        intent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(intent);
        return intent;
    }

    public static Intent getShortcutIntent(Context context, String title, final int iconResource,
                                           final Bitmap iconBitmap, Intent shortcutIntent) {
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
        if (iconResource != -1) {
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(context, iconResource));
        }
        if (iconBitmap != null) {
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, iconBitmap);
        }
        return intent;
    }

    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px * 1.0f / scale + 0.5f);
    }

    public static void forceDeleteFile(File file, boolean sync) {
        ForceDeleteTask task = new ForceDeleteTask(file);
        if (sync) task.run();
        else {
            if (sDeleteService == null)
                sDeleteService = Executors.newCachedThreadPool();
            sDeleteService.submit(task);
        }
    }

    public static void forceDeleteDir(File dir, boolean sync) {
        if (dir.isFile()) forceDeleteFile(dir, sync);
        else if (!dir.delete()) {
            for (File f : dir.listFiles()) {
                forceDeleteDir(f, sync);
            }
            // try delete empty dir
            if (!dir.delete())
                dir.deleteOnExit();
        }
    }

    private static final class ForceDeleteTask implements Runnable {
        private File target;
        private int total;
        private int sleep;

        private ForceDeleteTask(File file) {
            this.target = file;
            total = 3000;
            sleep = 200;
        }

        @Override
        public void run() {
            while (total > 0)
                if (target.exists() &&
                        !target.delete()) {
                    total -= sleep;
                    SystemClock.sleep(sleep);
                } else {
                    break;
                }
        }
    }

    public static void openUrlByBrowse(Context context, String url) {
        if (context == null || TextUtils.isEmpty(url)) return;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        if (!(context instanceof Activity))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}