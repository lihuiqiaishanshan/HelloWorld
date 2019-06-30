package com.hisilicon.tvui.installtion;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;

import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.installtion.IScanSubWnd.KeyDoResult;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.CheckPassWordDialog.CheckPassWordDialogInterface;


public class DtvManualScanActivity extends BaseActivity implements IScanMainWnd, OnKeyListener {
    private static final String TAG = "DtvManualScanActivity";

    private DVBTManualScanView mDvbtManualScanView = null;
    private DVBCManualScanView mDvbcManualScanView = null;
    private ATSCManualScanView mAtscManualScanView = null;
    private ScanProgressView mScanProgressView = null;

    private Dialog mCheckPassWordDialog = null;
    private final SubWndMsgHandler mMessageHandler = new SubWndMsgHandler(this);
    private CheckPassWordDialog.Builder mCheckPWBuilder = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogTool.i(LogTool.MCHANNEL, "===== onCreate =====");
        setContentView(R.layout.install_dtv_manual_scan_activity);

        initView();
    }

    private void initView() {
        mDvbtManualScanView = new DVBTManualScanView(this);
        mDvbcManualScanView = new DVBCManualScanView(this);
        mAtscManualScanView = new ATSCManualScanView(this);
        mScanProgressView = new ScanProgressView(this);
    }


    // Start search program
    private void startScan() {
        // Add these judgments are because the monkey operating too fast, click on the search button
        // several times, abnormal problems caused by
        LogTool.d(LogTool.MINSTALL, "start DTV Manual Scan enter");
        List<EnNetworkType> lstNetworkType = ((DTVApplication) this.getApplication()).getScanNetworkType();
        if (null == lstNetworkType || lstNetworkType.size() <= 0) {
            return;
        }

        if (null == ((DTVApplication) this.getApplication()).getScanParamNetwork()) {
            return;
        }

        LogTool.d(LogTool.MINSTALL, "start DTV Manual Scan...");
        mDvbcManualScanView.hide();
        mDvbtManualScanView.hide();
        mAtscManualScanView.hide();
        mScanProgressView.show();
        mPlayer.releaseResource(0);
    }

    private void showQuaryExit() {
        mScanProgressView.queryExist();
    }

    private void ShowNextView() {
        if (mDvbtManualScanView.isShow()) {
            mDvbtManualScanView.hide();
            mScanProgressView.show();
        } else if (mDvbcManualScanView.isShow()) {
            mDvbcManualScanView.hide();
            mScanProgressView.show();
        } else if (mAtscManualScanView.isShow()) {
            mAtscManualScanView.hide();
            mScanProgressView.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        dismissPlayerTipView();

        boolean bNeedPassword = CommonValue.MENU_LOCK_OPEN == mDtvConfig.getInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
        // If the password lock, then display the password input box
        if ((bNeedPassword) && (!super.isFinishing())) {
            if (null == mCheckPassWordDialog) {
                if (mCheckPWBuilder == null) {
                    mCheckPWBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
                    mCheckPWBuilder.setOnKeyListener(this);
                }
                mCheckPassWordDialog = mCheckPWBuilder.create();
                mCheckPWBuilder.setPasswordTitle(getString(R.string.play_password_menu_lock));
                mCheckPassWordDialog.setCanceledOnTouchOutside(false);
                mCheckPWBuilder.setCheckPassWordsListener(new CheckPassWordDialogInterface() {

                    @Override
                    public void onCheck(int which, String passWord) {
                        switch (which) {
                            case CheckPassWordDialogInterface.PASSWORD_RIGHT:
                                showManualScanView();
                                break;
                        }
                    }
                });
                mCheckPassWordDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if ((mCheckPWBuilder != null) && mCheckPWBuilder.getPasswordisRight()) {
                            showManualScanView();
                            LogTool.v(LogTool.MSCAN, "mCheckPassWordDialog right");
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
            showManualScanView();
        }

        // Clear scan param
        ((DTVApplication) this.getApplication()).setScanType(null, null);
        ((DTVApplication) this.getApplication()).setScanNetworkType(null);
        ((DTVApplication) this.getApplication()).setScanParam(null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LogTool.i(LogTool.MCHANNEL, "===== onPause =====");
        int curSource = halApi.getCurSourceID();
        mDvbcManualScanView.hide();
        mDvbtManualScanView.hide();
        mAtscManualScanView.hide();
        mScanProgressView.hide();
        if (halApi.isDTVSource(curSource)) {
            mDTV.prepareDTV();
            mPlayer.resumeResource();
            //DTV 重新播放后，需要重新配置无信号蓝屏
            halApi.setBlueScreen();
        }
        mPlayer.changeChannel(mChnHistory.getCurrentChn(curSource));
        finish();
    }

    @Override
    public void onDestroy() {
        LogTool.i(LogTool.MCHANNEL, "===== onDestroy =====");
        mCheckPassWordDialog = null;
        super.onDestroy();
    }

    @Override
    public boolean onKeyUp(int keyCode, android.view.KeyEvent event) {
        LogTool.d(LogTool.MINSTALL, "DTV Manual Scan onKeyUp . KeyCode = " + keyCode);
        if (mDvbcManualScanView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mDvbcManualScanView keyDispatch . ");
            KeyDoResult ret = mDvbcManualScanView.keyDispatch(keyCode, event, null);
        }

        if (mDvbtManualScanView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mDvbtManualScanView keyDispatch ");
            KeyDoResult ret = mDvbtManualScanView.keyDispatch(keyCode, event, null);
        }

        if (mAtscManualScanView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mAtscManualScanView keyDispatch ");
            KeyDoResult ret = mAtscManualScanView.keyDispatch(keyCode, event, null);
        }

        if (mScanProgressView.isShow()) {
            LogTool.d(LogTool.MPLAY, "  mScanProgressView keyDispatch . ");
            KeyDoResult ret = mScanProgressView.keyDispatch(keyCode, event, null);
            return true;
        }

        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_BACK: {
                mDvbcManualScanView.hide();
                mDvbtManualScanView.hide();
                mAtscManualScanView.hide();
                mScanProgressView.hide();
                finish();
                return true;
            }
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public boolean onKey(View arg0, int keyCode, KeyEvent arg2) {
        if ((KeyValue.DTV_KEYVALUE_BACK == keyCode) && (null != mCheckPassWordDialog)) {
            mCheckPassWordDialog.cancel();
            this.finish();
            return true;
        }
        return false;
    }

    @Override
    public void sendMessage(int messageID, Object obj) {
        LogTool.d(LogTool.MSCAN, " sendMessage=messageID=" + messageID);
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
        CommonDef.sendBroadcastEx(DtvManualScanActivity.this, dissmissIntent);
    }

    // ///////////////////////////////////
    private static class SubWndMsgHandler extends Handler {
        WeakReference<DtvManualScanActivity> mActivity;

        SubWndMsgHandler(DtvManualScanActivity parent) {
            mActivity = new WeakReference<DtvManualScanActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            LogTool.d(LogTool.MSCAN, " handleMessage=msg=" + msg.arg1);
            switch (msg.arg1) {
                case MSG_ID_READY_TO_SCAN:
                    if (null != mActivity) {
                        DtvManualScanActivity parent = mActivity.get();
                        if (null != parent) {
                            parent.startScan();
                        }
                    }
                    break;
                case MSG_ID_NEXT_STEP:

                    if (null != mActivity) {
                        DtvManualScanActivity parent = mActivity.get();
                        if (null != parent) {
                            parent.ShowNextView();
                        }
                    }
                    break;
                case MSG_ID_STOP_SCAN:

                    if (null != mActivity) {
                        DtvManualScanActivity parent = mActivity.get();
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

    private void showManualScanView() {
        EnNetworkType networkType = mNetworkManager.getCurrentNetworkType();
        LogTool.i(LogTool.MCHANNEL, "===== networkType =====" + networkType);
        if (EnNetworkType.CABLE == networkType) {
            mDvbcManualScanView.show();
        } else if (EnNetworkType.TERRESTRIAL == networkType
                || EnNetworkType.DTMB == networkType
                || EnNetworkType.ISDB_TER == networkType) {
            mDvbtManualScanView.show();
        } else if (EnNetworkType.ATSC_CAB == networkType
                || EnNetworkType.ATSC_T == networkType) {
            mAtscManualScanView.show();
        }
    }
}
