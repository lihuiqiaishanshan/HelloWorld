package com.hisilicon.tvui.play;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.SystemSetting;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.RecordSetTimeView.OnRecordClick;
import com.hisilicon.tvui.record.RecordToShutDownDialog;
import com.hisilicon.tvui.record.RecordToShutDownDialog.OnRecordToShutDownDialogListener;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.view.ConfirmDialog;
import com.hisilicon.tvui.view.ConfirmDialog.OnConfirmDialogListener;
import com.hisilicon.tvui.view.MyToast;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.os.PowerManager.PARTIAL_WAKE_LOCK;

/* 对PVR录制的控制类  */
public class PvrRecordView extends BaseView
{
    private MainActivity mMainActivity;
    private ChannelInfoBarView mChnInfoView = null;
    private RecordSetTimeView mRecordSetTimeView = null;
    private ConfirmDialog mStopRecordDialog = null;
    private RecordToShutDownDialog mRecordToShutDownDialog = null;
    private PowerManager.WakeLock wakeLock=null;
    private PowerManager pm = null;
    private boolean isScreen = false;
    private AudioManager mAudioManager = null;
    private int currentVolume;

    private boolean bLightOn = false;
    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {

        public void run() {
            if (bLightOn) {
                bLightOn = false;
            } else {
                bLightOn = true;
            }
            halApi.setPvrLight(bLightOn);
            mHandler.postDelayed(this, 300);
        }
    };

    public PvrRecordView(MainActivity mainActivity, ChannelInfoBarView chnInfoView)
    {
        super((LinearLayout) mainActivity.findViewById(R.id.ly_record_time_set));
        mMainActivity = mainActivity;
        mChnInfoView = chnInfoView;
        mAudioManager = (AudioManager)mMainActivity.getSystemService(AUDIO_SERVICE);
        currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        initView();
    }

    private void initView()
    {
        mRecordSetTimeView = new RecordSetTimeView(mMainActivity);
        OnRecordClick dialogListener = new OnRecordClick()
        {
            @Override
            public void onRecord(int duration)
            {
                Channel tmpChn = mPlayer.getCurrentChannel();
                startPvr(tmpChn, duration, null);
            }
        };
        mRecordSetTimeView.setRecordListener(dialogListener);

        String tipEnsureStop = mMainActivity.getResources().getString(R.string.pvr_ensure_stop);
        String tipTitle = mMainActivity.getResources().getString(R.string.pvr_ensure_stop_title);
        mStopRecordDialog = new ConfirmDialog(mMainActivity, R.style.DIM_STYLE, tipTitle,tipEnsureStop, 1f);
        OnConfirmDialogListener stopDialogListener = new OnConfirmDialogListener()
        {
            @Override
            public void onCheck(int which)
            {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK)
                {
                    stopPvr();
                }

                mStopRecordDialog.cancel();
            }
        };
        mStopRecordDialog.setConfirmDialogListener(stopDialogListener);

