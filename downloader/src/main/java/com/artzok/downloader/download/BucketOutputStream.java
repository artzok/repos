package com.artzok.downloader.download;

import android.support.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.ExecutorService;

class BucketOutputStream extends OutputStream implements Runnable {
    private static final int BUCKET_SIZE = 1024 * 1024; // 1MB pool
    private final Object LOCK = new Object();

    private final RandomAccessFile mAccessFile;
    private final ExecutorService mFlushThreadPool;
    private volatile BufferedOutputStream mBufferedStream;

    private volatile Boolean mIsWriting = false;
    private volatile long mHaveBuffedSize;

    BucketOutputStream(File file, ExecutorService flushExecutor, long seek) throws IOException {
        mAccessFile = new RandomAccessFile(file, "rwd");
        seek(seek);
        mFlushThreadPool = flushExecutor;
        mBufferedStream = new BufferedOutputStream(new FileOutputStream(mAccessFile.getFD()), BUCKET_SIZE);
    }

    /**
     * return true indicator will full if add more bytes
     */
    boolean isWillFull(int more) {
        synchronized (LOCK) {
            return mHaveBuffedSize + more > BUCKET_SIZE;
        }
    }

    /**
     * set write position that will be start position of this bucket flush.
     */
    void seek(long seek) throws IOException {
        synchronized (mAccessFile) {
            mAccessFile.seek(seek);
        }
    }

    /**
     * return true indicator writing
     */
    public synchronized boolean isWriting() {
        synchronized (LOCK) {
            return mIsWriting;
        }
    }

    @Override
    public void write(int i) throws IOException {
        write(new byte[]{(byte) i});
    }

    @Override
    public void write(@NonNull byte[] bytes) throws IOException {
        write(bytes, 0, bytes.length);
    }

    @Override
    public void write(@NonNull byte[] bytes, int off, int len) throws IOException {
        mBufferedStream.write(bytes, off, len);
        mHaveBuffedSize += len;
    }

    @Override
    public void run() {
        synchronized (mAccessFile) {
            try {// flush this bucket buffer to file
                mBufferedStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                synchronized (LOCK) {
                    mIsWriting = false;
                    mHaveBuffedSize = 0;
                }
            }
        }
    }

    @Override
    public void flush() throws IOException {
        if (mHaveBuffedSize != 0) {
            synchronized (LOCK) {
                mIsWriting = true;
                mFlushThreadPool.submit(this);
            }
        }
    }

    @Override
    public void close() throws IOException {
        mBufferedStream.close();
        mAccessFile.close();
    }
}