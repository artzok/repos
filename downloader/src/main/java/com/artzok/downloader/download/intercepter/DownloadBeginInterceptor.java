package com.artzok.downloader.download.intercepter;

import android.text.TextUtils;


import com.artzok.downloader.db.TaskMode;
import com.artzok.downloader.download.Interceptor;
import com.artzok.downloader.download.ResponseException;
import com.artzok.downloader.download.Task;
import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.ErrCodes;
import com.artzok.downloader.utils.Utils;

import java.io.File;
import java.io.IOException;

/**
 * name：赵坤 on 2018/12/20 15:34
 * email：zhaokun@ziipin.com
 * 1. check contentLength config and check MD5 config, if not exist then warn
 * 2. create file if not exist, error to failed
 * 3. check fileLength, = 0 continue download
 * 3. if fileLength == contentLength and md5 == fileMd5, finished
 * 4. other delete recreate file and continue download or direct continue download
 */
public class DownloadBeginInterceptor implements Interceptor {

    @Override
    public boolean intercept(Chain chain) throws ResponseException {
        Task task = chain.task();
        TaskConfig config = task.config();
        File file = new File(config.getFilePath(), config.getFileName());

        // db mode
        TaskMode mode = chain.dbHelper().taskDao().getTask(config.getTaskId());

        // 检查 MD5 contentLength为空  并warn
        if (config.getContentLength() <= 0)
            chain.downloadListener().warn(task.config(),
                    new ResponseException(ErrCodes.NO_ERR,
                            new IllegalArgumentException("no content length support")));

        // 没有contentLength 可能无法断点续传，
        if (TextUtils.isEmpty(config.getMd5()))
            chain.downloadListener().warn(task.config(),
                    new ResponseException(ErrCodes.NO_ERR,
                            new IllegalArgumentException("no md5 string support")));

        // ensure file exit and can access
        createAndCheckFile(file);

        long fileLength = file.length();
        if (fileLength == 0) return false;

        long contentLength = config.getContentLength();
        if (contentLength <= 0 && mode != null)
            contentLength = mode.mContentLength;

        String md5 = config.getMd5();
        if (TextUtils.isEmpty(md5) && mode != null)
            md5 = mode.mMd5;
        if (!TextUtils.isEmpty(md5))
            md5 = md5.toLowerCase();

        if (contentLength > 0 && fileLength == contentLength) {
            String fileMd5 = Utils.getFileMD5(file);
            if (TextUtils.isEmpty(md5) || TextUtils.equals(md5, fileMd5)) {
                chain.downloadListener().finished(task.config(), fileMd5);
                return true;
            } else return deleteAndRecreate(file);
        } else
            return (contentLength <= 0 || fileLength >
                    contentLength || mode == null) && deleteAndRecreate(file);
    }

    private boolean deleteAndRecreate(File file) throws ResponseException {
        if (file.delete()) {
            createAndCheckFile(file);
            return false;
        } else throw new ResponseException(ErrCodes.DELETE_FILE_ERR,
                new IllegalAccessException("can't delete file"));

    }

    private void createAndCheckFile(File file) throws ResponseException {
        // ensure file exit
        String absPath = file.getAbsolutePath();
        try {
            if (!file.exists() && !file.createNewFile())
                throw new IOException("create file failed!");
        } catch (IOException e) {
            throw new ResponseException(ErrCodes.CREATE_FILE_ERR, e);
        }

        // ensure file access
        if (!file.canRead() || !file.canWrite())
            throw new ResponseException(ErrCodes.FILE_CAN_NOT_RW,
                    new IllegalArgumentException("can't read and write file:" + absPath));
    }
}