        pm = (PowerManager)mMainActivity.getSystemService(POWER_SERVICE);
        mRecordToShutDownDialog = new RecordToShutDownDialog(mMainActivity);
        OnRecordToShutDownDialogListener onRecordToShutDownDialogListener = new OnRecordToShutDownDialogListener() {
            @Override
            public void onCheck(int which) {
                if (which == OnRecordToShutDownDialogListener.SHUTDOWN) {
                    mRecordToShutDownDialog.cancel();
                    if (mStopRecordDialog != null) {
                        mStopRecordDialog.cancel();
                    }
                    stopPvr();
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
                } else if (which == OnRecordToShutDownDialogListener.NO_ACTION) {
                    LogTool.d(LogTool.MSERVICE, "pvr go on");
                    mRecordToShutDownDialog.cancel();
                } else if (which == OnRecordToShutDownDialogListener.SCREEN_OFF) {
                    LogTool.d(LogTool.MSERVICE, "SCREEN_OFF");
                    currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    mRecordToShutDownDialog.setLinearLayoutScreenVisible();
                } else if (which == OnRecordToShutDownDialogListener.COMEBACK) {
                    LogTool.d(LogTool.MSERVICE, "SCREEN_OFF");
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
                    mRecordToShutDownDialog.setLinearLayoutScreenGone();
                    mRecordToShutDownDialog.cancel();

                }
            }
        };
        mRecordToShutDownDialog.setOnRecordToShutDownDialogListener(onRecordToShutDownDialogListener);
    }

    public void closeStopRecordDialog() {
        if (mStopRecordDialog != null) {
            mStopRecordDialog.cancel();
        }
    }
    public void startPvr(Channel channel, int duration, BookTask currentBookTask)
    {
        if (mChnInfoView.startPvr(channel, duration, currentBookTask))
        {
            mHandler.postDelayed(runnable, 300);
            sendPvrStartBroadcast();
            LogTool.d(LogTool.MPLAY, "pvrRecordView start pvr ok"  );
            mMainActivity.getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
            mChnInfoView.show(true);
            subPVRScribeEvent();
            registerUSBReceiver();
        }

    }

    public void stopPvr()
    {
        mHandler.removeCallbacks(runnable);
        if(mRecordToShutDownDialog!=null){
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
            mRecordToShutDownDialog.setLinearLayoutScreenGone();
            mRecordToShutDownDialog.cancel();
        }
        sendPvrStopBroadcast();
        if (mChnInfoView.stopPvr())
        {
            LogTool.d(LogTool.MPLAY, "pvrRecordView stop pvr ok"  );
            Window window = mMainActivity.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.privateFlags &= ~(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                    | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
            window.addPrivateFlags(lp.privateFlags);
            mStopRecordDialog.cancel();
            unSubPVRScribeEvent();
            unregisterUSBReceiver();
            mChnInfoView.hide();
        }

    }

    private void sendPvrStartBroadcast() {
        Intent start = new Intent(CommonValue.DTV_PVR_START);
        CommonDef.sendBroadcastEx(mMainActivity, start);
    }
    private void sendPvrStopBroadcast() {
        Intent stop = new Intent(CommonValue.DTV_PVR_STOP);
        CommonDef.sendBroadcastEx(mMainActivity, stop);

    }
    public void showSetTimeView()
    {
        mRecordSetTimeView.show();
    }

    public void hideSetTimeView() {
        if (mRecordSetTimeView.isShow()) {
            mRecordSetTimeView.hide();
        }
    }

    public int onListViewKeyUp(int keyCode, KeyEvent keyEvent) {
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_HOME: {
                mStopRecordDialog.show();
                return RET_SUPER_TRUE;
            }
        }
        return RET_SUPER_TRUE;
    }
    /* 0 return false , 1 return true, 2 not return  */
    public int onListViewKeyDown(int keyCode, KeyEvent keyEvent) {
        LogTool.d(LogTool.MPLAY, "pvrRecordView:keyCode=" + keyCode);
        if (mRecordSetTimeView.isShow()) {
            if (KeyValue.DTV_KEYVALUE_BACK == keyCode) {
                mRecordSetTimeView.hide();
            }
            return RET_SUPER_FALSE;
        }

        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_INFOBAR: {
                if (mChnInfoView.isShow()) {
                    mChnInfoView.hide();
                } else {
                    mChnInfoView.show(true);
                }
                break;
            }
            case KeyValue.DTV_KEYVALUE_RED: {
                if (!mChnInfoView.isShow()) {
                    mChnInfoView.show(true);
                }
                return RET_SUPER_TRUE;
            }
        /*
        case KeyValue.DTV_KEYVALUE_DPAD_CENTER:
        {
            String path = mRecorder.getRecordFile().getPath()+".ts";
            File recordFile = new File(path);
            if(recordFile.isFile())
            {
                LogTool.d(LogTool.MREC, "playing path: "+path);
                mMainActivity.gotoFullPlayRecordFile(true);
                Intent intent = new Intent(mMainActivity, PvrActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("VideoPath", path);
                intent.putExtra("RecordingPlayingState", CommonValue.SIDE_RECORDING_AND_PLANING);
                mMainActivity.startActivity(intent);
            }

            return RET_SUPER_TRUE;
        }
        */
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
            case KeyValue.DTV_KEYVALUE_VOLUME_DOWN:
            case KeyValue.DTV_KEYVALUE_VOLUME_UP:
            case KeyValue.DTV_KEYVALUE_SOUNDMODE:
            case KeyValue.DTV_KEYVALUE_PICTUREMODE:
            case KeyValue.DTV_KEYVALUE_SUB: {
                return RET_MAIN;
            }
            case KeyValue.DTV_KEYVALUE_POWER: {

                if (mRecordToShutDownDialog.isShowing()) {
                    mRecordToShutDownDialog.setLinearLayoutScreenGone();
                } else {
                    mRecordToShutDownDialog.show();
                    mRecordToShutDownDialog.setLinearLayoutScreenGone();
                }
            }
            case KeyValue.DTV_KEYVALUE_HOME:
                return RET_SUPER_TRUE;
            default: {
                mStopRecordDialog.show();
                return RET_SUPER_TRUE;
            }
        }
        return RET_SUPER_TRUE;
    }

    @Override
    public boolean isShow()
    {
        if (mRecordSetTimeView.isShow() || mChnHistory.isRecording())
        {
            return true;
        }
        return false;
    }

    IDTVListener dvbPlayListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "messageID:" + messageID + " param1: " + param1 + " param2:" + param2);

            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW:
            {
                MyToast.makeText(mMainActivity, R.string.pvr_disk_slow, MyToast.LENGTH_LONG).show();
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX:
            {
                LogTool.d(LogTool.MPLAY, "HI_SVR_EVT_PVR_REC_OVER_FIX");
                if (mChnHistory.isRecording())
                {
                    MyToast.makeText(mMainActivity, R.string.pvr_rec_finished, MyToast.LENGTH_LONG).show();
                    stopPvr();
                }
                //tell launcher to call standby
                LogTool.d(LogTool.MPLAY, "PROPERTY_BOOK_TASK_TYPE="+SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE));
                if(SystemProperties.getInt(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_IDLE) == Util.BOOK_TASK_TYPE_WORKING) {
                    SystemProperties.set(Util.PROPERTY_BOOK_TASK_TYPE, Util.BOOK_TASK_TYPE_FINISH + "");
                    mMainActivity.goToShutdown();
                }
                break;
            }
            case DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL:
            {
                if (mChnHistory.isRecording())
                {
                    MyToast.makeText(mMainActivity, R.string.pvr_disk_full_stop, MyToast.LENGTH_LONG).show();
                    stopPvr();
                }
                break;
            }
            case DTVMessage.HI_SVR_EVT_EWS_START: {
                if(mChnHistory.isRecording()){
                    MyToast.makeText(mMainActivity, R.string.pvr_rec_ews_start, MyToast.LENGTH_LONG).show();
                    stopPvr();
                }
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
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL, dvbPlayListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, dvbPlayListener, 0);
        }
    }

    private void unSubPVRScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_OVER_FIX, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISK_SLOW, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_PVR_REC_DISKFULL, dvbPlayListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, dvbPlayListener);
        }
    }

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
        }
    }

    private BroadcastReceiver mUsbBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uMountPath = intent.getData().getPath();
            String recordPath = mDtvConfig.getString(CommonValue.RECORD_PATH, CommonValue.DEFAULT_RECORD_PATH);
            if(recordPath.equals("/mnt/sdcard") || recordPath.equals("/mnt/nand")){
                stopPvr();
                return;
            }
            String action = intent.getAction();
            LogTool.d(LogTool.MREC, "action = " + action);
            if (action.equals(Intent.ACTION_MEDIA_EJECT) || action.equals(Intent.ACTION_MEDIA_SHARED)
                    || action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                //stop PVR
                if (mChnHistory.isRecording()) {
                    MyToast.makeText(mMainActivity, R.string.pvr_rec_finished, MyToast.LENGTH_LONG).show();
                    stopPvr();
                }
                mDtvConfig.setString(CommonValue.RECORD_PATH, "/mnt/sdcard");
            }

        }
    };
}
