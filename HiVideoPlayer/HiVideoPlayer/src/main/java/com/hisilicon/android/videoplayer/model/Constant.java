
package com.hisilicon.android.videoplayer.model;

/**
 * Procedure in constant
 */
public class Constant {
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
     * Lost 20s time
     */
    public static final int DISPEAR_TIME_20s = 20000;

    public static final int MIN_TO_MSECOND = 60000;

    /**
     * Parse config
     */
    public static final int ALL_MENU = 0;

    public static final int SOURCE_LIST = 1;

    /**
     * the tag of the user password in the configure file.
     */
    public static final String USER_PWD_KEY = "au8UserPW";

    public static final String COUNTRY_CODE_KEY = "au8CountryCode";

    /**
     * the default time of parental rating.
     */
    public static final String DEFAULT_PARENTAL_RATING = "off";

    /**
     * the default user password.
     */
    public static final String DEFAULT_USER_PWD = "2AC3DF314204A8F9";

    /**
     * the tag of the parental rating .
     */
    public static final String PARENTAL_RATING = "u8ParentalRating";

    /**
     * the length of the password.
     */
    public static final int PASSWORD_LENGTH = 4;

    /**
     * the Menu Lock is closed in the configure file.
     */
    public static final String MENU_LOCK = "bEnableMenuLock";

    /**
     * the tag of the Program Lock in the configure file.
     */
    public static final String PROGRAM_LOCK = "bEnableProgramLock";

    public static final String KEY_LOCK = "bEnableKeyLock";


    /**
     * DTV stop mode (Feeze/Black).
     */
    public static final String DTV_STOP_MODE_KEY = "u32StopMode";

    /**
     * sharedPreferences for change locale
     */
    public static final String SET_LOCALE = "setlocale";
    public static final String RESET_LOCALE = "resetlocale";

    /**
     * Set interface refresh message event
     */
    public static final int SETTING_UI_REFRESH_VIEWS = 0;
    public static final int EXIT_MENU = 1;

    //
    public static final String ACTION_TVPLAYER_MAIN_WINDOW = "com.hisilicon.tvui.play.MainActivity";

    /**
     * keyevent intent value key
     */
    public static final String KEYEVENT_KEYCODE = "keyevent_keycode";
    /**
     * close menu view
     */
    public static final String MENU_VIEW_CLOSE = "menu_view_close";
    /**
     * send from
     */
    public static final String SEND_FROM = "send_from";
    /**
     * Action of start scan
     */
    public static final String ACTION_START_RF_SCAN = "com.hisilicon.tvsetting.scan.start";
    /**
     * Action of scan finish
     */
    public static final String ACTION_FINISH_RF_SCAN = "com.hisilicon.tvsetting.scan.finish";
    /**
     * Action of scan aspect value change for HiTvPlayer freeze state change.
     */
    public static final String ACTION_ASPECT_CHANGE = "com.hisilicon.tvsetting.aspect.change";
    /**
     * Action of VGAAutoAdjust
     */
    public static final String VGAAutoAdjust = "com.hisilicon.tvsetting.constant.VGAAutoAdjust";
    /**
     * Specify .so plug_in name used in application.<br>
     * Input parameter of method DTV.getInstance(String value)
     */
    public static final String DTV_PLUGIN_NAME = "dtv-live://plugin.libhi_dtvplg";
    /**
     * Start ATV Intent
     */
    public static final String INTENT_ATV = "android.intent.hisiaction.HisiATV";
    /**
     * Start DTV Intent
     */
    public static final String INTENT_DTV = "android.intent.hisiaction.HisiDTV";

    /**
     * Start Source list
     */
    public static final String INTENT_SOURCE_LIST = "android.intent.hisiaction.HisiSourceSwitch";

    /**
     * Start Single Listener
     */
    public static final String INTENT_SINGLE_LISTENER = "com.hisilicon.tvsetting.HiSingleListener";

