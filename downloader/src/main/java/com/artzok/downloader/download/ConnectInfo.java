package com.artzok.downloader.download;

import java.io.InputStream;

/**
 * name：赵坤 on 2018/12/20 14:43
 * email：zhaokun@ziipin.com
 */
public class ConnectInfo {
    public InputStream mInputStream;
    public long mContentLength;
    public int httpCode;

    public ConnectInfo(InputStream inputStream, long contentLength, int httpCode) {
        mInputStream = inputStream;
        mContentLength = contentLength;
        this.httpCode = httpCode;
    }
}
