package com.hisilicon.tvui.play.cc;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.play.Ginga;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.network.service.EnCCAnalogSelector;
import com.hisilicon.dtv.network.service.EnCCDigitalSelector;
import com.hisilicon.dtv.network.service.EnClosedCaptionType;
import com.hisilicon.dtv.network.service.ClosedCaptionComponent;
import com.hisilicon.dtv.network.service.ClosedCaptionList;
import com.hisilicon.dtv.network.service.ClosedCaptionManager;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.hal.halApi;

public class ClosedCaptionDialog extends Dialog implements OnItemClickListener
{

    private ClosedCaptionManager mCCManager;
    private Player mPlayer;
    private PlayerManager mPlayerManager = null;
    private int mLastPositon = 0;
    private ListView mListView;
    private String name = null;
    private List<ClosedCaptionList> mCCGroup = null;
    private int mGroupIndex;
    private ClosedCaptionListAdapter mAdapter;
    private TextView mTextViewCCListName;
    private Ginga ginga;
    private Context mContext;
    private DTV mDTV;

    public ClosedCaptionDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {

        LogTool.d(LogTool.MSUBTITLE, "===== CC onCreate =====");
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
        LogTool.d(LogTool.MSUBTITLE, "===== CC onStart =====");
        super.onStart();

    }

    protected void onStop()
    {
        LogTool.d(LogTool.MSUBTITLE, "===== CC onStop =====");
        super.onStop();

    }

