package com.hisilicon.android.videoplayer.utils;


import java.lang.reflect.Method;

/**
 */

public class SystemProperties {

    public static String get(String key) {
        return getProperty(key, "unknown");
    }

    public static String get(String key, String defaultValue) {
        return getProperty(key, defaultValue);
    }

    public static void set(String key, String valude) {
        android.os.SystemProperties.set(key, valude);
//        try {
//            Class<?> c = Class.forName("android.os.SystemProperties");
//            Method set = c.getMethod("set", String.class, String.class);
//            set.invoke(c, key, valude);
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//        }
    }

    public static String getProperty(String key, String defaultValue) {
        return android.os.SystemProperties.get(key, defaultValue);
//        String value = defaultValue;
//        try {
//            Class<?> c = Class.forName("android.os.SystemProperties");
//            Method get = c.getMethod("get", String.class, String.class);
//            value = (String) (get.invoke(c, key, "unknown"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            return value;
//        }
    }

    public static int getInt(String key, int defaultValue) {
        return android.os.SystemProperties.getInt(key, defaultValue);
//        int value = defaultValue;
//        try {
//            Class<?> c = Class.forName("android.os.SystemProperties");
//            Method get = c.getMethod("getInt", String.class, int.class);
//            value = ReflectUtils.getIntResult(get.invoke(c, key, defaultValue));
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            return value;
//        }
    }
}
