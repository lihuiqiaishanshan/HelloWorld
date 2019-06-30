package com.hisilicon.tvui.play;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.os.Handler;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.channel.AnalogChannel;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.network.service.EnSubtitleType;
import com.hisilicon.dtv.network.service.SubtitleComponent;
import com.hisilicon.dtv.network.service.TeletextComponent;
import com.hisilicon.dtv.play.TeletextControl;
import com.hisilicon.dtv.record.PVREncryption;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.DeviceInformation;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.ParentalControlUtil;
import com.hisilicon.tvui.view.MyToast;

public class ChannelInfoBarView extends BaseView
{

    private final MainActivity mMainActivity;
    /* 整个INFO层  */
    private final LinearLayout mChannelInfoALL;
    /* INFO内一层  */
    private final LinearLayout mChannelInfoBackground;
    /* AV INFO层  */
    private final LinearLayout mChannelInfoAv;

    private final LinearLayout mChannelInfoDtv;

    private final LinearLayout mChannelInfoAtv;

    private final TextView mPresentEventTextView;

    private final TextView mFollowEventTextView;

    private final TextView mChannelIndexTextView;

    private final TextView mChannelNameTextView;

    private final TextView mChannelNumberTextView;

    private final TextView mChannelAtvColorSystem;

    private final TextView mChannelAtvAudioSystem;

    private final TextView mChannelAtvMts;

    private final LinearLayout mPvrTagView;

    private final ImageView mPayImageView;

    private final ImageView mSubtitleImageView;

    private final ImageView mChannelSkipImageView;

    private final ImageView mChannelLockImageView;

    private final TextView mChannelWeekdayTextView;

    private final TextView mChannelAgeTextView;

    private final ImageView mMultiAudioImageView;

    private final ImageView mTeletextImageView;

    private final ImageView mSDHDImageView;

    private final ImageView mUSBImageView;

    /* PVR层  */
    private final LinearLayout mPvrRecordingInfo;

    private final TextView mPvrStoragePathValueTextView;
    private final TextView mPvrRecordedTimeView;

    private final TextView mPvrUsedSpaceView;

    private final TextView mPvrTotalSpaceView;

    private final ProgressBar mPvrStorageProgress;
    private final TextView mChannelRating;
    private final Handler mInforBarHandler;

    private String mRecordPath;

    private Channel mChannel;

    private long mPvrFreeSize = 1;

    private int mMajorMinorSnFlag = 0;

    private static final String USB_PATH_PREFIX = "/mnt/sd";

    private int mKeepRefresh;
    private static final int DISK_SPACE_LACK_RET = -2;

    private static final double MIN_DISK_SPACE_LACK = 10*1024*1024;//10M

    private BookTask mRecBookTask = null;
    private String[] mMTSArray = null;
    private final Runnable CloseInfoBar = new Runnable()
    {
        public void run()
        {
            if (mKeepRefresh > 0)
            {
                mKeepRefresh = mKeepRefresh - 1;
                setInfoBarView();
                mInforBarHandler.postDelayed(CloseInfoBar, 1000);
            }
            else
            {
                hide();
            }
        }
    };

    private final Runnable pvrProcess = new Runnable()
    {
        public void run()
        {
            updatePVRTimeInfo();
            updateSpaceInfo();
            setInfoBarView();
            mInforBarHandler.postDelayed(pvrProcess, 500);
        }
    };

    private final Runnable pvrFreeSize = new Runnable()
    {
        public void run()
        {
            if (mRecordPath != null)
            {
                File file = new File(mRecordPath);
                if (file.exists())
                {
                    mPvrFreeSize = file.getUsableSpace();
                    LogTool.d(LogTool.MPLAY, "pvrFreeSize mRecordPath = " + mRecordPath + " mPvrFreeSize =" + mPvrFreeSize);
                }
            }
        }
    };

