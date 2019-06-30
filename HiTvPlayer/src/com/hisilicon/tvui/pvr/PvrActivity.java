package com.hisilicon.tvui.pvr;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.network.service.SubtitleComponent;
import com.hisilicon.dtv.network.service.TeletextComponent;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.dtv.play.EnTrickMode;
import com.hisilicon.dtv.play.TeletextControl;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.dtv.record.PVREncryption;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.play.TipMsgView;
import com.hisilicon.tvui.play.audio.AudioDialog;
import com.hisilicon.tvui.play.audio.AudioSelectorDialog;
import com.hisilicon.tvui.play.cc.ClosedCaptionDialog;
import com.hisilicon.tvui.play.subtitle.SubtitleDialog;
import com.hisilicon.tvui.play.subtitle.SubtitleSelectorDialog;
import com.hisilicon.tvui.play.teletext.TeletextDialog;
import com.hisilicon.tvui.record.RecordedFile;
import com.hisilicon.tvui.record.RecordingListActivity;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.DeviceInformation;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.tvui.hal.halApi;

/**
 * PVR Player PVR播放器 This player can play the recorded video and radio programs.
 * Its main functions are switching programs, fast-forward, rewind, play /
 * pause, switching tracks, switching subtitles, display information.
 * 可以播放录制的视频和广播节目。具有切上一首，切下一首，快进，快退，播放/暂停，切音轨，切字幕，显示播放信息的功能。
 * @author h00217063
 * @since 1.0
 */

public class PvrActivity extends Activity implements SurfaceHolder.Callback, IDTVListener
{
    private static final String TAG = "PvrActivity";
    private static final int SEEK_TIME =30;

    /** context */
    /** 上下文 */
    private final Context context = PvrActivity.this;

    /** prev button */
    /** 上30S */
    private ImageButton mPreBtn;

    /** next button */
    /** 下30S */
    private ImageButton mNextBtn;

    /** rewind button */
    /** 快退键 */
    private ImageButton mRewindBtn;

    /** fast-froward button */
    /** 快进键 */
    private ImageButton mForwardBtn;

    /** play/pause button */
    /** 播放暂停键 */
    private ImageButton mPlayPauseBtn;

    /** stop button */
    /** 停止键 */
    private ImageButton mStopBtn;

    /** subtitle button */
    /** 字幕键 */
    private ImageButton mSubBtn;

    /** track button */
    /** 音轨键 */
    private ImageButton mTrackBtn;

    /** info button */
    /** 视频信息 */
    private ImageButton mInfoBtn;

    /** slowplay_forward button */
    /**慢进*/
    private ImageButton mSlowRelease;

    /** seek */
    private ImageButton mSeek;

    /** seekBar */
    /** 进度条 */
    private SeekBar mSeekBar;

    /** the textView of current play time */
    /** 当前播放时间文本 */
    private TextView mPlayTimeView;

    /** the textView of total play time */
    /** 总播放时间文本 */
    private TextView mTotalTimeView;

    /** play rate */
    /** 快速播放进度提示 */
    private TextView mRateView;

    /** current play time */
    /** 当前播放时间 */
    private int mPlayTime;

    /** total play time */
    /** 当前播放总时间 */
    private int mTotalTime;

    /** rewind Or fast-forward */
    /** 是否快进快退中 */
    private boolean rewindOrForward = false;

    private boolean isPlaying = false;

    private boolean isFinishPlayed = false;

    /** timer */
    /** 定时器，用于隐藏搜索栏和导航栏 */
    private Timer mHideViewTimer;

    /** media controller */
    /** 控制条 */
    private RelativeLayout mNavigationLayout;

    /** focus view */
    /** 当前焦点View */
    private View focusView;

    private ArrayList<RecordedFile> mRecordFileList;

    /** video patch */
    /** 视频路径 */
    private String mVideoPath;

    private String mRecordingPlayingState;

    /** SharedPreference */

    /** track dialog */
    /** 音轨对话框 */
    private AudioSelectorDialog trackDialog = null;

    /** subtitle dialog */
    /** 字幕对话框 */
    private SubtitleSelectorDialog mSubtitleDialog = null;

    /** PREVIOUS_NEXT_VIDEO */
    /** 前一首或后一首 */
    private static final int PREVIOUS_NEXT_VIDEO = 0;

    private static final int CLEAR_PROGRESS_DIALOG = 8;

    /** the textview of seek time */
    /** seek时间文本 */
    private TextView mSeekTimeView;

    /** seek step */
    /** 虚进虚退的步长 */
    private int mStep = 1;

    /** progress dialog */
    /** 加载对话框 */
    private ProgressDialog progressDialog;

    private PvrSeekDialog mPvrSeekDialog;
    private PVRFilePlayer mPlayer;

    private LinearLayout mPvrRadioBackgroud = null;
    private LinearLayout mPvrPCBackgroud = null;

    private DTV mDTV;

    private SurfaceView mVideoView;

    private static int mLastPlayPos = 0;

    private boolean mBackFlag = false;

    private TipMsgView mTipMsgView = null;

    public DTVConfig mDtvConfig = null;

