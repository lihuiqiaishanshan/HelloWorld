package com.hisilicon.tvui.play;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.play.EnPlayStatus;
import com.hisilicon.dtv.play.EnTrickMode;
import com.hisilicon.dtv.record.PVREncryption;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.DeviceInformation;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.view.ConfirmDialog;
import com.hisilicon.tvui.view.ConfirmDialog.OnConfirmDialogListener;
import com.hisilicon.tvui.view.MyToast;

import static android.content.Context.POWER_SERVICE;

@SuppressWarnings("deprecation")
public class TimeshiftView extends BaseView
{

    private LinearLayout mTimeShiftLayout;

    private MainActivity mMainActivity;

    private SurfaceView mSurfaceView;

    private LinearLayout mTimeShiftTagLayout;

    private ImageView mTimeShiftTagImageView;

    private TextView mTimeshiftChannelID;

    private TextView mTimeshiftChannelName;

    private TextView mTimeShiftBeginTime;

    private TextView mTimeshiftRecordTime;

    private SeekBar mTimeshiftProgressBar;

    private Button mTimeshiftPlayTime;

    private static DTV mDTV;

    private DTVConfig mDTVConfig;
    private int mKeepRefresh;
    private Date mCurrentRecordtime = null;
    private Date mRecordEndTime = null;
    private Date mRecordStartTime = null;
    private Calendar mTimeshiftStartTime = null;
    private ConfirmDialog mTimeShiftConfirmDialog = null;
    private ConfirmDialog mCatchUpLiveDialog = null;
    private final static int TIMESHIFTTIMEUPDATE = 1;

    private final static int SECOND_TOTAL_MSEL = 1000;
    private final static int DISK_SPACE_LACK_RET = -2;

    private TimeshiftHandler mTimeshiftHandler = new TimeshiftHandler(this);

    private TimeShiftSeekDialog mTimeShiftDialog = null;

    private PVREncryption pvrEncryption = null;
    //MAX rectime
    private int maxRecTime = -1;
    private int preMaxRecTime = 0;

    private PowerManager pm = null;
    private static class TimeshiftHandler extends Handler
    {
        WeakReference<TimeshiftView> mTimeShift;

        public TimeshiftHandler(TimeshiftView timeshift)
        {
            mTimeShift = new WeakReference<TimeshiftView>(timeshift);

        }

        public void handleMessage(Message msg)
        {
            TimeshiftView dtvTimeShift = mTimeShift.get();
            if (msg.what == TIMESHIFTTIMEUPDATE)
            {
                dtvTimeShift.updateTimeshiftTime();
            }
        }
    }

    private Runnable timeshiftRunnable = new Runnable()
    {

        public void run()
        {
            mTimeshiftHandler.sendEmptyMessage(TIMESHIFTTIMEUPDATE);
            mTimeshiftHandler.postDelayed(timeshiftRunnable, SECOND_TOTAL_MSEL);
        }
    };

    /*
     * get the begin time of TimeShift
     */
    public Calendar getStartTimeCal() {

        Calendar beginTimeCal = (Calendar) mTimeshiftStartTime.clone();
        int startTime = mPlayer.getTimeShiftInfo().getBeginTimeSecond();
        beginTimeCal.add(Calendar.SECOND, startTime);
        return beginTimeCal;
    }

    public Calendar getOrgRecordTimeCal() {
        Calendar beginTimeCal = (Calendar) mTimeshiftStartTime.clone();
        return beginTimeCal;
    }

    public Calendar getRecordEndTimeCal() {
        Calendar beginTimeCal = (Calendar) mTimeshiftStartTime.clone();
        int startTime = mPlayer.getTimeShiftInfo().getBeginTimeSecond();
        beginTimeCal.add(Calendar.SECOND, startTime);

        Calendar recordtimeCal = (Calendar) beginTimeCal.clone();
        int rectime = mPlayer.getTimeShiftInfo().getRecordTime();
        recordtimeCal.add(Calendar.SECOND, rectime);
        return recordtimeCal;
    }

