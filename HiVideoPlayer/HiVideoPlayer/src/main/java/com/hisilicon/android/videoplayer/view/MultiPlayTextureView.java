//package com.hisilicon.test1.myapplication;
package com.hisilicon.android.videoplayer.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;

import com.hisilicon.android.videoplayer.utils.HiMediaPlayerInvoke;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.WindowType;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MultiPlayTextureView extends TextureView {

    private final String TAG = "HiVideoPlayer_" + MultiPlayTextureView.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private String videoPath;
    private MediaPlayer.OnCompletionListener mTopCompletionListener;
    private MediaPlayer.OnErrorListener mTopOnErrorListener;
    private MediaPlayer.OnInfoListener mTopOnInfoListener;
    private boolean mIsPrepared = false;
    private Surface mSurface = null;
    private MultiplePlayerOnDestoryFinish multiplePlayerOnDestoryFinish;

    public MultiPlayTextureView(Context context) {
        super(context);
        initView();
    }

    public MultiPlayTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MultiPlayTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MultiPlayTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
                // mediaPlayer.start();
            }
        }
    }

    private void initView() {
        this.setSurfaceTextureListener(surfaceTextureListener);
    }

    MediaPlayer.OnPreparedListener onPreparedListener = new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.start();
            mIsPrepared = true;
            if (multiplePlayerOnDestoryFinish != null) {
                multiplePlayerOnDestoryFinish.onPrepared();
            }
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

    SurfaceTextureListener surfaceTextureListener = new SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            LogTool.i(TAG, "---------->onSurfaceTextureAvailable");
            openVideo(surface);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            LogTool.i(TAG, "---------->onSurfaceTextureSizeChanged");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            LogTool.i(TAG, "---------->onSurfaceTextureDestroyed");
            destroy();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            LogTool.i(TAG, "---------->onSurfaceTextureUpdated");
        }
    };


    private void openVideo(SurfaceTexture surface) {
        if (TextUtils.isEmpty(videoPath) || surface == null) {
            return;
        }
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

        void onPrepared();
    }

    public interface MultiplePlayerOnPauseFinish {
        void multiplePlayOnPauseFinish(boolean isOnPause);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
    }

    private void getMediaplayer(SurfaceTexture surface) {
        if (surface == null && mSurface == null) {
            return;
        }
        try {
            mediaPlayer = new MediaPlayer();
            mSurface = surface != null ? new Surface(surface) : mSurface;
            mediaPlayer.setSurface(mSurface);
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

    public boolean hasMediaplayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                mediaPlayer.stop();
            }
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
        destroy();
    }

    public void destroyPlayer() {
        //repair reset problem may canceled.
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

    }

    private void destroy() {
        //repair reset problem may canceled.
        destroyPlayer();
        if (mSurface != null) {
            mSurface.release();
            mSurface = null;
        }
        multiplePlayerOnDestoryFinish.multiplePlayerOnDestoryFinish();
    }

    public void start() {
        if (!hasMediaplayer()) {
            getMediaplayer(null);
        }
    }

    public boolean isMediaReady() {
        return mediaPlayer != null;
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
