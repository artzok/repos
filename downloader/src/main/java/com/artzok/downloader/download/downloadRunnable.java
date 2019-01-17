package com.artzok.downloader.download;

/**
 * name：赵坤 on 2018/12/20 15:01
 * email：zhaokun@ziipin.com
 */
public abstract class downloadRunnable implements Runnable {

    private String name;

    public downloadRunnable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        execute();
    }

    protected abstract void execute();
}
