package com.hisilicon.tvui.installtion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.channel.EnSortType;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.ChannelHistory;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.tvui.view.NoTouchListView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ScanProgressActivity extends Activity implements OnKeyListener, OnTouchListener {
    private static final int TP_UNIT_RATE = 1000;
    private static final int STOPSCAN_STARTSCAN_MIN_SPACE = 1000;
    private static final DecimalFormat FREQ_DF = new DecimalFormat("#######.##");

    /*
     * Receive the message of HI_SVR_EVT_SRCH_LOCK_START When scanning, this is the second parameter
     * of the unit , S / C for the symbol rate , T is the bandwidth
     */
    private String mParamUnitType = " Kbps";
    private ChannelManager mChannelManager = null;
    private NetworkManager mNetworkManager = null;
    private EnNetworkType mNetworkType;
    private Network mCurrentScanNetwork = null;
    private List<Network> mLstNeedScanNetwork = null;
    private ScanType mScanType = null;
    private DTV mDtv = null;
    private PlayerManager mPlayerManager;

    private TextView mTVCountVw = null;
    private TextView mRadioCountVw = null;

    private final List<String> mTVDataList = new ArrayList<String>();
    private NoTouchListView mTVLstVw = null;
    private StringListAdapter mTVArrayAdapter = null;
    private int mTVCount = 0;

    private final List<String> mRadioDataList = new ArrayList<String>();
    private NoTouchListView mRadioLstVw = null;
    private StringListAdapter mRdoArrayAdapter = null;
    private int mRadioCount = 0;

    private final List<ScanFreqItem> mFreqDataList = new ArrayList<ScanFreqItem>();
    private NoTouchListView mScanFreq = null;
    private ScanFreqListAdapter mFreqArrayAdapter = null;

    // Search frequency count
    private int mFreCount = 0;

    // The current frequency of search to the program number
    private int mCurrentFreqChannelCount = 0;
    private TextView mTvPercent = null;
    private ProgressBar mProgressBar = null;
    private ImageView mIvSignalFlash = null;
    private AnimationDrawable mSignalFlash = null;

    // If you fail to save, to ensure the recovery.
    private boolean mIsResultSaved = false;
    private boolean mIsScanFinish = false;

    // The first channel to search(TV priority)
    private Channel mFirstScanChannel = null;
    private static final String TAG = "ScanProgressActivity";

    private static final int FREQUENCY_LIST_ITEM_NUM = 7;
    private static final int PROGRAM_LIST_ITEM_NUM = 5;
    private static final int MARGINS_TOP = 0; // down from top
    private boolean mIsFixFeqListViewHeight = false;
    private boolean mIsFixProgListViewHeight = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window scanWindow = this.getWindow();
        if (null == scanWindow) {
            LogTool.v(LogTool.MSCAN, "the install window is null");
            return;
        }
        scanWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.install_search_progress);

        mLstNeedScanNetwork = ((DTVApplication) this.getApplication()).getScanParamNetwork();
        EnNetworkType networkType = mLstNeedScanNetwork.get(0).getNetworkType();
        mScanType = ((DTVApplication) this.getApplication()).getScanType(networkType);
        initCtrl();
    }

    private void initCtrl() {
        mDtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = mDtv.getNetworkManager();
        if (null == mNetworkManager) {
            mNetworkManager = null;
        } else {
            mNetworkType = mNetworkManager.getCurrentNetworkType();
        }

        mTVCountVw = (TextView) this.findViewById(R.id.id_install_scan_tvcount);
        mRadioCountVw = (TextView) this.findViewById(R.id.id_install_scan_radiocount);
        mTVCountVw.setText("0");
        mRadioCountVw.setText("0");

        mTVLstVw = (NoTouchListView) this.findViewById(R.id.id_install_scan_tvlist);
        mTVLstVw.setDivider(null);
        mTVLstVw.setEnabled(false);
        mTVLstVw.setFocusable(false);
        mTVLstVw.setOnKeyListener(this);
        mTVLstVw.setOnTouchListener(this);

        mRadioLstVw = (NoTouchListView) this.findViewById(R.id.id_install_scan_radiolist);
        mRadioLstVw.setDivider(null);
        mRadioLstVw.setEnabled(false);
        mRadioLstVw.setFocusable(false);
        mRadioLstVw.setOnKeyListener(this);
        mRadioLstVw.setOnTouchListener(this);

        mScanFreq = (NoTouchListView) this.findViewById(R.id.id_install_scan_frqlist);
        mScanFreq.setDivider(null);
        mScanFreq.setEnabled(false);
        mScanFreq.setFocusable(false);
        mScanFreq.setOnKeyListener(this);
        mScanFreq.setOnTouchListener(this);

        mFreqArrayAdapter = new ScanFreqListAdapter(this, R.layout.install_dvbs_progress_freq_item, mFreqDataList);
        mScanFreq.setAdapter(mFreqArrayAdapter);

        ScanFreqItem item = new ScanFreqItem(); // add one item to get real height for listview
        mFreqDataList.add(item);

        mTVArrayAdapter = new StringListAdapter(this, R.layout.install_dvbs_progress_name_item, mTVDataList);
        mTVLstVw.setAdapter(mTVArrayAdapter);

        mRdoArrayAdapter = new StringListAdapter(this, android.R.layout.simple_expandable_list_item_1, mRadioDataList);
        mRadioLstVw.setAdapter(mRdoArrayAdapter);

        String nothing = ""; // add one item to get real height for progview
        mTVDataList.add(nothing);

        mIvSignalFlash = (ImageView) findViewById(R.id.id_install_scan_signal);
        if (mNetworkType != EnNetworkType.SATELLITE) {
            mIvSignalFlash.setVisibility(View.INVISIBLE);
        } else {
            mIvSignalFlash.setVisibility(View.VISIBLE);
        }
        mSignalFlash = (AnimationDrawable) mIvSignalFlash.getBackground();

        mTvPercent = (TextView) findViewById(R.id.id_install_scan_percentnum);
        mTvPercent.setText("0%");
        mProgressBar = (ProgressBar) findViewById(R.id.id_install_scan_percent);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = mDtv.getNetworkManager();
        mChannelManager = mDtv.getChannelManager();

        mPlayerManager = mDtv.getPlayerManager();
        if (mPlayerManager.getPlayers().size() > 0) {
            mPlayerManager.getPlayers().get(0).resumeResource();
        } else {
            mPlayerManager.createPlayer().resumeResource();
        }

        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_START, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, gScanListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_GET_PROG, gScanListener, 0);

        if (mLstNeedScanNetwork.size() > 0) {
            mCurrentScanNetwork = mLstNeedScanNetwork.get(0);
            mLstNeedScanNetwork.remove(mCurrentScanNetwork);
            if (EnNetworkType.TERRESTRIAL == mCurrentScanNetwork.getNetworkType()
                    || EnNetworkType.DTMB == mCurrentScanNetwork.getNetworkType()
                    || EnNetworkType.ISDB_TER == mCurrentScanNetwork.getNetworkType()) {
                mParamUnitType = " MHz";
            }

            LogTool.d(LogTool.MSCAN, " onResume mLstNeedScanNetwork size=" + mLstNeedScanNetwork.size());
            if (0 != mCurrentScanNetwork.startScan(mScanType)) {
                String strName = "";
                if (mCurrentScanNetwork.getNetworkType() == EnNetworkType.SATELLITE) {
                    strName = ((DVBSNetwork) mCurrentScanNetwork).getName();
                }
                String strTilte = String.format(getResources().getString(R.string.str_install_progress_start_fail),
                        strName);

                mCurrentScanNetwork.stopScan(false);
                if (mLstNeedScanNetwork.size() > 0) {
                    Toast.makeText(this, strTilte, Toast.LENGTH_LONG).show();
                    startNextScan();
                } else {
                    MyToast.makeText(this, strTilte, MyToast.LENGTH_LONG).show();
                    finishAndPlayChannel();
                }
            } else {
                mIsScanFinish = false;
                ((DTVApplication) getApplication()).setEnabledBook(false);
                mSignalFlash.start();
            }
        }
    }

    @Override
    public void onPause() {
        LogTool.d(LogTool.MSCAN, "onPause == mIsResultSaved=" + mIsResultSaved);
        stopScan();
        ((DTVApplication) getApplication()).setEnabledBook(true);

        mSignalFlash.stop();

        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_START, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, gScanListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_GET_PROG, gScanListener);
        LogTool.e(LogTool.MSCAN, "unSubScribeEvent --- ");

        if (mPlayerManager.getPlayers().size() > 0) {
            mPlayerManager.getPlayers().get(0).releaseResource(0);
        }
        if (!mIsScanFinish) {
            cancelScanResult();
            this.finish();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ((DTVApplication) getApplication()).setEnabledBook(true);
        super.onDestroy();
    }

    private void stopScan() {
        if (null != mCurrentScanNetwork) {
            LogTool.d(LogTool.MSCAN, "Stop scan =" + mCurrentScanNetwork.stopScan(false));
            mCurrentScanNetwork = null;
        }
    }

    private void getChanelById(int id) {
        Channel chl = mChannelManager.getChannelByID(id);
        String strFindTP = " not find channel id=" + id;
        if (null != chl) {
            if (EnServiceType.getTVServiceTypes().contains(chl.getServiceType())) {
                mTVDataList.add(chl.getChannelName());
                mTVCount++;
                mTVArrayAdapter.notifyDataSetChanged();
                mTVCountVw.setText(String.valueOf(mTVCount));
                if ((null == mFirstScanChannel)
                        || (!EnServiceType.getTVServiceTypes().contains(mFirstScanChannel.getServiceType()))) {
                    mFirstScanChannel = chl;
                }
            } else if (EnServiceType.getRadioServiceTypes().contains(chl.getServiceType())) {
                mRadioDataList.add(chl.getChannelName());
                mRadioCount++;
                mRdoArrayAdapter.notifyDataSetChanged();
                mRadioCountVw.setText(String.valueOf(mRadioCount));
                if (null == mFirstScanChannel) {
                    mFirstScanChannel = chl;
                }
            } else {
                strFindTP = "find the channel is not tv or radio";
                LogTool.v(LogTool.MSCAN, strFindTP);
            }

            strFindTP = "find channel name=" + chl.getChannelName();
            LogTool.d(LogTool.MSCAN, strFindTP);
        } else {
            LogTool.e(LogTool.MSCAN, strFindTP);
        }
    }

    private void saveScanResult() {
        mChannelManager.rebuildAllGroup();
        if (EnNetworkType.ISDB_TER == mNetworkType) {
            mChannelManager.sort(EnSortType.LCN, true);
        } else {

            LogTool.v(LogTool.MSCAN, "-- delChannelByTag --");
            mChannelManager.delChannelByTag(EnTagType.DEL);
        }

        if (0 != mNetworkManager.saveNetworks()) {
            String strTip = ScanProgressActivity.this.getString(R.string.str_save_fail);
            Looper.prepare();
            MyToast.makeText(ScanProgressActivity.this, strTip, MyToast.LENGTH_SHORT).show();
            Looper.loop();
            mIsResultSaved = false;
        } else {
            mIsResultSaved = true;
        }

        LogTool.v(LogTool.MSCAN, " HI_SVR_EVT_SRCH_FINISH saveScanResult=" + mIsResultSaved);
    }

    private void cancelScanResult() {
        mNetworkManager.recoverNetworks();

        LogTool.v(LogTool.MSCAN, " HI_SVR_EVT_SRCH_FINISH saveScanResult=" + mIsResultSaved);
    }

    private void finishAndPlayChannel() {
        mIsScanFinish = true;
        if (null != mFirstScanChannel) {
            EnTVRadioFilter type = EnTVRadioFilter.RADIO;
            if (EnServiceType.getTVServiceTypes().contains(mFirstScanChannel.getServiceType())) {
                type = EnTVRadioFilter.TV;

            }
            mChannelManager.setChannelServiceTypeMode(type);
            ArrayList<ChannelList> groupsList = (ArrayList<ChannelList>) mChannelManager.getUseGroups();

            if ((null != groupsList) && (!groupsList.isEmpty())) {
                mFirstScanChannel = groupsList.get(0).getChannelByIndex(0);
                ChannelHistory.getInstance().setCurrent(halApi.EnumSourceIndex.SOURCE_DVBS, groupsList.get(0),
                        mFirstScanChannel);
            }

        }
        finish();
    }

    private void queryExist() {
        String strTitle = getResources().getString(R.string.str_install_scan_stop_query);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(strTitle);
        builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                stopScan();
                dialog.cancel();
                querySave();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCurrentScanNetwork.resumeScan();
                dialog.cancel();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                int action = event.getAction();
                if ((action == KeyEvent.ACTION_DOWN) && (0 == event.getRepeatCount())) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        mCurrentScanNetwork.resumeScan();
                        dialog.cancel();
                    }
                }
                return false;
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void querySave() {
        if ((mTVCount <= 0) && (mRadioCount <= 0)) {
            if (null != mNetworkManager) {
                mNetworkManager.recoverNetworks();
            }

            finishAndPlayChannel();
            return;
        }
        String strTitle = getResources().getString(R.string.str_save_query);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(strTitle);
        builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        saveScanResult();
                    }
                };
                TaskUtil.post(runnable);
                dialog.cancel();
                finishAndPlayChannel();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                cancelScanResult();
                dialog.cancel();
                finishAndPlayChannel();
            }
        });
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                int action = event.getAction();
                if ((action == KeyEvent.ACTION_DOWN) && (0 == event.getRepeatCount())) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        cancelScanResult();
                        dialog.cancel();
                        finishAndPlayChannel();
                    }
                }
                return false;
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                LogTool.v(LogTool.MSCAN, "onKeyDown KEYCODE_BACK ");
                if (mCurrentScanNetwork != null) {
                    LogTool.v(LogTool.MSCAN, "onKeyDown KEYCODE_BACK: mCurrentScanNetwork is not null;");
                    if (0 == mCurrentScanNetwork.pauseScan()) {
                        queryExist();
                    }
                }
                return true;
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startNextScan() {
        boolean IsStartSucess = false;
        stopScan();
        SystemClock.sleep(STOPSCAN_STARTSCAN_MIN_SPACE);

        while ((!IsStartSucess) && (0 < mLstNeedScanNetwork.size())) {
            mCurrentScanNetwork = mLstNeedScanNetwork.get(0);
            mLstNeedScanNetwork.remove(mCurrentScanNetwork);
            LogTool.v(LogTool.MSCAN, " while IsStartSuccess=" + IsStartSucess);

            if (EnNetworkType.TERRESTRIAL == mCurrentScanNetwork.getNetworkType()
                    || EnNetworkType.DTMB == mCurrentScanNetwork.getNetworkType()
                    || EnNetworkType.ISDB_TER == mCurrentScanNetwork.getNetworkType()) {
                mParamUnitType = " MHz";
            }

            if (null != mScanType && (mScanType.getBaseType() == ScanType.EnBaseScanType.AUTO_FULL
                    || mScanType.getBaseType() == ScanType.EnBaseScanType.STEP)) {
                mChannelManager.deleteChannelsByNetworkType(mCurrentScanNetwork.getNetworkType());
                mChannelManager.rebuildAllGroup();
            }

            IsStartSucess = (0 == mCurrentScanNetwork.startScan(mScanType));
            mIsScanFinish = false;
            if (!IsStartSucess) {
                mCurrentScanNetwork.stopScan(false);

                String strName = "";
                if (mCurrentScanNetwork.getNetworkType() == EnNetworkType.SATELLITE) {
                    strName = ((DVBSNetwork) mCurrentScanNetwork).getName();
                }

                String strTilte = String.format(getResources().getString(R.string.str_install_progress_start_fail),
                        strName);
                Toast.makeText(this, strTilte, Toast.LENGTH_LONG).show();
            }
        }
        LogTool.v(LogTool.MSCAN, " startNextScan IsStartSucess=" + IsStartSucess);
        if (!IsStartSucess) {
            // Search program to saving, not to search the program without saving
            querySave();
        }
    }

    IDTVListener gScanListener = new IDTVListener() {
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            LogTool.d(LogTool.MSCAN, " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_START: {
                    EnNetworkType networkType = mCurrentScanNetwork.getNetworkType();
                    mFreCount++;
                    mCurrentFreqChannelCount = 0;
                    ScanFreqItem item = new ScanFreqItem();
                    item.Count = Integer.toString(mFreCount);
                    if (EnNetworkType.SATELLITE == networkType) {
                        item.Name = ((DVBSNetwork) mCurrentScanNetwork).getName();
                    }
                    if (EnNetworkType.TERRESTRIAL == networkType || EnNetworkType.DTMB == networkType) {
                        item.Freq = FREQ_DF.format((((float) param1) / TP_UNIT_RATE)) + " MHz";
                    } else if (EnNetworkType.ISDB_TER == networkType) {
                        item.Freq = FREQ_DF.format(((float) param1)) + " KHz";
                    } else {
                        item.Freq = (param1 / TP_UNIT_RATE) + " MHz";
                    }
                    item.Rate = (parm2 / TP_UNIT_RATE) + mParamUnitType;
                    item.Polarity = "";
                    if (EnNetworkType.SATELLITE == networkType) {
                        //item.Polarity = ((Parcel) obj).readString();
                    }
                    LogTool.v(LogTool.MSCAN, " HI_SVR_EVT_SRCH_LOCK_START Polarity = " + item.Polarity);
                    item.Status = "...";
                    mFreqDataList.add(item);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS: {
                    ScanFreqItem item = mFreqDataList.get(mFreCount);
                    LogTool.d(LogTool.MSCAN, " notifyMessage STATUS lock state=" + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE: {
                    int percent = param1 % 100;
                    if ((0 == percent) && (param1 > 99)) {
                        percent = 100;
                    }
                    mProgressBar.setProgress(percent);
                    mTvPercent.setText(percent + "%");
                    LogTool.d(LogTool.MSCAN, " notifyMessage SCHEDULE param1=" + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_FINISH: {
                    startNextScan();
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH: {
                    ScanFreqItem item = mFreqDataList.get(mFreCount);
                    // If the number of programs for 0 is not a status update
                    if (0 < mCurrentFreqChannelCount) {
                        LogTool.v(LogTool.MSCAN, "one freq finish,new =" + param1 + ",drop=" + parm2 + "," +
                                "mCurrentFreqChannelCount=" + mCurrentFreqChannelCount);
                        item.Status = getString(R.string.str_install_tuner_sucess);
                        mFreqArrayAdapter.notifyDataSetChanged();
                    } else {
                        item.Status = getString(R.string.str_install_tuner_lock_fail);
                        mFreqArrayAdapter.notifyDataSetChanged();
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_GET_PROG: {
                    mCurrentFreqChannelCount++;
                    getChanelById(parm2);
                    LogTool.d(LogTool.MSCAN, "find the channel : tpid=" + param1 + ",channel id=" + parm2);
                    break;
                }
                default:
                    break;
            }
        }
    };

    private class ViewNameHolder {
        public TextView mTvName = null;
    }

    private class StringListAdapter extends ArrayAdapter<String> {
        private final LayoutInflater mInflater;

        public StringListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = LayoutInflater.from(context);
        }

        public StringListAdapter(Context _context, int _resource, List<String> _items) {
            super(_context, _resource, _items);
            this.mInflater = LayoutInflater.from(_context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewNameHolder holder = null;
            if (null == convertView) {
                holder = new ViewNameHolder();
                convertView = mInflater.inflate(R.layout.install_dvbs_progress_name_item, null);
                holder.mTvName = (TextView) convertView.findViewById(R.id.id_dvbs_progress_name_itemname);
                convertView.setTag(holder);
                if (!mIsFixProgListViewHeight) {
                    convertView.measure(0, 0);
                    ViewGroup.LayoutParams params = mTVLstVw.getLayoutParams();
                    LogTool.d(LogTool.MSCAN,
                            "item height=" + convertView.getMeasuredHeight() + "listview  Height=" + params.height);
                    params.height = convertView.getMeasuredHeight() * PROGRAM_LIST_ITEM_NUM + MARGINS_TOP;
                    mTVLstVw.setLayoutParams(params);
                    mRadioLstVw.setLayoutParams(params);
                    mIsFixProgListViewHeight = true;
                }
            } else {
                holder = (ViewNameHolder) convertView.getTag();
            }

            String strName = getItem(position);
            holder.mTvName.setText(strName);
            return convertView;
        }
    }

    private class ScanFreqItem {
        public String Count;
        public String Name;
        public String Freq;
        public String Rate;
        public String Polarity;
        public String Status;
    }

    private final class ViewScanFreqHoder {
        public TextView vCount;
        public TextView vName;
        public TextView vFreq;
        public TextView vRate;
        public TextView vPolarity;
        public TextView vStatus;
    }

    private class ScanFreqListAdapter extends ArrayAdapter<ScanFreqItem> {
        private final LayoutInflater mInflater;

        public ScanFreqListAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
            this.mInflater = LayoutInflater.from(context);
        }

        public ScanFreqListAdapter(Context _context, int _resource, List<ScanFreqItem> _items) {
            super(_context, _resource, _items);
            this.mInflater = LayoutInflater.from(_context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewScanFreqHoder holder = null;
            if (null == convertView) {
                holder = new ViewScanFreqHoder();
                convertView = mInflater.inflate(R.layout.install_dvbs_progress_freq_item, null);
                holder.vCount = (TextView) convertView.findViewById(R.id.id_dvbs_progress_count);
                holder.vName = (TextView) convertView.findViewById(R.id.id_dvbs_progress_name);
                holder.vFreq = (TextView) convertView.findViewById(R.id.id_dvbs_progress_freq);
                holder.vRate = (TextView) convertView.findViewById(R.id.id_dvbs_progress_rate);
                holder.vPolarity = (TextView) convertView.findViewById(R.id.id_dvbs_progress_polarity);
                holder.vStatus = (TextView) convertView.findViewById(R.id.id_dvbs_progress_status);
                convertView.setTag(holder);

                if (!mIsFixFeqListViewHeight) {
                    convertView.measure(0, 0);
                    ViewGroup.LayoutParams params = mScanFreq.getLayoutParams();
                    LogTool.d(LogTool.MSCAN,
                            "item height=" + convertView.getMeasuredHeight() + "listview  Height=" + params.height);
                    params.height = convertView.getMeasuredHeight() * FREQUENCY_LIST_ITEM_NUM + MARGINS_TOP;
                    mScanFreq.setLayoutParams(params);
                    mIsFixFeqListViewHeight = true;
                }
            } else {
                holder = (ViewScanFreqHoder) convertView.getTag();
            }

            ScanFreqItem scanFreqItem = getItem(position);
            holder.vCount.setText(scanFreqItem.Count);
            holder.vName.setText(scanFreqItem.Name);
            holder.vFreq.setText(scanFreqItem.Freq);
            holder.vRate.setText(scanFreqItem.Rate);
            holder.vPolarity.setText(scanFreqItem.Polarity);
            holder.vStatus.setText(scanFreqItem.Status);

            return convertView;
        }
    }

    @Override
    public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
        LogTool.v(LogTool.MSCAN, "onKey keycode =" + arg2.getKeyCode());
        return true;
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        return true;
    }
}
