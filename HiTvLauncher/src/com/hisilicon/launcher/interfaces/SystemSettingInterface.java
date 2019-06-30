
package com.hisilicon.launcher.interfaces;

import java.util.ArrayList;

import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.SystemSetting;
import com.hisilicon.android.tvapi.vo.Wakeup;
import com.hisilicon.launcher.util.Constant;

/**
 * the interface of system setting
 *
 * @author huyq
 */
public class SystemSettingInterface {

    private static final String TAG = "SystemSettingInterface";

    /**
     * get instance of SystemSetting
     *
     * @return
     */
    public static SystemSetting getSystemSettingManager() {
        return HitvManager.getInstance().getSystemSetting();
    }

    /**
     * restore default
     */
    public static int restoreDefault() {
        LogHelper.d(TAG, "restoreDefault() begin");

        int value = getSystemSettingManager().restoreDefault();

        LogHelper.d(TAG, "restoreDefault() end value = " + value);
        return value;
    }

    /**
     * getWakeup
     */
    public static Wakeup getWakeup() {
        LogHelper.d(TAG, "getWakeup() begin");

        Wakeup value = getSystemSettingManager().getWakeup();

        LogHelper.d(TAG, "getWakeup() end value = " + value);
        return value;
    }

    /**
     * setBlueScreen
     */
    public static void setBlueScreen() {
        LogHelper.d(TAG, "setBlueScreen() begin");

        boolean value = getSystemSettingManager().isScreenBlueEnable();
        getSystemSettingManager().enableScreenBlue(value);

        LogHelper.d(TAG, "setBlueScreen() end value = " + value);
    }

    //get Current Lock
    public static ArrayList<Integer> isCurrentLocked()
    {
        return getSystemSettingManager().isCurrentLocked();
    }

    //All Lock Status
    public static boolean getLockEnable(int lockSwitchType)
    {
        return getSystemSettingManager().getLockEnable(lockSwitchType);
    }

    //All Pwd Status
    public static boolean getPwdStatus(int lockType)
    {
        return getSystemSettingManager().getPwdStatus(lockType);
    }

    //Source Lock
    public static boolean getSrcLockEnable(int source)
    {
        return getSystemSettingManager().getSrcLockEnable(source);
    }

    //Parent Lock
    public static ArrayList<Integer> getParentLock(int system)
    {
        return getSystemSettingManager().getParentLock(system);
    }

    public static boolean getParentUnRating()
    {
        return getSystemSettingManager().getParentUnRating();
    }
}
