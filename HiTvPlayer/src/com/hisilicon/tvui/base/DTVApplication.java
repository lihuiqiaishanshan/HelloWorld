package com.hisilicon.tvui.base;

import java.util.HashMap;
import java.util.List;

import android.app.Application;

import com.hisilicon.dtv.channel.EnTVRadioFilter;
import android.os.SystemProperties;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.util.LogTool;

/**
 * The application of APK, all activities of HiDTVPlayer can visit it. <br>
 * Use for store all register BrotherAcitivities and some status value of playing DTV. <br>
 * Also used for transport some status to DTVPlayerActivity, notice DTVPlayerActivity change play
 * channel and list.<br>
 * The logic of use this class is a little bit complicate, make sure you figure out it when you do
 * some changes. <br>
 * Suggest use ChannelHistoryRecorder to change the playing status values. <br>
 * Suggest use DTVBaseActivity to manage Brother Activities and resume status value.<br>
 * @author y00164887 updated : add set the log output level and module by z00120637
 *
 */
public class DTVApplication extends Application
{
    /**
     * Whether to enable the book
     */
    private boolean mIsEnabledBook = true;

    private boolean mIsEnabledStop = true;

    /**
     * Record scan type value set in Dialog, Use it in ScanProgressActivity.
     */
    private HashMap<EnNetworkType, ScanType> mMapScanType = null;

    /**
     * Record want scan satellites set in Fragment, Use it in ScanProgressActivity.
     */
    private List<Network> mNeedScanNetwork = null;

    /**
     * Record NetworkType list set in Dialog, Use it in ScanProgressActivity.
     */
    private List<EnNetworkType> mNeedScanNetworkType = null;

    /**
     * service type of search channel
     */
    private EnTVRadioFilter mTvRadioFilter = null;

    public EnTVRadioFilter getmTvRadioFilter() {
        return mTvRadioFilter;
    }

    public void setmTvRadioFilter(EnTVRadioFilter mTvRadioFilter) {
        this.mTvRadioFilter = mTvRadioFilter;
    }

    /**
     * MainActivity instance
     */
    private MainActivity mMainActivity;

    public void setMainActivity(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
    }

    public MainActivity getMainActivity() {
        return mMainActivity;
    }
    public void setScanType(EnNetworkType networkType, ScanType scanType)
    {
        if (null == mMapScanType)
        {
            mMapScanType = new HashMap<EnNetworkType, ScanType>();
        }
        if (null == networkType)
        {
            mMapScanType.clear();
        }
        else
        {
            mMapScanType.put(networkType, scanType);
        }
        return;
    }

    public ScanType getScanType(EnNetworkType networkType)
    {
        if (null == mMapScanType)
        {
            return null;
        }
        return mMapScanType.get(networkType);
    }

    public void setScanNetworkType(List<EnNetworkType> lstNetworkType)
    {
        mNeedScanNetworkType = lstNetworkType;
    }

    public List<EnNetworkType> getScanNetworkType()
    {
        return mNeedScanNetworkType;
    }

    public void setScanParam(List<Network> lstNetwork)
    {
        mNeedScanNetwork = lstNetwork;
    }

    public List<Network> getScanParamNetwork()
    {
        return mNeedScanNetwork;
    }

    /**
     * Call before all activities or services onCreate.
     */
    @Override
    public void onCreate()
    {
        LogTool.d(LogTool.MALLLOG, "Start Application.");
        super.onCreate();

        // You can change Log level and module here.
        LogTool.setLogModule(LogTool.MALLLOG);
        if (SystemProperties.get("service.quickplay.setting.status").equals("")) {
            SystemProperties.set("service.quickplay.setting.status", "0");
        }
    }

    /**
     * Get the enable book status
     * @return mIsEnableBook
     */
    public boolean isEnabledBook()
    {
        return mIsEnabledBook;
    }

    /**
     * Temporary enable book
     * @param isEnable
     */
    public void setEnabledBook(boolean isEnable)
    {
        mIsEnabledBook = isEnable;
    }

    /* Set it true when DtvPlayActivity onPause and set it false when DtvPlayActivity onCreate, onStart, onResume.
    OnStop just works after onPause. */
    public void setEnabledStop(boolean isEnable)
    {
        mIsEnabledStop = isEnable;
    }

    public boolean isEnabledStop()
    {
        return mIsEnabledStop;
    }

}