    private void getPvrFreeSize(){
        if (mRecordPath != null)
        {
            File file = new File(mRecordPath);
            if (file.exists())
            {
                mPvrFreeSize = file.getUsableSpace();
                LogTool.d(LogTool.MPLAY, "getPvrFreeSize mRecordPath = " + mRecordPath + " mPvrFreeSize =" + mPvrFreeSize);
            }
        }
    }
    private void initRatingView() {
        if (mMainActivity.mCurSourceId == halApi.EnumSourceIndex.SOURCE_ATSC) {
            mChannelRating.setText(ParentalControlUtil.getInstance().getRateString(mChannel, null));
            mChannelRating.setVisibility(View.VISIBLE);
        } else {
            mChannelRating.setVisibility(View.GONE);
        }
        //TODO other DTV parent rate
    }
    public ChannelInfoBarView(MainActivity activity)
    {
        super((LinearLayout) activity.findViewById(R.id.LinearLayout_channelInfo_all));
        mMainActivity = activity;
        mChannelInfoALL = (LinearLayout) mMainActivity.findViewById(R.id.LinearLayout_channelInfo_all);
        mChannelInfoAtv  = (LinearLayout) mMainActivity.findViewById(R.id.LinearLayout_atvInfo);
        mChannelInfoDtv  = (LinearLayout) mMainActivity.findViewById(R.id.LinearLayout_dtvpfInfo);
        mChannelInfoBackground = (LinearLayout) mMainActivity.findViewById(R.id.lv_channelInfo_background);
        mChannelInfoAv = (LinearLayout) mMainActivity.findViewById(R.id.LinearLayout_av_channelInfo);
        mChannelIndexTextView = (TextView) mMainActivity.findViewById(R.id.tv_play_channel_id);
        mChannelNameTextView = (TextView) mMainActivity.findViewById(R.id.TextView_Channel_Name);
        mChannelNumberTextView  = (TextView) mMainActivity.findViewById(R.id.TextView_Channel_Num);
        mPresentEventTextView = (TextView) mMainActivity.findViewById(R.id.TextView_PresentEvent_ID);
        mFollowEventTextView = (TextView) mMainActivity.findViewById(R.id.TextView_FollowEvent_Notify_ID);
        mChannelAtvColorSystem = (TextView) mMainActivity.findViewById(R.id.TextView_atv_colorsystem);
        mChannelAtvAudioSystem = (TextView) mMainActivity.findViewById(R.id.TextView_atv_audiosystem);
        mChannelAtvMts= (TextView) mMainActivity.findViewById(R.id.TextView_atv_mts);
        mMTSArray = mMainActivity.getResources().getStringArray(R.array.mts_values);

        mChannelRating = (TextView) mMainActivity.findViewById(R.id.tv_info_channel_rating);

        mPvrTagView = (LinearLayout) mMainActivity.findViewById(R.id.linearlayout_pvr_tag);
        mPayImageView = (ImageView) mMainActivity.findViewById(R.id.iv_info_pay);
        mSubtitleImageView = (ImageView) mMainActivity.findViewById(R.id.iv_info_subtitle);
        mChannelSkipImageView = (ImageView) mMainActivity.findViewById(R.id.iv_info_channel_skip);
        mChannelLockImageView = (ImageView) mMainActivity.findViewById(R.id.iv_info_channel_lock);
        mChannelWeekdayTextView = mMainActivity.findViewById(R.id.tv_info_channel_weekday);
        mChannelAgeTextView = mMainActivity.findViewById(R.id.tv_info_channel_age);
        mMultiAudioImageView = mMainActivity.findViewById(R.id.iv_info_multiaudio);
        mTeletextImageView = mMainActivity.findViewById(R.id.iv_info_teletext);
        mSDHDImageView = mMainActivity.findViewById(R.id.iv_info_sdhd);
        mUSBImageView = mMainActivity.findViewById(R.id.iv_infobar_usb);
        mPvrRecordingInfo = mMainActivity.findViewById(R.id.linearlayout_pvr_recording_info);
        mPvrRecordedTimeView = mMainActivity.findViewById(R.id.pvr_recordtime_info_value);

        mPvrUsedSpaceView = (TextView) mMainActivity.findViewById(R.id.pvr_storage_used_value);

        mPvrTotalSpaceView = (TextView) mMainActivity.findViewById(R.id.pvr_storage_total);

        mPvrStoragePathValueTextView = (TextView) mMainActivity.findViewById(R.id.pvr_storage_path_value);
        mPvrStorageProgress = (ProgressBar) mMainActivity.findViewById(R.id.pvr_storage_progress);
        mInforBarHandler = new Handler();

        mRecordPath = mDtvConfig.getString("au8RecordFilePath", "/mnt/sdcard");
        mMajorMinorSnFlag = mDtvConfig.getInt(CommonValue.MAJOR_MINOR_SN_FLAG, CommonValue.MAJOR_MINOR_SN_DISABLE);
    }