    private void initDTV()
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:initDTV Begin");

        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mPlayerManager = mDTV.getPlayerManager();
        mPlayer = mPlayerManager.getPlayers().get(0);
        mCCManager = mDTV.getCCManager();

    }

    private void initView()
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:initView begin");
        setContentView(R.layout.cc);
        mListView = (ListView) findViewById(R.id.lv_cc_listview);
        mTextViewCCListName = (TextView) findViewById(R.id.tv_cc_type_title);

        if (null == mCCManager)
        {
            return;
        }

        if (null != mPlayer)
        {
            Channel mCurChannel = mPlayer.getCurrentChannel();

            LogTool.d(LogTool.MSUBTITLE, "CC:initView mCurChannel" + mCurChannel);
        }

        mCCGroup = mCCManager.getUsedCCLists();

        LogTool.d(LogTool.MSUBTITLE, "CC:initView mCCGroup" + mCCGroup);

        if (null == mCCGroup)
        {
            mCCGroup = new ArrayList<ClosedCaptionList>();
        }

        LogTool.d(LogTool.MSUBTITLE, "CC:initView ,mCCGroup.size: " + mCCGroup.size() + "");

        if (0 == mCCGroup.size())
        {
            return;
        }

        ImageView ArrowL = (ImageView) findViewById(R.id.imageViewL);
        ImageView ArrowR = (ImageView) findViewById(R.id.ImageViewR);

        if (1 == mCCGroup.size())
        {
            ArrowL.setVisibility(View.GONE);
            ArrowR.setVisibility(View.GONE);
        }

        int i = 0;
        int curIndex = 0;
        int curListPos = 0;
        int groupCount = 0;
        int groupType = 0;

        groupCount = mCCGroup.size();
        for (i = 0; i < groupCount; i++)
        {
            groupType = mCCGroup.get(i).getListType().getValue();
            if (null != mCCManager.getCurrentCC() && null != mCCManager.getCurrentCC().getDataType()
                    && groupType == mCCManager.getCurrentCC().getDataType().getValue()) {
                curListPos = i;
                break;
            }
        }

        LogTool.d(LogTool.MSUBTITLE, "CC:initView curListPos = " + curListPos + "curIndex = " + curIndex);

        ClosedCaptionList curCCList = mCCGroup.get(curListPos);
        LogTool.d(LogTool.MSUBTITLE, "cc data type:" + curCCList.getListType());
        if (mCCManager.isCCShow())
        {
            LogTool.d(LogTool.MSUBTITLE, "isCCShow true");

            if (null != mCCManager.getCurrentCC())
            {

                switch (curCCList.getListType()) {
                    case CCARIB:
                        curIndex = 1;
                        break;
                    case CC608:
                        curIndex = mCCManager.getAnalogSelector().getValue() + 1;
                        break;
                    case CC708:
                        curIndex= mCCManager.getDigitalSelector().getValue();
                        break;
                    default:
                        break;
                }
            }
        }

        mAdapter = new ClosedCaptionListAdapter(mContext, curCCList);

        curCCList.setCurrentPosition(curIndex);

        //mAdapter.setCurrentPosition(curIndex);
        //mListView.setSelection(curIndex);
        mListView.setAdapter(mAdapter);

        showCCList(curCCList);

        mListView.setOnItemClickListener(this);

        LogTool.d(LogTool.MSUBTITLE, "CC:initView end, curIndex = " + curIndex);
    }

    /**
     * Show the CC and the name of the ClosedCaptionList
     * @param ClosedCaptionList
     */
    private void showCCList(ClosedCaptionList cclist)
    {
        int position = 0;

        LogTool.d(LogTool.MSUBTITLE, "CC:showCCList Begin");
        if (null == cclist)
        {
            return;
        }

        mAdapter.setCCList(cclist);

        name = cclist.getListName();
        LogTool.d(LogTool.MSUBTITLE, "CCList name" +name);
        mTextViewCCListName.setText(name);

        position = cclist.getCurrentPosition();
        mLastPositon = position;
        mListView.setSelection(position);

        mAdapter.setCurrentPosition(position);

        ClosedCaptionComponent mCurCCInfo = (ClosedCaptionComponent) mAdapter.getItem(position);
        if (null != mCurCCInfo)
        {
            mAdapter.getCCList().setCurrentPosition(position);
        }

        //if (mListView.getSelectedItemPosition() >= mListView.getCount())
        //{
        //    mListView.setSelection(mListView.getCount() - 1);
        //}

        //LogTool.d(LogTool.MSUBTITLE, "-------CC:showCCList getSelectedItemPosition =----" + mListView.getSelectedItemPosition());
        //LogTool.d(LogTool.MSUBTITLE, "-------CC:showCCList getCount =----" + mListView.getCount());

        //mAdapter.setCurrentPosition(mListView.getSelectedItemPosition());
    }

    /**
     * Get the up channelList in all groups.
     * @param bUp true get the up channelList, false get the down channelList.
     * @return channelList.
     */
    private ClosedCaptionList getUpCCList(boolean bUp)
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:getUpCCList");

        if (null == mCCGroup || mCCGroup.size() <= 1)
        {
            return null;
        }
        else
        {
            int groupCount = mCCGroup.size();
            if (bUp)
            {
                mGroupIndex = (mGroupIndex + groupCount - 1) % groupCount;
            }
            else
            {
                mGroupIndex = (mGroupIndex + 1) % groupCount;
            }


            if (mCCManager.isCCShow())
            {
                if ( 0 == mGroupIndex){
                    int index = mCCManager.getAnalogSelector().getValue();
                    LogTool.d(LogTool.MSUBTITLE, "CC:608Index= " +index);
                    mCCGroup.get(mGroupIndex).setCurrentPosition( index + 1 );
                } else {
                    int index = mCCManager.getDigitalSelector().getValue();
                    LogTool.d(LogTool.MSUBTITLE, "CC:708Index= " +index);
                    mCCGroup.get(mGroupIndex).setCurrentPosition( index );
                }
            }
            else
            {
                LogTool.d(LogTool.MSUBTITLE, "CC does't show ,set position 0 ");
                mCCGroup.get(mGroupIndex).setCurrentPosition(0);
            }

            return mCCGroup.get(mGroupIndex);
        }
    }

    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {

        LogTool.d(LogTool.MSUBTITLE, "CC:onKeyDown");

        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
        case KeyValue.DTV_KEYVALUE_RED:
        {
            this.dismiss();
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            ClosedCaptionList upCClList = getUpCCList(true);
            showCCList(upCClList);
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            ClosedCaptionList downCCList = getUpCCList(false);
            showCCList(downCCList);
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

    public void closeGinga ( ) {
        DTVConfig dtvConfig = mDTV.getConfig();
        if (0 == mPlayerManager.getPlayers().size()) {
            ginga = mPlayerManager.createPlayer().getGinga();
        } else {
            ginga = mPlayerManager.getPlayers().get(0).getGinga();
        }
        if (1 == dtvConfig.getInt("bGingaMode", 0)) {
            ginga.deinit();
            LogTool.d(LogTool.MSUBTITLE, " close ginga " );
            dtvConfig.setInt("bGingaMode", 0);
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:onItemClick : pos " + position);

        ClosedCaptionComponent mCurCCInfo = (ClosedCaptionComponent) mAdapter.getItem(position);

        if (null != mCurCCInfo)
        {
            if (name.equals("CC608")) {
                if (0 == position) {
                    mCCManager.showCC(false);
                    halApi.showCc(false);
                    Settings.System.putInt(mContext.getContentResolver(), Util.SETTING_CC_MODE, 0);
                } else{
                    mCCManager.showCC(true);
                    halApi.showCc(true);
                    //set CC mode on
                    if (0 == mLastPositon) {
                        Settings.System.putInt(mContext.getContentResolver(), Util.SETTING_CC_MODE, 1);
                    }
                    mCCManager.setAnalogSelector(EnCCAnalogSelector.values()[ position - 1 ]);
                    halApi.setCcChannel(position - 1);
                }
            } else if (name.equals("CC708")) {
                mCCManager.setDigitalSelector(EnCCDigitalSelector.values()[position]);
            } else if (name.equals("CCARIB")) {
                mCCManager.showCC(1 == position);
                halApi.showCc(1 == position);
                Settings.System.putInt(mContext.getContentResolver(), Util.SETTING_CC_MODE, position);
                if (1 == position) {
                    closeGinga();
                }
            }
            mLastPositon = position;
            mAdapter.getCCList().setCurrentPosition(position);

            mAdapter.setCurrentPosition(position);
            mAdapter.notifyDataSetChanged();
        }
    }
}
