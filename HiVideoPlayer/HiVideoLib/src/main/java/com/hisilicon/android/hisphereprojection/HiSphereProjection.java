package com.hisilicon.android.hisphereprojection;


import com.hisilicon.android.base.ReflectBase;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 */

public class HiSphereProjection extends ReflectBase {
    private static final String CLS_NAME = "com.hisilicon.android.hisphereprojection.HiSphereProjection";

    @Override
    public String clsName() {
        return CLS_NAME;
    }

    public HiSphereProjection() {
        super();
    }

    public int getTextureID() {
        return getIntResult(invokeInner(getMethod("getTextureID")));
    }

    public void project(int id) {
        invokeInner(getMethod("project", Integer.class), id);
    }

    public void deleteTextureId(int id) {
        invokeInner(getMethod("deleteTextureId", Integer.class), id);
    }

    public void release() {
        invokeInner(getMethod("release"));

    }

    ///Class<?> cls = Class.forName("package.ClassName$InnerClass");
    public static class ViewPointParam extends ReflectBase {
        private static final String CLS_NAME_INNER = "com.hisilicon.android.hisphereprojection.HiSphereProjection$ViewPointParam";

        @Override
        public String clsName() {
            return CLS_NAME_INNER;
        }

        /**
         * parameter of doing Sphere Projection
         * CN:旋转投影参数结构体
         */
        public float fRollAdd;                //垂直方向旋转角度  -90 - 90  float类型
        public float fYawAdd;                 //水平方向旋转角度  -180 - 180  float类型
        public float fFovy;                   //视线范围          90 - 120 float类型
    }


    public void setViewRotation(ViewPointParam param) {
        try {
            setPar(param);
            invokeInner(getMethod("setViewRotation", param.getReflectCls()), param.getReflectObj());
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }
    }

    public void setViewScale(ViewPointParam param){
        try {
            setPar(param);
            invokeInner(getMethod("setViewScale", param.getReflectCls()), param.getReflectObj());
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

    }

    private void setPar(ViewPointParam param) throws NoSuchFieldException, IllegalAccessException {
        Field f;
        f = param.getReflectCls().getDeclaredField("fRollAdd");
        f.setAccessible(true);
        f.set(param.getReflectObj(), param.fRollAdd);
        f = param.getReflectCls().getDeclaredField("fYawAdd");
        f.setAccessible(true);
        f.set(param.getReflectObj(), param.fYawAdd);
        f = param.getReflectCls().getDeclaredField("fFovy");
        f.setAccessible(true);
        f.set(param.getReflectObj(), param.fFovy);
    }
}
