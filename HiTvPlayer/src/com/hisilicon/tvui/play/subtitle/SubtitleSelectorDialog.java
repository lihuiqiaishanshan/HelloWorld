package com.hisilicon.tvui.play.subtitle;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.WindowManager.LayoutParams;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.network.service.AudioComponent;
import com.hisilicon.dtv.network.service.EnSubtComponentType;
import com.hisilicon.dtv.network.service.EnSubtitleType;
import com.hisilicon.dtv.network.service.SubtitleComponent;
import com.hisilicon.dtv.play.EnAudioTrackMode;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LanguageMap;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;


public class SubtitleSelectorDialog extends Dialog implements  OnClickListener
{
    private static final String TAG = "SubtitleSelectorDialog";
    private Player mAvPlayer;

    private PVRFilePlayer mPvrPlayer;

    public static final int subtDialogTypeAV = 1;
    public static final int subtDialogTypePVR = 2;

    private List<SubtitleComponent> mSubtitelList = null;

    private DTV mDTV;

    private int mSubtDialogType;

    private Context mContext;

    private int mCurSubtPos;

    private Channel mCurChannel = null;

    private LanguageMap mLanguageMap;

    private static final int DIALOG_CLOSE = 0;

    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            if(msg.what == DIALOG_CLOSE)
            {
                SubtitleSelectorDialog.this.dismiss();
            }
        }
    };

    public SubtitleSelectorDialog(Context context, int theme, int subtDialogType)
    {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mSubtDialogType = subtDialogType;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LayoutParams params = this.getWindow().getAttributes();
        params.y = (int) mContext.getResources().getDimension(R.dimen.dimen_390px);
        this.getWindow().setAttributes(params);
        initDTV();
        initView();
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onStop()
    {
        super.onStop();
    }

    private void initDTV()
    {
        LogTool.d(LogTool.MSUBTITLE, "SUB:initDTV");
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

        if (subtDialogTypeAV == mSubtDialogType)
        {
            PlayerManager mPlayerManager = mDTV.getPlayerManager();
            mAvPlayer = mPlayerManager.getPlayers().get(0);
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            mPvrPlayer = mDTV.getPVRFilePlayer();
        }

    }

    private void initView()
    {
        if (subtDialogTypeAV == mSubtDialogType)
        {
            if (null != mAvPlayer && null != mAvPlayer.getCurrentSubtitle())
            {
                mCurSubtPos = mAvPlayer.getCurrentSubtitle().getPos();
                mCurChannel = mAvPlayer.getCurrentChannel();
            }

            if (null != mCurChannel)
            {
                mSubtitelList = mCurChannel.getSubtitleComponents();
                if (null == mSubtitelList)
                {
                    mSubtitelList = new ArrayList<SubtitleComponent>();
                }
            }
            else
            {
                mSubtitelList = new ArrayList<SubtitleComponent>();
            }
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            if (null != mPvrPlayer)
            {
                mCurSubtPos = mPvrPlayer.getCurrentSubtitle().getPos();
                mSubtitelList = mPvrPlayer.getSubtitleComponents();
            }

            if (null == mSubtitelList)
            {
                mSubtitelList = new ArrayList<SubtitleComponent>();
            }

        }
        mLanguageMap = new LanguageMap(mContext);
        SubtitleSelectorView view  = new SubtitleSelectorView(this, mContext, "Subtitle Switch");
        setContentView(view);
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        mHandler.removeMessages(DIALOG_CLOSE);
        mHandler.sendEmptyMessageDelayed(DIALOG_CLOSE, Util.DISPEAR_TIME);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View arg0)
    {

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus)
    {
        if(hasWindowFocus)
        {
            mHandler.removeMessages(DIALOG_CLOSE);
            mHandler.sendEmptyMessageDelayed(DIALOG_CLOSE, Util.DISPEAR_TIME);
        }
        else
        {
            mHandler.removeMessages(DIALOG_CLOSE);
        }
    }

    public String getCurrentSubtitle()
    {
        String ret = "";
        if(mCurSubtPos == 0)
        {
            ret = mContext.getResources().getString(R.string.subtitle_off);
        }
        else
        {
            ret = mLanguageMap.getLanguage(mSubtitelList.get(mCurSubtPos).getLanguageCode());
        }

        if(mSubtitelList.size() > 0)
        {
            if (EnSubtitleType.SUBTITLE == mSubtitelList.get(mCurSubtPos).getSubtitleType())
            {
                if (EnSubtComponentType.HOH == mSubtitelList.get(mCurSubtPos).getSubtComponentType())
                {
                    ret = ret +  " (hoh)";
                }
            }
            else if (EnSubtitleType.TELETEXT == mSubtitelList.get(mCurSubtPos).getSubtitleType())
            {
                ret = ret +  " (TTX)";
            }
        }

        return ret;
    }

    public boolean setPreSubtitle()
    {
        if (0 == mSubtitelList.size())
        {
            return false;
        }

        mCurSubtPos --;
        if(mCurSubtPos < 0)
        {
            mCurSubtPos = mSubtitelList.size()-1;
        }
        SubtitleComponent mCurSubtInfo = mSubtitelList.get(mCurSubtPos);

        if (subtDialogTypeAV == mSubtDialogType)
        {
            mAvPlayer.selectSubtitle(mCurSubtInfo);
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            mPvrPlayer.selectSubtitle(mCurSubtInfo);
        }
        return true;
    }

    public boolean  setNextSubtitle()
    {
        if (0 == mSubtitelList.size())
        {
            return false;
        }

        mCurSubtPos ++;
        if(mCurSubtPos > mSubtitelList.size()-1)
        {
            mCurSubtPos = 0;
        }
        SubtitleComponent mCurSubtInfo = mSubtitelList.get(mCurSubtPos);

        if (subtDialogTypeAV == mSubtDialogType)
        {
            mAvPlayer.selectSubtitle(mCurSubtInfo);
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            mPvrPlayer.selectSubtitle(mCurSubtInfo);
        }
        return true;
    }

}
