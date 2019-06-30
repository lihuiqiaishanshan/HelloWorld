package com.hisilicon.tvui.service;

import java.util.Date;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.SystemProperties;
import android.view.WindowManager;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.base.SourceValue;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.record.BookAlarmDialog;
import com.hisilicon.tvui.record.BookArriveDialog;
import com.hisilicon.tvui.record.BookArrivingDialog;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.util.Util;

public class DTVService extends Service
{
    //EWS param1
    private static final int HI_SVR_EWS_MODE_ISDB = 0x01;
    private static final int HI_SVR_EWS_MODE_SBTVD = 0x02;
    private static final int HI_SVR_EWS_MODE_DVB = 0x03;
    private static final int HI_SVR_EWS_MODE_INDONESIA = 0x04;
    // DTV class
    private DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

    private BroadcastReceiver mBookAlarmReceiver = null;
    private BroadcastReceiver mBookArriveReceiver = null;
    private BroadcastReceiver mEwsAlarmReceiver = null;
    private BroadcastReceiver mReceiverVolumeUpdate = null;
    private BroadcastReceiver mStandByReceiver = null;
    private Date beforeStandByDate;

    IDTVListener mDTVListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MSERVICE, "IDTVListener.notifyMessage(" + messageID + "," + param1 + "," + param2 + "," + obj.toString() + ")");
            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_BOOK_REMIND:
            {
                // Whether to enable the book, if enabled, send a book messag
                    BookTask mBookTask = mDTV.getBookManager().getTaskByID(param1);
                    if (null != mBookTask && ((DTVApplication) getApplication()).isEnabledBook()) {
                        Intent bookArrivingIntent = new Intent(CommonValue.DTV_BOOK_ALARM_ARRIVING);
                        Bundle b = new Bundle();
                        b.putInt(CommonValue.DTV_BOOK_ID, param1);
                        b.putInt(CommonValue.DTV_BOOK_CHANNEL_ID, param2);
                        b.putInt(CommonValue.DTV_BOOK_DURATION, mBookTask.getDuration());
                        b.putInt(CommonValue.DTV_BOOK_TYPE, mBookTask.getType().ordinal());
                        bookArrivingIntent.putExtras(b);
                        //sendBroadcast(bookAlarmIntent);
                        CommonDef.sendBroadcastEx(DTVService.this, bookArrivingIntent);
                    }
                break;
            }
            case DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE:
            {
                break;
            }
            case DTVMessage.HI_SVR_EVT_EWS_START:{
             LogTool.d(LogTool.MSERVICE, "(start)CurSourceID=" + halApi.getCurSourceID());
                    if (halApi.getCurSourceID() == SourceValue.HI_MW_SRC_DVBT) {//DVB-T sup INDONESIA
                        if (param1 != HI_SVR_EWS_MODE_INDONESIA) {
                            Intent ewsPlayIntent = new Intent(CommonValue.DTV_EWS_ALARM_PLAY);
                            Bundle b = new Bundle();
                            b.putInt(CommonValue.DTV_EWS_CHANNEL_ID, param2);
                            ewsPlayIntent.putExtras(b);
                            CommonDef.sendBroadcastEx(DTVService.this, ewsPlayIntent);
                            LogTool.d(LogTool.MSERVICE, "EWS start ChannelID= " + param2);
                        } else {
                            int disasterCode = ((Parcel) obj).readInt();
                            int authorityCode = ((Parcel) obj).readInt();
                            int locationType = ((Parcel) obj).readInt();
                            String strLocaionCode = ((Parcel) obj).readString();
                            String strDiasterCode = ((Parcel) obj).readString();
                            String strDiasterPos = ((Parcel) obj).readString();
                            String strDiasterDate = ((Parcel) obj).readString();
                            String strDiasterCharacter = ((Parcel) obj).readString();
                            String strDiasterMessage = ((Parcel) obj).readString();
                            Intent ewsAlarmIntent = new Intent(CommonValue.DTV_EWS_ALARM_REMINDE);
                            Bundle b = new Bundle();
                            b.putInt(CommonValue.DTV_EWS_DISASTER_CODE, disasterCode);
                            b.putInt(CommonValue.DTV_EWS_AUTHORITY_CODE, authorityCode);
                            b.putInt(CommonValue.DTV_EWS_LOCATION_CODE, locationType);
                            b.putString(CommonValue.DTV_EWS_LOCATION_DESC, strLocaionCode);
                            b.putString(CommonValue.DTV_EWS_DISASTER_DESC, strDiasterCode);
                            b.putString(CommonValue.DTV_EWS_POSITION_DESC, strDiasterPos);
                            b.putString(CommonValue.DTV_EWS_DATE_DESC, strDiasterDate);
                            b.putString(CommonValue.DTV_EWS_CHARACTER_DESC, strDiasterCharacter);
                            b.putString(CommonValue.DTV_EWS_MESSAGE_DESC, strDiasterMessage);
                            ewsAlarmIntent.putExtras(b);
                            LogTool.d(LogTool.MSERVICE, "disasterCode= " + disasterCode + " authorityCode= " + authorityCode + " locationType= " + locationType
                                    + " strLocaionCode= " + strLocaionCode + " strDiasterCode= " + strDiasterCode + " strDiasterPos= " + strDiasterPos
                                    + " strDiasterDate= " + strDiasterDate + " strDiasterCharacter= " + strDiasterCharacter);
                            //sendBroadcast(ewsAlarmIntent);
                            CommonDef.sendBroadcastEx(DTVService.this, ewsAlarmIntent);
                        }
                    } else if (halApi.getCurSourceID() == SourceValue.HI_MW_SRC_ISDBT) {
                        Intent ewsPlayIntent = new Intent(CommonValue.DTV_EWS_ALARM_PLAY);
                        Bundle b = new Bundle();
                        b.putInt(CommonValue.DTV_EWS_CHANNEL_ID, param2);
                        ewsPlayIntent.putExtras(b);
                        CommonDef.sendBroadcastEx(DTVService.this, ewsPlayIntent);
                        LogTool.d(LogTool.MSERVICE, "EWS start ChannelID= " + param2);

                    } else {
                        LogTool.d(LogTool.MSERVICE, "Current source is not supported EWS");
                    }
                    break;
                }
            case DTVMessage.HI_SVR_EVT_EWS_STOP:{
               LogTool.d(LogTool.MSERVICE, "stop CurSourceID=" + halApi.getCurSourceID());
                    if (halApi.getCurSourceID() == SourceValue.HI_MW_SRC_DVBT) {
                        if (param1 != HI_SVR_EWS_MODE_INDONESIA) {
                            Intent ewsPlayIntent = new Intent(CommonValue.DTV_EWS_ALARM_PLAY);
                            Bundle b = new Bundle();
                            b.putInt(CommonValue.DTV_EWS_CHANNEL_ID, param2);
                            ewsPlayIntent.putExtras(b);
                            CommonDef.sendBroadcastEx(DTVService.this, ewsPlayIntent);
                            LogTool.d(LogTool.MSERVICE, "EWS stop ChannelID= " + param2);
                        }
                    } else if (halApi.getCurSourceID() == SourceValue.HI_MW_SRC_ISDBT) {
                        Intent ewsPlayIntent = new Intent(CommonValue.DTV_EWS_ALARM_PLAY);
                        Bundle b = new Bundle();
                        b.putInt(CommonValue.DTV_EWS_CHANNEL_ID, param2);
                        ewsPlayIntent.putExtras(b);
                        CommonDef.sendBroadcastEx(DTVService.this, ewsPlayIntent);
                        LogTool.d(LogTool.MSERVICE, "EWS stop ChannelID= " + param2);
                    }
                    break;
                }
            default:
            {
                break;
            }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        LogTool.v(LogTool.MSERVICE, LogTool.MSERVICE, "DTVService onCreate");
        super.onCreate();

        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_REMIND, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, mDTVListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EWS_STOP, mDTVListener, 0);

        IntentFilter bookAlarmIntentFilter = new IntentFilter(CommonValue.DTV_BOOK_ALARM_ARRIVING);
        mBookAlarmReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                LogTool.d(LogTool.MSERVICE, "Start BookArrivingDialog");
                Bundle bundle = intent.getExtras();
                if (null == bundle) {
                    LogTool.w(LogTool.MSERVICE, "bundle is null!");
                    return;
                }
                int id = bundle.getInt(CommonValue.DTV_BOOK_ID);
                int channelID = bundle.getInt(CommonValue.DTV_BOOK_CHANNEL_ID);
                int duration = bundle.getInt(CommonValue.DTV_BOOK_DURATION);
                int type = bundle.getInt(CommonValue.DTV_BOOK_TYPE);
                LogTool.d(LogTool.MSERVICE, "id = " + id + " channelID = " + channelID + " duration = " + duration + " type = " + type);
                BookArrivingDialog bookArrivingDialog = new BookArrivingDialog(context, duration, channelID, type, id);
                bookArrivingDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                bookArrivingDialog.show();
            }
        };
        registerReceiver(mBookAlarmReceiver, bookAlarmIntentFilter);

        IntentFilter bookArriveIntentFilter = new IntentFilter(CommonValue.DTV_BOOK_ALARM_ARRIVE);
        mBookArriveReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent){
            }
        };
        registerReceiver(mBookArriveReceiver, bookArriveIntentFilter);

        IntentFilter ewsAlarmIntentFilter = new IntentFilter(CommonValue.DTV_EWS_ALARM_REMINDE);
        mEwsAlarmReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                LogTool.d(LogTool.MSERVICE, "Start EwsAlarmActivity");
                Bundle bundle = intent.getExtras();
                int disasterCode = bundle.getInt(CommonValue.DTV_EWS_DISASTER_CODE);
                int authorityCode = bundle.getInt(CommonValue.DTV_EWS_AUTHORITY_CODE);
                int locationType = bundle.getInt(CommonValue.DTV_EWS_LOCATION_CODE);
                String strLocaionCode = bundle.getString(CommonValue.DTV_EWS_LOCATION_DESC);
                String strDiasterCode = bundle.getString(CommonValue.DTV_EWS_DISASTER_DESC);
                String strDiasterPos = bundle.getString(CommonValue.DTV_EWS_POSITION_DESC);
                String strDiasterDate = bundle.getString(CommonValue.DTV_EWS_DATE_DESC);
                String strDiasterCharacter = bundle.getString(CommonValue.DTV_EWS_CHARACTER_DESC);
                String strDiasterMessage = bundle.getString(CommonValue.DTV_EWS_MESSAGE_DESC);

                Intent i = new Intent();
                Bundle b = new Bundle();
                b.putInt(CommonValue.DTV_EWS_DISASTER_CODE, disasterCode);
                b.putInt(CommonValue.DTV_EWS_AUTHORITY_CODE, authorityCode);
                b.putInt(CommonValue.DTV_EWS_LOCATION_CODE, locationType);
                b.putString(CommonValue.DTV_EWS_LOCATION_DESC, strLocaionCode);
                b.putString(CommonValue.DTV_EWS_DISASTER_DESC, strDiasterCode);
                b.putString(CommonValue.DTV_EWS_POSITION_DESC, strDiasterPos);
                b.putString(CommonValue.DTV_EWS_DATE_DESC, strDiasterDate);
                b.putString(CommonValue.DTV_EWS_CHARACTER_DESC, strDiasterCharacter);
                b.putString(CommonValue.DTV_EWS_MESSAGE_DESC, strDiasterMessage);
                i.putExtras(b);
                i.setClass(context, com.hisilicon.tvui.play.EwsAlarmActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                LogTool.d(LogTool.MSERVICE, "disasterCode= " + disasterCode + " authorityCode= " + authorityCode + " locationType= " + locationType
                        + " strLocaionCode= " + strLocaionCode + " strDiasterCode= " + strDiasterCode + " strDiasterPos= " + strDiasterPos
                        + " strDiasterDate= " + strDiasterDate + " strDiasterCharacter= " + strDiasterCharacter);

                //((DTVApplication) getApplication()).startBrotherActivity("EwsAlarmActivity");
                CommonDef.startActivityEx(context, i);
            }
        };
        registerReceiver(mEwsAlarmReceiver, ewsAlarmIntentFilter);

        IntentFilter powerIntentFilter = new IntentFilter();
        powerIntentFilter.addAction(Intent.ACTION_SCREEN_ON);
        powerIntentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mStandByReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
                {
                    LogTool.d(LogTool.MSERVICE, "ACTION_SCREEN_OFF come");
                    beforeStandByDate = mDTV.getNetworkManager().getTimeManager().getTime();
                    BookTask nextTask = mDTV.getBookManager().getComingTask();
                    if (nextTask != null)
                    {
                        Date nextData = nextTask.getStartDate();
                        long millsecond = nextData.getTime() + mDTV.getNetworkManager().getTimeManager().getTimeZone() * 1000;
                        int internal = (int) ((millsecond - beforeStandByDate.getTime()) / 1000);
                        Date a = new Date(millsecond);
                        LogTool.d(LogTool.MSERVICE, "task data = " + a + " beforeStandByDate = " + beforeStandByDate);
                        if (internal > 60)
                        {
                            internal -= 20;
                        }

                        LogTool.d(LogTool.MSERVICE, "internal = " + internal);
                        mDTV.getNetworkManager().getTimeManager().setWakeupInternal(internal);
                    }
                }
                else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON))
                {
                    LogTool.d(LogTool.MSERVICE, "ACTION_SCREEN_ON come");
                    if (beforeStandByDate != null)
                    {
                        long Internal = mDTV.getNetworkManager().getTimeManager().getSleepTime(); //get duration of sleep
                        LogTool.d(LogTool.MSERVICE, "sleeptime Internal = " + Internal);
                        Date afterWakeUpDate = new Date(beforeStandByDate.getTime() + Internal * 1000);
                        mDTV.getNetworkManager().getTimeManager().setTime(afterWakeUpDate);
                    }
                }
            }
        };
        registerReceiver(mStandByReceiver, powerIntentFilter);
    }

    @Override
    public void onDestroy()
    {
        if (null != mReceiverVolumeUpdate)
        {
            this.unregisterReceiver(mReceiverVolumeUpdate);
            mReceiverVolumeUpdate = null;
        }

        if (null != mBookAlarmReceiver)
        {
            this.unregisterReceiver(mBookAlarmReceiver);
            mBookAlarmReceiver = null;
        }

        if (null != mEwsAlarmReceiver)
        {
            this.unregisterReceiver(mEwsAlarmReceiver);
            mEwsAlarmReceiver = null;
        }

        if (null != mBookArriveReceiver)
        {
            this.unregisterReceiver(mBookArriveReceiver);
            mBookArriveReceiver = null;
        }

        if (null != mStandByReceiver)
        {
            this.unregisterReceiver(mStandByReceiver);
            mStandByReceiver = null;
        }

        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_REMIND, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_BOOK_TIME_ARRIVE, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWS_START, mDTVListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EWS_STOP, mDTVListener);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }
    private void recordDirectly(final int taskId, final int channelId, final int duration, final int type) {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if(null != activityManager.getRunningTasks(1))
        {
            String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
            LogTool.d(LogTool.MREC, "startBookTask  runningActivity = " + runningActivity);
            if (!runningActivity.equals("com.hisilicon.tvui.play.MainActivity"))
            {
                // Jump to DTVPlayerActivity
                Intent dtvPlayerIntent = new Intent();
                dtvPlayerIntent.setClass(this, MainActivity.class);
                Bundle b = new Bundle();
                b.putInt(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY, taskId);

                //get change source
                Channel bookedChannel = mDTV.getChannelManager().getChannelByID(channelId);
                int dstSource = -1;
                if (bookedChannel != null)
                {
                    //get change source
                    EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
                    LogTool.d(LogTool.MPLAY, "startBookTask bookNetworkType : " + bookNetworkType);
                    if(bookNetworkType == EnNetworkType.CABLE)
                    {
                        dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                    }
                    else if(bookNetworkType == EnNetworkType.TERRESTRIAL)
                    {
                        dstSource = halApi.EnumSourceIndex.SOURCE_DVBT;
                    }
                    else if(bookNetworkType == EnNetworkType.DTMB)
                    {
                        dstSource = halApi.EnumSourceIndex.SOURCE_DTMB;
                    }
                    else if(bookNetworkType == EnNetworkType.ISDB_TER)
                    {
                        dstSource = halApi.EnumSourceIndex.SOURCE_ISDBT;
                    }

                    LogTool.d(LogTool.MPLAY, "startBookTask book channelID : " + channelId + "startBookTask dstSource: " + dstSource);
                }

                dtvPlayerIntent.putExtra("SourceName", dstSource);

                dtvPlayerIntent.putExtras(b);

                CommonDef.startActivityEx(this, dtvPlayerIntent);
                // Send book task broadcast
            }
        }
        TaskUtil.post(new Runnable() {
            public void run() {
                /* delay 500ms */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LogTool.e(LogTool.MREC, "sendArriveBroadcast failed: " + e);
                }
                Intent bookArriveIntent = new Intent();
                bookArriveIntent.setAction(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CommonValue.DTV_BOOK_ID, taskId);
                bundle.putInt(CommonValue.DTV_BOOK_CHANNEL_ID, channelId);
                bundle.putInt(CommonValue.DTV_BOOK_DURATION, duration);
                bundle.putInt(CommonValue.DTV_BOOK_TYPE, type);
                bookArriveIntent.putExtras(bundle);
                CommonDef.sendBroadcastEx(DTVService.this, bookArriveIntent);
                LogTool.d(LogTool.MREC, "sendArriveBroadcast will start ");
            }
        });
    }
}
