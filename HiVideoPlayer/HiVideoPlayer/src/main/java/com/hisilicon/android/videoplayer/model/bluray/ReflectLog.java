package com.hisilicon.android.videoplayer.model.bluray;

import android.util.Log;

/**
 */

public class ReflectLog {
    private static boolean isDebug = true;

    public static void e_debug(Object obj, Exception e) {
        if (!isDebug) {
            return;
        }
        if (obj != null && e != null) {
            Log.e(obj.getClass().getSimpleName(), e.toString());
        }
    }


    public static void i_debug(Object obj, Object e) {
        if (!isDebug) {
            return;
        }
        if (obj != null && e != null) {
            Log.i(obj.getClass().getSimpleName(), e.toString());
        }
    }

    public static void d_debug(Object obj, Object e) {
        if (!isDebug) {
            return;
        }
        if (obj != null && e != null) {
            Log.i(obj.getClass().getSimpleName(), e.toString());
        }
    }
}
