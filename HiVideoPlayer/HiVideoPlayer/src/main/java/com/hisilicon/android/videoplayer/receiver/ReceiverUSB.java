package com.hisilicon.android.videoplayer.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

//import com.hisilicon.android.videoplayer.activity.activity.ActivityVideo;
import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.base.FrameActivity;
import com.hisilicon.android.videoplayer.utils.LogTool;

public class ReceiverUSB extends BroadcastReceiver {
    private Activity mActivity;

    public ReceiverUSB(Activity pActivity) {
        mActivity = pActivity;
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {
        final String _Action = pIntent.getAction();

        if (_Action.equals(Intent.ACTION_MEDIA_REMOVED)
                || _Action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            doDeviceRemoved();
        }
    }

    private void doDeviceRemoved() {
    }
}
