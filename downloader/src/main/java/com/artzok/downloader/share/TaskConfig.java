package com.artzok.downloader.share;

import android.os.Bundle;
import android.text.TextUtils;

/**
 * name：赵坤 on 2018/12/20 14:26
 * email：zhaokun@ziipin.com
 */
public final class TaskConfig {
    /**
     * 不能为空
     */
    private String mTaskId;

    /**
     * 重试次数
     */
    private int mRetryTimes;

    /**
     * 下载链接url
     */
    private String mDownloadUrl;

    /**
     * 文件路径
     */
    private String mFilePath;

    /**
     * 文件名
     */
    private String mFileName;

    /**
     * md5 值, maybe null
     */
    private String mMd5;

    /**
     * 文件长度, maybe null
     */
    private long mContentLength;

    /**
     * 分配任务应用包名
     */
    private String mTaskPackageName;

    public TaskConfig(Bundle bundle) {
        mTaskId = bundle.getString("_flg_task_id");
        mRetryTimes = bundle.getInt("_flg_retry_times");
        mDownloadUrl = bundle.getString("_flg_download_url");
        mFilePath = bundle.getString("_flg_file_path");
        mFileName = bundle.getString("_flg_file_name");
        mMd5 = bundle.getString("_flg_md5");
        mContentLength = bundle.getLong("_flg_content_length");
        mTaskPackageName = bundle.getString("_flg_task_package_name");
    }

    public void saveToBundle(Bundle bundle) {
        bundle.putString("_flg_task_id", mTaskId);
        bundle.putInt("_flg_retry_times", mRetryTimes);
        bundle.putString("_flg_download_url", mDownloadUrl);
        bundle.putString("_flg_file_path", mFilePath);
        bundle.putString("_flg_file_name", mFileName);
        bundle.putString("_flg_md5", mMd5);
        bundle.putLong("_flg_content_length", mContentLength);
        bundle.putString("_flg_task_package_name", mTaskPackageName);
    }

    private TaskConfig(Builder builder) {
        mTaskId = builder.mTaskId;
        mRetryTimes = builder.mRetryTimes;
        mDownloadUrl = builder.mDownloadUrl;
        mFilePath = builder.mFilePath;
        mFileName = builder.mFileName;
        mMd5 = builder.mMd5;
        mContentLength = builder.mContentLength;
        mTaskPackageName = builder.mTaskPackageName;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public int getRetryTimes() {
        return mRetryTimes;
    }

    public String getDownloadUrl() {
        return mDownloadUrl;
    }

    public String getFilePath() {
        return mFilePath;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getMd5() {
        return mMd5;
    }

    public long getContentLength() {
        return mContentLength;
    }

    public String getTaskPackageName() {
        return mTaskPackageName;
    }

    public static class Builder {
        private String mTaskId;
        private int mRetryTimes;
        private String mDownloadUrl;
        private String mFilePath;
        private String mFileName;
        private String mMd5;
        private long mContentLength;
        private String mTaskPackageName;

        public Builder(String taskId, String packageName,
                       String downloadUrl, String filePath, String fileName) {
            mTaskId = taskId;
            mDownloadUrl = downloadUrl;
            mFilePath = filePath;
            mFileName = fileName;
            mTaskPackageName = packageName;
            mRetryTimes = 1;
        }

        public Builder setRetryTimes(int retryTimes) {
            mRetryTimes = retryTimes;
            return this;
        }

        public Builder setMd5(String md5) {
            mMd5 = md5;
            return this;
        }

        public Builder setContentLength(long length) {
            mContentLength = length;
            return this;
        }

        /**
         * @see IllegalArgumentException when argus not valid
         */
        public TaskConfig build() {
            requestNotEmpty(mTaskId, "task id");
            requestNotEmpty(mDownloadUrl, "download url");
            requestNotEmpty(mFilePath, "path");
            requestNotEmpty(mFileName, "name");
            requestNotEmpty(mTaskPackageName, "name");
            return new TaskConfig(this);
        }

        private void requestNotEmpty(String params, String name) {
            if (TextUtils.isEmpty(params))
                throw new IllegalArgumentException(name + " can't be empty.");
        }
    }
}
