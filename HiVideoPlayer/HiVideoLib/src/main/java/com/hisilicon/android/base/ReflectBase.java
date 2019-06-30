package com.hisilicon.android.base;

import android.text.TextUtils;

import com.hisilicon.android.utils.ReflectLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 */

public abstract class ReflectBase {

    /**
     * 需要反射的类名（继承此类子类实现）
     *
     * @return
     */
    public abstract String clsName();

    protected Class reflectCls;
    protected Object reflectObj;

    public Class getReflectCls() {
        return reflectCls;
    }

    public void setReflectCls(Class reflectCls) {
        this.reflectCls = reflectCls;
    }

    public Object getReflectObj() {
        return reflectObj;
    }

    public void setReflectObj(Object reflectObj) {
        this.reflectObj = reflectObj;
    }

    public ReflectBase() {
        if (TextUtils.isEmpty(clsName())) {
            throw new NullPointerException("Class Name Required");
        }
        try {
            reflectCls = Class.forName(clsName());
            reflectObj = reflectCls.newInstance();
        } catch (ClassNotFoundException e) {
            ReflectLog.e_debug(this, e);
        } catch (InstantiationException e) {
            ReflectLog.e_debug(this, e);
        } catch (IllegalAccessException e) {
            ReflectLog.e_debug(this, e);
        }
    }


    protected Method getMethod(String name, Class<?>... parameterTypes) {
        if (this.reflectObj == null || this.reflectCls == null) {
            ReflectLog.d_debug(this, "this.reflectObj==null||this.reflectCls==null");
            return null;
        }
        Method method = null;
        try {
            method = this.reflectCls.getDeclaredMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            ReflectLog.e_debug(this, e);
        }
        return method;
    }

    protected Object invoke(Method method, Object o, Object... args) {
        if (method == null || o == null) {
            ReflectLog.d_debug(this, "method == null || o == null");
            return null;
        }
        Object result = null;
        try {
            result = method.invoke(o, args);
        } catch (IllegalAccessException e) {
            ReflectLog.e_debug(this, e);
        } catch (InvocationTargetException e) {
            ReflectLog.e_debug(this, e);
        }
        return result;
    }

    protected Object invokeInner(Method method, Object... args) {
        if (method == null || this.reflectObj == null) {
            ReflectLog.d_debug(this, "method == null || o == null");
            return null;
        }
        Object result = null;
        try {
            result = method.invoke(this.reflectObj, args);
        } catch (IllegalAccessException e) {
            ReflectLog.e_debug(this, e);
        } catch (InvocationTargetException e) {
            ReflectLog.e_debug(this, e);
        }
        return result;
    }

    protected int getIntResult(Object result) {
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
