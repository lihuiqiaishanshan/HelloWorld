package com.hisilicon.tvui.channelmanager;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelFilter;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.DVBSTransponder;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.ConfirmDialog;
import com.hisilicon.tvui.view.ConfirmDialog.OnConfirmDialogListener;
import com.hisilicon.tvui.view.MyToast;

public class ChannelEditActivity extends Activity implements OnItemClickListener, OnItemSelectedListener
{
    private static final String DEFAULT_ADDED_CHANNEL_NAME = "";
    private static final int DEFAULT_ADDED_CHANNEL_AUDIO_PID = 512;
    private static final int DEFAULT_ADDED_CHANNEL_VIDEO_PID = 513;
    private static final int DEFAULT_ADDED_CHANNEL_PCR_PID = 515;
    private static final int DEFAULT_ADDED_CHANNEL_INVALID_VIDEO_PID = 8191;
    private ListView mSatListView;
    private ListView mTpListView;
    private ListView mChannelListView;
    private ChannelEditListAdapter mChannelAdapter;
    private ChannelManager mChannelManager;
    private NetworkManager mNetworkManager;
    private Dialog mEditDialog;
    private View mDialogView;
    private EditText mChannelNameEditText;
    private EditText mAudioPidEditText;
    private EditText mVideoPidEditText;
    private EditText mPcrPidEditText;
    private boolean mIsNeedSaveData = false;
    private boolean mIsAddChannel = false;
    private Channel mChannelToEdit;
    private ChannelEditActivity mContext = null;

