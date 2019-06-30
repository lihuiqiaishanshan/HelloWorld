package com.hisilicon.android.videoplayer.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * <p>
 * Toast Tool,  通过makeText避免Toast信息堆栈
 * </p>
 * {@link android.widget.Toast#makeText}
 */
public class ToastUtil {

        private static Handler handler = new Handler(Looper.getMainLooper());

        private static Toast toast = null;

        private static Object synObj = new Object();

        public static void showMessage(final Context act, final String msg) {
            showMessage(act, msg, Toast.LENGTH_SHORT);
        }

        public static void showMessage(final Context act, final int msg) {
            showMessage(act, msg, Toast.LENGTH_SHORT);
        }

        public static void showMessage(final Context act, final String msg,
                                       final int len) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (synObj) {
                        if (toast != null) {
                            toast.cancel();
                            toast = Toast.makeText(act, msg, len);
                        } else {
                            toast = Toast.makeText(act, msg, len);
                        }
                        toast.show();
                    }
                }
            });
        }

        public static void showMessage(final Context act, final int msg,
                                       final int len) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    synchronized (synObj) {
                        if (toast != null) {
                            toast.cancel();
                            toast = Toast.makeText(act, msg, len);
                        } else {
                            toast = Toast.makeText(act, msg, len);
                        }
                        toast.show();
                    }
                }
            });
        }
    }
