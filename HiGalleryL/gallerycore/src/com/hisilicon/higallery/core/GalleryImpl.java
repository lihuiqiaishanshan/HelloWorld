
package com.hisilicon.higallery.core;

import java.util.Random;
import java.util.HashSet;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Movie;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;
import android.view.*;
import android.net.*;
import android.content.Context;
import java.io.File;
import com.hisilicon.higallery.core.DecodeThread.DecodeListener;
import com.download.DownLoadTask;
import com.download.DownLoadListener;
import java.io.IOException;
import android.os.SystemProperties;

class GalleryImpl extends GalleryCore {

    static {
        System.loadLibrary("gallerycore");
    }

    static final String TAG = "gallerycore";
    static final int ANIM_DURATION = 2000;
    static final int cacheFileCount = 1;

    static final int TYPE_NORMAL_k = 0;
    static final int TYPE_NORMAL_2k = 1;
    static final int TYPE_NORMAL_S4k = 2;
    static final int TYPE_NORMAL_4k = 3;
    static final int TYPE_NORMAL_8k = 4;

    private static final float MAX_SCALE = 8f;
    private static final float MIN_SCALE = 0.125f;
    private float mScaleLevel = 1;

    private static final int CALL_UNF_VO_CREATEWINDOW_FAILED = 1;
    private static final int CALL_UNF_VO_ATTACHWINDOW_FAILED = 2;
    private static final int CALL_UNF_VO_SETTIMING_FAILED = 3;
    private static final int CALL_UNF_VO_SETWINDOWENABLE_FAILED = 4;
    private static final int CALL_SYS_INIT_FAILED = 5;
    private static final int CALL_UNF_VO_DETACHWINDOW_FAILED = 6;
    private static final int CALL_UNF_VO_DESTROYWINDOW_FAILED = 8;
    private static final int CALL_UNF_DISP_INIT_FAILED = 9;
    private static final int CALL_UNF_DISP_ATTACHINTF_FAILED = 10;
    private static final int CALL_UNF_DISP_OPEN_FAILED = 11;
    private static final int CALL_UNF_VO_INIT_FAILED = 12;
    private int mDisplayWidth;
    private int mDisplayHeight;
    private int mGraphicLayerWidth;
    private int mGraphicLayerHeight;
    private Rect mShownFrame = new Rect();
    private boolean mInit = false;


    private static final int MOVE_STEP = 30;

    int mUserAnimType = AnimType.ANIM_NONE.type;
    int mUserAnimDuration = 0;

    boolean mEnablePQ = false;

    boolean mAnimationEnabled;
    Handler mGLHandler;
    GLThread mGLThread;
    Handler mMainHandler;
    Context mContext;

    Bitmap mCurrentBmp;
    Bitmap mFailBitmap;
    String mCurrentFile;

    HandlerThread mHandlerThread;
    DecodeHander mDecodeHandler;

    Bitmap mFalureBitmap;
    long lastViewTime;
    long initBeginTime;

    HashSet<String> mToShow = new HashSet<String>();
    HashMap<String, String> mShown = new HashMap<String, String>();
    String mCurrentShow = null;

    AnimType mAnimType;
    AnimType[] mAnimRandomSeeds;
    long mSlidingInterval;
    boolean mIsSliding = false;
    Sliding mSliding;
    Runnable mSlidingTask = new Runnable() {

        @Override
        public void run() {
            if (mAnimType == AnimType.ANIM_RANDOM) {
                if (mAnimRandomSeeds != null) {
                    int animTypeIndex = new Random().nextInt(mAnimRandomSeeds.length);
                    AnimType a = mAnimRandomSeeds[animTypeIndex];
                    if (a == AnimType.ANIM_RANDOM)
                        a = AnimType.ANIM_NONE;
                    nativeSetAnimationType(a.type, ANIM_DURATION);
                } else {
                    int animType = new Random().nextInt(3) + 1;
                    nativeSetAnimationType(animType, ANIM_DURATION);
                }
            } else {
                nativeSetAnimationType(mAnimType.type, ANIM_DURATION);
            }
            if (mSliding != null)
                mSliding.showNext(mSlidingInterval);
        }
    };

