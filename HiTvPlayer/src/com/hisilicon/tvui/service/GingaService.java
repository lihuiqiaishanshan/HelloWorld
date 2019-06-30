package com.hisilicon.tvui.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hisilicon.tvui.aidl.GingaInterface;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.play.MainActivity;

import java.util.List;

/**
 * Created by cWX657041 on 2019/3/11.
 */

public class GingaService extends Service {
    private static final String TAG = "GingaService";
    private MainActivity mainActivity;

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "GingaService onBind");
        mainActivity = ((DTVApplication) getApplication()).getMainActivity();
        return gingaBinder;
    }

    IBinder gingaBinder = new GingaInterface.Stub() {

        @Override
        public void open() throws RemoteException {
            Log.e(TAG, "ginga open");
            if (null != mainActivity) {
                mainActivity.openGinga(true);
            }
        }

        @Override
        public void close() throws RemoteException {
            Log.e(TAG, "ginga close");
            if (null != mainActivity) {
                mainActivity.openGinga(false);
            }
        }

        @Override
        public List<String> getAppList() throws RemoteException {
            Log.e(TAG, "ginga getAppList");
            if (null != mainActivity) {
                return mainActivity.getAppList();
            }
            return null;
        }

        @Override
        public void startApp(String appName) throws RemoteException {
            Log.e(TAG, "ginga startApp");
            if (null != mainActivity) {
                mainActivity.startApp(appName);
            }
        }
    };
}
