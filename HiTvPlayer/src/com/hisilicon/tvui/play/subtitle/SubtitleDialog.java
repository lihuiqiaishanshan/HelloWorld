package com.hisilicon.tvui.play.subtitle;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.service.SubtitleComponent;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;

public class SubtitleDialog extends Dialog implements OnItemClickListener
{
    public static final int subtDialogTypeAV = 1;
    public static final int subtDialogTypePVR = 2;

    private DTV mDTV;

    private int mSubtDialogType;

    private Player mAvPlayer;
    private PVRFilePlayer mPvrPlayer;

    private ListView mListView;

    private List<SubtitleComponent> mSubtitelList = null;

    private SubtitleListAdapter mAdapter;

    private int mCurSubtPos;

    private static final int DTV_TBM_TABLE_TYPE_PMT = 0x2;

    private Channel mCurChannel = null;

    private Context mContext;

    IDTVListener subListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "messageID:" + messageID + " param1: " + param1 + " param2:" + param2);

            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_TBM_UPDATE:
            {
                LogTool.d(LogTool.MPLAY, "messageID=" + messageID + " param1 = " + param1 + " param2 = " + param2);

                if (DTV_TBM_TABLE_TYPE_PMT == param1)
                {
                    //updateSubtList();
                    //MyToast.makeText(mContext, R.string.str_pu_program_updated, MyToast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                break;
            }
        }
    };

    private void subScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_TBM_UPDATE, subListener, 0);
        }
    }

    private void unsubScribeEvent()
    {
        if (null != mDTV)
        {
            mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_TBM_UPDATE, subListener);
        }
    }

    public SubtitleDialog(Context context, int theme, int subtDialogType)
    {
        super(context, theme);
        mContext = context;
        mSubtDialogType = subtDialogType;
    }

    protected void onCreate(Bundle savedInstanceState)
    {

        LogTool.d(LogTool.MSUBTITLE, "SUB:onCreate");
        super.onCreate(savedInstanceState);
        LayoutParams params = this.getWindow().getAttributes();
        params.x = (int) mContext.getResources().getDimension(R.dimen.dimen_450px);
        params.y = (int) mContext.getResources().getDimension(R.dimen.dimen_180px);
        params.dimAmount = 0.0f;
        this.getWindow().setAttributes(params);
        initDTV();
        initView();
    }

    protected void onStart()
    {
        LogTool.d(LogTool.MSUBTITLE, "SUB:onStart");
        super.onStart();

    }

    protected void onStop()
    {
        LogTool.d(LogTool.MSUBTITLE, "SUB:onStop");

        unsubScribeEvent();
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
            subScribeEvent();
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            mPvrPlayer = mDTV.getPVRFilePlayer();
        }

    }

    private void initView()
    {
        LogTool.d(LogTool.MSUBTITLE, "SUB:initView");
        setContentView(R.layout.subtitle);
        mListView = (ListView) findViewById(R.id.lv_sub_subtitlelist);

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

        for (SubtitleComponent temp : mSubtitelList)
        {
            LogTool.d(LogTool.MSUBTITLE, "SUB: lang:" + temp.getLanguageCode() + " pid:" + temp.getPID() + " type:" + temp.getSubtitleType() + " MagazingNum:"
                    + temp.getMagazingNum() + " PageNum:" + temp.getPageNum() + " .");
        }

        mAdapter = new SubtitleListAdapter(mContext, mSubtitelList);

        int curIndex = 0;

        if (subtDialogTypeAV == mSubtDialogType)
        {
            if ((null != mAvPlayer) && (mAvPlayer.isSubtitleVisible()))
            {
                curIndex = mCurSubtPos;
            }
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            if ((null != mPvrPlayer) && (mPvrPlayer.isSubtitleVisible()))
            {
                curIndex = mCurSubtPos;
            }
        }

        mAdapter.setCurrentPosition(curIndex);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(curIndex);
        mListView.setOnItemClickListener(this);
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
        case KeyValue.DTV_KEYVALUE_SUB:
        {
            this.dismiss();
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        {
            if (mListView.getSelectedItemPosition() == 0)
            {
                mListView.setSelection(mListView.getCount() - 1);
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        {
            if (mListView.getSelectedItemPosition() == (mListView.getCount() - 1))
            {
                mListView.setSelectionFromTop(0, 0);
            }
            break;
        }
        default:
        {
            break;
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        SubtitleComponent mCurSubtInfo = (SubtitleComponent) mAdapter.getItem(position);

        if (subtDialogTypeAV == mSubtDialogType)
        {
            mAvPlayer.selectSubtitle(mCurSubtInfo);
        }
        else if (subtDialogTypePVR == mSubtDialogType)
        {
            mPvrPlayer.selectSubtitle(mCurSubtInfo);
        }

        mAdapter.setCurrentPosition(position);
        mAdapter.notifyDataSetChanged();
    }

}
