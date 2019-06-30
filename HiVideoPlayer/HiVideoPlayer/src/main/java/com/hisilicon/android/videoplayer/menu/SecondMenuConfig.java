package com.hisilicon.android.videoplayer.menu;

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

public class SecondMenuConfig {
    /**
     * 字幕设置菜单相关
     */
    public static final String SUB_OPT_ON_OFF = "Subtitle On-Off";
    public static final String SUB_OPT_SWITCHING = "Subtitle Switching";
    public static final String SUB_OPT_SEARCH = "Subtitle Search";
    public static final String SUB_OPT_ADVANCED = "Subtitle Advanced";

    public static final String SUB_OPT_SELECT = "Subtitle Select";


    /**
     * 音频设置相关
     */
    public static final String AUDIO_OPT_TRACK_SWITCH = "Track Switch";
    public static final String AUDIO_OPT_CHANNEL_SWITCH = "Channel Switch";


    /**
     * 高级设置相关菜单
     */
    public static final String ADVANCED_CONTINUE_PLAY = "Continue Play";
    public static final String ADVANCED_FORWARD_REWIND = "Forward Rewind";
    public static final String ADVANCED_FULL_SCREEN = "Full-Screen Playback";
    public static final String ADVANCED_SINGLE_CYCLY = "Single Cycle";
    public static final String ADVANCED_3D_MVC_ADPTION = "3D MVC Adaption";


    public static final String ADVANCED_SDR_MODE = "SDR Mode";

    public static final String ADVANCED_DTS_DRC = "DTS DRC Mode";


    /**
     * 3D选项
     */
    public static final String MODE_MVC_2D_TO_3D = "2D MVC Mode -> 3D";
    public static final String MODE_SBS_2D_TO_3D = "2D SBS Mode -> 3D";
    public static final String MODE_TAB_2D_TO_3D = "2D TAB Mode -> 3D";


    public static final String MODE_3D_TO_2D = "3D -> 2D Mode";
    public static final String MODE_3D_TO_2D_SIDE_EXCHANGE = "3D -> 3D Side Exchange";
    public static final String MODE_3D_TO_2D_STEREO_DEPTH = "3D Stereo Depth";

    public static final String MODE_2D_TO_2D_BEFORE_SBS = "2D SBS Mode -> 2D Mode";
    public static final String MODE_2D_TO_2D_BEFORE_TAB = "2D TAB Mode -> 2D Mode";

    public static final String MODE_2D_TO_2D_AFTER = "2D -> 2D Original Mode";

    public static final String TIMING_3D_FRAME_PACKING = "Frame Packing";
    public static final String TIMING_3D_SIDE_BY_SIDE = "Side By Side";
    public static final String TIMING_3D_TOP_AND_BOTTOM = "Top And Bottom";

    public static final String MODE_8K_MOUSE = "8k Mode Mouse";
    public static final String MODE_8K_CROSS = "8k Mode Cross";
    /**
     * 通用
     */
    private static List<MenuKeyValuePair> commonSubOptVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commonIpTvSubOptVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commonAudioOptVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commonJumpTimeVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> commonAdvancedOptVp = new ArrayList<MenuKeyValuePair>();

    private static List<MenuKeyValuePair> convert2Dto3DVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> convert3Dto2DVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> convert2Dto2DBeforeVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> convert2Dto2DOriginalVp = new ArrayList<MenuKeyValuePair>();
    private static List<MenuKeyValuePair> convert3DTIMINGVp = new ArrayList<MenuKeyValuePair>();

    private static List<MenuKeyValuePair> mode8kVp = new ArrayList<MenuKeyValuePair>();


    static {
        initCommSubOptVp();
        initCommIpTvSubOptVp();
        initCommAudioOptVp();
        initCommJumpTimeVp();
        initCommAdvancedOptVp();
        init3DOptions();
        initMode8kVp();
    }

    private static void initCommSubOptVp() {
        commonSubOptVp.add(new MenuKeyValuePair(SUB_OPT_ON_OFF, R.string.subtitle_on_off, R.drawable.iconp));
        commonSubOptVp.add(new MenuKeyValuePair(SUB_OPT_SWITCHING, R.string.subtitle_switching, R.drawable.iconq));
        commonSubOptVp.add(new MenuKeyValuePair(SUB_OPT_SEARCH, R.string.subtitle_search, R.drawable.icont));
        commonSubOptVp.add(new MenuKeyValuePair(SUB_OPT_ADVANCED, R.string.subtitle_advanced, R.drawable.iconu));
    }

