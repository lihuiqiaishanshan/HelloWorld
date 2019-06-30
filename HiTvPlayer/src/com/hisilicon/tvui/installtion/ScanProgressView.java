package com.hisilicon.tvui.installtion;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Context;

import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

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
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.dtv.network.service.EnServiceType;
import com.hisilicon.dtv.util.PublicDefine;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.ChannelHistory;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.view.MyToast;


public class ScanProgressView extends BaseView implements IScanSubWnd {
    private static final int TP_UNIT_RATE = 1000;
    private static final int STOPSCAN_STARTSCAN_MIN_SPACE = 1000;
    //a never used channel value used in  changesource to ATV scan program
    private static final int HI_CHANNEL_SCAN_FLAG = 0x7FFF;
    private static final DecimalFormat FREQ_DF = new DecimalFormat("#######.##");

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;
    private TextView mTvCountTxt = null;
    private TextView mRadioCountTxt = null;
    private TextView mAtvCountTxt = null;
    private TextView mCurrentFreqTxt = null;
    private TextView mPercentTxt = null;
    private TextView mAtscChTxt = null;
    private SeekBar mProgressBar = null;
    private RelativeLayout scanProgressFreqLayout, scanProgressAtscCHLayout;

    private EnNetworkType mDtvNetworkType = null;
    private Network mCurrentScanNetwork = null;
    private List<Network> mLstNeedScanNetwork = null;
    private Channel mFirstScanChannel = null;
    private int mTVCount = 0;
    private int mRadioCount = 0;
    private int mAtvCount = 0;
    private boolean mIsScanFinish = false;
    private boolean mIsBackStop = false;
    private boolean isStartSuccess = false;
    private boolean needWaite = false;
    private int curDTVSourceId = -1;
    private boolean isMixScan = false;

