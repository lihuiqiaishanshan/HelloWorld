package com.hisilicon.explorer.utils;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class SystemProperties {

    private static final String TAG = SystemProperties.class.getSimpleName();

    public static String get(String key) {
        String invoke = null;
        try {
            Class<?> mSystemProperties = Class.forName("android.os.SystemProperties");
            Method get = mSystemProperties.getDeclaredMethod("get", String.class);
            invoke = (String) get.invoke(mSystemProperties, key);
        } catch (ClassNotFoundException e) {
            Log.e(TAG,"ClassNotFoundException !");
        } catch (NoSuchMethodException e) {
            Log.e(TAG,"NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.e(TAG,"InvocationTargetException !");
        } catch (IllegalAccessException e) {
            Log.e(TAG,"IllegalAccessException !");
        }
        return invoke;
    }

    public static String get(String key,String def) {
        String invoke = null;
        try {
            Class<?> mSystemProperties = Class.forName("android.os.SystemProperties");
            Method get = mSystemProperties.getDeclaredMethod("get", String.class,String.class);
            invoke = (String) get.invoke(mSystemProperties, key,def);
        } catch (ClassNotFoundException e) {
            Log.e(TAG,"ClassNotFoundException !");
        } catch (NoSuchMethodException e) {
            Log.e(TAG,"NoSuchMethodException !");
        } catch (InvocationTargetException e) {
            Log.e(TAG,"InvocationTargetException !");
        } catch (IllegalAccessException e) {
            Log.e(TAG,"IllegalAccessException !");
        }
        return invoke;
    }
}