    private static void initCommIpTvSubOptVp() {
        commonIpTvSubOptVp.addAll(commonSubOptVp);
        commonIpTvSubOptVp.add(new MenuKeyValuePair(SUB_OPT_SELECT, R.string.subtitle_select, R.drawable.iconr));
    }

    private static void initCommAudioOptVp() {
        commonAudioOptVp.add(new MenuKeyValuePair(AUDIO_OPT_TRACK_SWITCH, R.string.track_switch, R.drawable.iconn));
        commonAudioOptVp.add(new MenuKeyValuePair(AUDIO_OPT_CHANNEL_SWITCH, R.string.channel_switch, R.drawable.icong));
    }

    private static void initCommJumpTimeVp() {
    }

    private static void initCommAdvancedOptVp() {
        commonAdvancedOptVp.add(new MenuKeyValuePair(ADVANCED_CONTINUE_PLAY, R.string.continue_play));
        commonAdvancedOptVp.add(new MenuKeyValuePair(ADVANCED_FORWARD_REWIND, R.string.forward_rewind));
        commonAdvancedOptVp.add(new MenuKeyValuePair(ADVANCED_FULL_SCREEN, R.string.full_screen_playback));
        commonAdvancedOptVp.add(new MenuKeyValuePair(ADVANCED_SINGLE_CYCLY, R.string.single_cycle));
        commonAdvancedOptVp.add(new MenuKeyValuePair(ADVANCED_3D_MVC_ADPTION, R.string._3d_mvc_adaption));
    }


    private static void init3DOptions() {
        //3D mode 初始
        convert2Dto3DVp.add(new MenuKeyValuePair(MODE_MVC_2D_TO_3D, R.string._2d_mvc_mode_to_3d));
        convert2Dto3DVp.add(new MenuKeyValuePair(MODE_SBS_2D_TO_3D, R.string._2d_sbs_mode_to_3d));
        convert2Dto3DVp.add(new MenuKeyValuePair(MODE_TAB_2D_TO_3D, R.string._2d_tab_mode_to_3d));
        convert2Dto3DVp.add(new MenuKeyValuePair(MODE_2D_TO_2D_BEFORE_SBS, R.string._2d_sbs_mode_to_2d_mode));
        convert2Dto3DVp.add(new MenuKeyValuePair(MODE_2D_TO_2D_BEFORE_TAB, R.string._2d_tab_mode_to2d_mode));
        //切到 3D
        convert3Dto2DVp.add(new MenuKeyValuePair(MODE_3D_TO_2D, R.string._3d_to_2d_mode));
        convert3Dto2DVp.add(new MenuKeyValuePair(MODE_3D_TO_2D_SIDE_EXCHANGE, R.string._3d_to_3d_side_exchange));
        convert3Dto2DVp.add(new MenuKeyValuePair(MODE_3D_TO_2D_STEREO_DEPTH, R.string._3d_stereo_depth));
        //2D mode 初始
        convert2Dto2DBeforeVp.add(new MenuKeyValuePair(MODE_2D_TO_2D_BEFORE_SBS, R.string._2d_sbs_mode_to_2d_mode));
        convert2Dto2DBeforeVp.add(new MenuKeyValuePair(MODE_2D_TO_2D_BEFORE_TAB, R.string._2d_tab_mode_to2d_mode));
        //切换到2D后
        convert2Dto2DOriginalVp.add(new MenuKeyValuePair(MODE_2D_TO_2D_AFTER, R.string._2d_to_2d_original_mode));

        convert3DTIMINGVp.add(new MenuKeyValuePair(TIMING_3D_FRAME_PACKING, R.string.frame_packing));
        convert3DTIMINGVp.add(new MenuKeyValuePair(TIMING_3D_SIDE_BY_SIDE, R.string.side_by_side));
        convert3DTIMINGVp.add(new MenuKeyValuePair(TIMING_3D_TOP_AND_BOTTOM, R.string.top_and_bottom));
    }

    private static void initMode8kVp() {
        mode8kVp.add(new MenuKeyValuePair(MODE_8K_MOUSE, R.string._8k_mode_mouse));
        mode8kVp.add(new MenuKeyValuePair(MODE_8K_CROSS, R.string._8k_mode_cross));
    }