    // for Settingprovider
    /**
     * single listener
     */
    public static final String SETTING_SINGLE_LISTENER = "setting_single_listener";
    /**
     * Poweron time
     */
    public static final String SETTING_POWERON_TIME = "setting_poweron_time";

    /**
     * Poweron source
     */
    public static final String SETTING_POWERON_SOURCE = "setting_poweron_source";

    /**
     * Poweron channel
     */
    public static final String SETTING_POWERON_ATVCHANNEL = "setting_poweron_atvchanne";

    /**
     * Poweron channel
     */
    public static final String SETTING_POWERON_DTVCHANNEL = "setting_poweron_dtvchannel";

    /**
     * Poweron valume
     */
    public static final String SETTING_POWERON_VALUME = "setting_poweron_valume";

    /**
     * Poweron repeat
     */
    public static final String SETTING_POWERON_REPEAT = "setting_poweron_repeat";

    /**
     * Poweroff time
     */
    public static final String SETTING_POWEROFF_TIME = "setting_poweroff_time";

    /**
     * Poweroff time
     */
    public static final String SETTING_POWEROFF_REPEAT = "setting_poweroff_repeat";

    /**
     * Poweron mode
     */
    public static final String SETTING_BOOT_MODE = "setting_boot_mode";
    /**
     * audio language
     */
    public static final String SETTING_AUDIOLAN_PRIMARY = "setting_audiolan_primary";
    /**
     * subtitle language
     */
    public static final String SETTING_AUDIOLAN_SECONDARY = "setting_audiolan_secondary";
    /**
     * subtitle language
     */
    public static final String SETTING_SUBTITLE_PRIMARY = "setting_subtitle_primary";
    /**
     * subtitle language
     */
    public static final String SETTING_SUBTITLE_SECONDARY = "setting_subtitle_secondary";

    /**
     * boot wake switch
     */
    public static final String SETTING_BOOT_WAKE = "SETTING_BOOT_WAKE";
    /**
     * boot wake switch
     */
    public static final String SETTING_BOOT_WAKE_RS232 = "setting_boot_wake_rs232";
    /**
     * boot wake switch
     */
    public static final String SETTING_BOOT_WAKE_NET = "setting_boot_wake_net";
    /**
     * boot wake switch
     */
    public static final String SETTING_BOOT_WAKE_WIFI = "setting_boot_wake_wifi";
    /**
     * boot wake switch
     */
    public static final String SETTING_BOOT_WAKE_VGA = "setting_boot_wake_vga";
    /**
     * auto sleep time
     */
    public static final String SETTING_AUTO_SLEEP_TIME = "setting_auto_sleep_time";
    /**
     * sleep mode time
     */
    public static final String SETTING_SLEEP_MODE_TIME = "setting_sleep_mode_time";
    /**
     * dhcp mode 0 means , 1 means auto manual
     */
    public static final String SETTING_NET_DHCP_MODE = "setting_net_dhcp_mode";

    /**
     * one-key change source
     */
    public static final String SETTING_SOURCE_SOURCE1 = "setting_source_source1";
    public static final String SETTING_SOURCE_SOURCE2 = "setting_source_source2";

    /**
     * Pip position
     */
    public static final String SETTING_PIP_POSITION = "setting_pip_position";

    /**
     * Dolby Notification
     */
    public static final String SETTING_DOLBY_ONOFF = "setting_dolby_onoff";

    /**
     * AI mode(Face recognition/scene recognition)
     */
    public static final String SETTING_AI_MODE = "setting_ai_mode";

    // for Settingprovider end

    /**
     * The ATV package name
     */
    public static final String PACKAGE_ATV = "com.hisilicon.atv";

    /**
     * The DTV package name
     */
    public static final String PACKAGE_DTV = "com.hisilicon.dtv";

    /**
     * quick picture mode number
     */
    public static final int QUICK_PICTURE_MODE = 1;

    /**
     * quick sound mode number
     */
    public static final int QUICK_SOUND_MODE = 2;

