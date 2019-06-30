package com.hisilicon.tvui.channelmanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.AtvChannelManager;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.channel.EnFavTag;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnSortType;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.CheckPassWordDialog.CheckPassWordDialogInterface;
import com.hisilicon.tvui.view.ConfirmDialog;
import com.hisilicon.tvui.view.ConfirmDialog.OnConfirmDialogListener;
import com.hisilicon.tvui.view.FavoriteSelectDialog;
import com.hisilicon.tvui.view.MyToast;

public class ChannelManagerActivity extends BaseActivity implements OnItemClickListener, OnItemSelectedListener {

    private static final String CURRENT_SORT_TYPE = "current_sort_type";

    private ListView mGroupListView;
    private ListView mChannelListView;
    private TextView mTextViewChannelManagerTitle;
    private TextView mTextViewChannelManagerPosition;
    private View mMultiOprateDialogView;
    private View mSureToDelDialogView;
    private View mSureToDelDialogAllView;
    private AlertDialog mMultiOprateDialog;
    private AlertDialog mSureToDelDialog;
    private AlertDialog mSureToDelAllDialog;
    private FavoriteSelectDialog mAddToFavoriteDialog;
    private AlertDialog mSortDialog;
    private GroupsListAdapter mGroupsListAdapter;
    private ChannelManagerChannelListAdapter mChannelListAdapter;
    private ChannelManagerAdapter mMixChannelListAdapter;
    private EnOperateMode mEnOperateMode = EnOperateMode.OPERATE_MODE_NONE;
    private EnNetworkType mNetworkType;
    private ControlBarView mControlBarView;
    private int mPositionOfChannelToMove = -1;
    private boolean mIsNeedSaveData = false;
    private boolean mFirstSelected = true;
    private Channel mFavoriteChannel;
    private List<Channel> mChannelToDelList;
    private SharedPreferences mSharedPreferences;
    private final String[] msortArray = new String[]{"default", "ServiceName", "FTA", "SrvcIDTSID", "LCN", "TPID", "Lock"};

    private Channel mSeletedChannel;
    private int mCurrentPositionOfChannel = -1;
    private ChannelManagerActivity mContext = null;
    private EnTVRadioFilter mplayMode = null;
    private int currentChannelID = 0;
    private CheckPassWordDialog.Builder mCheckPWBuilder = null;
    private ChannelList mChannelList = null;
    private int mCurSourceId = -1;
    private ChannelList atvChannelList = null;
    private ChannelList dtvChannelList = null;
    private List<Channel> mMixChannelList;
    private boolean isNeedMixSort = false;

    private enum EnOperateMode {
        OPERATE_MODE_NONE, OPERATE_MODE_MOVE, OPERATE_MODE_SKIP, OPERATE_MODE_LOCK, OPERATE_MODE_DELETE, OPERATE_MODE_FAVORITE
    }

    private class ControlBarView {
        public ImageView imageView_channel_manager_f1;
        public ImageView imageView_channel_manager_f2;
        public ImageView imageView_channel_manager_f3;
        public ImageView imageView_channel_manager_f4;
        public TextView textView_channel_manager_f1;
        public TextView textView_channel_manager_f2;
        public TextView textView_channel_manager_f3;
        public TextView textView_channel_manager_f4;
    }

    @SuppressLint("WorldWriteableFiles")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogTool.i(LogTool.MCHANNEL, "===== onCreate =====");
        mCurSourceId = halApi.getCurSourceID();
        initDTV();
        initView();
        showContent(false);
        mSharedPreferences = getSharedPreferences(CURRENT_SORT_TYPE, Context.MODE_PRIVATE);