    public ScanProgressView(BaseActivity arg0) {
        super((LinearLayout) arg0.findViewById(R.id.ly_scan_progress));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd) arg0;
        initView();
    }

    private static String formatFreqDf(float value) {
        synchronized (FREQ_DF) {
            return FREQ_DF.format(value);
        }
    }

    private void initView() {
        scanProgressFreqLayout = (RelativeLayout) mParentWnd.findViewById(R.id.scan_progress_freq_layout);
        scanProgressAtscCHLayout = (RelativeLayout) mParentWnd.findViewById(R.id.scan_progress_atsc_ch_layout);
        mTvCountTxt = (TextView) mParentWnd.findViewById(R.id.tv_count_txt);
        mRadioCountTxt = (TextView) mParentWnd.findViewById(R.id.radio_count_txt);
        mAtvCountTxt = (TextView) mParentWnd.findViewById(R.id.atv_count_txt);
        mCurrentFreqTxt = (TextView) mParentWnd.findViewById(R.id.freq_txt);
        mPercentTxt = (TextView) mParentWnd.findViewById(R.id.seekbar_value_txt);
        mProgressBar = (SeekBar) mParentWnd.findViewById(R.id.widget_seekbar);
        mAtscChTxt = (TextView) mParentWnd.findViewById(R.id.atsc_ch_txt);
    }

    @Override
    public void show() {
        Intent mIntentHidDra = new Intent();
        mIntentHidDra.setAction(CommonValue.HIDE_DRA);
        mParentWnd.sendBroadcast(mIntentHidDra);

        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_BEGIN, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_START, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_GET_PROG, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_BEGIN, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_PROGRESS, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_LOCK, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, gScanListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_ATV_EVT_SELECT_SOURCE_COMPLETE, gScanListener, 0);

        mLstNeedScanNetwork = ((DTVApplication) mParentWnd.getApplication()).getScanParamNetwork();
        ((DTVApplication) mParentWnd.getApplication()).setEnabledBook(false);
        LogTool.d(LogTool.MPLAY, "mLstNeedScanNetwork size =" + mLstNeedScanNetwork);
        if (mLstNeedScanNetwork.size() > 0) {
            if (mLstNeedScanNetwork.get(0).getNetworkType() == EnNetworkType.RF && !isMixScan) {
                mCurrentScanNetwork = mLstNeedScanNetwork.get(0);
                mLstNeedScanNetwork.remove(mCurrentScanNetwork);
                if (halApi.isDTVSource(halApi.getCurSourceID())) {
                    if (null != mPlayer) {
                        mPlayer.releaseResource(0);
                    }
                    halApi.changeSource(halApi.getCurSourceID(), halApi.EnumSourceIndex.SOURCE_ATV, HI_CHANNEL_SCAN_FLAG);
                    needWaite = true;
                    isStartSuccess = true;
                    isMixScan = true;
                } else {
                    startATVScan();
                }
            } else {
                startNextScan();
            }
        }
        mParentWnd.getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);

        super.show();
    }

    @Override
    public void hide() {
        stopScan();

        ((DTVApplication) mParentWnd.getApplication()).setEnabledBook(true);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_BEGIN, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_START, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_FINISH, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SRCH_GET_PROG, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_BEGIN, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_PROGRESS, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_LOCK, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SCAN_FINISH, gScanListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_ATV_EVT_SELECT_SOURCE_COMPLETE, gScanListener);

        if (!mIsScanFinish) {
            cancleScanResult();
            playFirstChannel();
        }
        WindowManager.LayoutParams lp = mParentWnd.getWindow().getAttributes();
        lp.privateFlags &= ~(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY
                | Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);
        mParentWnd.getWindow().addPrivateFlags(lp.privateFlags);
        super.hide();
        LogTool.d(LogTool.MSCAN, "hide end");
    }


    @Override
    public void toggle() {
        if (super.isShow()) {
            hide();
        } else {
            show();
        }
    }

    @Override
    public boolean isCanStartScan() {
        return true;
    }

    @Override
    public boolean isNetworkScan() {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        LogTool.v(LogTool.MSCAN, "keyDispatch keyCode " + keyCode);
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_BACK:
            case KeyValue.DTV_KEYVALUE_HOME:
            case KeyValue.DTV_KEYVALUE_SOURCE:
                if (mCurrentScanNetwork != null && 0 == mCurrentScanNetwork.pauseScan()) {
                    mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_STOP_SCAN, null);
                    LogTool.v(LogTool.MSCAN, "keyDispatch queryExist");
                }
                break;
            case KeyValue.DTV_KEYVALUE_POWER:
                goToShutdown();
                break;
            default:
                break;
        }
        return KeyDoResult.DO_OVER;
    }

    /**
     * 待机
     */
    private void goToShutdown() {
        PowerManager pm = (PowerManager) mParentWnd.getSystemService(Context.POWER_SERVICE);
        if (SystemProperties.get(("persist.prop.suspend.mode")).equals("lightsleep")) {
            LogTool.d(LogTool.MSCAN, "powersave suspend");
            pm.setPowerSaveMode(true);
        } else if (SystemProperties.get("persist.prop.suspend.mode").equals("str")) {
            LogTool.d(LogTool.MSCAN, "STR suspend");
            pm.goToSleep(SystemClock.uptimeMillis(),
                    PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON,
                    PowerManager.GO_TO_SLEEP_FLAG_NO_DOZE);
        } else if (SystemProperties.get("persist.prop.suspend.mode").equals("shutdown")) {
            LogTool.d(LogTool.MSCAN, "shut down");
            Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
            intent.putExtra(Intent.EXTRA_KEY_CONFIRM, false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mParentWnd.startActivity(intent);
        }
    }

    private void startNextScan() {
        LogTool.d(LogTool.MSCAN, " startNextScan " + mLstNeedScanNetwork.size() +
                ",backStop:" + mIsBackStop + ",IsStartSuccess:" + isStartSuccess);

        while (!isStartSuccess && (0 < mLstNeedScanNetwork.size()) && !mIsBackStop) {
            mCurrentScanNetwork = mLstNeedScanNetwork.get(0);
            mLstNeedScanNetwork.remove(mCurrentScanNetwork);

            int dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
            int curSource = halApi.getCurSourceID();
            if (halApi.isDTVSource(curSource)) {
                curDTVSourceId = curSource;
            }
            EnNetworkType networkType = mCurrentScanNetwork.getNetworkType();
            ScanType mScantype = ((DTVApplication) mParentWnd.getApplication()).getScanType(networkType);
            if (EnNetworkType.TERRESTRIAL == networkType || EnNetworkType.DTMB == networkType
                    || EnNetworkType.CABLE == networkType || EnNetworkType.ISDB_TER == networkType) {
                switch (networkType) {
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
                    case ATSC_CAB:
                        dstSource = halApi.EnumSourceIndex.SOURCE_ATSC;
                        break;
                    default:
                        break;
                }

                // 当前源是ATV，需要切换成DTV源
                if (halApi.isATVSource(curSource)) {
                    mDTV.prepareDTV();
                    if (null != mPlayer) {
                        mPlayer.releaseResource(0);
                    }
                    LogTool.d(LogTool.MSCAN, "startNextScan change to " + dstSource);
                    halApi.changeSource(curSource, dstSource);
                    curSource = dstSource;
                }

                if (null != mScantype &&
                        (mScantype.getBaseType() == ScanType.EnBaseScanType.AUTO_FULL ||
                                mScantype.getBaseType() == ScanType.EnBaseScanType.STEP ||
                                mScantype.getBaseType() == ScanType.EnBaseScanType.AUTO_FULL_FIRST ||
                                mScantype.getBaseType() == ScanType.EnBaseScanType.BLIND )) {
                    mDtvNetworkType = networkType;
                    halApi.changeSource(curSource, dstSource);
                    curSource = dstSource;
                    mChannelManager.deleteChannelsByNetworkType(networkType);
                    LogTool.d(LogTool.MSCAN, "deleteChannelsByNetworkType");
                    mChannelManager.rebuildAllGroup();
                    ChannelHistory.getInstance().setCurrent(curSource, null, null);
                    TaskUtil.post(new Runnable() {
                        public void run() {
                            mBookManager.clearAllTasks();
                        }
                    });
                }
            } else if (EnNetworkType.RF == mCurrentScanNetwork.getNetworkType()) {
                if (isMixScan) {
                    if (null != mPlayer) {
                        mPlayer.releaseResource(0);
                    }
                    LogTool.d(LogTool.MSCAN, "startNextScan change to ATV");
                    //开机后直接搜台 会发现source切换不过 搜台退出的问题
                    //scan program in DTV+ATV mode,when scan DTV over,will changesourcre to ATV and play ATV channel
                    //so edit changesource interface to  a designated channel value in case of play ATV channel
                    halApi.changeSource(curSource, halApi.EnumSourceIndex.SOURCE_ATV, HI_CHANNEL_SCAN_FLAG);
                    needWaite = true;
                    isStartSuccess = true;
                }
            }

            if (null != mPlayer) {
                mPlayer.releaseResource(0);
            }
            if (!needWaite) {
                LogTool.d(LogTool.MSCAN, "before start scan :" + mScantype);
                isStartSuccess = (0 == mCurrentScanNetwork.startScan(mScantype));
                LogTool.d(LogTool.MSCAN, "after start scan :" + mScantype);
                if (!isStartSuccess) {
                    mCurrentScanNetwork.stopScan(false);

                    String strName = "";
                    if (mCurrentScanNetwork.getNetworkType() == EnNetworkType.SATELLITE) {
                        strName = ((DVBSNetwork) mCurrentScanNetwork).getName();
                    }

                    String strTilte = String.format(mParentWnd.getString(R.string.str_install_progress_start_fail), strName);
                    MyToast.makeText(mParentWnd, strTilte, MyToast.LENGTH_LONG).show();
                } else {
                    mIsScanFinish = false;
                }
                LogTool.d(LogTool.MSCAN, "current Scan IsStartSuccess : " + isStartSuccess);
            }
        }
        if (!isStartSuccess && !mIsBackStop) {
            // Search program to saving, not to search the program without saving
            directSave();
        }
    }

    private void stopScan() {
        int result = 0;
        if (null != mCurrentScanNetwork) {
            result = mCurrentScanNetwork.stopScan(false);
            LogTool.d(LogTool.MSCAN, "Stop scan result : " + (result == 0));
            mCurrentScanNetwork = null;
        }
    }

    private void startATVScan() {
        EnNetworkType networkType = mCurrentScanNetwork.getNetworkType();
        ScanType mScantype = ((DTVApplication) mParentWnd.getApplication()).getScanType(networkType);
        LogTool.d(LogTool.MSCAN, "before start scan :" + mScantype);
        isStartSuccess = (0 == mCurrentScanNetwork.startScan(mScantype));
        LogTool.d(LogTool.MSCAN, "after start scan :" + mScantype);
        if (!isStartSuccess) {
            mCurrentScanNetwork.stopScan(false);

            String strName = "";
            if (mCurrentScanNetwork.getNetworkType() == EnNetworkType.SATELLITE) {
                strName = ((DVBSNetwork) mCurrentScanNetwork).getName();
            }

            String strTilte = String.format(mParentWnd.getString(R.string.str_install_progress_start_fail), strName);
            MyToast.makeText(mParentWnd, strTilte, MyToast.LENGTH_LONG).show();
        } else {
            mIsScanFinish = false;
        }
        isStartSuccess = false;
        LogTool.d(LogTool.MSCAN, "current Scan IsStartSuccess : " + isStartSuccess);
    }

    private void getChanelById(int id) {
        Channel chl = mChannelManager.getChannelByID(id);
        String strFindTP = " not find channel id=" + id;
        if (null != chl) {
            if (EnServiceType.getTVServiceTypes().contains(chl.getServiceType())) {
                mTVCount++;
                if ((null == mFirstScanChannel) || (!EnServiceType.getTVServiceTypes().contains(mFirstScanChannel.getServiceType()))) {
                    mFirstScanChannel = chl;
                }
                mTvCountTxt.setText("" + mTVCount);
            } else if (EnServiceType.getRadioServiceTypes().contains(chl.getServiceType())) {
                mRadioCount++;
                if (null == mFirstScanChannel) {
                    mFirstScanChannel = chl;
                }
                mRadioCountTxt.setText("" + mRadioCount);
            } else {
                strFindTP = "find the channel is not tv or radio";
                LogTool.v(LogTool.MSCAN, strFindTP);
            }

            strFindTP = "find channel name is " + chl.getChannelName();
            LogTool.d(LogTool.MSCAN, strFindTP);
        } else {
            LogTool.w(LogTool.MSCAN, strFindTP);
        }
    }

    private void saveScanResult() {
        mChannelManager.rebuildAllGroup();

        LogTool.v(LogTool.MSCAN, "-- delChannelByTag --");
        mChannelManager.delChannelByTag(EnTagType.DEL);
        if (EnNetworkType.ISDB_TER == mDtvNetworkType) {
            mChannelManager.sort(EnSortType.LCN, true);
        }

        if (0 != mNetworkManager.saveNetworks()) {
            LogTool.v(LogTool.MSCAN, "-- saveNetworks error --");
            mNetworkManager.recoverNetworks();
            mChannelManager.rebuildAllGroup();
            String strTip = mParentWnd.getString(R.string.str_save_fail);
        } else {
            LogTool.d(LogTool.MSCAN, "saveScanResult success");
        }
    }

    private void cancleScanResult() {
        LogTool.d(LogTool.MSCAN, "cancleScanResult");
        mNetworkManager.recoverNetworks();
    }

    private void playFirstChannel() {
        if (null != mFirstScanChannel) {
            // reset mFirstScanChannel
            if (null != mNetworkManager && mNetworkManager.getCurrentNetworkType() != EnNetworkType.RF) {
                if (null != mChannelManager.getUseGroups() && mChannelManager.getUseGroups().size() > 0
                        && null != mChannelManager.getUseGroups().get(0)) {
                    mFirstScanChannel = mChannelManager.getUseGroups().get(0).getChannelByIndex(0);
                }
            }
            EnTVRadioFilter type = EnTVRadioFilter.RADIO;
            if (EnServiceType.getTVServiceTypes().contains(mFirstScanChannel.getServiceType())) {
                type = EnTVRadioFilter.TV;
            }
            mChannelManager.setChannelServiceTypeMode(type);
            ArrayList<ChannelList> groupsList = (ArrayList<ChannelList>) mChannelManager.getUseGroups();

            if ((null != groupsList) && (!groupsList.isEmpty())) {
                if (PublicDefine.FAIL == groupsList.get(0).getPosByChannelID(mFirstScanChannel.getChannelID())) {
                    mFirstScanChannel = groupsList.get(0).getChannelByIndex(0);
                }

                ChannelHistory.getInstance().setCurrent(halApi.EnumSourceIndex.SOURCE_DVBC, groupsList.get(0), mFirstScanChannel);
            }
        }

        //refresh ATV channel list
        Channel mFirstAtvScanChannel = mAtvChannelManager.getDefaultOpenChannel();
        ChannelList mAtvChannelList = mAtvChannelManager.getAllChannelList();
        ChannelHistory.getInstance().setCurrent(halApi.EnumSourceIndex.SOURCE_ATV,
                mAtvChannelList, mFirstAtvScanChannel);
    }

    private void finishAndPlayChannel() {
        mIsScanFinish = true;
        playFirstChannel();
        mParentWnd.finish();
    }

    public void queryExist() {
        String strTilte = mParentWnd.getString(R.string.str_install_scan_stop_query);
        AlertDialog.Builder builder = new AlertDialog.Builder(mParentWnd, R.style.DIM_STYLE);
        builder.setMessage(strTilte);
        builder.setPositiveButton(mParentWnd.getString(R.string.str_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mIsBackStop = true;
                stopScan();
                dialog.cancel();
                directSave();
            }
        });
        builder.setNegativeButton(mParentWnd.getString(R.string.str_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                mCurrentScanNetwork.resumeScan();
                dialog.cancel();
            }
        });
        builder.setOnKeyListener(new android.content.DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                int action = event.getAction();
                if (action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyValue.DTV_KEYVALUE_BACK) {
                        mCurrentScanNetwork.resumeScan();
                        dialog.cancel();
                        return true;
                    }
                }
                return false;
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

    private void directSave() {
        LogTool.d(LogTool.MSCAN, "directSave AtvCount : " + mAtvCount + ", DtvCount : " + mTVCount
                + ", RadioCount : " + mRadioCount);
        if ((mTVCount <= 0) && (mRadioCount <= 0) && (mAtvCount <= 0)) {
            if (null != mNetworkManager) {
                mNetworkManager.recoverNetworks();
            }
        } else {
            TaskUtil.post(new Runnable() {
                public void run() {
                    saveScanResult();
                }
            });
        }
        finishAndPlayChannel();
        return;
    }

    IDTVListener gScanListener = new IDTVListener() {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj) {
            switch (messageID) {
                case DTVMessage.HI_SVR_EVT_SRCH_BEGIN: {
                    LogTool.d(LogTool.MSCAN, "scan message : DTV scan begin");
                    int percent = 0;
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress(percent);
                    mPercentTxt.setText(percent + "%");
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_START: {
                    if (mCurrentScanNetwork == null)
                        return;
                    EnNetworkType networkType = mCurrentScanNetwork.getNetworkType();
                    if (EnNetworkType.ATSC_CAB == networkType || EnNetworkType.ATSC_T == networkType) {
                        LogTool.d(LogTool.MSCAN, "scan message : param1 : " + param1 + " param2 : " + param2);
                        if (scanProgressAtscCHLayout.getVisibility() != View.VISIBLE) {
                            scanProgressAtscCHLayout.setVisibility(View.VISIBLE);
                            scanProgressFreqLayout.setVisibility(View.GONE);
                        }
                        mAtscChTxt.setText("" + param1);
                    } else {
                        if (scanProgressFreqLayout.getVisibility() != View.VISIBLE) {
                            scanProgressAtscCHLayout.setVisibility(View.GONE);
                            scanProgressFreqLayout.setVisibility(View.VISIBLE);
                        }
                        String strFreq;
                        if (EnNetworkType.TERRESTRIAL == networkType || EnNetworkType.DTMB == networkType) {
                            strFreq = formatFreqDf((((float) param1) / TP_UNIT_RATE)) + " MHz";
                        } else if (EnNetworkType.ISDB_TER == networkType) {
                            strFreq = formatFreqDf(((float) param1)) + " KHz";
                        } else {
                            strFreq = (param1 / TP_UNIT_RATE) + " MHz";
                        }
                        mCurrentFreqTxt.setText(strFreq);
                        LogTool.d(LogTool.MSCAN, "scan message : DTV scan lock start freq : " + strFreq);
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_LOCK_STATUS: {
                    //LogTool.d(LogTool.MSCAN, "scan message : DTV scan lock status is " + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_FREQ_INFO: {
                    //LogTool.d(LogTool.MSCAN, "scan message : DTV scan current freq is "
                    //+ param1 + ", tpid is " + param2);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_CUR_SCHEDULE: {
                    LogTool.d(LogTool.MSCAN, "scan message : DTV scan progress : " + param1);
                    int percent = param1 % 100;
                    if ((0 == percent) && (param1 > 99)) {
                        percent = 100;
                    }
                    mProgressBar.setMax(100);
                    if (percent < 100) {
                        mProgressBar.setProgress(percent);
                        mPercentTxt.setText(percent + "%");
                    }
                    if (param1 == 100) {
                        mProgressBar.setProgress(100);
                        mPercentTxt.setText(100 + "%");
                    }
                    break;
                }
                case DTVMessage.HI_ATV_EVT_SCAN_FINISH: {
                    LogTool.d(LogTool.MSCAN, "scan message : ATV/DTV scan finish");
                    stopScan();
                    try {
                        Thread.sleep(STOPSCAN_STARTSCAN_MIN_SPACE);
                    } catch (InterruptedException e) {
                        LogTool.v(LogTool.MSCAN, "startNextScan sleep error :" + e);
                    }
                    mProgressBar.setProgress(100);
                    mPercentTxt.setText(100 + "%");
                    startNextScan();
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_FINISH: {
                    if (mCurrentScanNetwork != null && mCurrentScanNetwork.getNetworkType() == EnNetworkType.RF) {
                        return;
                    } else {
                        mProgressBar.setProgress(100);
                        mPercentTxt.setText(100 + "%");
                        stopScan();
                        isStartSuccess = false;
                        try {
                            Thread.sleep(STOPSCAN_STARTSCAN_MIN_SPACE);
                        } catch (InterruptedException e) {
                            LogTool.v(LogTool.MSCAN, "startNextScan sleep error :" + e);
                        }
                        if (((0 < mLstNeedScanNetwork.size()) && !mIsBackStop)) {
                            LogTool.d(LogTool.MSCAN, "mix scan");
                            isMixScan = true;
                        }
                        startNextScan();
                    }
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_ONE_FREQ_FINISH: {
                /*
                ScanFreqItem item = mlstFreqData.get(mFreCount);
                // If the number of programs for 0 is not a status update
                if (0 < mCurrentFreqChannelCount)
                {
                    LogTool.v(LogTool.MSCAN, "one freq finish,new =" + param1 + ",drop="
                            + param2 + ",mCurrentFreqChannelCount=" + mCurrentFreqChannelCount);
                    item.Status = getString(R.string.str_install_tuner_sucess);
                    mFreqArrayAdapter.notifyDataSetChanged();
                }
                else
                {
                    item.Status = getString(R.string.str_install_tuner_lock_fail);
                    mFreqArrayAdapter.notifyDataSetChanged();
                }
                */
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SRCH_GET_PROG: {
                    LogTool.d(LogTool.MSCAN, "scan message : DTV scan get program id : " + param2);
                    getChanelById(param2);
                    break;
                }

                case DTVMessage.HI_ATV_EVT_SCAN_BEGIN: {
                    LogTool.d(LogTool.MSCAN, "scan message : ATV scan begin");
                    int percent = 0;
                    mProgressBar.setMax(100);
                    mProgressBar.setProgress(percent);
                    mPercentTxt.setText(percent + "%");
                    mAtvCount = 0;
                    break;
                }
                case DTVMessage.HI_ATV_EVT_SCAN_LOCK: {
                    LogTool.d(LogTool.MSCAN, "scan message : ATV scan lock channel");
                    mAtvCount++;
                    mAtvCountTxt.setText("" + mAtvCount);
                    break;
                }
                case DTVMessage.HI_ATV_EVT_SCAN_PROGRESS: {
                    LogTool.d(LogTool.MSCAN, "scan message : ATV scan progress :"
                            + param1 + " , Freq :" + param2);
                    if (scanProgressFreqLayout.getVisibility() != View.VISIBLE) {
                        scanProgressAtscCHLayout.setVisibility(View.GONE);
                        scanProgressFreqLayout.setVisibility(View.VISIBLE);
                    }
                    int percent = param1;
                    int currentFreq = param2;
                    DecimalFormat df = new DecimalFormat("#.000");
                    float f = ((float) currentFreq) / 1000;
                    mCurrentFreqTxt.setText(df.format(f) + "MHz");
                    mProgressBar.setProgress(percent);
                    mPercentTxt.setText(percent + "%");
                    break;
                }
                case DTVMessage.HI_ATV_EVT_SELECT_SOURCE_COMPLETE: {
                    LogTool.d(LogTool.MSCAN, "HI_ATV_EVT_SELECT_SOURCE_COMPLETE");
                    // param2 is change source status;success is 0,fail is -1
                    if (isMixScan){
                        if (param2 == 0){
                            startATVScan();
                        } else {
                            LogTool.e(LogTool.MSCAN,"change source to ATV failed");
                            if (halApi.isDTVSource(curDTVSourceId)) {
                                halApi.changeSource(halApi.EnumSourceIndex.SOURCE_ATV, curDTVSourceId);
                                directSave();
                            }
                        }
                    }

                }
                default:
                    break;
            }
        }
    };

}
