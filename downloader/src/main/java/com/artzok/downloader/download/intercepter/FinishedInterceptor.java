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

/**
 * name：赵坤 on 2018/12/20 15:37
 * email：zhaokun@ziipin.com
 */
public class FinishedInterceptor implements Interceptor {

    @Override
    public boolean intercept(Chain chain) throws ResponseException {
        Task task = chain.task();
        TaskConfig config = task.config();
        File file = new File(config.getFilePath(), config.getFileName());
        TaskMode mode = chain.dbHelper().taskDao().getTask(config.getTaskId());

        String md5 = config.getMd5();
        if (TextUtils.isEmpty(md5) && mode != null)
            md5 = mode.mMd5;
        if (!TextUtils.isEmpty(md5))
            md5 = md5.toLowerCase();

        String fileMd5 = Utils.getFileMD5(file);
        if (TextUtils.isEmpty(md5) || TextUtils.equals(md5, fileMd5)) {
            chain.downloadListener().finished(chain.task().config(), fileMd5);
        } else {
            ResponseException ex = new
                    ResponseException(ErrCodes.FILE_MD5_CHECK_ERR,
                    new IllegalStateException("finished but md5 check failed"));
            ex.setInterruptedRetry(true); // should not retry again download
            throw ex;
        }
        return false;
    }
}
