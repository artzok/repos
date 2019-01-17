package com.artzok.downloader.download;

import android.os.SystemClock;

public abstract class SingleAsyncWatchDog extends Thread {
    private volatile boolean watching;

    public SingleAsyncWatchDog() {
        watching = true;
    }

    public abstract boolean watch();

    public abstract void occur();

    public void exit() {
        synchronized (this) {
            watching = false;
        }
    }

    @Override
    public void run() {
        while (watching) {
            if (watch()) {
                occur();
                break;
            }
            SystemClock.sleep(16); // 1/60fps
        }
    }
}