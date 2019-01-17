package com.artzok.downloader.share;

/**
 * name：赵坤 on 2018/12/20 17:03
 * email：zhaokun@ziipin.com
 */
public class ErrCodes {
    /**
     * 没有错误
     */
    public static final int NO_ERR = 0x00;
    /**
     * 未知错误
     */
    public static final int UNKNOWN = 0x01;
    /**
     * 下载时链接服务器错误
     */
    public static final int CONNECT_ERR = 0x01 << 1;
    /**
     * 取消下载
     */
    public static final int USER_CANCELED = 0x01 << 2;
    /**
     * 网络访问结果为空
     */
    public static final int RESPONSE_EMPTY = 0x01 << 3;
    /**
     * 文件不存在
     */
    public static final int FILE_NOT_EXIST = 0x01 << 4;
    /**
     * 读取流数据错误
     */
    public static final int READ_STREAM_ERR = 0x01 << 5;
    /**
     * 创建文件失败
     */
    public static final int CREATE_FILE_ERR = 0x01 << 6;
    /**
     * 删除文件失败
     */
    public static final int DELETE_FILE_ERR = 0x01 << 7;
    /**
     * 文件不能读写
     */
    public static final int FILE_CAN_NOT_RW = 0x01 << 8;
    /**
     * md5 校验失败
     */
    public static final int FILE_MD5_CHECK_ERR = 0x01 << 9;
    /**
     * 远程链接失败
     */
    public static final int REMOTE_HANDlER_DEAD = 0x01 << 10;
    /**
     *  中断(用户关闭网络)
     */
    public static final int SOFTWARE_ABORT = 0x01 << 11;

}
