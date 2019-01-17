package com.artzok.downloader.manager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.Constants;
import com.artzok.downloader.share.ErrCodes;
import com.artzok.downloader.share.ForegroundServiceConfig;
import com.artzok.downloader.share.MessengerParcelable;
import com.artzok.downloader.share.MsgEvent;
import com.artzok.downloader.utils.LogUtils;
import com.artzok.downloader.utils.MessageUtils;
import com.artzok.downloader.utils.Utils;


/**
 * name：赵坤 on 2018/12/17 14:38
 * email：zhaokun@ziipin.com
 * <p>
 * ui 进程的管理类，用于封装 ui 与 downloadManger（.download进程）的操作
 * <p>
 * <p>
 * 1. 当用户退出时，如果没有任务则停止服务
 * 2. 当用户退出时，有任务则下载完成之后退出服务
 * 3. 当应用启动时，尝试启动服务
 * 当用户进入 ui 时 绑定服务，建立 messenger 通讯
 * 当用户退出 ui 时 解绑服务, 取消 messenger 通讯，改用 broadcast 通讯
 */
public final class DownloadManager {
    private static final String TAG = "download_manager";
    private static final String HOST_APP_PACKAGE_NAME = "com.badam.androiddownload";

    private static DownloadManager sManager;

    public static boolean hasInstance() {
        return sManager != null;
    }

    public static DownloadManager getManager(Context context) {
        if (sManager == null) {
            synchronized (DownloadManager.class) {
                if (sManager == null)
                    sManager = new DownloadManager(context);
            }
        }
        return sManager;
    }

    private Context mContext;
    private Activity mActivity;

    private Intent mServiceIntent;
    private ServiceConnection mConn;

    private ClientHandler mClientHandler;
    private Messenger mClientMessenger; /* wrap client handler: receiver download service msg */
    private Messenger mServiceMessenger; /* wrap service handler: send download service msg */


    private ForegroundServiceConfig mForegroundConfig;

    private DownloadFactory mDownloadFactory;

    private DownloadManager(Context context) {
        mContext = context.getApplicationContext();
    }

    public void init(DownloadFactory factory, UIListener listener) {
        String name = Utils.getProcessName(mContext);
        LogUtils.d("process", name);

        // just ui process
        if (TextUtils.isEmpty(name) || !Utils.isUiProcess(mContext))
            return;

        mDownloadFactory = factory;

        mClientHandler = new ClientHandler(mContext);
        mClientHandler.registerUiListener(listener);
        // ensure service running
        startService(mDownloadFactory);
    }

    public ClientHandler clientHandler() {
        return mClientHandler;
    }

    public void download(TaskConfig config) throws Exception {
        sendServiceMessage(new MessageUtils.ClientActionMsg(MsgEvent.Client.DOWNLOAD, config).msg());
    }

    public void cancel(TaskConfig config) throws Exception {
        sendServiceMessage(new MessageUtils.ClientActionMsg(MsgEvent.Client.CANCEL, config).msg());
    }

    public void remove(TaskConfig config) throws Exception {
        sendServiceMessage(new MessageUtils.ClientActionMsg(MsgEvent.Client.REMOVE, config).msg());
    }

    public void onReceiverMsgEvent(Message msg) {
        try {
            if (mClientMessenger != null)
                mClientMessenger.send(msg);
            if ((msg.arg2 & ErrCodes.REMOTE_HANDlER_DEAD) != 0) {
                Log.d(TAG, "service's client target dead");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void onCreate(Activity activity) {
        if (factoryChanged()) startService(mDownloadFactory);
        if (mConn == null) {
            mActivity = activity;
            mClientMessenger = new Messenger(mClientHandler);
            MessengerParcelable mp = new MessengerParcelable(mClientMessenger);
            mServiceIntent.putExtra(Constants.EXTRA_CLIENT_MESSENGER, mp);
            mConn = new ServiceConnectionImpl();
            mActivity.bindService(mServiceIntent, mConn, Context.BIND_AUTO_CREATE);
        }
    }

    public void onDestroy(Activity activity) {
        if (mConn != null) {
            mActivity.unbindService(mConn);
            mConn = null;
        }
    }

    private void startService(DownloadFactory factory) {
        if (factory == null) throw new RuntimeException("factory == null");
        mForegroundConfig = factory.getForegroundConfig();
        // ui process build, start service
        mServiceIntent = new Intent();
        mServiceIntent.setAction(Constants.DOWNLOAD_SERVICE_DEFAULT_CATEGORY_ACTION);
        mServiceIntent.setPackage(HOST_APP_PACKAGE_NAME);
        mServiceIntent.putExtra(Constants.TASK_PACKAGE_NAME, mContext.getPackageName());

        if (mForegroundConfig != null) {
            if (mForegroundConfig.notifyId <= 0) throw new RuntimeException("notifyId <= 0");
            if(mForegroundConfig.notification == null) throw new RuntimeException("notification == null");
            mServiceIntent.putExtra(Constants.EXTRA_FOREGROUND_ID, mForegroundConfig.notifyId);
            mServiceIntent.putExtra(Constants.EXTRA_FOREGROUND_NOTIFICATION, mForegroundConfig.notification);
        }

        ComponentName cn = null;
        try {
            cn = mContext.startService(mServiceIntent);
        } catch (SecurityException e) {
            Log.d(TAG, "need badam download service permission.");
        } catch (IllegalStateException e) {
            // foreground service notification
            if (mForegroundConfig != null
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                cn = mContext.startForegroundService(mServiceIntent);
            } else {
                Log.d(TAG, "can't launcher service", e);
            }
        } finally {
            if (cn != null)
                Log.d(TAG, "start service by :" + cn.flattenToString());
        }
    }

    /**
     * 向服务发送消息，添加必要信息
     */
    private void sendServiceMessage(Message msg) throws Exception {
        if (factoryChanged()) startService(mDownloadFactory);
        if (mServiceMessenger != null) {
            mServiceMessenger.send(msg);
        }
    }

    private boolean factoryChanged() {
        return mDownloadFactory != null && mForegroundConfig
                != mDownloadFactory.getForegroundConfig();
    }

    private class ServiceConnectionImpl implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceMessenger = null;
        }
    }
}