    /*
     * update the times.such as the begin time.the record time and the timeshift play time
     */
    public void updateTimeshiftTime()
    {
        if (mTimeshiftStartTime != null)
        {
            Calendar beginTimeCal = (Calendar) mTimeshiftStartTime.clone();
            int startTime = mPlayer.getTimeShiftInfo().getBeginTimeSecond();
            beginTimeCal.add(Calendar.SECOND, startTime);

            mTimeShiftBeginTime.setText(String.format("%02d:%02d:%02d",
                    beginTimeCal.get(Calendar.HOUR_OF_DAY),
                    beginTimeCal.get(Calendar.MINUTE),
                    beginTimeCal.get(Calendar.SECOND)));

            Calendar recordtimeCal = (Calendar) beginTimeCal.clone();
            int rectime = mPlayer.getTimeShiftInfo().getRecordTime();
            recordtimeCal.add(Calendar.SECOND, rectime);

            mTimeshiftRecordTime.setText(String.format("%02d:%02d:%02d",
                    recordtimeCal.get(Calendar.HOUR_OF_DAY),
                    recordtimeCal.get(Calendar.MINUTE),
                    recordtimeCal.get(Calendar.SECOND)));

            Calendar playDateCal = (Calendar) mTimeshiftStartTime.clone();
            int playTime = mPlayer.getTimeShiftInfo().getPlayTimeSecond();
            playDateCal.add(Calendar.SECOND, playTime);

            mTimeshiftPlayTime.setText(String.format("%02d:%02d:%02d",
                    playDateCal.get(Calendar.HOUR_OF_DAY),
                    playDateCal.get(Calendar.MINUTE),
                    playDateCal.get(Calendar.SECOND)));
            preMaxRecTime=rectime;
            if (preMaxRecTime > maxRecTime) {
                maxRecTime = preMaxRecTime;
                rectime = preMaxRecTime;
            } else if (preMaxRecTime == maxRecTime && startTime != 0) {
                rectime = maxRecTime+startTime;
            }
            int progress = 0;
            if (rectime != startTime) {
                progress = (int) (((float) (playDateCal.getTimeInMillis() - beginTimeCal.getTimeInMillis())
                    / (float) (recordtimeCal.getTimeInMillis() - beginTimeCal.getTimeInMillis()))
                    * mTimeshiftProgressBar.getMax());
                LogTool.d(LogTool.MPLAY, "playTime=" + playTime + ";rectime=" + rectime + ";startTime" + startTime+"preMaxRecTime:"+preMaxRecTime+"MaxRecTime:"+maxRecTime);
                if ((playTime >= rectime && playTime != 0) || startTime > rectime || playTime < startTime) {
                    MyToast.makeText(mMainActivity, mMainActivity.getResources()
                            .getString(R.string.timeshift_playing_error), MyToast.LENGTH_LONG).show();
                    stop();
                }
            }
            //Exception handling stop
            mTimeshiftProgressBar.setProgress(progress);

            float x = mTimeshiftProgressBar.getX() + mTimeshiftProgressBar.getWidth() * progress / mTimeshiftProgressBar.getMax();
            x = x - mTimeshiftPlayTime.getWidth() / 2;
            mTimeshiftPlayTime.setX(x);
            if (mTimeshiftPlayTime.getVisibility() == View.INVISIBLE)
            {
                mTimeshiftPlayTime.setVisibility(View.VISIBLE);
            }
        }
    }

    public void closeTimeShiftConfirmDialog() {
        if (mTimeShiftConfirmDialog != null) {
            mTimeShiftConfirmDialog.cancel();
        }
    }

    public void closeCatchUpLiveDialog() {
        if (mCatchUpLiveDialog != null) {
            mCatchUpLiveDialog.cancel();
        }
    }
    public Date addSecond(Date date, int seconds)
    {
        if (null == date)
        {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, seconds);
        return calendar.getTime();
    }

