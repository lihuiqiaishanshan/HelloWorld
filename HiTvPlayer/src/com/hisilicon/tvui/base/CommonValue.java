package com.hisilicon.tvui.base;

/**
 * Common static final values which will be used in different activities.
 * @author y00164887
 *
 */
public class CommonValue
{
    /**
     * Specify .so plug_in name used in application.<br>
     * Input parameter of method DTV.getInstance(String value)
     */
    public static final String DTV_PLUGIN_NAME = "dtv-live://plugin.libhi_dtvplg";

    /**
     * Shared preference's file name. <br>
     * Currently, content in preference file decide whether start activity when system reboot.
     */
    public static final String DTV_PREFERCE_NAME = "dtv_preference";

    /**
     * Intent name. Used for DTVService notice DTVPlayerActivity that initialization has finished.
     */
    public static final String DTV_INTENT_INIT_FINISH = "com.hisilicon.init.finish";

    /**
     * Intent name. Used for EPGActivity notice DTVPlayerActivity to dismiss CA/No signal tip and
     * Radio Background.
     */
    public static final String DTV_INTENT_DISMISS_TIP = "com.hisilicon.dismiss.tip";

    /**
     * Intent name. Used for Activities which transparent to notice DTVPlayerActivity show/dismiss
     * No signal tip.
     */
    public static final String DTV_INTENT_SIGNAL_STATU = "com.hisilicon.signal.statu";

    /**
     * Intent name. Used for DTVService to show BookAlarmActivity.
     */
    public static final String DTV_BOOK_ALARM_REMINDE = "com.hisilicon.book.alarm.reminde";
    public static final String DTV_BOOK_ALARM_REMINDE_PLAY = "com.hisilicon.book.alarm.remindeplayer";
    /**
     * Intent name. Used for DTVService to start book task.
     */
    public static final String DTV_BOOK_ALARM_ARRIVE = "com.hisilicon.book.alarm.arrive";
    public static final String DTV_BOOK_ALARM_ARRIVE_PLAY = "com.hisilicon.book.alarm.arriveplayer";
    public static final String DTV_BOOK_ALARM_ARRIVING = "com.hisilicon.book.alarm.arriving";
    public static final String DTV_BOOK_ID = "Id";

    public static final String DTV_BOOK_DURATION = "Duration";

    public static final String DTV_BOOK_CHANNEL_ID = "ChannelId";

    public static final String DTV_BOOK_TYPE = "Type";

    /**
     * Intent name. Used for DTVService to show EwsAlarmActivity.
     */
    public static final String DTV_EWS_ALARM_REMINDE = "com.hisilicon.ews.alarm.reminde";

    public static final String DTV_EWS_DISASTER_CODE = "Disaster";
    public static final String DTV_EWS_AUTHORITY_CODE = "Authority";
    public static final String DTV_EWS_LOCATION_CODE = "Location";
    public static final String DTV_EWS_LOCATION_DESC = "LocationDesc";
    public static final String DTV_EWS_DISASTER_DESC = "DisasterDesc";
    public static final String DTV_EWS_POSITION_DESC = "Position";
    public static final String DTV_EWS_DATE_DESC = "Date";
    public static final String DTV_EWS_CHARACTER_DESC = "Character";
    public static final String DTV_EWS_MESSAGE_DESC = "Message";

    public static final String DTV_EWS_ALARM_PLAY = "com.hisilicon.ews.alarm.play";
    public static final String DTV_EWS_CHANNEL_ID = "EwsChannelID";

    public static final String DTV_PVR_START = "com.hisilicon.pvr.start";
    public static final String DTV_PVR_STOP = "com.hisilicon.pvr.stop";

    /**
     * Extra value tag of DTV_INTENT_SIGNAL_STATU intent.
     */
    public static final String SIGNAL_TAG = "SignalTag";
    public static final int VALUE_HAVE_SIGNAL = 1;
    public static final int VALUE_NO_SIGNAL = 0;

    public static final int VALUE_IS_CA = 1;
    public static final int VALUE_NOT_CA = 0;

    public static final int VALUE_AV_RESUME = 1;
    public static final int VALUE_AV_STOP = 0;
    /**
     * Extra value tag in intent. <br>
     * Used for DTVPlayerActivity notice ChannelListActivity to show Favorite list when creating.
     */
    public static final String FAV_TAG = "FavType";
    public static final int VALUE_OPEN_FAV = 1;

