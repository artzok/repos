package com.artzok.downloader.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.MsgEvent;
import com.artzok.downloader.utils.MessageUtils;
import com.artzok.downloader.utils.Utils;

import java.util.ArrayList;

/**
 * name：赵坤 on 2018/12/18 15:11
 * email：zhaokun@ziipin.com
 */
public class ClientHandler extends Handler {
    private static final String TAG = "ClientHandler";

    private Context mContext;
    private ComponentUiListener mCallback;

    public ClientHandler(Context context) {
        super();
        mContext = context;
        mCallback = new ComponentUiListener();
    }

    public void registerUiListener(UIListener listener) {
        checkValidInvoke();
        mCallback.registerUiListener(listener);
    }


    public void unregisterUiListener(UIListener listener) {
        checkValidInvoke();
        mCallback.unregisterUiListener(listener);

    }

    public void unregisterAll() {
        checkValidInvoke();
         mCallback.unregisterAll();
    }

    /**
     * 检查是初始化并且在非后台进程调用
     */
    private void checkValidInvoke() {
        if (!Utils.isUiProcess(mContext))
            throw new RuntimeException("Must invoke method on ui process and init on Application.onCreate.");
    }

    @Override
    public void handleMessage(Message msg) {
        MessageUtils.ServiceRespMsg respMsg = new MessageUtils.ServiceRespMsg(msg);
        int response = respMsg.response();
        if (response == MsgEvent.Service.ACTION_RESPONSE) {
            handleActionResp(respMsg);
        } else {
            handleDownloadResp(respMsg);
        }
    }

    private void handleActionResp(MessageUtils.ServiceRespMsg respMsg) {
        int clientAction = respMsg.getClientAction();
        TaskConfig config = respMsg.config();
        switch (clientAction) {
            case MsgEvent.Client.DOWNLOAD:
                mCallback.onRespDownload(config, respMsg.getPendingCount());
                break;
            case MsgEvent.Client.CANCEL:
                mCallback.onRespCancel(config, respMsg.getResult());
                break;
            case MsgEvent.Client.REMOVE:
                mCallback.onRespRemove(config, respMsg.getResult());
                break;
        }
    }

    private void handleDownloadResp(MessageUtils.ServiceRespMsg respMsg) {
        int response = respMsg.response();
        TaskConfig config = respMsg.config();
        switch (response) {
            case MsgEvent.Service.FAILED:
                mCallback.failed(config,respMsg.getErrCode(), respMsg.getException());
                break;
            case MsgEvent.Service.STARTED:
                mCallback.started(config, respMsg.getSofar());
                break;
            case MsgEvent.Service.CONNECTED:
                mCallback.connected(config, respMsg.getTotal());
                break;
            case MsgEvent.Service.FINISHED:
                mCallback.finished(config, respMsg.getMd5());
                break;
            case MsgEvent.Service.RETRY:
                mCallback.retry(config, respMsg.getException(), respMsg.getTimes());
                break;
            case MsgEvent.Service.WARN:
                mCallback.started(config, respMsg.getSofar());
                break;
            case MsgEvent.Service.DOWNLOADING:
                mCallback.downloading(config, respMsg.getSofar(), respMsg.getTotal());
                break;
        }
    }

    private static class ComponentUiListener implements UIListener {
        private final ArrayList<UIListener> mUIListeners;

        ComponentUiListener() {
            mUIListeners = new ArrayList<>();
        }

        void registerUiListener(UIListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("The listener is null.");
            }
            synchronized (mUIListeners) {
                if (mUIListeners.contains(listener)) {
                    throw new IllegalStateException("UIListener " + listener + " is already registered.");
                }
                mUIListeners.add(listener);
            }
        }


        public void unregisterUiListener(UIListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("The listener is null.");
            }
            synchronized (mUIListeners) {
                int index = mUIListeners.indexOf(listener);
                if (index == -1) {
                    throw new IllegalStateException("UIListener " + listener + " was not registered.");
                }
                mUIListeners.remove(index);
            }
        }

        void unregisterAll() {
            synchronized (mUIListeners) {
                mUIListeners.clear();
            }
        }

        @Override
        public void onRespDownload(TaskConfig config, int pendingCount) {
            for (UIListener listener : mUIListeners)
                listener.onRespDownload(config, pendingCount);
        }

        @Override
        public void onRespCancel(TaskConfig config, boolean result) {
            for (UIListener listener : mUIListeners)
                listener.onRespCancel(config, result);
        }

        @Override
        public void onRespRemove(TaskConfig config, boolean result) {
            for (UIListener listener : mUIListeners)
                listener.onRespRemove(config, result);
        }

        @Override
        public void failed(TaskConfig config,int errCode, Exception e) {
            for (UIListener listener : mUIListeners)
                listener.failed(config, errCode, e);
        }

        @Override
        public void started(TaskConfig config, long sofar) {
            for (UIListener listener : mUIListeners)
                listener.started(config, sofar);
        }

        @Override
        public void connected(TaskConfig config, long contentLength) {
            for (UIListener listener : mUIListeners)
                listener.connected(config, contentLength);
        }

        @Override
        public void finished(TaskConfig config, String md5) {
            for (UIListener listener : mUIListeners)
                listener.finished(config, md5);
        }

        @Override
        public void retry(TaskConfig config, Exception e, int times) {
            for (UIListener listener : mUIListeners)
                listener.retry(config, e, times);
        }

        @Override
        public void warn(TaskConfig config, Exception e) {
            for (UIListener listener : mUIListeners)
                listener.warn(config, e);
        }

        @Override
        public void downloading(TaskConfig config, long sofar, long total) {
            for (UIListener listener : mUIListeners)
                listener.downloading(config, sofar, total);
        }
    }
}
