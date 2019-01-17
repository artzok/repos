package com.artzok.downloader.manager;


import com.artzok.downloader.download.DownloadListener;
import com.artzok.downloader.share.TaskConfig;

/**
 * name：赵坤 on 2018/12/18 16:08
 * email：zhaokun@ziipin.com
 */
public interface UIListener extends DownloadListener {
    void onRespDownload(TaskConfig config, int pendingCount);

    void onRespCancel(TaskConfig config, boolean result);

    void onRespRemove(TaskConfig config, boolean result);
}