    /**
     * the Tag of the OAD switch in the configure file.
     */
    public static final String OAD_SWITCH = "u32SsuMode";

    /**
     * the Tag of the OAD switch in the configure file.
     */
    public static final String APP_VERSION = "u32Version";

    public static final int APP_VERSION_DEFAULT = 0x1000300;

    /**
     * the OAD switch is open.
     */
    public static final int OAD_OPEN = 0x10;

    /**
     * the OAD switch is close.
     */
    public static final int OAD_CLOSE = 0xff00;



    /**
     * the Tag of the TDT Lock in the configure file.
     */
    public static final String TDT_LOCK = "s32TimeControlSyncTDT";

    /**
     * the TDT Lock is open.
     */
    public static final int TDT_LOCK_OPEN = 1;

    /**
     * the TDT Lock is close.
     */
    public static final int TDT_LOCK_CLOSE = 0;

    /**
     * the Tag of the TOT Lock in the configure file.
     */
    public static final String TOT_LOCK = "s32TimeControlSyncTOT";

    /**
     * the TOT Lock is open.
     */
    public static final int TOT_LOCK_OPEN = 1;

    /**
     * the TOT Lock is close.
     */
    public static final int TOT_LOCK_CLOSE = 0;

    /**
     * the tot region code.
     */
    public static final String TOT_REGIONCODE_LANG = "u32TotCountryRegionId";

    /**
     * the tag of the Program Lock in the configure file.
     */
    public static final String PROGRAM_LOCK = "bEnableProgramLock";

    public static final String KEY_LOCK ="bEnableKeyLock";

    public static final String AUDIO_MODE = "au8AudioMode";
    public static final int AUDIO_MODE_STEREO = 0;
    public static final int AUDIO_MODE_MULTICHANNEL = 1;

    public static final String AUDIO_DESCRIPTION = "bEnableAD";

    /**
     * the Program Lock is open.
     */
    public static final int PROGRAM_LOCK_OPEN = 1;

    /**
     * the Program Lock is close.
     */
    public static final int PROGRAM_LOCK_CLOSE = 0;

    /**
     * the Menu Lock is closed in the configure file.
     */
    public static final String MENU_LOCK = "bEnableMenuLock";

    /**
     * the flag of select country in the configure file.
     */
    public static final String SELECT_COUNTRYCODE = "bEnableSetCountryCode";

    public static final String COUNTRY_CODE_KEY = "au8CountryCode";

    /**
     * the Menu Lock is open.
     */
    public static final int MENU_LOCK_OPEN = 1;

    /**
     * the Menu Lock is close.
     */
    public static final int MENU_LOCK_CLOSE = 0;

    /**
     * the key Lock is open.
     */
    public static final int KEY_LOCK_OPEN = 1;

    /**
     * the key Lock is close.
     */
    public static final int KEY_LOCK_CLOSE = 0;

    /**
     * the tag of the user password in the configure file.
     */
    public static final String USER_PWD_KEY = "au8UserPW";

    public static final String SYS_FIRST_AUDIO_LANG_KEY   = "au8FirstAudioLanguage";
    public static final String SYS_SECOND_AUDIO_LANG_KEY  = "au8SecondAudioLanguage";
    public static final String SYS_FIRST_SUBT_LANG_KEY    = "au8FirstSubtitleLanguage";
    public static final String SYS_SECOND_SUBT_LANG_KEY   = "au8SecondSubtitleLanguage";
    public static final String SYS_FIRST_EPG_LANG_KEY     = "au8FirstEPGLanguage";
    public static final String SYS_PRIMARY_TTX_LANG_KEY   = "au8PrimaryTeletextLanguage";
    /**
     * the tag of the inforbar show time in the configure file.
     */
    public static final String INFOBAR_SHOW_TIME = "u32InfoBarInsistTime";

    /**
     * the default show time of the inforbar.
     */
    public static final int DEFAULT_INFORBAR_SHOW_TIME = 5;

    /**
     * the tag of the timeshift to record switch in the configure file.
     */
    public static final String TIMESHIFT_TO_RECORD_SWITCH = "bEnableTimeShiftToPvr";

    /**
     * the timeshift to record switch is open.
     */
    public static final int TIMESHIFT_TO_RECORD_SWITCH_OPEN = 1;