    /**
     * 加载Subtitle Options 菜单项
     *
     * @return
     */
    public List<MenuKeyValuePair> loadSubOptVp() {
        if (VersionControl.isIptvEnable()) {
            return commonIpTvSubOptVp;
        } else {
            return commonSubOptVp;
        }
    }

    /**
     * 加载Audio Options 菜单项
     *
     * @return
     */
    public List<MenuKeyValuePair> loadAudioOptVp() {
        return commonAudioOptVp;
    }

    /**
     * 加载高级设置菜单项
     *
     * @return
     */
    public List<MenuKeyValuePair> loadAdvancedOptVp() {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(commonAdvancedOptVp);
        ret.addAll(getDiffAdvancedOptVpByVersion());
        return ret;
    }


    public List<MenuKeyValuePair> getDiffAdvancedOptVpByVersion() {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        String brandName = SystemProperties.get(VersionControl.PRODUCT_BRAND);
        if (TextUtils.isEmpty(brandName)) {
            LogTool.d("Brand name is null");
            return ret;
        }
        if (brandName.startsWith(VersionControl.BRAND_HIDPT)) { // DPT 分支
            if (SystemProperties.get("ro.HiDPTAndroid.version").contains("V300R002")) {
                //DptAndroid V300R200
                ret.add(new MenuKeyValuePair(ADVANCED_SDR_MODE, R.string.sdr_mode));
                ret.add(new MenuKeyValuePair(ADVANCED_DTS_DRC, R.string.dts_drc_mode));
                return ret;
            }
            if (Build.VERSION.SDK_INT >= 26) {
                //DPT  O 版本
                ret.add(new MenuKeyValuePair(ADVANCED_SDR_MODE, R.string.sdr_mode));
            } else if (Build.VERSION.SDK_INT >= 21) {
                ret.add(new MenuKeyValuePair(ADVANCED_SDR_MODE, R.string.sdr_mode));
                ret.add(new MenuKeyValuePair(ADVANCED_SDR_MODE, R.string.dts_drc_mode));
            } else { //低版本 // Dpt与Stb 4.4 功能基本一致

            }
        } else {
            if (Build.VERSION.SDK_INT >= 26) {//高版本
                ret.add(new MenuKeyValuePair(ADVANCED_SDR_MODE, R.string.sdr_mode));
            } else { //低版本

            }
        }

        return ret;
    }


    public List<MenuKeyValuePair> load2Dto3DVp() {
        return convert2Dto3DVp;
    }

    public List<MenuKeyValuePair> load3Dto2DVp() {
        return convert3Dto2DVp;
    }

    public List<MenuKeyValuePair> load2Dto2DAfterVp() {
        return convert2Dto2DBeforeVp;
    }

    public List<MenuKeyValuePair> load2Dto2DOriginalVp() {
        return convert2Dto2DOriginalVp;
    }

    public List<MenuKeyValuePair> load3DTiming() {
        return convert3DTIMINGVp;
    }

    public List<MenuKeyValuePair> load8kModeVp() {
        return mode8kVp;
    }

    public List<MenuKeyValuePair> getMenuConfigByKey(String key) {
        if (TextUtils.isEmpty(key)) {
            return new ArrayList<MenuKeyValuePair>();
        }
        if (MenuConfig.SUBTITLE_OPTIONS.equals(key)) {
            return loadSubOptVp();
        } else if (MenuConfig.AUDIO_OPTIONS.equals(key)) {
            return loadAudioOptVp();
        } else if (MenuConfig.MODE_2D.equals(key)) {
            return load2Dto2DOriginalVp();
        } else if (MenuConfig.MODE_3D.equals(key)) {
            return load3Dto2DVp();
        } else if (MenuConfig.JUMP_TIME.equals(key)) {
            return new ArrayList<MenuKeyValuePair>();
        } else if (MenuConfig.ADVANCED_OPTIONS.equals(key)) {
            return loadAdvancedOptVp();
        } else if (MenuConfig.MODE_360.equals(key)) {
            return new ArrayList<MenuKeyValuePair>();
        } else if (MenuConfig.MODE_8K.equals(key)) {
            return mode8kVp;
        } else if (MenuConfig.SPEED.equals(key)) {
            return new ArrayList<MenuKeyValuePair>();
        } else if (MenuConfig.TIMING_3D.equals(key)) {
            return load3DTiming();
        }
        return new ArrayList<MenuKeyValuePair>();
    }

}
