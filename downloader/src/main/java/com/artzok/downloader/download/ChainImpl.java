package com.artzok.downloader.download;

import com.artzok.downloader.db.DbHelper;

/**
 * name：赵坤 on 2018/12/20 15:54
 * email：zhaokun@ziipin.com
 */
public final class ChainImpl implements Interceptor.Chain {

    private final Task mTask;
    private final DownloadListener mDownloadListener;
    private final DbHelper mDbHelper;

    private Object tmpObj;

    public ChainImpl(Task task,DbHelper dbHelper, DownloadListener listener) {
        mTask = task;
        mDbHelper = dbHelper;
        mDownloadListener = listener;
    }

    @Override
    public Task task() {
        return mTask;
    }

    @Override
    public DownloadListener downloadListener() {
        return mDownloadListener;
    }

    @Override
    public DbHelper dbHelper() {
        return mDbHelper;
    }

    @Override
    public void setTmpObj(Object tmpObj) {
        this.tmpObj = tmpObj;
    }

    @Override
    public Object getTmpObj() {
        return tmpObj;
    }
}
