package com.hisilicon.tvui.play;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.security.SecureRandom;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.epg.IExtendedDescription;
import com.hisilicon.dtv.hardware.EnModulation;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.DVBCChannelDot;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.DVBSTransponder;
import com.hisilicon.dtv.network.DVBSTransponder.EnPolarity;
import com.hisilicon.dtv.network.DVBTChannelDot;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.network.service.EnStreamType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

/**
 *
 * Detail information of current channel Activity. The main function:<br>
 * 1.Shows playing channel number and name information;<br>
 * 2.Shows satellite name, transponder and symbol rate information for DVB-S;<br>
 * 3.Shows modulation, frequency and symbol rate information for DVB-C;<br>
 * 4.Shows bandwidth, frequency and symbol rate information for DTV-T;<br>
 * 5.Shows SD/HD, video type, audio type and usb statusinformation;<br>
 * 6.Shows current signal quality and strength progress information;<br>
 * 7.Shows current current program time and name information;<br>
 * 8.Shows current current program short and detail description.<br>
 *
 */
public class DetailInfoView extends BaseView
{
    private final MainActivity mMainActivity;

    private static final DecimalFormat FREQ_DF = new DecimalFormat("#######.##");

    private TextView mSignalQualityTextView = null;

    private TextView mSignalStrengthTextView = null;

    private TextView mPresentProgramTextView = null;

    private TextView mFollowProgramTextView = null;

    private TextView mPresentProgramTextViewTitle = null;

    private TextView mFollowProgramTextViewTitle = null;

    private TextView mPresentProgramDescriptionTextView = null;

    private TextView mFollowProgramDescriptionTextView = null;

    private ImageView mUSBButton = null;

    private ProgressBar mSignalQualityProgressBar = null;

    private ProgressBar mSignalStrengthProgressBar = null;

    private Channel mCurrentChannel = null;

    private static final String USB_PATH_PREFIX = "/mnt/sd";

    private static final int MILLISECONDSECOND = 1000;

    private final SecureRandom mRandom = new SecureRandom();

    private final Handler mInforBarHandler = new Handler();

    private static String formatFreqDf(float value)
    {
        synchronized (FREQ_DF)
        {
            return FREQ_DF.format(value);
        }
    }

    public DetailInfoView(MainActivity activity)
    {
        super((LinearLayout) activity.findViewById(R.id.LinearLayout_detail_info));
        mMainActivity = activity;
    }

    @Override
    public void show()
    {
        initAll();
        subScribeEvent();
        super.show();
    }

    @Override
    public void hide()
    {
        unSubSribeEvent();
        mInforBarHandler.removeCallbacks(mRefreshInfoBar);
        super.hide();
    }

    private final Runnable mRefreshInfoBar = new Runnable()
    {
        @Override
        public void run()
        {
            setSignalView();
            setUSBView();
            mInforBarHandler.postDelayed(mRefreshInfoBar, MILLISECONDSECOND);
        }
    };

