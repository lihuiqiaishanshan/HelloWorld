package com.hisilicon.tvui.util;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

public class PlayerProviderObserver extends ContentObserver{

    private Handler mHandler;
    private int messageWhat = 0;

    public PlayerProviderObserver(Context context,Handler handler,int msgWhat) {
        super(handler);
        mHandler = handler;
        messageWhat = msgWhat;
    }

    @Override
    public void onChange(boolean selfChange) {
        mHandler.sendEmptyMessage(messageWhat);
        super.onChange(selfChange);
    }
}
