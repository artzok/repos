package com.artzok.downloader.download;

import com.artzok.downloader.share.TaskConfig;

/**
 * name：赵坤 on 2018/12/18 17:04
 * email：zhaokun@ziipin.com
 */
public interface Task {

    /**
     * @return succeed true else false failed
     */
    boolean cancel();

    /**
     * @return -1 have same task,
     * -2 too much task can't add more,
     * other have how many task need execute before this
     */
    int download();

    /**
     * @return succeed true else false failed
     */
    boolean remove();

    /**
     * @return get config of task
     */
    TaskConfig config();
}