    private final IDTVListener mDTVListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "IDTVListener.notifyMessage(" + messageID + "," + param1 + "," + param2 + "," + obj.toString() + ")");
            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED:
            {
                // please see HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH
            }
            case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH:
            {
                if (null != mCurrentChannel)
                {
                    int channelID = mCurrentChannel.getChannelID();
                    if (param1 == channelID)
                    {
                        setCurrentProgramEventInformationView(true);
                        setCurrentProgramEventInformationView(false);
                        setCurrentProgramEventDescriptionView(true);
                        setCurrentProgramEventDescriptionView(false);
                    }
                }
                break;
            }
            case DTVMessage.HI_SVR_EVT_AV_SIGNAL_STAUTS:
            {
                Intent signalStatusIntent = new Intent(CommonValue.DTV_INTENT_SIGNAL_STATU);
                signalStatusIntent.putExtra(CommonValue.SIGNAL_TAG, param1);
                CommonDef.sendBroadcastEx(mMainActivity, signalStatusIntent);
                break;
            }
            default:
            {
                break;
            }
            }
        }
    };

    private void initAll()
    {
        initCurrentChannelView();

        initsatelliteModulationTransponderView();

        initialHDSDView();

        initVideoView();

        initAudioView();

        initUSBView();

        initialSignalView();

        initCurrentProgramView();

        startRefreshHandler();
    }

    /**
     * Initilizes current channel number and name TextView.<br>
     */
    @SuppressLint("DefaultLocale")
    private void initCurrentChannelView()
    {
        TextView mCurrentChannelTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_channelname);

        mCurrentChannel = mChnHistory.getCurrentChn(mMainActivity.mCurSourceId);

        int currentChannelLcn = -1;
        int mMajorMinorSnFlag = 0;

        mMajorMinorSnFlag = mDTV.getConfig().getInt(CommonValue.MAJOR_MINOR_SN_FLAG, CommonValue.MAJOR_MINOR_SN_DISABLE);

        // Formats current channel index as channel number
        String channelNumber = null;

        if (CommonValue.MAJOR_MINOR_SN_DISABLE == mMajorMinorSnFlag && EnumSourceIndex.SOURCE_ATSC != TvSourceManager.getInstance().getCurSourceId(0)) {
            if (mCurrentChannel != null) {
                currentChannelLcn = mCurrentChannel.getChannelNo();
            } else {
                currentChannelLcn = -1;
            }

            if (currentChannelLcn < 0)
            {
                channelNumber = mMainActivity.getString(R.string.info_channel_num_invalid);
            }
            else
            {
                DecimalFormat fourDig = new DecimalFormat("0000");
                channelNumber = fourDig.format(currentChannelLcn);
            }
        }
        else
        {
            int majorSN = 0, minorSN = 0;
            if (mCurrentChannel != null)
            {
                majorSN = (mCurrentChannel.getLCN() >> 16) & 0xffff;
                minorSN = mCurrentChannel.getLCN() & 0xffff;
                LogTool.v(LogTool.MCHANNEL, "LCN = " + mCurrentChannel.getLCN());
            }
            channelNumber = String.format("%02d-%02d", majorSN, minorSN);
        }

        // Obtains current channel name
        String currentChannelName = null;
        if (null != mCurrentChannel)
        {
            currentChannelName = mCurrentChannel.getChannelName();
        }
        if (null == currentChannelName)
        {
            currentChannelName = mMainActivity.getString(R.string.info_unknown_channel);
        }

        mCurrentChannelTextView.setText(String.format("%s/%s", channelNumber, currentChannelName));
    }

    private String getSatelliteFrequencyInfo(DVBSTransponder dvbSTransponder, String strFreqInfo)
    {
        StringBuffer buffer = new StringBuffer(strFreqInfo);

        int frequency = dvbSTransponder.getFrequency();
        if (frequency > 0)
        {
            frequency /= MILLISECONDSECOND;
            buffer.append(frequency);
        }
        else
        {
            buffer.append("");
        }
        buffer.append("/");

        int symbolRate = dvbSTransponder.getSymbolRate();
        if (symbolRate > 0)
        {
            symbolRate /= MILLISECONDSECOND;
            buffer.append(symbolRate);
        }
        else
        {
            buffer.append("");
        }
        buffer.append("/");

        EnPolarity enPolarity = dvbSTransponder.getPolarity();
        if (null != enPolarity)
        {
            if (EnPolarity.HORIZONTAL == enPolarity)
            {
                buffer.append(mMainActivity.getResources().getString(R.string.info_horizontal));
            }
            else
            {
                buffer.append(mMainActivity.getResources().getString(R.string.info_vertical));
            }
        }

        return buffer.toString();
    }

    private String getTerrestrialFrequencyInfo(DVBTChannelDot dvbTChannelDot, String strFreqInfo)
    {
        StringBuffer buffer = new StringBuffer(strFreqInfo);

        float frequency = (float) dvbTChannelDot.getFrequency();
        if (frequency > 0)
        {
            buffer.append(formatFreqDf(frequency));
        }
        else
        {
            buffer.append("");
        }

        buffer.append("/");

        int bandwidth = dvbTChannelDot.getBandWidth();
        if (bandwidth > 0)
        {
            buffer.append(bandwidth);
        }
        else
        {
            buffer.append("");
        }

        return buffer.toString();
    }

    private String getDtmbFrequencyInfo(DVBTChannelDot dvbDTMBChannelDot, String strFreqInfo)
    {
        StringBuffer buffer = new StringBuffer(strFreqInfo);

        float frequency = (float) dvbDTMBChannelDot.getFrequency();
        if (frequency > 0)
        {
            buffer.append(formatFreqDf(frequency));
        }
        else
        {
            buffer.append("");
        }

        buffer.append("/");

        int bandwidth = dvbDTMBChannelDot.getBandWidth();
        if (bandwidth > 0)
        {
            buffer.append(bandwidth);
        }
        else
        {
            buffer.append("");
        }
        return buffer.toString();
    }

    private String getIsdbtFrequencyInfo(DVBTChannelDot isdbtChannelDot, String strFreqInfo)
    {
        StringBuffer buffer = new StringBuffer(strFreqInfo);

        float frequency = (float) isdbtChannelDot.getFrequency();
        if (frequency > 0)
        {
            buffer.append(formatFreqDf(frequency));
        }
        else
        {
            buffer.append("");
        }

        buffer.append("/");

        int bandwidth = isdbtChannelDot.getBandWidth();
        if (bandwidth > 0)
        {
            buffer.append(bandwidth);
        }
        else
        {
            buffer.append("");
        }

        return buffer.toString();
    }

    private String getCableFrequencyInfo(DVBCChannelDot dvbCChannelDot, String strFreqInfo)
    {
        StringBuffer buffer = new StringBuffer(strFreqInfo);

        int frequency = dvbCChannelDot.getFrequency();
        if (frequency > 0)
        {
            frequency /= MILLISECONDSECOND;
            buffer.append(frequency);
        }
        else
        {
            buffer.append("");
        }
        buffer.append("/");

        int symbolRate = dvbCChannelDot.getSymbolRate();
        if (symbolRate > 0)
        {
            symbolRate /= MILLISECONDSECOND;
            buffer.append(symbolRate);
        }
        else
        {
            buffer.append("");
        }

        return buffer.toString();
    }

    /**
     * Initilizes satellite name for DVB-S or bandwidth for DVB-T and modulation for DVB-C TextView
     * and transponder for DVB-S or frequency for DVB-T and DVB-C TextView.<br>
     */
    private void initsatelliteModulationTransponderView()
    {
        /*
      Define satellite name for DVB-S or bandwidth for DVB-T and modulation for DVB-C TextView.<br>
     */
        TextView mSatelliteModulationTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_satellite_modulation);
        /*
      Define frequency, symbol rate and polarity for DVB-S or frequency for DVB-T and DVB-C
      TextView.<br>
     */
        TextView mFrequencySymbolRatePolarityTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_frequency_symbol_rate_polarity);
        LinearLayout mSignalQualityLinearLayout = (LinearLayout) mMainActivity.findViewById(R.id.lay_info_quality);

        if (null == mCurrentChannel)
        {
            return;
        }
        EnNetworkType enNetworkType = mCurrentChannel.getNetworkType();
        if ((null == enNetworkType))
        {
            return;
        }

        LogTool.d(LogTool.MPLAY, "NetworkType:" + enNetworkType.toString());

        String satelliteModulation = "";
        String frequencySymbolRatePolarity = "";
        switch (enNetworkType) {
            case SATELLITE: {
                mSignalQualityLinearLayout.setVisibility(View.VISIBLE);

                satelliteModulation = mMainActivity.getResources().getString(R.string.info_satellite);
                frequencySymbolRatePolarity = mMainActivity.getResources().getString(R.string.info_transponder);

                DVBSNetwork satellite = (DVBSNetwork) mCurrentChannel.getBelongNetwork();
                if (null != satellite) {
                    String satelliteName = satellite.getName();
                    if (null != satelliteName) {
                        satelliteModulation += satelliteName;
                    }
                }

                DVBSTransponder dvbSTransponder = (DVBSTransponder) mCurrentChannel.getBelongMultiplexe();
                if (null != dvbSTransponder) {
                    frequencySymbolRatePolarity = getSatelliteFrequencyInfo(dvbSTransponder, frequencySymbolRatePolarity);
                }
                break;
            }
            case TERRESTRIAL: {
                mSignalQualityLinearLayout.setVisibility(View.VISIBLE);
                satelliteModulation = mMainActivity.getResources().getString(R.string.info_modulation);
                frequencySymbolRatePolarity = mMainActivity.getResources().getString(R.string.info_bandwidth_frequency);

                DVBTChannelDot dvbTChannelDot = (DVBTChannelDot) mCurrentChannel.getBelongMultiplexe();
                if (null == dvbTChannelDot) {
                    break;
                }
                EnModulation enModulation = dvbTChannelDot.getModulation();
                if (null != enModulation) {
                    satelliteModulation += enModulation.toString();
                }

                frequencySymbolRatePolarity = getTerrestrialFrequencyInfo(dvbTChannelDot, frequencySymbolRatePolarity);
                break;
            }
            case DTMB: {
                mSignalQualityLinearLayout.setVisibility(View.GONE);
                satelliteModulation = mMainActivity.getResources().getString(R.string.info_modulation);
                frequencySymbolRatePolarity = mMainActivity.getResources().getString(R.string.info_bandwidth_frequency);

                DVBTChannelDot dvbDTMBChannelDot = (DVBTChannelDot) mCurrentChannel.getBelongMultiplexe();
                if (null == dvbDTMBChannelDot) {
                    break;
                }
                EnModulation enModulation = null;
                if (null != mTuner) {
                    enModulation = mTuner.getModulation();
                }
                if (null != enModulation) {
                    satelliteModulation += enModulation.toString();
                }

                frequencySymbolRatePolarity = getDtmbFrequencyInfo(dvbDTMBChannelDot, frequencySymbolRatePolarity);
                break;
            }
            case ISDB_TER: {
                mFrequencySymbolRatePolarityTextView.setVisibility(View.INVISIBLE);
                mSignalQualityLinearLayout.setVisibility(View.VISIBLE);
                frequencySymbolRatePolarity = mMainActivity.getResources().getString(R.string.info_bandwidth_frequency);

                DVBTChannelDot isdbtChannelDot = (DVBTChannelDot) mCurrentChannel.getBelongMultiplexe();
                if (null == isdbtChannelDot) {
                    break;
                }

                frequencySymbolRatePolarity = getIsdbtFrequencyInfo(isdbtChannelDot, frequencySymbolRatePolarity);
                break;
            }
            case ATSC_CAB:
            case ATSC_T: {
                mFrequencySymbolRatePolarityTextView.setVisibility(View.INVISIBLE);
                break;
            }
            case CABLE: {
                mSignalQualityLinearLayout.setVisibility(View.GONE);

                satelliteModulation = mMainActivity.getResources().getString(R.string.info_modulation);
                frequencySymbolRatePolarity = mMainActivity.getResources().getString(R.string.info_frequency_symbolrate);

                DVBCChannelDot dvbCChannelDot = (DVBCChannelDot) mCurrentChannel.getBelongMultiplexe();
                if (null != dvbCChannelDot) {
                    EnModulation enModulation = dvbCChannelDot.getModulation();
                    if (null != enModulation) {
                        satelliteModulation += enModulation.toString();
                    }

                    frequencySymbolRatePolarity = getCableFrequencyInfo(dvbCChannelDot, frequencySymbolRatePolarity);
                }
                break;
            }
            default: {
                break;
            }
        }

        mSatelliteModulationTextView.setText(satelliteModulation);
        mFrequencySymbolRatePolarityTextView.setText(frequencySymbolRatePolarity);
    }

    /**
     * Initilizes HD or SD type icon button.<br>
     */
    private void initialHDSDView()
    {
        ImageView mHDSDButton = (ImageView) mMainActivity.findViewById(R.id.iv_info_hdsd);

        if (null == mCurrentChannel)
        {
            return;
        }

        if (EnServiceType.getRadioServiceTypes().contains(mCurrentChannel.getServiceType()))
        {
            mHDSDButton.setVisibility(View.INVISIBLE);
        }
        else
        {
            int videoHeight = mPlayer.getVideoResolutionHeight();
            int dimen = (int) mMainActivity.getResources().getDimension(R.dimen.dimen_720px);
            if (videoHeight >= dimen)
            {
                mHDSDButton.setImageResource(R.drawable.info_hd_on);
                mHDSDButton.setVisibility(View.VISIBLE);
            }
            else if (videoHeight > 0)
            {
                mHDSDButton.setImageResource(R.drawable.info_sd_on);
                mHDSDButton.setVisibility(View.VISIBLE);
            }
            else
            {
                mHDSDButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * Initilizes Video type icon button.<br>
     */
    private void initVideoView()
    {
        ImageView mVideoButton = (ImageView) mMainActivity.findViewById(R.id.iv_info_video);

        if (null == mCurrentChannel)
        {
            return;
        }

        int mVideoType = mCurrentChannel.getVideoType();

        LogTool.d(LogTool.MPLAY, "VideoType:" + EnStreamType.valueOf(mVideoType).toString());
        switch (EnStreamType.valueOf(mVideoType))
        {
        case HI_PSISI_STREAM_VIDEO_MPEG1:
        {
            mVideoButton.setImageResource(R.drawable.info_mpeg1);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_MPEG2:
        {
            mVideoButton.setImageResource(R.drawable.info_mpeg2);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_MPEG4:
        {
            mVideoButton.setImageResource(R.drawable.info_mpeg4);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_H264:
        {
            mVideoButton.setImageResource(R.drawable.info_h264);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_AVS:
        {
            mVideoButton.setImageResource(R.drawable.info_avs);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_WM9:
        {
            mVideoButton.setImageResource(R.drawable.info_wm9);
            break;
        }
        case HI_PSISI_STREAM_VIDEO_HEVC: {
            mVideoButton.setImageResource(R.drawable.icon_hevc);
            break;
        }
        default:
        {
            mVideoButton.setVisibility(View.INVISIBLE);
            return;
        }
        }
        mVideoButton.setVisibility(View.VISIBLE);
    }

    /**
     * Initilizes audio type icon button.<br>
     */
    private void initAudioView()
    {
        ImageView mAudioButton = (ImageView) mMainActivity.findViewById(R.id.iv_info_audio);

        AudioComponent mAudioComponent = mPlayer.getCurrentAudio();

        if (null != mAudioComponent)
        {
            EnStreamType enStreamType = mAudioComponent.getType();
            if (null == enStreamType)
            {
                return;
            }

            LogTool.d(LogTool.MPLAY, "AudioType:" + enStreamType.toString());
            switch (enStreamType)
            {
            // case HI_PSISI_STREAM_AUDIO_DTS:
            // {
            // int tag = (Integer) mAudioButton.getTag();
            // if (tag != 1)
            // {
            // mAudioButton.setImageResource(R.drawable.info_dts_on);
            // mAudioButton.setTag(1);
            //
            // }
            // break;
            // }
            case HI_PSISI_STREAM_AUDIO_MPEG1:
            {
                mAudioButton.setImageResource(R.drawable.info_mpeg1);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_MPEG2:
            {
                mAudioButton.setImageResource(R.drawable.info_mpeg2);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_AAC_ADTS:
            {
                // Please see HI_PSISI_STREAM_AUDIO_AAC_RAW
            }
            case HI_PSISI_STREAM_AUDIO_AAC_LATM:
            {
                // Please see HI_PSISI_STREAM_AUDIO_AAC_RAW
            }
            case HI_PSISI_STREAM_AUDIO_AAC_RAW:
            {
                mAudioButton.setImageResource(R.drawable.info_aac);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_AVS:
            {
                mAudioButton.setImageResource(R.drawable.info_avs);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_WM9:
            {
                mAudioButton.setImageResource(R.drawable.info_wm9);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_AC3:
            {
                mAudioButton.setImageResource(R.drawable.info_ac3);
                break;
            }
            case HI_PSISI_STREAM_AUDIO_DTS:
            {
                mAudioButton.setImageResource(R.drawable.info_dts_on);
                break;
            }
            default:
            {
                mAudioButton.setVisibility(View.INVISIBLE);
                return;
            }
            }
            mAudioButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Initilizes USB type icon button.<br>
     */
    private void initUSBView()
    {
        mUSBButton = (ImageView) mMainActivity.findViewById(R.id.iv_info_usb);

        setUSBView();
    }

    /**
     * Sets USB type icon button.<br>
     */
    private void setUSBView()
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
            mUSBButton.setImageResource(R.drawable.info_usb_on);
        }
        else
        {
            mUSBButton.setImageResource(R.drawable.info_usb_off);
        }
    }

    /**
     * Initilizes signal quality and strength TextView and ProgressBar.<br>
     */
    private void initialSignalView()
    {
        mSignalQualityTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_qualitynum);
        mSignalStrengthTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_strengthnum);
        mSignalQualityProgressBar = (ProgressBar) mMainActivity.findViewById(R.id.pb_info_quality);
        mSignalStrengthProgressBar = (ProgressBar) mMainActivity.findViewById(R.id.pb_info_strength);

        //setSignalView();
    }

    /**
     * Sets signal quality and strength.<br>
     */
    private void setSignalView()
    {
        mTuner = mPlayer.getTuner();
        if (null != mTuner)
        {
            LogTool.v(LogTool.MPLAY, "getModulation = " + mTuner.getModulation());
            int mSignalQuality = mTuner.getSignalQuality();
            int mSignalStrength = mTuner.getSignalStrength();
            mSignalQuality = mSignalQuality > 0 ? (mSignalQuality + getRandom(-3, 3)) : 0;
            mSignalStrength = mSignalStrength > 0 ? (mSignalStrength + getRandom(-3, 3)) : 0;
            mSignalQuality = mSignalQuality < 100 ? mSignalQuality : 100;
            mSignalStrength = mSignalStrength < 100 ? mSignalStrength : 100;
            mSignalQuality = mSignalQuality > 0 ? mSignalQuality : 0;
            mSignalStrength = mSignalStrength > 0 ? mSignalStrength : 0;
            mSignalQualityTextView.setText(String.format("%s%%", Integer.toString(mSignalQuality)));
            mSignalStrengthTextView.setText(String.format("%s%%", Integer.toString(mSignalStrength)));
            mSignalQualityProgressBar.setProgress(mSignalQuality);
            mSignalStrengthProgressBar.setProgress(mSignalStrength);
        }
        else
        {
            mSignalQualityTextView.setText("0%");
            mSignalStrengthTextView.setText("0%");
            mSignalQualityProgressBar.setProgress(0);
            mSignalStrengthProgressBar.setProgress(0);
        }
    }

    private int getRandom(int min, int max)
    {
        int r = mRandom.nextInt(max - min);
        return r + min;
    }

    /**
     * Initilizes current program name and time TextView and short description and detail
     * description TextView.<br>
     */
    private void initCurrentProgramView()
    {
        TextView mPFProgramTextViewTitle = (TextView) mMainActivity.findViewById(R.id.tv_info_pf_program_title);
        mPresentProgramTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_present_program);
        mPresentProgramDescriptionTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_present_program_description);

        mFollowProgramTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_follow_program);
        mFollowProgramDescriptionTextView = (TextView) mMainActivity.findViewById(R.id.tv_info_follow_program_description);

        mPresentProgramTextViewTitle = (TextView) mMainActivity.findViewById(R.id.tv_info_present_program_description_title);
        mFollowProgramTextViewTitle = (TextView) mMainActivity.findViewById(R.id.tv_info_follow_program_description_title);

        mPFProgramTextViewTitle.setText("  P/F Program Info: ");

        setCurrentProgramEventInformationView(true);
        setCurrentProgramEventInformationView(false);

        setCurrentProgramEventDescriptionView(true);
        setCurrentProgramEventDescriptionView(false);

    }

    /**
     * Sets current program event time and name information TextView.<br>
     */
    private void setCurrentProgramEventInformationView(boolean bIsPresentInfo)
    {
        EPGEvent EPGEvent = null;
        TextView ProgramTextView = null;

        if (bIsPresentInfo)
        {
            EPGEvent = mEpg.getPresentEvent(mCurrentChannel);

            ProgramTextView = mPresentProgramTextView;
        }
        else
        {
            EPGEvent = mEpg.getFollowEvent(mCurrentChannel);

            ProgramTextView = mFollowProgramTextView;
        }

        if (null != EPGEvent)
        {
            String name = EPGEvent.getEventName();
            if (null == name)
            {
                name = "";
            }
            if (name.length() <= 0)
            {
                name = mMainActivity.getString(R.string.info_unknown_program);
            }

            SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());


            Calendar startCal = EPGEvent.getStartTimeCalendar();
            String startDateString = "";
            if (null != startCal) {
                int hour = startCal.get(Calendar.HOUR_OF_DAY);
                int min = startCal.get(Calendar.MINUTE);
                startDateString = String.format("%02d", hour) + ":" + String.format("%02d", min);
            }
            if (startDateString.length() > 0) {
                // Add separate string between start time and end time while start time string is
                // not empty
                startDateString += "-";
            }

            Calendar endCal = EPGEvent.getEndTimeCalendar();
            String endDateString = "";
            if (null != endCal) {
                int hour = endCal.get(Calendar.HOUR_OF_DAY);
                int min = endCal.get(Calendar.MINUTE);
                endDateString = String.format("%02d", hour) + ":" + String.format("%02d", min);
            }

            if (bIsPresentInfo)
            {
                ProgramTextView.setText(String.format("Present Program : %s%s %s", startDateString, endDateString, name));
            }
            else
            {
                ProgramTextView.setText(String.format("Follow Program : %s%s %s", startDateString, endDateString, name));
            }
        }
        else
        {

            if (bIsPresentInfo)
            {
                ProgramTextView.setText(String.format("Present Program : %s", mMainActivity.getResources().getString(R.string.info_unknown_program)));
            }
            else
            {
                ProgramTextView.setText(String.format("Follow Program : %s", mMainActivity.getResources().getString(R.string.info_unknown_program)));
            }
        }
    }

    /**
     * sets current program event description information TextView.<br>
     *
     * @param isShortOrDetailDescription true short description, false detail description.<br>
     */
    private void setCurrentProgramEventDescriptionView(boolean isPresentProgramDescription)
    {
        String mProgramShortDescription = null;
        String mProgramDetailDescription = null;

        String mProgramExtendItemContent = null;
        String mProgramExtendDetail = null;

        TextView ProgramTextViewContent = null;

        EPGEvent mProgramEPGEvent = null;
        if (isPresentProgramDescription)
        {
            /*get present program event info*/
            mProgramEPGEvent = mEpg.getPresentEvent(mCurrentChannel);

            ProgramTextViewContent = mPresentProgramDescriptionTextView;

            mPresentProgramTextViewTitle.setText(String.format("Present %s", mMainActivity.getResources().getString(R.string.info_program_description)));

        }
        else
        {
            /*get follow program event info*/
            mProgramEPGEvent = mEpg.getFollowEvent(mCurrentChannel);

            ProgramTextViewContent = mFollowProgramDescriptionTextView;

            mFollowProgramTextViewTitle.setText(String.format("Follow %s", mMainActivity.getResources().getString(R.string.info_program_description)));
        }

        if (null != mProgramEPGEvent)
        {
            mProgramShortDescription = mProgramEPGEvent.getShortDescription();

            IExtendedDescription mIExtendedDescription = mProgramEPGEvent.getExtendedDescription();

            mProgramDetailDescription = "";

            if (null != mIExtendedDescription)
            {
                /*2. item content of extend Description */
                Map<String, String> mapExtendItem = mIExtendedDescription.getItemsContent();
                Iterator<String> iter = mapExtendItem.values().iterator();

                while (iter.hasNext())
                {
                    mProgramExtendItemContent = iter.next() + "\n"; //mProgramExtendItemContent.concat(iter.next());

                    mProgramDetailDescription = mProgramDetailDescription.concat(mProgramExtendItemContent);

                    LogTool.d(LogTool.MPLAY, "get item value: = " + mProgramExtendItemContent);
                }

                /*2. text content of extend Description */
                mProgramExtendDetail = mIExtendedDescription.getDetailDescription();
                LogTool.d(LogTool.MPLAY, "Extend detail:" + mProgramExtendDetail);

                mProgramDetailDescription = mProgramDetailDescription.concat(mProgramExtendDetail);
            }

            /* parental rating description*/
            int parentalRating = 0;
            String strCountry = "";
            String strRating = "";
            String parentalDescription = "";

            parentalRating = mProgramEPGEvent.getParentLockLevel();
            strCountry = mProgramEPGEvent.getParentCountryCode();

            LogTool.d(LogTool.MEPG, "strCountry = " + strCountry + " parentalRating = " + parentalRating);
            if (parentalRating != 0)
            {
                strRating = mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_age);

                parentalDescription += "\n";
                parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_title);
                if (strCountry.equals("BRA") || strCountry.equals("bra"))
                {
                    int age = parentalRating & 0xf;
                    int content = (parentalRating >> 4) & 0xf;

                    if ((age <= 1) || (age >= 7))
                    {
                        age = 0;
                    }
                    else
                    {
                        age = (age + 3) * 2;
                    }

                    if (age < 10)
                    {
                        //less than 10 years old
                        parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_less);
                    }
                    else
                    {
                        //at least (age) years old
                        parentalDescription += String.format(strRating, age);
                    }

                    if (content != 0)
                    {
                        int contenNum = 0;
                        parentalDescription += ",";
                        parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_content);
                        if ((content & 0x1) != 0)
                        {
                            parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_drugs);
                            contenNum++;
                        }
                        if ((content & 0x2) != 0)
                        {
                            if (contenNum != 0)
                            {
                                parentalDescription += ",";
                            }
                            parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_violence);
                            contenNum++;

                        }
                        if ((content & 0x4) != 0)
                        {
                            if (contenNum != 0)
                            {
                                parentalDescription += ",";
                            }
                            parentalDescription += mMainActivity.getResources().getString(R.string.epg_parent_lock_desc_sex);
                        }
                    }
                }
                else
                {
                    parentalDescription += String.format(strRating, parentalRating);
                }
            }
            LogTool.d(LogTool.MEPG, "parentalDescription = " + parentalDescription);

            mProgramDetailDescription = mProgramDetailDescription.concat(parentalDescription) + "\n";

            String genreDescription = getGenreDescription(mProgramEPGEvent);
            mProgramDetailDescription = mProgramDetailDescription.concat(genreDescription);
        }

        LogTool.d(LogTool.MPLAY, " Short Description:" + mProgramShortDescription);
        LogTool.d(LogTool.MPLAY, " Extend Description:" + mProgramDetailDescription);

        if (null == mProgramShortDescription)
        {
            if (null == mProgramDetailDescription)
            {
                ProgramTextViewContent.setText("");
            }
            else
            {
                ProgramTextViewContent.setText(mProgramDetailDescription);
            }
        }
        else
        {
            if (null == mProgramDetailDescription)
            {
                ProgramTextViewContent.setText(mProgramShortDescription);
            }
            else
            {
                ProgramTextViewContent.setText(String.format("%s\n%s", mProgramShortDescription, mProgramDetailDescription));
            }
        }
    }
    private String getGenreDescription(EPGEvent programEPGEvent)
    {
        //genre
        String genreDescription = "";
        int levelOne = programEPGEvent.getContentLevel1();
        LogTool.d(LogTool.MEPG, "levelOne = " + levelOne);
        if (levelOne > 15 && levelOne < 25)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_movie);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 16];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 31 && levelOne < 37)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_new);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 32];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 47 && levelOne < 52)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_game);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 48];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 63 && levelOne < 76)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_sport);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 64];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 79 && levelOne < 86)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_child);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 80];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 95 && levelOne < 103)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_music);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 96];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 111 && levelOne < 124)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_art);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 112];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 127 && levelOne < 132)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_social);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 128];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 143 && levelOne < 152)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_edu);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 142];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 159 && levelOne < 168)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_lei);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 160];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }
        else if (levelOne > 175 && levelOne < 180)
        {
            String[] sports = mMainActivity.getResources().getStringArray(R.array.epg_content_spe);
            genreDescription += "\n";
            genreDescription += "Genre   ";
            genreDescription += sports[levelOne - 176];
            genreDescription += "(";
            genreDescription += levelOne;
            genreDescription += ")";
        }

        return genreDescription;
    }


    /**
     * Starts hanndler runnable to refresh signal status and USB type icon.<br>
     */
    private void startRefreshHandler()
    {
        mInforBarHandler.removeCallbacks(mRefreshInfoBar);
        mInforBarHandler.post(mRefreshInfoBar);
    }

    private void subScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, mDTVListener, 0);
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, mDTVListener, 0);
        }
    }

    private void unSubSribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, mDTVListener);
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, mDTVListener);
        }

    }

    public boolean onListViewKeyDown(int keyCode, KeyEvent keyEvent)
    {
        if (keyCode == KeyValue.DTV_KEYVALUE_BACK || keyCode == KeyValue.DTV_KEYVALUE_INFOBAR)
        {
            hide();
        }
        return false;
    }
}
