package com.hisilicon.tvui.installtion;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.installtion.IScanSubWnd.KeyDoResult;
import com.hisilicon.tvui.play.ChannelHistory;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.Combox;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DVBSInstallActivity extends BaseActivity implements OnItemSelectedListener, OnClickListener,
        IScanMainWnd, OnKeyListener, OnItemClickListener {
    private static final String TAG = "DVBSInstallActivity";

    private static final int KEY_MAX_SPACE = 5;
    private static final int INVALIDATE_SATELLITE_ID = -1;

    private static final int SUB_FRAGMENT_ID_ANTENNA = 0;
    private static final int SUB_FRAGMENT_ID_SATELLITELIST = 1;
    private static final int SUB_FRAGMENT_ID_TPLIST = 2;
    private static final int SUB_FRAGMENT_ID_MOTOR = 3;
    private static final int SUB_FRAGMENT_ID_LIMIT = 4;

    private int mCrtSatelliteID = INVALIDATE_SATELLITE_ID;
    private ListView mWndNameList;
    private final Map<String, Object> mMenuActivity = new LinkedHashMap<String, Object>();
    private IScanSubWnd mCurrentIScanSubWnd = null;
    private final SubWndMsgHandler myMessageHandler = new SubWndMsgHandler(this);

    private Dialog mDlgScan = null;
    private Combox mCbxFta = null;
    private Combox mCbxChannel = null;
    private Combox mCbxNetWork = null;
    private Combox mCbxMode = null;
    private Button mDlgBtnScan = null;
    private Button mDlgBtnCancel = null;
    private long mPreKeyTime = 0;
    private Dialog mCheckPassWordDialog = null;
    private CheckPassWordDialog.Builder mCheckPWBuilder = null;

    private int mSelectIndex = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        Window installWindow = this.getWindow();
        if (null == installWindow) {
            LogTool.v(LogTool.MSCAN, "the install window is null");
            return;
        }

        installWindow.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install_menu);

        mWndNameList = (ListView) this.findViewById(R.id.id_scanmenu_wndlist);
        List<String> list = new ArrayList<String>();
        final CharSequence[] items = this.getResources().getStringArray(R.array.array_install_s_menu);

        for (int i = 0; i < items.length; i++) {
            list.add(items[i].toString());
            mMenuActivity.put(items[i].toString(), DVBSInstallActivity.class);
        }
        ArrayAdapter<String> subWndNameAdapter = new ArrayAdapter<String>(this, R.layout.install_dvbs_menu_item,
                R.id.id_dvbs_menu_name, list);
        mWndNameList.setAdapter(subWndNameAdapter);
        mWndNameList.setOnItemSelectedListener(this);
        mWndNameList.setOnItemClickListener(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        this.selectFragmentChange(arg0, null, mSelectIndex, 0);
    }

    public void setCurrentIScanSubWnd(IScanSubWnd subWnd) {
        mCurrentIScanSubWnd = subWnd;
    }

    public boolean isFocused() {
        return mWndNameList.isFocused();
    }

    private void selectFragmentChange(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (arg2 == mSelectIndex) {
            return;
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mCurrentIScanSubWnd = null;
        Fragment fragment = null;

        mSelectIndex = arg2;
        switch (arg2) {
            case SUB_FRAGMENT_ID_ANTENNA: {
                fragment = new AntennaFragment();
                break;
            }
            case SUB_FRAGMENT_ID_SATELLITELIST: {
                fragment = new SatelliteListFragment();
                break;
            }
            case SUB_FRAGMENT_ID_TPLIST: {
                fragment = new TPListFragment();
                break;
            }
            case SUB_FRAGMENT_ID_MOTOR: {

                fragment = new MotorFragment();
                break;
            }
            case SUB_FRAGMENT_ID_LIMIT: {
                fragment = new MotorLimitFragment();
                break;
            }
            default: {
                fragment = new MotorLimitFragment();
                break;
            }
        }
        mCurrentIScanSubWnd = (IScanSubWnd) fragment;
        mCurrentIScanSubWnd.setMainWnd(this);
        fragmentTransaction.replace(R.id.id_scanmenu_frame, fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        selectFragmentChange(arg0, arg1, arg2, arg3);
    }

    @Override
    public void onDestroy() {
        LogTool.d(LogTool.MSCAN, "=====onDestroy======");
        mCheckPassWordDialog = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        LogTool.d(LogTool.MSCAN, "=====onPause======");
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Runnable runnable = () -> {
            LogTool.d(LogTool.MPLAY, "mPlayer.releaseResource(0)");
            mPlayer.releaseResource(0);
        };
        TaskUtil.post(runnable);
        dismissPlayerTipView();

        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        DTVConfig config = dtv.getConfig();

        boolean bNeedPassword = CommonValue.MENU_LOCK_OPEN == config.getInt(CommonValue.MENU_LOCK,
                CommonValue.MENU_LOCK_OPEN);

        if ((bNeedPassword) && (!super.isFinishing())) {
            if (null == mCheckPassWordDialog) {
                if (mCheckPWBuilder == null) {
                    mCheckPWBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
                    mCheckPWBuilder.setOnKeyListener(this);
                }
                mCheckPassWordDialog = mCheckPWBuilder.create();
                mCheckPWBuilder.setPasswordTitle(getString(R.string.play_password_menu_lock));
                mCheckPassWordDialog.setCanceledOnTouchOutside(false);

                mCheckPassWordDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if ((mCheckPWBuilder != null) && mCheckPWBuilder.getPasswordisRight()) {
                            LogTool.v(LogTool.MSCAN, "=====mCheckPassWordDialog right======");
                        } else {
                            finish();
                        }
                    }
                });
            }
            if ((!mCheckPassWordDialog.isShowing()) && (!mCheckPWBuilder.getPasswordisRight())) {
                mCheckPassWordDialog.show();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (SystemClock.elapsedRealtime() - mPreKeyTime < KEY_MAX_SPACE) {
            LogTool.v(LogTool.MSCAN, "=====min 50======" + SystemClock.currentThreadTimeMillis());
            return true;
        }
        mPreKeyTime = SystemClock.elapsedRealtime();
        if (null == mCurrentIScanSubWnd) {
            return super.onKeyDown(keyCode, event);
        }
        KeyDoResult ret = mCurrentIScanSubWnd.keyDispatch(keyCode, event, null);
        if (ret == KeyDoResult.DO_OVER) {
            return true;
        } else if (ret == KeyDoResult.DO_DONE_NEED_SYSTEM) {
            return super.onKeyDown(keyCode, event);
        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT: {
                if (!mWndNameList.isFocused()) {
                    mWndNameList.setSelection(mSelectIndex);
                    mWndNameList.requestFocus();
                    mWndNameList.requestFocusFromTouch();
                }
                return true;
            }
            case KeyValue.DTV_KEYVALUE_YELLOW: {
                showScanDlg();
                return true;
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showScanDlg() {
        if (!mCurrentIScanSubWnd.isCanStartScan()) {
            return;
        }
        if (null != mDlgScan) {
            mDlgScan.show();
            mCbxMode.setEnabled(mCurrentIScanSubWnd.isNetworkScan());
            mCbxMode.setFocusable(mCurrentIScanSubWnd.isNetworkScan());
            return;
        }
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.install_dvbs_searchif,
                (ViewGroup) this.findViewById(R.id.id_install_dvbs_scanif_dlg));
        Context mContext = inflater.getContext();

        LogTool.e(LogTool.MSCAN, " ++++ showScanDlg ++++");

        mCbxFta = (Combox) layout.findViewById(R.id.id_scan_if_fta);
        LinkedHashMap<String, Object> mapFta = new LinkedHashMap<String, Object>();
        mapFta.put(getString(R.string.str_install_all), EnScrambleFilter.ALL);
        mapFta.put(getString(R.string.str_install_fta), EnScrambleFilter.FTA);
        mapFta.put(getString(R.string.str_install_scramble), EnScrambleFilter.SCRAMBLE);
        mCbxFta.setData(mapFta);

        mCbxChannel = (Combox) layout.findViewById(R.id.id_scan_if_channel);
        LinkedHashMap<String, Object> mapChannel = new LinkedHashMap<String, Object>();
        mapChannel.put(getString(R.string.str_install_all), EnTVRadioFilter.ALL);
        mapChannel.put(getString(R.string.str_install_tv), EnTVRadioFilter.TV);
        mapChannel.put(getString(R.string.str_install_radio), EnTVRadioFilter.RADIO);
        mCbxChannel.setData(mapChannel);

        mCbxNetWork = (Combox) layout.findViewById(R.id.id_scan_if_network);
        LinkedHashMap<String, Object> mapNetwork = new LinkedHashMap<String, Object>();
        mapNetwork.put(getString(R.string.str_no), false);
        mapNetwork.put(getString(R.string.str_yes), true);
        mCbxNetWork.setData(mapNetwork);

        mCbxMode = (Combox) layout.findViewById(R.id.id_scan_if_mode);
        LinkedHashMap<String, Object> mapBlind = new LinkedHashMap<String, Object>();
        mapBlind.put(getString(R.string.str_install_blind), ScanType.EnBaseScanType.BLIND);
        mapBlind.put(getString(R.string.str_install_auto_full), ScanType.EnBaseScanType.AUTO_FULL);
        mCbxMode.setData(mapBlind);

        mDlgBtnScan = (Button) layout.findViewById(R.id.id_install_scanif_start);
        mDlgBtnScan.setOnClickListener(this);

        mDlgBtnCancel = (Button) layout.findViewById(R.id.id_install_scanif_cancel);
        mDlgBtnCancel.setOnClickListener(this);

        mCbxMode.setEnabled(mCurrentIScanSubWnd.isNetworkScan());
        mCbxMode.setFocusable(mCurrentIScanSubWnd.isNetworkScan());

        mDlgScan = new Dialog(mContext, R.style.DIM_STYLE);
        mDlgScan.setCanceledOnTouchOutside(false);
        mDlgScan.show();
        mDlgScan.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setCrtSelectedNetwor(Network network) {
        mCrtSatelliteID = INVALIDATE_SATELLITE_ID;
        if ((null != network) && (network.isSelected())) {
            mCrtSatelliteID = network.getID();
        }
    }

    @Override
    public Network getCrtSelectedNetwork() {
        DVBSNetwork retSatellite = null;
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        if (INVALIDATE_SATELLITE_ID == mCrtSatelliteID) {
            Channel chl = ChannelHistory.getInstance().getCurrentChn(halApi.EnumSourceIndex.SOURCE_DVBS);
            if (null != chl) {
                Network network = chl.getBelongNetwork();
                if ((null != network) && (EnNetworkType.SATELLITE == network.getNetworkType())) {
                    DVBSNetwork satellite = (DVBSNetwork) network;
                    if (satellite.isSelected()) {
                        retSatellite = (DVBSNetwork) network;
                        mCrtSatelliteID = retSatellite.getID();
                    }
                }
            }
        } else {
            retSatellite = (DVBSNetwork) dtv.getNetworkManager().getNetworkById(mCrtSatelliteID);
            if ((null != retSatellite) && (!retSatellite.isSelected())) {
                return null;
            }
        }
        return retSatellite;
    }

    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
            case R.id.id_install_scanif_start: {
                mDlgScan.cancel();
                ScanType type = new ScanType();
                if (mCurrentIScanSubWnd.isNetworkScan()) {
                    type.setBaseType((ScanType.EnBaseScanType) mCbxMode.getTag());
                } else {
                    type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
                }

                type.enableNit((Boolean) mCbxNetWork.getTag());
                type.setFTAFilter((EnScrambleFilter) mCbxFta.getTag());
                type.setTVRadioFilter((EnTVRadioFilter) mCbxChannel.getTag());
                ((DTVApplication) getApplication()).setScanType(EnNetworkType.SATELLITE, type);
                CommonDef.startActivityEx(DVBSInstallActivity.this,
                        new Intent(this, ScanProgressActivity.class));
                this.finish();
                break;
            }
            case R.id.id_install_scanif_cancel: {
                mDlgScan.cancel();
                break;
            }
            default:
                break;
        }
    }

    @Override
    public void sendMessage(int messageID, Object obj) {
        LogTool.v(LogTool.MSCAN, " ==== sendMessage=messageID=" + messageID);
        Message msg = new Message();
        msg.arg1 = messageID;
        msg.obj = obj;
        myMessageHandler.sendMessage(msg);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if ((KeyEvent.KEYCODE_BACK == keyCode) && (null != mCheckPassWordDialog)) {
            mCheckPassWordDialog.cancel();
            this.finish();
            return true;
        }
        return false;
    }

    /**
     * Dismiss player window tip view while enter Install window.<br>
     */
    private void dismissPlayerTipView() {
        LogTool.d(LogTool.MSCAN, "dismissPlayerTipView()");
        Intent dissmissIntent = new Intent(CommonValue.DTV_INTENT_DISMISS_TIP);
        CommonDef.sendBroadcastEx(DVBSInstallActivity.this, dissmissIntent);
    }

    private static class SubWndMsgHandler extends Handler {
        WeakReference<DVBSInstallActivity> mActivity;

        SubWndMsgHandler(DVBSInstallActivity parent) {
            mActivity = new WeakReference<DVBSInstallActivity>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            LogTool.d(LogTool.MSCAN, " ==== handleMessage=msg=" + msg.arg1);
            if ((MSG_ID_READY_TO_SCAN == msg.arg1) && (null != mActivity)) {
                DVBSInstallActivity parent = mActivity.get();
                if (null != parent) {
                    parent.showScanDlg();
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        selectFragmentChange(arg0, arg1, arg2, arg3);
    }
}
