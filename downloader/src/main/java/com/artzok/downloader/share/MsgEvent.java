package com.artzok.downloader.share;

/**
 * name：赵坤 on 2018/12/17 16:16
 * email：zhaokun@ziipin.com
 */
public final class MsgEvent {
    public static class Client {
       public static final int DOWNLOAD = 0X01;
       public static final int CANCEL = 0X02;
       public static final int REMOVE = 0X03;
    }

    public static class Service {

        public static final int ACTION_RESPONSE = 0;

        /**
         * 各种失败
         */
        public static final int  FAILED = 1;

        /**
         * 开始下载事件
         */
        public static final int STARTED = 2;

        /**
         * 链接服务器
         */
        public static final int CONNECTED  = 3;

        /**
         * 完成下载
         */
        public static final int FINISHED = 4;

        /**
         * 重试
         */
        public static final int RETRY = 5;

        /**
         * 警告
         */
        public static final int WARN = 6;

        /**
         * 下载中
         */
        public static final int DOWNLOADING = 7;
    }
}
