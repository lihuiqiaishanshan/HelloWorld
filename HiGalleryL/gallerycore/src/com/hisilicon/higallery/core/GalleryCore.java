
package com.hisilicon.higallery.core;

import android.graphics.Point;
import android.os.Looper;
import android.os.Parcel;
import android.view.Surface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.content.Context;

public abstract class GalleryCore {
    /**
     * 初始化完成时回调的命令，obj:boolean 初始化的结果
     *
     * @see Callback#onReceiveCMD
     */
    public static final int CMD_INIT_COMPLETED = 0;

    /**
     * 查看一张图片完成时回调的命令，obj:boolean 显示完成的结果
     *
     * @see Callback#onReceiveCMD
     */
    public static final int CMD_VIEW_COMPLETED = 1;
    public static final int CMD_SHOWN_FRAME_CHANGED = 2;
    public static final int CMD_HTTP_IMAGE = 3;
    public static final int CMD_DOWNLOADED_IMAGE = 4;
    public static final int CMD_IO_ERROR = 5;
    public static final int CMD_DECODING_ERROR = 6;
    public static final int CMD_NETWORK_DENIED_ERROR = 7;
    public static final int CMD_OUT_OF_MEMORY_ERROR = 8;
    public static final int CMD_CANCELED = 9;
    public static final int CMD_UNKNOWN_ERROR = 10;
    public static final int CMD_DECODE_OUT_OF_MEMORY_ERROR = 11;
    public static final int STB = 0;
    public static final int DPT = 1;

    /** 图片移动的方向 */
    public enum Direction {
        LEFT, UP, RIGHT, DOWN
    };

    /** 图片旋转角度 */
    public enum Rotation {
        ROTATION_0(0),
        ROTATION_90(90),
        ROTATION_180(180),
        ROTATION_270(270);

        int degree;

        private Rotation(int d) {
            degree = d;
        }
    }

    /** 幻灯片使用的动画类型 */
    public enum AnimType {

        /** 无动画 */
        ANIM_NONE(0),
        /** 缩放 */
        ANIM_SCALE(1),
        /** 滑动 */
        ANIM_SLIDE(2),
        /** 淡入淡出 */
        ANIM_FADE(3),
        /** 随机 */
        ANIM_RANDOM(4);

        int type;

        private AnimType(int t) {
            type = t;
        }
    }

    public enum ViewMode {
        ORIGINAL_MODE(0),
        FULLSCREEN_MODE(1),
        AUTO_MODE(2),
        SCALE_MODE(3);

        int mode;

        private ViewMode(int m) {
            mode = m;
        }
    }

    /**
     * 接收底层回调回来的命令
     */
    public interface Callback {
        /**
         * @param cmd 底层回调回来的命令
         * @param obj 与命令相关的状态
         * @see GalleryCore#CMD_INIT_COMPLETED
         * @see GalleryCore#CMD_VIEW_COMPLETED
         */
        public void onReceiveCMD(int cmd, Object obj);
    }

    /**
     * 接收底层回调回来的命令,包含图片Url
     */
    public interface CallbackWithUrl {
        /**
         * @param cmd 底层回调回来的命令
         * @param parcel 包含图片显示状态、图片url
         * @see GalleryCore#CMD_INIT_COMPLETED
         * @see GalleryCore#CMD_VIEW_COMPLETED
         */
        public void onReceiveCMDWithUrl(int cmd, Parcel parcel);
    }
    public interface Sliding{
        public void showNext(long delay);
        public void setIsSliding(boolean isSliding);
    }

    public interface SlidingShow {
        void startSlidingShow();
        void showMenu();
    }

    static GalleryImpl sGalleryCore;
    /** 获得GalleryCore的实例 */
    public static GalleryCore getGallery(Looper looper, Context context) {
        if (sGalleryCore == null) {
            sGalleryCore = new GalleryImpl(looper, context);
        } else {
            sGalleryCore.setLooper(looper);
        }
        return sGalleryCore;
    }

    /**
     * 设置底层状态回调的接收
     *
     * @param callback
     * @see Callback
     */
    public abstract void setCallback(Callback callback);
    /**
     * 设置底层状态回调的接收，并带图片url
     *
     * @param callback
     * @see CallbackWithUrl
     */
    public abstract void setCallbackWithUrl(CallbackWithUrl callback);

    /**
     * 初始化GalleryCore，一般在 {@link android.app.Activity#onCreate} 里调用
     *
     * @param width 视频层的宽度
     * @param height 视频层的高度
     */
    public abstract void init(int videoLayerWidth, int videoLayerHeight);

