package com.artzok.downloader.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.artzok.downloader.share.TaskConfig;


/**
 * name：赵坤 on 2018/12/19 18:55
 * email：zhaokun@ziipin.com
 */
@Entity(indices = {@Index(value = {"task_id"}, unique = true)})
public final class TaskMode {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "task_id")
    public String mTaskId;

    @ColumnInfo(name = "retry_times")
    public int mRetryTimes;

    @ColumnInfo(name = "download_url")
    public String mDownloadUrl;

    @ColumnInfo(name = "file_path")
    public String mFilePath;

    @ColumnInfo(name = "file_name")
    public String mFileName;

    @ColumnInfo(name = "task_package_name")
    public String mTaskPackageName;

    @ColumnInfo(name = "status")
    public int mStatus;

    @ColumnInfo(name = "md5")
    public String mMd5;

    @ColumnInfo(name = "content_length")
    public long mContentLength;

    @ColumnInfo(name = "err_code")
    public int mErrorCode;

    @ColumnInfo(name = "sofar")
    public long mSofar;

    @ColumnInfo(name = "exception_cls")
    public String mExceptionCls;

    @ColumnInfo(name = "exception_msg")
    public String mExceptionMsg;

    @ColumnInfo(name = "allow_wifi")
    public boolean mAllowWifi;

    public TaskMode() {
    }

    public TaskMode(TaskConfig config, TaskMode db, int status) {
        id = db != null ? db.id : 0;

        mTaskId = config.getTaskId();
        mRetryTimes = config.getRetryTimes();
        mDownloadUrl = config.getDownloadUrl();
        mFilePath = config.getFilePath();
        mFileName = config.getFileName();
        mTaskPackageName = config.getTaskPackageName();

        mStatus = status;

        // update mode if db data have
        mMd5 = config.getMd5();
        if (mMd5 == null && db != null) mMd5 = db.mMd5;

        mContentLength = config.getContentLength();
        if (mContentLength <= 0L && db != null)
            mContentLength = db.mContentLength;

        mErrorCode = db != null ? db.mErrorCode : 0;
        mSofar = db != null ? db.mSofar : 0;
        mExceptionCls = db != null ? db.mExceptionCls : null;
        mExceptionMsg = db != null ? db.mExceptionMsg : null;
    }

    public TaskMode setErrorCode(int errorCode) {
        mErrorCode = errorCode;
        return this;
    }

    public TaskMode setSofar(long sofar) {
        mSofar = sofar;
        return this;
    }

    public TaskMode setExceptionCls(String cls) {
        mExceptionCls = cls;
        return this;
    }

    public TaskMode setExceptionMsg(String msg) {
        mExceptionMsg = msg;
        return this;
    }

    @Override
    public String toString() {
        return "TaskMode{" +
                "id=" + id +
                ", mTaskId='" + mTaskId + '\'' +
                ", mRetryTimes=" + mRetryTimes +
                ", mDownloadUrl='" + mDownloadUrl + '\'' +
                ", mFilePath='" + mFilePath + '\'' +
                ", mFileName='" + mFileName + '\'' +
                ", mTaskPackageName='" + mTaskPackageName + '\'' +
                ", mStatus=" + mStatus +
                ", mMd5='" + mMd5 + '\'' +
                ", mContentLength=" + mContentLength +
                ", mErrorCode=" + mErrorCode +
                ", mSofar=" + mSofar +
                ", mExceptionCls='" + mExceptionCls + '\'' +
                ", mExceptionMsg='" + mExceptionMsg + '\'' +
                '}';
    }
}
