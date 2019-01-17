package com.artzok.downloader.download;

import android.arch.persistence.room.Room;
import android.content.Context;

import com.artzok.downloader.db.DbHelper;
import com.artzok.downloader.share.TaskConfig;


/**
 * name：赵坤 on 2018/12/18 17:43
 * email：zhaokun@ziipin.com
 */
public final class Downloader {

    public enum status {
        started, connected, downloading, failed, finished
    }

    public static final int CALLBACK_INTERVAL = 100 * 1024;// 100 kb

    private final Dispatcher mDispatcher;
    private final DbHelper mDbHelper;

    public Downloader(Context context) {
        mDispatcher = new Dispatcher();
        mDbHelper = Room.databaseBuilder(context, DbHelper.class, "downloader").build();
    }

    public Task createTask(TaskConfig taskConfig, DownloadListener listener) {
        Task task = mDispatcher.getTask(taskConfig.getTaskId());
        if (task != null) return task;
        return new RealTask(this, taskConfig, taskConfig.getTaskId(), listener);
    }

    public Dispatcher dispatcher() {
        return mDispatcher;
    }

    public DbHelper dbHelper() {
        return mDbHelper;
    }
}
