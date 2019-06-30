
package com.hisilicon.launcher.interfaces;

import java.util.ArrayList;

import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.SourceManager;
import com.hisilicon.android.tvapi.vo.RectInfo;
import com.hisilicon.android.tvapi.vo.TimingInfo;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.launcher.interfaces.PictureInterface;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.FullScreenRectInfo;

public class SourceManagerInterface {

    private static final String TAG = "SourceManagerInterface";

    public static SourceManager getSourceManager() {
        return HitvManager.getInstance().getSourceManager();
    }
    public static TvSourceManager getTvSourceManager() {
        return TvSourceManager.getInstance();
    }

    public static int deselectSource(int srcId, boolean bDestroy) {
        LogHelper.d(TAG, "deselectSource(int srcId = " + srcId
                + ", boolean bDestroy = " + bDestroy + ")  begin");

        int value = getTvSourceManager().deselectSource(srcId, bDestroy);

        LogHelper.d(TAG, "deselectSource(int srcId = " + srcId
                + ", boolean bDestroy =" + bDestroy + ") end value = "
                + value);
        return value;
    }

    /**
     * set enable dualdisplay
     */
    public static int enableDualDisplay(boolean enable) {
        LogHelper.d(TAG, "enableDualDisplay(boolean enable = " + enable
                + ") begin");

        int value = getSourceManager().enableDualDisplay(enable);

        LogHelper.d(TAG, "enableDualDisplay(boolean enable = " + enable
                + ") end value = " + value);
        return value;
    }

    /**
     * get avail source list
     */
    public static ArrayList<Integer> getAvailSourceList() {
        LogHelper.d(TAG, "getAvailSourceList() begin");

        ArrayList<Integer> value = getSourceManager().getAvailSourceList();

        LogHelper.d(TAG, "getAvailSourceList() end value = " + value);
        return value;
    }

    /**
     * get cursource id
     */
    public static int getCurSourceId() {
        LogHelper.d(TAG, "getCurSourceId() begin");

        int value = getTvSourceManager().getCurSourceId(0);

        LogHelper.d(TAG, "getCurSourceId() end value = " + value);
        return value;
    }

    /**
     * Get current SourceId saved
     */
    public static int getSelectSourceId() {
        LogHelper.d(TAG, "getSelectSourceId() begin");

        int value = getTvSourceManager().getSelectSourceId();

        LogHelper.d(TAG, "getSelectSourceId() end value = " + value);
        return value;
    }

    /**
     * get signal status
     */
    public static int getSignalStatus() {
        LogHelper.d(TAG, "getSignalStatus() begin");

        int value = getTvSourceManager().getSignalStatus();

        LogHelper.d(TAG, "getSignalStatus() end value = " + value);
        return value;
    }

    /**
     * get source list
     */
    public static ArrayList<Integer> getSourceList() {
        LogHelper.d(TAG, "getSourceList() begin");

        ArrayList<Integer> value = getSourceManager().getSourceList();

        LogHelper.d(TAG, "getSourceList() end value = " + value);
        return value;
    }

    /**
     * get Timming
     */
    public static TimingInfo getTimingInfo() {
        LogHelper.d(TAG, "getTimingInfo() begin");

        TimingInfo value = getSourceManager().getTimingInfo();

        LogHelper.d(TAG, "getTimingInfo() end value = " + value);
        return value;
    }

    /**
     * get isDVI model
     */
    public static boolean isDVIMode() {
        LogHelper.d(TAG, "isDVIMode() begin");

        boolean value = getSourceManager().isDVIMode();

        LogHelper.d(TAG, "isDVIMode() end value = " + value);
        return value;
    }

    public static int setWindowRect(RectInfo rect, int mainWindow) {

        int res = getSourceManager().setWindowRect(rect, mainWindow);

        LogHelper.d(TAG, "SourceManager  setWindowRect ret = " + res);

        return res;

    }

