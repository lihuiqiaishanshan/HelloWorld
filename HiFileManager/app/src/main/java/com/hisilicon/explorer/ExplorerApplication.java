package com.hisilicon.explorer;

import android.app.Application;
import android.os.Build;

import com.hisilicon.explorer.utils.MenuUtils;
import com.hisilicon.explorer.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 */

public class ExplorerApplication  extends Application{
    private List<HashMap<String, Object>> sambaList;
    @Override
    public void onCreate() {
        super.onCreate();
        MenuUtils.init(this);
        initConfig();
    }

    private void initConfig() {
        Config.getInstance().setmIsIPTV(Utils.isIptvEnable());
        Config.getInstance().setmIsHiDPT(Utils.isHiDPT());
        Config.getInstance().setIsDebug(true);
        Config.getInstance().setmThreadPollSize(10);
        Config.getInstance().setFileFilterType(MenuUtils.getFileFilterType());
        Config.getInstance().setFileShowType(MenuUtils.getFileShowType());
        Config.getInstance().setFileFilterType(MenuUtils.getFileFilterType());
    }

}
