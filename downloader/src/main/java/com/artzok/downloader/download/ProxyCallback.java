package com.artzok.downloader.download;

import android.os.Handler;
import android.os.Looper;


import com.artzok.downloader.db.DbHelper;
import com.artzok.downloader.db.TaskDao;
import com.artzok.downloader.db.TaskMode;
import com.artzok.downloader.share.ErrCodes;
import com.artzok.downloader.share.TaskConfig;

import java.io.File;

/**
 * name：赵坤 on 2018/12/24 10:27
 * email：zhaokun@ziipin.com
 */
public class ProxyCallback implements DownloadListener {

    private final TaskDao mTaskDao;
    private final Handler mHandler;
    private final DownloadListener mDownloadListener;

    public ProxyCallback(DbHelper helper, DownloadListener listener) {
        mTaskDao = helper.taskDao();
        mDownloadListener = listener;
        mHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * update status, err_code, ex_cls, ex_msg
     */
    @Override
    public void failed(final TaskConfig config, final int errCode, final Exception e) {
        int status = Downloader.status.failed.ordinal();
        int err = e instanceof ResponseException ? ((ResponseException) e).mErrCode : ErrCodes.UNKNOWN;
        TaskMode old = mTaskDao.getTask(config.getTaskId());
        TaskMode mode = new TaskMode(config, old, status);
        mode.setErrorCode(err)
                .setExceptionCls(e.getClass().getName())
                .setExceptionMsg(e.getMessage());
        updateTaskMode(config, old, mode);

        if (mDownloadListener!= null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.failed(config, errCode, e);
                }
            });
    }

    /**
     * file check succeed
     * update status, sofar
     */
    @Override
    public void started(final TaskConfig config, final long sofar) {
        int status = Downloader.status.started.ordinal();
        // update db
        TaskMode old = mTaskDao.getTask(config.getTaskId());
        TaskMode mode = new TaskMode(config, old, status);
        mode.setSofar(sofar);
        updateTaskMode(config, old, mode);

        if (mDownloadListener != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.started(config, sofar);
                }
            });
    }

    /**
     * connect service
     * update content length
     */
    @Override
    public void connected(final TaskConfig config, final long contentLength) {
        int status = Downloader.status.connected.ordinal();
        // update db
        TaskMode old = mTaskDao.getTask(config.getTaskId());
        TaskMode mode = new TaskMode(config, old, status)
                .setSofar(new File(config.getFilePath(), config.getFileName()).length());
        mode.mContentLength = contentLength;
        updateTaskMode(config, old, mode);

        if (mDownloadListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.connected(config, contentLength);
                }
            });
        }
    }

    /**
     * download succeed and check md5 succeed
     * update md5 for task
     */
    @Override
    public void finished(final TaskConfig config, final String md5) {
        int status = Downloader.status.finished.ordinal();
        // update db
        TaskMode old = mTaskDao.getTask(config.getTaskId());
        TaskMode mode = new TaskMode(config, old, status);
        mode.mMd5 = md5;
        updateTaskMode(config, old, mode);

        if (mDownloadListener != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.finished(config, md5);
                }
            });
    }

    @Override
    public void retry(final TaskConfig config, final Exception e, final int times) {
        if (mDownloadListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.retry(config, e, times);
                }
            });
        }
    }

    @Override
    public void warn(final TaskConfig config, final Exception e) {
        if (mDownloadListener != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.warn(config, e);
                }
            });
    }

    /**
     * downloading
     * update sofar
     */
    @Override
    public void downloading(final TaskConfig config, final long sofar, final long total) {
        int status = Downloader.status.downloading.ordinal();
        // update db
        TaskMode old = mTaskDao.getTask(config.getTaskId());
        TaskMode mode = new TaskMode(config, old, status)
                .setSofar(sofar);
        updateTaskMode(config, old, mode);

        if (mDownloadListener != null)
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mDownloadListener.downloading(config, sofar, total);
                }
            });
    }

    private void updateTaskMode(TaskConfig config, TaskMode old, TaskMode newest) {
        try {
            if (old == null) {
                mTaskDao.addTask(newest);
            } else {
                mTaskDao.updateTask(newest);
            }
        } catch (Exception e) {
            if (mDownloadListener != null)
                mDownloadListener.warn(config, e);
        }
    }
}