    /**
     * set window size
     */
    public static int setFullWindow(boolean isFullScreen) {
        LogHelper.d(TAG, "setFullWindow(" + isFullScreen +")");
        int value = getTvSourceManager().setFullWindow(isFullScreen);
        return value;
    }

    /**
     * select source
     */
    public static int selectSource(int srcId, int nMainWindow) {
        LogHelper.d(TAG, "selectSource(int srcId = " + srcId
                + ", int nMainWindow =" + nMainWindow + ")  begin");
        int value = -1;
        value = getTvSourceManager().selectSource(srcId, nMainWindow);
        LogHelper.d(TAG, "selectSource(int srcId = " + srcId
                + ", int nMainWindow = " + nMainWindow
                + ")  end value = " + value);
        return value;
    }

    /**
     * select source
     */
    public static int selectSource(int srcId, int nMainWindow, int channelId) {
        LogHelper.d(TAG, "selectSource(int srcId = " + srcId
                + ", int nMainWindow =" + nMainWindow + "channelId = " + channelId +")  begin");
        int value = -1;
        value = getTvSourceManager().selectSource(srcId, nMainWindow, channelId);
        LogHelper.d(TAG, "selectSource(int srcId = " + srcId
                + ", int nMainWindow = " + nMainWindow
                + "channelId = " + channelId + ")  end value = " + value);
        return value;
    }

    /**
     * Set the window said displayed in the left or right, if the two terminal
     * display this interface must be set
     */
    public static int setDisplayOnLeft(boolean left, int nMainWindow) {
        LogHelper.d(TAG, "setDisplayOnLeft(boolean left = " + left
                + ", int nMainWindow = " + nMainWindow + ")  begin");

        int value = getSourceManager().setDisplayOnLeft(left, nMainWindow);

        LogHelper.d(TAG, "setDisplayOnLeft(boolean left = " + left
                + ", int nMainWindow = " + nMainWindow
                + ")  end value = " + value);
        return value;
    }

    /**
     * set focus window
     */
    public static int setFocusWindow(int nMainWindow) {
        LogHelper.d(TAG, "setFocusWindow(int nMainWindow = " + nMainWindow
                + ")  begin");

        int value = getSourceManager().setFocusWindow(nMainWindow);

        LogHelper.d(TAG, "setFocusWindow(int nMainWindow = " + nMainWindow
                + ")  end value = " + value);
        return value;
    }

    public static boolean isDTVSource(int source){
        if(source == EnumSourceIndex.SOURCE_DTMB
                || source == EnumSourceIndex.SOURCE_DVBC
                || source == EnumSourceIndex.SOURCE_DVBT
                || source == EnumSourceIndex.SOURCE_ATSC
                || source == EnumSourceIndex.SOURCE_ISDBT
                || source == EnumSourceIndex.SOURCE_DVBS) {
            return true;
        }else{
            return false;
        }
    }

    public static boolean isTVSource(int source){
        if(isDTVSource(source) || source == EnumSourceIndex.SOURCE_ATV){
            return true;
        }else{
            return false;
        }
    }

    /**
     * setBootSource
     */
    public static int setBootSource(int srcId) {
        LogHelper.d(TAG, "setBootSource(int srcId = " + srcId + ")  begin");

        int value = getSourceManager().setBootSource(srcId);

        LogHelper.d(TAG, "setBootSource(int srcId = " + srcId + ")  end value = " + value);
        return value;
    }

    /**
     * getBootSource
     */
    public static int getBootSource() {
        LogHelper.d(TAG, "getBootSource()  begin");

        int value = getSourceManager().getBootSource();

        LogHelper.d(TAG, "getBootSource()  end value = " + value);
        return value;
    }

    /**
    * setSourceHolder
    */
    public static int setSourceHolder() {
        int value = getTvSourceManager().setSourceHolder("HiTvLauncher");
        return value;
    }
}