    public TimeshiftView(MainActivity mainActivity)
    {
        super((LinearLayout) mainActivity.findViewById(R.id.ly_timeshift));
        mMainActivity = mainActivity;
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        if (null != mDTV)
        {
            mDTVConfig = mDTV.getConfig();
        }
        // Set TimeShift File not saved
        if (null != mDTVConfig) {
            mDTVConfig.setInt(CommonValue.TIMSHIFT_TO_PVR_ENABLE, 0);
        }
        pvrEncryption = new PVREncryption(PVREncryption.PVR_ENCRYPTION_TYPE_AES, DeviceInformation.getDeviceMac());
        mSurfaceView = (SurfaceView) mMainActivity.findViewById(R.id.play_surfaceview);
        mTimeShiftLayout = (LinearLayout) mMainActivity.findViewById(R.id.timeshift_status);
        mTimeShiftTagLayout = (LinearLayout) mMainActivity.findViewById(R.id.linearlayout_timeshift_tag);
        mTimeShiftTagImageView = (ImageView) mMainActivity.findViewById(R.id.timeshift_tag);
        mTimeshiftChannelID = (TextView) mMainActivity.findViewById(R.id.timeshift_channel_id);
        mTimeshiftChannelName = (TextView) mMainActivity.findViewById(R.id.timeshift_channel_name);
        mTimeShiftBeginTime = (TextView) mMainActivity.findViewById(R.id.timeshift_begin_time);
        mTimeshiftRecordTime = (TextView) mMainActivity.findViewById(R.id.timeshift_record_time);
        mTimeshiftProgressBar = (SeekBar) mMainActivity.findViewById(R.id.timeshift_play_progressBar);
        mTimeshiftPlayTime = (Button) mMainActivity.findViewById(R.id.timeshift_play_time);

        String tip = mMainActivity.getResources().getString(R.string.timeshift_stop_tip);
        String tip_title = mMainActivity.getResources().getString(R.string.timeshift_stop_tip_title);
        mTimeShiftConfirmDialog = new ConfirmDialog(mMainActivity, R.style.DIM_STYLE, tip_title , tip, 1f);
        OnConfirmDialogListener tmpConfimListener = new OnConfirmDialogListener()
        {
            @Override
            public void onCheck(int which)
            {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK)
                {
                    stop();
                }

                mTimeShiftConfirmDialog.cancel();
                if (mCatchUpLiveDialog != null) {
                    mCatchUpLiveDialog.cancel();
                }

            }
        };
        mTimeShiftConfirmDialog.setConfirmDialogListener(tmpConfimListener);

