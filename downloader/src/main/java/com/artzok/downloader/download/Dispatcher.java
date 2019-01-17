package com.artzok.downloader.download;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * name：赵坤 on 2018/12/20 15:01
 * email：zhaokun@ziipin.com
 */
public final class Dispatcher {
    private int mMaxRunningTask = 4;

    private ExecutorService mExecutorService;
    private Deque<RealTask> mReadyTasks = new ArrayDeque<>();
    private Deque<RealTask> mRunningTasks = new ArrayDeque<>();
    private Runnable mIdleRunnable;

    private Map<String, RealTask> mAllTasks = Collections.synchronizedMap(new HashMap<String, RealTask>());

    Dispatcher() {
    }

    private synchronized ExecutorService executorService() {
        if (mExecutorService == null)
            mExecutorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                    60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
        return mExecutorService;
    }

    public synchronized Task getTask(String name) {
        return mAllTasks.get(name);
    }

    public synchronized int getAllTaskSize() {
        return mAllTasks.size();
    }

    // can only remove ready task
    // avoid remove running task
    public synchronized boolean remove(RealTask realTask) {
        return mReadyTasks.remove(realTask) &&
                mAllTasks.remove(realTask.getName()) != null;
    }

    /**
     * @return 前面还有几个任务需要执行完才能到自己，
     * -1 表明添加失败(有相同的任务在了)，
     * -2 表明任务列表已经满了，稍后才能下载
     * 0 表明直接执行
     */
    public synchronized int enqueue(RealTask task) {
        String name = task.getName();
        if (mAllTasks.containsKey(name)) return -1;
        mAllTasks.put(name, task);
        if (mRunningTasks.size() < mMaxRunningTask) {
            mRunningTasks.add(task);
            executorService().execute(task);
            return 0;
        } else {
            int size = mReadyTasks.size();
            if (size >= 32) return -2;
            mReadyTasks.add(task);
            return mReadyTasks.size();
        }
    }

    public synchronized void finished(RealTask realTask) {
        if (!mRunningTasks.remove(realTask))
            throw new AssertionError("can't get real task:" + realTask.getName());
        mAllTasks.remove(realTask.getName());
        if(mAllTasks.size() == 0 && mIdleRunnable != null)
            mIdleRunnable.run();
        promoteTask();
    }

    private void promoteTask() {
        if (mRunningTasks.size() >= mMaxRunningTask) return;
        if (mReadyTasks.isEmpty()) return;

        for (Iterator<RealTask> i = mReadyTasks.iterator(); i.hasNext(); ) {
            RealTask task = i.next();
            i.remove();
            mRunningTasks.add(task);
            mExecutorService.execute(task);
            if (mRunningTasks.size() >= mMaxRunningTask) return;
        }
    }

    public void setIdleRunnable(Runnable runnable) {
       mIdleRunnable = runnable;
    }
}
