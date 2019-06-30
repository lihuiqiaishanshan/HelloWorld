package com.hisilicon.tvui.play.cc;

import java.util.ArrayList;
import java.util.List;

import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.android.tvapi.constant.EnumSystemTvSystem;

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
import com.hisilicon.dtv.network.service.ClosedCaptionManager;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;

public class ATVClosedCaptionDialog extends Dialog implements OnItemClickListener
{

    private int mLastPositon = 0;
    private int mPosition = 0;
    private int mGroupIndex;
    private int mTvSystem;
    private String name = null;
    private Context mContext;
    private ListView mListView;
    private ClosedCaptionManager mCCManager;
    private Player mPlayer;
    private PlayerManager mPlayerManager = null;
    private ATVClosedCaptionListAdapter mAdapter;
    private TextView mTextViewCCListName;
    private Ginga ginga;
    private DTV mDTV;
    private ArrayList<String> curCCList = new ArrayList<String>();


    public ATVClosedCaptionDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {

        LogTool.d(LogTool.MSUBTITLE, "===== ATVCC onCreate =====");
        super.onCreate(savedInstanceState);
        LayoutParams params = this.getWindow().getAttributes();
        params.x = (int) mContext.getResources().getDimension(R.dimen.dimen_450px);
        params.y = (int) mContext.getResources().getDimension(R.dimen.dimen_180px);
        params.dimAmount = 0.0f;
        this.getWindow().setAttributes(params);
        initTV();
        initView();

    }

    protected void onStart()
    {
        LogTool.d(LogTool.MSUBTITLE, "===== ATVCC onStart =====");
        super.onStart();

    }

    protected void onStop()
    {
        LogTool.d(LogTool.MSUBTITLE, "===== ATVCC onStop =====");
        super.onStop();

    }

    private void initTV()
    {
        LogTool.d(LogTool.MSUBTITLE, "ATVCC:initDTV Begin");
        mTvSystem = halApi.getTvSystemType();
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mPlayerManager = mDTV.getPlayerManager();
        mPlayer = mPlayerManager.getPlayers().get(0);
        mCCManager = mDTV.getCCManager();


    }
    private void initCClist() {
        ArrayList<String> ccList608 = new ArrayList<String>();
        ccList608.add("off");
        ccList608.add("CC1");
        ccList608.add("CC2");
        ccList608.add("CC3");
        ccList608.add("CC4");
        ccList608.add("TEXT1");
        ccList608.add("TEXT2");
        ccList608.add("TEXT3");
        ccList608.add("TEXT4");
        curCCList = ccList608;
    }
    private void initView()
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:initView begin");
        setContentView(R.layout.cc);
        mListView = (ListView) findViewById(R.id.lv_cc_listview);
        mTextViewCCListName = (TextView) findViewById(R.id.tv_cc_type_title);
        ImageView ArrowL = (ImageView) findViewById(R.id.imageViewL);
        ImageView ArrowR = (ImageView) findViewById(R.id.ImageViewR);
        ArrowL.setVisibility(View.GONE);
        ArrowR.setVisibility(View.GONE);
        initCClist();
        LogTool.d(LogTool.MSUBTITLE, "ATV CC Mode = " + halApi.getCcEnable());
        if (1 == halApi.getCcEnable())
        {
            mPosition = halApi.getCcChannel() + 1;
        }
        mAdapter = new ATVClosedCaptionListAdapter(mContext, curCCList);
        mListView.setAdapter(mAdapter);
        showCCList(curCCList);
        mListView.setOnItemClickListener(this);
        LogTool.d(LogTool.MSUBTITLE, "CC:initView end, mPosition = " + mPosition);

    }

    /**
     * Show the CC and the name of the CCList
     * @param ArrayList<String>
     */
    private void showCCList(ArrayList<String> cclist)
    {
        LogTool.d(LogTool.MSUBTITLE, "CC:showCCList Begin");
        if (null == cclist)
        {
            return;
        }

        mAdapter.setCCList(cclist);
        name = "CC608";
        LogTool.d(LogTool.MSUBTITLE, "CCList name" +name);
        mTextViewCCListName.setText(name);
        mListView.setSelection(mPosition);
        mAdapter.setCurrentPosition(mPosition);
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

        if (0 == position) {
            mCCManager.showCC(false);
            halApi.showCc(false);
            Settings.System.putInt(mContext.getContentResolver(), Util.SETTING_CC_MODE, 0);
        } else{
            if (0 == mPosition) {
                mCCManager.showCC(true);
                halApi.showCc(true);
                Settings.System.putInt(mContext.getContentResolver(), Util.SETTING_CC_MODE, 1);
            }
            mCCManager.setAnalogSelector(EnCCAnalogSelector.values()[ position - 1 ]);
            halApi.setCcChannel(position - 1);
        }
        mPosition = position;
        mAdapter.setCurrentPosition(mPosition);
        mAdapter.notifyDataSetChanged();
    }
}

