package com.hisilicon.tvui.util;

import android.os.SystemClock;

public class TimeUtil {
   // return unit ms
    public static long getBootTime() {
          return SystemClock.elapsedRealtime() / 1000;
    }

}
