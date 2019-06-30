
package com.hisilicon.tvui.interfaces;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.SystemSetting;

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

    // All Lock Status
    public static boolean getLockEnable(int lockSwitchType) {
        return getSystemSettingManager().getLockEnable(lockSwitchType);
    }
}
