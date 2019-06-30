package com.hisilicon.android.videoplayer.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.TimedText;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.MediaController.MediaPlayerControl;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.activity.VideoActivity;
import com.hisilicon.android.videoplayer.activity.inter.OnPlayerDestroyListener;
import com.hisilicon.android.videoplayer.model.Common;
import com.hisilicon.android.videoplayer.model.DoblyResult;
import com.hisilicon.android.videoplayer.model.bluray.base.LanguageXmlParser;
import com.hisilicon.android.videoplayer.utils.Constants;
import com.hisilicon.android.videoplayer.utils.HiMediaPlayerDefine;
import com.hisilicon.android.videoplayer.utils.HiMediaPlayerInvoke;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.PlayerJugdment;
import com.hisilicon.android.videoplayer.utils.SystemProperties;
import com.hisilicon.android.videoplayer.utils.SSlUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import android.media.PlaybackParams;
import javax.net.ssl.HttpsURLConnection;

public class HisiVideoView extends SurfaceView implements MediaPlayerControl {
    private String TAG = "HisiVideoView";
    private Context mContext;
    private Uri mUri;
    private int mDuration;
    private SurfaceHolder mSurfaceHolder = null;
    public MediaPlayer mMediaPlayer = null;
    protected boolean mIsPrepared;
    protected int mVideoWidth;
    protected int mVideoHeight;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    private OnCompletionListener mOnCompletionListener;

    private MediaPlayer.OnPreparedListener mOnPreparedListener;
    private MediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener;
    private int mCurrentBufferPercentage;
    private OnErrorListener mOnErrorListener;
    private OnInfoListener mOnInfoListener;
    private OnPlayerDestroyListener mOnPlayerDestroyListener;
    private MediaPlayer.OnTimedTextListener mOnTimedTextListener;
    private boolean mStartWhenPrepared;
    private int mSeekWhenPrepared;

    private int mSubtitleNumber;

    private int mExtSubtitleNumber;

    private int mAudioTrackNumber;

    private int mSelectSubtitleId = 0;

    private int mSelectAudioTrackId = 0;

    private int mSelectAudioChannelId = 0;

    private List<String> mSubtitleLanguageList;

    private List<String> mExtraSubtitleList;

    private List<String> mAudioTrackLanguageList;

    private List<String> mAudioFormatList;

    private List<String> mAudioSampleRateList;

    private List<String> mAudioChannelList;

    public String[] mSubFormat = {"ASS", "LRC", "SRT", "SMI", "SUB", "TXT", "PGS", "DVB", "DVD", "TTML", "WEBVTT"};

    protected final static String IMEDIA_PLAYER = "android.media.IMediaPlayer";

    private Surface mSubSurface;
    private SurfaceView mSubtitelView;
    private SurfaceHolder mSubtiteHolder;

    private final boolean DEBUG = false;

    private static final Object mObjLock = new Object();

    private int defaultWidth = 0;
    private int defaultHeight = 0;
    private LanguageXmlParser xmlParser;
    private int subTitleId = -1;
    /**
     * Recorded videoFmt in the standby startup to resume the previous setting
     */
    private int inVideoFmt = 0;

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public void setVideoScale(int width, int height) {
        LayoutParams lp = getLayoutParams();

        lp.width = width;
        mVideoWidth = width;

        lp.height = height;
        mVideoHeight = height;
        LogTool.d(TAG, "setVideoScale width : " + width + " Height : " + height);
        setLayoutParams(lp);
        invalidate();
        setLayoutLocation(0, 0);
    }


    public HisiVideoView(Context context) {
        super(context);
        mContext = context;
        initVideoView();
    }

