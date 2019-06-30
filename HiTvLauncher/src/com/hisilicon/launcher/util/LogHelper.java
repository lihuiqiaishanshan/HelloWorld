package com.hisilicon.launcher.util;

import android.text.TextUtils;
import android.os.SystemProperties;
import android.util.Log;

public class LogHelper {
    private static final int STACK_LEVEL = 5;

    private static final String MSG_EMPTY = "Empty Msg";

    private static final String TAG_PREFIX = "HiTvApp_Launcher_";

    private static final String LOG_LEVEL_PROPERTY_NAME = "persist.sys.loglevel.tvapp";

    public static final char[] LOG_LEVEL_ARRAY = new char[] { 'v', 'd', 'i', 'w', 'e' };

    public static final int LOG_LEVEL_VERBOSE = 0;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_WARNING = 3;
    public static final int LOG_LEVEL_ERROR = 4;

    public static void v(String tag, String desc) {
        if (LOG_LEVEL_VERBOSE >= getLogLevel()) {
            Log.v(TAG_PREFIX + tag, desc);
        }
    }

    public static void v(String tag, String desc, Throwable tr) {
        if (LOG_LEVEL_VERBOSE >= getLogLevel()) {
            Log.v(TAG_PREFIX + tag, desc, tr);
        }
    }

    public static void d(String tag, String desc) {
        if (LOG_LEVEL_DEBUG >= getLogLevel()) {
            Log.d(TAG_PREFIX + tag, desc);
        }
    }

    public static void d(String tag, String desc, Throwable tr) {
        if (LOG_LEVEL_DEBUG >= getLogLevel()) {
            Log.d(TAG_PREFIX + tag, desc, tr);
        }
    }

    public static void i(String tag, String desc) {
        if (LOG_LEVEL_INFO >= getLogLevel()) {
            Log.i(TAG_PREFIX + tag, desc);
        }
    }

    public static void i(String tag, String desc, Throwable tr) {
        if (LOG_LEVEL_INFO >= getLogLevel()) {
            Log.i(TAG_PREFIX + tag, desc, tr);
        }
    }

    public static void w(String tag, String desc) {
        if (LOG_LEVEL_WARNING >= getLogLevel()) {
            Log.w(TAG_PREFIX + tag, desc);
        }
    }

    public static void w(String tag, String desc, Throwable e) {
        if (LOG_LEVEL_WARNING >= getLogLevel()) {
            Log.w(TAG_PREFIX + tag, desc, e);
        }
    }

    public static void e(String tag, String desc) {
        if (LOG_LEVEL_ERROR >= getLogLevel()) {
            Log.e(TAG_PREFIX + tag, getMsgWithMethodNLine(desc));
        }
    }

    public static void e(String tag, String desc, Throwable tr) {
        if (LOG_LEVEL_ERROR >= getLogLevel()) {
            Log.e(TAG_PREFIX + tag, getMsgWithMethodNLine(desc), tr);
        }
    }

    /**
     * property persist.sys.loglevel.tvapp should be one of:"e","w","i","d","v"
     * or no such property
     *
     * @return logLevel
     */
    private static int getLogLevel() {
        String logLevelStr = SystemProperties.get(LOG_LEVEL_PROPERTY_NAME, "v");
        if (logLevelStr != null && logLevelStr.length() > 0) {
            char c = logLevelStr.charAt(0);
            for (int i = 0; i < LOG_LEVEL_ARRAY.length; i++) {
                if (LOG_LEVEL_ARRAY[i] == c) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String getMsgWithMethodNLine(String pMsg) {
        if (TextUtils.isEmpty(pMsg)) {
            pMsg = MSG_EMPTY;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        appendMethodName(sb);
        sb.append(" :  ");
        appendLineNumber(sb);
        sb.append("] ");
        sb.append(pMsg);
        return sb.toString();
    }

    private static void appendMethodName(StringBuilder sb) {
        sb.append(Thread.currentThread().getStackTrace()[STACK_LEVEL]
                .getMethodName());
    }

    private static void appendLineNumber(StringBuilder sb) {
        sb.append(Thread.currentThread().getStackTrace()[STACK_LEVEL]
                .getLineNumber());
    }
}