    /**
     * the timeshift to record switch is close.
     */
    public static final int TIMESHIFT_TO_RECORD_SWITCH_CLOSE = 0;

    /**
     * the tag of the parental rating .
     */
    public static final String PARENTAL_RATING = "u8ParentalRating";

    /**
     * the default time of parental rating.
     */
    public static final String DEFAULT_PARENTAL_RATING = "off";

    /**
     * the tag of the record path switch in the configure file.
     */
    public static final String RECORD_PATH = "au8RecordFilePath";

    /**
     * the default path for recording.
     */
    public static final String DEFAULT_RECORD_PATH = "/mnt/nand";

    public static final int DEFAULT_MIN_ALLOW_REC_SPACE = 100 * 1024 * 1024;

    /**
     * the tag of the timeshift duration.
     */
    public static final String TIMESHIFT_TIME = "u64TimeShiftDuration";

    /**
     * the default time of timeshift.
     */
    public static final String DEFAULT_TIMESHIFT_TIME = "1h";

    /**
     * the tag of the super password.
     */
    public static final String SUPER_PWD = "au8SuperPW";

    /**
     * the default user password.
     */
    public static final String DEFAULT_USER_PWD = "2AC3DF314204A8F9";

    /**
     * the length of the password.
     */
    public static final int PASSWORD_LENGTH = 4;

    /**
     * The name of the AV group in stack
     */
    public static final String ALLLIST_NAME = "AV";

    /**
     * The name of the TV group in stack
     */
    public static final String TVLIST_NAME = "Tv";

    /**
     * The name of the Radio group in stack
     */
    public static final String RADIOLIST_NAME = "Radio";

    /**
     * The name of the Favorite Group
     */
    public static final String FAVLIST_NAME = "Fav";

    /**
     * Format string.
     */
    public final static String FORMAT_STR = "0000";

    /**
     * Default pop item number
     */
    public static final int DEFAULT_POP_ITEM_NUMBER = 4;

    /**
     * Send DTVPlayerActivity to close PIP
     */
    public static final String BROADCAST_CLOSE_PIP = "com.hisilicon.tvui.play.DTVPlayerActivity.ClosePIP";

    /**
     * Default video type HI_PSISI_STREAM_VIDEO_MPEG2 = 0x02;
     */
    public static final int DEFAULT_VIDEO_TYPE = 2;

    /**
     * Default audio type HI_PSISI_STREAM_AUDIO_MPEG2 = 0x04;
     */
    public static final int DEFAULT_AUDIO_TYPE = 4;

    /**
     * OneSeg flag
     */
    public static final String ONESEG_FLAG = "bOneSegEnable";

    /**
     * the OneSeg flag is on.
     */
    public static final int ONESEG_FLAG_ON = 1;

    /**
     * the OneSeg flag is off.
     */
    public static final int ONESEG_FLAG_OFF = 0;

    /**
     * Major and Minor SN flag
     */
    public static final String MAJOR_MINOR_SN_FLAG = "bMajorMinorSnEnable";

    /**
     * Major and Minor SN is enable.
     */
    public static final int MAJOR_MINOR_SN_ENABLE = 1;

    /**
     * bEnableTimeShiftToPvr
     */
     public static final  String  TIMSHIFT_TO_PVR_ENABLE = "bEnableTimeShiftToPvr";
    /**
     * Major and Minor SN is disable.
     */
    public static final int MAJOR_MINOR_SN_DISABLE = 0;

    /*
    *  Show channel list.
    */
    public static final String CHANNEL_LIST_SHOW = "com.hisilicon.view.ChannelListView";

    /*
    *  Show favorite channel list.
    */
    public static final String FAV_CHANNEL_LIST_SHOW = "com.hisilicon.view.FavChannelListView";

    /*
    *  Play DTV channel.
    */
    public static final String PLAY_DTV_ACTION = "com.hisilicon.tvui.PlayDTVAction";

    public static final String SIDE_RECORDING_AND_PLANING = "side_recording_and_planing";

    /*
    *  Hide Radio BG.
    */
    public static final String HIDE_RADIOBG = "com.hisilicon.tvui.HideRadioBG";

    /*
    *  Hide DRA txt.
    */
    public static final String HIDE_DRA = "com.hisilicon.tvui.HideDRA";
}