    /**
     * 初始化GalleryCore，一般在 {@link android.app.Activity#onCreate} 里调用
     *
     * @param width 视频层的宽度
     * @param height 视频层的高度
     * @param maxUsedMemSize 解码可以使用的内存大小，单位M
     */
    public abstract void init(int videoLayerWidth, int videoLayerHeight, int maxUsedMemSize);

    /**
     * 反初始化GalleryCore， 释放内部资源一般在 {@link android.app.Activity#onStop} 里调用
     *
     * @return 反初始化结果
     */
    public abstract boolean deinit();

    /**
     * 设置动画开关
     *
     * @param enable true 打开动画；false 关闭动画
     * @return 动画设置的结果
     */
    public abstract void enableAnimation(boolean enable);

    /**
     * 查看图片 图片显示完成时 {@link Callback#onReceiveCMD} 会收到
     * {@link #CMD_INIT_COMPLETED} 命令
     * 默认为全屏显示
     *
     * @param path 图片所在目录
     */
    public abstract void viewImage(String path);

    /**
     * 查看图片 图片显示完成时 {@link Callback#onReceiveCMD} 会收到
     * {@link #CMD_INIT_COMPLETED} 命令
     *
     * @param path 图片所在目录
     * @param fullScreen, true 图片全屏显示， false 图片按原始尺寸显示
     */
    public abstract void viewImage(String path, boolean fullScreen);

    public abstract void viewImage(final String path, final ViewMode viewmode);

    public abstract void viewImage(final String path, final ViewMode viewmode, final int rotateDegree);

    /**
     * 查看图片 图片显示完成时 {@link Callback#onReceiveCMD} 会收到
     * {@link #CMD_INIT_COMPLETED} 命令
     *
     * @param path 图片所在目录
     * @param scale 图片显示时的拉伸比例
     */
    public abstract void viewImage(String path, float scale);

    /**
     * 获得当前显示的图片的真实尺寸
     *
     * @param size 保存图片的尺寸
     */
    public abstract void getImageSize(Point size);

    /**
     * 放大当前图片
     *
     * @return true 图片被放大， false 图片不能继续放大
     */
    public abstract boolean zoomIn();

    /**
     * 缩小当前图片
     *
     * @return true 图片被缩小， false 图片不能继续缩小
     */
    public abstract boolean zoomOut();

    /**
     * 放大、缩小当前图片
     *
     * @param scale 缩放的倍数（0.125 ～ 0.8 之间）
     * @return true 缩放成功， 缩放失败
     */
    public abstract boolean zoom(float scale);

    /**
     * 朝指定方向移动当前图片
     *
     * @param r 图片移动的方向
     * @param step 移动的大小（像素）
     * @return true 图片移动成功，false 图片不能像该方向移动
     * @see Direction
     */
    public abstract boolean move(Direction r, int step);

    /**
     * 旋转当前图片
     *
     * @param r 旋转的角度
     * @return true 旋转成功，false 旋转失败
     * @see Rotation
     */
    public abstract boolean rotate(Rotation r);

    /**
     * 重置所有变换
     *
     * @return true 重置成功，false 重置失败
     */
    public abstract boolean reset();

    /**
     * 重置缩放等级
     *
     * @return true 重置成功，false 重置失败
     */
    public abstract boolean resetScaleLevel();

    /**
     * 启动幻灯片浏览模式
     *
     * @param a 幻灯片切换的动画
     * @param interval 幻灯片切换的间隔（秒）
     * @return true 幻灯片模式已经启动
     * @see AnimType
     */
    public abstract boolean startSliding(Sliding s, AnimType a, long interval);

    public abstract boolean startSliding(Sliding s, AnimType a, AnimType[] randomSeeds, long interval);

    /**
     * 停止幻灯片浏览模式
     *
     * @return true 幻灯片模式已经停止
     */
    public abstract boolean stopSliding();

    public abstract void enablePQ(boolean enable);

    // Maybe remove
    public abstract void initWithSurface(Surface surface, int width, int height);

    public abstract void setFailBitmap(Bitmap bitmap);
    public abstract void setAnimationType(GalleryCore.AnimType type, int duration);
    public abstract String getCurrentPath();
    public abstract void getDisplaySize(Point size);
    public abstract int getFormat();
    public abstract boolean decodeSizeEvaluate(String path, int width, int height, int sampleSize ,int size);
    public abstract Rect getShownFrame();
    public abstract int getBitmapOrientation(String path);
    public abstract int getDecode();
}
