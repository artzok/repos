package com.artzok.downloader.services;

import android.os.Message;

/**
 * name：赵坤 on 2018/12/17 17:54
 * email：zhaokun@ziipin.com
 */
public interface ClientsCallback {
    void sendMsgToClients(Message msg, String taskPackageName);
}
