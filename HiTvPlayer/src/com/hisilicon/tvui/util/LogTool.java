package com.hisilicon.tvui.util;

import android.text.TextUtils;
import android.os.SystemProperties;
import android.util.Log;

public class LogTool
{
    private static final int STACK_LEVEL = 5;

    private static final String MSG_EMPTY = "Empty Msg";

    private static final String TAG_PREFIX = "HiTvApp_Player_";

    private static final String LOG_LEVEL_PROPERTY_NAME = "persist.sys.loglevel.tvapp";

    public static final char[] LOG_LEVEL_ARRAY = new char[] { 'v', 'd', 'i', 'w', 'e' };

    public static final int LOG_LEVEL_VERBOSE = 0;
    public static final int LOG_LEVEL_DEBUG = 1;
    public static final int LOG_LEVEL_INFO = 2;
    public static final int LOG_LEVEL_WARNING = 3;
    public static final int LOG_LEVEL_ERROR = 4;

    public static final int MAUDIO = 1;
    public static final int MCHANNEL = 2;
    public static final int MCOMP = 4;
    public static final int MEPG = 8;
    public static final int MMAIN = 16;
    public static final int MREC = 32;
    public static final int MSCAN = 64;
    public static final int MSUBTITLE = 128;
    public static final int MINSTALL = 256;
    public static final int MBASE = 512;
    public static final int MPLAY = 1024;
    public static final int MSERVICE = 2048;
    public static final int MFIND = 2048;
    public static final int MSETTING = 2048;
    public static final int MALLLOG = 0xFFFFFFF;

    private static int mModuleType = MALLLOG;
    private static int mSubModuleType = MALLLOG;


    /**
     * Set the log output module and submodule. <br>
     *
     * @param nModule module is : MAUDIO,MCHANNEL,MCOMP,MEPG,MMAIN,MREC,MSCAN,MSUBTITLE,MINSTALL
     * @param nSubType submodule is defined by developer of module
     */
    public static void setLogModule(int nModule, int nSubType)
    {
        mModuleType = nModule;
        mSubModuleType = nSubType;
    }

    /**
     * Set the log output module. <br>
     *
     * @param nModule module is : MAUDIO,MCHANNEL,MCOMP,MEPG,MMAIN,MREC,MSCAN,MSUBTITLE,MINSTALL
     */
    public static void setLogModule(int nModule)
    {
        mModuleType = nModule;
        mSubModuleType = MALLLOG;
    }

    public static void v(int nModule, String pMsg)
    {
        if ((LOG_LEVEL_VERBOSE >= getLogLevel()) && (0 != (mModuleType & nModule)))
        {
            Log.v(getFinalTag(), pMsg);
        }
    }

    public static void d(int nModule, String pMsg)
    {
        if ((LOG_LEVEL_DEBUG >= getLogLevel()) && (0 != (mModuleType & nModule)))
        {
            Log.d(getFinalTag(), pMsg);
        }
    }

    public static void i(int nModule, String pMsg)
    {
        if ((LOG_LEVEL_INFO >= getLogLevel()) && (0 != (mModuleType & nModule)))
        {
            Log.i(getFinalTag(), pMsg);
        }
    }

    public static void w(int nModule, String pMsg)
    {
        if ((LOG_LEVEL_WARNING >= getLogLevel()) && (0 != (mModuleType & nModule)))
        {
            Log.w(getFinalTag(), pMsg);
        }
    }

    public static void e(int nModule, String pMsg)
    {
        if ((LOG_LEVEL_ERROR >= getLogLevel()) && (0 != (mModuleType & nModule)))
        {
            Log.e(getFinalTag(), getMsgWithMethodNLine(pMsg));
        }
    }

    // ////////////
    public static void v(int nModule, int nSubModule, String pMsg)
    {
        if ((LOG_LEVEL_VERBOSE >= getLogLevel()) && (0 != (mModuleType & nModule)) && (0 != (mSubModuleType & nSubModule)))
        {
            Log.v(getFinalTag(), pMsg);
        }
    }

    public static void d(int nModule, int nSubModule, String pMsg)
    {
        if ((LOG_LEVEL_DEBUG >= getLogLevel()) && (0 != (mModuleType & nModule)) && (0 != (mSubModuleType & nSubModule)))
        {
            Log.d(getFinalTag(), pMsg);
        }
    }

    public static void i(int nModule, int nSubModule, String pMsg)
    {
        if ((LOG_LEVEL_INFO >= getLogLevel()) && (0 != (mModuleType & nModule)) && (0 != (mSubModuleType & nSubModule)))
        {
            Log.i(getFinalTag(), pMsg);
        }
    }

    public static void w(int nModule, int nSubModule, String pMsg)
    {
        if ((LOG_LEVEL_WARNING >= getLogLevel()) && (0 != (mModuleType & nModule)) && (0 != (mSubModuleType & nSubModule)))
        {
            Log.w(getFinalTag(), pMsg);
        }
    }

    public static void e(int nModule, int nSubModule, String pMsg)
    {
        if ((LOG_LEVEL_ERROR >= getLogLevel()) && (0 != (mModuleType & nModule)) && (0 != (mSubModuleType & nSubModule)))
        {
            Log.e(getFinalTag(), getMsgWithMethodNLine(pMsg));
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


    private static String getFinalTag()
    {
        return TAG_PREFIX + getClassName();
    }

    private static String getClassName()
    {
        String strClassName = Thread.currentThread().getStackTrace()[STACK_LEVEL].getClassName();
        String strRet = strClassName;
        int nLow = strClassName.lastIndexOf('.') + 1;
        if ((nLow > 0) && (nLow < strClassName.length()))
        {
            strRet = strClassName.substring(nLow);
        }
        return strRet;
    }
}
