package com.hisilicon.tvui.pvr;

/**
 * constant class
 * 常量类
 * constant
 * 常量
 * @author  h00217063
 * @since  1.0
 */
public class Constant
{
    /** current video path */
    /** 当前视频路径 */
    public static final String KEY_PATH = "URL";

    /** the position of current video in the list */
    /** 当前视频在列表中的位置 */
    public static final String KEY_POS = "position";

    /** full screen */
    /** 全屏 */
    public static final int SCREEN_FULL = 0;

    /** default screen */
    /** 普屏 */
    public static final int SCREEN_DEFAULT = 1;

    /** keycode of next */
    /** 下一首按键值 */
    public static final int KEYCODE_NEXT = 93;

    /** keycode of pre */
    /** 上一首按键值 */
    public static final int KEYCODE_PRE = 92;

    /** keycode of forward */
    /** 快进 */
    public static final int FORWARD = 200;

    /** keycode of rewind */
    /** 快退 */
    public static final int REWIND = 201;

    /** set track */
    /** 设置音轨 */
    public static final int SET_TRACK = 12;

    /** get track */
    /** 获得音轨 */
    public static final int GET_TRACK = 205;

    /** set subtitle id */
    /** 设置字幕 */
    public static final int SET_SUB_ID = 101;

    /** get the selected subtitle id */
    /** 获取当前选中字幕 */
    public static final int GET_SUB_ID = 102;

    /** get subtitle info */
    /** 获取字幕信息 */
    public static final int SUB_INFO = 103;

    /** disable subtitle or not */
    /** 设置是否显示字幕 */
    public static final int SUB_DISABLE = 135;

    /** database version */
    /** 数据库版本号 */
    public static final int VERSION = 1;

    /** bookmark table name */
    /** 书签表名 */
    public static final String VIDEO_BOOKMARK = "video_bookmark";

    /** video table name */
    /** 视频表名 */
    public static final String TABLE_VIDEO = "video";

    /** max progress */
    /** 最大进度 */
    public static final int MAX_PROGRESS = 1000;

    /** the message of updata bar */
    /** 更新进度条消息 */
    public static final int MSG_UPDATE_BAR = 1001;

    /** the message of hide the media controller */
    /** 隐藏进度条消息 */
    public static final int MSG_HIDE_MEDIA_CONTROLLER = 1002;

    public static final int MSG_HIDE_DIALOG = 1003;

    /** the delay time of hide the media controller */
    /** 隐藏控制条延迟时间 */
    public static final int HIDE_DELAY = 10000;

    /** play mode of cycle */
    /** 循环播放 */
    public static final int PLAY_MODE_CYCLE = 0;

    /** play mode of random */
    /** 随机播放 */
    public static final int PLAY_MODE_RANDOM = 1;

    /** the file name which saved play mode */
    /** 存储播放模式的临时文件名 */
    public static final String SHARE_NAME = "tempinfo";

    /** field name of play mode */
    /** 存储播放模式的字段名 */
    public static final String SHARE_PLAY_MODE_KEY = "playmode";

    /**
     * Poweroff time
     */
    public static final String SETTING_POWEROFF_TIME = "setting_poweroff_time";

    /**
     * Poweroff time
     */
    public static final String SETTING_POWEROFF_REPEAT = "setting_poweroff_repeat";
}
