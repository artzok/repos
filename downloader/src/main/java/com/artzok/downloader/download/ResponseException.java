package com.artzok.downloader.download;

/**
 * name：赵坤 on 2018/12/20 15:47
 * email：zhaokun@ziipin.com
 */
public class ResponseException extends Exception {

    public final int mErrCode;
    public final Exception mOrigin;

    private boolean mInterruptedRetry;

    public ResponseException(int errCode, Exception originEx) {
        super(originEx);
        mOrigin = originEx;
        mErrCode = errCode;
    }

    public boolean isInterruptedRetry() {
        return mInterruptedRetry;
    }

    public void setInterruptedRetry(boolean interruptedRetry) {
        mInterruptedRetry = interruptedRetry;
    }

    @Override
    public String toString() {
        return super.toString() +
                "mErrCode=" + mErrCode +
                '}';
    }
}
