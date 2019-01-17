package com.artzok.downloader.download;

import com.artzok.downloader.download.intercepter.ConnectInterceptor;
import com.artzok.downloader.download.intercepter.DownloadBeginInterceptor;
import com.artzok.downloader.download.intercepter.DownloadingInterceptor;
import com.artzok.downloader.download.intercepter.FinishedInterceptor;
import com.artzok.downloader.share.ErrCodes;
import com.artzok.downloader.share.TaskConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * name：赵坤 on 2018/12/20 14:32
 * email：zhaokun@ziipin.com
 */
public class RealTask extends downloadRunnable implements Task {
    private final Downloader mDownloader;
    private final TaskConfig mOriginTaskConfig;
    private volatile InterruptControl mInterruptControl;
    private final ProxyCallback mCallbackImpl;
    private volatile boolean mPending;

    public RealTask(Downloader downloader,
                    TaskConfig taskConfig, String name,
        DownloadListener listener) {
        super(name);

        mDownloader = downloader;
        mOriginTaskConfig = taskConfig;
        mCallbackImpl = new ProxyCallback(downloader.dbHelper(), listener);
        mInterruptControl = null;
    }

    @Override
    public TaskConfig config() {
        return mOriginTaskConfig;
    }

    @Override
    public boolean cancel() {
        if (mInterruptControl != null &&
                !mInterruptControl.isCanceled()) {
            mInterruptControl.cancel();
            return true;
        }
        return false;
    }

    @Override
    public boolean remove() {
        return mPending && mDownloader.dispatcher().remove(this);
    }

    @Override
    public int download() {
        int ret = mDownloader.dispatcher().enqueue(this);
        if (ret > 0) mPending = true;
        else if(ret == 0) mPending = false;
        return ret;
    }

    @Override
    protected void execute() {
        mPending = false;
        mInterruptControl = new InterruptControl(mOriginTaskConfig);
        List<Interceptor> interceptors = getInterceptors(mInterruptControl);
        int retryTimes = 0;
        try {
            while (true) {
                ChainImpl chain = new ChainImpl(this, mDownloader.dbHelper(), mCallbackImpl);
                try {
                    for (Interceptor interceptor : interceptors)
                        if (interceptor.intercept(chain))  // throw failed canceled
                            break; // true 中断
                    break;
                } catch (Exception e) {
                   int errCode = ErrCodes.UNKNOWN;
                   boolean interrupt = false;
                    if(e instanceof ResponseException) {
                        ResponseException repExp = (ResponseException) e;
                        interrupt = repExp.isInterruptedRetry();
                        errCode = repExp.mErrCode;
                        e = repExp.mOrigin;
                    }
                    if (interrupt || !mInterruptControl.shouldRetry()) {
                        mCallbackImpl.failed(config(), errCode, e);
                        break;
                    } else {
                        // no network
                        mCallbackImpl.retry(config(), e, ++retryTimes);
                        mInterruptControl.retry();
                    }
                }

            }
        } finally {
            mDownloader.dispatcher().finished(this);
            mPending = false;
        }
    }

    private List<Interceptor> getInterceptors(InterruptControl control) {
        ArrayList<Interceptor> list = new ArrayList<>();
        list.add(new DownloadBeginInterceptor());
        list.add(new ConnectInterceptor(control));
        list.add(new DownloadingInterceptor(control));
        list.add(new FinishedInterceptor());
        return list;
    }

    @Override
    public String toString() {
        return "RealTask{" +
                ", mOriginTaskConfig=" + mOriginTaskConfig +
                ", mInterruptControl=" + mInterruptControl +
                '}';
    }
}
