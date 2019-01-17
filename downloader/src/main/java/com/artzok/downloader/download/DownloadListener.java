package com.artzok.downloader.download;

import com.artzok.downloader.share.TaskConfig;

/**
 * name：赵坤 on 2018/12/20 14:27
 * email：zhaokun@ziipin.com
 */
public interface DownloadListener {
    /**
     * 只有失败才会调用，失败之后，后续接口不会再调用
     */
    void failed(TaskConfig config, int errCode, Exception e);

    /**
     * md5 校验成功，文件权限校验成功，文件可写
     */
    void started(TaskConfig config, long sofar);

    /**
     * 链接服务端成功
     */
    void connected(TaskConfig config, long contentLength);

    /**
     * 下载完成且md5校验成功
     */
    void finished(TaskConfig config, String md5);

    /**
     * 下载错误重试
     */
    void retry(TaskConfig config, Exception e, int times);

    /**
     * 一些意外情况下调用，比如数据库更新失败，下载配置中没有 md5 或者 content Length 为 0 等等
     */
    void warn(TaskConfig config, Exception e);

    /**
     * 下载中
     */
    void downloading(TaskConfig task, long sofar, long total);
}
