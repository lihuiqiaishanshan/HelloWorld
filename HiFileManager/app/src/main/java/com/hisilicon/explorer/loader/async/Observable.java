package com.hisilicon.explorer.loader.async;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.hisilicon.explorer.utils.thread.Priority;
import com.hisilicon.explorer.utils.thread.PriorityRunnable;
import com.hisilicon.explorer.utils.thread.ThreadPoolUtils;

/**
 */

public class Observable {

    final OnSubscribe onSubscribe;

    private Observable(OnSubscribe onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static Observable create(OnSubscribe onSubscribe) {
        return new Observable(onSubscribe);
    }

    public void subscribe(Subscriber subscriber) {
        subscriber.onStart();
        onSubscribe.call(subscriber);
    }

    public Observable subscribeOn() {
        return Observable.create(new OnSubscribe() {
            @Override
            public void call(final Subscriber subscriber) {
                ThreadPoolUtils.getInstance().runThread(new PriorityRunnable(Priority.NORMAL, new Runnable() {
                    @Override
                    public void run() {
                        Observable.this.onSubscribe.call(subscriber);
                    }
                }));
            }
        });
    }

    public Observable observeOn() {
        return Observable.create(new OnSubscribe() {
            @Override
            public void call(final Subscriber subscriber) {
                subscriber.onStart();
                Observable.this.onSubscribe.call(new Subscriber() {
                    @Override
                    public void onCompleted() {
                        Looper mainLooper = Looper.getMainLooper();
                        new Handler(mainLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                subscriber.onCompleted();
                            }
                        });
                    }

                    @Override
                    public void onCompleted(final Object obj) {
                        Looper mainLooper = Looper.getMainLooper();
                        new Handler(mainLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                subscriber.onCompleted(obj);
                            }
                        });
                    }

                    @Override
                    public void onError(final Object obj) {
                        Looper mainLooper = Looper.getMainLooper();
                        new Handler(mainLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                subscriber.onError(obj);
                            }
                        });
                    }

                    @Override
                    public void onError() {
                        Looper mainLooper = Looper.getMainLooper();
                        new Handler(mainLooper).post(new Runnable() {
                            @Override
                            public void run() {
                                subscriber.onError();
                            }
                        });
                    }
                });
            }
        });
    }

    public interface OnSubscribe {
        void call(Subscriber subscriber);
    }
}
