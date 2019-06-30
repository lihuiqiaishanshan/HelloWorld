package com.hisilicon.android.videoplayer.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.hisilicon.android.videoplayer.utils.HiMediaPlayerInvoke;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.WindowType;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultiPlaySurfaceView extends SurfaceView {

    private final String TAG = "HiVideoPlayer_" + MultiPlaySurfaceView.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private String videoPath;
    private MediaPlayer.OnCompletionListener mTopCompletionListener;
    private MediaPlayer.OnErrorListener mTopOnErrorListener;
    private MediaPlayer.OnInfoListener mTopOnInfoListener;
    private boolean mIsPrepared = false;
    //    private Surface mSurface = null;
    private SurfaceHolder mSurfaceHolder = null;
    private Surface mSurface = null;
    private MultiplePlayerOnDestoryFinish multiplePlayerOnDestoryFinish;

    public MultiPlaySurfaceView(Context context) {
        super(context);
        initView();
    }

    public MultiPlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MultiPlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public MultiPlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        Class aClass = getClass();
        try {
            Constructor constructor = aClass.getConstructor(Context.class, AttributeSet.class, Integer.class, Integer.class);
            if (constructor != null) {
                constructor.newInstance(context, attrs, defStyleAttr, defStyleRes);
            }
        } catch (NoSuchMethodException e) {
            LogTool.e(e.toString());
        } catch (IllegalAccessException e) {
            LogTool.e(e.toString());
        } catch (InstantiationException e) {
            LogTool.e(e.toString());
        } catch (InvocationTargetException e) {
            LogTool.e(e.toString());
        }
        initView();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility != VISIBLE) {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
            return;
        }
        //repair reset problem canceled.
        else {
            if (mediaPlayer != null && mIsPrepared) {
                mediaPlayer.start();
            }
        }
    }

    private void initView() {
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

//        setWindowType();
//        this.setSurfaceTextureListener(surfaceTextureListener);
    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            mIsPrepared = true;
        }
    };

    MediaPlayer.OnInfoListener onInfoListener = new MediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            if (null != mTopOnInfoListener) {
                mTopOnInfoListener.onInfo(mp, what, extra);
            }
            return false;
        }
    };

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (null != mTopCompletionListener) {
                mTopCompletionListener.onCompletion(mp);
            }
        }
    };

    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            if (null != mTopOnErrorListener) {
                mTopOnErrorListener.onError(mp, what, extra);
            }
            return false;
        }
    };

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            openVideo(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            destroyPlayer();
        }
    };
//    SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            Log.i(TAG, "---------->onSurfaceTextureAvailable");
//            openVideo(surface);
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            Log.i(TAG, "---------->onSurfaceTextureSizeChanged");
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            Log.i(TAG, "---------->onSurfaceTextureDestroyed");
//            destroyPlayer();
//            return true;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            Log.i(TAG, "---------->onSurfaceTextureUpdated");
//        }
//    };


    private void openVideo(SurfaceHolder surface) {
        if (TextUtils.isEmpty(videoPath) || surface == null) {
            return;
        }
        mSurfaceHolder = surface;
        if (!hasMediaplayer()) {
            getMediaplayer(surface);
        }
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener mTopCompletionListener) {
        this.mTopCompletionListener = mTopCompletionListener;
    }

    public void setOnErrorListener(MediaPlayer.OnErrorListener mTopOnErrorListener) {
        this.mTopOnErrorListener = mTopOnErrorListener;
    }

    public void setOnInfoListener(MediaPlayer.OnInfoListener mTopOnInfoListener) {
        this.mTopOnInfoListener = mTopOnInfoListener;
    }

    public void setMultiplePlayerOnDestoryFinish(MultiplePlayerOnDestoryFinish multiplePlayerOnDestoryFinish) {
        this.multiplePlayerOnDestoryFinish = multiplePlayerOnDestoryFinish;
    }

    public interface MultiplePlayerOnDestoryFinish {
        void multiplePlayerOnDestoryFinish();
    }

    public interface MultiplePlayerOnPauseFinish {
        void multiplePlayOnPauseFinish(boolean isOnPause);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    private void getMediaplayer(SurfaceHolder surface) {
        if (surface == null) {
            return;
        }
        mediaPlayer = new MediaPlayer();
        try {
//            mSurface = new su;
//            mediaPlayer.setSurface(mSurface);
            mSurface = mSurfaceHolder.getSurface();
            mediaPlayer.setDisplay(surface);
            mediaPlayer.setOnPreparedListener(onPreparedListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnErrorListener(onErrorListener);
            mediaPlayer.setOnInfoListener(onInfoListener);
            mediaPlayer.setDataSource(videoPath);
            mediaPlayer.setLooping(true);
            setWindowType();
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            LogTool.e(TAG, "prepare IOException!");
        }
    }

    private boolean hasMediaplayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(videoPath);
                mediaPlayer.setLooping(true);
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                LogTool.e(TAG, "prepare IOException!");
            }
            return true;
        }
        return false;
    }

    public void beginDestory() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        destroyPlayer();
    }

    public void destroyPlayer() {
        //repair reset problem may canceled.
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }

        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        multiplePlayerOnDestoryFinish.multiplePlayerOnDestoryFinish();
    }

    protected final static String IMEDIA_PLAYER = "android.media.IMediaPlayer";

    public int setWindowType() {
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();
        Request.writeInterfaceToken(IMEDIA_PLAYER);
        Request.writeInt(HiMediaPlayerInvoke.CMD_SET_VOUT_WINDOWS_TYPE);
        Request.writeInt(WindowType.DEFINE_VOUT_WIN_TYPE_SUB);
        invoke(Request, Reply);
        Reply.readInt();
        int Result = Reply.readInt();
        Request.recycle();
        Reply.recycle();
        LogTool.d(TAG, "excuteCommand : Result : " + Result);
        return Result;
    }

    public void invoke(Parcel request, Parcel reply) {
        if ((mediaPlayer != null)) {
            /*Add catch because DPT don't support setVideoCvrs function*/
            /*and SDK don't support setNewFounction*/
            try {
                mediaInvoke(request, reply);
                LogTool.d("SetWindowType mediaInvoke");
            } catch (Exception e) {
                LogTool.d("SetWindowType " + e.toString());
            }
        } else {
            LogTool.d("SetWindowType " + " MediaPlayer == Null");
        }
    }

    private void mediaInvoke(Parcel request, Parcel reply) {
        Method method = null;
        try {
            method = this.mediaPlayer.getClass().getDeclaredMethod("invoke", Parcel.class, Parcel.class);
            if (method != null) {
                method.invoke(mediaPlayer, request, reply);
            }
        } catch (NoSuchMethodException e) {
            LogTool.e(TAG, "get Method error !");
        } catch (InvocationTargetException e) {
            LogTool.e(TAG, "Media Invoke Error" + e.toString());
        } catch (IllegalAccessException e) {
            LogTool.e(TAG, "Media Invoke Error " + e.toString());
        }

    }

}
