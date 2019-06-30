
package com.hisilicon.launcher.util;

import android.net.Uri;

/**
 * @author tang_shengchang HiSi.ltd <br>
 *         Procedure in constant
 */
public class Constant {

    /**
     * log tag
     */
    public static final boolean LOG_TAG = true;
    /**
     * Lost time
     */
    public static final int DISPEAR_TIME = 5000;
    /**
     * The volume of the length
     */
    public static final int BARLENGTH = 100;
    /**
     * Set interface refresh message event
     */
    public static final int SETTING_UI_REFRESH_VIEWS = 0;
    /**
     * Start ATV Intent
     */
    public static final String INTENT_ATV = "android.intent.hisiaction.HisiATV";
    /**
     * start DTV Intent
     */
    public static final String INTENT_DTV = "android.intent.hisiaction.HisiDTV";
    /**
     * start quickSetting Intent
     */
    public static final String INTENT_QUICKSETTING = "android.intent.hisiaction.HisiQuicksetting";

    /**
     * sharedPreferences for change locale
     */
    public static final String SET_LOCALE = "setlocale";
    public static final String RESET_LOCALE = "resetlocale";
    public static final String TEST_VEDIO_PATH = "/mnt/sdcard/test.mp4";
    public static final String DATA_LOCAL = "dataLocal";
    public static final String DATA_CURRENT_SCREEN = "currentScreen";
    /**
     * Mark index of view which currently has focus
     */
    public static final int NUMBER_0 = 0;
    public static final int NUMBER_1 = 1;
    public static final int NUMBER_2 = 2;
    public static final int NUMBER_3 = 3;
    public static final int NUMBER_4 = 4;
    public static final int NUMBER_5 = 5;
    public static final int NUMBER_6 = 6;
    public static final int NUMBER_7 = 7;
    public static final int NUMBER_8 = 8;
    public static final int NUMBER_9 = 9;
    public static final int NUMBER_10 = 10;
    public static final int NUMBER_11 = 11;

    /**
     * views scare animation duration
     */
    public static final int SCARE_ANIMATION_DURATION = 100;

    public static final String SOURCEDATA = "sourceidx";

    /**
     * boot mode
     */
    public static final String SETTING_BOOT_MODE = "setting_boot_mode";
    /**
     *net state
     */
    public static final String ETHERNET_STATE = "main_ethernet_state";

    public static final String PPPOE_STATE = "main_pppoe_state";

    public static final String WIFI_STATE = "main_wifi_state";

    /**
     * boot mode source
     */
    //for setting provider
    public static final String SETTING_BOOT_MODE_SOURCE = "setting_poweron_source";

    public static final String SETTING_POWERON_VALUME = "setting_poweron_valume";

    public static final String SETTING_POWERON_REPEAT = "setting_poweron_repeat";

    public static final String SETTING_POWERON_TIME = "setting_poweron_time";

    public static final String AUTHORITY = "com.hisilicon.tvsetting.model";

    public static final Uri CONTENT_ITEM_URI = Uri.parse("content://"
            + AUTHORITY + "/delay");

    public static final String _ID = "_id";

    public static final String NAME = "name";

    public static final String VALUE = "value";

    public static final String CONTENT_SETTINGS_TYPE = "vnd.android.cursor.settings/com.hisilicon.tvsetting";

    public static final Uri CONTENT_SETTINGS_URI  = Uri.parse("content://"
            + AUTHORITY + "/settings");
    public static final String DTV_PLUGIN_NAME = "dtv-live://plugin.libhi_dtvplg";
    /**
     * the tag of the parental rating .
     */
    public static final String PARENTAL_RATING = "u8ParentalRating";
    /**
     * the tag of the Program Lock in the configure file.
     */
    public static final String PROGRAM_LOCK = "bEnableProgramLock";

    /**
     * the Menu Lock is closed in the configure file.
     */
    public static final String MENU_LOCK = "bEnableMenuLock";

    /**
     * the flag of select country in the configure file.
     */
    public static final String SELECT_COUNTRYCODE = "bEnableSetCountryCode";
 /**
     * Europe DTV auto scan, new service found flag, 0 means no new service found, 1 means new service found
     */
    public static final String SETTING_NEW_SERVICE_FOUND = "setting_new_service_found";
    /**
     * lock enum.
     */
    public static final int SOURCE_LOCK     = 1;
    public static final int CHANNEL_LOCK    = 2;
    public static final int PARENTAL_LOCK   = 3;
    public static final int SOURCE_LOCK_TYPE    = 0;
    public static final int CHANNEL_LOCK_TYPE   = 1;
    public static final int PARENTAL_LOCK_TYPE  = 2;

    public static final String SETTING_POWERON_ATVCHANNEL = "setting_poweron_atvchanne";
	   /**
     * Europe DTV standby book task
     */
    public static final String PROPERTY_BOOK_TASK_TYPE = "sys.background.booktasktype";

    public static final int BOOK_TASK_TYPE_IDLE = 0;

    public static final int BOOK_TASK_TYPE_WORKING = 1;

    public static final int BOOK_TASK_TYPE_POWER_ON = 2;

    public static final int BOOK_TASK_TYPE_FINISH = 3;
}
