package com.hisilicon.tvui.util;

/**
 *
 * @author tang_shengchang HiSi.ltd <br>
 *
 *         Procedure in constant
 */
public class Util
{
    /**
     * Log tag
     */
    public static final boolean LOG_TAG = true;
    /**
     * Lost 5s time
     */
    public static final int DISPEAR_TIME = 5000;
    /**
     * Lost 10s time
     */
    public static final int DISPEAR_TIME_10s = 10000;
    /**
     * The volume of the length
     */
    // public static final int BAR_LENGTH = 100;
    /**
     * Set interface refresh message event
     */
    public static final int SETTING_UI_REFRESH_VIEWS = 0;
    public static final int EXIT_MUNE = 1;
    /**
     * Action of start scan
     */
    public static final String ACTION_START_RF_SCAN = "com.hisilicon.atv.scan.start";
    /**
     * Action of scan finish
     */
    public static final String ACTION_FINISH_RF_SCAN = "com.hisilicon.atv.scan.finish";
    /**
     * Start ATV Intent
     */
    public static final String INTENT_ATV = "android.intent.hisiaction.HisiATV";

    public static final String SETTING_AUTO_SLEEP_TIME = "setting_auto_sleep_time";

    public static final String SETTING_POWERON_ATVCHANNEL = "setting_poweron_atvchanne";

    public static final String SETTING_POWERON_DTVCHANNEL = "setting_poweron_dtvchannel";

    public static final String SETTING_PIP_POSITION = "setting_pip_position";

    public static final String SETTING_DOLBY_ONOFF = "setting_dolby_onoff";
    public static final String SETTING_CC_MODE = "setting_cc_mode";

    public static final int MIN_TO_MSECOND = 60000;

    public static final int NOSIGNAL_TIME_SHOWUI = 20; // 20秒倒计时

    public static final String SETTING_ATSC_NETWORK = "setting_atsc_network";

    /**
     * Europe DTV standby book task
     */
    public static final String PROPERTY_BOOK_TASK_TYPE = "sys.background.booktasktype";

    public static final int BOOK_TASK_TYPE_IDLE = 0;

    public static final int BOOK_TASK_TYPE_WORKING = 1;

    public static final int BOOK_TASK_TYPE_POWER_ON = 2;

    public static final int BOOK_TASK_TYPE_FINISH = 3;


    /**
     * Europe DTV auto scan, new service found flag, 0 means no new service found, 1 means new service found
     */
    public static final String SETTING_NEW_SERVICE_FOUND = "setting_new_service_found";

    public static final int HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY = 0x20000000;
    public static final int HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY = 0x40000000;
    public static final int HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY = 0x80000000;
}