    public GalleryImpl(Looper mainLooper, Context context) {
        mContext = context;
        WindowManager mWM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        mWM.getDefaultDisplay().getSize(size);
        mGraphicLayerWidth = size.x;
        mGraphicLayerHeight = size.y;
        Log.i(TAG, "mGraphicLayerSize: " + size);
        mGLThread = new GLThread("GLThread");
        mGLThread.start();
        setLooper(mainLooper);
    }

    protected void getHttpImage(String uri, final ViewMode mode, Context context)
    {
        DownLoadTask downLoadTask = new DownLoadTask(new DownLoadListener() {
            @Override
            public void onLoadSuccess(String filePath) {

                Message msg = mMainHandler.obtainMessage(CMD_DOWNLOADED_IMAGE);
                msg.arg1 = mode.mode;
                msg.obj = filePath;
                if(mToShow.contains(filePath))
                {
                     mShown.put(filePath, filePath);
                     mMainHandler.sendMessage(msg);
                }
                else
                {
                     showCompleted(false, filePath, false, CMD_CANCELED);
                     return;
                }
            }
            @Override
            public void onLoadFailed(String filePath, Exception e) {
               showToast(e.toString());
            }
        },context);
        downLoadTask.execute(uri);
    }

    public void setLooper(Looper mainLooper) {
        mMainHandler = new Handler(mainLooper) {

            @Override
            public void handleMessage(Message msg) {
                ViewMode mode = ViewMode.AUTO_MODE;
                switch(msg.what){
                case CMD_INIT_COMPLETED:
                case CMD_VIEW_COMPLETED:
                    if (mCallback != null) {
                        mCallback.onReceiveCMD(msg.what, (msg.arg1==1));
                    }
                    if (mCallbackWithUrl != null){
                        Parcel parcel = Parcel.obtain();
                        parcel.writeInt(msg.arg1);
                        if(CMD_VIEW_COMPLETED == msg.what && null != msg.obj && mShown.containsKey((String)msg.obj))//for http downloaded file
                        {
                            parcel.writeString(mShown.get((String)msg.obj));
                            mShown.remove((String)msg.obj);
                        }
                        else
                        {
                            if(null == msg.obj)
                            {
                                parcel.writeString("");
                            }
                            else
                            {
                                parcel.writeString((String)msg.obj);
                            }
                        }
                        parcel.writeInt(msg.arg2);
                        mCallbackWithUrl.onReceiveCMDWithUrl(msg.what, parcel);
                        parcel.recycle();
                    }
                    break;
                case CMD_SHOWN_FRAME_CHANGED:
                    if (mCallback != null) {
                        mCallback.onReceiveCMD(msg.what, msg.obj);
                    }
                    break;
                case CMD_HTTP_IMAGE:
                    mode.mode = msg.arg1;
                    getHttpImage((String)msg.obj, mode, mContext);
                    break;
                case CMD_DOWNLOADED_IMAGE:
                    String filePath = (String)msg.obj;
                    mode.mode = msg.arg1;
                    viewImage(filePath, mode);
                    break;
                default:
                    if (mCallback != null) {
                        mCallback.onReceiveCMD(msg.what, msg.obj);
                    }
                    break;
                }
            }
        };
    }

    protected Callback mCallback;
    protected CallbackWithUrl mCallbackWithUrl;

    public void setCallback(Callback callback) {
        mCallback = callback;
    }
    public void setCallbackWithUrl(CallbackWithUrl callback) {
        mCallbackWithUrl = callback;
    }

    @Override
    public void enablePQ(boolean enable) {
        mEnablePQ = enable;
    }

