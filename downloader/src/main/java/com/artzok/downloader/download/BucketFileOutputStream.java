package com.artzok.downloader.download;

import android.os.SystemClock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BucketFileOutputStream extends OutputStream {

    private long mWroteLength;
    private volatile BucketOutputStream mCurrentBucket;

    private final File mTargetFile;
    private final ExecutorService mFlushThreadPool;
    private final ArrayList<BucketOutputStream> mBuckets;

    public BucketFileOutputStream(File tf, long sofar) {
        mTargetFile = tf;
        mWroteLength = sofar;
        mBuckets = new ArrayList<>();
        mFlushThreadPool = Executors.newCachedThreadPool();
    }

    private BucketOutputStream createNewBucket(long seek) throws IOException {
        return new BucketOutputStream(mTargetFile, mFlushThreadPool, seek);
    }

    private BucketOutputStream findReuseBucket(int more, long seek) throws IOException {
        for (BucketOutputStream buffed : mBuckets) {
            if (mCurrentBucket != buffed && !buffed.isWillFull(more)) {
                buffed.seek(seek);
                mCurrentBucket = buffed;
                return mCurrentBucket;
            }
        }
        return null;
    }

    private BucketOutputStream getAvailableBucket(int more, long seek) throws IOException {
        if (mCurrentBucket != null) {
            if (mCurrentBucket.isWillFull(more)) {
                mCurrentBucket.flush();
                // find a appropriate
                mCurrentBucket = findReuseBucket(more, seek);
                // create a new
                if (mCurrentBucket == null) {
                    mCurrentBucket = createNewBucket(seek);
                    mBuckets.add(mCurrentBucket);
                }
            }
        } else {
            mCurrentBucket = createNewBucket(seek);
            mBuckets.add(mCurrentBucket);
        }
        return mCurrentBucket;
    }

    @Override
    public void write(int i) throws IOException {
        throw new RuntimeException("can't invoke this method.");
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        throw new RuntimeException("can't invoke this method.");
    }

    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        BucketOutputStream curr = getAvailableBucket(len, mWroteLength);
        if (curr == null) throw new RuntimeException("can't find a buffer");
        curr.write(bytes, off, len);
        mWroteLength += len;
    }

    @Override
    public void flush() throws IOException {
        while (true) {
            boolean allFlushed = true;
            for (BucketOutputStream bf : mBuckets)
                if (bf.isWriting())
                    allFlushed = false;
                else bf.close();
            if (allFlushed) break;
            SystemClock.sleep(200);
        }
    }

    @Override
    public void close() throws IOException {
        for (BucketOutputStream bf : mBuckets) {
            bf.close();
        }
    }
}