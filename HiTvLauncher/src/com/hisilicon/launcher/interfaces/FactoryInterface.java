package com.hisilicon.launcher.interfaces;

import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.android.tvapi.Factory;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.launcher.util.Constant;

public class FactoryInterface {
    private static final String TAG = "FactoryInterface";

    public static Factory getFactoryManager() {
        return HitvManager.getInstance().getFactory();
    }

    /**
     * set the PowerMusic switch
     */
    public static int enablePowerMusic(boolean onoff) {
        LogHelper.d(TAG, "enablePowerMusic(boolean onoff = " + onoff + ") begin");

        int value = getFactoryManager().enablePowerMusic(onoff);

        LogHelper.d(TAG, "enablePowerMusic(boolean onoff = " + onoff
                + ") end value = " + value);
        return value;
    }

    /**
     * gets a PowerMusic switch
     */
    public static boolean isPowerMusicEnable() {
        LogHelper.d(TAG, "isPowerMusicEnable() begin");

        boolean value = getFactoryManager().isPowerMusicEnable();

        LogHelper.d(TAG, "isPowerMusicEnable() end value = " + value);
        return value;
    }

    /**
     * set the PowerMusic number
     */
    public static int setPowerMusicNo(int no) {
        LogHelper.d(TAG, "setPowerMusicNo(int no = " + no + ") begin");

        int value = getFactoryManager().setPowerMusicNo(no);

        LogHelper.d(TAG, "setPowerMusicNo(int no = " + no
                + ") end value = " + value);
        return value;
    }

    /**
     * gets a PowerMusic number
     */
    public static int getPowerMusicNo() {
        LogHelper.d(TAG, "getPowerMusicNo() begin");

        int value = getFactoryManager().getPowerMusicNo();

        LogHelper.d(TAG, "getPowerMusicNo() end value = " + value);
        return value;
    }
}
