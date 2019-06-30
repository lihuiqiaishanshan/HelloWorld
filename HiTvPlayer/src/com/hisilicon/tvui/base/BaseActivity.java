package com.hisilicon.tvui.base;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.AtvChannelManager;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.dtv.network.service.ClosedCaptionManager;
import com.hisilicon.dtv.pc.ParentalControlManager;
import com.hisilicon.dtv.play.Ginga;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.dtv.record.Recorder;
import com.hisilicon.dtv.record.RecorderManager;
import com.hisilicon.tvui.play.ChannelHistory;
import com.hisilicon.tvui.play.ChannelInfoBarView;

/* 该Activity提供一些基础方法，供调用。理论上，所有Activity均应继承该方法。  */
public abstract class BaseActivity extends Activity
{
    /* 按键返回值。跳过MainActivity，直接返回false给上层。  */
    public static final int RET_SUPER_FALSE = 0;
    /* 按键返回值。跳过MainActivity，直接返回true给上层。  */
    public static final int RET_SUPER_TRUE = 1;
    /* 按键返回值。交由MainActivity处理。  */
    public static final int RET_MAIN = 2;

    public DTV mDTV = null;
    public PlayerManager mPlayerManager = null;
    public Player mPlayer = null;
    public ChannelManager mChannelManager = null;
    public AtvChannelManager mAtvChannelManager = null;
    public NetworkManager mNetworkManager = null;
    public ChannelInfoBarView mChnInfoView = null;
    public ChannelHistory mChnHistory = null;
    public RecorderManager mRecorderManager = null;
    public Recorder mRecorder = null;
    public DTVConfig mDtvConfig = null;
    public ParentalControlManager mPCManager;
    public Ginga ginga;
    public ClosedCaptionManager ccManager;
    private AudioManager mAudioManager = null;
    public static final int HI_INVALID_PID = 0x1FFF;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mPlayerManager = mDTV.getPlayerManager();
        mChannelManager = mDTV.getChannelManager();
        ccManager = mDTV.getCCManager();
        mAtvChannelManager = mDTV.getAtvChannelManager();
        mNetworkManager = mDTV.getNetworkManager();
        mDtvConfig = mDTV.getConfig();
        mPCManager = mDTV.getParentalControlManager();
        mChnHistory = ChannelHistory.getInstance();
        if (0 == mPlayerManager.getPlayers().size()) {
            mPlayer = mPlayerManager.createPlayer();
        } else {
            mPlayer = mPlayerManager.getPlayers().get(0);
        }
        ginga = mPlayer.getGinga();
        mRecorderManager = mDTV.getRecordManager();
        if (0 == mRecorderManager.getAllRecorders().size()) {
            mRecorder = mRecorderManager.createRecorder();
        } else {
            mRecorder = mRecorderManager.getAllRecorders().get(0);
        }
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    /**
     * Use Android's interface to adjust lower volume.
     */
    public void adjustLowerVolume()
    {
        mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_MUSIC, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

    /**
     * Use Android's interface to adjust raise volume.
     */
    public void adjustRaiseVolume()
    {
        mAudioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_MUSIC, AudioManager.FX_FOCUS_NAVIGATION_UP);
    }

}
