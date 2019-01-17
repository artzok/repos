package com.artzok.downloader.services;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;


import com.artzok.downloader.download.DownloadListener;
import com.artzok.downloader.download.Downloader;
import com.artzok.downloader.download.Task;
import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.MsgEvent;
import com.artzok.downloader.utils.MessageUtils;

import java.util.HashMap;

/**
 * name：赵坤 on 2018/12/17 14:39
 * email：zhaokun@ziipin.com
 */
public class ServiceHandler extends Handler {
    private static final String TAG = "ServiceHandler";

    private ClientsCallback mCallback;
    private Downloader mDownloader;
    private DownloadListenerImpl mDownloadListener;

    public ServiceHandler(Looper looper, Downloader downloader, ClientsCallback callback) {
        super(looper);
        mCallback = callback;
        mDownloader = downloader;
        mDownloadListener = new DownloadListenerImpl(mCallback);
    }

    @Override
    public void handleMessage(Message msg) {
        MessageUtils.ClientActionMsg actionMsg = new MessageUtils.ClientActionMsg(msg);
        int action = actionMsg.action();
        TaskConfig config = actionMsg.config();
        switch (action) {
            case MsgEvent.Client.DOWNLOAD:
                download(config);
                break;
            case MsgEvent.Client.CANCEL:
                cancel(config);
                break;
            case MsgEvent.Client.REMOVE:
                remove(config);
                break;
            default:
                Log.d(TAG, "no valid action:" + action);
                break;
        }
    }

    private void download(TaskConfig config) {
        mDownloadListener.putClientAction(config.getTaskId(), MsgEvent.Client.DOWNLOAD);
        Task task = mDownloader.createTask(config, mDownloadListener);
        int pendingCount = task.download();
        // task event download
        MessageUtils.ServiceRespMsg msg =
                new MessageUtils.ServiceRespMsg(
                        MsgEvent.Service.ACTION_RESPONSE, MsgEvent.Client.DOWNLOAD, config).
                        setPendingCount(pendingCount);
        mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
    }

    private void cancel(TaskConfig config) {
        mDownloadListener.putClientAction(config.getTaskId(), MsgEvent.Client.CANCEL);
        Task task = mDownloader.createTask(config, mDownloadListener);
        boolean result = task.cancel();
        // task event cancel
        MessageUtils.ServiceRespMsg msg =
                new MessageUtils.ServiceRespMsg(
                        MsgEvent.Service.ACTION_RESPONSE, MsgEvent.Client.CANCEL, config).
                        setResult(result);
        mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
    }

    private void remove(TaskConfig config) {
        mDownloadListener.putClientAction(config.getTaskId(), MsgEvent.Client.REMOVE);
        Task task = mDownloader.createTask(config, mDownloadListener);
        boolean result = task.remove();
        // task event cancel
        MessageUtils.ServiceRespMsg msg =
                new MessageUtils.ServiceRespMsg(
                        MsgEvent.Service.ACTION_RESPONSE, MsgEvent.Client.REMOVE, config).
                        setResult(result);
        mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
    }


    private static class DownloadListenerImpl implements DownloadListener {
        private ClientsCallback mCallback;
        private HashMap<String, Integer> mClientAction;

        DownloadListenerImpl(ClientsCallback callback) {
            this.mCallback = callback;
            mClientAction = new HashMap<>();
        }

        void putClientAction(String taskId, int action) {
            mClientAction.put(taskId, action);
        }

        int getClientAction(String taskId) {
            return mClientAction.get(taskId);
        }

        @Override
        public void failed(TaskConfig config, int errCode, Exception e) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.FAILED,
                            getClientAction(config.getTaskId()), config)
                            .setErrCode(errCode)
                            .setException(e);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void started(TaskConfig config, long sofar) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.STARTED,
                            getClientAction(config.getTaskId()), config)
                            .setSofar(sofar);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void connected(TaskConfig config, long contentLength) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.CONNECTED,
                            getClientAction(config.getTaskId()), config)
                            .setTotal(contentLength);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void finished(TaskConfig config, String md5) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.FINISHED,
                            getClientAction(config.getTaskId()), config)
                            .setMd5(md5);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void retry(TaskConfig config, Exception e, int times) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.RETRY,
                            getClientAction(config.getTaskId()), config)
                            .setException(e)
                            .setTimes(times);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void warn(TaskConfig config, Exception e) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.WARN,
                            getClientAction(config.getTaskId()), config)
                            .setException(e);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }

        @Override
        public void downloading(TaskConfig config, long sofar, long total) {
            MessageUtils.ServiceRespMsg msg =
                    new MessageUtils.ServiceRespMsg(MsgEvent.Service.DOWNLOADING,
                            getClientAction(config.getTaskId()), config)
                            .setSofar(sofar)
                            .setTotal(total);
            mCallback.sendMsgToClients(msg.getMsg(), config.getTaskPackageName());
        }
    }
}
