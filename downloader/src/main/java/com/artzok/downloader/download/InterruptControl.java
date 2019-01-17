package com.artzok.downloader.download;

import android.os.SystemClock;

import com.artzok.downloader.share.TaskConfig;

/**
 * name：赵坤 on 2018/12/20 15:29
 * email：zhaokun@ziipin.com
 */
public final class InterruptControl implements Cancelable {

    private volatile int mRetryTimes;
    private volatile boolean mCanceled;
    private final int mRetryInterval;

    public InterruptControl(TaskConfig config) {
        mRetryTimes = Math.min(Math.max(config.getRetryTimes(), 0), 5);
        mRetryInterval = mRetryTimes;
        mCanceled = false;
    }

    public boolean shouldRetry() {
        synchronized (this) {
            return mRetryTimes > 0 && !mCanceled;
        }
    }

    public void retry() {
        synchronized (this) {
            mRetryTimes--;
        }
    }

    public void cancel() {
        if (mRetryInterval > 0)
            SystemClock.sleep(mRetryInterval);
        synchronized (this) {
            mCanceled = true;
        }
    }

    public boolean isCanceled() {
        synchronized (this) {
            return mCanceled;
        }
    }
}
