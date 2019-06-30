package com.hisilicon.launcher.interfaces;

import com.hisilicon.launcher.util.LogHelper;
import com.hisilicon.android.tvapi.HdmiCEC;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumCecMenu;
import com.hisilicon.android.tvapi.constant.EnumCecUICommand;
import com.hisilicon.android.tvapi.impl.HdmiCECImpl;
import com.hisilicon.android.tvapi.vo.HdmiCecDeviceInfo;
import com.hisilicon.launcher.util.Constant;

public class CECInterface {
    private static final String TAG = "CECInterface";

    public static HdmiCEC getCECManager() {
        return HitvManager.getInstance().getHdmiCEC();
    }

    /**
     * enableCEC
     */
    public static int enableCEC(boolean onOff) {
        LogHelper.d(TAG, "enableCEC(boolean onOff = " + onOff + ") begin");

        int value = getCECManager().setUIStatus(
                EnumCecMenu.CEC_MENU_ENABLE, onOff);

        LogHelper.d(TAG, "enableCEC(boolean onOff = " + onOff + ") end value = "
                + value);
        return value;
    }

    /**
     * isCECEnable
     */
    public static boolean isCECEnable() {
        LogHelper.d(TAG, "isCECEnable() begin");

        boolean value = getCECManager().getUIStatus(
                EnumCecMenu.CEC_MENU_ENABLE);

        LogHelper.d(TAG, "isCECEnable() end value = " + value);
        return value;
    }

    /**
     * isCECDevMenuCtrlEnable
     */
    public static boolean isCECDevMenuCtrlEnable() {
        LogHelper.d(TAG, "isCECDevMenuCtrlEnable() begin");

        boolean value = getCECManager().getUIStatus(
                EnumCecMenu.CEC_MENU_DEVMENUCTRL);

        LogHelper.d(TAG, "isCECDevMenuCtrlEnable() end value = " + value);
        return value;
    }

    /**
     * setMenuLag
     */
    public static int setMenuLag(int menuLanguage){
        int value = 0;
        if(isCECEnable() && isCECDevMenuCtrlEnable()){
            value = getCECManager().setMenuLag(menuLanguage);
        }
        LogHelper.d(TAG, "menuLanguage = "+menuLanguage + " value ="+value);
        return value;
    }

}
