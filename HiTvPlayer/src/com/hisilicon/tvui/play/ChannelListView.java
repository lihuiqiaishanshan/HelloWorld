package com.hisilicon.tvui.play;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.channellist.ChannelListAdapter;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.MyToast;

public class ChannelListView extends BaseView implements OnItemClickListener, OnItemSelectedListener
{
    private final MainActivity        mMainActivity;
    private final ListView            mListView;
    private final EpgListView         mEpgListView;
    private final TextView            mTextViewChannelListName;
    private View                mCasDialogView;
    private View                mAlphaDialogView;
    private ImageView           mArrowLeft = null;
    private ImageView           mArrowRight = null;

    private ImageView           mImgViewTipBlue = null;
    private TextView            mTextViewTipBlue = null;

    private AlertDialog         mCasAlertDialog;
    private AlertDialog         mAlphaAlertDialog;
    private List<Integer>       mAlphaAlertDialogButtonsID;

    private List<ChannelList>   mDtvAllGroups = null;
    private List<ChannelList>   mAtvAllGroups = null;
    private List<ChannelList>   mAllGroups = null;

    private ChannelList         mCurDtvChannelList = null;
    private ChannelList         mCurAtvChannelList = null;
    private int                 mGroupIndex = 0;
    private EnEntranceType      mEntranceType = EnEntranceType.CURRENT_GROUP;

    public enum EnEntranceType
    {
        CURRENT_GROUP,
        ALL_GROUP,
        FAV_GROUP
    }

