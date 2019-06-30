package com.hisilicon.android.videoplayer.observer;

import android.database.ContentObserver;
import android.os.Handler;

/**
 * Created on 2018/7/11.
 */

public class PlayerProviderObserver extends ContentObserver {

    private Handler handler;
    private int msgWhat = 0;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public PlayerProviderObserver(Handler handler, int what) {
        super(handler);
        this.handler = handler;
        this.msgWhat = what;
    }


    @Override
    public void onChange(boolean selfChange) {
        if (handler != null) {
            handler.sendEmptyMessage(this.msgWhat);
        }
        super.onChange(selfChange);
    }
}
