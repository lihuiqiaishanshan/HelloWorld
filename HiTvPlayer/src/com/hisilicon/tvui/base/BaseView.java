package com.hisilicon.tvui.base;

import android.view.View;
import android.widget.LinearLayout;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookManager;
import com.hisilicon.dtv.channel.AtvChannelManager;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.epg.EPG;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.dtv.record.Recorder;
import com.hisilicon.dtv.record.RecorderManager;
import com.hisilicon.tvui.play.ChannelHistory;

public class BaseView
{
    /* 按键返回值。跳过MainActivity，直接返回false给上层。  */
    public static final int RET_SUPER_FALSE = 0;
    /* 按键返回值。跳过MainActivity，直接返回true给上层。  */
    public static final int RET_SUPER_TRUE = 1;
    /* 按键返回值。交由MainActivity处理。  */
    public static final int RET_MAIN = 2;

    private LinearLayout mAllLayout;
    public DTV mDTV = null;
    public PlayerManager mPlayerManager = null;
    public Player mPlayer = null;
    public ChannelManager mChannelManager = null;
    public AtvChannelManager mAtvChannelManager = null;
    public NetworkManager mNetworkManager = null;
    public TimeManager mTimeManager = null;
    public EPG mEpg = null;
    public ChannelHistory mChnHistory = ChannelHistory.getInstance();
    public DTVConfig mDtvConfig = null;
    public RecorderManager mRecorderManager = null;
    public Recorder mRecorder = null;
    public BookManager mBookManager = null;
    public Tuner mTuner = null;

    public BaseView(LinearLayout allLayout)
    {
        mAllLayout = allLayout;
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mPlayerManager = mDTV.getPlayerManager();
        mChannelManager = mDTV.getChannelManager();
        mAtvChannelManager = mDTV.getAtvChannelManager();
        mNetworkManager = mDTV.getNetworkManager();
        mTimeManager = mDTV.getNetworkManager().getTimeManager();
        mEpg = mDTV.getEPG();
        mDtvConfig = mDTV.getConfig();
        mBookManager = mDTV.getBookManager();
        mRecorderManager = mDTV.getRecordManager();
        if (0 == mPlayerManager.getPlayers().size())
        {
            mPlayer = mPlayerManager.createPlayer();
        }
        else
        {
            mPlayer = mPlayerManager.getPlayers().get(0);
        }

        if (0 == mRecorderManager.getAllRecorders().size())
        {
            mRecorder = mRecorderManager.createRecorder();
        }
        else
        {
            mRecorder = mRecorderManager.getAllRecorders().get(0);
        }
        mTuner = mPlayer.getTuner();
    }

    public void show()
    {
        if(mAllLayout != null)
        {
            mAllLayout.setVisibility(View.VISIBLE);
            mAllLayout.requestFocus();
        }
    }

    public void hide()
    {
        if(mAllLayout != null)
        {
            mAllLayout.setVisibility(View.GONE);
        }
    }

    public void toggle()
    {
        if(mAllLayout != null)
        {
            if (View.VISIBLE == mAllLayout.getVisibility())
            {
                mAllLayout.setVisibility(View.GONE);
            }
            else
            {
                mAllLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public boolean isShow()
    {
        if(mAllLayout != null)
        {
            if (View.VISIBLE == mAllLayout.getVisibility())
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }
}
