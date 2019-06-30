package android.os;

import com.hisilicon.android.utils.ReflectLog;

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
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method set = c.getMethod("set", String.class, String.class);
            set.invoke(c, key, valude);
        } catch (Exception e) {
        } finally {
        }
    }

    public static String getProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, "unknown"));
        } catch (Exception e) {
        } finally {
            return value;
        }
    }

    public static int getInt(String key, int defaultValue) {
        int value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("getInt", String.class, int.class);
            value = getIntResult(get.invoke(c, key, defaultValue));
        } catch (Exception e) {
        } finally {
            return value;
        }
    }

    private static int getIntResult(Object result) {
        if (result == null) {
            return -1;
        }
        if (result instanceof Integer) {
            Integer integer = (Integer) result;
            return integer.intValue();
        }

        return -1;
    }
}
