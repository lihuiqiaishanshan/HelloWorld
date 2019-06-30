package com.hisilicon.android.videoplayer.menu;

import com.hisilicon.android.videoplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2018/6/29.
 */

public class ThirdMenuConfig {

    public static final String SUB_ADVANCED_TIME_ADJUSTEMNT = "Time Adjustment";
    public static final String SUB_ADVANCED_SUBTITLE_ENCODE = "Subtitle Encode";
    public static final String SUB_ADVANCED_FONT_COLOR = "Font Color";
    public static final String SUB_ADVANCED_FONT_SIZE = "Font Size";
    public static final String SUB_ADVANCED_FONT_EFFECT = "Font Effect";
    public static final String SUB_ADVANCED_THE_LINE_PACING = "The Line Spacing";
    public static final String SUB_ADVANCED_CHARACTER_SPACING = "Character Spacing";
    public static final String SUB_ADVANCED_3D_SUBTITLE_ADJECT = "3D Subtitle Adjust";


    private static List<MenuKeyValuePair> commonSubAdvOptVp = new ArrayList<MenuKeyValuePair>();

    static {
        initCommSubAdvOptVp();
    }


    private static void initCommSubAdvOptVp() {
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_TIME_ADJUSTEMNT, R.string.time_adjustment));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_SUBTITLE_ENCODE, R.string.subtitle_encode));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_FONT_COLOR, R.string.font_color));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_FONT_SIZE, R.string.font_size));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_FONT_EFFECT, R.string.font_effect));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_THE_LINE_PACING, R.string.line_space));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_CHARACTER_SPACING, R.string.character_spacing));
        commonSubAdvOptVp.add(new MenuKeyValuePair(SUB_ADVANCED_3D_SUBTITLE_ADJECT, R.string._3d_subtitle_adjust));
    }


    public List<MenuKeyValuePair> loadSubAdvOptVp() {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();
        ret.addAll(commonSubAdvOptVp);
        ret.addAll(loadDiffAdvOptVp());
        return commonSubAdvOptVp;
    }

    private List<MenuKeyValuePair> loadDiffAdvOptVp() {
        List<MenuKeyValuePair> ret = new ArrayList<MenuKeyValuePair>();

        return ret;
    }

}
