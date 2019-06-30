
package com.hisilicon.launcher;

import java.util.List;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.util.Constant;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    // sharing of data,application list
    private List<ResolveInfo> mResolveInfos = null;
    private static Context mContext;
    private Handler mhandler;
   private int curScreenId;

    public void setCurScreenId(int curScreenId) {
        this.curScreenId = curScreenId;
    }

    public int getCurScreenId() {
        return curScreenId;
    }

    // access to shared data
    public List<ResolveInfo> getResolveInfos() {
        return mResolveInfos;
    }

    public void setResolveInfos(List<ResolveInfo> resolveInfos) {
        clearList();
        this.mResolveInfos = resolveInfos;
        mhandler.sendEmptyMessage(MyAppActivity.UPDATE_VIEW);
    }

    public void setHandler(Handler handler) {
        this.mhandler = handler;
    }

    /**
     * clear all data
     */
    public void clearList() {
        if (null != mResolveInfos && mResolveInfos.size() > 0) {
            mResolveInfos.clear();
            mResolveInfos = null;
        }
    }

    public void onCreate() {
        super.onCreate();
        LogHelper.i(TAG, "===== onCreate =====");
        MyApplication.mContext = getApplicationContext();
        /*
         * try { startOtherService(); } catch (Exception e) {
         * e.printStackTrace(); }
         */
    }

    @Override
    public void onLowMemory() {
        LogHelper.d(TAG, "onLowMemory");
        super.onLowMemory();
    }

    public static Context getAppContext() {
        return MyApplication.mContext;
    }
}