    @Override
    public void init(int videoLayerWidth, int videoLayerHeight) {
        init(videoLayerWidth, videoLayerHeight, 300);
    }

    @Override
    public void init(int videoLayerWidth, int videoLayerHeight, int maxUsedMemSize) {
        initBeginTime = System.currentTimeMillis();
        while (mGLHandler == null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                Log.e(TAG,"in run",e);
            }
        }

        mInit = true;

        Message msg = mGLHandler.obtainMessage(GLThread.GL_INIT, videoLayerWidth, videoLayerHeight);
        msg.obj = maxUsedMemSize * 1024 * 1024;
        mGLHandler.sendMessage(msg);
        if (mHandlerThread == null) {
            mHandlerThread = new HandlerThread("decode_thread");
            mHandlerThread.start();
        }
        if (mDecodeHandler == null) {
            mDecodeHandler = new DecodeHander(mHandlerThread.getLooper());
        }
        mDisplayWidth = videoLayerWidth;
        mDisplayHeight = videoLayerHeight;
    }

    @Override
    public boolean deinit() {
        mInit = false;
        if (mIsSliding) {
            stopSliding();
        }
        Message msg = mGLHandler.obtainMessage(GLThread.GL_DEINIT);
        mGLHandler.sendMessage(msg);
        if (mDecodeHandler != null) {
            mDecodeHandler.stop();
            mDecodeHandler = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.quit();
            mHandlerThread = null;
        }
        mCallback = null;
        mCallbackWithUrl = null;
        return true;
    }

    @Override
    public void enableAnimation(boolean enable) {
        mAnimationEnabled = enable;
    }

    @Override
    public void viewImage(final String path) {
        viewImage(path, true);
    }

    @Override
    public void viewImage(final String path, final boolean fullScreen) {
        viewImage(path, fullScreen ? ViewMode.FULLSCREEN_MODE : ViewMode.ORIGINAL_MODE);
    }

    public boolean viewImageCheck(final String path, final ViewMode viewmode) {
        Uri uri = Uri.parse(path);
        if(uri == null) {
            return true;
        }
        String scheme = uri.getScheme();
        Log.i(TAG, "path="+path+" uri="+uri+" scheme"+scheme);
        if(uri != null && scheme!=null && scheme.equals("http"))
        {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_HTTP_IMAGE, viewmode.mode, 0, path));
            return false;
        }

        return true;
    }

    public void viewImage(final String path, final ViewMode viewmode)
    {
        viewImage(path, viewmode, 0);
    }

    @Override
    public void viewImage(final String path, final ViewMode viewmode, final int rotateDegree) {
        if (!mInit) {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_VIEW_COMPLETED, 0, 0, path));
            return;
        }
        mCurrentShow = path;

        if(!viewImageCheck(path, viewmode))
        {
            return;
        }

        mCurrentFile = path;
        lastViewTime = System.currentTimeMillis();
        Log.d(TAG, "viewImage " + path);
        DecodeThread thread = new DecodeThread();
        thread.setListener(new DecodeListener() {
            @Override
            public void onStartDecode() {
                long time = SystemClock.currentThreadTimeMillis();
                showBitmap(path, viewmode.mode, rotateDegree);//use native decode
/*
                // Bitmap bitmap = BitmapDecodeUtils.getOrigionBitmap(path);
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                long duration = SystemClock.currentThreadTimeMillis() - time;
                Log.d(TAG, "decode pic duration = " + duration + "ms" + ", succ = "
                        + (bitmap != null));
                if (bitmap == null) {
                    showCompleted(false);
                    if (mFailBitmap != null)
                        showBitmap(mFailBitmap, fullScreen);
                } else {
                    showBitmap(bitmap, fullScreen);
                }
*/
            }
        });
        mDecodeHandler.postDecode(thread);
    }

    @Override
    public void viewImage(final String path, float scale) {
        viewImage(path, true);
    }

    @Override
    public void getImageSize(Point size) {
        // TODO Auto-generated method stub

    }

