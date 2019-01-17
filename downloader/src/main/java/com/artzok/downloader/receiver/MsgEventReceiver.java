package com.artzok.downloader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;

import com.artzok.downloader.manager.DownloadManager;


/**
 * name：赵坤 on 2018/12/19 11:59
 * email：zhaokun@ziipin.com
 */
public class MsgEventReceiver extends BroadcastReceiver {
    public static final String DEFAULT_CATEGORY_ACTION = "com.ziipin.badam.MsgEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        int what = intent.getIntExtra("what", 0);
        int arg1 = intent.getIntExtra("arg1", 0);
        int arg2 = intent.getIntExtra("arg2", 0);
        Parcelable obj = intent.getParcelableExtra("obj");
        Bundle data = intent.getParcelableExtra("data");
        Message message = Message.obtain(null, what, arg1, arg2, obj);
        message.setData(data);
        if(DownloadManager.hasInstance())
            DownloadManager.getManager(context).onReceiverMsgEvent(message);
    }
}