    public HisiVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mContext = context;
        initVideoView();
    }

    public HisiVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        initVideoView();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);

        setMeasuredDimension(width, height);
    }

    public int resolveAdjustedSize(int desiredSize, int measureSpec) {
        int result = desiredSize;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        switch (specMode) {
            case MeasureSpec.UNSPECIFIED:

                result = desiredSize;
                break;

            case MeasureSpec.AT_MOST:

                result = Math.min(desiredSize, specSize);
                break;

            case MeasureSpec.EXACTLY:

                result = specSize;
                break;
        }

        return result;
    }

    private void initVideoView() {
        xmlParser = new LanguageXmlParser(getContext());
        mVideoWidth = 0;
        mVideoHeight = 0;
        getHolder().addCallback(mSHCallback);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        getHolder().setFormat(PixelFormat.RGBA_8888);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        try {
            HttpsURLConnection.setDefaultSSLSocketFactory(SSlUtils.createSSLSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new SSlUtils.TrustAllHostnameVerifier());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setVideoPath(String path) {
        path = Common.transferredMeaning(path);
        setVideoURI(Uri.parse(path));
    }

    public void setVideoURI(Uri uri) {
        mUri = uri;
        mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    private void setSubtitleSurfaceInvoke(final Surface surface) {
        int ret = -1;

        if (surface == null) {
            LogTool.e(TAG, " setSubtitleSurfaceInvoke surface is null!");
            return;
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();

        request.writeInterfaceToken(IMEDIA_PLAYER);
        request.writeInt(HiMediaPlayerInvoke.CMD_SET_SUB_SURFACE);
        int temp = request.dataPosition();

        surface.writeToParcel(request, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        request.setDataPosition(temp);
        if (DEBUG)
            LogTool.i(TAG, " parcel offset " + request.dataPosition() + " is " + request.readString());
        if (DEBUG)
            LogTool.i(TAG, " parcel offset " + request.dataPosition() + " is " + request.readStrongBinder());
        request.setDataPosition(temp);
        surface.readFromParcel(request);
        if (DEBUG) LogTool.i(TAG, " parcel offset " + request.dataPosition() + " is " + surface);

        invoke(request, reply);
        request.setDataPosition(temp);
        surface.readFromParcel(request);
        if (DEBUG) LogTool.i(TAG, " parcel offset " + request.dataPosition() + " is " + surface);
        reply.setDataPosition(0);
        ret = reply.readInt();
        if (ret != 0) {
            LogTool.e(TAG, "Subtitle Invoke call set failed , ret = " + ret);
        } else
            LogTool.i(TAG, "Subtitle Invoke Sucessfull !");

        request.recycle();
        reply.recycle();
    }

    private void setSubtitleMode(int mode) {
        int ret = -1;
        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(IMEDIA_PLAYER);
        request.writeInt(HiMediaPlayerInvoke.CMD_SET_SUB_MODE);
        request.writeInt(mode);
        invoke(request, reply);
        reply.setDataPosition(0);
        ret = reply.readInt();
        if (ret != 0) {
            LogTool.e(TAG, "set Subtitle mode Invoke failed , ret = " + ret);
        } else {
            LogTool.i(TAG, "set Subtitle mode Invoke Sucessfull !");
        }
    }

    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    public void setSubtitleSurface(Surface SubSurface) {
        mSubSurface = SubSurface;
    }

    public void setSubtitleView(SurfaceView SubtitelView) {/* for bug"setVideoSurfaceTexture failed: -22" */
        mSubtitelView = SubtitelView;
        // set Subtitle SurfaceView
        mSubtiteHolder = mSubtitelView.getHolder();
        mSubtiteHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        mSubtiteHolder.setFormat(PixelFormat.RGBA_8888);
        mSubtiteHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //TODO Auto-generated method stub
                LogTool.d(TAG, "HiSurfaceView surfaceDestroyed");
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                LogTool.d(TAG, "HiSurfaceView surfaceCreated");
                Surface surface;
                if (mSubtiteHolder != null) {
                    surface = mSubtiteHolder.getSurface();
                    setSubtitleSurface(surface);
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
            }
        });
    }

    private void openVideo() {
        if ((mUri == null) || (mSurfaceHolder == null)) {
            if (DEBUG) LogTool.i(TAG, " openVideo return ! ");
            return;
        }
        if (mMediaPlayer != null) {
            mSubtitelView.setVisibility(View.GONE);/* for bug"setVideoSurfaceTexture failed: -22", subtitle surface cause surface reconnect error. the player will call reset when surface reconnect error. */
            mMediaPlayer.reset();
            LogTool.i(TAG, "DataSource :" + mUri);
            try {
                mIsPrepared = false;
                mDuration = -1;
                mCurrentBufferPercentage = 0;
                mSelectSubtitleId = 0;
                mSelectAudioTrackId = 0;
                /*
                    The video that resolves some special string names can not be played.
                    for example:%5B4K123.COM%5(HEVC_3840x2032)
                */
                //mMediaPlayer.setDataSource(mContext, mUri);
                mMediaPlayer.setDataSource(mUri.toString());
                Surface mSurface;
                mSurface = mSurfaceHolder.getSurface();
                int[] location = new int[2];
                getLocationOnScreen(location);

                int mX, mY, mW, mH;
                if (null != mSurface) {
                    mX = location[0];
                    mY = location[1];
                    mW = mVideoWidth;
                    mH = mVideoHeight;
                    // this function not found if MediaPlayer
                    //mMediaPlayer.setVideoRange(mX, mY, mW, mH);
                }
                if (VideoActivity.getFormatAdption() == 1) {
                    setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
                }
                //Subtitle Invoke Call

                mSubtitelView.setVisibility(View.VISIBLE);
                if (mSubSurface != null) {
                    setSubtitleSurfaceInvoke(mSubSurface);
                    setSubtitleMode(1);
                } else {
                    LogTool.e(TAG, "Error : Before call Subtitle Invoke , the Subtitle Surface is null!");
                }
                if (VideoActivity.get3dModeAdpate() == 1) {
                    if (VideoActivity.getFormatAdption() == 1) {
                        setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK +
                                HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
                    } else {
                        setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK);
                    }
                } else {
                    if (VideoActivity.getFormatAdption() == 1) {
                        setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_2D_MASK +
                                HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
                    } else {
                        setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_2D_MASK);
                    }
                }
                mMediaPlayer.prepareAsync();
            } catch (IOException ex) {
                LogTool.w(TAG, "IO Unable to open content: " + mUri, ex);
                callActivityErrorListener();
                return;
            } catch (IllegalArgumentException ex) {
                LogTool.w(TAG, "IL Unable to open content: " + mUri, ex);
                callActivityErrorListener();
                return;
            }
            return;
        }

        try {
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setOnPreparedListener(mPreparedListener);
            mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
            mIsPrepared = false;
            mDuration = -1;
            mMediaPlayer.setOnCompletionListener(mCompletionListener);
            mMediaPlayer.setOnInfoListener(m3DModeReceivedListener);
            mMediaPlayer.setOnErrorListener(mErrorListener);
            mMediaPlayer.setOnTimedTextListener(mTimedTextListener);
            mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
            mCurrentBufferPercentage = 0;
            mSelectSubtitleId = 0;
            mSelectAudioTrackId = 0;
            mSelectAudioChannelId = 0;
            LogTool.i(TAG, "DataSource in try:" + mUri);
            /*
                The video that resolves some special string names can not be played.
                for example:%5B4K123.COM%5D(HEVC_3840x2032)
            */
            //mMediaPlayer.setDataSource(mContext, mUri);
            mMediaPlayer.setDataSource(mUri.toString());
            mMediaPlayer.setDisplay(mSurfaceHolder);
            Surface mSurface;
            mSurface = mSurfaceHolder.getSurface();
            //Subtitle Invoke Call
            if (mSubSurface != null) {
                setSubtitleSurfaceInvoke(mSubSurface);
                setSubtitleMode(1);
                sharedPreferencesOpration(Constants.SHARED, "subtitle_show_tag", 0, 0, true);
            } else {
                LogTool.e(TAG, "Error : Before call Subtitle Invoke , the Subtitle Surface is null!");
            }

            int[] location = new int[2];
            getLocationOnScreen(location);

            int mX, mY, mW, mH;
            if (null != mSurface) {
                mX = location[0];
                mY = location[1];
                mW = mVideoWidth;
                mH = mVideoHeight;
                // this function not found if MediaPlayer
                //mMediaPlayer.setVideoRange(mX, mY, mW, mH);
            }

            if (VideoActivity.getFormatAdption() == 1) {
                setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
            }
            //mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mMediaPlayer.setScreenOnWhilePlaying(true);
            if (VideoActivity.get3dModeAdpate() == 1) {
                if (VideoActivity.getFormatAdption() == 1) {
                    setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK +
                            HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
                } else {
                    setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_ADAPT_MASK);
                }
            } else {
                if (VideoActivity.getFormatAdption() == 1) {
                    setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_2D_MASK +
                            HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_24FPS_MASK);
                } else {
                    setStereoStrategy(HiMediaPlayerDefine.DEFINE_STEREOVIDEO_STRATEGY_2D_MASK);
                }
            }
            mMediaPlayer.prepareAsync();
        } catch (IOException ex) {
            LogTool.w(TAG, "IOE Unable to open content: " + mUri, ex);
            callActivityErrorListener();
            return;
        } catch (IllegalArgumentException ex) {
            LogTool.w(TAG, "ILL Unable to open content: " + mUri, ex);
            callActivityErrorListener();
            return;
        }
    }

    private void callActivityErrorListener() {
        if (mOnErrorListener != null) {
            mOnErrorListener.onError(mMediaPlayer, 0, 0);
        }
    }

    MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
            new MediaPlayer.OnVideoSizeChangedListener() {
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    if ((mVideoWidth != 0) && (mVideoHeight != 0)) {
                    }
                    if (mOnVideoSizeChangedListener != null) {
                        mOnVideoSizeChangedListener.onVideoSizeChanged(mp,width,height);
                    }

                }
            };

    MediaPlayer.OnPreparedListener mPreparedListener = new MediaPlayer.OnPreparedListener() {
        public void onPrepared(MediaPlayer mp) {
            LogTool.d(TAG, "HisiVideoView,onPrepared():" + mOnPreparedListener);
            mIsPrepared = true;
            enableSubtitle(sharedPreferencesOpration(Constants.SHARED, "subtitle_show_tag", 0, 1, false));
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            defaultWidth = mVideoWidth;
            defaultHeight = mVideoHeight;
            if (mOnPreparedListener != null) {
                mOnPreparedListener.onPrepared(mMediaPlayer);
                return;
            }

            setVisibility(GONE);
            if ((mVideoWidth != 0) && (mVideoHeight != 0)) {
                getHolder().setFixedSize(mVideoWidth, mVideoHeight);

                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }

                if (mStartWhenPrepared) {
                    mMediaPlayer.start();
                    mStartWhenPrepared = false;
                }
            } else {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }

                if (mStartWhenPrepared) {
                    mMediaPlayer.start();
                    mStartWhenPrepared = false;
                }
            }
            setVisibility(VISIBLE);
        }
    };

    private OnCompletionListener mCompletionListener =
            new OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    LogTool.d(TAG, "HisiVideoView,onCompletion()");
                    if (mOnCompletionListener != null) {
                        LogTool.d(TAG, "HisiVideoView,onCompletion() != null");
                        mOnCompletionListener.onCompletion(mMediaPlayer);
                    }
                }
            };

    private OnSeekCompleteListener mOnSeekCompleteListener =
            new OnSeekCompleteListener() {
                public void onSeekComplete(MediaPlayer mp) {
                    LogTool.d(TAG, "HisiVideoView,onSeekComplete()");
                    if (mOnSeekCompleteListener != null) {
                        mOnSeekCompleteListener.onSeekComplete(mMediaPlayer);
                    }
                }
            };

    private OnInfoListener m3DModeReceivedListener =
            new OnInfoListener() {
                public boolean onInfo(MediaPlayer mp, int what, int extra) {
                    LogTool.d(TAG, "HisiVideoView,onInfo(what=" + what + ", extra=" + extra + ")");
                    if (mOnInfoListener != null) {
                        return mOnInfoListener.onInfo(mMediaPlayer, what, extra);
                    }

                    return false;
                }
            };

    private OnErrorListener mErrorListener =
            new OnErrorListener() {
                public boolean onError(MediaPlayer mp, int framework_err, int impl_err) {
                    LogTool.d(TAG, "Error: " + framework_err + "," + impl_err);

                    if (mOnErrorListener != null) {
                        if (mOnErrorListener.onError(mMediaPlayer, framework_err, impl_err)) {
                            return true;
                        }
                    }

                    if (getWindowToken() != null) {
                    }
                    return true;
                }
            };

    private MediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener =
            new MediaPlayer.OnBufferingUpdateListener() {
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    mCurrentBufferPercentage = percent;
                }
            };

    private MediaPlayer.OnTimedTextListener mTimedTextListener = new MediaPlayer.OnTimedTextListener() {
        @Override
        public void onTimedText(MediaPlayer mediaPlayer, TimedText timedText) {
            if (mOnTimedTextListener != null) {
                mOnTimedTextListener.onTimedText(mediaPlayer, timedText);
            }
        }
    };

    public void setmOnTimedTextListener(MediaPlayer.OnTimedTextListener l) {
        mOnTimedTextListener = l;
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnSeekCompleteListener(OnSeekCompleteListener l) {
        mOnSeekCompleteListener = l;
    }

    // public void setOn3DModeReceivedListener(OnInfoListener l)
    // {
    //     mOn3DModeReceivedListener = l;
    // }

    public void setOnErrorListener(OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnInfoListener(OnInfoListener l) {
        mOnInfoListener = l;
    }

    public void setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener listener){
        mOnVideoSizeChangedListener = listener;
    }

    public void setOnPlayerDestroyListener(OnPlayerDestroyListener l) {
        mOnPlayerDestroyListener = l;
    }

    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {
        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int w, int h) {
            LogTool.d(TAG, "HiSurfaceView surfaceChanged");
            mSurfaceWidth = w;
            mSurfaceHeight = h;
            if ((mMediaPlayer != null) && mIsPrepared && (mVideoWidth == w) && (mVideoHeight == h)) {
                if (mSeekWhenPrepared != 0) {
                    mMediaPlayer.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
            }
        }

        public void surfaceCreated(SurfaceHolder holder) {
            LogTool.d(TAG, "HiSurfaceView surfaceCreated");
            mSurfaceHolder = holder;
            if (mMediaPlayer == null) {
                openVideo();
            }/* else {
                mMediaPlayer.setDisplay(mSurfaceHolder);
                requestLayout();
                invalidate();
            }*/
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            LogTool.d(TAG, "HiSurfaceView surfaceDestroyed");
            if (mOnPlayerDestroyListener != null) {
                mOnPlayerDestroyListener.onPlayerDestroy();
            }
            destroyPlayer();
        }
    };

    public void invoke(Parcel request, Parcel reply) {
        if ((mMediaPlayer != null)) {
            /*Add catch because DPT don't support setVideoCvrs function*/
            /*and SDK don't support setNewFounction*/
            try {
//                mediaInvoke(request, reply);
                mMediaPlayer.invoke(request, reply);
            } catch (Exception e) {
                LogTool.e(TAG, "--->MediaPlayer.invoke ------" + e.toString());
            }
        }
    }

    private void mediaInvoke(Parcel request, Parcel reply) {
        Method method = null;
        try {
            method = this.mMediaPlayer.getClass().getDeclaredMethod("invoke", Parcel.class, Parcel.class);
            method.setAccessible(true);
            if (method != null) {
                method.invoke(mMediaPlayer, request, reply);
            }
        } catch (NoSuchMethodException e) {
            LogTool.e(TAG, "get Method error !");
        } catch (InvocationTargetException e) {
            LogTool.e(TAG, "Media Invoke Error" + e.toString());
        } catch (IllegalAccessException e) {
            LogTool.e(TAG, "Media Invoke Error " + e.toString());
        }

    }

    public int resume() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Common.setResume(true);
            return play();
        }
        return -1;
    }

    public int play() {
        int result = 0;
        if (PlayerJugdment.isHiPlayer()) {
            Parcel requestParcel = Parcel.obtain();
            requestParcel.writeInterfaceToken(IMEDIA_PLAYER);
            requestParcel.writeInt(HiMediaPlayerInvoke.CMD_SET_STOP_FASTPLAY);
            Parcel replyParcel = Parcel.obtain();
            invoke(requestParcel, replyParcel);
            requestParcel.recycle();
            replyParcel.recycle();
            result = replyParcel.readInt();
        } else {
            if ((mMediaPlayer != null) /*&& mIsPrepared*/) {
                final float playbackRate = 1.0f;
                mMediaPlayer.setPlaybackParams(new PlaybackParams().setSpeed(playbackRate));
                result = 0;
            } else {
                result = -1;
            }
        }
        return result;
    }

    public void start() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            mMediaPlayer.start();
            mStartWhenPrepared = false;
        } else {
            mStartWhenPrepared = true;
        }
    }

    public void reset() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            mMediaPlayer.reset();
        }
    }

    public void pause() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
            }
        }
        mStartWhenPrepared = false;
    }

    public int getDuration() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            if (mDuration > 0) {
                return mDuration;
            }
            mDuration = mMediaPlayer.getDuration();
            return mDuration;
        }
        mDuration = -1;
        return mDuration;
    }

    public int getCurrentPosition() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public void seekTo(int msec) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            mMediaPlayer.seekTo(msec);
        } else {
            mSeekWhenPrepared = msec;
        }
    }

    public int setSpeed(int speed) {
        int flag = 0;

        if ((mMediaPlayer != null) && mIsPrepared) {

            if (speed == 1) {
                flag = HiMediaPlayerInvoke.CMD_SET_STOP_FASTPLAY;
            } else if (speed == 2 || speed == 4 || speed == 8 || speed == 16 || speed == 32) {
                flag = HiMediaPlayerInvoke.CMD_SET_FORWORD;
            } else if (speed == -2 || speed == -4 || speed == -8 || speed == -16 || speed == -32) {
                flag = HiMediaPlayerInvoke.CMD_SET_REWIND;
                speed = -speed;
            } else {
                LogTool.e(TAG, "setSpeed error:" + speed);
            }
            return excuteCommand(flag, speed, false);
        }
        return -1;
    }

    public void setSpeed(double speed) {
        double tempSpeed = speed * 100;
        int mFirst = (int) (tempSpeed / 100);
        int mSecond = (int) (tempSpeed % 100);
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();
        Request.writeInterfaceToken(IMEDIA_PLAYER);
        Request.writeInt(HiMediaPlayerInvoke.CMD_SET_TRICKPLAY_SPEED);
        Request.writeInt(mFirst);
        Request.writeInt(mSecond);
        invoke(Request, Reply);
        Request.recycle();
        Reply.recycle();
    }

    private int excuteCommand(int pCmdId, int pArg, boolean pIsGet) {
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();
        Request.writeInterfaceToken(IMEDIA_PLAYER);
        Request.writeInt(pCmdId);
        Request.writeInt(pArg);
        invoke(Request, Reply);
        if (pIsGet) {
            Reply.readInt();
        }
        int Result = Reply.readInt();
        Request.recycle();
        Reply.recycle();
        LogTool.d(TAG, "excuteCommand : Result : " + Result);
        return Result;
    }

    public boolean isPlaying() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    public int getBufferPercentage() {
        if (mMediaPlayer != null) {
            return mCurrentBufferPercentage;
        }
        return 0;
    }

    public int setStereoVideoFmt(int inVideoFmt) {
        if (mMediaPlayer != null) {
            return setStereoVideoFmtInvoke(inVideoFmt);
        }
        LogTool.e(TAG, "setStereoVideoFmt mMediaPlayer == null, return -1");
        return -1;
    }

    public int setStereoStrategy(int strategy) {
        if (mMediaPlayer != null) {
            return setStereoStrategyInvoke(strategy);
        }
        LogTool.e(TAG, "setStereoStrategy mMediaPlayer == null , return -1");
        return -1;
    }

    public int setStereoStrategyInvoke(int strategy) {
        int ret = -1;
        if (mMediaPlayer != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(HiMediaPlayerInvoke.CMD_SET_3D_STRATEGY);
            request.writeInt(strategy);
            invoke(request, reply);
            reply.setDataPosition(0);
            ret = reply.readInt();
            if (ret != 0)
                LogTool.e(TAG, "setStereoStrategyInvoke Failed !");

            request.recycle();
            reply.recycle();
        }
        return ret;
    }

    public int setStereoVideoFmtInvoke(int inVideoFmt) {
        int ret = -1;
        if (mMediaPlayer != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(HiMediaPlayerInvoke.CMD_SET_3D_FORMAT);
            request.writeInt(inVideoFmt);

            invoke(request, reply);
            reply.setDataPosition(0);
            ret = reply.readInt();

            if (ret != 0) {
                LogTool.e(TAG, "setStereoVideoFmtInvoke Failed !");
            }
            request.recycle();
            reply.recycle();
        }
        return ret;
    }


    public Parcel getMediaInfo() {
        if (mMediaPlayer != null) {
            int flag = HiMediaPlayerInvoke.CMD_GET_FILE_INFO;
            Parcel Request = Parcel.obtain();
            Parcel Reply = Parcel.obtain();
            Request.writeInterfaceToken(IMEDIA_PLAYER);
            Request.writeInt(flag);
            invoke(Request, Reply);
            Reply.setDataPosition(0);
            Request.recycle();
            return Reply;
        }
        return null;
    }

    private Parcel getInfo(int flag) {
        Parcel requestParcel = Parcel.obtain();
        requestParcel.writeInterfaceToken(IMEDIA_PLAYER);
        requestParcel.writeInt(flag);
        Parcel replyParcel = Parcel.obtain();
        invoke(requestParcel, replyParcel);
        replyParcel.setDataPosition(0);
        requestParcel.recycle();
        return replyParcel;
    }

    public int setVideoCvrs(int flag) {
        if (mMediaPlayer != null) {
            Parcel Request = Parcel.obtain();
            Parcel Reply = Parcel.obtain();

            Request.writeInterfaceToken(IMEDIA_PLAYER);
            Request.writeInt(HiMediaPlayerInvoke.CMD_SET_VIDEO_CVRS);
            Request.writeInt(flag);

            /*need catch in invoke() because DPT don't support setVideoCvrs function*/
            invoke(Request, Reply);

            Reply.readInt();

            int Result = Reply.readInt();

            Request.recycle();
            Reply.recycle();

            return Result;
        }

        return -1;
    }

    public synchronized List<String> getAudioInfoList() {
        if (PlayerJugdment.isHiPlayer()) {
            if ((mMediaPlayer != null) && mIsPrepared) {
                Parcel _Request = Parcel.obtain();
                Parcel _Reply = Parcel.obtain();

                _Request.writeInterfaceToken(IMEDIA_PLAYER);
                _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_INFO);

                invoke(_Request, _Reply);

                List<String> _AudioInfoList = new ArrayList<String>();

                // for get
                _Reply.readInt();
                int _Num = _Reply.readInt();
                String _Language = "";
                String _Format = "";
                String _SampleRate = "";
                String _Channel = "";

                for (int i = 0; i < _Num; i++) {
                    _Language = _Reply.readString();
                    if (_Language == null || _Language.equals("und")) {
                        _Language = "";
                    }

                    _AudioInfoList.add(_Language);

                    _Format = Integer.toString(_Reply.readInt());
                    _AudioInfoList.add(_Format);

                    _SampleRate = Integer.toString(_Reply.readInt());
                    _AudioInfoList.add(_SampleRate);

                    int _ChannelNum = _Reply.readInt();
                    int bitrate = _Reply.readInt();
                    switch (_ChannelNum) {
                        case 0:
                        case 1:
                        case 2:
                            _Channel = _ChannelNum + ".0";
                            break;
                        default:
                            _Channel = (_ChannelNum - 1) + ".1";
                            break;
                    }

                    _AudioInfoList.add(_Channel);
                }

                _Request.recycle();
                _Reply.recycle();

                return _AudioInfoList;
            }
        } else {
            return TrackHandler.HANDLER.getTracks(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        }
        return null;
    }

    public int getAudioTrackNumber() {
        return mAudioTrackNumber;
    }

    public void setAudioTrackNumber(int pAudioTrackNumber) {
        mAudioTrackNumber = pAudioTrackNumber;
    }

    public int getSelectAudioTrackId() {
        return mSelectAudioTrackId;
    }

    public void setSelectAudioTrackId(int pSelectAudioTrackId) {
        mSelectAudioTrackId = pSelectAudioTrackId;
    }

    public int getSelectAudioChannelId() {
        return mSelectAudioChannelId;
    }

    public void setSelectAudioChannelId(int pSelectAudioChannelId) {
        mSelectAudioChannelId = pSelectAudioChannelId;
    }

    public List<String> getAudioTrackLanguageList() {
        return mAudioTrackLanguageList;
    }

    public void setAudioTrackLanguageList(List<String> pAudioTrackLanguageList) {
        mAudioTrackLanguageList = pAudioTrackLanguageList;
    }

    public List<String> getAudioFormatList() {
        return mAudioFormatList;
    }

    public void setAudioFormatList(List<String> pAudioFormatList) {
        mAudioFormatList = pAudioFormatList;
    }

    public List<String> getAudioSampleRateList() {
        return mAudioSampleRateList;
    }

    public void setAudioSampleRateList(List<String> pAudioSampleRateList) {
        mAudioSampleRateList = pAudioSampleRateList;
    }

    public List<String> getAudioChannelList() {
        return mAudioChannelList;
    }

    public int setAudioTrackPid(int pAudioId) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return setAudioTrack(pAudioId);
        }

        return -1;
    }

    public int setAudioTrack(int track) {
        if (PlayerJugdment.isHiPlayer()) {
            int flag = HiMediaPlayerInvoke.CMD_SET_AUDIO_TRACK_PID;
            return excuteCommand(flag, track, false);
        } else {
            return TrackHandler.HANDLER.selectTrack(mMediaPlayer, track, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        }
    }

    public int getAudioTrackPid() {
        if (PlayerJugdment.isHiPlayer()) {
            if ((mMediaPlayer != null) && mIsPrepared) {
                Parcel Request = Parcel.obtain();
                Parcel Reply = Parcel.obtain();
                Request.writeInterfaceToken(IMEDIA_PLAYER);
                Request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_TRACK_PID);
                invoke(Request, Reply);
                Reply.readInt();
                int Result = Reply.readInt();
                LogTool.d(HisiVideoView.class.getSimpleName(), "Audio getAudioPid : " + Result);
                Request.recycle();
                Reply.recycle();
                return Result;
            }
        } else {
            return TrackHandler.HANDLER.getCurrTrackId(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO);
        }
        return -1;
    }

    public int getAudioChannelPid() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel _Request = Parcel.obtain();
            Parcel _Reply = Parcel.obtain();

            _Request.writeInterfaceToken(IMEDIA_PLAYER);
            _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_AUDIO_CHANNEL_MODE);

            invoke(_Request, _Reply);

            _Reply.setDataPosition(0);
            _Reply.readInt();
            int ChannelPid = _Reply.readInt();

            _Request.recycle();
            _Reply.recycle();

            return ChannelPid;
        }
        return -1;
    }

    public int setAudioChannelPid(int pAudioChannelId) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return setAudioChannel(pAudioChannelId);
        }

        return -1;
    }

    public int setAudioChannel(int channel) {

        int flag = HiMediaPlayerInvoke.CMD_SET_AUDIO_CHANNEL_MODE;
        return excuteCommand(flag, channel, false);

    }


    public void setAudioChannelList(List<String> pAudioChannelList) {
        mAudioChannelList = pAudioChannelList;
    }

    public List<String> getInternalSubtitleLanguageInfoList() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel _Request = Parcel.obtain();
            Parcel _Reply = Parcel.obtain();

            _Request.writeInterfaceToken(IMEDIA_PLAYER);
            _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);

            invoke(_Request, _Reply);

            List<String> _LanguageList = new ArrayList<String>();

            // for get
            _Reply.readInt();
            int _Num = _Reply.readInt();
            String _Language = "";
            String _SubFormat = "";
            int _IsExt = 0;

            for (int i = 0; i < _Num; i++) {
                _Reply.readInt();
                _IsExt = _Reply.readInt();
                _Language = _Reply.readString();
                _SubFormat = mSubFormat[_Reply.readInt()];
                if (_Language == null || _Language.equals("-")) {
                    _Language = "";
                }
                if (_IsExt == 0) {
                    _LanguageList.add(_SubFormat);
                    _LanguageList.add(_Language);
                }
            }

            _Request.recycle();
            _Reply.recycle();

            return _LanguageList;
        }

        return new ArrayList<String>();
    }

    public int setDolbyCertification(int start, int end) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel _Request = Parcel.obtain();
            Parcel _Reply = Parcel.obtain();

            _Request.writeInterfaceToken(IMEDIA_PLAYER);
            _Request.writeInt(HiMediaPlayerInvoke.CMD_SET_AVSYNC_START_REGION);
            _Request.writeInt(start);
            _Request.writeInt(end);

            invoke(_Request, _Reply);

            _Reply.setDataPosition(0);
            int result = _Reply.readInt();

            _Request.recycle();
            _Reply.recycle();

            return result;
        }

        return -1;
    }

    public int setDolbyDacDectUnable(int unable) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel _Request = Parcel.obtain();
            Parcel _Reply = Parcel.obtain();

            _Request.writeInterfaceToken(IMEDIA_PLAYER);
            _Request.writeInt(HiMediaPlayerInvoke.CMD_SET_DAC_DECT_ENABLE);
            _Request.writeInt(unable);

            invoke(_Request, _Reply);
            _Reply.setDataPosition(0);
            int result = _Reply.readInt();

            _Request.recycle();
            _Reply.recycle();

            return result;
        }

        return -1;
    }

    public List<String> getExtSubtitleLanguageInfoList() {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel _Request = Parcel.obtain();
            Parcel _Reply = Parcel.obtain();

            _Request.writeInterfaceToken(IMEDIA_PLAYER);
            _Request.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);

            invoke(_Request, _Reply);

            List<String> _LanguageList = new ArrayList<String>();

            // for get
            _Reply.readInt();
            int _Num = _Reply.readInt();
            String _Language = "";
            String _SubFormat = "";
            int _IsExt = 0;

            for (int i = 0; i < _Num; i++) {
                _Reply.readInt();
                _IsExt = _Reply.readInt();
                _Language = _Reply.readString();
                _SubFormat = mSubFormat[_Reply.readInt()];
                if (_Language == null || _Language.equals("-")) {
                    _Language = "";
                }
                if (_IsExt == 1) {
                    _LanguageList.add(_SubFormat);
                    _LanguageList.add(_Language);
                }
            }

            _Request.recycle();
            _Reply.recycle();

            return _LanguageList;
        }

        return new ArrayList<String>();
    }

    public List<String> getSubtitles() {
        return TrackHandler.HANDLER.getTracks(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
    }

    public String getCurrSubTrack() {
        if (PlayerJugdment.isHiPlayer()) {
            Parcel reply = null;
            reply = getInfo(HiMediaPlayerInvoke.CMD_GET_SUB_INFO);
            reply.readInt();
            int subCount = reply.readInt();
            if (subCount != 0) {
                int selectSub = getSubtitleId();
                int isExt = 0;
                String Sub = "";
                String SubFormat = "";
                for (int i = 0; i < subCount; i++) {
                    int tempid = reply.readInt();
                    int tempisExt = reply.readInt();
                    String tempSub = reply.readString();
                    String tempSubFormat = mSubFormat[reply.readInt()];
                    if (tempid == selectSub) {
                        isExt = tempisExt;
                        Sub = tempSub;
                        SubFormat = tempSubFormat;
                        break;
                    }
                }
                if (Sub == null)
                    Sub = "";
                int _SubNum = getSubtitleNumber();
                int _ExtNum = getExtSubtitleNumber();
                int _Select = getSubtitleId();
                if (_ExtNum != 0) {
                    if (_Select >= (_SubNum - _ExtNum))
                        _Select = _Select - (_SubNum - _ExtNum);
                    else if (_Select < (_SubNum - _ExtNum))
                        _Select = _Select + _ExtNum;
                }
                _Select++;
                Sub = xmlParser.getLanguage(Sub);
                if (isExt == 0 && Sub.equals("-")) {
                    return _Select + "/" + _SubNum + " " + getResources().getString(R.string.subintitle) + " " + SubFormat;
                } else if (isExt == 0 && (!Sub.isEmpty())) {
                    return _Select + "/" + _SubNum + " " + getResources().getString(R.string.subintitle) + " " + SubFormat + " [ " + Sub + " ]";
                } else if (isExt == 1 && Sub.equals("-")) {
                    return _Select + "/" + _SubNum + " " + getResources().getString(R.string.subexttitle) + " " + SubFormat;
                } else if (isExt == 1 && (!Sub.isEmpty())) {
                    return _Select + "/" + _SubNum + " " + getResources().getString(R.string.subexttitle) + " " + SubFormat + " [ " + Sub + " ]";
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            if (mMediaPlayer == null) {
                return null;
            }
            return TrackHandler.HANDLER.getCurrTrackName(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
        }
    }

    public int getSubtitleNumber() {
        return mSubtitleNumber;
    }

    public void setSubtitleNumber(int pSubtitleNumber) {
        mSubtitleNumber = pSubtitleNumber;
    }

    public int getExtSubtitleNumber() {
        return mExtSubtitleNumber;
    }

    public void setExtSubtitleNumber(int pExtSubtitleNumber) {
        mExtSubtitleNumber = pExtSubtitleNumber;
    }

    public int getSelectSubtitleId() {
        return mSelectSubtitleId;
    }

    public void setSelectSubtitleId(int pSelectSubtitleId) {
        mSelectSubtitleId = pSelectSubtitleId;
    }

    public List<String> getSubtitleLanguageList() {
        return mSubtitleLanguageList;
    }

    public void setSubtitleLanguageList(List<String> pSubtitleLanguageList) {
        mSubtitleLanguageList = pSubtitleLanguageList;
    }

    public List<String> getExtraSubtitleList() {
        return mExtraSubtitleList;
    }

    public void setExtraSubtitleList(List<String> pExtraSubtitleList) {
        mExtraSubtitleList = pExtraSubtitleList;
    }

    public int enableSubtitle(int enable) {
        LogTool.d(TAG, "enableSubtitle: " + enable);
        if ((mMediaPlayer != null) && mIsPrepared) {
            sharedPreferencesOpration(Constants.SHARED, "subtitle_show_tag", enable, 0, true);
            return enableSubtitleInvoke(enable);
        }
        return -1;
    }

    public int sharedPreferencesOpration(String name, String key, int value, int defaultValue, boolean isEdit) {
        if (isEdit) {
            SharedPreferences.Editor editor = mContext.getSharedPreferences(name, mContext.MODE_PRIVATE).edit();
            editor.putInt(key, value);
            editor.commit();
            return 0;
        } else {
            return mContext.getSharedPreferences(name, Context.MODE_PRIVATE).getInt(key, defaultValue);
        }
    }

    public int enableSubtitleInvoke(int enable) {
        if (PlayerJugdment.isHiPlayer()) {
            int flag = HiMediaPlayerInvoke.CMD_SET_SUB_DISABLE;
            return excuteCommand(flag, enable, false);
        } else {
            if (enable == 1) {
                LogTool.d(HisiVideoView.class.getSimpleName(), "enable subtitle deselectTrack");
                int currTrackId = TrackHandler.HANDLER.getCurrTrackId(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
                if (currTrackId != -1) {
                    subTitleId = currTrackId;
                    TrackHandler.HANDLER.selectOrDeselectTrack(mMediaPlayer, false, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE, currTrackId);
                }
            } else {
                LogTool.d(HisiVideoView.class.getSimpleName(), "enable subtitle selectTrack");
                if (subTitleId != -1) {
                    TrackHandler.HANDLER.selectOrDeselectTrack(mMediaPlayer, true, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE, subTitleId);
                }
            }
            return 1;
        }
    }

    public int setSubVertical(int position) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return setSubVerticalInvoke(position);
        }

        return -1;
    }

    public int setSubVerticalInvoke(int position) {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_FONT_VERTICAL;

        return excuteCommand(flag, position, false);
    }


    public int getSubtitleId() {
        if (PlayerJugdment.isHiPlayer()) {
            if ((mMediaPlayer != null) && mIsPrepared) {
                Parcel Request = Parcel.obtain();
                Parcel Reply = Parcel.obtain();

                Request.writeInterfaceToken(IMEDIA_PLAYER);
                Request.writeInt(HiMediaPlayerInvoke.CMD_GET_SUB_ID);

                invoke(Request, Reply);

                Reply.readInt();
                int Result = Reply.readInt();

                Request.recycle();
                Reply.recycle();

                return Result;
            }
        } else {
            int currTrackId = TrackHandler.HANDLER.getCurrTrackId(mMediaPlayer, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
            LogTool.d(TAG, " Curr Track Id : " + currTrackId);
            return currTrackId;
        }
        return -1;
    }

    public int setSubtitleId(int pSubtitleId) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return setSubTrack(pSubtitleId);
        }

        return -1;
    }

    public int setSubTrack(int track) {
        if (PlayerJugdment.isHiPlayer()) {
            int flag = HiMediaPlayerInvoke.CMD_SET_SUB_ID;
            return excuteCommand(flag, track, false);
        } else {
            return TrackHandler.HANDLER.selectTrack(mMediaPlayer, track, MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_SUBTITLE);
        }
    }


    public int setSubtitlePath(String pPath) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            return setSubPath(pPath);
        }

        return -1;
    }

    public int setSubPath(String path) {
        int flag = HiMediaPlayerInvoke.CMD_SET_SUB_EXTRA_SUBNAME;
        Parcel Request = Parcel.obtain();
        Parcel Reply = Parcel.obtain();

        Request.writeInterfaceToken(IMEDIA_PLAYER);
        Request.writeInt(flag);
        Request.writeString(path);

        invoke(Request, Reply);

        int Result = Reply.readInt();

        Request.recycle();
        Reply.recycle();

        return Result;
    }

    public boolean canPause() {
        return false;
    }

    public boolean canSeekBackward() {
        return false;
    }

    public boolean canSeekForward() {
        return false;
    }

    public void destroyPlayer() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }

        if (mMediaPlayer != null) {
            LogTool.d(TAG, "destroyPlayer ");
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
            LogTool.d(TAG, "destroyPlayer finish");
        }
    }

    public int getAudioSessionId() {
        return 0;
    }


    public int setHDRInvoke(int mode)// 0 : sdr display mode  1: hdr display mode.
    {
        int flag = HiMediaPlayerInvoke.CMD_SET_HDR_MODE;

        return excuteCommand(flag, mode, false);
    }

    /**
     * Set videoFmt according to the last setting if not set to 0
     */
    public int resetVideoFmt() {
        return setStereoVideoFmt(inVideoFmt);
    }


    boolean mInPipMode;

    public void setInPipMode(boolean inPipMode) {
        mInPipMode = inPipMode;
    }

    public void setmVideoCallback(HisiVideoCallback callback) {
        this.mVideoCallback = callback;
    }

    private HisiVideoCallback mVideoCallback;

    public interface HisiVideoCallback {
        void onFileNotFound();
    }


    public int getDraRawChannel() {
        int ret = -1;
        if (mMediaPlayer != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(HiMediaPlayerInvoke.CMD_GET_CHANNEL_MODE);
            invoke(request, reply);
            ret = reply.readInt();
            request.recycle();
            reply.recycle();
        }
        return ret;
    }

    public void setChangeableVideoScale(int width, int height, int screenWidth, int screenHeight) {


        LayoutParams lp = getLayoutParams();

        lp.width = width;
        mVideoWidth = width;
        lp.height = height;
        mVideoHeight = height;
        setLayoutParams(lp);
        setLayoutLocation((screenWidth - width) / 2, (screenHeight - height) / 2);
        invalidate();
    }

    public void setLayoutLocation(int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(margin);
        setLayoutParams(layoutParams);
    }


    public int setDTSDRC(int able) {
        if ((mMediaPlayer != null) && mIsPrepared) {
            Parcel Request = Parcel.obtain();
            Parcel Reply = Parcel.obtain();
            Request.writeInterfaceToken(IMEDIA_PLAYER);
            Request.writeInt(HiMediaPlayerInvoke.CMD_SET_DTS_DRCMODE);
            Request.writeInt(able);
            invoke(Request, Reply);
            Reply.readInt();
            int Result = Reply.readInt();
            Request.recycle();
            Reply.recycle();
            return Result;
        }
        return -1;
    }

    public int setVideoRect(int x, int y, int videoWidth, int videoHeight) {
        int ret = -1;
        if (mMediaPlayer != null) {
            Parcel request = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            request.writeInterfaceToken(IMEDIA_PLAYER);
            request.writeInt(HiMediaPlayerInvoke.CMD_SET_WIN_INPUT_RECT);
            request.writeInt(x);
            request.writeInt(y);
            request.writeInt(videoWidth);
            request.writeInt(videoHeight);
            invoke(request, reply);
            reply.setDataPosition(0);
            ret = reply.readInt();
            if (ret != 0)
                LogTool.e(TAG, "setVideoRectInvoke Failed !");
            request.recycle();
            reply.recycle();
        }
        return ret;
    }

    public MediaPlayer getMediaPlayer() {
        return mMediaPlayer;
    }


    public int getDefaultHeight() {
        return defaultHeight;
    }

    public void setDefaultHeight(int defaultHeight) {
        this.defaultHeight = defaultHeight;
    }

    public int getDefaultWidth() {
        return defaultWidth;
    }

    public void setDefaultWidth(int defaultWidth) {
        this.defaultWidth = defaultWidth;
    }


    /**
     * 获取Dolby Atmos Flag
     *
     * @return
     */
    public DoblyResult getDolbyAtmos() {
        if (mMediaPlayer == null) {
            return new DoblyResult();
        }

        Parcel request = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        request.writeInterfaceToken(IMEDIA_PLAYER);
        request.writeInt(HiMediaPlayerInvoke.CMD_GET_DOLBY_AUDIO_FLAG);
        invoke(request, reply);
        reply.setDataPosition(0);
        int err_flag = reply.readInt();
        if (err_flag != 0) {
            DoblyResult resut = new DoblyResult();
            resut.setError_flag(err_flag);
            return resut;
        }
        int audio_flag = reply.readInt();
        int atmos_flag = reply.readInt();
        DoblyResult doblyResult = new DoblyResult();
        doblyResult.setAtmos_flag(atmos_flag);
        doblyResult.setAudio_flag(audio_flag);
        doblyResult.setError_flag(0);
        request.recycle();
        reply.recycle();
        return doblyResult;
    }

    public void clearDraw() {
        boolean isBlack = "true".equals(SystemProperties.get("media.hp.switch.black", "true"));
        if (PlayerJugdment.isGstreamer() && isBlack) {
            LogTool.d(TAG, "clearDraw start");
            setVisibility(GONE);
            setVisibility(VISIBLE);
            LogTool.d(TAG, "clearDraw finish");
        }
    }
}
