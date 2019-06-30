package com.hisilicon.tvui.util;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.hisilicon.tvui.play.MainActivity;


public class AutoSleepSettingProviderObserver extends ContentObserver
{
    private Handler mHandler ;
    public AutoSleepSettingProviderObserver(Context context,Handler handler)
    {
        super(handler);
        mHandler = handler ;
    }


    @Override
    public void onChange(boolean selfChange)
    {
        LogTool.d(LogTool.MAUDIO, "SettingService.MSG_SETTINGS_CHANGE");
        mHandler.sendEmptyMessage(MainActivity.MSG_AUTOSLEEP_SETTINGS_CHANGE);
        super.onChange(selfChange);
    }
}
