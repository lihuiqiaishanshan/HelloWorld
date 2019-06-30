package com.hisilicon.android.videoplayer.menu;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.adapter.VersionControl;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.SystemProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/6/29.
 */

public class MenuConfig {


    public static final String SUBTITLE_OPTIONS = "subtitle_options";
    public static final String AUDIO_OPTIONS = "audio_options";
    public static final String JUMP_TIME = "jump_time";
    public static final String ADVANCED_OPTIONS = "advanced_options";

    public static final String TIMING_3D = "3d_timing";
    public static final String MODE_3D = "3d_mode";

    public static final String MODE_2D = "2d_mode";

    public static final String SPEED = "speed";

    public static final String MODE_360 = "360_mode";
    public static final String MODE_8K = "8k_mode";


    public static final String MODE_MKV = "mkv_mode";
    /**
     * 所有版本都有的功能
     */
    private static List<MenuKeyValuePair> commonKeyValuePair = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commMode3DKeyValuePair = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commNormalKeyValuePair = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commFmMode3DKeyValuePair = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commFmNormalKeyValuePair = new ArrayList<MenuKeyValuePair>();

    /**
     * 特定版本才有功能
     */
    private static List<MenuKeyValuePair> dptoAllKeyValuePair = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> stbOAllNormalKeyValuePair = new ArrayList<MenuKeyValuePair>();


    static {
        initCommonKeyValuePair();
        initCommMode3DKeyValuePair();
        initCommNormalKeyValuePair();
        initCommFmMode3DKeyValuePair();
        initCommFmNormalKeyValuePair();
        initDptoAllKeyValuePair();
        initStbOAllNormalKeyValuePair();
    }

    private static void initCommonKeyValuePair() {
        commonKeyValuePair.add(new MenuKeyValuePair(SUBTITLE_OPTIONS, R.string.subtitle_options, R.drawable.iconf));
        commonKeyValuePair.add(new MenuKeyValuePair(AUDIO_OPTIONS, R.string.audio_options, R.drawable.iconn));
        commonKeyValuePair.add(new MenuKeyValuePair(JUMP_TIME, R.string.jump_time, R.drawable.iconc));
        commonKeyValuePair.add(new MenuKeyValuePair(ADVANCED_OPTIONS, R.string.advanced_options, R.drawable.iconh));
    }

    private static void initCommMode3DKeyValuePair() {
        commMode3DKeyValuePair.add(new MenuKeyValuePair(MODE_3D, R.string._3d_mode, R.drawable.iconj));
        commMode3DKeyValuePair.add(new MenuKeyValuePair(TIMING_3D, R.string._3d_timing, R.drawable.icons));
    }

    private static void initCommNormalKeyValuePair() {
        commNormalKeyValuePair.add(new MenuKeyValuePair(MODE_2D, R.string._2d_mode, R.drawable.iconj));
    }

    private static void initCommFmMode3DKeyValuePair() {
        commFmMode3DKeyValuePair.add(new MenuKeyValuePair(MODE_3D, R.string._3d_mode, R.drawable.iconj));
        commFmMode3DKeyValuePair.add(new MenuKeyValuePair(TIMING_3D, R.string._3d_timing, R.drawable.icons));
    }

    private static void initCommFmNormalKeyValuePair() {
        commFmNormalKeyValuePair.add(new MenuKeyValuePair(MODE_2D, R.string._2d_mode, R.drawable.iconj));
    }


    private static void initDptoAllKeyValuePair() {
        dptoAllKeyValuePair.add(new MenuKeyValuePair(MODE_360, R.string._360_mode, R.drawable.iconh));
        dptoAllKeyValuePair.add(new MenuKeyValuePair(SPEED, R.string.speed, R.drawable.iconh));
        //dptoAllKeyValuePair.add(new MenuKeyValuePair(MODE_8K, R.string._8k_mode, R.drawable.iconh)); // hide 8K mode menu in dpt
    }

