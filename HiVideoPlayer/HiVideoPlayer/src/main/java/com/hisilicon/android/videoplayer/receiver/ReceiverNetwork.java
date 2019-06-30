package com.hisilicon.android.videoplayer.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

//import com.hisilicon.android.videoplayer.activity.activity.ActivityVideo;
import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.base.FrameActivity;
import com.hisilicon.android.videoplayer.utils.LogTool;

public class ReceiverNetwork extends BroadcastReceiver {
    private Activity mActivity;

    public ReceiverNetwork(Activity pActivity) {
        mActivity = pActivity;
    }

    @Override
    public void onReceive(Context pContext, Intent pIntent) {
        boolean _IsConnect = true;
        final String _Action = pIntent.getAction();

        LogTool.d(_Action);

        if (_Action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager _ConnectivityManager =
                    (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo _Info = _ConnectivityManager.getActiveNetworkInfo();
            _IsConnect = (_Info != null) && _Info.isConnected();
        }

        if (!_IsConnect) {
            doNetworkInterrupt();
        }
    }

    private void doNetworkInterrupt() {
    }

    //return true if network is connected
    public static boolean isNetConnected(Context pContext) {
        ConnectivityManager _ConnectivityManager =
                (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo _Info = _ConnectivityManager.getActiveNetworkInfo();
        return (_Info != null) && _Info.isConnected();
    }
}