    private PVREncryption mPvrEncryption;
    /**
     * show progress dialog 显示加载对话框
     * @return void
     */
    private void showProgressDialog()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
            progressDialog = null;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(getResources().getString(R.string.dialog_title_loading));
        progressDialog.setMessage(getResources().getString(R.string.dialog_message_loading));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(2000);
        progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener()
        {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if (keyCode == KeyValue.DTV_KEYVALUE_BACK)
                {
                    return true;
                }

                return false;
            }
        });
        progressDialog.show();

        //DialogTool.disableBackgroundDim(progressDialog);
    }

    /**
     * transfer second to string
     * 把秒数转换成时分秒字符串
     * @param mill
     * @return string
     */
    private String formatMill(int mill)
    {
        int duration = mill;

        int _sec = duration % 60;

        duration = duration / 60;
        int _min = duration % 60;

        duration = duration / 60;
        int _hour = duration;

        String _strHour = (_hour < 10) ? "0" + _hour : "" + _hour;
        String _strMin = (_min < 10) ? "0" + _min : "" + _min;
        String _strSec = (_sec < 10) ? "0" + _sec : "" + _sec;

        String _strTime = _strHour + ":" + _strMin + ":" + _strSec;

        return _strTime;
    }

    private void enableRewindForward(boolean bEnable)
    {
        if (mForwardBtn != null)
        {
            mForwardBtn.setFocusable(bEnable);
            if (bEnable)
            {
                mForwardBtn.setImageResource(R.drawable.icon_forward_normal);
            }
            else
            {
                mForwardBtn.setImageResource(R.drawable.icon_forward_nouse);
            }
        }

        if (mRewindBtn != null)
        {
            mRewindBtn.setFocusable(bEnable);
            if (bEnable)
            {
                mRewindBtn.setImageResource(R.drawable.icon_rewind_normal);
            }
            else
            {
                mRewindBtn.setImageResource(R.drawable.icon_rewind_nouse);
            }
        }
    }

    private void initPVREncryption() {
        mPvrEncryption = new PVREncryption(PVREncryption.PVR_ENCRYPTION_TYPE_AES, DeviceInformation.getDeviceMac());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        LogTool.i(LogTool.MPLAY, "===== onCreate =====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_activity);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        initPVREncryption();
        initNavigation();
        showProgressDialog();
        initVariable();
        initVideoPlayer();
        mPvrRadioBackgroud = (LinearLayout) findViewById(R.id.video_radio_bg);
        mHandler.sendEmptyMessage(Constant.MSG_UPDATE_BAR);
        resetHideViewTimer(Constant.HIDE_DELAY);
        mLastPlayPos = 0;

    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case PREVIOUS_NEXT_VIDEO:
            {
                if (isFinishPlayed)
                {
                    mPlayer.open();
                }
                else
                {
                    mPlayer.stop();
                }

                if (0 == mPlayer.start(mVideoPath, mPvrEncryption))
                {
                    mStartTime = -1;
                    mPvrRadioBackgroud = (LinearLayout) findViewById(R.id.video_radio_bg);
                    if (null != mPvrRadioBackgroud)
                    {
                        if (mPlayer.getPVRFileInfo().isRadio())
                        {
                            mPvrRadioBackgroud.setVisibility(View.VISIBLE);
                            enableRewindForward(false);
                        }
                        else
                        {
                            mPvrRadioBackgroud.setVisibility(View.INVISIBLE);
                            enableRewindForward(true);
                        }
                    }

                    //                        handler.sendEmptyMessage(CLEAR_PROGRESS_DIALOG);
                    isPlaying = true;
                    isFinishPlayed = false;
                    mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                    mTotalTime = mPlayer.getPVRFileInfo().getDuration();
                    mTotalTimeView.setText(formatMill(mTotalTime));
                }

                progressDialog.dismiss();
                progressDialog = null;
                break;
            }
            default:
            {
                LogTool.v(LogTool.MPLAY, "Enter handleMessgae others");
                break;
            }
            }
        }
    };

    /**
     * initialize Variables 初始化变量
     * @return void
     */
    private void initVariable()
    {
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mDtvConfig = mDTV.getConfig();
        mPlayer = mDTV.getPVRFilePlayer();
        mTipMsgView=new TipMsgView(this);
        mPlayer.open();
        mRecordFileList = RecordingListActivity.GetRecordedFileList();
    }

    /**
     * initialize video player 初始化播放器
     * @return void
     */
    public void initVideoPlayer()
    {
        Intent intent = getIntent();

        mVideoPath = intent.getStringExtra("VideoPath");
        mRecordingPlayingState = intent.getStringExtra("RecordingPlayingState");
        if(isSideRecordingAndPlaying())
        {
            mPreBtn.setEnabled(false);
            mPreBtn.setFocusable(false);
            mNextBtn.setEnabled(false);
            mNextBtn.setFocusable(false);
        }

        mVideoView = (SurfaceView) findViewById(R.id.video_surface);
        SurfaceHolder mSubSurfaceHolder = mVideoView.getHolder();

        mVideoView.setVisibility(View.VISIBLE);

        mSubSurfaceHolder.addCallback(this);
        if (0 == mPlayer.start(mVideoPath, mPvrEncryption))
        {
            mStartTime = -1;
            mPvrRadioBackgroud = (LinearLayout) findViewById(R.id.video_radio_bg);
            if (null != mPvrRadioBackgroud)
            {
                if (mPlayer.getPVRFileInfo().isRadio())
                {
                    mPvrRadioBackgroud.setVisibility(View.VISIBLE);
                    enableRewindForward(false);
                }
                else
                {
                    mPvrRadioBackgroud.setVisibility(View.INVISIBLE);
                    enableRewindForward(true);
                }
            }

            isPlaying = true;
            mTotalTime = mPlayer.getPVRFileInfo().getDuration();
            mTotalTimeView.setText(formatMill(mTotalTime));
        }

        progressDialog.dismiss();
        progressDialog = null;
        handler.sendEmptyMessage(CLEAR_PROGRESS_DIALOG);
    }

    @Override
    public void notifyMessage(int messageID, int param1, int param2, Object obj) {
        LogTool.d(LogTool.MPLAY, "messageID v:" + messageID+";param1="+param1+";param2="+param2);
        switch (messageID) {
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF: {
                if (null != mSeekBar) {
                    if (mTotalTime > 0) {
                        mSeekBar.setProgress(Constant.MAX_PROGRESS);
                    }
                }

                // dismiss track dialog
                // 消除音轨对话框
                if (null != trackDialog) {
                    trackDialog.dismiss();
                    trackDialog = null;
                }

                // dismiss subtitle dialog
                // 消除字幕对话框
                if (null != mSubtitleDialog) {
                    mSubtitleDialog.dismiss();
                    mSubtitleDialog = null;
                }

                // initialize secondary progress
                // 初始化第二级进度条
                initSecondaryProgress();
                if (0 == mPlayer.stop()) {
                    isPlaying = false;
                    isFinishPlayed = true;
                    mPlayer.close();
                }

                mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
                mRateView.setVisibility(View.INVISIBLE);
                rewindOrForward = false;
                finish();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR: {
                if ((null != trackDialog) && (trackDialog.isShowing())) {
                    trackDialog.dismiss();
                }

                if ((null != mSubtitleDialog) && (mSubtitleDialog.isShowing())) {
                    mSubtitleDialog.dismiss();
                }

                if ((null != progressDialog) && (progressDialog.isShowing())) {
                    progressDialog.dismiss();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.sorry);
                builder.setMessage(R.string.loading_error);
                builder.setPositiveButton(R.string.confirm, null);
                Dialog loadErrorDialog = builder.create();
                loadErrorDialog.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                });

                loadErrorDialog.show();
                mPlayer.stop();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF: {
                mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                mRateView.setVisibility(View.INVISIBLE);
                rewindOrForward = false;
                mPlayer.resume();
            }
            break;
            case DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING: {//start parental
                if(checkPvrPCLock()){
                    mTipMsgView.pvrPClockshow(TipMsgView.TIPMSG_PARENTAL_RATING);
                    setmPvrPCBackgroud(true);//black ground
                    hideMediaController();
                    mediaPlayPause();
                }
            }
            break;
        }

    }

    /**
     * 检查是否需要弹出父母锁
     * @return
     */
    private boolean checkPvrPCLock() {
        // 父母锁已解锁过
        if (halApi.getPwdStatus(halApi.EnumLockType.PARENTAL_LOCK_TYPE)) {
            LogTool.i(LogTool.MPLAY, "PARENTAL_LOCK_TYPE_OK");
            return false;
        }
        //已经显示了锁
        if (mTipMsgView.isShow(TipMsgView.TIPMSG_SOURCE_LOCK)
                || mTipMsgView.isShow(TipMsgView.TIPMSG_PARENTAL_RATING)) {
            LogTool.i(LogTool.MPLAY, "TIPMSG_PARENTAL_RATING");
            return false;
        }

        boolean bEqualBlock = false;
        Channel mPlayerCurrentChannel = mPlayer.getCurrentChannel();
        if (mPlayerCurrentChannel == null || mDtvConfig == null) {
            LogTool.i(LogTool.MPLAY, "mPlayerCurrentChannel null");
            return false;
        }
        String strCountry = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "");
        int id = mPlayerCurrentChannel.getChannelID();
        ParentalControlManager mPCManager = mDTV.getParentalControlManager();
        if (mPCManager == null) {
            LogTool.i(LogTool.MPLAY, "mPCManager null");
            return false;
        }
        int userParentalRating = mPCManager.getParentLockAge();
        int parentalRating = mPCManager.getParental(id);
        LogTool.i(LogTool.MPLAY, "parentalRating=" + parentalRating + ";userParentalRating=" + userParentalRating);
        if ((0 == parentalRating) || (0 == userParentalRating)) {
            return false;
        }
        if (null != strCountry) {
            if (strCountry.equalsIgnoreCase("MYS") || strCountry.equalsIgnoreCase("IDN") || strCountry.equalsIgnoreCase("NZL")
                    || strCountry.equalsIgnoreCase("SGP") || strCountry.equalsIgnoreCase("THA") || strCountry.equalsIgnoreCase("VNM")
                    || strCountry.equalsIgnoreCase("BRA") || strCountry.equalsIgnoreCase("RUS")) {
                bEqualBlock = true;
            } else {
                bEqualBlock = false;
            }
        }

        if (parentalRating > userParentalRating) {
            return true;
        } else if ((parentalRating == userParentalRating) && (bEqualBlock)) {
            return true;
        }

        return false;
    }

    /**
     * initialize navigations 初始化控制条各控件
     * @return void
     */
    public void initNavigation()
    {
        mSeekTimeView = (TextView) findViewById(R.id.media_seek_time);
        mSeekTimeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);

        // mSeekTimeView.setTextSize(15);
        mSeekBar = (SeekBar) findViewById(R.id.media_seekbar);
        mPlayTimeView = (TextView) findViewById(R.id.media_play_time);
        mPlayTimeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
        mTotalTimeView = (TextView) findViewById(R.id.media_total_time);
        mTotalTimeView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
        mRateView = (TextView) findViewById(R.id.media_rate);
        mRateView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 15);
        mNavigationLayout = (RelativeLayout) findViewById(R.id.layout_media_navigation);
        mPreBtn = (ImageButton) findViewById(R.id.media_pre);
        mNextBtn = (ImageButton) findViewById(R.id.media_next);
        mRewindBtn = (ImageButton) findViewById(R.id.media_rewind);
        mForwardBtn = (ImageButton) findViewById(R.id.media_forward);
        mPlayPauseBtn = (ImageButton) findViewById(R.id.media_pause);
        mStopBtn = (ImageButton) findViewById(R.id.media_stop);
        mSubBtn = (ImageButton) findViewById(R.id.media_sub);
        mTrackBtn = (ImageButton) findViewById(R.id.media_track);
        mInfoBtn = (ImageButton) findViewById(R.id.media_info);
        mPvrPCBackgroud =(LinearLayout) findViewById(R.id.video_PClock_bg);
        mSlowRelease=(ImageButton)findViewById(R.id.media_slowRelease);
        mSeek=(ImageButton)findViewById(R.id.media_seek_pre);
        mPlayPauseBtn.requestFocus();

        mRewindBtn.setOnClickListener(clickListener);
        mForwardBtn.setOnClickListener(clickListener);
        mPreBtn.setOnClickListener(clickListener);
        mNextBtn.setOnClickListener(clickListener);
        mPlayPauseBtn.setOnClickListener(clickListener);
        mStopBtn.setOnClickListener(clickListener);
        mSubBtn.setOnClickListener(clickListener);
        mTrackBtn.setOnClickListener(clickListener);
        mInfoBtn.setOnClickListener(clickListener);
        mSeekBar.setOnSeekBarChangeListener(seekBarChange);

        mSeek.setOnClickListener(clickListener);
        mSlowRelease.setOnClickListener(clickListener);

        focusView = getCurrentFocus();
    }

    /** on seekbar change listener */
    /** 进度条变化监听 */
    OnSeekBarChangeListener seekBarChange = new OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
        {
            if (fromUser)
            {
                resetHideViewTimer(Constant.HIDE_DELAY);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }
    };

    /** on click listener */
    /** 按键监听 */
    OnClickListener clickListener = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            resetHideViewTimer(Constant.HIDE_DELAY);
            LogTool.d(LogTool.MPLAY, "clickListener v:" + v);
            if (v.getId() == mPlayPauseBtn.getId())
            {
                mediaPlayPause();
            }
            else if (v.getId() == mRewindBtn.getId())
            {
                rewind();

            }
            else if (v.getId() == mForwardBtn.getId())
            {
                forward();
            }
            else if (v.getId() == mPreBtn.getId())
            {
                seekToPre();
            }
            else if (v.getId() == mNextBtn.getId())
            {
                seekToNext();
            }
            else if (v.getId() == mStopBtn.getId())
            {
                mPlayer.stop();
                finish();
            }
            else if (v.getId() == mSubBtn.getId())
            {
                setSubDialog();
            }
            else if (v.getId() == mTrackBtn.getId())
            {
                setTrackDialog();
            }
            else if (v.getId() == mInfoBtn.getId())
            {
                showMediaInfo();
            } else if (v.getId() == mSlowRelease.getId()) { //slowPlay
                slowNextForward();
            } else if (v.getId() == mSeek.getId()) {//seek
                setSeekDialog();
            }
        }
    };

    private void setSeekDialog() {
        if (null != mPlayer) {
            mPvrSeekDialog = new PvrSeekDialog(PvrActivity.this, R.style.DIM_STYLE, mPlayer);
            mPvrSeekDialog.show();
        }
    }
    private void setSubDialog()
    {
        if (null != mPlayer)
        {
            List<SubtitleComponent> mSubtitelList = mPlayer.getSubtitleComponents();
            if (null != mSubtitelList)
            {
                mSubtitleDialog = new SubtitleSelectorDialog(PvrActivity.this, R.style.dialog_transparent, SubtitleDialog.subtDialogTypePVR);
                mSubtitleDialog.show();
            }
            else
            {
                MyToast.makeText(this, R.string.no_subtitle, MyToast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * set track dialog 设置音轨对话框
     * @return void
     */
    private void setTrackDialog()
    {
        if (null != mPlayer)
        {
            List<AudioComponent> audiolList = mPlayer.getPVRFileInfo().getAudioComponents();
            if (null != audiolList)
            {
                trackDialog = new AudioSelectorDialog(PvrActivity.this, audiolList, mPlayer);
                trackDialog.show();
            }
            else
            {
                MyToast.makeText(this, R.string.no_soundtrack, MyToast.LENGTH_LONG).show();
            }
        }
    }

    /** timer operations(update media controller,hide media controller) */
    /** 定时操作(更新进度条,隐藏控制条) */
    Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case Constant.MSG_UPDATE_BAR:
                mediaUpdateBar();
                msg = obtainMessage(Constant.MSG_UPDATE_BAR);
                sendMessageDelayed(msg, 1000);
                if (rewindOrForward || !isPlaying)
                {
                    resetHideViewTimer(Constant.HIDE_DELAY);
                }
                break;

            case Constant.MSG_HIDE_MEDIA_CONTROLLER:
                initSecondaryProgress();
                hideMediaController();
                break;

            case Constant.MSG_HIDE_DIALOG:
                if (null != trackDialog)
                {
                    trackDialog.cancel();
                }

                if (null != mSubtitleDialog)
                {
                    mSubtitleDialog.cancel();
                }

                break;
            default:
                break;
            }

            super.handleMessage(msg);
        }
    };

    /** reset hiding media controller timer */
    /** 重置隐藏进度条的timer */
    public void resetHideViewTimer(long period)
    {
        if (mHideViewTimer != null)
        {
            mHideViewTimer.cancel();
            mHideViewTimer = null;
        }
        try
        {
            mHideViewTimer = new Timer();
            mHideViewTimer.scheduleAtFixedRate(new TimerTask()
            {
                @Override
                public void run()
                {
                    if (mHandler != null)
                    {
                        mHandler.sendEmptyMessage(Constant.MSG_HIDE_MEDIA_CONTROLLER);
                    }
                }
            }, period, period);
        }
        catch (IllegalArgumentException e)
        {
            LogTool.e(LogTool.MPLAY, e.toString());
        }
        catch (IllegalStateException e)
        {
            LogTool.e(LogTool.MPLAY, e.toString());
        }
    }

    /**
     * forward operation 快进
     * @return void
     */
    private void forward()
    {
        EnTrickMode trickMode = mPlayer.getCurrentTrickMode();
        EnTrickMode nextTrickMode = null;

        switch (trickMode)
        {
        case FAST_FORWARD_TWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_FOUR;
            break;
        }
        case FAST_FORWARD_FOUR:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_EIGHT;
            break;
        }
        case FAST_FORWARD_EIGHT:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_SIXTEEN;
            break;
        }
        case FAST_FORWARD_SIXTEEN:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_THIRTYTWO;
            break;
        }
        case FAST_FORWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
            break;
        }
        case FAST_FORWARD_NORMAL:
        case FAST_BACKWARD_TWO:
        case FAST_BACKWARD_FOUR:
        case FAST_BACKWARD_EIGHT:
        case FAST_BACKWARD_SIXTEEN:
        case FAST_BACKWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_TWO;
            break;
        }
        default:
            break;
        }

        if (null == nextTrickMode)
        {
            return;
        }

        if (0 == mPlayer.trickPlay(nextTrickMode))
        {
            if (!mRateView.isShown())
            {
                mRateView.setVisibility(View.VISIBLE);
            }

            if (EnTrickMode.FAST_FORWARD_NORMAL != nextTrickMode)
            {
                mRateView.setText(nextTrickMode.getValue() / 1024 + "X");
                mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
                rewindOrForward = true;
            }
            else
            {
                mRateView.setText("");
                mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                rewindOrForward = false;
            }
        }
    }

    /**
     * rewind operation 快退
     * @return void
     */
    private void rewind()
    {
        EnTrickMode trickMode = mPlayer.getCurrentTrickMode();
        EnTrickMode nextTrickMode = null;

        switch (trickMode)
        {
        case FAST_BACKWARD_TWO:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_FOUR;
            break;
        }
        case FAST_BACKWARD_FOUR:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_EIGHT;
            break;
        }
        case FAST_BACKWARD_EIGHT:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_SIXTEEN;
            break;
        }
        case FAST_BACKWARD_SIXTEEN:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_THIRTYTWO;
            break;
        }
        case FAST_BACKWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
            break;
        }
        case FAST_FORWARD_NORMAL:
        case FAST_FORWARD_TWO:
        case FAST_FORWARD_FOUR:
        case FAST_FORWARD_EIGHT:
        case FAST_FORWARD_SIXTEEN:
        case FAST_FORWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            break;
        }
        default:
            break;
        }

        if (null == nextTrickMode)
        {
            return;
        }

        if (0 == mPlayer.trickPlay(nextTrickMode))
        {
            if (!mRateView.isShown())
            {
                mRateView.setVisibility(View.VISIBLE);
            }

            if (EnTrickMode.FAST_FORWARD_NORMAL != nextTrickMode)
            {
                mRateView.setText(nextTrickMode.getValue() / 1024 + "X");
                mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
                rewindOrForward = true;
            }
            else
            {
                mRateView.setText("");
                mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                rewindOrForward = false;
            }
        }
    }

    private String getPreNextVideo(String currentVideoPath, Boolean bNext)
    {
        int i = 0;

        for (i = 0; i < mRecordFileList.size(); i++)
        {
            if (currentVideoPath.equals(mRecordFileList.get(i).getFilePath()))
            {
                break;
            }
        }

        if (bNext)
        {
            i = (i + mRecordFileList.size() + 1) % mRecordFileList.size();
        }
        else
        {
            i = (i + mRecordFileList.size() - 1) % mRecordFileList.size();
        }

        return mRecordFileList.get(i).getFilePath();
    }

    /**
     * play tne next video 播放下一首
     * @return void
     */
    private void nextVideo()
    {
        handler.removeMessages(PREVIOUS_NEXT_VIDEO);
        initSecondaryProgress();
        showProgressDialog();
        mRateView.setVisibility(View.INVISIBLE);
        mVideoPath = getPreNextVideo(mVideoPath, true);
        LogTool.v(LogTool.MPLAY, "videoPath = " + mVideoPath);

        LogTool.v(LogTool.MPLAY, "message.what = PREVIOUS_NEXT_VIDEO;");
        handler.sendEmptyMessageDelayed(PREVIOUS_NEXT_VIDEO, 500);
    }

    /**
     * play the prev video 播放上一首
     * @return void
     */
    private void preVideo()
    {
        handler.removeMessages(PREVIOUS_NEXT_VIDEO);
        initSecondaryProgress();
        showProgressDialog();
        mRateView.setVisibility(View.INVISIBLE);
        mVideoPath = getPreNextVideo(mVideoPath, false);
        LogTool.v(LogTool.MPLAY, "videoPath = " + mVideoPath);

        LogTool.v(LogTool.MPLAY, "message.what = PREVIOUS_NEXT_VIDEO;");
        handler.sendEmptyMessageDelayed(PREVIOUS_NEXT_VIDEO, 500);
    }

    /**
     * show the video info 显示视频信息
     * @return void
     */
    private void showMediaInfo()
    {
        String fileName = mVideoPath.substring(mVideoPath.lastIndexOf("/") + 1, mVideoPath.length());

        MyToast.makeText(this, getResources().getString(R.string.media_title_str) + fileName, MyToast.LENGTH_LONG).show();
    }

    public void setmPvrPCBackgroud(boolean isPCLock) {
        if (isPCLock) {
            mPvrPCBackgroud.setVisibility(View.VISIBLE);
        } else {
            mPvrPCBackgroud.setVisibility(View.GONE);
        }
  }
    /**
     * play/pause operations 播放暂停
     * @return void
     */
    public void mediaPlayPause()
    {
        if (rewindOrForward)
        {
            mRateView.setVisibility(View.INVISIBLE);
            rewindOrForward = false;
            mPlayer.resume();
            mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
        }
        else
        {
            if (isPlaying)
            {
                mPlayer.pause();
                mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
                isPlaying = false;
            }
            else
            {
                if (!isFinishPlayed)
                {
                    mPlayer.resume();
                    mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                    isPlaying = true;
                }
                else
                {
                    mPlayer.open();
                    mPlayer.start(mVideoPath, mPvrEncryption);
                    mStartTime = -1;
                    isPlaying = true;
                    mTotalTime = mPlayer.getPVRFileInfo().getDuration();
                    mTotalTimeView.setText(formatMill(mTotalTime));
                    isFinishPlayed = false;
                    mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                }
            }
        }
    }

    /**
     * next 30s
     */
    private void seekToNext(){

        if(0 == mPlayer.trickPlay(EnTrickMode.FAST_FORWARD_NORMAL)){
            mRateView.setVisibility(View.GONE);
            int nowTime=mPlayer.getCurrentPosition();
            int nextTime=nowTime+SEEK_TIME;
            mTotalTime = mPlayer.getPVRFileInfo().getDuration();
            LogTool.d(LogTool.MPLAY, "seekToNext nowTime="+nowTime+";nextTime="+nextTime);
            if(nextTime>mTotalTime){
                finish();
            }else {//执行跳放
                if(0==mPlayer.seekTo(nextTime)){
                    LogTool.d(LogTool.MPLAY, "seekToNext  is ok");
                }else {
                    LogTool.d(LogTool.MPLAY, "seekToNext  error");
                }
            }
        }

    }

    /**
     * pre 30s
     */
    private void seekToPre(){
        if(0 == mPlayer.trickPlay(EnTrickMode.FAST_FORWARD_NORMAL)){
            mRateView.setVisibility(View.GONE);
            int nowTime=mPlayer.getCurrentPosition();
            int preTime=nowTime-SEEK_TIME;
            mTotalTime = mPlayer.getPVRFileInfo().getDuration();
            LogTool.d(LogTool.MPLAY, "seekToPre nowTime="+nowTime+";preTime="+preTime);
            if(preTime<0){
                if(0==mPlayer.seekTo(0)){
                    LogTool.d(LogTool.MPLAY, "seekToPre  is ok");
                }else {
                    LogTool.d(LogTool.MPLAY, "seekToPre  error");
                }
            }else {
                if(0==mPlayer.seekTo(preTime)){
                    LogTool.d(LogTool.MPLAY, "seekToPre  is ok");
                }else {
                    LogTool.d(LogTool.MPLAY, "seekToPre  error");
                }
            }
        }else {
            LogTool.d(LogTool.MPLAY, "seekToPre  error");
        }

     }
    /**
     * slowNextForward
     */
     private void slowNextForward(){

         EnTrickMode trickMode = mPlayer.getCurrentTrickMode();
         EnTrickMode nextTrickMode = null;
         LogTool.d(LogTool.MPLAY, "slowNextForward_trickMode:" + trickMode);
         switch (trickMode)
         {
             case SLOW_FORWARD_TWO:
             {
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_FOUR;
                 break;
             }
             case SLOW_FORWARD_FOUR:
             {
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_EIGHT;
                 break;
             }
             case SLOW_FORWARD_EIGHT:
             {
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_SIXTEEN;
                 break;
             }
             case SLOW_FORWARD_SIXTEEN:
             {
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_THIRTYTWO;
                 break;
             }
             case SLOW_FORWARD_THIRTYTWO:
             {
                 nextTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
                 break;
             }
             case FAST_FORWARD_NORMAL:
             case FAST_FORWARD_TWO:
             case FAST_FORWARD_FOUR:
             case FAST_FORWARD_EIGHT:
             case FAST_FORWARD_SIXTEEN:
             case FAST_FORWARD_THIRTYTWO:
             {
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_TWO;
                 break;
             }
             default:
                 nextTrickMode = EnTrickMode.SLOW_FORWARD_TWO;
                 break;
         }

         LogTool.d(LogTool.MPLAY, "slowForward_nextTrickMode:" + nextTrickMode);
         if (0 == mPlayer.trickPlay(nextTrickMode))
         {
             if (!mRateView.isShown())
             {
                 mRateView.setVisibility(View.VISIBLE);
             }

             if (EnTrickMode.FAST_FORWARD_NORMAL != nextTrickMode)
             {
                 mRateView.setText("1"+"/"+(1024/nextTrickMode.getValue()));
                 mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
                 rewindOrForward = true;
             }
             else
             {
                 mRateView.setText("");
                 mPlayPauseBtn.setImageResource(R.drawable.icon_pause_normal);
                 rewindOrForward = false;
             }
         }else {
             LogTool.d(LogTool.MPLAY, "slowNextForward error");
         }

     }
    /**
     * update media controller 更新进度条
     * @param void
     * @return void
     */
    int mStartTime = -1;

    private void mediaUpdateBar()
    {
        if (mVideoView != null)
        {
            mPlayTime = mPlayer.getCurrentPosition();
            LogTool.d(LogTool.MPLAY, "PvrActivity mPlayTime = "+mPlayTime );
            if (-1 == mStartTime)
            {
                mStartTime = mPlayTime;
            }
            mPlayTime = mPlayTime - mStartTime;
        }

        if(isSideRecordingAndPlaying())
        {
            mTotalTime = mPlayer.getPVRFileInfo().getDuration();
            mTotalTimeView.setText(formatMill(mTotalTime));
        }

        mPlayTimeView.setText(formatMill(mPlayTime));
        if (mSeekBar != null)
        {
            if (mTotalTime > 0)
            {
                int _progress = (int) ((float) mPlayTime / mTotalTime * Constant.MAX_PROGRESS);
                mSeekBar.setProgress(_progress);
            }
        }
    }

    /** the time of advance or retreat */
    /** 虚进虚退的时间 */
    private int progerssFwRwind = -1;

    /** the position of advance or retreat */
    /** 虚进虚退的位置 */
    private int position = -1;

    /**
     * set advance or retreat step 设置虚进或虚退
     * @param step
     * @param isAdvance
     * @return void
     */
    private void advanceAndRetreat(int step, boolean isAdvance)
    {
        if (isAdvance)
        {
            progerssFwRwind += step;
        }
        else
        {
            progerssFwRwind -= step;
        }

        //position = (int) (progerssFwRwind / mTotalTime * Constant.MAX_PROGRESS);
        if (mTotalTime > 0)
        {
            position = (int) ((float) progerssFwRwind / mTotalTime * Constant.MAX_PROGRESS);
        }

        /* 当mTotalTime很大时，可能Position会为0。以下相当于设置一个最小值。 */
        if (0 == position)
        {
            if (isAdvance)
            {
                position = 1;
            }
            else
            {
                position = -1;
            }
        }

        if (progerssFwRwind > mTotalTime)
        {
            position = Constant.MAX_PROGRESS;
            progerssFwRwind = mTotalTime;
        }
        else if (progerssFwRwind <= 0)
        {
            position = 0;
            progerssFwRwind = 0;
        }

        mSeekBar.setSecondaryProgress(position);

        if (!mSeekTimeView.isShown())
        {
            mSeekTimeView.setVisibility(View.VISIBLE);
        }

        mSeekTimeView.setText(formatMill(progerssFwRwind));
    }

    /** get focus but no seek operation */
    /** 获得焦点但无虚进虚退操作 */
    private boolean haveLeftRightOpration = false;

    /**
     * initialize seek progress 初始化进度条虚进
     * @return void
     */
    private void initSecondaryProgress()
    {
        progerssFwRwind = -1;
        if(null != mSeekBar)
        {
            mSeekBar.setSecondaryProgress(0);
        }

        // ((MySeekBar)videoSeekBar).haveSecPro=false;
        haveLeftRightOpration = false;
        if (mSeekTimeView.isShown())
        {
            mSeekTimeView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * onKeyUp process 按键弹起处理
     * @param keyCode
     * @param event
     * @return boolean
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        LogTool.d(LogTool.MPLAY, "onKeyUp keyCode:" + keyCode);
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_DPAD_CENTER:
//            showMediaController();
            break;
        default:
            break;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * set buttons' focusable attribute 设置控制条按钮可否获得焦点
     * @param bool
     * @return void
     */
    private void setBtnFocusable(boolean bool)
    {
        mPreBtn.setFocusable(bool);
        mNextBtn.setFocusable(bool);
        mRewindBtn.setFocusable(bool);
        mForwardBtn.setFocusable(bool);
        mPlayPauseBtn.setFocusable(bool);
        mStopBtn.setFocusable(bool);
        mSubBtn.setFocusable(bool);
        mTrackBtn.setFocusable(bool);
        mInfoBtn.setFocusable(bool);

        mSeek.setFocusable(bool);
        mSeekBar.setFocusable(bool);
        if (mPlayer.getPVRFileInfo().isRadio())
        {
            mRewindBtn.setFocusable(false);
            mForwardBtn.setFocusable(false);
        }
    }

    /** is seekBar selected */
    /** seekBar 是否获得焦点 */
    private boolean isSeekBarSelected = false;

    /**
     * onKeyDown process 按键按下监听事件
     * @param keyCode
     * @param event
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        resetHideViewTimer(Constant.HIDE_DELAY);
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        {
            break;
        }
        case KeyValue.DTV_KEYVALUE_MENU:
        case KeyValue.DTV_KEYVALUE_INFOBAR:
        {
            if (View.VISIBLE == mNavigationLayout.getVisibility())
            {
                hideMediaController();
            }
            else
            {
                mNavigationLayout.setVisibility(View.VISIBLE);
                // mSeekBar.setFocusable(false);
                setBtnFocusable(true);
                mPlayPauseBtn.requestFocus();
                isSeekBarSelected = false;

                if (mSeekTimeView.isShown())
                {
                    mSeekTimeView.setVisibility(View.INVISIBLE);
                }

                initSecondaryProgress();
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);

            // mSeekBar.setFocusable(false);
            setBtnFocusable(true);
            mPlayPauseBtn.requestFocus();
            isSeekBarSelected = false;

            if (mSeekTimeView.isShown())
            {
                mSeekTimeView.setVisibility(View.INVISIBLE);
            }

            initSecondaryProgress();
            break;
        }
        case KeyValue.DTV_KEYVALUE_SUB:
        {
            LogTool.d(LogTool.MPLAY, "KEYCODE_SUB");
            setSubDialog();
            break;
        }
        case KeyValue.DTV_KEYVALUE_AUDIO:
        {
            setTrackDialog();
            return true;
        }
        case KeyValue.DTV_KEYVALUE_TXT:
        {
            LogTool.d(LogTool.MPLAY, "KEYCODE_SEEK");

            /*hide info bar*/
            if (View.VISIBLE == mNavigationLayout.getVisibility())
            {
                hideMediaController();
            }

            if (null != mPlayer)
            {
                TeletextControl mTeletextControl = mPlayer.getTeletextControl();
                if (null != mTeletextControl)
                {
                    TeletextComponent mTeletextComponent = mTeletextControl.getCurrentTTX();
                    if (null != mTeletextComponent)
                    {
                        /*
      Dialog use to show teltext.
     */
                        TeletextDialog mTeletextDialog = new TeletextDialog(PvrActivity.this, R.style.dialog_transparent, TeletextDialog.ttxDialogTypePVR);
                        mTeletextDialog.show();
                    }
                    else
                    {
                        MyToast.makeText(this, R.string.no_teletext, MyToast.LENGTH_LONG).show();
                    }
                }
            }

            break;
        }
        case KeyValue.DTV_KEYVALUE_BACK:
        {
            LogTool.d(LogTool.MPLAY, "KEYCODE_BACK");
            if (View.VISIBLE == mNavigationLayout.getVisibility())
            {
                hideMediaController();
                return true;
            }
            else
            {
                mBackFlag = true;
                mPlayer.stop();
                finish();
            }

            break;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_STOP:
        {
            mPlayer.stop();
            finish();
            break;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);
            mediaPlayPause();
            mPlayPauseBtn.requestFocus();
            break;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_FAST_FORWARD:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);
            forward();
            mForwardBtn.requestFocus();
            break;
        }
        case KeyValue.DTV_KEYVALUE_MEDIA_REWIND:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);
            rewind();
            mRewindBtn.requestFocus();
            break;
        }
        case Constant.KEYCODE_PRE:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);
            seekToPre();
            break;
        }
        case Constant.KEYCODE_NEXT:
        {
            mNavigationLayout.setVisibility(View.VISIBLE);
            seekToNext();
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
            mNavigationLayout.setVisibility(View.VISIBLE);
            if (isSeekBarSelected)
            {
                progerssFwRwind = progerssFwRwind == -1 ? mPlayer.getCurrentPosition() - mStartTime : progerssFwRwind;
                advanceAndRetreat(mStep, false);
                haveLeftRightOpration = true;
            }

            break;
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
            mNavigationLayout.setVisibility(View.VISIBLE);
            if (isSeekBarSelected)
            {
                progerssFwRwind = progerssFwRwind == -1 ? mPlayer.getCurrentPosition() - mStartTime : progerssFwRwind;
                advanceAndRetreat(mStep, true);
                haveLeftRightOpration = true;
            }

            break;
        case KeyValue.DTV_KEYVALUE_DPAD_CENTER:
            if (haveLeftRightOpration)
            {
                if (!isPlaying)
                {
                    mediaPlayPause();
                }

                mSeekBar.setProgress(position);
                if (mSeekTimeView.isShown())
                {
                    mSeekTimeView.setVisibility(View.INVISIBLE);
                }

                if (0 != mPlayer.seekTo(progerssFwRwind + mStartTime))
                {
                    LogTool.d(LogTool.MPLAY, "seek eroor");
                }

                initSecondaryProgress();
            }

            break;

        case KeyValue.DTV_KEYVALUE_RED:
        {
            /*hide info bar*/
            if (View.VISIBLE == mNavigationLayout.getVisibility())
            {
                hideMediaController();
            }

            /*juest for cc win*/
            if (null != mPlayer)
            {
                if (null != mDTV.getCCManager().getUsedCCLists())
                {
                    /* 临时文件操作类实例 */
                    ClosedCaptionDialog mCCDialog = new ClosedCaptionDialog(PvrActivity.this, R.style.dialog_transparent);

                    mCCDialog.show();
                }
                else
                {
                    LogTool.w(LogTool.MPLAY, " getCCList: is null !!");

                    MyToast.makeText(this, R.string.cc_nodata, MyToast.LENGTH_LONG).show();
                }
            }
        }
            break;
        case KeyValue.DTV_KEYVALUE_YELLOW:
        case KeyValue.DTV_KEYVALUE_BLUE:
        {
            return true;
        }
        default:
            break;
        }

        return super.onKeyDown(keyCode, event);
    }

    /**
     * hide media controller 隐藏控制条
     * @return void
     */
    public void hideMediaController()
    {
        focusView = getCurrentFocus();
        if (mSeekTimeView.isShown())
        {
            mSeekTimeView.setVisibility(View.INVISIBLE);
        }

        mNavigationLayout.setVisibility(View.INVISIBLE);
    }

    /**
     * show media controller 显示控制条
     * @return void
     */
    public void showMediaController()
    {
        mNavigationLayout.setVisibility(View.VISIBLE);
        if (focusView != null)
        {
            focusView.requestFocus();
        }
        else
        {
            mPlayPauseBtn.requestFocus();
        }
    }

    @Override
    protected void onDestroy()
    {
        LogTool.i(LogTool.MPLAY, "===== onDestroy =====");
        if (null != mHandler)
        {
            mHandler = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        LogTool.i(LogTool.MPLAY, "===== onPause =====");
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, this);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, this);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF, this);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, this);
        unregisterReceiver(mUsbRemoveBroadCastReceiver);
        RecordPlayStatus.getInstance().setPlaying(false);
        mPlayer.stop();
        mPlayer.close();
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        LogTool.i(LogTool.MPLAY, "===== onResume =====");
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, this, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, this, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF, this, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PARENTAL_RATING, this, 0);
        IntentFilter usbIntentFilter = new IntentFilter();
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbIntentFilter.addDataScheme("file");
        registerReceiver(mUsbRemoveBroadCastReceiver, usbIntentFilter);
        RecordPlayStatus.getInstance().setPlaying(true);
        super.onResume();
    }

    @Override
    protected void onStart()
    {
        LogTool.i(LogTool.MPLAY, "===== onStart ===== mLastPlayPos = " + mLastPlayPos);
        if (mLastPlayPos != 0)
        {
            mPlayer.open();
            mPlayer.start(mVideoPath, mPvrEncryption);
            mStartTime = -1;
            mPlayer.seekTo(mLastPlayPos);
            mPlayer.pause();
            showMediaController();
            mPlayPauseBtn.setImageResource(R.drawable.icon_play_normal);
            isPlaying = false;
            mHandler.sendEmptyMessage(Constant.MSG_UPDATE_BAR);
        }
        mLastPlayPos = 0;
        super.onStart();
    }

    @Override
    protected void onStop()
    {
        LogTool.i(LogTool.MPLAY, "===== onStop =====");
        if ((null != mPvrSeekDialog) && (mPvrSeekDialog.isShowing()))
        {
            mPvrSeekDialog.dismiss();
        }
        if ((null != trackDialog) && (trackDialog.isShowing()))
        {
            trackDialog.dismiss();
        }

        if ((null != mSubtitleDialog) && (mSubtitleDialog.isShowing()))
        {
            mSubtitleDialog.dismiss();
        }

        if ((null != progressDialog) && (progressDialog.isShowing()))
        {
            progressDialog.dismiss();
        }
        mLastPlayPos = mPlayer.getCurrentPosition();

//        mPlayer.stop();
//        mPlayer.close();
        if(isSideRecordingAndPlaying() && mBackFlag)
        {
            Intent mIntent = new Intent();
            mIntent.setAction(CommonValue.PLAY_DTV_ACTION);
            sendBroadcast(mIntent);
        }
        releaseResource();
        finish();
        super.onStop();
    }

    private boolean isSideRecordingAndPlaying()
    {
        if(CommonValue.SIDE_RECORDING_AND_PLANING.equals(mRecordingPlayingState))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * release resources 释放资源
     * @return void
     */
    private void releaseResource()
    {
        if (null != mHandler)
        {
            mHandler.removeMessages(Constant.MSG_UPDATE_BAR);
            mHandler.removeMessages(Constant.MSG_HIDE_MEDIA_CONTROLLER);
            //mHandler = null;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        //holder.getSurface().setAlpha(0);
        LogTool.i(LogTool.MPLAY, "===== PVR surfaceCreated =====");
        holder.setFormat(PixelFormat.TRANSPARENT);

        //holder.getSurface().setSize(1280, 720);
        if (null != mPlayer)
        {
            mPlayer.setSurface(holder);
        }

        Rect videoDisplayArea = halApi.getDisplayRect(this);

        if ((null != videoDisplayArea) && (null != mPlayer))
        {
            mPlayer.setWindowRect(videoDisplayArea);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        LogTool.i(LogTool.MPLAY, "===== PVR surfaceDestroyed =====");
        if (null != mPlayer) {
            mPlayer.setSurface(null);
        }
    }

    private BroadcastReceiver mUsbRemoveBroadCastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String uMountPath = intent.getData().getPath();
            DTVConfig dtvConfig = mDTV.getConfig();
            String recordPath = dtvConfig.getString("au8RecordFilePath", "/mnt/sdcard");
            LogTool.v(LogTool.MPLAY, "recordPath = " + recordPath + "uMountPath = " + uMountPath);
            if (uMountPath.equals(recordPath))
            {
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_SHARED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED))
                {
                    mPlayer.stop();
                    mPlayer.close();
                    dtvConfig.setString("au8RecordFilePath", "/mnt/sdcard");
                    //PvrActivity.this.finish();
                    Intent dtvPlayerIntent = new Intent();
                    dtvPlayerIntent.setClass(PvrActivity.this, MainActivity.class);

                    CommonDef.startActivityEx(PvrActivity.this, dtvPlayerIntent);
                }
            }
        }
    };
}