    private static void initStbOAllNormalKeyValuePair() {
        stbOAllNormalKeyValuePair.add(new MenuKeyValuePair(MODE_360, R.string._360_mode, R.drawable.iconh));
        stbOAllNormalKeyValuePair.add(new MenuKeyValuePair(SPEED, R.string.speed, R.drawable.iconh));
    }

    private Context context;

    public MenuConfig(Context context) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        }
        this.context = context;
    }


    /**
     * 加载播放器控制菜单列表
     *
     * @return
     */
    public List<MenuKeyValuePair> getPlayMenu3DMode(boolean isMkvVideo) {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(getPlayerCommonMenu());
        ret.addAll(commMode3DKeyValuePair);
        ret.addAll(getDiffMenuByVersion());
        if (isMkvVideo) {
            ret.add(new MenuKeyValuePair(MODE_MKV, R.string.mkv_video, R.drawable.iconh));
        }
        return ret;
    }

    /**
     * 加载播放器控制菜单列表
     *
     * @return
     */
    public List<MenuKeyValuePair> getPlayMenuNormalMode(boolean isMkvVideo) {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(getPlayerCommonMenu());
        ret.addAll(commNormalKeyValuePair);
        ret.addAll(getDiffMenuByVersion());
        if (isMkvVideo) {
            ret.add(new MenuKeyValuePair(MODE_MKV, R.string.mkv_video, R.drawable.iconh));
        }
        return ret;
    }

    /**
     * 加载播放器控制菜单列表
     *
     * @return
     */
    public List<MenuKeyValuePair> getPlayMenuFm3DMode(boolean isMkvVideo) {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(getPlayerCommonMenu());
        ret.addAll(commFmMode3DKeyValuePair);
        ret.addAll(getDiffMenuByVersion());
        if (isMkvVideo) {
            ret.add(new MenuKeyValuePair(MODE_MKV, R.string.mkv_video, R.drawable.iconh));
        }
        return ret;
    }

    /**
     * 加载播放器控制菜单列表
     *
     * @return
     */
    public List<MenuKeyValuePair> getPlayMenuFmNormalMode(boolean isMkvVideo) {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(getPlayerCommonMenu());
        ret.addAll(commFmNormalKeyValuePair);
        ret.addAll(getDiffMenuByVersion());
        if (isMkvVideo) {
            ret.add(new MenuKeyValuePair(MODE_MKV, R.string.mkv_video, R.drawable.iconh));
        }
        return ret;
    }

    /**
     * 获取所有版本公有控制菜单列表
     *
     * @return
     */
    private List<MenuKeyValuePair> getPlayerCommonMenu() {
        return commonKeyValuePair;
    }

    private List<MenuKeyValuePair> getDiffMenuByVersion() {
        String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (TextUtils.isEmpty(brandName)) {
            LogTool.d("Brand name is null");
            return new ArrayList<MenuKeyValuePair>();
        }
        if (brandName.startsWith(VersionControl.BRAND_HIDPT)) { // DPT 分支
            if (SystemProperties.get("ro.HiDPTAndroid.version").contains("V300R002")) {
                //DptAndroid V300R200
                return new ArrayList<MenuKeyValuePair>();
            }
            if (Build.VERSION.SDK_INT >= 25) {
                return dptoAllKeyValuePair;
            } else if (Build.VERSION.SDK_INT >= 21) { // 高版本
                return new ArrayList<MenuKeyValuePair>();
            } else { //低版本 // Dpt与Stb 4.4 功能基本一致
                return new ArrayList<MenuKeyValuePair>();
            }
        } else {
            if (Build.VERSION.SDK_INT >= 24) {//高版本
                return stbOAllNormalKeyValuePair;
            } else { //低版本
                return new ArrayList<MenuKeyValuePair>();
            }
        }
    }

    public boolean isCN() {
        return context.getResources().getConfiguration().locale.getCountry().equals("CN");
    }
}
