package com.artzok.downloader.download.intercepter;


import android.accounts.NetworkErrorException;


import com.artzok.downloader.download.Cancelable;
import com.artzok.downloader.download.ConnectInfo;
import com.artzok.downloader.download.Interceptor;
import com.artzok.downloader.download.ResponseException;
import com.artzok.downloader.download.SingleAsyncWatchDog;
import com.artzok.downloader.share.TaskConfig;
import com.artzok.downloader.share.ErrCodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * name：赵坤 on 2018/12/20 15:36
 * email：zhaokun@ziipin.com
 */
public class ConnectInterceptor implements Interceptor {
    private OkHttpClient mOkHttpClient;
    private final Cancelable mCancelable;

    public ConnectInterceptor(Cancelable cancelable) {
        this.mCancelable = cancelable;
        mOkHttpClient = new OkHttpClient();
    }

    @Override
    public boolean intercept(Chain chain) throws ResponseException {
        final TaskConfig config = chain.task().config();
        File file = new File(config.getFilePath(), config.getFileName());

        if (!file.exists())
            throw new ResponseException(ErrCodes.FILE_NOT_EXIST,
                    new FileNotFoundException(file.getAbsolutePath()));
        long offset = file.length();

        // create request
        Request request = new Request.Builder().
                tag(config.getTaskId()).
                addHeader("RANGE", "bytes=" + offset + "-").
                url(config.getDownloadUrl()).
                cacheControl(CacheControl.FORCE_NETWORK).
                build();
        final Call call = mOkHttpClient.newCall(request);

        // create watch dog for user cancel action
        SingleAsyncWatchDog watchDog = watchCancelable(call);
        try {
            watchDog.start();
            Response response = call.execute();
            watchDog.exit();
            if (response.isSuccessful()) {
                ResponseBody body = response.body();
                if (body == null)
                    throw new ResponseException(ErrCodes.RESPONSE_EMPTY,
                            new NetworkErrorException("response empty!"));
                long contentLength = offset + body.contentLength();
                ConnectInfo info = new ConnectInfo(body.byteStream(), contentLength, response.code());
                chain.setTmpObj(info);
                chain.downloadListener().connected(chain.task().config(), contentLength);
            } else throw new IOException("connect service failed");
        } catch (Exception e) {
            if (e instanceof ResponseException)
                throw (ResponseException) e;
            int errCode = mCancelable.isCanceled() ?
                    ErrCodes.USER_CANCELED : ErrCodes.CONNECT_ERR;
            throw new ResponseException(errCode, e);
        }
        return false;
    }

    private SingleAsyncWatchDog watchCancelable(final Call call) {
        return new SingleAsyncWatchDog() {
            public boolean watch() {
                return mCancelable.isCanceled();
            }

            public void occur() {
                call.cancel();
            }
        };
    }
}
