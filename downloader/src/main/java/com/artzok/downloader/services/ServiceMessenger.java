package com.artzok.downloader.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;


import com.artzok.downloader.BuildConfig;
import com.artzok.downloader.download.Downloader;
import com.artzok.downloader.receiver.MsgEventReceiver;
import com.artzok.downloader.share.Constants;
import com.artzok.downloader.share.ErrCodes;
import com.artzok.downloader.share.MessengerParcelable;
import com.artzok.downloader.share.MsgEvent;
import com.artzok.downloader.utils.LogUtils;
import com.artzok.downloader.utils.MessageUtils;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * name：赵坤 on 2018/12/17 15:04
 * email：zhaokun@ziipin.com
 * 不考虑第三方使用
 */
public final class ServiceMessenger implements ClientsCallback {
    private static final String TAG = "ServiceMessenger";

    private static ServiceMessenger sInst;
    private final ServiceHandler mServiceHandler;
    private final Downloader mDownloader;

    public static ServiceMessenger getInst(Service service) {
        if (sInst == null) {
            synchronized (ServiceMessenger.class) {
                if (sInst == null)
                    sInst = new ServiceMessenger(service);
            }
        }
        return sInst;
    }

    private final Service mService;
    private final Messenger mServiceMessenger;
    private final Map<String, Messenger> mClientMessengers;

    private ServiceMessenger(Service service) {
        mService = service;
        mClientMessengers = Collections.synchronizedSortedMap(new TreeMap<String, Messenger>());
        mDownloader = new Downloader(service);
        mDownloader.dispatcher().setIdleRunnable(new Runnable() {
            @Override
            public void run() {
                checkStopService();
            }
        });
        HandlerThread mThread = new HandlerThread("downloader");
        mThread.start();
        mServiceHandler = new ServiceHandler(mThread.getLooper(), mDownloader, this);
        mServiceMessenger = new Messenger(mServiceHandler);
    }

    private void checkStopService() {
        // 没有任务也没有人连接，自动开
        if (mDownloader.dispatcher().getAllTaskSize() <= 0
                && mClientMessengers.size() <= 0)
            mService.stopForeground(true);
    }

    public void onStartCommand(Service service, Intent intent, int flags, int startId) {
        if (intent == null) return;
        int id = intent.getIntExtra(Constants.EXTRA_FOREGROUND_ID, 0);
        Notification notification = intent.getParcelableExtra(Constants.EXTRA_FOREGROUND_NOTIFICATION);
        // start foreground service
        if (id > 0 && notification != null) service.startForeground(id, notification);
        LogUtils.d(TAG, "onStartCommand: " + intent + ", " + flags + ", " + startId);
    }

    public IBinder onBind(Service service, Intent intent) {
        tryAddClientMessenger(intent);
        return mServiceMessenger.getBinder();
    }

    public void onUnbind(Service service, Intent intent) {
        tryRemoveClientMessenger(intent);
        checkStopService();
    }

    private void tryAddClientMessenger(Intent intent) {
        if (intent != null) {
            MessengerParcelable client = intent.getParcelableExtra(Constants.EXTRA_CLIENT_MESSENGER);
            if (client == null) return;
            Messenger messenger = client.getMessenger();
            if (messenger == null) {
                Log.d(TAG, "client messenger is null or parcel action failed!");
                return;
            }
            String packageName = intent.getStringExtra(Constants.TASK_PACKAGE_NAME);
            if (TextUtils.isEmpty(packageName))
                throw new IllegalArgumentException("start or bind Service Intent must have packageName");
            else if (mClientMessengers.get(packageName) != null)
                LogUtils.d(TAG, "package " + packageName + " has been add client, ignore!");
            else {
                LogUtils.d(TAG, "add client messenger from package: " + packageName);
                mClientMessengers.put(packageName, messenger);
            }
        }
    }

    private void tryRemoveClientMessenger(Intent intent) {
        if (intent != null) {
            String packageName = intent.getStringExtra(Constants.TASK_PACKAGE_NAME);
            if (TextUtils.isEmpty(packageName)) {
                Log.d(TAG, "unbind package name is empty");
            } else {
                if (BuildConfig.DEBUG) {
                    if (mClientMessengers.get(packageName) != null)
                        LogUtils.d(TAG, "remove client messenger from package: " + packageName);
                }
                mClientMessengers.remove(packageName);
            }
        }
    }

    /**
     * 将消息发送到客户端，如果 Messenger 渠道无法发送则使用 broadcast 方式实现
     * 如果 taskPackageName 不为空，则检查 3td app 是否与 service bind
     * 如果没有 bind 则通过 broadcast 方式发送消息
     *
     * @param msg             Messenger
     * @param taskPackageName 指定发起任务的 3td party app package name
     */
    @Override
    public void sendMsgToClients(Message msg, String taskPackageName) {
        // 如果 taskPackageName 不为空
        // 则表明该消息属于某个 app 的事件而不是系统事件, 所以只需要发送给对应应用即可
        if (!TextUtils.isEmpty(taskPackageName)) {
            if (mClientMessengers.containsKey(taskPackageName)) {
                Messenger messenger = mClientMessengers.get(taskPackageName);
                safeSendMsgEvent(messenger, msg, taskPackageName);
            } else sendMessageByBroadcast(msg, taskPackageName);
        } else for (String packageName : mClientMessengers.keySet()) {
            Messenger messenger = mClientMessengers.get(packageName);
            safeSendMsgEvent(messenger, msg, packageName);
        }
    }

    private void safeSendMsgEvent(Messenger messenger, Message msg, String packageName) {
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            Log.d(TAG, "send client msg failed:", e);
            msg.arg2 |= ErrCodes.REMOTE_HANDlER_DEAD;
            sendMessageByBroadcast(msg, packageName);
            mClientMessengers.remove(packageName);
        }
    }

    /**
     * 使用 broadcast 方式实现发送消息到 ui process (contain 3td party app)
     * 注意，避免性能影响，MsgEvent.Service.DWONLOADING 事件不通过 broadcast 方式发送
     *
     * @param msg         Message
     * @param packageName app package name
     */
    private void sendMessageByBroadcast(Message msg, String packageName) {
        if (msg == null || TextUtils.isEmpty(packageName))
            throw new RuntimeException("msg == null || package name is empty");
        if (new MessageUtils.ServiceRespMsg(msg).response() ==
                MsgEvent.Service.DOWNLOADING) {
            Log.d(TAG, "downloading action but can't find binder!");
            return;
        }
        Intent intent = new Intent();
        intent.setAction(MsgEventReceiver.DEFAULT_CATEGORY_ACTION);
        intent.setPackage(packageName); // to specified 3td party app
        intent.putExtra("what", msg.what);
        intent.putExtra("arg1", msg.arg1);
        intent.putExtra("arg2", msg.arg2);
        if (msg.obj != null && !(msg.obj instanceof Parcelable))
            throw new RuntimeException("msg.obj must be Parcelable");
        intent.putExtra("obj", (Parcelable) msg.obj);
        intent.putExtra("data", msg.getData());
        mService.sendBroadcast(intent);
    }
}