    private void subScribeEvent()
    {
        LogTool.d(LogTool.MPLAY, "info:subScribeEvent");
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, dvbPlayListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, dvbPlayListener, 0);
    }

    private final IDTVListener dvbPlayListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "IDTVListener.notifyMessage(" + messageID + "," + param1 + "," + parm2 + "," + obj.toString() + ")");
            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH:
            case DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED:
            {

                if (null != mChannel)
                {
                    int channelID = mChannel.getChannelID();
                    if (param1 == channelID)
                    {
                        setInfoBarView();
                    }
                }
                break;
            }
            default:
            {
                LogTool.d(LogTool.MPLAY, "info:default");
                break;
            }

            }
        }
    };

    private void unSubSribeEvent()
    {
        LogTool.d(LogTool.MPLAY, "info:unSubSribeEvent");
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, dvbPlayListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, dvbPlayListener);
    }

    @SuppressWarnings("deprecation")
    public void show(boolean bShowPvr)
    {
        mKeepRefresh = mDtvConfig.getInt(CommonValue.INFOBAR_SHOW_TIME, CommonValue.DEFAULT_INFORBAR_SHOW_TIME);
        if(halApi.isATVSource(mMainActivity.mCurSourceId))
        {
            mChannelInfoDtv.setVisibility(View.GONE);
            mChannelInfoAtv.setVisibility(View.VISIBLE);
        }else{
            mChannelInfoAtv.setVisibility(View.GONE);
            mChannelInfoDtv.setVisibility(View.VISIBLE);
        }
        subScribeEvent();
        mChannel = ChannelHistory.getInstance().getCurrentChn(mMainActivity.mCurSourceId);
        mInforBarHandler.removeCallbacks(CloseInfoBar);
        mInforBarHandler.removeCallbacks(pvrProcess);

        mChannelIndexTextView.setVisibility(View.VISIBLE);
        mChannelInfoALL.setVisibility(View.VISIBLE);

        if (bShowPvr)
        {
            mChannelInfoAv.setBackgroundDrawable(null);
            mInforBarHandler.post(pvrProcess);
            updatePVRTimeInfo();
            mPvrRecordingInfo.setVisibility(View.VISIBLE);
            mChannelInfoBackground.setBackgroundDrawable(mMainActivity.getResources().getDrawable(R.drawable.custom_view_bg));
        }
        else
        {
            mChannelInfoAv.setBackgroundDrawable(mMainActivity.getResources().getDrawable(R.drawable.custom_view_bg));
        }

        mInforBarHandler.post(CloseInfoBar);
        setTextView();
    }

    private void setTextView()
    {
        if (null != mChannel)
        {
            // set CA icon
            if (mChannel.isScramble())
            {
                mPayImageView.setImageResource(R.drawable.common_pay_on);
            }
            else
            {
                mPayImageView.setImageResource(R.drawable.info_pay_off);
            }

            mChannelIndexTextView.setText(initNumber(mChannel));

            // set channel name
            String mChannelName = (!TextUtils.isEmpty(mChannel.getChannelName())) ? mChannel.getChannelName() : (mMainActivity
                   .getString(R.string.info_unknown_channel));
            mChannelNameTextView.setText(String.format("%s%s", mMainActivity.getString(R.string.channel_name), mChannelName));
            DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
            mChannelNumberTextView.setText(mMainActivity.getString(R.string.channel_num) + initNumber(mChannel));
            if (mChannel.getNetworkType() == EnNetworkType.RF) {
                AnalogChannel mAnalogChannel = (AnalogChannel) mChannel;
                String colorStr = "";
                int colorSystem = mAnalogChannel.getColorSystem();
                if (colorSystem == halApi.EnumAtvClrsys.CLRSYS_AUTO)
                {
                    //get original color system
                    colorSystem = mAnalogChannel.getOriginalColorSystem();
                }
                switch (colorSystem) {
                case halApi.EnumAtvClrsys.CLRSYS_AUTO:
                    colorStr = "AUTO";
                    break;
                case halApi.EnumAtvClrsys.CLRSYS_NTSC:
                case halApi.EnumAtvClrsys.CLRSYS_NTSC443:
                    colorStr = "NTSC";
                    break;
                case halApi.EnumAtvClrsys.CLRSYS_PAL:
                case halApi.EnumAtvClrsys.CLRSYS_PAL_60:
                case halApi.EnumAtvClrsys.CLRSYS_PAL_NC:
                    colorStr = "PAL";
                    break;
                case halApi.EnumAtvClrsys.CLRSYS_SECAM:
                    colorStr = "SECAM";
                    break;
                case halApi.EnumAtvClrsys.CLRSYS_PAL_M:
                    colorStr = "PAL M";
                    break;
                case halApi.EnumAtvClrsys.CLRSYS_PAL_N:
                    colorStr = "PAL N";
                    break;
                default:
                    colorStr = "PAL";
                    break;
                }
                mChannelAtvColorSystem.setText(String.format("%s%s", mMainActivity
                        .getResources().getString(R.string.info_color_system), colorStr));
                String soundStr = "";
                switch (mAnalogChannel.getAudioSystem()) {
                case halApi.EnumAtvAudsys.AUDSYS_DK:
                case halApi.EnumAtvAudsys.AUDSYS_DK1_A2:
                case halApi.EnumAtvAudsys.AUDSYS_DK2_A2:
                case halApi.EnumAtvAudsys.AUDSYS_DK3_A2:
                case halApi.EnumAtvAudsys.AUDSYS_DK_NICAM:
                    soundStr = "D/K";
                    break;
                case halApi.EnumAtvAudsys.AUDSYS_BG:
                case halApi.EnumAtvAudsys.AUDSYS_BG_A2:
                case halApi.EnumAtvAudsys.AUDSYS_BG_NICAM:
                    soundStr = "BG";
                    break;
                case halApi.EnumAtvAudsys.AUDSYS_M:
                case halApi.EnumAtvAudsys.AUDSYS_M_A2:
                case halApi.EnumAtvAudsys.AUDSYS_M_BTSC:
                case halApi.EnumAtvAudsys.AUDSYS_M_EIA_J:
                    soundStr = "M";
                    break;
                case halApi.EnumAtvAudsys.AUDSYS_I:
                    soundStr = "I";
                    break;
                case halApi.EnumAtvAudsys.AUDSYS_L:
                case halApi.EnumAtvAudsys.AUDSYS_LL:
                    soundStr = "L";
                    break;
                default:
                    soundStr = "D/K";
                    break;
                }
                mChannelAtvAudioSystem.setText(String.format("%s%s", mMainActivity.
                        getResources().getString(R.string.info_audio_system), soundStr));
                mChannelAtvMts.setText(String.format("%s%s", mMainActivity.getResources()
                        .getString(R.string.info_mtsmode), mMTSArray[mAnalogChannel.getMtsMode()]));
            }else{
                // set pf
                setPFView();
            }
        } else {
            mChannelIndexTextView.setText(R.string.info_unknown_channel);
            mChannelNameTextView.setText(R.string.info_unknown_channel);
            mChannelNumberTextView.setText(R.string.info_unknown_channel);
            mPresentEventTextView.setText(R.string.info_unknown_program);
            mFollowEventTextView.setText(R.string.info_unknown_program);

            mPayImageView.setImageResource(R.drawable.info_pay_off);
        }
    }

    /**
     * Initilizes HD or SD type icon button.<br>
     */
    private void initialHDSDView()
    {
        if (EnServiceType.getRadioServiceTypes().contains(mChannel.getServiceType()))
        {
            mSDHDImageView.setVisibility(View.GONE);
        }
        else
        {
            int videoHeight = mPlayer.getVideoResolutionHeight();
            int dimen = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_720px);
            if (videoHeight >= dimen)
            {
                mSDHDImageView.setImageResource(R.drawable.info_hd_on);
                mSDHDImageView.setVisibility(View.VISIBLE);
            }
            else if (videoHeight > 0)
            {
                mSDHDImageView.setImageResource(R.drawable.info_sd_on);
                mSDHDImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mSDHDImageView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Initilizes USB type icon .<br>
     */
    private void initUSBView()
    {
        StorageManager mStorageManager = (StorageManager) mMainActivity.getSystemService(Context.STORAGE_SERVICE);
        StorageVolume[] storageVolumes = mStorageManager.getVolumeList();

        boolean mUSBOn = false;

        if (null != storageVolumes)
        {
            for (int i = 0; i < storageVolumes.length; i++)
            {
                if (storageVolumes[i].getPath().contains(USB_PATH_PREFIX))
                {
                    LogTool.d(LogTool.MPLAY, "USB:" + storageVolumes[i].getPath());
                    mUSBOn = true;
                    break;
                }
            }
        }

        if (mUSBOn)
        {
            mUSBImageView.setImageResource(R.drawable.info_usb_on);
        }
        else
        {
            mUSBImageView.setImageResource(R.drawable.info_usb_off);
        }

        mUSBImageView.setVisibility(View.VISIBLE);
    }

    private void setInfoBarView()
    {
        LogTool.d(LogTool.MPLAY, "info:setPFTextView");

        if (null != mChannel) {
            // set CA icon
            if (mChannel.isScramble())
            {
                mPayImageView.setImageResource(R.drawable.common_pay_on);
            }
            else
            {
                mPayImageView.setImageResource(R.drawable.info_pay_off);
            }
            //set channel weekday
            Calendar ca = mTimeManager.getCalendarTime();
            if (null != ca) {
                String weekday = "";
                switch (ca.get(Calendar.DAY_OF_WEEK) - 1) {
                    case 0:
                        weekday = "SUNDAY";
                        break;
                    case 1:
                        weekday = "MONDAY";
                        break;
                    case 2:
                        weekday = "TUESDAY";
                        break;
                    case 3:
                        weekday = "WEDNESDAY";
                        break;
                    case 4:
                        weekday = "THURSDAY";
                        break;
                    case 5:
                        weekday = "FRIDAY";
                        break;
                    case 6:
                        weekday = "SATURDAY";
                        break;
                    default:
                        break;
                }
                mChannelWeekdayTextView.setText(weekday);
            }
            LogTool.d(LogTool.MPLAY, "setInfoBarView mCurSourceId" + mMainActivity.mCurSourceId);
            if (halApi.isDTVSource(mMainActivity.mCurSourceId)) {
                mEpg = mDTV.getEPG();
                EPGEvent mProgramEPGEvent = mEpg.getPresentEvent(mChannel);
                if (null != mProgramEPGEvent) {
                    int parentalRating = mProgramEPGEvent.getParentLockLevel();
                    String strCountry = mProgramEPGEvent.getParentCountryCode();
                    if (parentalRating != 0) {
                        mChannelAgeTextView.setVisibility(View.VISIBLE);
                        if (strCountry.equals("BRA") || strCountry.equals("bra")) {
                            int age = parentalRating & 0xf;

                            if ((age <= 1) || (age >= 7)) {
                                age = 0;
                            } else {
                                age = (age + 3) * 2;
                            }

                            if (age < 10) {
                                mChannelAgeTextView.setText(mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_less));
                                LogTool.w(LogTool.MREC, "parent Lock =" + mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_less));
                            } else {
                                mChannelAgeTextView.setText(mMainActivity.getResources().getString(R.string.epg_age_source, age));
                                LogTool.w(LogTool.MREC, "parent Lock =" + age);
                            }

                        } else {
                            mChannelAgeTextView.setText(mMainActivity.getResources().getString(R.string.epg_age_source, parentalRating));
                            LogTool.w(LogTool.MREC, "parent Lock =" + parentalRating);
                        }
                    } else {
                        mChannelAgeTextView.setVisibility(View.GONE);
                    }
                }
            } else {
                mChannelAgeTextView.setVisibility(View.GONE);
            }
            // set channel name
            String mChannelName = (!TextUtils.isEmpty(mChannel.getChannelName()) ? mChannel.getChannelName() : mMainActivity
                    .getString(R.string.info_unknown_channel));
            mChannelNameTextView.setText(String.format("%s%s", mMainActivity.getString(R.string.channel_name), mChannelName));
            mChannelNumberTextView.setText(mMainActivity.getString(R.string.channel_num) + initNumber(mChannel));
            // set pf
            setPFView();

            // set subtitle icon
            List<SubtitleComponent> mSubtitleList = mChannel.getSubtitleComponents();
            if ((null != mSubtitleList) && (mSubtitleList.size() > 0))
            {
                mSubtitleImageView.setImageResource(R.drawable.info_sub_on);
            }
            else
            {
                mSubtitleImageView.setImageResource(R.drawable.info_sub_off);
            }

            //set channel skip icon
            if (mChannel.getTag(EnTagType.HIDE))
            {
                mChannelSkipImageView.setImageResource(R.drawable.channel_skip);
                mChannelSkipImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mChannelSkipImageView.setVisibility(View.GONE);
            }

            //set channel lock icon
            if (mChannel.getTag(EnTagType.LOCK)){
                mChannelLockImageView.setVisibility(View.VISIBLE);
            }else{
                mChannelLockImageView.setVisibility(View.GONE);
            }

            // set audio icon
            List<AudioComponent> mAudioList = mChannel.getAudioComponents();
            if ((null != mAudioList) && (mAudioList.size() > 1))
            {

                mMultiAudioImageView.setImageResource(R.drawable.info_voice_on);
            }
            else
            {
                mMultiAudioImageView.setImageResource(R.drawable.info_voice_off);
            }

            // set teletext icon
            TeletextControl mTeletextControl = mPlayer.getTeletextControl();
            if(halApi.isDTVSource(mMainActivity.mCurSourceId))
            {
                if (null != mTeletextControl)
                {
                    TeletextComponent mCurrentTeletext = mTeletextControl.getCurrentTTX();
                    if (null != mCurrentTeletext)
                    {
                        mTeletextImageView.setImageResource(R.drawable.info_txt_on);
                    }
                    else
                    {
                        mTeletextImageView.setImageResource(R.drawable.info_txt_off);
                    }
                }
                else
                {
                    mTeletextImageView.setImageResource(R.drawable.info_txt_off);
                }
            }
            else
            {
                if (mTeletextControl.isTTXVisible())
                {
                    mTeletextImageView.setImageResource(R.drawable.info_txt_on);
                }
                else
                {
                    mTeletextImageView.setImageResource(R.drawable.info_txt_off);
                }
            }

            // set sd/hd icon
            initialHDSDView();
            initUSBView();

            mSubtitleImageView.setVisibility(View.VISIBLE);
            mMultiAudioImageView.setVisibility(View.VISIBLE);
            mTeletextImageView.setVisibility(View.VISIBLE);
            initRatingView();
        }
        else
        {
            mChannelIndexTextView.setText(R.string.info_unknown_channel);
            mChannelNameTextView.setText(R.string.info_unknown_channel);
            mChannelNumberTextView.setText(R.string.info_unknown_channel);
            mPresentEventTextView.setText(R.string.info_unknown_program);
            mFollowEventTextView.setText(R.string.info_unknown_program);

            mPayImageView.setImageResource(R.drawable.info_pay_off);
            mSubtitleImageView.setImageResource(R.drawable.info_sub_off);
            mMultiAudioImageView.setImageResource(R.drawable.info_voice_off);
            mTeletextImageView.setImageResource(R.drawable.info_txt_off);
            mSDHDImageView.setVisibility(View.GONE);
            mChannelSkipImageView.setVisibility(View.GONE);
            mChannelLockImageView.setVisibility(View.GONE);
        }
    }

    private void setEventTextView(EPGEvent epgEvent, TextView textView, String title)
    {
        EPGEvent epgEvent1 = epgEvent;
        TextView textView1 = textView;
        String title1 = title;
        if (null != epgEvent)
        {
            String name = epgEvent.getEventName();
            if (name.length() <= 0)
            {
                name = mMainActivity.getString(R.string.info_unknown_program);
            }
            Calendar startCal = epgEvent.getStartTimeCalendar();
            String startDate = getDateHourMinute(startCal);
            long duration = epgEvent.getDuration();
            duration *= 1000;
            Calendar endCal = epgEvent.getEndTimeCalendar();
            String endDate = getDateHourMinute(endCal);
            if ((duration > 0) && ("00:00".equals(endDate)))
            {
                endDate = "24:00";
            }

            String event = startDate + "-" + endDate + "     " + name;
            textView.setText(String.format("%s%s", title, event));
        }
        else
        {
            textView.setText(String.format("%s%s", title, mMainActivity.getString(R.string.info_unknown_program)));
        }
    }

    private String getDateHourMinute(Calendar cal)
    {
        int hour;
        int minute;

        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        String hours = changeIntToString(hour);
        String minutes = changeIntToString(minute);
        String hourMinute = hours + ":" + minutes;

        return hourMinute;
    }

    private String getDateHourMinute(Date date)
    {
        int hour;
        int minute;

        Calendar ca = Calendar.getInstance();
        if (date == null)
        {
            Date currentDate = mDTV.getNetworkManager().getTimeManager().getTime();
            if (currentDate == null)
            {
                currentDate = new Date();
            }

            date = currentDate;
        }
        ca.setTime(date);
        int ap = ca.get(Calendar.AM_PM);
        // Log.v(TAG, "ap = " + ap);
        minute = ca.get(Calendar.MINUTE);
        // Log.v(TAG, "minute = " + minute);
        hour = ca.get(Calendar.HOUR);
        if (ap == 1)
        {
            hour += 12;
        }
        // Log.v(TAG, "hour = " + hour);

        String hours = changeIntToString(hour);
        String minutes = changeIntToString(minute);
        String hourMinute = hours + ":" + minutes;

        return hourMinute;
    }

    private String changeIntToString(int month)
    {
        String startMonth;
        if ((month >= 0) && (month < 10))
        {
            startMonth = "0" + String.valueOf(month);
        }
        else
        {
            startMonth = String.valueOf(month);
        }
        return startMonth;
    }

    @Override
    public void hide()
    {
        unSubSribeEvent();
        mChannelIndexTextView.setVisibility(View.GONE);
        mInforBarHandler.removeCallbacks(CloseInfoBar);
        mInforBarHandler.removeCallbacks(pvrProcess);
        mSubtitleImageView.setVisibility(View.GONE);
        mMultiAudioImageView.setVisibility(View.GONE);
        mTeletextImageView.setVisibility(View.GONE);
        mSDHDImageView.setVisibility(View.GONE);
        mUSBImageView.setVisibility(View.GONE);
        mChannelSkipImageView.setVisibility(View.GONE);
        mChannelLockImageView.setVisibility(View.GONE);
        super.hide();
    }

    public boolean stopPvr()
    {
        if (0 == mRecorder.stop())
        {
            mChnHistory.setIsRecording(false);
            mPvrRecordingInfo.setVisibility(View.GONE);
            mChannelInfoBackground.setBackgroundDrawable(null);
            mPvrTagView.setVisibility(View.GONE);
            mInforBarHandler.removeCallbacks(pvrProcess);
            mInforBarHandler.post(pvrFreeSize);
            if (null != mRecBookTask)
            {
                if (EnTaskCycle.ONETIME == mRecBookTask.getCycle())
                {
                    mDTV.getBookManager().deleteTask(mRecBookTask);
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean startPvr(Channel channel, int duration, BookTask currentBookTask)
    {
        int dimen = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_1920px);
        if (mPlayer.getVideoResolutionWidth() > dimen)
        {
            MyToast.makeText(mMainActivity, R.string.toast_4k_not_support, MyToast.LENGTH_LONG).show();
            LogTool.w(LogTool.MREC, "4K stream and 4K display out DO NOT support now");
            return false;
        }

        if (findUsbDevice().size() == 0) {
            MyToast.makeText(
                    mMainActivity,
                    mMainActivity
                            .getString(R.string.setting_pvr_path_null),
                    MyToast.LENGTH_LONG).show();
            return false;
        }

        //mInforBarHandler.post(pvrFreeSize);
        getPvrFreeSize();
        Date beginTime = mDTV.getNetworkManager().getTimeManager().getTime();
        if (null == beginTime)
        {
            beginTime = Calendar.getInstance().getTime();
            if (null == beginTime)
            {
                return false;
            }
        }
        SimpleDateFormat f1 = new SimpleDateFormat("yyyyMMdd_HH-mm-ss");
        String beginTimeString = f1.format(beginTime);
        String recordName = channel.getChannelName().trim();
        recordName = recordName.replace('/', '_');
        //TODO need to distinguish the radio and video
        mRecordPath = mDtvConfig.getString("au8RecordFilePath", "");
        if (mRecordPath.equals("/mnt/sdcard") || mRecordPath.equals("/mnt/nand")) {
            MyToast.makeText(mMainActivity, mMainActivity.getResources()
                    .getString(R.string.pvr_record_path_removed_tip), MyToast.LENGTH_LONG).show();
            return false;
        }
        int ret = getRecordPathStatus(mRecordPath);
        if (-1 == ret)
        {
            MyToast.makeText(mMainActivity, mMainActivity.getResources()
                    .getString(R.string.pvr_record_path_removed_tip), MyToast.LENGTH_LONG).show();
            return false;
        }
        else if (-2 == ret)
        {
            MyToast.makeText(mMainActivity, mMainActivity.getResources()
                    .getString(R.string.timeshift_no_space), MyToast.LENGTH_LONG).show();
            return false;
        }
        if ('/' != mRecordPath.charAt(mRecordPath.length() - 1))
        {
            mRecordPath += "/";
        }
        File file = new File(mRecordPath + recordName + "-" + beginTimeString);
        mPvrStoragePathValueTextView.setText(String.format("%s%s-%s.ts", mRecordPath, recordName, beginTimeString));
        LogTool.w(LogTool.MREC, "mRecordPath=" + mRecordPath + ";recordName" + recordName + ";beginTimeString" + beginTimeString);
        mPvrRecordedTimeView.setText("");
        int startPvrRet = mRecorder.start(channel, file, duration, new PVREncryption(PVREncryption.PVR_ENCRYPTION_TYPE_AES, DeviceInformation.getDeviceMac()));
        LogTool.w(LogTool.MREC, "Mac=" + DeviceInformation.getDeviceMac());
        LogTool.w(LogTool.MREC, "startPvrRet=" + startPvrRet);
        if (0 == startPvrRet)
        {
            mChnHistory.setIsRecording(true);
            mPvrTagView.setVisibility(View.VISIBLE);
            mPvrRecordingInfo.setVisibility(View.VISIBLE);
            mRecBookTask = currentBookTask;
            return true;
        }
        else if (DISK_SPACE_LACK_RET == startPvrRet)
        {
            if (null != currentBookTask)
            {
                if (EnTaskCycle.ONETIME == currentBookTask.getCycle())
                {
                    mDTV.getBookManager().deleteTask(currentBookTask);
                }
            }
            LogTool.w(LogTool.MREC, "free disk space is less then 100M");
            MyToast.makeText(mMainActivity, R.string.pvr_disk_space_lack, MyToast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            if (null != currentBookTask)
            {
                if (EnTaskCycle.ONETIME == currentBookTask.getCycle())
                {
                    mDTV.getBookManager().deleteTask(currentBookTask);
                }
            }
            LogTool.w(LogTool.MREC, "start pvr error");
            MyToast.makeText(mMainActivity, R.string.pvr_timeshift_start_fail, MyToast.LENGTH_SHORT).show();
            return false;
        }
    }

    private List<String> findUsbDevice()
    {
        return halApi.getUsbDeviceList();
    }

    private void updatePVRTimeInfo() {
        int totalTime = 0;
        DecimalFormat newFormat = new DecimalFormat("00");

        if (null != mPlayer) {
            if (null != mRecorder) {
                totalTime = mRecorder.getAlreadyRecordTime();
            }
            if (totalTime >= 3600 * 24) {
                stopPvr();
            }
            if (totalTime > 0) {
                int hour = totalTime / 3600;
                int minute = (totalTime / 60) % 60;
                int second = totalTime % 60;
                String strRet = newFormat.format(hour) + ":" + newFormat.format(minute) + ":" + newFormat.format(second);
                mPvrRecordedTimeView.setText(strRet);
            } else {
                mPvrRecordedTimeView.setText(R.string.pvr_record_time_zero);
            }
        }
    }

    private void updateSpaceInfo()
    {
        if (mRecordPath == null)
            return;
        File dataFile = new File(mRecordPath);
        if (!dataFile.exists())
            return;
        if (null != mRecorder)
        {
            StatFs sfIn = new StatFs(mRecordPath);
            int blockSize = sfIn.getBlockSize();
            int totalCount = sfIn.getBlockCount();
            int availCount = sfIn.getAvailableBlocks();
            double totalGSize = (double) blockSize * (double) totalCount / (1024 * 1024 * 1024);
            double freeGSize = (double) blockSize * (double) availCount / (1024 * 1024 * 1024);
            double usedGSize = totalGSize - freeGSize;
            DecimalFormat format = new DecimalFormat();
            format.applyPattern("#0.00");
            String used = format.format(usedGSize);
            mPvrUsedSpaceView.setText(String.format("%sG  ", used));
            String total = format.format(totalGSize);
            if (total.matches("00$"))
            {
                total = total.substring(0, total.indexOf('.'));
            }
            mPvrTotalSpaceView.setText(String.format("%sG", total));
            mPvrStorageProgress.setProgress((int) (usedGSize / totalGSize * 100));
        }
    }

    private void setPFView()
    {

        EPGEvent mPresentEPGEvent = mEpg.getPresentEvent(mChannel);
        setEventTextView(mPresentEPGEvent, mPresentEventTextView, "");
        EPGEvent mFollowingEPGEvent = mEpg.getFollowEvent(mChannel);
        setEventTextView(mFollowingEPGEvent, mFollowEventTextView, "");
    }

    private int getRecordPathStatus(String recordPath)
    {
        mRecordPath = recordPath;
        LogTool.d(LogTool.MPLAY, "get mRecordPath = " + mRecordPath + "--mPvrFreeSize >" + mPvrFreeSize);
        if("".equals(recordPath)){
            return -1;
        }

        File file = new File(recordPath);
        if (!file.exists())
        {
            return -1;
        }
        else if (mPvrFreeSize <= MIN_DISK_SPACE_LACK)
        {
            return -2;
        }
        else
        {
            return 0;
        }
    }

    private String initNumber(Channel channel) {
        DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
        if (TvSourceManager.getInstance().getCurSourceId(0) == EnumSourceIndex.SOURCE_ATV) {
            int channelNumber = channel.getChannelNo();
            if (channelNumber <= 0) {
                return mMainActivity.getString(R.string.info_unknown_channel);
            }
            List<Integer> sourceList = HitvManager.getInstance().getSourceManager().getSourceList();
            if (sourceList.contains(halApi.EnumSourceIndex.SOURCE_ATSC) || sourceList.contains(halApi.EnumSourceIndex.SOURCE_ISDBT)) {
                return String.format("%2d-0", channelNumber);
            } else {
                return fourDig.format(channelNumber);
            }
        } else {
            if (CommonValue.MAJOR_MINOR_SN_DISABLE == mMajorMinorSnFlag && EnumSourceIndex.SOURCE_ATSC != TvSourceManager.getInstance().getCurSourceId(0)
                    && EnumSourceIndex.SOURCE_ISDBT != TvSourceManager.getInstance().getCurSourceId(0)) {
                return fourDig.format(channel.getChannelNo());
            } else {
                int majorSN = (channel.getLCN() >> 16) & 0xffff;
                int minorSN = channel.getLCN() & 0xffff;
                return String.format("%2d-%2d", majorSN, minorSN);
            }
        }
    }
}
