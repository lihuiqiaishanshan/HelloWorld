package com.hisilicon.explorer.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hisilicon.android.hinetshare.Jni;
import com.hisilicon.android.hinetshare.SambaTreeNative;


public class SambaService extends Service {

    private SambaTreeNative sambaTreeNative;
    private Jni smbClient;

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
    }

    private void initData() {
        sambaTreeNative = new SambaTreeNative();
        smbClient = new Jni();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    ISambaAidlService.Stub mBinder = new ISambaAidlService.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public String getWorkgroups() throws RemoteException {
            return sambaTreeNative.getWorkgroups();
        }

        @Override
        public boolean UImount(String address, String workpath, String mountpoint, String user, String password) throws RemoteException {
            int i = smbClient.UImount(address, workpath, mountpoint, user, password);
            if (i == 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public boolean myUmount(String path) throws RemoteException {
            int i = smbClient.myUmount(path);
            if (i == 0) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public String getMountPoint(String path) throws RemoteException {
            return smbClient.getMountPoint(path);
        }

        @Override
        public String getDetailsBy(String ip, String account, String pwd) {
            return sambaTreeNative.getDetailsBy(ip, account, pwd);
        }
    };
}
