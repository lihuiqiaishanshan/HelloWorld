package com.hisilicon.android.hinetshare;

import android.util.Log;
import java.io.File;

/**
 * JNI
*/
public class Jni
{
    public static final String HINETSHARE_JNI_PATH = "/system/lib/libhinetshare_jni.so";
    private static final String TAG = "JNI";

    public Jni() {
        if(existsAndLoad()) {
            sambaInit();
        }
    }

    public static boolean existsAndLoad() {
        File file = new File(HINETSHARE_JNI_PATH);
        if(file.exists()) {
            System.loadLibrary("hinetshare_jni");
            return true;
        } else {
            Log.d(TAG, "failed to find " + HINETSHARE_JNI_PATH);
        }
        return false;
    }

    public native int UImount(String address, String workpath,String mountpoint,String user,String password);
    public native int myUmount(String path);
    public native String getMountPoint(String path);
    public native String getPcName(String ip);
    public native int umountlist();
    private static native final int sambaInit();
}