    public ChannelListView(MainActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.ly_channel_list));
        mMainActivity = arg0;
        mEpgListView = new EpgListView(mMainActivity);
        mListView = (ListView) mMainActivity.findViewById(R.id.channelListView);
        mListView.setOnItemClickListener(this);
        mListView.setOnItemSelectedListener(this);
        mTextViewChannelListName = (TextView) mMainActivity.findViewById(R.id.textViewGroup);
        mArrowLeft = (ImageView) mMainActivity.findViewById(R.id.imageViewArrow1);
        mArrowRight = (ImageView) mMainActivity.findViewById(R.id.imageViewArrow2);

        mImgViewTipBlue = (ImageView) mMainActivity.findViewById(R.id.ImgBlue);
        mTextViewTipBlue = (TextView) mMainActivity.findViewById(R.id.TipBlue);

        initCASDialogView();
        initAlphaDialogView();
    }

    @Override
    public void show() {
        super.show();
        initChannelListData();
        setChannelListPosition();
        mEpgListView.hide();
    }

    @Override
    public void hide()
    {
        if (!isShow()) {
            return;
        }
        if (mCurAtvChannelList != null)
        {
            ChannelFilter channelFilter = mCurAtvChannelList.getFilter();

            EnScrambleFilter enCAFilter = EnScrambleFilter.ALL;

            channelFilter.setCASType(enCAFilter);
            channelFilter.setFirstLetters("");
            mCurAtvChannelList.setFilter(channelFilter);
        }
        else
        {
            LogTool.d(LogTool.MCHANNEL, "mCurAtvChannelList is null");
        }

        if (mCurDtvChannelList != null)
        {
            ChannelFilter channelFilter = mCurDtvChannelList.getFilter();

            EnScrambleFilter enCAFilter = EnScrambleFilter.ALL;

            channelFilter.setCASType(enCAFilter);
            channelFilter.setFirstLetters("");
            mCurDtvChannelList.setFilter(channelFilter);
        }
        else
        {
            LogTool.d(LogTool.MCHANNEL, "mCurDtvChannelList is null");
        }

        mEpgListView.hide();
        if (mCasAlertDialog != null)
        {
            mCasAlertDialog.dismiss();
        }
        if (mAlphaAlertDialog != null)
        {
            mAlphaAlertDialog.dismiss();
        }

        mEntranceType = EnEntranceType.CURRENT_GROUP;
        super.hide();
    }

    @Override
    public void toggle()
    {
        if (super.isShow())
        {
            hide();
        }
        else
        {
            show();
        }
    }

    public void show(EnEntranceType type)
    {
        mEntranceType = type;
        show();
    }

    public void directShowEPG()
    {
        show(EnEntranceType.ALL_GROUP);
        showEPG();
    }

    public void directShowFavlist()
    {
        show(EnEntranceType.FAV_GROUP);
    }

    private void showEPG()
    {
        Channel tempChannel = (Channel) mListView.getSelectedItem();
        if (null == tempChannel)
        {
            return;
        }

        if (halApi.isDTVSource(mMainActivity.mCurSourceId)
                && (tempChannel.getNetworkType() != EnNetworkType.RF))
        {
            ChannelList currentList = mAllGroups.get(mGroupIndex);
            mEpgListView.show(tempChannel);
        }
    }

    /**
     * Initialization the data of channel list
     */
    private void initChannelListData()
    {
        String groupName = "";

        ChannelList curSourceChannellist = null;
        ChannelList otherSourceChannellist = null;

        mDtvAllGroups = mChannelManager.getUseGroups();
        mAtvAllGroups = mAtvChannelManager.getUseGroups();
        List<ChannelList> mDtvFavGroups = mChannelManager.getUseFavGroups();
        List<ChannelList> mAtvFavGroups = mAtvChannelManager.getUseFavGroups();

        List<ChannelList> mFavGroups = null;
        if (halApi.isDTVSource(mMainActivity.mCurSourceId))
        {
            mAllGroups = mDtvAllGroups;
            mFavGroups = mDtvFavGroups;

            curSourceChannellist = mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_DVBC);
            if (null != mAtvAllGroups && 0 < mAtvAllGroups.size())
            {
                otherSourceChannellist = mAtvAllGroups.get(0);
            }

            if (null == curSourceChannellist && null != mDtvAllGroups && 0 < mDtvAllGroups.size())
            {
                curSourceChannellist = mDtvAllGroups.get(0);
            }
        }
        else
        {
            mAllGroups = mAtvAllGroups;
            mFavGroups = mAtvFavGroups;

            curSourceChannellist = mChnHistory.getCurrentList(halApi.EnumSourceIndex.SOURCE_ATV);
            if (null != mDtvAllGroups && 0 < mDtvAllGroups.size())
            {
                otherSourceChannellist = mDtvAllGroups.get(0);
            }

            if (null == curSourceChannellist && null != mAtvAllGroups && 0 < mAtvAllGroups.size())
            {
                curSourceChannellist = mAtvAllGroups.get(0);
            }
        }

        if (EnEntranceType.CURRENT_GROUP == mEntranceType)
        {
            if (null != curSourceChannellist)
            {
                groupName = curSourceChannellist.getListName();
            }
            if (null != groupName && groupName.equals(CommonValue.ALLLIST_NAME))
            {
                groupName = mMainActivity.getResources().getString(R.string.all);
            }
            if (!mMainActivity.getResources().getString(R.string.all).equals(groupName)) {
                otherSourceChannellist = null;
                if (null != groupName && groupName.equals("ATSC")) {
                    groupName = mMainActivity.getResources().getString(R.string.all);
                }
            }
            mGroupIndex = getGroupIndex(curSourceChannellist);
        }
        else if (EnEntranceType.ALL_GROUP == mEntranceType)
        {
            if(mAllGroups != null && mAllGroups.size() > 0)
            {
                curSourceChannellist = mAllGroups.get(0);
            }
            groupName = mMainActivity.getResources().getString(R.string.all);
            mGroupIndex = 0;
        }
        else if (EnEntranceType.FAV_GROUP == mEntranceType)
        {
            if ( null != mFavGroups && 0 < mFavGroups.size())
            {
                curSourceChannellist = mFavGroups.get(0);
                groupName = curSourceChannellist.getListName();
                mGroupIndex = getGroupIndex(curSourceChannellist);
            }
            else
            {
                curSourceChannellist = null;
                groupName = CommonValue.FAVLIST_NAME;
                mGroupIndex = mAllGroups.size() - 1;
            }
            otherSourceChannellist = null;
        }

        if (halApi.isDTVSource(mMainActivity.mCurSourceId))
        {
            mCurDtvChannelList = curSourceChannellist;
            mCurAtvChannelList = otherSourceChannellist;
        }
        else
        {
            mCurAtvChannelList = curSourceChannellist;
            mCurDtvChannelList = otherSourceChannellist;
        }

        //过滤隐藏频道
        if (null != mCurDtvChannelList)
        {
            ChannelFilter channelFilter = mCurDtvChannelList.getFilter();
            if (channelFilter != null)
            {
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);

                EnTVRadioFilter radioFilter = mChannelManager.getChannelServiceTypeMode();
                channelFilter.setGroupType(radioFilter);

                mCurDtvChannelList.setFilter(channelFilter);
            }
        }

        if (null != mCurAtvChannelList)
        {
            ChannelFilter atvChannelFilter = mCurAtvChannelList.getFilter();
            if (atvChannelFilter != null)
            {
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                atvChannelFilter.setTagType(mEditTypes);
                mCurAtvChannelList.setFilter(atvChannelFilter);
            }
        }
        if (!TextUtils.isEmpty(groupName)) {
            mTextViewChannelListName.setText(groupName);
        }
    }

    /**
     * Set the position of the current channel.
     */
    private void setChannelListPosition()
    {
        Channel channel = mChnHistory.getCurrentChn(mMainActivity.mCurSourceId);
        int index = 0;

        if (null != channel)
        {
            int currentChannelID = channel.getChannelID();
            // 所有分组中，DTV和ATV频道混合编排
            if(channel.getNetworkType() != EnNetworkType.RF)
            {
                if (null != mCurDtvChannelList)
                {
                    index = mCurDtvChannelList.getPosByChannelID(currentChannelID);
                }
            }
            else
            {
                if (null != mCurAtvChannelList)
                {
                    index = mCurAtvChannelList.getPosByChannelID(currentChannelID);
                }
                if (null != mCurDtvChannelList)
                {
                    index = mCurDtvChannelList.getChannelCount() + ((index >= 0) ? index : 0);
                }
            }
        }

        ChannelListAdapter mAdapter = new ChannelListAdapter(mMainActivity, mCurDtvChannelList, mCurAtvChannelList);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(index);

        if(null != mAllGroups && 1 < mAllGroups.size())
        {
            mArrowLeft.setVisibility(View.VISIBLE);
            mArrowRight.setVisibility(View.VISIBLE);
        }
        else
        {
            mArrowLeft.setVisibility(View.GONE);
            mArrowRight.setVisibility(View.GONE);
        }

        if (halApi.isDTVSource(mMainActivity.mCurSourceId))
        {
            mImgViewTipBlue.setVisibility(View.VISIBLE);
            mTextViewTipBlue.setVisibility(View.VISIBLE);
        }
        else
        {
            mImgViewTipBlue.setVisibility(View.GONE);
            mTextViewTipBlue.setVisibility(View.GONE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        Channel tempChannel = (Channel) (arg0.getAdapter()).getItem(arg2);
        LogTool.d(LogTool.MCHANNEL, "ChannelListView onItemClick arg2  = " + arg2);
        if (null == tempChannel)
        {
            return;
        }
        LogTool.d(LogTool.MCHANNEL, "ChannelListView tempChannel.name = " + tempChannel.getChannelName());
        LogTool.d(LogTool.MCHANNEL, "ChannelListView tempChannel.id = " + tempChannel.getChannelID());
        boolean bNeedChange = true;
        ChannelList currentList = null;
        EnNetworkType   networkType = tempChannel.getNetworkType();
        // get current channel list
        if( EnNetworkType.RF == networkType)
        {
            currentList = mAtvAllGroups.get(mGroupIndex);
        }
        else
        {
            currentList = mDtvAllGroups.get(mGroupIndex);
            ChannelFilter channelFilter = currentList.getFilter();

            EnScrambleFilter enCAFilter = EnScrambleFilter.ALL;

            channelFilter.setCASType(enCAFilter);
            channelFilter.setFirstLetters("");
        }

        // 如果节目类型与当前源不对应，先切源
        if (EnNetworkType.RF == networkType && halApi.isDTVSource(mMainActivity.mCurSourceId))
        {
            halApi.changeSource(mMainActivity.mCurSourceId,
                    halApi.EnumSourceIndex.SOURCE_ATV, tempChannel.getChannelID());
            mMainActivity.mCurSourceId = halApi.EnumSourceIndex.SOURCE_ATV;
            mMainActivity.hideTipMsgView();
            bNeedChange = false;
        }
        else if (EnNetworkType.RF != networkType && halApi.isATVSource(mMainActivity.mCurSourceId))
        {
            int dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
            switch(networkType)
            {
            case CABLE:
                dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                break;
            case TERRESTRIAL:
                dstSource = halApi.EnumSourceIndex.SOURCE_DVBT;
                break;
            case DTMB:
                dstSource = halApi.EnumSourceIndex.SOURCE_DTMB;
                break;
            case ISDB_TER:
                dstSource = halApi.EnumSourceIndex.SOURCE_ISDBT;
                break;
            case ATSC_T:
                dstSource = halApi.EnumSourceIndex.SOURCE_ATSC;
                break;
            default:
                break;
            }
            // current not DTV, change source to DTV
            halApi.setSourceHolder();
            halApi.changeSource(mMainActivity.mCurSourceId, dstSource);
            mMainActivity.mCurSourceId = dstSource;
            mMainActivity.setSurfaceVisible(mMainActivity.mCurSourceId);
            mMainActivity.hideTipMsgView();
            // resume DTV resource
            mDTV.prepareDTV();
            mPlayer.resumeResource();
        }

        mMainActivity.playChannel(currentList, tempChannel, true, bNeedChange);
        mMainActivity.setPipSurfaceToDisplay();
        hide();
    }

    /**
     * Get the index of the channelList in all groups.
     * @param list channel list.
     * @return index, default is 0.
     */
    private int getGroupIndex(ChannelList list)
    {
        if (null != list && null != mAllGroups)
        {
            for (int i = 0; i < mAllGroups.size(); i++)
            {
                if (list.getListName().equals(mAllGroups.get(i).getListName()))
                {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * switch channelList in all groups.
     */
    private void switchChannelListGroup()
    {
        String name = null;
        if (null == mAllGroups || mAllGroups.size() <= 1)
        {
            return;
        }

        int groupCount = mAllGroups.size();
        mGroupIndex = (mGroupIndex + 1) % groupCount;

        if (halApi.isDTVSource(mMainActivity.mCurSourceId))
        {
            mCurDtvChannelList = mAllGroups.get(mGroupIndex);
            name = mCurDtvChannelList.getListName();
            if (0 == mGroupIndex && mAtvAllGroups.size() > 0)
            {
                mCurAtvChannelList = mAtvAllGroups.get(mGroupIndex);
            }
            else
            {
                mCurAtvChannelList = null;
            }
        }
        else
        {
            mCurAtvChannelList = mAllGroups.get(mGroupIndex);
            name = mCurAtvChannelList.getListName();
            if (0 == mGroupIndex && mDtvAllGroups.size() > 0)
            {
                mCurDtvChannelList = mDtvAllGroups.get(mGroupIndex);
            }
            else
            {
                mCurDtvChannelList = null;
            }
        }

        if (name.equals(CommonValue.ALLLIST_NAME))
        {
            name = mMainActivity.getResources().getString(R.string.all);
        }

        mTextViewChannelListName.setText(name);

        if (null != mCurDtvChannelList)
        {
            ChannelFilter channelFilter = mCurDtvChannelList.getFilter();
            EnTagType mSkipTagType = EnTagType.HIDE;
            List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
            mEditTypes.add(mSkipTagType);
            channelFilter.setTagType(mEditTypes);
            mCurDtvChannelList.setFilter(channelFilter);
        }

        if (null != mCurAtvChannelList)
        {
            ChannelFilter channelFilter = mCurAtvChannelList.getFilter();
            EnTagType mSkipTagType = EnTagType.HIDE;
            List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
            mEditTypes.add(mSkipTagType);
            channelFilter.setTagType(mEditTypes);
            mCurAtvChannelList.setFilter(channelFilter);
        }
        ((ChannelListAdapter) mListView.getAdapter()).setChannelList(mCurDtvChannelList, mCurAtvChannelList);
    }

    public boolean onListViewKeyDown(int keyCode, android.view.KeyEvent event)
    {
        LogTool.d(LogTool.MCHANNEL, "ChannelListView onListViewKeyDown");
        Channel tempChannel = (Channel) mListView.getSelectedItem();
        if (mEpgListView.isShow())
        {
            return mEpgListView.onListViewKeyDown(keyCode, event);
        }

        if (KeyValue.DTV_KEYVALUE_BACK == keyCode)
        {
            hide();
            mMainActivity.openGinga(true);
        }
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            mEpgListView.hide();
            break;
        }
        case KeyValue.DTV_KEYVALUE_EPG:
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            showEPG();
            return true;
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
            LogTool.d(LogTool.MCHANNEL, "mListView.getSelectedItemPosition() = " + mListView.getSelectedItemPosition());
            LogTool.d(LogTool.MCHANNEL, "mListView.getCount() - 1 = " + (mListView.getCount() - 1));
            if (mListView.getSelectedItemPosition() == (mListView.getCount() - 1))
            {
                mListView.setSelectionFromTop(0, 0);
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_RED:
        {
            showAlphaDialog();
            break;
        }
        case KeyValue.DTV_KEYVALUE_GREEN:
        {
            showCASDialog();
            break;
        }
        case KeyValue.DTV_KEYVALUE_YELLOW:
        {
            // switch group
            switchChannelListGroup();
            return true;
        }
        case KeyValue.DTV_KEYVALUE_BLUE:
        {
            if (halApi.isDTVSource(mMainActivity.mCurSourceId) && null != tempChannel) {
                mEpgListView.showBookDialog(null, tempChannel);
            } else {
                MyToast.makeText(mMainActivity, R.string.epg_book_scramble_cannot_book, MyToast.LENGTH_SHORT).show();
            }
            return true;
        }
        default:
            break;
        }
        return false;
    }

    @SuppressLint("InflateParams")
    private void initCASDialogView()
    {
        mCasDialogView = LayoutInflater.from(mMainActivity).inflate(R.layout.channel_list_cas_dialog, null);
        Button button_channellist_cas_all = (Button) mCasDialogView.findViewById(R.id.button_channellist_cas_all);
        Button button_channellist_cas_fat = (Button) mCasDialogView.findViewById(R.id.button_channellist_cas_fat);
        Button button_channellist_cas_scramble = (Button) mCasDialogView.findViewById(R.id.button_channellist_cas_scramble);

        button_channellist_cas_all.setOnClickListener(mClickListener);
        button_channellist_cas_fat.setOnClickListener(mClickListener);
        button_channellist_cas_scramble.setOnClickListener(mClickListener);
    }

    /**
     * Initialization the data of the dialog for alphabetic filter.
     */
    @SuppressLint("InflateParams")
    private void initAlphaDialogView()
    {
        LogTool.d(LogTool.MCHANNEL, "initAlphaDialogView");
        mAlphaDialogView = LayoutInflater.from(mMainActivity).inflate(R.layout.channel_list_alpha_dialog, null);
        List<Button> mAlphaAlertDialogButtons = new ArrayList<Button>();
        mAlphaAlertDialogButtonsID = new ArrayList<Integer>();
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_all);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_a);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_b);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_c);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_d);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_e);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_f);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_g);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_h);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_i);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_j);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_k);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_l);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_m);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_n);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_o);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_p);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_q);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_r);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_s);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_t);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_u);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_v);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_w);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_x);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_y);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_alpha_z);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_0);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_1);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_2);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_3);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_4);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_5);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_6);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_7);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_8);
        mAlphaAlertDialogButtonsID.add(R.id.button_channellist_digital_9);
        for (int i = 0; i < mAlphaAlertDialogButtonsID.size(); i++)
        {
            Button tempButton = (Button) mAlphaDialogView.findViewById(mAlphaAlertDialogButtonsID.get(i));
            mAlphaAlertDialogButtons.add(tempButton);
            tempButton.setOnClickListener(mClickListener);
        }
        for (int i = 0; i < mAlphaAlertDialogButtonsID.size(); i++)
        {
            int iButtonTotalNumber = 37;
            int iButtonPerRowNumber = 10;
            int iFullNumber = 40;
            Button tempButton = (Button) mAlphaDialogView.findViewById(mAlphaAlertDialogButtonsID.get(i));
            tempButton.setNextFocusLeftId(mAlphaAlertDialogButtonsID.get((i - 1 + iButtonTotalNumber) % iButtonTotalNumber));
            tempButton.setNextFocusRightId(mAlphaAlertDialogButtonsID.get((i + 1 + iButtonTotalNumber) % iButtonTotalNumber));
            int iUpId = (i - iButtonPerRowNumber + iFullNumber) % iFullNumber;
            if ( iUpId >= iButtonTotalNumber )
            {
                iUpId -= iButtonPerRowNumber;
            }
            tempButton.setNextFocusUpId(mAlphaAlertDialogButtonsID.get(iUpId));
            int iDownId = (i + iButtonPerRowNumber + iFullNumber) % iFullNumber;
            if ( iDownId >= iButtonTotalNumber )
            {
                iDownId += iButtonPerRowNumber;
                iDownId %= iFullNumber;
            }
            tempButton.setNextFocusDownId(mAlphaAlertDialogButtonsID.get(iDownId));
        }
        for (int i = 1; i < mAlphaAlertDialogButtonsID.size(); i++)
        {
            char code = 'A';
            String str = "";
            if ( i < 27 )
            {
                code = 'A';
                str = String.valueOf((char) (code + i - 1));
            }
            else if (i >= 27)
            {
                code = '0';
                str = String.valueOf((char) (code + i - 27));
            }
            Button tempButton = mAlphaAlertDialogButtons.get(i);
            tempButton.setText(str);
        }
    }

    /**
     * Show dialog of the CAS.
     */
    private void showCASDialog()
    {
        if (null == mCasDialogView)
        {
            return;
        }

        if (null == mCasAlertDialog)
        {
            mCasAlertDialog = new AlertDialog.Builder(mMainActivity, R.style.DIM_STYLE).create();
            mCasAlertDialog.show();
            mCasAlertDialog.addContentView(mCasDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        else
        {
            if (!mCasAlertDialog.isShowing())
            {
                mCasAlertDialog.show();
                Button button_channellist_cas_all = (Button) mCasDialogView.findViewById(R.id.button_channellist_cas_all);
                button_channellist_cas_all.requestFocus();
            }
        }
    }

    /**
     * Show dialog of the alphabetic filter.
     */
    private void showAlphaDialog()
    {
        LogTool.d(LogTool.MCHANNEL, "showAlphaDialog");
        if (null == mAlphaDialogView)
        {
            return;
        }
        if (null == mAlphaAlertDialog)
        {
            mAlphaAlertDialog = new AlertDialog.Builder(mMainActivity, R.style.DIM_STYLE).create();
            mAlphaAlertDialog.show();
            mAlphaAlertDialog.addContentView(mAlphaDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
        else
        {
            if (!mAlphaAlertDialog.isShowing())
            {
                mAlphaAlertDialog.show();
                Button button_channellist_Alpha_all = (Button) mAlphaAlertDialog.findViewById(R.id.button_channellist_alpha_all);
                button_channellist_Alpha_all.requestFocus();
            }
        }
    }

    /**
     * Show channelList by alphabetic filter
     * @param str
     */
    private void showCurrentChannelListByAlpha(String str)
    {
        LogTool.d(LogTool.MCHANNEL, "showCurrentChannelListByAlpha str = " + str);
        if (null == str)
        {
            return;
        }

        if (null != mCurDtvChannelList)
        {
            LogTool.d(LogTool.MCHANNEL, "null != mCurDtvChannelList");
            ChannelFilter channelFilter = mCurDtvChannelList.getFilter();
            if (null != channelFilter)
            {
                channelFilter.setFirstLetters(str);
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);
                mCurDtvChannelList.setFilter(channelFilter);
            }
            LogTool.d(LogTool.MCHANNEL, "mCurDtvChannelList.count = " + mCurDtvChannelList.getChannelCount());
        }

        if (null != mCurAtvChannelList)
        {
            ChannelFilter channelFilter = mCurAtvChannelList.getFilter();
            if (null != channelFilter)
            {
                channelFilter.setFirstLetters(str);
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);
                mCurAtvChannelList.setFilter(channelFilter);
            }
        }
        ((ChannelListAdapter) mListView.getAdapter()).setChannelList(mCurDtvChannelList, mCurAtvChannelList);
    }

    /**
     * Show channelList by CAS filter.
     * @param casFilter
     */
    private void showCurrentChannelListByCAS(EnScrambleFilter casFilter)
    {
        if (null == casFilter)
        {
            return;
        }

        if (null != mCurDtvChannelList)
        {
            ChannelFilter channelFilter = mCurDtvChannelList.getFilter();
            if (null != channelFilter)
            {
                channelFilter.setCASType(casFilter);
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);
                mCurDtvChannelList.setFilter(channelFilter);
            }
        }

        if (null != mCurAtvChannelList)
        {
            ChannelFilter channelFilter = mCurAtvChannelList.getFilter();
            if (null != channelFilter)
            {
                channelFilter.setCASType(casFilter);
                EnTagType mSkipTagType = EnTagType.HIDE;
                List<EnTagType> mEditTypes = new ArrayList<EnTagType>();
                mEditTypes.add(mSkipTagType);
                channelFilter.setTagType(mEditTypes);
                mCurAtvChannelList.setFilter(channelFilter);
            }
        }

        ((ChannelListAdapter) mListView.getAdapter()).setChannelList(mCurDtvChannelList, mCurAtvChannelList);
    }

    private final OnClickListener mClickListener = new OnClickListener()
    {
        @Override
        public void onClick(View arg0)
        {
            LogTool.d(LogTool.MCHANNEL, "OnClickListener onClick");
            if (mAlphaAlertDialog != null && mAlphaAlertDialog.isShowing() && mAlphaAlertDialogButtonsID.contains(arg0.getId()))
            {
                String alpha = ((Button) arg0).getText().toString();
                if (alpha.length() > 1) // "ALL" length is more than one.
                {
                    showCurrentChannelListByAlpha("");
                }
                else
                // others length is one.
                {
                    showCurrentChannelListByAlpha(alpha);
                }
                mAlphaAlertDialog.dismiss();
                return;
            }

            switch (arg0.getId())
            {
            case R.id.button_channellist_cas_all:
            {
                showCurrentChannelListByCAS(EnScrambleFilter.ALL);
                break;
            }
            case R.id.button_channellist_cas_fat:
            {
                showCurrentChannelListByCAS(EnScrambleFilter.FTA);
                break;
            }
            case R.id.button_channellist_cas_scramble:
            {
                showCurrentChannelListByCAS(EnScrambleFilter.SCRAMBLE);
                break;
            }
            default:
                break;
            }
            mCasAlertDialog.dismiss();
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        // When the first item is selected, the marquee effect does not work. Refresh selected state.
        if (arg1 != null) {
            arg1.setSelected(false);
            arg1.setSelected(true);
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {

    }
}
