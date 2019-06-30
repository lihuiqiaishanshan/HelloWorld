package com.hisilicon.explorer.utils.thread;

import com.hisilicon.explorer.Config;

/**
 */

public class ThreadPoolUtils {

    private ThreadPoolUtils() {
    }

    private static volatile ThreadPoolUtils mThreadPool;

    private PriorityExecutor mPriorityExector = new PriorityExecutor(Config.getInstance().getmThreadPollSize(), false);

    public static ThreadPoolUtils getInstance() {
        if (mThreadPool == null) {
            synchronized (ThreadPoolUtils.class) {
                if (mThreadPool == null) {
                    mThreadPool = new ThreadPoolUtils();
                }
            }
        }
        return mThreadPool;
    }

    public void runThread(PriorityRunnable runnable) {
        mPriorityExector.execute(runnable);
    }

}