//    public void showBitmap(Bitmap bitmap, int viewmode) {
//        if (mCurrentBmp != null && mCurrentBmp != mFailBitmap)
//            mCurrentBmp.recycle();
//        mCurrentBmp = bitmap;
//        Message msg = mGLHandler.obtainMessage(GLThread.GL_SHOW_BITMAP);
//        msg.obj = bitmap;
//        msg.arg1 = viewmode;
//        mGLHandler.sendMessage(msg);
//    }

    public void showBitmap(final String path, int viewmode, int rotateDegree) {

        Message msg = mGLHandler.obtainMessage(GLThread.GL_SHOW_BITMAP);
        msg.obj = path;
        msg.arg1 = viewmode;
        msg.arg2 = rotateDegree;
        mGLHandler.sendMessage(msg);
    }

    @Override
    public boolean zoomIn() {
        if (mScaleLevel >= MAX_SCALE)
            return false;
        mScaleLevel *= 2;
        scale(2f, 2f);
        return true;
    }

    @Override
    public boolean zoomOut() {
        if (mScaleLevel <= MIN_SCALE)
            return false;
        mScaleLevel *= 0.5f;
        scale(0.5f, 0.5f);
        return true;
    }

    public boolean zoom(float scale) {
        if (scale == 1) {
            return false;
        } else if (scale > 1 && mScaleLevel >= MAX_SCALE) {
            return false;
        } else if (scale < 1 && mScaleLevel <= MIN_SCALE) {
            return false;
        }

        mScaleLevel *= scale;
        scale(scale, scale);
        if (mScaleLevel == 1 && scale < 1) {
            resetScaleLevel();
        }
        return true;
    }

    @Override
    public boolean move(Direction r, int step) {
        if (mScaleLevel <= 1)
            return false;
        switch (r) {
            case LEFT:
                if (mShownFrame.left >= 0)
                    return false;
                translate(step, 0);
                break;
            case RIGHT:
                if (mShownFrame.right <= mDisplayWidth)
                    return false;
                translate(-step, 0);
                break;
            case UP:
                if (mShownFrame.top >= 0)
                    return false;
                translate(0, step);
                break;
            case DOWN:
                if (mShownFrame.bottom <= mDisplayHeight)
                    return false;
                translate(0, -step);
                break;
        }
        return true;
    }

    public boolean scale(float scaleX, float scaleY) {
        Message msg = mGLHandler.obtainMessage(GLThread.GL_SCALE);
        Bundle data = msg.getData();
        data.putFloat("scaleX", scaleX);
        data.putFloat("scaleY", scaleY);
        mGLHandler.sendMessage(msg);
        return true;
    }

    public boolean translate(int tX, int tY) {
        Message msg = mGLHandler.obtainMessage(GLThread.GL_TRANSLATE);
        msg.arg1 = tX;
        msg.arg2 = tY;
        mGLHandler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean rotate(Rotation r) {
        Message msg = mGLHandler.obtainMessage(GLThread.GL_ROTATE);
        // msg.arg1 = r.degree;
        msg.arg1 = 360 - r.degree;
        mGLHandler.sendMessage(msg);
        return true;
    }

    public boolean setAlpha(float a) {
        if (a < 0 || a > 1) {
            Log.e(TAG, "alpha should be 0~1, not " + a);
            return false;
        }

        Message msg = mGLHandler.obtainMessage(GLThread.GL_SET_ALPHA);
        msg.obj = a;
        mGLHandler.sendMessage(msg);
        return true;
    }

    @Override
    public boolean reset() {
        Message msg = mGLHandler.obtainMessage(GLThread.GL_RESET);
        mGLHandler.sendMessage(msg);

        mScaleLevel = 1;
        return true;
    }

    public boolean resetScaleLevel() {
        mScaleLevel = 1;
        return true;
    }

    @Override
    public boolean startSliding(Sliding s, AnimType a, long interval) {
        return startSliding(s, a, null, interval);
    }

    public boolean startSliding(Sliding s, AnimType a, AnimType[] randomSeeds, long interval) {
        mAnimType = a;
        mAnimRandomSeeds = randomSeeds;
        mSlidingInterval = interval;
        mSliding = s;
        if(!mIsSliding){
            mMainHandler.postDelayed(mSlidingTask, mSlidingInterval);
        }
        mIsSliding = true;
        mSliding.setIsSliding(mIsSliding);
        return true;
    }

    @Override
    public boolean stopSliding() {
        mIsSliding = false;
        mSliding.setIsSliding(mIsSliding);
        mMainHandler.removeCallbacks(mSlidingTask);
        if(mUserAnimType != AnimType.ANIM_NONE.type && mUserAnimDuration > 0)
        {
            nativeSetAnimationType(mUserAnimType, mUserAnimDuration);
        }
        else
        {
            nativeSetAnimationType(AnimType.ANIM_NONE.type, ANIM_DURATION);
        }
        return true;
    }

    @Override
    public void setFailBitmap(Bitmap bitmap) {
        mFailBitmap = bitmap;
    }

    public void setAnimationType(GalleryCore.AnimType type, int duration)
    {
        mUserAnimType = type.type;
        mUserAnimDuration = duration;
        if(type == AnimType.ANIM_RANDOM){
            mUserAnimType = new Random().nextInt(4);
        }
        nativeSetAnimationType(mUserAnimType, duration);
    }

    @Override
    public boolean decodeSizeEvaluate(String path, int width, int height, int sampleSize ,int usedDecSize) {
        return nativeDecodeSizeEvaluate(path, width, height, sampleSize ,usedDecSize);
    }

    public void initCompleted(boolean result) {
        mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_INIT_COMPLETED, (result?1:0), 0));
        long duration = System.currentTimeMillis() - initBeginTime;
        Log.d(TAG, "Init timecost = " + duration + "ms, result = " + result);
    }

    public void showCompleted(boolean result) {
        showCompleted(result, null, false, -1);
    }

    public void showCompleted(boolean result, String url) {
        showCompleted(result, url, false, -1);
    }

    public void showCompleted(boolean result, String url, boolean hdr)
    {
        showCompleted(result, url, hdr, -1);
    }

    public void showCompleted(boolean result, String url, boolean hdr, int err) {
        if(result)
        {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_VIEW_COMPLETED, 1, (hdr?1:0), url));
        }
        else
        {
            mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_VIEW_COMPLETED, 0, err, url));
        }
        Runtime.getRuntime().gc();
        long duration = System.currentTimeMillis() - lastViewTime;
        Log.d(TAG, "View timecost = " + duration + "ms, result = " + result + " url=" + url + " hdr=" + hdr);

        if (mIsSliding)
            mMainHandler.postDelayed(mSlidingTask, mSlidingInterval);
    }

    public void shownFrameChanged(int left, int top, int right, int bottom) {
        mShownFrame.set(left, top, right, bottom);
        mMainHandler.sendMessage(mMainHandler.obtainMessage(CMD_SHOWN_FRAME_CHANGED, mScaleLevel));
    }

    public String getCurrentPath() {
        return mCurrentFile;
    }

    public int getFormat() {
        return nativeGetFormat();
    }

    public void getDisplaySize(Point size) {
        if (size == null)
            size = new Point();
        int type = nativeGetFormat();
        if(TYPE_NORMAL_k ==type){
            mDisplayWidth = 1280;
            mDisplayHeight = 720;
        }else if(TYPE_NORMAL_2k ==type){
            mDisplayWidth = 1920;
            mDisplayHeight =1080;
        }else if(TYPE_NORMAL_S4k ==type){
           mDisplayWidth = 3840;
           mDisplayHeight =2160;
        }else if(TYPE_NORMAL_4k ==type){
           mDisplayWidth = 4096;
           mDisplayHeight =2160;
        }else if(TYPE_NORMAL_8k ==type){
           mDisplayWidth = 7680;
           mDisplayHeight =4320;
        }

        size.x = mDisplayWidth;
        size.y = mDisplayHeight;
    }

    public Rect getShownFrame() {
        Rect r = new Rect(mShownFrame);
        return r;
    }

    @Override
    public void initWithSurface(Surface surface, int width, int height) {
        Message msg = mGLHandler.obtainMessage(GLThread.GL_INIT_SURFACE, width, height, surface);
        mGLHandler.sendMessage(msg);
    }
    public int getBitmapOrientation(String path){
        return nativeGetBitmapOrientation(path);

    }
    class GLThread extends HandlerThread {
        static final int GL_INIT = 1;
        static final int GL_DEINIT = 2;
        static final int GL_SHOW_BITMAP = 3;
        static final int GL_SCALE = 4;
        static final int GL_TRANSLATE = 5;
        static final int GL_ROTATE = 6;
        static final int GL_SET_ALPHA = 7;
        static final int GL_RESET = 8;

        static final int GL_INIT_SURFACE = 99;

        public GLThread(String name) {
            super(name, Process.THREAD_PRIORITY_DISPLAY);
        }

        @Override
        protected void onLooperPrepared() {
            mGLHandler = new Handler(getLooper()) {

                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case GL_INIT: {
                            int videoLayerWidth = msg.arg1;
                            int videoLayerHeight = msg.arg2;
                            int maxUsedMemSizeByte = (Integer)msg.obj;
                            // add enablePQ
                            Log.d(TAG, "init with enablePQ = " + mEnablePQ);
                            nativeInit(videoLayerWidth, videoLayerHeight, mGraphicLayerWidth,
                                mGraphicLayerHeight, maxUsedMemSizeByte, mEnablePQ);
                            break;
                        }
                        case GL_DEINIT:
                            nativeDeinit();
                            if (mCurrentBmp != null) {
                                mCurrentBmp.recycle();
                            }
                            //quitSafely();
                            break;

                        case GL_SHOW_BITMAP:
                            int viewmode = msg.arg1;
                            int rotateDegree = msg.arg2;
                            try {
                                if(mCurrentShow.equals((String) msg.obj))
                                {
                                    nativeShowImage((String) msg.obj, viewmode, rotateDegree);
                                }
                                else //user call viewImage with new picture path
                                {
                                    showCompleted(false, (String) msg.obj, false, CMD_CANCELED);
                                }
                            } catch (Exception e) {
                                //nativeShowBitmap(mFalureBitmap, viewmode);
                                showCompleted(false, (String) msg.obj);
                            }
                            break;
                        case GL_INIT_SURFACE: {
                            int width = msg.arg1;
                            int height = msg.arg2;
                            Surface surface = (Surface) msg.obj;
                            nativeInitWithSurface(surface, width, height);
                            break;
                        }

                        case GL_SCALE: {
                            Bundle data = msg.getData();
                            float scaleX = data.getFloat("scaleX");
                            float scaleY = data.getFloat("scaleY");
                            nativeScale(scaleX, scaleY);
                            break;
                        }
                        case GL_TRANSLATE: {
                            int tX = msg.arg1;
                            int tY = msg.arg2;
                            nativeTranslate(tX, tY);
                            break;
                        }
                        case GL_ROTATE: {
                            int degree = msg.arg1;
                            nativeRotate(degree);
                            break;
                        }
                        case GL_SET_ALPHA: {
                            float a = (Float) msg.obj;
                            nativeSetAlpha(a);
                            break;
                        }
                        case GL_RESET: {
                            nativeReset();
                            nativeSetAnimationType(mUserAnimType, mUserAnimDuration);
                            break;
                        }
                        default: {
                            nativeDeinit();
                            if (mCurrentBmp != null) {
                                mCurrentBmp.recycle();
                            }
                            break;
                        }
                    }
                }

            };
        }
    }

    Bitmap loadGifBitmap(String path) {
        Movie movie = Movie.decodeFile(path);
        Bitmap bitmap = Bitmap.createBitmap(movie.width(), movie.height(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        c.drawColor(0xffff0000);
        movie.setTime(movie.duration());
        movie.draw(c, movie.width(), movie.height());
        return bitmap;

    }

  private void showDecode(){
      int decode = nativeGetDecodeData();
      switch(decode){
          case 1:
              showToast("decode failed");
              break;
          case 2:
              showToast("Have not enough memory size to decode this pic");
              break;
          case 3:
              showToast("decode failed");
              break;
         default:
              break;
        }
    }

    public int getDecode(){
        int decode = nativeGetDecodeData();
        return decode;
    }

    private void showErr(int errData){
        switch(errData){
            case CALL_UNF_VO_CREATEWINDOW_FAILED:
                showToast("HI_UNF_VO_CreateWindow Failed");
                break;
            case CALL_UNF_VO_ATTACHWINDOW_FAILED:
                showToast("HI_UNF_VO_AttachWindow Failed");
                break;
            case CALL_UNF_VO_SETTIMING_FAILED:
                showToast("HI_UNF_VO_SetTiming Failed");
                break;
            case CALL_UNF_VO_SETWINDOWENABLE_FAILED:
                showToast("HI_UNF_VO_SetWindowEnable Failed");
                break;
            case CALL_SYS_INIT_FAILED:
                showToast("HI_SYS_Init Failed");
                break;
            case CALL_UNF_VO_DETACHWINDOW_FAILED:
                showToast("HI_UNF_VO_DetachWindow(hWindow) Failed");
                break;
            case CALL_UNF_VO_DESTROYWINDOW_FAILED:
                showToast("HI_UNF_VO_DestroyWindow(hWindow) Failed");
                break;
            case CALL_UNF_DISP_INIT_FAILED:
                showToast("HI_UNF_DISP_Init Failed");
                break;
            case CALL_UNF_DISP_ATTACHINTF_FAILED:
                showToast("HI_UNF_DISP_AttachIntf Failed");
                break;
            case CALL_UNF_DISP_OPEN_FAILED:
                showToast("HI_UNF_DISP_Open Failed");
                break;
            case CALL_UNF_VO_INIT_FAILED:
                showToast("HI_UNF_VO_Init Failed");
                break;
            default:
                break;
        }
    }

    private void showToast(String error){
            Toast.makeText(mContext, error, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resource){
            Toast.makeText(mContext, resource, Toast.LENGTH_SHORT).show();
    }

    public native int nativeGetDecodeData();

    public native int nativeGetErrData();

    public native void nativeInit(int videoLayerWidth, int videoLayerHeight, int graphicLayerWidth,
        int graphicLayerHeight, int maxUsedMemSizeByte, boolean enablePQ);

    public native void nativeInitWithSurface(Surface surface, int width, int height);

    public native void nativeDeinit();

    public native void nativeShowImage(String path, int viewmode, int rotateDegree);

    public native void nativeShowBitmap(Bitmap bitmap, int fullScreen);

    public native void nativeScale(float scaleX, float scaleY);

    public native void nativeTranslate(int tX, int tY);

    public native void nativeRotate(int degree);

    public native int nativeGetFormat();

    public native void nativeSetAlpha(float a);

    public native void nativeReset();

    public native void nativeSetAnimationType(int animType, int duration);

    public native boolean nativeDecodeSizeEvaluate(String path, int width, int height, int sampleSize ,int usedDecSize);

    public native int nativeGetBitmapOrientation(String path);
}
