package com.artzok.downloader.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * name：赵坤 on 2018/12/17 11:50
 * email：zhaokun@ziipin.com
 */
public class DownloadService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getServiceMessenger().onStartCommand(this, intent, flags, startId);
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return getServiceMessenger().onBind(this, intent);
    }

    @Override
    public void onRebind(Intent intent) {
        getServiceMessenger().onBind(this, intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        getServiceMessenger().onUnbind(this, intent);
        return true;
    }

    private ServiceMessenger getServiceMessenger() {
        return ServiceMessenger.getInst(this);
    }


}
