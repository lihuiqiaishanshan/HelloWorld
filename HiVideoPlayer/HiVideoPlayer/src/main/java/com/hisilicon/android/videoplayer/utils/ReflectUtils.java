package com.hisilicon.android.videoplayer.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 */

public class ReflectUtils {
    private static final String TAG = ReflectUtils.class.getSimpleName();
    public static Method getMethod(Object obj ,String name, Class<?>... parameterTypes) {

        Method method = null;
        try {
            method = obj.getClass().getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            LogTool.e(TAG,"get Method error !");
        }
        return method;
    }

    public static Object invoke(Method method, Object o, Object... args) {
        if (method == null || o == null) {
            return null;
        }
        Object result = null;
        try {
            result = method.invoke(o, args);
        } catch (IllegalAccessException e) {
            LogTool.e(TAG,"invoke Method error !");
        }catch ( InvocationTargetException e) {
            LogTool.e(TAG,"invoke Method error !");
        }
        return result;
    }

    public static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(c, key, "unknown" ));
        } catch (Exception e) {
            LogTool.e(e.toString());
        }finally {
            return value;
        }
    }

    public int getSystemInt(String key, int defaultValue) {
        int value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("getInt", String.class, Integer.class);
            Object o = get.invoke(c, key, "unknown");
            value = getIntResult(o);
        } catch (Exception e) {
            LogTool.e(e.toString());
        }finally {
            return value;
        }
    }

    public static int getIntResult(Object result) {
        if (result==null){
            return -1;
        }
        if (result instanceof Integer){
            Integer integer = (Integer) result;
            return integer.intValue();
        }

        return -1 ;
    }

}
