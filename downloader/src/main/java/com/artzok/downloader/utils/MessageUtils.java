package com.artzok.downloader.utils;

import android.os.Bundle;
import android.os.Message;

import com.artzok.downloader.share.TaskConfig;

/**
 * name：赵坤 on 2018/12/29 14:29
 * email：zhaokun@ziipin.com
 */
public final class MessageUtils {

    public static class ClientActionMsg {
        private Message mMsg;
        private int mAction;
        private TaskConfig mTaskConfig;

        public ClientActionMsg(Message msg) {
            mMsg = msg;
            mAction = msg.what;
            mTaskConfig = new TaskConfig(msg.getData());
        }

        public ClientActionMsg(int action, TaskConfig config) {
            mMsg = Message.obtain(null, action);
            config.saveToBundle(mMsg.getData());
            mAction = action;
            mTaskConfig = config;
        }

        public Message msg() {
            return mMsg;
        }

        public int action() {
            return mAction;
        }

        public TaskConfig config() {
            return mTaskConfig;
        }
    }

    public static class ServiceRespMsg {
        private Message mMsg;

        public ServiceRespMsg(Message msg) {
            mMsg = msg;
        }

        public ServiceRespMsg(int resp, int clientAction, TaskConfig config) {
            mMsg = Message.obtain(null, resp);
            mMsg.arg1 = clientAction;
            config.saveToBundle(mMsg.getData());
        }

        public Message getMsg() {
            return mMsg;
        }

        public int response() {
            return mMsg.what;
        }

        public int getClientAction() {
            return mMsg.arg1;
        }

        public TaskConfig config() {
            return new TaskConfig(mMsg.getData());
        }

        public ServiceRespMsg setPendingCount(int count) {
            mMsg.arg2 = count;
            return this;
        }

        public int getPendingCount() {
            return mMsg.arg2;
        }

        public ServiceRespMsg setException(Exception e) {
            Bundle data = mMsg.getData();
            data.putSerializable("exp", e); // todo should use parcelable
            return this;
        }

        public Exception getException() {
            Bundle data = mMsg.getData();
            return (Exception) data.getSerializable("exp");
        }

        public ServiceRespMsg setSofar(long sofar) {
            Bundle data = mMsg.getData();
            data.putLong("sofar", sofar);
            return this;
        }

        public long getSofar() {
            return mMsg.getData().getLong("sofar");
        }

        public ServiceRespMsg setTotal(long total) {
            mMsg.getData().putLong("total", total);
            return this;
        }

        public long getTotal() {
            return mMsg.getData().getLong("total");
        }

        public ServiceRespMsg setTimes(int times) {
            mMsg.arg2 = times;
            return this;
        }

        public int getTimes() {
            return mMsg.arg2;
        }

        public boolean getResult() {
            return mMsg.arg2 == 1;
        }

        public ServiceRespMsg setResult(boolean res) {
            mMsg.arg2 = res ? 1 : 0;
            return this;
        }

        public String getMd5() {
            return mMsg.getData().getString("md5");
        }

        public ServiceRespMsg setMd5(String md5) {
            mMsg.getData().putString("md5", md5);
            return this;
        }

        public ServiceRespMsg setErrCode(int errCode) {
            mMsg.arg2 = errCode;
            return this;
        }

        public int getErrCode() {
            return mMsg.arg2;
        }
    }

}
