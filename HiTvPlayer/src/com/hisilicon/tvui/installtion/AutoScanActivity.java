package com.hisilicon.tvui.installtion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.installtion.IScanSubWnd.KeyDoResult;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.CheckPassWordDialog.CheckPassWordDialogInterface;


public class AutoScanActivity extends BaseActivity implements IScanMainWnd {
    private static final String TAG = "AutoScanActivity";

    private ScanTypeView mScanTypeView = null;
    private SignalTypeView mSignalTypeView = null;
    private ScanProgressView mScanProgressView = null;
    private SetupCountryView mSetupCountryView = null;
    private SetupAreaCodeView mSetupAreaCodeView = null;
    private Dialog mCheckPassWordDialog = null;
    private final SubWndMsgHandler mMessageHandler = new SubWndMsgHandler(this);
    private CheckPassWordDialog.Builder mCheckPWBuilder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogTool.i(LogTool.MINSTALL, "===== onCreate =====");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install_auto_scan_activity);
        initView();
    }

    private void initView() {
        mSignalTypeView = new SignalTypeView(this);
        mScanTypeView = new ScanTypeView(this);
        mScanProgressView = new ScanProgressView(this);
        mSetupCountryView = new SetupCountryView(this);
        mSetupAreaCodeView = new SetupAreaCodeView(this);
    }

    // Start search program
    private void startScan() {
        // Add these judgments are because the monkey operating too fast, click on the search button
        // several times, abnormal problems caused by
        List<EnNetworkType> lstNetworkType = ((DTVApplication) this.getApplication()).getScanNetworkType();
        if (null == lstNetworkType || lstNetworkType.size() <= 0) {
            return;
        }
        int currentSource = halApi.getCurSourceID();
        HashMap<EnNetworkType, Integer> mMapSource = new HashMap<>();
        mMapSource.put(EnNetworkType.CABLE, new Integer(halApi.EnumSourceIndex.SOURCE_DVBC));
        mMapSource.put(EnNetworkType.TERRESTRIAL, new Integer(halApi.EnumSourceIndex.SOURCE_DVBT));
        mMapSource.put(EnNetworkType.DTMB, new Integer(halApi.EnumSourceIndex.SOURCE_DTMB));
        mMapSource.put(EnNetworkType.ATSC_T, new Integer(halApi.EnumSourceIndex.SOURCE_ATSC));
        mMapSource.put(EnNetworkType.ATSC_CAB, new Integer(halApi.EnumSourceIndex.SOURCE_ATSC));
        mMapSource.put(EnNetworkType.ISDB_TER, new Integer(halApi.EnumSourceIndex.SOURCE_ISDBT));
        mMapSource.put(EnNetworkType.RF, new Integer(halApi.EnumSourceIndex.SOURCE_ATV));

        int changedSource = mMapSource.get(mNetworkManager.getCurrentNetworkType());
        if (currentSource != changedSource) {
            halApi.changeSource(currentSource, changedSource);
        }

        List<Network> mLstScanNetwork = ((DTVApplication) this.getApplication()).getScanParamNetwork();
        if (null == mLstScanNetwork) {
            mLstScanNetwork = new ArrayList<>();
        }
        for (int i = 0; i < lstNetworkType.size(); i++) {
            EnNetworkType networkType = lstNetworkType.get(i);
            if (networkType == EnNetworkType.ATSC_CAB) {
                mNetworkManager.setCurrentNetworkType(EnNetworkType.ATSC_CAB);
                Settings.System.putInt(getContentResolver(), Util.SETTING_ATSC_NETWORK, 1);
            } else if (networkType == EnNetworkType.ATSC_T) {
                mNetworkManager.setCurrentNetworkType(EnNetworkType.ATSC_T);
                Settings.System.putInt(getContentResolver(), Util.SETTING_ATSC_NETWORK, 0);
            } else if (null == ((DTVApplication) this.getApplication()).getScanType(networkType)) {
                ScanType type = new ScanType();
                type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                type.enableNit(false);
                type.setFTAFilter(EnScrambleFilter.ALL);
                type.setTVRadioFilter(EnTVRadioFilter.ALL);
                ((DTVApplication) this.getApplication()).setScanType(networkType, type);
            }

            boolean networkFound = false;
            for (int j = 0; j < mLstScanNetwork.size(); j++) {
                Network dvbNetwork = mLstScanNetwork.get(j);
                if (networkType == dvbNetwork.getNetworkType()) {
                    networkFound = true;
                    break;
                }
            }

            if (!networkFound) {
                List<Network> mLstNetwork = mNetworkManager.getNetworks(networkType, mDTV.getCountry());
                if (null == mLstNetwork || mLstNetwork.size() <= 0) {
                    mLstNetwork = mNetworkManager.getNetworks(networkType);
                }

                if (null != mLstNetwork && mLstNetwork.size() > 0) {
                    mLstScanNetwork.add(mLstNetwork.get(0));
                }
            }
        }

        ((DTVApplication) this.getApplication()).setScanParam(mLstScanNetwork);

        mSignalTypeView.hide();
        mScanTypeView.hide();
        mSetupCountryView.hide();
        mSetupAreaCodeView.hide();
        mScanProgressView.show();
    }

    private void showNextView() {
        int i = 0;
        List<EnNetworkType> lstNetworkType = ((DTVApplication) this.getApplication()).getScanNetworkType();
        EnNetworkType dtvNetworkType = EnNetworkType.NONE;
        for (i = 0; lstNetworkType != null && i < lstNetworkType.size(); i++) {
            EnNetworkType tmpNetworkType = lstNetworkType.get(i);
            if (EnNetworkType.RF == tmpNetworkType) {
                continue;
            } else {
                dtvNetworkType = tmpNetworkType;
                break;
            }
        }

        if (mSignalTypeView.isShow()) {
            if (EnNetworkType.NONE != dtvNetworkType) {
                mSignalTypeView.hide();
                mSetupCountryView.setNetworkType(mSignalTypeView.getSelectNetworkType());
                mSetupCountryView.show();
            } else {
                startScan();
            }
        } else if (mSetupCountryView.isShow() || mSetupAreaCodeView.isShow()) {
            if (mSetupCountryView.isShow()) {
                if (mSetupAreaCodeView.isNeedSetArea()) {
                    mSetupCountryView.hide();
                    mSetupAreaCodeView.show();
                    return;
                } else {
                    mSetupAreaCodeView.restoreDefaultAreaCode();
                }
            }
            mSetupCountryView.hide();
            mSetupAreaCodeView.hide();
            if (EnNetworkType.CABLE == dtvNetworkType || EnNetworkType.TERRESTRIAL == dtvNetworkType) {
                mScanTypeView.setScanNetworkType(dtvNetworkType);
                mScanTypeView.show();
            } else if ((EnNetworkType.ISDB_TER == dtvNetworkType) || (EnNetworkType.DTMB == dtvNetworkType)) {
                startScan();
            } else if ((EnNetworkType.ATSC_T == dtvNetworkType) || (EnNetworkType.ATSC_CAB == dtvNetworkType)) {
                startScan();
            }
        } else if (mScanTypeView.isShow()) {
            startScan();
        }
    }

    private void showQuaryExit() {
        mScanProgressView.queryExist();
    }

    @Override
    public void onResume() {
        super.onResume();
        LogTool.i(LogTool.MINSTALL, "===== onResume =====");
        dismissPlayerTipView();
        boolean bNeedPassword = CommonValue.MENU_LOCK_OPEN == mDtvConfig.getInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
        // If the password lock, then display the password input box
        if ((bNeedPassword) && (!super.isFinishing())) {

            if (null == mCheckPassWordDialog) {
                if (mCheckPWBuilder == null) {
                    mCheckPWBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
                    mCheckPWBuilder.setOnKeyListener((v, keyCode, event) -> {
                        if ((KeyValue.DTV_KEYVALUE_BACK == keyCode) && (null != mCheckPassWordDialog)) {
                            mCheckPassWordDialog.cancel();
                            this.finish();
                            return true;
                        }
                        return false;
                    });
                }
                mCheckPassWordDialog = mCheckPWBuilder.create();
                mCheckPWBuilder.setPasswordTitle(getString(R.string.play_password_menu_lock));
                mCheckPassWordDialog.setCanceledOnTouchOutside(false);
                mCheckPWBuilder.setCheckPassWordsListener(new CheckPassWordDialogInterface() {
                    @Override
                    public void onCheck(int which, String passWord) {
                        switch (which) {
                            case CheckPassWordDialogInterface.PASSWORD_RIGHT:
                                mSignalTypeView.show();
                                break;
                        }
                    }
                });
                mCheckPassWordDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if ((mCheckPWBuilder != null) && mCheckPWBuilder.getPasswordisRight()) {
                            mSignalTypeView.show();
                            LogTool.d(LogTool.MSCAN, "mCheckPassWordDialog right");
                        } else {
                            finish();
                        }
                    }
                });
            }
            if (!mCheckPassWordDialog.isShowing()) {
                mCheckPassWordDialog.show();
            }
        } else {
            mSignalTypeView.show();
        }

        // Clear scan param
        ((DTVApplication) this.getApplication()).setScanType(null, null);
        ((DTVApplication) this.getApplication()).setScanNetworkType(null);
        ((DTVApplication) this.getApplication()).setScanParam(null);
    }

    @Override
    protected void onPause() {
        LogTool.i(LogTool.MINSTALL, "===== onPause =====");

        mSetupCountryView.hide();
        mSetupAreaCodeView.hide();
        mSignalTypeView.hide();
        mScanTypeView.hide();
        mScanProgressView.hide();
        int curSource = halApi.getCurSourceID();
        if (halApi.isDTVSource(curSource)) {
            mDTV.prepareDTV();
            mPlayer.resumeResource();
            //DTV After replaying, need to reconfigure the no signal blue screen.
            halApi.setBlueScreen();
        } else {
            mPlayer.releaseResource(0);
        }
        mPlayer.changeChannel(mChnHistory.getCurrentChn(curSource));
        super.onPause();
        finish();
    }

    @Override
    public void onDestroy() {
        LogTool.i(LogTool.MINSTALL, "===== onDestroy =====");
        mCheckPassWordDialog = null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogTool.d(LogTool.MINSTALL, "Scan onKeyDown . KeyCode = " + keyCode);
        if (mSignalTypeView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mSignalTypeView keyDispatch . ");
            mSignalTypeView.keyDispatch(keyCode, event, null);
        }
        if (mSetupCountryView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mSetupCountryView keyDispatch . ");
            mSetupCountryView.keyDispatch(keyCode, event, null);
        }
        if (mScanTypeView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mScanTypeView keyDispatch ");
            if (keyCode == KeyValue.DTV_KEYVALUE_DPAD_LEFT
                    || keyCode == KeyValue.DTV_KEYVALUE_DPAD_RIGHT) {
                return true;
            }
        }
        if (mScanProgressView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mScanProgressView keyDispatch . ");
            mScanProgressView.keyDispatch(keyCode, event, null);
            return true;
        }
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_BACK: {
                if (mSignalTypeView.isShow()) {
                    //ATSC source has 2 network types, it will change programs to be shown
                    EnNetworkType networkType = mSignalTypeView.getSelectNetworkType();
                    if (EnNetworkType.ATSC_CAB == networkType || EnNetworkType.ATSC_T == networkType) {
                        mNetworkManager.setCurrentNetworkType(networkType);
                        Settings.System.putInt(getContentResolver(), Util.SETTING_ATSC_NETWORK, EnNetworkType.ATSC_T == networkType ? 0 : 1);
                    }
                    finish();
                    break;
                } else if (mSetupAreaCodeView.isShow()) {
                    mSetupAreaCodeView.hide();
                    mSetupCountryView.setNetworkType(mSignalTypeView.getSelectNetworkType());
                    mSetupCountryView.show();
                } else if (mSetupCountryView.isShow()) {
                    mSetupCountryView.hide();
                    mSignalTypeView.show();
                    return true;
                } else if (mScanTypeView.isShow()) {
                    mScanTypeView.hide();
                    if (mSetupAreaCodeView.isNeedSetArea()) {
                        mSetupAreaCodeView.show();
                        return true;
                    }
                    mSetupCountryView.setNetworkType(mSignalTypeView.getSelectNetworkType());
                    mSetupCountryView.show();
                    return true;
                } else if (mScanProgressView.isShow()) {
                    mScanProgressView.hide();
                    finish();
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void sendMessage(int messageID, Object obj) {
        LogTool.d(LogTool.MSCAN, "sendMessage=messageID:" + messageID);
        Message msg = new Message();
        msg.arg1 = messageID;
        msg.obj = obj;
        mMessageHandler.sendMessage(msg);
    }

    @Override
    public void setCrtSelectedNetwor(Network network) {
    }

    @Override
    public Network getCrtSelectedNetwork() {
        return null;
    }

    /**
     * Dismiss player window tip view while enter Install window.<br>
     */
    private void dismissPlayerTipView() {
        LogTool.d(LogTool.MSCAN, "dismissPlayerTipView()");
        Intent dissmissIntent = new Intent(CommonValue.DTV_INTENT_DISMISS_TIP);
        //this.sendBroadcast(dissmissIntent);
        CommonDef.sendBroadcastEx(AutoScanActivity.this, dissmissIntent);
    }

    // ///////////////////////////////////
    private static class SubWndMsgHandler extends Handler {
        WeakReference<AutoScanActivity> mActivity;

        SubWndMsgHandler(AutoScanActivity parent) {
            mActivity = new WeakReference<AutoScanActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            LogTool.d(LogTool.MSCAN, "handleMessage=msg==" + msg.arg1);
            switch (msg.arg1) {
                case MSG_ID_READY_TO_SCAN:
                    if (null != mActivity) {
                        AutoScanActivity parent = mActivity.get();
                        if (null != parent) {
                            parent.startScan();
                        }
                    }
                    break;
                case MSG_ID_NEXT_STEP:

                    if (null != mActivity) {
                        AutoScanActivity parent = mActivity.get();
                        if (null != parent) {
                            parent.showNextView();
                        }
                    }
                    break;
                case MSG_ID_STOP_SCAN:

                    if (null != mActivity) {
                        AutoScanActivity parent = mActivity.get();
                        if (null != parent) {
                            parent.showQuaryExit();
                        }
                    }
                    break;
                default:
                    break;
            }

        }
    }
}