    @SuppressLint("InflateParams")
    public ChannelEditActivity() {

    }

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LogTool.i(LogTool.MCHANNEL, "===== onCreate =====");
        mDialogView = LayoutInflater.from(this).inflate(R.layout.channel_edit_dialog, null);
        initDTV();
        initView();
        mContext = this;
    }

    /**
     * Initialization the data of DTV.
     */
    private void initDTV()
    {
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = mDTV.getNetworkManager();
        mChannelManager = mDTV.getChannelManager();
    }

    /**
     * Initialization the data of the views.
     */
    private void initView()
    {
        setContentView(R.layout.channel_edit);
        mSatListView = (ListView) findViewById(R.id.listView_channel_edit_satellite_list);
        mTpListView = (ListView) findViewById(R.id.listView_channel_edit_tp_list);
        mChannelListView = (ListView) findViewById(R.id.listView_channel_edit_channel_list);
        mChannelNameEditText = (EditText) mDialogView.findViewById(R.id.editText_channel_edit_dialog_item_name);
        mAudioPidEditText = (EditText) mDialogView.findViewById(R.id.editText_channel_edit_dialog_audio_pid);
        mVideoPidEditText = (EditText) mDialogView.findViewById(R.id.editText_channel_edit_dialog_video_pid);
        mPcrPidEditText = (EditText) mDialogView.findViewById(R.id.editText_channel_edit_dialog_pcr_pid);

        List<Network> list = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);
        SatelliteListAdapter mSatAdapter = new SatelliteListAdapter(this, list);
        mSatListView.setAdapter(mSatAdapter);
        mSatListView.setOnItemClickListener(this);
        mSatListView.setOnItemSelectedListener(this);
        mTpListView.setOnItemClickListener(this);
        mTpListView.setOnItemSelectedListener(this);
        mChannelListView.setOnItemClickListener(this);
        mChannelListView.setOnItemSelectedListener(this);
    }

    private void ExitDialog()
    {
        String tip = mContext.getResources().getString(R.string.str_save_query);
        String tip_title = mContext.getResources().getString(R.string.str_save);
        final ConfirmDialog dialog = new ConfirmDialog(mContext, R.style.DIM_STYLE, tip_title ,tip, 1.0f);
        dialog.setConfirmDialogListener(new OnConfirmDialogListener()
        {
            @Override
            public void onCheck(int which)
            {
                if (which == ConfirmDialog.OnConfirmDialogListener.OK)
                {
                    TaskUtil.post(new Runnable() {
                        public void run() {
                            mChannelManager.save();
                            mChannelManager.rebuildAllGroup();
                            mIsNeedSaveData = false;
                        }
                    });
                }
                else
                {
                    mChannelManager.recover();
                    mIsNeedSaveData = false;
                }
                finish();
                dialog.cancel();
            }
        });
        dialog.show();
    }



    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event)
    {
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
            if (mIsNeedSaveData)
            {
                ExitDialog();
            }

            break;
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            if (mTpListView.hasFocus())
            {
                mSatListView.requestFocus();
                View view = mSatListView.getSelectedView();
                if (null != view)
                {
                    view.requestFocus();
                }
                return true;
            }
            else if (mChannelListView.hasFocus())
            {
                mTpListView.requestFocus();
                View view = mTpListView.getSelectedView();
                if (null != view)
                {
                    view.requestFocus();
                }
                return true;
            }
        }
            break;
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            if (mSatListView.hasFocus())
            {
                mTpListView.requestFocus();
                View view = mTpListView.getSelectedView();
                if (null != view)
                {
                    view.requestFocus();
                }
                return true;
            }
        }
            break;
        case KeyValue.DTV_KEYVALUE_RED:
        {
            mChannelToEdit = (Channel) mChannelListView.getSelectedItem();
            if (null != mChannelToEdit)
            {
                mIsAddChannel = false;
                showChannelEditDialog(R.string.channel_edit_dialog_title);
            }
            else
            {
                LogTool.d(LogTool.MCHANNEL, "channel to edit is null");
            }
        }
            break;
        case KeyValue.DTV_KEYVALUE_GREEN:
        {
            mIsAddChannel = true;
            showChannelEditDialog(R.string.add_channel_dialog_title);
        }
            break;
        default:
            break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        switch (arg0.getId())
        {
        case R.id.listView_channel_edit_satellite_list:
        {
            DVBSNetwork tempSatellite = (DVBSNetwork) arg0.getAdapter().getItem(arg2);
            List<Multiplex> tps = tempSatellite.getMultiplexes();
            if (null != tps)
            {
                TPListAdapter mTPAdapter = new TPListAdapter(this, tps);
                mTpListView.setAdapter(mTPAdapter);
            }

            if (tps != null) {
                if (0 == tps.size())
                {
                    mChannelAdapter = new ChannelEditListAdapter(this, null);
                    mChannelListView.setAdapter(mChannelAdapter);
                }
            }
        }
            break;
        case R.id.listView_channel_edit_tp_list:
        {
            DVBSTransponder tempTp = (DVBSTransponder) arg0.getAdapter().getItem(arg2);
            ChannelFilter tempFilter = new ChannelFilter();
            tempFilter.setSIElement(tempTp);
            EnTVRadioFilter currentFilter = mChannelManager.getChannelServiceTypeMode();
            tempFilter.setGroupType(currentFilter);
            ChannelList tempList = mChannelManager.getChannelList(tempFilter);
            if (null != tempList)
            {
                mChannelAdapter = new ChannelEditListAdapter(this, tempList);
                mChannelListView.setAdapter(mChannelAdapter);
            }
        }
            break;
        default:
            break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0)
    {
        // nothing
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        switch (arg0.getId())
        {
        case R.id.listView_channel_edit_channel_list:
        {
            mChannelToEdit = (Channel) arg0.getAdapter().getItem(arg2);
            if (null != mChannelToEdit)
            {
                mIsAddChannel = false;
                showChannelEditDialog(R.string.channel_edit_dialog_title);
            }
            else
            {
                LogTool.d(LogTool.MCHANNEL, "channel is null");
            }
        }
            break;
        default:
            break;
        }
    }

    /**
     * Show saving dialog.
     */
    /*
    private void showSaveDialog(boolean b)
    {
        if (b)
        {
            if (null == saveDialog)
            {
                saveDialog = new ProgressDialog(this, R.style.DIM_STYLE);
                saveDialog.setTitle(R.string.save_dialog_title);
                String msg = getResources().getString(R.string.save_dialog_msg);
                saveDialog.setMessage(msg);
            }
            if (!saveDialog.isShowing() && !this.isFinishing())
            {
                saveDialog.show();
            }
        }
        else
        {
            if (null != saveDialog)
            {
                saveDialog.dismiss();
            }
        }

    }
    */
    private int getMaxLcnNum()
    {
        int maxLcnNum = 0;
        int tmpLcn = 0;

        List<ChannelList> tmpChnListList = mChannelManager.getUseGroups();
        ChannelList tmpChnList = tmpChnListList.get(0);
        for (int i = 0; i < tmpChnList.getChannelCount(); i++)
        {
            tmpLcn = tmpChnList.getChannelByIndex(i).getLCN();
            maxLcnNum = maxLcnNum > tmpLcn ? maxLcnNum : tmpLcn;
        }

        return maxLcnNum;
    }

    private boolean isChannelNameConflict(String name, boolean isAdd, DVBSTransponder tempTP)
    {
        List<ChannelList> tmpChnListList = mChannelManager.getUseGroups();
        ChannelList tmpChnList = tmpChnListList.get(0);
        Channel tmpChn;
        for (int i = 0; i < tmpChnList.getChannelCount(); i++)
        {
            tmpChn = tmpChnList.getChannelByIndex(i);
            if (tempTP.getID() != tmpChn.getBelongMultiplexe().getID())
            {
                continue;
            }

            if (!isAdd)
            {
                if (mChannelToEdit.getChannelID() == tmpChn.getChannelID())
                {
                    continue;
                }
            }

            if (tmpChn.getChannelName().equals(name))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Show channel edit dialog by title ID.
     * @param titleId the ID of the string for dialog title.
     */
    private void showChannelEditDialog(int titleId)
    {
        if (null == mDialogView)
        {
            return;
        }
        if (mIsAddChannel)
        {
            mChannelNameEditText.setText(DEFAULT_ADDED_CHANNEL_NAME);
            mAudioPidEditText.setText(String.valueOf(DEFAULT_ADDED_CHANNEL_AUDIO_PID));
            mVideoPidEditText.setText(String.valueOf(DEFAULT_ADDED_CHANNEL_VIDEO_PID));
            mPcrPidEditText.setText(String.valueOf(DEFAULT_ADDED_CHANNEL_PCR_PID));
        }
        else
        {
            mChannelNameEditText.setText(String.valueOf(mChannelToEdit.getChannelName()));
            mAudioPidEditText.setText(String.valueOf(mChannelToEdit.getAudioPID()));
            mVideoPidEditText.setText(String.valueOf(mChannelToEdit.getVideoPID()));
            mPcrPidEditText.setText(String.valueOf(mChannelToEdit.getPCRPID()));
        }
        if (EnTVRadioFilter.RADIO == mChannelManager.getChannelServiceTypeMode())
        {
            mVideoPidEditText.setText(String.valueOf(DEFAULT_ADDED_CHANNEL_INVALID_VIDEO_PID));
            mVideoPidEditText.setFocusable(false);
            mVideoPidEditText.setEnabled(false);
            mVideoPidEditText.setBackgroundResource(R.drawable.channeledit_dialog_input_stop);
        }
        if (null == mEditDialog)
        {
            mEditDialog = new Dialog(this, R.style.DIM_STYLE);
            mEditDialog.show();
            mEditDialog.setContentView(mDialogView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            Button button_channeledit_dialog = (Button) mDialogView.findViewById(R.id.button_channeledit_dialog);
            button_channeledit_dialog.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (mChannelNameEditText.getText().toString().length() == 0 || mAudioPidEditText.getText().toString().length() == 0
                            || mVideoPidEditText.getText().toString().length() == 0 || mPcrPidEditText.getText().toString().length() == 0)
                    {
                        LogTool.v(LogTool.MCHANNEL, "EditText is empty");
                        if (mChannelNameEditText.getText().toString().length() == 0)
                        {
                            if (!mIsAddChannel)
                            {
                                mChannelNameEditText.setText(String.valueOf(mChannelToEdit.getChannelName()));
                            }
                            mChannelNameEditText.requestFocus();
                        }
                        else if (mAudioPidEditText.getText().toString().length() == 0)
                        {
                            if (!mIsAddChannel)
                            {
                                mAudioPidEditText.setText(String.valueOf(mChannelToEdit.getAudioPID()));
                            }
                            mAudioPidEditText.requestFocus();
                        }
                        else if (mVideoPidEditText.getText().toString().length() == 0)
                        {
                            if (!mIsAddChannel)
                            {
                                mVideoPidEditText.setText(String.valueOf(mChannelToEdit.getVideoPID()));
                            }
                            mVideoPidEditText.requestFocus();
                        }
                        else if (mPcrPidEditText.getText().toString().length() == 0)
                        {
                            if (!mIsAddChannel)
                            {
                                mPcrPidEditText.setText(String.valueOf(mChannelToEdit.getPCRPID()));
                            }
                            mPcrPidEditText.requestFocus();
                        }
                        showToastCanNotBeEmpty();
                        return;
                    }

                    if (Integer.parseInt(mAudioPidEditText.getText().toString()) > Channel.MAX_PID_NUM
                            || Integer.parseInt(mVideoPidEditText.getText().toString()) > Channel.MAX_PID_NUM
                            || Integer.parseInt(mPcrPidEditText.getText().toString()) > Channel.MAX_PID_NUM)
                    {
                        LogTool.v(LogTool.MCHANNEL, "EditText PID is too big");
                        if (Integer.parseInt(mAudioPidEditText.getText().toString()) > Channel.MAX_PID_NUM)
                        {
                            mAudioPidEditText.requestFocus();
                        }
                        else if (Integer.parseInt(mVideoPidEditText.getText().toString()) > Channel.MAX_PID_NUM)
                        {
                            mVideoPidEditText.requestFocus();
                        }
                        else if (Integer.parseInt(mPcrPidEditText.getText().toString()) > Channel.MAX_PID_NUM)
                        {
                            mPcrPidEditText.requestFocus();
                        }
                        showToastBeOutOfRange();
                        return;
                    }

                    DVBSTransponder tempTP = (DVBSTransponder) mTpListView.getSelectedItem();
                    if (null == tempTP)
                    {
                        LogTool.v(LogTool.MCHANNEL, "tempTP is null");
                        return;
                    }

                    if (isChannelNameConflict(mChannelNameEditText.getText().toString(), mIsAddChannel, tempTP))
                    {
                        LogTool.v(LogTool.MCHANNEL, "Channel name is conflict");
                        mChannelNameEditText.requestFocus();
                        showToastNameConflict();
                        return;
                    }

                    if (mIsAddChannel)
                    {
                        mChannelToEdit = null;
                        mChannelToEdit = mChannelManager.createChannel(tempTP);
                        if (mChannelToEdit != null)
                        {
                            mChannelToEdit.setAudioType(CommonValue.DEFAULT_AUDIO_TYPE);
                            mChannelToEdit.setVideoType(CommonValue.DEFAULT_VIDEO_TYPE);
                            mChannelToEdit.setLCN(getMaxLcnNum() + 1);
                        }
                    }

                    if (null != mChannelToEdit)
                    {
                        LogTool.v(LogTool.MCHANNEL, "mChannelToEdit id = " + mChannelToEdit.getChannelID());

                        mChannelToEdit.setMultiplexe(tempTP);

                        mChannelToEdit.setChannelName(mChannelNameEditText.getText().toString());
                        mChannelToEdit.setAudioPID(Integer.parseInt(mAudioPidEditText.getText().toString()));
                        mChannelToEdit.setVideoPID(Integer.parseInt(mVideoPidEditText.getText().toString()));
                        mChannelToEdit.setPCRPID(Integer.parseInt(mPcrPidEditText.getText().toString()));
                        mIsNeedSaveData = true;

                        ChannelFilter tempFilter = new ChannelFilter();
                        tempFilter.setSIElement(tempTP);
                        EnTVRadioFilter currentFilter = mChannelManager.getChannelServiceTypeMode();
                        tempFilter.setGroupType(currentFilter);
                        ChannelList tempList = mChannelManager.getChannelList(tempFilter);
                        if (null != tempList)
                        {
                            mChannelAdapter = new ChannelEditListAdapter(ChannelEditActivity.this, tempList);
                            mChannelListView.setAdapter(mChannelAdapter);
                        }
                    }
                    else
                    {
                        LogTool.v(LogTool.MCHANNEL, "mChannelToEdit is null ");
                    }
                    mEditDialog.dismiss();
                }
            });
        }
        TextView textView_channeledit_dialog = (TextView) mDialogView.findViewById(R.id.textView_channeledit_dialog);
        textView_channeledit_dialog.setText(titleId);
        if (!mEditDialog.isShowing())
        {
            mEditDialog.setTitle(titleId);
            mEditDialog.show();
        }
    }

    private void showToastCanNotBeEmpty()
    {
        MyToast.makeText(this, this.getResources().getString(R.string.it_cant_be_empty), MyToast.LENGTH_SHORT).show();
    }

    private void showToastBeOutOfRange()
    {
        MyToast.makeText(this, this.getResources().getString(R.string.pid_is_out_of_range) + Channel.MAX_PID_NUM, MyToast.LENGTH_SHORT).show();
    }

    private void showToastNameConflict()
    {
        MyToast.makeText(this, this.getResources().getString(R.string.name_conflict), MyToast.LENGTH_SHORT).show();
    }
}
