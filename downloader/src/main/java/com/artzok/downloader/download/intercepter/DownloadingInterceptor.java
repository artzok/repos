package com.artzok.downloader.download.intercepter;



import com.artzok.downloader.download.BucketFileOutputStream;
import com.artzok.downloader.download.Cancelable;
import com.artzok.downloader.download.ConnectInfo;
import com.artzok.downloader.download.Downloader;
import com.artzok.downloader.download.Interceptor;
import com.artzok.downloader.download.ResponseException;
import com.artzok.downloader.download.Task;
import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.ErrCodes;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;


/**
 * name：赵坤 on 2018/12/20 15:36
 * email：zhaokun@ziipin.com
 */
public class DownloadingInterceptor implements Interceptor {
    private Cancelable mCancelable;

    public DownloadingInterceptor(Cancelable cancelable) {
        mCancelable = cancelable;
    }

    @Override
    public boolean intercept(Chain chain) throws ResponseException {
        Task task = chain.task();
        TaskConfig config = task.config();
        File file = new File(config.getFilePath(), config.getFileName());

        if (!file.exists())
            throw new ResponseException(ErrCodes.FILE_NOT_EXIST,
                    new FileNotFoundException(file.getAbsolutePath()));

        ConnectInfo info = (ConnectInfo) chain.getTmpObj();
        if (info == null)
            throw new RuntimeException("connect info should not be null.");

        InputStream input = info.mInputStream;
        OutputStream output = null;

        byte[] buffer = new byte[8192];
        long sofar = file.length();
        int read, interval = 0;

        try {

            output = new BucketFileOutputStream(file, sofar);

            while (true) {

                if (mCancelable.isCanceled())
                    throw new ResponseException(ErrCodes.USER_CANCELED, new RuntimeException("use cancel downloading."));

                read = input.read(buffer);

                if (read == -1) {
                    if (sofar == info.mContentLength)
                        chain.downloadListener().downloading(task.config(), sofar, sofar);
                    else throw new ResponseException(ErrCodes.READ_STREAM_ERR,
                            new IllegalStateException("read -1 but total length can't equal content length"));
                    break;
                }

                output.write(buffer, 0, read);

                sofar += read;
                interval += read;

                if (interval > Downloader.CALLBACK_INTERVAL) {
                    interval = 0;
                    chain.downloadListener().downloading(task.config(), sofar, info.mContentLength);
                }
            }

            output.flush();

        } catch (IOException e) {
            ResponseException exp = null;
            if (e instanceof SocketException &&
                    "Software caused connection abort".equals(e.getMessage())) {
                exp = new ResponseException(ErrCodes.SOFTWARE_ABORT, e);
                exp.setInterruptedRetry(false);
            } else {
                exp = new ResponseException(ErrCodes.READ_STREAM_ERR, e);
            }
            throw exp;
        } finally {
            safeClose(input);
            safeClose(output);
        }
        return false;
    }

    private void safeClose(Closeable close) {
        try {
            if (close != null)
                close.close();
        } catch (Throwable ignore) {
        }
    }
}
