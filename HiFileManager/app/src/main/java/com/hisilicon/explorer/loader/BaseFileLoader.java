package com.hisilicon.explorer.loader;


import android.os.Handler;
import android.os.Looper;

import com.hisilicon.explorer.utils.thread.Priority;
import com.hisilicon.explorer.utils.thread.PriorityRunnable;
import com.hisilicon.explorer.utils.thread.ThreadPoolUtils;


/**
 */

public abstract class BaseFileLoader<T> {

    public void run() {
        ThreadPoolUtils.getInstance().runThread(new PriorityRunnable(Priority.NORMAL, new Runnable() {
            @Override
            public void run() {
                final T t = load();
                Looper mainLooper = Looper.getMainLooper();
                new Handler(mainLooper).post(new Runnable() {
                    @Override
                    public void run() {
                        loadSuccess(t);
                    }
                });
            }
        }));
    }
    public abstract T load();
    public abstract void loadFail();
    public abstract void loadSuccess(T t);
}
