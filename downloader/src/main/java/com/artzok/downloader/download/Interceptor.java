package com.artzok.downloader.download;


import com.artzok.downloader.db.DbHelper;

public interface Interceptor {
    /**
     * @param chain
     * @return  true 中断
     * @throws ResponseException
     */
    boolean intercept(Chain chain) throws ResponseException;

    interface Chain {
        Task task();

        DownloadListener downloadListener();

        DbHelper dbHelper();

        void setTmpObj(Object obj);

        Object getTmpObj();
    }

}