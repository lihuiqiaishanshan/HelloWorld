package com.hisilicon.android.videoplayer;

import android.app.Application;

import com.hisilicon.android.videoplayer.utils.CrashCatchHandler;

/**
 * Created on 2018/5/31.
 */

public class HiVideoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashCatchHandler crashCatchHandler = CrashCatchHandler.getInstance();//获得单例
        crashCatchHandler.init(getApplicationContext());//初始化,传入context
    }
}