    /**
     * quick aspect mode number
     */
    public static final int QUICK_ASPECT_MODE = 3;

    /**
     * quick 3D mode number
     */
    public static final int QUICK_3D_MODE = 4;

    /**
     * Mts mode number
     */
    public static final int QUICK_MTS_MODE = 5;

    /**
     * quick sleep time mode
     */
    public static final int QUICK_SLEEP_TIME_MODE = 6;

    /**
     * quick sleep time mode
     */
    public static final int QUICK_FREEZE_MODE = 7;

    /**
     * HdmiTx Show time
     */
    public static final int HDMITX_SHOW_TIME = 10;

    public static final String AUTOSLEEP_ACITION = "com.hisilicon.tvsetting.autosleep";
    public static final String STANDBY_ACITION = "com.hisilicon.tvsetting.standby";

    public static final String ACTION_TV_SETTING_FREEZE_OPEN = "com.hisilicon.tv.setting.freeze.open";
    public static final String ACTION_TV_SETTING_FREEZE_CLOSE = "com.hisilicon.tv.setting.freeze.close";

    public static final String TV_PACKAGE_NAME = "com.hisilicon.dtvui";
    public static final String TV_PACKAGE_NAME_2 = "com.hisilicon.tvui";

    public static final int AUTO_SLEEP_TIME = 1;
    public static final int SLEEP_MODE_TIME = 2;
    public static final int SCREEN_OFF_TIME = 3;

    public static final int NOSIGNAL_TIME_SHOWUI = 20; // 20秒倒计时

    public static final String TV_AUTO_TIME = "tv_auto_time";

    /**
     * the tag of the record path switch in the configure file.
     */
    public static final String RECORD_PATH = "au8RecordFilePath";

    /**
     * the tag of the timeshift duration.
     */
    public static final String TIMESHIFT_TIME = "u64TimeShiftDuration";

    /**
     * the tag of the timeshift duration.
     */
    public static final String TIMESHIFT_SIZE = "u64TimeShiftFileSize";

    /**
     * the tag of the timeshift duration.
     */
    public static final String TIMESHIFT_TIME_DEFAUFT_VALUE = "900";

    /**
     * the tag of the timeshift size.
     */
    public static final String TIMESHIFT_SIZE_DEFAUFT_VALUE = "128";// 128 M

    public static final String[] timeZoneStringMappingTab = {
            /* 0 */"Australia/Sydney",
            /* 1 */"Europe/Paris",
            /* 2 */"Europe/Rome",
            /* 3 */"Europe/London",
            /* 4 */"Asia/Dubai",
            /* 5 */"Asia/Tokyo",
            /* 6 */"Asia/Shanghai",
            /* 7 */"Asia/Calcutta",
            /* 8 */"Africa/Harare",
            /* 9 */"Asia/Karachi",
            /* 10 */"Africa/Brazzaville",
            /* 11 */"Asia/Beirut",
            /* 12 */"Asia/Jerusalem",
            /* 13 */"Asia/Tehran",
            /* 14 */"Asia/Baghdad",
            /* 15 */"Asia/Ulaanbaatar",
            /* 16 */"Asia/Damascus"
    };

    public static final String SYS_FIRST_AUDIO_LANG_KEY = "au8FirstAudioLanguage";
    public static final String SYS_SECOND_AUDIO_LANG_KEY = "au8SecondAudioLanguage";
    public static final String SYS_FIRST_SUBT_LANG_KEY = "au8FirstSubtitleLanguage";
    public static final String SYS_SECOND_SUBT_LANG_KEY = "au8SecondSubtitleLanguage";
    public static final String SYS_TELETEXT_LANG_KEY = "au8TeletextLanguage";
    public static final String SYS_FIRST_EPG_LANG_KEY = "au8FirstEPGLanguage";

    public static final String[] LANGUAGE_VALUE = {
            "eng", "fre", "por", "spa", "rus", "vie",
            "tha", "ind", "ara", "heb", "ita", "msa"
    };
}