        mContext = this;
        int isProgramLockOn = mDtvConfig.getInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
        if (isProgramLockOn > 0) {

            if (mCheckPWBuilder == null) {
                mCheckPWBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
                mCheckPWBuilder.setCheckPassWordsListener(new checkPassWordListener());

            }

            Dialog mCheckPassWordDialog = mCheckPWBuilder.create();

            mCheckPWBuilder.setPasswordTitle(getString(R.string.play_password_menu_lock));
            mCheckPassWordDialog.setCanceledOnTouchOutside(false);

            mCheckPassWordDialog.show();
            mCheckPWBuilder.setCheckPassWordsListener(new CheckPassWordDialogInterface() {
                @Override
                public void onCheck(int which, String passWord) {
                    switch (which) {
                        case CheckPassWordDialogInterface.PASSWORD_RIGHT:
                            showContent(true);
                            break;
                    }
                }
            });
            mCheckPassWordDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                    if (arg1 == KeyValue.DTV_KEYVALUE_BACK) {
                        finish();
                    }
                    return false;
                }
            });
        } else {
            showContent(true);
        }

    }

    /**
     * Initialization the data of DTV.
     */
    private void initDTV() {
        if (null == mNetworkManager) {
            mNetworkType = null;
        } else {
            mNetworkType = mNetworkManager.getCurrentNetworkType();
        }
        List<Integer> sourceList = HitvManager.getInstance().getSourceManager().getSourceList();
        isNeedMixSort = sourceList.contains(halApi.EnumSourceIndex.SOURCE_ATSC) || sourceList.contains(halApi.EnumSourceIndex.SOURCE_ISDBT);
        LogTool.d(LogTool.MPLAY, "isNeedMixSort :" + isNeedMixSort + "currentEnNetworkType = " + mNetworkType);
    }

    /**
     * Initialization the data of the views.
     */
    @SuppressLint("InflateParams")
    private void initView() {
        setContentView(R.layout.channel_manager);
        mGroupListView = findViewById(R.id.listView_channel_manager_group_list);
        mChannelListView = findViewById(R.id.listView_channel_manager_channel_list);
        mTextViewChannelManagerTitle = findViewById(R.id.textView_channelManager_title);
        mTextViewChannelManagerPosition = findViewById(R.id.textView_channelManager_position);
        mMultiOprateDialogView = LayoutInflater.from(this).inflate(R.layout.channel_manager_multi_operate, null);
        mSureToDelDialogView = LayoutInflater.from(this).inflate(R.layout.channel_manager_sure_to_delete_dialog, null);
        mSureToDelDialogAllView = LayoutInflater.from(this).inflate(R.layout.channel_manager_sure_to_delete_all_dialog, null);
        if (null == mControlBarView) {
            mControlBarView = new ControlBarView();
        }
        mControlBarView.imageView_channel_manager_f1 = findViewById(R.id.imageView_channel_manager_f1);
        mControlBarView.imageView_channel_manager_f2 = findViewById(R.id.imageView_channel_manager_f2);
        mControlBarView.imageView_channel_manager_f3 = findViewById(R.id.imageView_channel_manager_f3);
        mControlBarView.imageView_channel_manager_f4 = findViewById(R.id.imageView_channel_manager_f4);
        mControlBarView.textView_channel_manager_f1 = findViewById(R.id.textView_channel_manager_f1);
        mControlBarView.textView_channel_manager_f2 = findViewById(R.id.textView_channel_manager_f2);
        mControlBarView.textView_channel_manager_f3 = findViewById(R.id.textView_channel_manager_f3);
        mControlBarView.textView_channel_manager_f4 = findViewById(R.id.textView_channel_manager_f4);
        mControlBarView.textView_channel_manager_f4.setText(R.string.channel_rename_title);
        if (halApi.isATVSource(mCurSourceId)) {
            mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
            mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
        }
        if (isNeedMixSort) {
            mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
            mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
        }
        mGroupListView.setOnItemClickListener(this);
        mGroupListView.setOnItemSelectedListener(this);
        mChannelListView.setOnItemClickListener(this);
        mChannelListView.setOnItemSelectedListener(this);
    }

    private void initGroupListPos() {
        LogTool.d(LogTool.MPLAY, "init group list pos");
        if (isNeedMixSort) {
            if (mMixChannelList != null && mMixChannelList.size() > 0) {
                setNewAdapter(mMixChannelList);
            }
        } else {
            int pos = this.getChannelListPosition(mChannelList);
            pos = (pos >= 0) ? pos : 0;
            if ((null != mChannelList) && (mChannelList.getChannelCount() > 0)) {
                //mGroupListView.setSelection(pos);
                setListAdapter(mChannelList);
            }
        }
    }

    /**
     * Update the data of the DTV.
     */

    private void updateData() {
        LogTool.d(LogTool.MPLAY, "update Data begin is need mix sort:" + isNeedMixSort);
        if (isNeedMixSort) {
            mMixChannelList = null;
            mMixChannelList = mixSort();
            if (mMixChannelList != null && mMixChannelList.size() > 0 && mChannelListView != null) {
                setNewAdapter(mMixChannelList);
            }
        } else {
            List<ChannelList> mAllGroups = null;
            if (halApi.isATVSource(mCurSourceId)) {
                mAllGroups = mAtvChannelManager.getUseGroups();
            } else {
                mAllGroups = mChannelManager.getUseGroups();
            }

            if ((null == mAllGroups) || (mAllGroups.isEmpty())) {
                LogTool.d(LogTool.MPLAY, "groups is null");
                return;
            }
            mChannelList = mAllGroups.get(0);
            mGroupsListAdapter = new GroupsListAdapter(this, mAllGroups);
            mGroupListView.setAdapter(mGroupsListAdapter);
            goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
            if (null != mChannelList) {
                //mGroupListView.setSelection(pos);
                setListAdapter(mChannelList);
            }
        }
        TaskUtil.post(new Runnable() {
            public void run() {
                mChannelManager.save();
            }
        });
    }


    public List<Channel> mixSort() {
        ChannelList atvChannelList = null;
        ChannelList dtvChannelList = null;
        List<Channel> mMixChannelList;
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        ChannelManager mChannelManager = mDTV.getChannelManager();
        AtvChannelManager mAtvChannelManager = mDTV.getAtvChannelManager();
        LogTool.d(LogTool.MPLAY, "mixSort begin ");
        if (mAtvChannelManager.getUseGroups() != null && mAtvChannelManager.getUseGroups().size() > 0) {
            atvChannelList = mAtvChannelManager.getUseGroups().get(0);
        }
        if (mChannelManager.getUseGroups() != null && mChannelManager.getUseGroups().size() > 0) {
            dtvChannelList = mChannelManager.getUseGroups().get(0);
        }
        mMixChannelList = new ArrayList<>();
        mMixChannelList.clear();
        if (atvChannelList != null) {
            int atvChannelCount = atvChannelList.getChannelCount();
            for (int i = 0; i < atvChannelCount; i++) {
                mMixChannelList.add(atvChannelList.getChannelByIndex(i));
            }
        }
        if (dtvChannelList != null) {
            int dtvChannelCount = dtvChannelList.getChannelCount();
            for (int i = 0; i < dtvChannelCount; i++) {
                mMixChannelList.add(dtvChannelList.getChannelByIndex(i));
            }
        }
        //Channel collection without sorting
        List<Channel> sortList = new ArrayList<>();
        //List of changes
        List<Channel> oldList = new ArrayList<>(mMixChannelList);
        //Sequence number to be deleted
        List<Integer> indexList = new ArrayList<>();
        if (mMixChannelList != null) {
            //Determine if each channel has been sorted
            for (int i = 0; i < oldList.size(); i++) {
                mChannelManager.getChannelByID(oldList.get(i).getChannelID());
                if (oldList.get(i).getSortTag() == 0) {
                    sortList.add(oldList.get(i));
                    indexList.add(i);
                }
            }
            for (int i = 0; i < indexList.size(); i++) {
                oldList.remove(indexList.get(i));
            }
            //All new channels, sort and set the tags directly according to the frequency.
            if (mMixChannelList.size() == sortList.size()) {
                LogTool.d(LogTool.MPLAY, "all channel is new " + sortList.size());
                Collections.sort(mMixChannelList, new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        if (getFreq(o1) > getFreq(o2)) {
                            return 1;
                        } else if (getFreq(o1) < getFreq(o2)) {
                            return -1;
                        } else if (getFreq(o1) == getFreq(o2)) {
                            int minorSN1 = o1.getLCN() & 0xffff;
                            int minorSN2 = o2.getLCN() & 0xffff;
                            if (minorSN1 > minorSN2) {
                                return 1;
                            } else {
                                return -1;
                            }
                        } else {
                            return 0;
                        }
                    }
                });
                //Set the tag (position) to the channel after sorting
                for (int i = 0; i < mMixChannelList.size(); i++) {
                    mMixChannelList.get(i).setSortTag(i + 1);
                }
            } else if (sortList.size() == 0) {
                LogTool.d(LogTool.MPLAY, "don't have new Channel :" + mMixChannelList.size());
                //No new channels, sort by tag
                Collections.sort(mMixChannelList, new Comparator<Channel>() {
                    @Override
                    public int compare(Channel o1, Channel o2) {
                        if (o1.getSortTag() > o2.getSortTag()) {
                            return 1;
                        }
                        if (o1.getSortTag() < o2.getSortTag()) {
                            return -1;
                        }
                        return 0;
                    }
                });
            } else if (sortList.size() > 0 && mMixChannelList.size() > sortList.size()) {
                //Have new channels, reorder
                for (int i = 0; i < sortList.size(); i++) {
                    for (int j = 0; j < oldList.size(); j++) {
                        //Insert by frequency
                        //Freq equal description is dtv channel, need to compare minorSN
                        if (getFreq(sortList.get(i)) == getFreq(oldList.get(j))) {
                            if (getMinorSN(sortList.get(i)) < getMinorSN(oldList.get(0))) {
                                oldList.add(0, sortList.get(i));
                            } else if (getMinorSN(sortList.get(i)) > getMinorSN(oldList.get(oldList.size() - 1))) {
                                oldList.add(sortList.get(i));
                            } else if (j < oldList.size() - 1) {
                                //If the frequency is greater than the previous one is less than the last digit
                                if (getFreq(sortList.get(i)) > getFreq(oldList.get(j)) && getFreq(sortList.get(i)) < getFreq(oldList.get(j + 1))) {
                                    oldList.add(j + 1, sortList.get(i));
                                }
                            }
                        } else {
                            if (getFreq(sortList.get(i)) < getFreq(oldList.get(0))) {
                                oldList.add(0, sortList.get(i));
                            } else if (getFreq(sortList.get(i)) > getFreq(oldList.get(oldList.size() - 1))) {
                                oldList.add(sortList.get(i));
                            } else if (j < oldList.size() - 1) {
                                if (getFreq(sortList.get(i)) > getFreq(oldList.get(j)) && getFreq(sortList.get(i)) < getFreq(oldList.get(j + 1))) {
                                    oldList.add(j + 1, sortList.get(i));
                                }
                            }
                        }

                    }
                }
                mMixChannelList.clear();
                mMixChannelList.addAll(oldList);
                for (int i = 0; i < mMixChannelList.size(); i++) {
                    mMixChannelList.get(i).setSortTag(i + 1);
                }
            }
        }
        return mMixChannelList;
    }

    private int getFreq(Channel channel) {
        if (null != channel.getBelongNetwork() && null != channel.getBelongNetwork().getNetworkType()
                && channel.getBelongNetwork().getNetworkType() == EnNetworkType.RF) {
            return channel.getChannelID();
        } else {
            return (channel.getLCN() >> 16) & 0xffff;
        }
    }

    private int getMinorSN(Channel channel) {
        return channel.getLCN() & 0xffff;
    }

    private void setNewAdapter(List<Channel> mChannelList) {
        LogTool.d(LogTool.MPLAY, "set mix list data");
        ListAdapter tempAdapter = mChannelListView.getAdapter();
        if (null == tempAdapter) {
            mMixChannelListAdapter = new ChannelManagerAdapter(this, mChannelList);
            mChannelListView.setAdapter(mMixChannelListAdapter);
        } else {
            ((ChannelManagerAdapter) mChannelListView.getAdapter()).setChannelList(mChannelList);
        }

        if (mFirstSelected) {
            int index = 0;
            if (mChnHistory.getLastChn(mCurSourceId) != null) {
                currentChannelID = mChnHistory.getCurrentChn(mCurSourceId).getChannelID();
                if (null != mChannelList) {
                    for (int i = 0; i < mChannelList.size(); i++) {
                        if (mChannelList.get(i).getChannelID() == currentChannelID) {
                            index = i;
                        }
                    }
                }
                mChannelListView.setSelection(index);
                mChannelListView.setSelectionFromTop(index, 100);
                mFirstSelected = false;
            } else {
                mChannelListView.setSelection(0);
                mChannelListView.setSelectionFromTop(0, 100);
            }
        }
        if (!mChannelListView.hasFocus()) {
            mChannelListView.requestFocus();
        }
    }

    private int getChannelListPosition(ChannelList tmpChannelList) {
        List<ChannelList> allGroups = mChannelManager.getUseGroups();
        String tmpListName, channelName;

        if (null != allGroups && !allGroups.isEmpty()) {
            for (int i = 0; i < allGroups.size(); i++) {
                tmpListName = allGroups.get(i).getListName();
                if (null != tmpChannelList) {
                    channelName = tmpChannelList.getListName();
                    if (tmpListName.equals(channelName)) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Check password listener.
     *
     * @author m00206002
     */
    private class checkPassWordListener implements CheckPassWordDialogInterface {
        @Override
        public void onCheck(int which, String passWord) {
            if (which == CheckPassWordDialogInterface.PASSWORD_RIGHT) {
                updateData();
                initGroupListPos();
            }
        }
    }

    private void ExitDialog() {
        String tip = mContext.getResources().getString(R.string.str_save_query);
        String tip_title = mContext.getResources().getString(R.string.str_save);
        final ConfirmDialog dialog = new ConfirmDialog(mContext, R.style.DIM_STYLE, tip_title, tip, 1.0f);
        dialog.setConfirmDialogListener(new OnConfirmDialogListener() {
            @Override
            public void onCheck(int which)
            {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK)
                {
                    TaskUtil.post(new Runnable() {
                        public void run() {
                            mChannelManager.save();
                            mIsNeedSaveData = false;
                        }
                    });
                } else {
                    mChannelManager.recover();
                    mIsNeedSaveData = false;
                    if (null != mplayMode) {
                        mChannelManager.setChannelServiceTypeMode(mplayMode);
                    }
                    mChannelManager.rebuildAllGroup();
                }
                finish();
                dialog.cancel();
            }
        });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        LogTool.i(LogTool.MCHANNEL, "===== onDestroy =====");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        LogTool.i(LogTool.MCHANNEL, "===== onRestart =====");
        super.onRestart();
    }

    @Override
    protected void onStart() {
        LogTool.i(LogTool.MCHANNEL, "===== onStart =====");
        super.onStart();
    }

    @Override
    protected void onStop() {
        LogTool.i(LogTool.MCHANNEL, "===== onStop =====");
        super.onStop();
    }

    @Override
    protected void onPause() {
        LogTool.i(LogTool.MCHANNEL, "===== onPause =====");
        if (mIsNeedSaveData) {
            TaskUtil.post(new Runnable() {
                public void run() {
                    mChannelManager.save();
                    mIsNeedSaveData = false;
                }
            });
        }
        if (halApi.isDTVSource(mCurSourceId)) {
            mDTV.prepareDTV();
            mPlayer.resumeResource();
            //DTV 重新播放后，需要重新配置无信号蓝屏
            halApi.setBlueScreen();
            //DTV删除所有TV台后.列表刷新为Radio的台, 但是频道没有播放。
            Channel mCurChannel = mChnHistory.getCurrentChn(halApi.getSelectSourceID());
            if (mCurChannel != null) {
                if (mCurChannel.getServiceType() == EnServiceType.TV) {
                    mPlayer.changeChannel(mChnHistory.getCurrentChn(halApi.getSelectSourceID()));
                } else if (mCurChannel.getServiceType() == EnServiceType.RADIO) {
                    mPlayer.changeChannel(mChnHistory.getPreTvRadioChn(mChannelManager.getChannelServiceTypeMode()));
                }
            }
        }
        finish();
        super.onPause();
    }

    @Override
    protected void onResume() {
        LogTool.i(LogTool.MCHANNEL, "===== onResume =====");
        super.onResume();
        dismissPlayerTipView();
        ListAdapter adapter = mChannelListView.getAdapter();
        if (null != adapter) {
            if (isNeedMixSort) {
                ((ChannelManagerAdapter) adapter).notifyDataSetChanged();
            } else {
                ((ChannelManagerChannelListAdapter) adapter).notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        switch (keyCode)
        {
            case KeyValue.DTV_KEYVALUE_YELLOW:
                if (!checkChannelList()) {
                    LogTool.e(LogTool.MPLAY, "Channel list is null");
                    return true;
                }
                if (isNeedMixSort) {
                    return true;
                }
                if (mEnOperateMode == EnOperateMode.OPERATE_MODE_NONE) {
                    if (halApi.isDTVSource(mCurSourceId)) {
                        showSortDialog();
                    }
                }
                return true;
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT: {
           /* if (mChannelListView.hasFocus())
            {
                mGroupListView.requestFocus();
                View tempView = mGroupListView.getSelectedView();
                if (null != tempView)
                {
                    tempView.requestFocus();
                }
            }*/
        }
            break;
            case KeyValue.DTV_KEYVALUE_RED: {
                if (!checkChannelList()) {
                    LogTool.e(LogTool.MPLAY, "Channel list is null");
                    return true;
                }
                LogTool.e(LogTool.MPLAY, "red key" + mEnOperateMode);
                if (mEnOperateMode == EnOperateMode.OPERATE_MODE_NONE) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_MOVE);
                } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_MOVE) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
                } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_SKIP) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
                    updateData();
                } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_LOCK) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
                    updateData();
                } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_DELETE) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
                    updateData();
                } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_FAVORITE) {
                    goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
                    updateData();
                }
            }
            break;
        case KeyValue.DTV_KEYVALUE_GREEN:
            if (!checkChannelList()) {
                LogTool.e(LogTool.MPLAY, "Channel list is null");
                return true;
            }
            LogTool.i(LogTool.MCHANNEL, "green key " + mEnOperateMode);
            if (mEnOperateMode == EnOperateMode.OPERATE_MODE_NONE) {
                showMultiOperateDialog();
            } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_DELETE) {
                addChannelsToTag(EnTagType.DEL);
                updateData();
            } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_FAVORITE) {
                addFavChannelsToTag(EnFavTag.FAV_ALL);
                updateData();
            } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_MOVE) {
                moveChannel();
                goToOperateMode(EnOperateMode.OPERATE_MODE_NONE);
            } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_SKIP) {
                addChannelsToTag(EnTagType.HIDE);
                updateData();
            } else if (mEnOperateMode == EnOperateMode.OPERATE_MODE_LOCK) {
                addChannelsToTag(EnTagType.LOCK);
                updateData();
            }
            break;
        case KeyValue.DTV_KEYVALUE_BLUE:
            if (!checkChannelList()) {
                LogTool.e(LogTool.MPLAY, "Channel list is null");
                return true;
            }
            if (mEnOperateMode == EnOperateMode.OPERATE_MODE_NONE) {
                showChannelRenameDialog();
            }
            return true;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private boolean checkChannelList() {
        if (isNeedMixSort && null != mMixChannelList && mMixChannelList.size() > 0) {
            return true;
        } else if (!isNeedMixSort && null != mChannelList && mChannelList.getChannelCount() > 0) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * Move the channel in group.
     */
    private void moveChannel() {
        if (mPositionOfChannelToMove < 0 || mCurrentPositionOfChannel < 0) {
            return;
        }
        if (isNeedMixSort) {
            Channel moveChannel = mMixChannelList.get(mCurrentPositionOfChannel);
            mMixChannelList.set(mCurrentPositionOfChannel, mMixChannelList.get(mPositionOfChannelToMove));
            mMixChannelList.set(mPositionOfChannelToMove, moveChannel);
            //Serial number starts from 1
            mMixChannelList.get(mPositionOfChannelToMove).setSortTag(mCurrentPositionOfChannel + 1);
            moveChannel.setSortTag(mPositionOfChannelToMove + 1);
            for (int i = 0; i < mMixChannelList.size(); i++) {
                mMixChannelList.get(i).setSortTag(i + 1);
            }
        } else {
            if (null != mChannelList) {
                if (halApi.isATVSource(mCurSourceId)) {
                    mChannelList.swap(mPositionOfChannelToMove, mCurrentPositionOfChannel);
                } else {
                    mChannelList.move(mPositionOfChannelToMove, mCurrentPositionOfChannel);
                }
                mChannelListView.setSelection(mCurrentPositionOfChannel);
                mIsNeedSaveData = true;
            }
        }
    }

    /**
     * Dismiss player window tip view while enter ChannelManager window.<br>
     */
    private void dismissPlayerTipView() {
        LogTool.d(LogTool.MCHANNEL, "dismissPlayerTipView()");
        Intent dissmissIntent = new Intent(CommonValue.DTV_INTENT_DISMISS_TIP);
        //this.sendBroadcast(dissmissIntent);
        CommonDef.sendBroadcastEx(ChannelManagerActivity.this, dissmissIntent);
    }

    /**
     * Show sort dialog.
     */
    private void showSortDialog() {
        String[] sortArrayToShow = this.getResources().getStringArray(R.array.channelmanager_sort_array);
        if (null == mSortDialog) {
            DialogInterface.OnClickListener mlistener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    int pos = mGroupListView.getSelectedItemPosition();
                    List<ChannelList> allGroups = mChannelManager.getUseGroups();
                    ChannelList list = allGroups.get(pos);
                    int curGroupType = list.getGroupType();
                    mChannelManager.sortProgramView(curGroupType, EnSortType.values()[whichButton], true);
                    Editor editor = mSharedPreferences.edit();
                    editor.putString(CURRENT_SORT_TYPE, msortArray[whichButton]);
                    editor.apply();
                    mIsNeedSaveData = true;
                    mSortDialog.dismiss();
                    updateData();
                }
            };

            mSortDialog = new AlertDialog.Builder(this, R.style.dim_holo_style).setTitle(R.string.choice_sort_type_dialog_title)
                    .setSingleChoiceItems(sortArrayToShow, 0, mlistener).create();

            ListView tempList = mSortDialog.getListView();
            tempList.setBackgroundResource(R.drawable.dialog_background);
            tempList.setSelector(R.drawable.channelmanager_fav_add_dialog_item_bg);

        }

        if (!mSortDialog.isShowing()) {
            mSortDialog.show();
            ListView tempList = mSortDialog.getListView();
            int position = getCurrentSortType();
            tempList.setSelection(position);
            tempList.setItemChecked(position, true);
        }
    }

    /**
     * Get current sort type , recorded in SharedPreferences.
     *
     * @return current sort type.
     */
    private int getCurrentSortType() {
        //int defaultSortType = mSortTypes.get(0);
        int defaultSortType = 0;
        String curSortTyep = mSharedPreferences.getString(CURRENT_SORT_TYPE, msortArray[0]);
        for (int index = 0; index < msortArray.length; index++) {
            if (msortArray[index].equals(curSortTyep)) {
                defaultSortType = index;
            }
        }

        return defaultSortType;
    }

    /**
     * Show save dialog.
     * @param b true to show, false to dismiss.
     */
    /*
    private void showSaveDialog(boolean b)
    {
        LogTool.v(LogTool.MCHANNEL, "showSaveDialog  " + b);
        if (b)
        {
            if (null == mSaveProgressDialog)
            {
                mSaveProgressDialog = new ProgressDialog(this, R.style.DIM_STYLE);
                mSaveProgressDialog.setTitle(R.string.save_dialog_title);
                String msg = getResources().getString(R.string.save_dialog_msg);
                mSaveProgressDialog.setMessage(msg);
            }
            if (!mSaveProgressDialog.isShowing() && !this.isFinishing())
            {
                mSaveProgressDialog.show();
            }
        }
        else
        {
            if (null != mSaveProgressDialog)
            {
                mSaveProgressDialog.dismiss();
            }
        }

    }
    */

    /**
     * Add tags for channels selected.
     *
     * @param tag EnTagType
     */
    private void addChannelsToTag(EnTagType tag) {
        SparseBooleanArray tempSparseBooleanArray = mChannelListView.getCheckedItemPositions();
        if (null == tempSparseBooleanArray) {
            return;
        }
        mChannelToDelList = new ArrayList<>();
        for (int i = 0; i < tempSparseBooleanArray.size(); i++) {
            if (tempSparseBooleanArray.valueAt(i)) {
                Channel tempChannel = (Channel) mChannelListView.getItemAtPosition(tempSparseBooleanArray.keyAt(i));
                if (null != tempChannel) {
                    if (EnTagType.DEL == tag) {
                        mChannelToDelList.add(tempChannel);
                    } else {
                        if (tempChannel.getTag(tag)) {
                            tempChannel.setTag(tag, false);
                        } else {
                            tempChannel.setTag(tag, true);
                        }
                        mIsNeedSaveData = true;
                    }
                } else {
                    LogTool.v(LogTool.MCHANNEL, "tempChannel is null " + i);
                }
            }
        }

        if (EnTagType.DEL == tag) {
            if (mChannelToDelList.size() > 0) {
                showSureToDelDialog();
            }
        }
    }

    /**
     * Add tags for channels selected.
     * @param tag EnFavTag
     */
    private void addFavChannelsToTag(EnFavTag tag) {
        LogTool.d(LogTool.MPLAY, "add atv fav channel");
        SparseBooleanArray tempSparseBooleanArray = mChannelListView.getCheckedItemPositions();
        if (null == tempSparseBooleanArray) {
            return;
        }
        for (int i = 0; i < tempSparseBooleanArray.size(); i++) {
            if (tempSparseBooleanArray.valueAt(i)) {
                Channel tempChannel = (Channel) mChannelListView.getItemAtPosition(tempSparseBooleanArray.keyAt(i));
                if (null != tempChannel) {
                    if (EnFavTag.FAV_ALL == tag) {
                        ArrayList<EnFavTag> favTagList = new ArrayList<EnFavTag>();
                        favTagList.add(EnFavTag.FAV_ALL);
                        if (tempChannel.getFavTag() != null && tempChannel.getFavTag().contains(EnFavTag.FAV_ALL)) {
                            tempChannel.setFavTag(favTagList, false);
                        } else {
                            tempChannel.setFavTag(favTagList, true);
                        }
                    }
                }
            }
        }
    }

    /**
     * Show multi-operation dialog.
     */
    private void showMultiOperateDialog() {
        if (null == mMultiOprateDialogView) {
            return;
        }
        if (null == mMultiOprateDialog) {
            mMultiOprateDialog = new AlertDialog.Builder(this, R.style.DIM_STYLE).create();
            mMultiOprateDialog.show();
            mMultiOprateDialog.addContentView(mMultiOprateDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            WindowManager.LayoutParams lp = mMultiOprateDialog.getWindow().getAttributes();
            lp.width = (int) mContext.getResources().getDimension(R.dimen.dimen_200px);;
            mMultiOprateDialog.getWindow().setAttributes(lp);
            View.OnClickListener l = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.button_channel_manager_add_to_fav: {
                            LogTool.d(LogTool.MPLAY, "item fav");
                            mFavoriteChannel = (Channel) mChannelListView.getSelectedItem();
                            if (null != mFavoriteChannel) {
                                if (isNeedMixSort) {
                                    if (mFavoriteChannel.getNetworkType() == EnNetworkType.RF) {
                                        goToOperateMode(EnOperateMode.OPERATE_MODE_FAVORITE);
                                    } else {
                                        showAddToFavoriteDialog();
                                    }
                                } else {
                                    if (halApi.isATVSource(mCurSourceId)) {
                                        goToOperateMode(EnOperateMode.OPERATE_MODE_FAVORITE);
                                    } else {
                                        showAddToFavoriteDialog();
                                    }
                                }
                            }
                        }
                        break;
                        case R.id.button_channel_manager_skip: {
                            LogTool.d(LogTool.MPLAY, "item skip");
                            goToOperateMode(EnOperateMode.OPERATE_MODE_SKIP);
                        }
                        break;
                        case R.id.button_channel_manager_lock: {
                            LogTool.d(LogTool.MPLAY, "item lock");
                            goToOperateMode(EnOperateMode.OPERATE_MODE_LOCK);
                        }
                        break;
                        case R.id.button_channel_manager_delete: {
                            LogTool.d(LogTool.MPLAY, "item delete");
                            goToOperateMode(EnOperateMode.OPERATE_MODE_DELETE);
                        }
                        break;
                        case R.id.button_channel_manager_delete_all: {
                            LogTool.d(LogTool.MPLAY, "item delete all");
                            showSureToDelAllDialog();
                        }
                        break;
                        default:
                            break;
                    }
                    mMultiOprateDialog.dismiss();
                }
            };

            Button addToFavButton = mMultiOprateDialogView.findViewById(R.id.button_channel_manager_add_to_fav);
            Button skipButton = mMultiOprateDialogView.findViewById(R.id.button_channel_manager_skip);
            Button lockButton = mMultiOprateDialogView.findViewById(R.id.button_channel_manager_lock);
            Button deleteButton = mMultiOprateDialogView.findViewById(R.id.button_channel_manager_delete);
            Button deleteAllButton = mMultiOprateDialogView.findViewById(R.id.button_channel_manager_delete_all);
            addToFavButton.setOnClickListener(l);
            skipButton.setOnClickListener(l);
            lockButton.setOnClickListener(l);
            deleteButton.setOnClickListener(l);
            deleteAllButton.setOnClickListener(l);
            if (mCurSourceId == EnumSourceIndex.SOURCE_ATSC){
                deleteButton.setVisibility(View.GONE);
                deleteAllButton.setVisibility(View.GONE);
            }
        }

        if (!mMultiOprateDialog.isShowing()) {
            mMultiOprateDialog.show();
        }
    }

    /**
     * Show make sure to delete channels dialog.
     */
    private void showSureToDelDialog() {
        if (null == mSureToDelDialogView || null == mChannelToDelList) {
            return;
        }
        if (null == mSureToDelDialog) {
            mSureToDelDialog = new AlertDialog.Builder(this, R.style.DIM_STYLE).create();
            mSureToDelDialog.show();
            mSureToDelDialog.addContentView(mSureToDelDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            View.OnClickListener l = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.button_channelmanager_sure_to_delete_cancel: {

                        }
                        break;
                        case R.id.button_channelmanager_sure_to_delete_ok: {
                            deleteAllChannelInList(mChannelToDelList);
                            mSeletedChannel = null;
                        }
                        break;
                        default:
                            break;
                    }
                    mSureToDelDialog.dismiss();
                }
            };

            Button cancelButton = mSureToDelDialogView.findViewById(R.id.button_channelmanager_sure_to_delete_cancel);
            Button okButton = mSureToDelDialogView.findViewById(R.id.button_channelmanager_sure_to_delete_ok);
            cancelButton.setOnClickListener(l);
            okButton.setOnClickListener(l);
            okButton.requestFocus();
        } else {
            Button okButton = mSureToDelDialogView.findViewById(R.id.button_channelmanager_sure_to_delete_ok);
            okButton.requestFocus();
        }

        TextView tempTextView = mSureToDelDialogView.findViewById(R.id.textView_channelmanager_sure_to_delete_msg);
        String tempDeleteChannelsString = getResources().getString(R.string.sure_to_delete_message);
        String tempDeleteOneChannelString = getResources().getString(R.string.sure_to_delete_one_message);
        int channelNum = mChannelToDelList.size();
        if (channelNum <= 1) {
            tempTextView.setText(String.format("%d %s", channelNum, tempDeleteOneChannelString));
        } else {
            tempTextView.setText(String.format("%d %s", channelNum, tempDeleteChannelsString));
        }

        if (!mSureToDelDialog.isShowing()) {
            mSureToDelDialog.show();
        }
    }

    /**
     * Show make sure to delete all channels dialog.
     */
    private void showSureToDelAllDialog() {
        if (null == mSureToDelDialogAllView) {
            return;
        }
        if (null == mSureToDelAllDialog) {
            mSureToDelAllDialog = new AlertDialog.Builder(this, R.style.DIM_STYLE).create();
            mSureToDelAllDialog.show();
            mSureToDelAllDialog.addContentView(mSureToDelDialogAllView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            View.OnClickListener l = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.button_channelmanager_sure_to_delete_all_cancel: {

                        }
                        break;
                        case R.id.button_channelmanager_sure_to_delete_all_ok: {
                            deleteAllChannels();
                            //ExitDialog();
                        }
                        break;
                        default:
                            break;
                    }
                    mSureToDelAllDialog.dismiss();
                }
            };

            Button cancelButton = mSureToDelDialogAllView.findViewById(R.id.button_channelmanager_sure_to_delete_all_cancel);
            Button okButton = mSureToDelDialogAllView.findViewById(R.id.button_channelmanager_sure_to_delete_all_ok);
            cancelButton.setOnClickListener(l);
            okButton.setOnClickListener(l);
            okButton.requestFocus();
        } else {
            Button okButton = mSureToDelDialogAllView.findViewById(R.id.button_channelmanager_sure_to_delete_all_ok);
            okButton.requestFocus();
        }

        if (!mSureToDelAllDialog.isShowing()) {
            mSureToDelAllDialog.show();
        }
    }

    /**
     * Delete all channels.
     */
    private void deleteAllChannels() {
        if (halApi.isATVSource(mCurSourceId)) {
            mAtvChannelManager.deleteAll();
        } else {
            mChannelManager.deleteChannelsByNetworkType(mNetworkType);
            mPlayer.releaseResource(0);//删除DTV所有台后,当前台还在播放的问题
            mIsNeedSaveData = false;
            TaskUtil.post(new Runnable() {
                public void run() {
                    mChannelManager.save();
                }
            });
        }
        finish();
    }

    /**
     * Delete all channels in list.
     * @param tempChannelList
     */
    private void deleteAllChannelInList(List<Channel> tempChannelList) {

        if (null == tempChannelList) {
            return;
        }

        if (halApi.isATVSource(mCurSourceId)) {
            for (int i = tempChannelList.size() - 1; i >= 0; i--) {
                mAtvChannelManager.deleteChannelByID(tempChannelList.get(i).getChannelID());
                mIsNeedSaveData = true;
            }
            updateData();
        } else {
            for (int i = 0; i < tempChannelList.size(); i++) {
                mChannelManager.deleteChannelByID(tempChannelList.get(i).getChannelID());
                //删除当前台位后仍在播放的问题
                if (currentChannelID == tempChannelList.get(i).getChannelID()) {
                    mPlayer.releaseResource(0);
                }
                mIsNeedSaveData = true;
            }
            updateData();

            ChannelList ChannelList = (ChannelList) mGroupListView.getSelectedItem();

            if ((null == ChannelList) || (0 == ChannelList.getChannelCount())) {
                // Realize TV/Radio mode change here.
                mplayMode = mChannelManager.getChannelServiceTypeMode();
                if (EnTVRadioFilter.TV == mChannelManager.getChannelServiceTypeMode()) {
                    mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.RADIO);
                    mChnHistory.setCurrent(mCurSourceId, null, null);
                } else {
                    mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.TV);
                    mChnHistory.setCurrent(mCurSourceId, null, null);
                }

                List<ChannelList> allGroups = mChannelManager.getUseGroups();
                if ((null == allGroups) || (allGroups.isEmpty())) {
                    if (EnTVRadioFilter.TV == mChannelManager.getChannelServiceTypeMode()) {
                        mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.RADIO);
                        mChnHistory.setCurrent(mCurSourceId, null, null);
                    } else {
                        mChannelManager.setChannelServiceTypeMode(EnTVRadioFilter.TV);
                        mChnHistory.setCurrent(mCurSourceId, null, null);
                    }

                    mIsNeedSaveData = true;
                    updateData();
                    return;
                }

                mGroupsListAdapter = new GroupsListAdapter(this, allGroups);
                mGroupListView.setAdapter(mGroupsListAdapter);
                ChannelList tempList = allGroups.get(0);

                if (isNeedMixSort) {
                    mMixChannelListAdapter.notifyDataSetChanged();
                } else {
                    mChannelListAdapter.notifyDataSetChanged();
                }
                // Have radio channel in DB.
                if ((null == tempList) || (0 == tempList.getChannelCount())) {
                    this.finish();
                } else if (0 < tempList.getChannelCount()) {
                    updateData();
                }

            }
        }
    }

    /**
     * Show add favorite dialog.
     */
    private void showAddToFavoriteDialog() {
        if (null == mAddToFavoriteDialog) {
            mAddToFavoriteDialog = new FavoriteSelectDialog(this, R.style.DIM_STYLE, mFavoriteChannel, 1.0f);
            mAddToFavoriteDialog.show();

            mAddToFavoriteDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    editFavoriteTag();
                }
            });
        }

        if (!mAddToFavoriteDialog.isShowing()) {
            mAddToFavoriteDialog.refreshByChannel(mFavoriteChannel);
            mAddToFavoriteDialog.show();
        }

    }

    /**
     * Edit favorite tag.
     */
    private void editFavoriteTag() {
        List<EnFavTag> favTagsToAdd = new ArrayList<EnFavTag>();
        List<EnFavTag> favTagsToDel = new ArrayList<EnFavTag>();
        SparseBooleanArray boolChoiceArray = mAddToFavoriteDialog.getListCheckedItemPositions();
        for (int i = 0; i < EnFavTag.FAV_16.ordinal(); i++) {
            if (boolChoiceArray.get(i)) {
                LogTool.d(LogTool.MPLAY, "add fav 1?");
                favTagsToAdd.add(EnFavTag.values()[i + 1]);
            } else {
                LogTool.d(LogTool.MPLAY, "add fav 2?");
                favTagsToDel.add(EnFavTag.values()[i + 1]);
            }
        }
        LogTool.d(LogTool.MPLAY, "add dtv fav channel");
        mFavoriteChannel.setFavTag(favTagsToAdd, true);
        mFavoriteChannel.setFavTag(favTagsToDel, false);
        mIsNeedSaveData = true;
        updateData();
    }

    /**
     * Go to operate mode.
     * @param mode EnOperateMode
     */
    private void goToOperateMode(EnOperateMode mode) {
        mEnOperateMode = mode;
        switch (mode) {
            case OPERATE_MODE_NONE: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_NONE");
                mGroupListView.setFocusable(true);
                mGroupListView.setFocusableInTouchMode(true);
                if (mChannelListView.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
                    mChannelListView.clearChoices();
                }
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                ListAdapter tempAdatper = mChannelListView.getAdapter();
                if (null != tempAdatper) {
                    if (isNeedMixSort) {
                        ((ChannelManagerAdapter) tempAdatper).notifyDataSetChanged();
                    } else {
                        ((ChannelManagerChannelListAdapter) tempAdatper).notifyDataSetChanged();
                    }
                }
                mTextViewChannelManagerTitle.setText(R.string.channel_edit_list);
                mTextViewChannelManagerPosition.setText("");
                mControlBarView.textView_channel_manager_f1.setText(R.string.muti_operate);
                mControlBarView.textView_channel_manager_f2.setText(R.string.move);
                mControlBarView.textView_channel_manager_f3.setText(R.string.sort);
                mControlBarView.textView_channel_manager_f4.setText(R.string.channel_rename_title);
                mControlBarView.textView_channel_manager_f1.setVisibility(View.VISIBLE);
                mControlBarView.textView_channel_manager_f2.setVisibility(View.VISIBLE);
                if (!isNeedMixSort) {
                    mControlBarView.textView_channel_manager_f3.setVisibility(View.VISIBLE);
                    mControlBarView.imageView_channel_manager_f3.setVisibility(View.VISIBLE);
                }
                mControlBarView.textView_channel_manager_f4.setVisibility(View.VISIBLE);
                mControlBarView.imageView_channel_manager_f1.setVisibility(View.VISIBLE);
                mControlBarView.imageView_channel_manager_f2.setVisibility(View.VISIBLE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.VISIBLE);
                if (halApi.isATVSource(mCurSourceId)) {
                    mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                    mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                }
                setMoveItemView(mCurrentPositionOfChannel, false);
                setMoveItemView(mPositionOfChannelToMove, false);
            }
            break;
            case OPERATE_MODE_MOVE: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_MOVE");
                if (null == mSeletedChannel) {
                    return;
                }
                mGroupListView.setFocusable(false);
                mGroupListView.setFocusableInTouchMode(false);
                if (mChannelListView.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
                    mChannelListView.clearChoices();
                }
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
                if (isNeedMixSort) {
                    ((ChannelManagerAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                } else {
                    ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                }
                mControlBarView.textView_channel_manager_f1.setText(R.string.move);
                mControlBarView.textView_channel_manager_f2.setText(R.string.cancel_move);
                mControlBarView.textView_channel_manager_f1.setVisibility(View.VISIBLE);
                mControlBarView.textView_channel_manager_f2.setVisibility(View.VISIBLE);
                mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.textView_channel_manager_f4.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f1.setVisibility(View.VISIBLE);
                mControlBarView.imageView_channel_manager_f2.setVisibility(View.VISIBLE);
                mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.GONE);
                String channelName = mSeletedChannel.getChannelName();
                String tempStr = getResources().getString(R.string.move_to);
                mTextViewChannelManagerTitle.setText(R.string.move);
                //mTextViewChannelManagerTitle.setText(String.format("%s %s ", channelName, tempStr));
                //mTextViewChannelManagerPosition.setText(String.valueOf(mCurrentPositionOfChannel));
                mPositionOfChannelToMove = mCurrentPositionOfChannel;
                setMoveItemView(mPositionOfChannelToMove, true);
            }
            break;
            case OPERATE_MODE_SKIP: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_SKIP");
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                if (isNeedMixSort) {
                    ((ChannelManagerAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                } else {
                    ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                }
                mTextViewChannelManagerTitle.setText(R.string.channel_edit_list);
                mTextViewChannelManagerPosition.setText("");
                mControlBarView.textView_channel_manager_f1.setText(R.string.skip_unskip);
                mControlBarView.textView_channel_manager_f2.setText(R.string.cancel_skip);
                mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.textView_channel_manager_f4.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.GONE);
            }
            break;
            case OPERATE_MODE_LOCK: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_LOCK");
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                if (isNeedMixSort) {
                    ((ChannelManagerAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                } else {
                    ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                }
                mTextViewChannelManagerTitle.setText(R.string.channel_edit_list);
                mTextViewChannelManagerPosition.setText("");
                mControlBarView.textView_channel_manager_f1.setText(R.string.lock_unlock);
                mControlBarView.textView_channel_manager_f2.setText(R.string.cancel_lock);
                mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.textView_channel_manager_f4.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.GONE);

            }
            break;
            case OPERATE_MODE_DELETE: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_DELETE");
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                if (isNeedMixSort) {
                    ((ChannelManagerAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                } else {
                    ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                }
                mTextViewChannelManagerTitle.setText(R.string.channel_edit_list);
                mTextViewChannelManagerPosition.setText("");
                mControlBarView.textView_channel_manager_f1.setText(R.string.delete);
                mControlBarView.textView_channel_manager_f2.setText(R.string.cancel_delete);
                mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.textView_channel_manager_f4.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.GONE);

            }
            break;
            case OPERATE_MODE_FAVORITE: {
                LogTool.d(LogTool.MPLAY, "OPERATE_MODE_FAVORITE");
                mChannelListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                if (isNeedMixSort) {
                    ((ChannelManagerAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                } else {
                    ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).notifyDataSetChanged();
                }
                mTextViewChannelManagerTitle.setText(R.string.channel_edit_list);
                mTextViewChannelManagerPosition.setText("");
                mControlBarView.textView_channel_manager_f1.setText(R.string.fav_unfav);
                mControlBarView.textView_channel_manager_f2.setText(R.string.cancel_delete);
                mControlBarView.textView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.textView_channel_manager_f4.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f3.setVisibility(View.GONE);
                mControlBarView.imageView_channel_manager_f4.setVisibility(View.GONE);

        }
            break;
        default:
            break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        switch (arg0.getId()) {
            case R.id.listView_channel_manager_group_list: {
                ChannelList tempChannelList = (ChannelList) arg0.getAdapter().getItem(arg2);
                if (null != tempChannelList) {
                    ListAdapter tempAdapter = mChannelListView.getAdapter();
                    if (null == tempAdapter) {
                        if (isNeedMixSort) {
                            mMixChannelListAdapter = new ChannelManagerAdapter(this, mMixChannelList);
                            mChannelListView.setAdapter(mMixChannelListAdapter);
                        } else {
                            mChannelListAdapter = new ChannelManagerChannelListAdapter(this, tempChannelList);
                            mChannelListView.setAdapter(mChannelListAdapter);
                        }

                    } else {
                        if (isNeedMixSort) {
                            ((ChannelManagerAdapter) mChannelListView.getAdapter()).setChannelList(mMixChannelList);
                        } else {
                            ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).setChannelList(tempChannelList);
                        }

                    }

                    if (mFirstSelected) {
                        int index = 0;
                        currentChannelID = mChnHistory.getLastChn(mCurSourceId).getChannelID();
                        if (isNeedMixSort) {
                            if (null != mMixChannelList) {
                                for (int i = 0; i < mMixChannelList.size(); i++) {
                                    if (mMixChannelList.get(i).getChannelID() == currentChannelID) {
                                        index = i;
                                    }
                                }
                            }
                        } else {
                            if (null != mChannelList) {
                                index = mChannelList.getPosByChannelID(currentChannelID);
                            }
                            index = (index >= 0) ? index : 0;
                        }
                        mChannelListView.setSelection(index);
                    mChannelListView.setSelectionFromTop(index, (int) getResources().getDimension(R.dimen.dimen_100px));
                    mFirstSelected = false;
                }
            }
        }
            break;
            case R.id.listView_channel_manager_channel_list: {
                Channel tempChannel = (Channel) arg0.getAdapter().getItem(arg2);
                if (mEnOperateMode == EnOperateMode.OPERATE_MODE_MOVE && mPositionOfChannelToMove != mCurrentPositionOfChannel) {
                    setMoveItemView(mCurrentPositionOfChannel, false);
                }
                if (null != tempChannel) {
                    mSeletedChannel = tempChannel;
                    mCurrentPositionOfChannel = arg2;
                }
                if (mEnOperateMode == EnOperateMode.OPERATE_MODE_MOVE) {
                    setMoveItemView(mCurrentPositionOfChannel, true);
                    mTextViewChannelManagerPosition.setText("");
                }
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        // Noting
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        switch (arg0.getId()) {
            case R.id.listView_channel_manager_group_list: {
            }
            break;
            case R.id.listView_channel_manager_channel_list: {
                SparseBooleanArray tempSparseBooleanArray = mChannelListView.getCheckedItemPositions();
                if (null != tempSparseBooleanArray) {
                    if (isNeedMixSort) {
                        ((ChannelManagerAdapter) mChannelListView.getAdapter()).setSparseBooleanArray(tempSparseBooleanArray);
                    } else {
                        ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).setSparseBooleanArray(tempSparseBooleanArray);
                    }
                }
                Channel tempChannel = (Channel) arg0.getAdapter().getItem(arg2);
                if (null != tempChannel) {
                    mSeletedChannel = tempChannel;
                    mCurrentPositionOfChannel = arg2;
                }
            }
            break;
        default:
            break;
        }
    }

    private void setListAdapter(ChannelList tempChannelList) {
        ListAdapter tempAdapter = mChannelListView.getAdapter();
        if (null == tempAdapter) {
            mChannelListAdapter = new ChannelManagerChannelListAdapter(this, tempChannelList);
            mChannelListView.setAdapter(mChannelListAdapter);
        } else {
            ((ChannelManagerChannelListAdapter) mChannelListView.getAdapter()).setChannelList(tempChannelList);
        }

        if (mFirstSelected) {
            int index = 0;
            if (mChnHistory.getLastChn(mCurSourceId) != null) {
                currentChannelID = mChnHistory.getCurrentChn(mCurSourceId).getChannelID();
                if (null != mChannelList) {
                    index = mChannelList.getPosByChannelID(currentChannelID);
                }
                index = (index >= 0) ? index : 0;
                mChannelListView.setSelection(index);
                mChannelListView.setSelectionFromTop(index, (int) getResources().getDimension(R.dimen.dimen_100px));
                mFirstSelected = false;
            }
            else
            {
                mChannelListView.setSelection(0);
                mChannelListView.setSelectionFromTop(0, (int) getResources().getDimension(R.dimen.dimen_100px));
            }
        }
        if (!mChannelListView.hasFocus())
        {
            mChannelListView.requestFocus();
        }
    }

    private void showContent(boolean flag) {
        View view = findViewById(R.id.channel_manager_root);
        if (flag) {
            updateData();
            initGroupListPos();
        }
        view.setVisibility(flag?View.VISIBLE:View.INVISIBLE);
    }


    private void setMoveItemView(int itemIndex,boolean itemSelect){
        if(itemIndex == -1)
            return;
        LogTool.v(LogTool.MCHANNEL, "setMoveItemView index :" + itemIndex + " itemSelect:" + itemSelect);
        View mItemView = getViewByPosition(itemIndex, mChannelListView);
        if (null == mItemView) {
            return;
        }

        TextView mChannelNo = mItemView.findViewById(R.id.textView_channel_manager_channel_list_item_no);
        TextView mChannelName = mItemView.findViewById(R.id.textView_channel_manager_channel_list_item_name);
        if (itemSelect) {
            mChannelNo.setTextColor(getResources().getColor(R.color.red));
            mChannelName.setTextColor(getResources().getColor(R.color.red));
        }else{
            mChannelNo.setTextColor(getResources().getColor(R.color.white));
            mChannelName.setTextColor(getResources().getColor(R.color.white));
        }

    }

    private View getViewByPosition(int pos, ListView listView){
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;
        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    private void showChannelRenameDialog() {
        @SuppressLint("InflateParams") View renameDialogView = LayoutInflater.from(this).inflate(R.layout.channel_rename_dialog, null);
        final EditText channelRenameEditText = (EditText) renameDialogView.findViewById(R.id.editText_channel_rename_name);
        final Channel channelToRename = (Channel) mChannelListView.getSelectedItem();
        channelRenameEditText.setText(String.valueOf(channelToRename.getChannelName()));
        final Dialog mEditDialog = new Dialog(this, R.style.DIM_STYLE);
        mEditDialog.show();
        WindowManager.LayoutParams lp = mEditDialog.getWindow().getAttributes();
        lp.width = (int) mContext.getResources().getDimension(R.dimen.dimen_600px);
        ;
        mEditDialog.getWindow().setAttributes(lp);
        mEditDialog.setContentView(renameDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        Button button_channeledit_dialog = (Button) renameDialogView.findViewById(R.id.button_channelrename_dialog);
        button_channeledit_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (0 == channelRenameEditText.getText().toString().length()) {
                    channelRenameEditText.setText(String.valueOf(channelToRename.getChannelName()));
                    channelRenameEditText.requestFocus();
                    MyToast.makeText(ChannelManagerActivity.this, ChannelManagerActivity.this.getResources().getString(R.string.it_cant_be_empty), MyToast.LENGTH_SHORT).show();
                } else {
                    int ret = channelToRename.setChannelName(channelRenameEditText.getText().toString());
                    if (ret == 0) {
                        List<ChannelList> allGroups = mChannelManager.getUseGroups();
                        int groupPosition = mGroupListView.getSelectedItemPosition();
                        if ((null != allGroups) && !(allGroups.isEmpty()) && groupPosition >= 0) {
                            ChannelList list = allGroups.get(groupPosition);
                            int curGroupType = list.getGroupType();
                            mChannelManager.sortProgramView(curGroupType, EnSortType.values()[getCurrentSortType()], true);
                        }
                        updateData();
                        mIsNeedSaveData = true;
                    }
                    mEditDialog.cancel();
                }
            }
        });
    }
}
