
package com.hisilicon.launcher.util;

import com.hisilicon.launcher.util.Util;

import android.os.Handler;
import android.os.Message;
import android.content.pm.IPackageDataObserver;

// Clear data of user
public class ClearUserDataObserver extends IPackageDataObserver.Stub {
    private Handler mHandler;

    public ClearUserDataObserver(Handler handler) {
        mHandler = handler;
    }

    public void onRemoveCompleted(final String packageName,
            final boolean succeeded) {
        final Message msg = mHandler.obtainMessage();
        if (succeeded) {
            msg.what = Util.CLEAR_USER_DATA;
        } else {
            msg.what = Util.NOT_CLEAR_USER_DATA;
        }
        mHandler.sendMessage(msg);
    }
}