        String tipCatchUp = mMainActivity.getResources().getString(R.string.timeshift_catch_live) + " "
                + mMainActivity.getResources().getString(R.string.timeshift_stop_tip);
        mCatchUpLiveDialog = new ConfirmDialog(mMainActivity, R.style.DIM_STYLE, "", tipCatchUp, 1f);
        OnConfirmDialogListener tmpCatchListener = new OnConfirmDialogListener()
        {
            @Override
            public void onCheck(int which)
            {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK)
                {
                    stop();
                }

                mCatchUpLiveDialog.cancel();
                if (mTimeShiftConfirmDialog != null) {
                    mTimeShiftConfirmDialog.cancel();
                }
            }
        };
        mCatchUpLiveDialog.setConfirmDialogListener(tmpCatchListener);
    }

    /**
     * 停止时移
     */
    public void stop()
    {
        Window window = mMainActivity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.privateFlags &= ~(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
        window.addPrivateFlags(lp.privateFlags);
        mPlayer.stopTimeShift();
        closeTimeShiftView();
        unSubPVRScribeEvent();
        unregisterUSBReceiver();
        super.hide();
        mMainActivity.playChannel(mChnHistory.getCurrentList(mMainActivity.mCurSourceId),
                mChnHistory.getCurrentChn(mMainActivity.mCurSourceId), true);
    }

    /**
     * 时移快进
     */
    public boolean forward()
    {
        Drawable drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_play);
        EnPlayStatus status = mPlayer.getStatus();
        if (status != EnPlayStatus.TIMESHIFTPLAY)
        {
            return false;
        }
        EnTrickMode trickMode = mPlayer.getTrickMode();
        EnTrickMode nextTrickMode = null;
        switch (trickMode)
        {
        case FAST_FORWARD_TWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_FOUR;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_fast_forward_4);
            break;
        }
        case FAST_FORWARD_FOUR:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_EIGHT;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_fast_forward_8);
            break;
        }
        case FAST_FORWARD_EIGHT:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_SIXTEEN;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_fast_forward_16);
            break;
        }
        case FAST_FORWARD_SIXTEEN:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_THIRTYTWO;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_fast_forward_32);
            break;
        }
        case FAST_FORWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_play);
            break;
        }
        // case FAST_FORWARD_THIRTYTWO:
        case FAST_FORWARD_NORMAL:
        case FAST_BACKWARD_TWO:
        case FAST_BACKWARD_FOUR:
        case FAST_BACKWARD_EIGHT:
        case FAST_BACKWARD_SIXTEEN:
        case FAST_BACKWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_TWO;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_fast_forward_2);
            break;
        }
        default:
            break;
        }
        if (null == nextTrickMode)
        {
            return false;
        }
        if (mPlayer.trickPlay(nextTrickMode) == 0 && (null != drawable))
        {
            mTimeShiftTagImageView.setImageDrawable(drawable);
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * 时移快退
     */
    public boolean backward()
    {
        Drawable drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_play);
        EnPlayStatus status = mPlayer.getStatus();
        if (status != EnPlayStatus.TIMESHIFTPLAY)
        {
            return false;
        }
        EnTrickMode trickMode = mPlayer.getTrickMode();
        EnTrickMode nextTrickMode = null;
        switch (trickMode)
        {
        case FAST_BACKWARD_TWO:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_FOUR;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_quick_return_4);
            break;
        }
        case FAST_BACKWARD_FOUR:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_EIGHT;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_quick_return_8);
            break;
        }
        case FAST_BACKWARD_EIGHT:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_SIXTEEN;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_quick_return_16);
            break;
        }
        case FAST_BACKWARD_SIXTEEN:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_THIRTYTWO;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_quick_return_32);
            break;
        }
        case FAST_BACKWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_FORWARD_NORMAL;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_play);
            break;
        }
        case FAST_FORWARD_NORMAL:
        case FAST_FORWARD_TWO:
        case FAST_FORWARD_FOUR:
        case FAST_FORWARD_EIGHT:
        case FAST_FORWARD_SIXTEEN:
        case FAST_FORWARD_THIRTYTWO:
        // case FAST_BACKWARD_THIRTYTWO:
        {
            nextTrickMode = EnTrickMode.FAST_BACKWARD_TWO;
            drawable = mMainActivity.getResources().getDrawable(R.drawable.timeshift_quick_return_2);
            break;
        }
        default:
            break;
        }
        if (null == nextTrickMode)
        {
            return false;
        }
        if (mPlayer.trickPlay(nextTrickMode) == 0 && (null != drawable))
        {
            mTimeShiftTagImageView.setImageDrawable(drawable);
            return true;
        }
        else
        {
            return false;
        }

    }

    public boolean seek(long seekDateLong)
    {
        if (mPlayer.seek(seekDateLong) == 0)
        {
            return true;
        }
        else
        {
            LogTool.w(LogTool.MREC, "timeshift seek error!");
            return false;
        }
    }

    public boolean isTimeShiftPlaying()
    {
        EnPlayStatus status = mPlayer.getStatus();
        if (EnPlayStatus.TIMESHIFTPLAY == status)
        {
            if (EnTrickMode.FAST_FORWARD_NORMAL == mPlayer.getTrickMode())
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 时移开始，暂停，播放
     */
    public boolean playOrPause()
    {
        EnPlayStatus status = mPlayer.getStatus();
        LogTool.d(LogTool.MPLAY, "play status =" + status);
        switch (status)
        {
        case TIMESHIFTPLAY:
        {
            // mPlayer.stop(EnStopType.FREEZE);
            LogTool.d(LogTool.MPLAY, "TIMESHIFTPLAY!");
            EnTrickMode trickMode = mPlayer.getTrickMode();
            if (trickMode == EnTrickMode.FAST_FORWARD_NORMAL)
            {
                mTimeShiftTagImageView.setImageDrawable(
                        mMainActivity.getResources().getDrawable(R.drawable.timeshift_pause));
                if (0 == mPlayer.pause())
                {
                    LogTool.d(LogTool.MPLAY, "TIMESHIFTPLAY Pause success!");
                }
                else
                {
                    LogTool.w(LogTool.MPLAY, "TIMESHIFTPLAY Pause error");
                    return false;
                }
            }
            else
            {
                LogTool.d(LogTool.MPLAY, "FAST_FORWARD_NORMAL!");
                if (0 == mPlayer.trickPlay(EnTrickMode.FAST_FORWARD_NORMAL))
                {
                    mTimeShiftTagImageView.setImageDrawable(
                            mMainActivity.getResources().getDrawable(R.drawable.timeshift_play));
                }
                else
                {
                    LogTool.d(LogTool.MPLAY, "TIMESHIFTPLAY FAST_FORWARD_NORMAL error!");
                    return false;
                }
            }
            break;
        }
        case PAUSE:
        {
            LogTool.d(LogTool.MPLAY, "TIMESHIFT PAUSE");
            if (0 == mPlayer.play())
            {
                LogTool.d(LogTool.MPLAY, "mPlayer.play() success!");
                mTimeShiftTagImageView.setImageDrawable(
                        mMainActivity.getResources().getDrawable(R.drawable.timeshift_play));
            }
            else
            {
                LogTool.w(LogTool.MPLAY, "mPlayer.play() error!");
                return false;
            }
            break;
        }
        case LIVEPLAY:
        {
            LogTool.d(LogTool.MPLAY, "TIMESHIFT LIVEPLAY!");
            if (isTimeShiftPathEmpty())
            {
                MyToast.makeText(mMainActivity, mMainActivity.getResources()
                        .getString(R.string.timeshift_no_space), Toast.LENGTH_LONG).show();
                return false;
            }
            mPlayer.setTimeShiftEncryption(pvrEncryption);
            int timeshiftStartRet = mPlayer.startTimeShift();
            if (0 == timeshiftStartRet)
            {
                mTimeshiftPlayTime.setVisibility(View.INVISIBLE);
                openTimeShiftView();
            }
            else if (DISK_SPACE_LACK_RET == timeshiftStartRet)
            {
                LogTool.w(LogTool.MREC, "free disk space is less then 100M");
                MyToast.makeText(mMainActivity, R.string.pvr_disk_space_lack, Toast.LENGTH_SHORT).show();
                return false;
            }
            else
            {
                LogTool.w(LogTool.MREC, "start timeshift error");
                MyToast.makeText(mMainActivity, R.string.timeshift_start_fail, Toast.LENGTH_SHORT).show();
                return false;
            }
            break;
        }
        default:
        {
            LogTool.d(LogTool.MPLAY, "default TIMESHIFT PAUSE");
            if (0 == mPlayer.play())
            {
                LogTool.d(LogTool.MPLAY, "mPlayer.play() success!");
                mTimeShiftTagImageView.setImageDrawable(mMainActivity.getResources().getDrawable(R.drawable.timeshift_play));
            }
            else
            {
                LogTool.e(LogTool.MPLAY, "mPlayer.play() error!");
                return false;
            }
            break;
        }
        }
        return true;

    }

    public void playPause()
    {
        mTimeShiftTagImageView.setImageDrawable(
                mMainActivity.getResources().getDrawable(R.drawable.timeshift_pause));
        mPlayer.pause();
    }

    /**
     *
     */
    public boolean playResume()
    {
        EnPlayStatus status = mPlayer.getStatus();
        if (EnPlayStatus.TIMESHIFTPLAY == status)
        {
            LogTool.d(LogTool.MPLAY, "TIMESHIFT PAUSE");
            EnTrickMode trickMode = mPlayer.getTrickMode();
            if (trickMode != EnTrickMode.FAST_FORWARD_NORMAL)
            {
                LogTool.d(LogTool.MPLAY, "FAST_FORWARD_NORMAL!");
                if (0 == mPlayer.trickPlay(EnTrickMode.FAST_FORWARD_NORMAL))
                {
                    mTimeShiftTagImageView.setImageDrawable(
                            mMainActivity.getResources().getDrawable(R.drawable.timeshift_play));
                }
                else
                {
                    LogTool.d(LogTool.MPLAY, "TIMESHIFTPLAY FAST_FORWARD_NORMAL error!");
                    return false;
                }
            }
        }

        return true;
    }

    private Runnable closeTimeShiftInforbar = new Runnable()
    {
        @Override
        public void run()
        {
            if (mKeepRefresh > 0)
            {
                if (EnTrickMode.FAST_FORWARD_NORMAL == mPlayer.getTrickMode() && EnPlayStatus.TIMESHIFTPLAY == mPlayer.getStatus())
                {
                    mKeepRefresh = mKeepRefresh - 1;
                }
                mTimeShiftLayout.postDelayed(closeTimeShiftInforbar, 1000);
            }
            else
            {
                mTimeShiftLayout.setVisibility(View.INVISIBLE);
            }
        }
    };

    public void setTimeShiftInforbarAlwaysVisible()
    {
        mTimeShiftLayout.removeCallbacks(closeTimeShiftInforbar);
        mTimeShiftLayout.setVisibility(View.VISIBLE);
    }

    public void setTimeShiftInforbarVisible(boolean bVisible)
    {

        if (bVisible)
        {
            mKeepRefresh = mDTVConfig.getInt(CommonValue.INFOBAR_SHOW_TIME, CommonValue.DEFAULT_INFORBAR_SHOW_TIME);
            mTimeShiftLayout.setVisibility(View.VISIBLE);
            mTimeShiftLayout.removeCallbacks(closeTimeShiftInforbar);
            mTimeShiftLayout.post(closeTimeShiftInforbar);
        }
        else
        {
            mKeepRefresh = 0;
            mTimeShiftLayout.setVisibility(View.INVISIBLE);
        }
    }

    public boolean getTimeShiftInforbarVisible()
    {
        if (mTimeShiftLayout.getVisibility() == View.INVISIBLE)
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    /**
     * <p>
     * 关闭时移界面。<br>
     * <p>
     * close time tip information for time shift.<br>
     */
    private void closeTimeShiftView()
    {
        setTimeShiftInforbarVisible(false);
        mTimeshiftProgressBar.setProgress(0);
        mTimeShiftTagLayout.setVisibility(View.INVISIBLE);
        mTimeshiftHandler.removeCallbacks(timeshiftRunnable);
        if (null != mTimeShiftDialog)
        {
            mTimeShiftDialog.dismiss();
        }
    }

    /**
     * <p>
     * 打开时移界面。<br>
     * <p>
     * open time tip information for time shift.<br>
     */
    private boolean openTimeShiftView()
    {
        setTimeShiftInforbarVisible(true);
        mTimeShiftTagLayout.setVisibility(View.VISIBLE);
        mTimeShiftTagLayout.getParent().requestTransparentRegion(mSurfaceView);
        mTimeShiftTagImageView.setImageDrawable(
                mMainActivity.getResources().getDrawable(R.drawable.timeshift_pause));

        int index = mChnHistory.getCurrentChn(mMainActivity.mCurSourceId).getChannelNo();
        DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
        String number = fourDig.format(index);
        mTimeshiftChannelID.setText(number);
        if (null != mPlayer.getCurrentChannel() && null != mPlayer.getCurrentChannel().getChannelName()) {
            mTimeshiftChannelName.setText(mPlayer.getCurrentChannel().getChannelName());
        } else {
            mTimeshiftChannelName.setText("");
        }
        mTimeshiftHandler.post(timeshiftRunnable);
        return true;
    }

    private boolean isTimeShiftPathEmpty()
    {
        if (null == mDTVConfig)
        {
            return false;
        }
        String path = mDTVConfig.getString(CommonValue.RECORD_PATH, CommonValue.DEFAULT_RECORD_PATH);
        File file = new File(path);
        if (file.exists() && (file.getUsableSpace() > CommonValue.DEFAULT_MIN_ALLOW_REC_SPACE))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void timeshiftStart()
    {
        mTimeshiftStartTime = mTimeManager.getCalendarTime();
        // Avoiding the empty Calendar object in ATSC
        if (mTimeshiftStartTime == null) {
            LogTool.d(LogTool.MPLAY, "mTimeshiftStartTime is null");
            mTimeshiftStartTime = Calendar.getInstance();
        }
        mMainActivity.getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
        // TODO:条件判断
        if (playOrPause())
        {
            subPVRScribeEvent();
            registerUSBReceiver();
            setTimeShiftInforbarVisible(true);
            super.show();
        }
    }
    public int onListViewKeyUp(int keyCode, KeyEvent keyEvent) {
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_HOME: {
                mTimeShiftConfirmDialog.show();
                return RET_SUPER_TRUE;
            }
        }
        return RET_SUPER_TRUE;
    }
    /* 0 return false , 1 return true, 2 not return  */
    public int onListViewKeyDown(int keyCode, KeyEvent keyEvent)
    {
        switch (keyCode)
        {

            case KeyValue.DTV_KEYVALUE_INFOBAR:
            {
                if (getTimeShiftInforbarVisible())
                {
                    setTimeShiftInforbarVisible(false);
                }
                else
                {
                    setTimeShiftInforbarVisible(true);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_BACK:
            {
                if (getTimeShiftInforbarVisible())
                {
                    setTimeShiftInforbarVisible(false);
                }
                else
                {
                    mTimeShiftConfirmDialog.show();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_BACKWARD:
            {
                setTimeShiftInforbarVisible(true);
                backward();
                break;
            }
            case KeyValue.DTV_KEYVALUE_FORWARD_40:
            case KeyValue.DTV_KEYVALUE_FORWARD_42:
            {
                setTimeShiftInforbarVisible(true);
                forward();
                break;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
            case KeyValue.DTV_KEYVALUE_VOLUME_DOWN:
            case KeyValue.DTV_KEYVALUE_VOLUME_UP:
            {
                return RET_SUPER_FALSE;
            }
            case KeyValue.DTV_KEYVALUE_RED:
            {
                setTimeShiftInforbarAlwaysVisible();
                if (isTimeShiftPlaying())
                {
                    mTimeShiftDialog = new TimeShiftSeekDialog(mMainActivity, R.style.DIM_STYLE, this);
                    mTimeShiftDialog.show();
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_MEDIA_PLAY_PAUSE:
            case KeyValue.DTV_KEYVALUE_GREEN:
            {
                setTimeShiftInforbarVisible(true);
                playOrPause();
                break;
            }
            case KeyValue.DTV_KEYVALUE_POWER: {
                stop();
                pm = (PowerManager)mMainActivity.getSystemService(POWER_SERVICE);
                if (SystemProperties.get("persist.prop.suspend.mode").equals("str")) {
                    LogTool.d(LogTool.MSERVICE, "STR suspend");
                    halApi.goToSleep(pm);
                } else if (SystemProperties.get("persist.prop.suspend.mode").equals("shutdown")) {
                    LogTool.d(LogTool.MSERVICE, "shut down");
                    Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                    intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mMainActivity.startActivity(intent);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_HOME:
                return RET_SUPER_TRUE;
            case KeyValue.DTV_KEYVALUE_SUB:
                setTimeShiftInforbarVisible(false);
                return RET_MAIN;
            default:
            {
                mTimeShiftConfirmDialog.show();
                return RET_SUPER_TRUE;
            }
        }
        return RET_SUPER_TRUE;
    }

    IDTVListener dvbPlayListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "messageID:" + messageID + " param1: " + param1 + " param2:" + param2);

            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF:
            {
                LogTool.d(LogTool.MPLAY, "HI_SVR_EVT_PVR_PLAY_SOF");
                playResume();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR:
            {
                MyToast.makeText(mMainActivity, mMainActivity.getResources()
                        .getString(R.string.pvr_timeshift_start_fail), MyToast.LENGTH_LONG).show();
                stop();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF:
                break;
            case DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC:
            {
                playPause();
                mCatchUpLiveDialog.show();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_REC_ERROR:
            {
                stop();
                DTVConfig dtvConfig = mDTV.getConfig();
                dtvConfig.setString(CommonValue.RECORD_PATH, "/mnt/sdcard");
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW:
            {
                MyToast.makeText(mMainActivity, R.string.pvr_disk_slow, MyToast.LENGTH_LONG).show();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL:
            {
                MyToast.makeText(mMainActivity, R.string.pvr_disk_full_stop, MyToast.LENGTH_LONG).show();
                stop();
                break;
            }
            case DTVMessage.HI_SVR_EVT_EWS_START: {
                MyToast.makeText(mMainActivity, R.string.timeshift_ews_start, MyToast.LENGTH_LONG).show();
                stop();
                break;
            }
            default:
                break;
            }
        }
    };

    private void subPVRScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_ERROR, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, dvbPlayListener, 0);
        }
    }

    private void unSubPVRScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_REACH_REC, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_SOF, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_ERROR, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_ERROR, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_PLAY_EOF, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, dvbPlayListener);
        }
    }

    private BroadcastReceiver mUsbBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uMountPath = intent.getData().getPath();
            DTVConfig dtvConfig = mDTV.getConfig();
            String recordPath = dtvConfig.getString(CommonValue.RECORD_PATH, CommonValue.DEFAULT_RECORD_PATH);
            if (recordPath.equals("/mnt/sdcard") || recordPath.equals("/mnt/nand")) {
                stop();
                return;
            }
            LogTool.w(LogTool.MPLAY, "mUsbRemoveBroadCastReceiver recordPath = " + recordPath + "uMountPath= " + uMountPath);
            String action = intent.getAction();
            LogTool.d(LogTool.MREC, "action = " + action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_SHARED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                stop();
                dtvConfig.setString(CommonValue.RECORD_PATH, "/mnt/sdcard");
            }
        }
    };

    private void registerUSBReceiver()
    {
        LogTool.d(LogTool.MPLAY, "registerUSBReceiver");
        IntentFilter usbIntentFilter = new IntentFilter();
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbIntentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbIntentFilter.addDataScheme("file");
        mMainActivity.registerReceiver(mUsbBroadCastReceiver, usbIntentFilter);
    }

    private void unregisterUSBReceiver()
    {
        if (mUsbBroadCastReceiver != null)
        {
            LogTool.d(LogTool.MPLAY, "unregisterUSBReceiver");
            mMainActivity.unregisterReceiver(mUsbBroadCastReceiver);
            mUsbBroadCastReceiver = null;
        }
    }
}
