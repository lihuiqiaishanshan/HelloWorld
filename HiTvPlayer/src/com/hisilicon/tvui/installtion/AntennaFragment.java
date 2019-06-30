package com.hisilicon.tvui.installtion;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.hardware.Antenna;
import com.hisilicon.dtv.hardware.Antenna.EnLNBPowerSwitch;
import com.hisilicon.dtv.hardware.Antenna.EnSwitchType;
import com.hisilicon.dtv.hardware.LNBData;
import com.hisilicon.dtv.hardware.LNBData.EnLNBBand;
import com.hisilicon.dtv.hardware.LNBData.EnLNBType;
import com.hisilicon.dtv.hardware.Motor;
import com.hisilicon.dtv.hardware.Motor.EnMotorType;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.DVBSTransponder;
import com.hisilicon.dtv.network.DVBSTransponder.EnPolarity;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.NetworkManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

/**
 * Antenna settings Fragment.<br>
 *
 * @author HiSilicon DTV stack software group
 * @see IScanMainWnd
 */
public class AntennaFragment extends Fragment implements IScanSubWnd, OnComboxSelectChangeListener, OnClickListener {
    // TP data unit conversion ratio
    private static final int TP_UNIT_RATE = 1000;

    // LNB data definition
    private final LNBData[] LNB_DATA = {
            new LNBData(LNBData.EnLNBBand.LNB_BAND_C, LNBData.EnLNBType.LNB_SINGLE_LO, 5150, 5150),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_C, LNBData.EnLNBType.LNB_SINGLE_LO, 5750, 5750),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_C, LNBData.EnLNBType.LNB_DUAL_LO, 5150, 5750),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 9750, 9750),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 10000, 10000),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 10250, 10250),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 10600, 10600),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 10678, 10678),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 10750, 10750),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 11250, 11250),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, EnLNBType.LNB_SINGLE_LO, 11300, 11300),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, LNBData.EnLNBType.LNB_DUAL_LO, 9750, 10600),
            new LNBData(LNBData.EnLNBBand.LNB_BAND_KU, LNBData.EnLNBType.LNB_DUAL_LO, 9750, 10750)};

    private boolean mIsChange = false;
    private IScanMainWnd mScanMainWnd = null;
    private Context mContext = null;

    private Button mBtnScan = null;
    private RefreshQS mRefreshQS = null;
    private Combox mCbxSatellite = null;
    private Combox mCbxTp = null;
    private Combox mCbxLnbType = null;
    private Combox mCbxLnbPwr = null;
    private Combox mCbx22k = null;
    private Combox mCbxDiSEqCType = null;
    private Combox mCbxDiSEqC10 = null;
    private Combox mCbxDiSEqC11 = null;
    private Combox mCbxMotorType = null;

    private NetworkManager mNetworkManager = null;
    private DVBSNetwork mCurrentSettingNetwork = null;
    private DVBSNetwork mLastSettingNetwork = null;
    private Antenna mAntenna = null;

    private ProgressDialog mTunerCntPrgDlg = null;
    private TunerCntHandler mCntHandler = null;
    private Multiplex mCrtMultiplex = null;
    private boolean mIsExist = false;

    private boolean mbResumeLockTag = true;
    private Handler mLockTunerHandler = new Handler();
    private boolean mbLockTunerRun = false;
    private Runnable mLockTunerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mCurrentSettingNetwork != null) {
                    LinkedHashMap<String, Object> mapTPs = new LinkedHashMap<String, Object>();
                    List<Multiplex> lstTPs = mCurrentSettingNetwork.getMultiplexes();
                    // If the TP has data, start frequency.<br>
                    if (!lstTPs.isEmpty()) {
                        LogTool.d(LogTool.MSCAN, " ==== 1s  to start lock tuner ===");
                        connectMultiplex(lstTPs.get(0));
                    } else {
                        connectMultiplex(null);
                    }
                }
                mbLockTunerRun = true;
            } catch (Exception e) {
                LogTool.e(LogTool.MSCAN, " ==== time to start lock tuner === e=" + e);
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.install_dvbs_antennasetting, container, false);
        mContext = inflater.getContext();

        mBtnScan = (Button) v.findViewById(R.id.id_antennasetting_search);
        mBtnScan.setOnClickListener(this);
        mRefreshQS = new RefreshQS(v);
        mCbxSatellite = (Combox) v.findViewById(R.id.id_antennaseting_satellite);
        mCbxTp = (Combox) v.findViewById(R.id.id_antennaseting_tp);
        mCbxLnbType = (Combox) v.findViewById(R.id.id_antennaseting_lnbtype);
        mCbxLnbPwr = (Combox) v.findViewById(R.id.id_antennaseting_lnbpwr);
        mCbx22k = (Combox) v.findViewById(R.id.id_antennaseting_22K);
        mCbxDiSEqCType = (Combox) v.findViewById(R.id.id_antennaseting_dsqctype);
        mCbxDiSEqC10 = (Combox) v.findViewById(R.id.id_antennaseting_dieqc10);
        mCbxDiSEqC11 = (Combox) v.findViewById(R.id.id_antennaseting_dieqc11);
        mCbxMotorType = (Combox) v.findViewById(R.id.id_antennaseting_motortype);

        mCbxSatellite.setOnSelectChangeListener(this);
        mCbxTp.setOnSelectChangeListener(this);
        mCbxLnbType.setOnSelectChangeListener(this);
        mCbxLnbPwr.setOnSelectChangeListener(this);
        mCbx22k.setOnSelectChangeListener(this);
        mCbxDiSEqCType.setOnSelectChangeListener(this);
        mCbxDiSEqC10.setOnSelectChangeListener(this);
        mCbxDiSEqC11.setOnSelectChangeListener(this);
        mCbxMotorType.setOnSelectChangeListener(this);

        initCtrlData();
        ((DVBSInstallActivity) getActivity()).setCurrentIScanSubWnd(this);
        mScanMainWnd = (IScanMainWnd) getActivity();

        mCntHandler = new TunerCntHandler(this);
        return v;
    }

    private void initCtrlData() {
        // List of LNB data
        LinkedHashMap<String, Object> mapLnbType = new LinkedHashMap<String, Object>();
        for (LNBData lnb : LNB_DATA) {
            mapLnbType.put(lnb.toString(), lnb);
        }
        mCbxLnbType.setData(mapLnbType);

        LinkedHashMap<String, Object> mapLnbPwr = new LinkedHashMap<String, Object>();
        mapLnbPwr.put(mContext.getString(R.string.str_off), Antenna.EnLNBPowerSwitch.OFF);
        mapLnbPwr.put(mContext.getString(R.string.str_on), Antenna.EnLNBPowerSwitch.ON);
        mCbxLnbPwr.setData(mapLnbPwr);

        LinkedHashMap<String, Object> map22k = new LinkedHashMap<String, Object>();
        map22k.put(mContext.getString(R.string.str_none), Antenna.EnSwitchType.NONE);
        map22k.put(mContext.getString(R.string.str_off), Antenna.EnSwitchType.OFF);
        map22k.put(mContext.getString(R.string.str_on), Antenna.EnSwitchType.ON);
        mCbx22k.setData(map22k);

        LinkedHashMap<String, Object> mapDiscType = new LinkedHashMap<String, Object>();
        mapDiscType.put(mContext.getString(R.string.str_none), Antenna.EnDiSEqCType.NONE);
        mapDiscType.put(mContext.getString(R.string.str_install_diseqc10), Antenna.EnDiSEqCType.DISEQC10);
        mapDiscType.put(mContext.getString(R.string.str_install_diseqc11), Antenna.EnDiSEqCType.DISEQC11);
        mapDiscType.put(mContext.getString(R.string.str_install_diseqc1110), Antenna.EnDiSEqCType.DISEQC1110);
        mCbxDiSEqCType.setData(mapDiscType);

        LinkedHashMap<String, Object> mapDisPort10 = new LinkedHashMap<String, Object>();
        mapDisPort10.put(mContext.getString(R.string.str_none), Antenna.EnDiSEqC10Port.NONE);
        mapDisPort10.put(mContext.getString(R.string.str_install_port_a), Antenna.EnDiSEqC10Port.PORT_1);
        mapDisPort10.put(mContext.getString(R.string.str_install_port_b), Antenna.EnDiSEqC10Port.PORT_2);
        mapDisPort10.put(mContext.getString(R.string.str_install_port_c), Antenna.EnDiSEqC10Port.PORT_3);
        mapDisPort10.put(mContext.getString(R.string.str_install_port_d), Antenna.EnDiSEqC10Port.PORT_4);
        mCbxDiSEqC10.setData(mapDisPort10);

        // Port 16, and the one value is invalid, is total of 17
        LinkedHashMap<String, Object> mapDisPort11 = new LinkedHashMap<String, Object>();
        mapDisPort11.put(mContext.getString(R.string.str_none), Antenna.EnDiSEqC11Port.valueOf(0));
        for (int i = 1; i < 17; i++) {
            mapDisPort11.put("" + i, Antenna.EnDiSEqC11Port.valueOf(i));
        }
        mCbxDiSEqC11.setData(mapDisPort11);

        LinkedHashMap<String, Object> mapMotorType = new LinkedHashMap<String, Object>();
        mapMotorType.put(mContext.getString(R.string.str_none), Motor.EnMotorType.NONE);
        mapMotorType.put(mContext.getString(R.string.str_install_diseqc12), Motor.EnMotorType.DISEQC12);
        mapMotorType.put(mContext.getString(R.string.str_install_usals), Motor.EnMotorType.USALS);
        mCbxMotorType.setData(mapMotorType);
        mCbxMotorType.setEnabled(false);
        mCbxMotorType.setFocusable(false);

    }

    @Override
    public void onResume() {
        super.onResume();
        mIsExist = false;
        mbLockTunerRun = false;
        mbResumeLockTag = true;
        refreshSatellite();
        if (mAntenna != null && mAntenna.getMotorType() != EnMotorType.NONE) {
            mLockTunerHandler.postDelayed(mLockTunerRunnable, 1000); // delay 1s to run
        } else {
            mLockTunerHandler.postDelayed(mLockTunerRunnable, 100); // delay 100ms to run
        }
        mRefreshQS.open();
    }

    @Override
    public void onPause() {
        mIsExist = true;
        mCntHandler.removeMessages(RefreshQS.MESSAGE_CONNECT_SHOW_DIALOG);
        mCntHandler.removeMessages(RefreshQS.MESSAGE_CONNECT_CLOSE_DIALOG);
        if (!mbLockTunerRun) {
            mLockTunerHandler.removeCallbacks(mLockTunerRunnable); //delete delay lock funtion
        }
        mRefreshQS.close();
        if (null != mCurrentSettingNetwork) {
            LogTool.d(LogTool.MSCAN, " ==== onPause === mIsChange=" + mIsChange);
            if (mIsChange) {
                Runnable runnable = () -> {
                    LogTool.d(LogTool.MSCAN, "puai onPause === EnDiSEqCType=" +
                            mCurrentSettingNetwork.getAntenna().getDiSEqCType().ordinal());
                    mCurrentSettingNetwork.getAntenna().save();
                    mIsChange = false;
                };
                TaskUtil.post(runnable);
            }
        }
        showConnectDialog(false);
        super.onPause();
    }

    private void refreshSatellite() {
        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mNetworkManager = dtv.getNetworkManager();

        // Refresh the list of satellite
        List<Network> lstNetwork = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);
        LinkedHashMap<String, Object> lstSatellite = new LinkedHashMap<>();
        for (int i = 0; i < lstNetwork.size(); i++) {
            DVBSNetwork satellite = (DVBSNetwork) lstNetwork.get(i);
            if (satellite.isSelected()) {
                lstSatellite.put(satellite.getName(), satellite);
            }
        }
        mCbxSatellite.setData(lstSatellite);

        // Set the current being used in satellite
        DVBSNetwork network = (DVBSNetwork) mScanMainWnd.getCrtSelectedNetwork();
        if (null == network) {
            // If access to the current use of the satellite is empty, the default list first
            network = (DVBSNetwork) mCbxSatellite.getTag();
        } else {
            LogTool.d(LogTool.MSCAN, " ====  satellite name =" + network.getName());
            mCbxSatellite.setText(network.getName());
        }
        refreshAntennaConfigData(network);
    }

    private void refreshAntennaConfigData(DVBSNetwork satellite) {
        if ((null == satellite)
                || ((null != mCurrentSettingNetwork) && (mCurrentSettingNetwork.getID() == satellite.getID()))) {
            return;
        }
        mCurrentSettingNetwork = satellite;
        mAntenna = satellite.getAntenna();
        mScanMainWnd.setCrtSelectedNetwor(mCurrentSettingNetwork);

        LNBData lnbData = mAntenna.getLNBData();
        mCbxLnbType.setText(lnbData.toString());

        boolean canClick = (EnLNBBand.LNB_BAND_KU != lnbData.getLNBBAND())
                || ((EnLNBBand.LNB_BAND_KU == lnbData.getLNBBAND()) && (lnbData.getHighLO() == lnbData.getLowLO()));
        mCbx22k.setEnabled(canClick);
        mCbx22k.setFocusable(canClick);
        mCbxLnbPwr.setTag(mAntenna.getLNBPower());
        mCbx22k.setTag(mAntenna.get22KSwitch());

        mCbxDiSEqCType.setTag(mAntenna.getDiSEqCType());
        changeDiSEqCType(mAntenna.getDiSEqCType());
        mCbxDiSEqC10.setTag(mAntenna.getDiSEqC10Port());
        mCbxDiSEqC11.setTag(mAntenna.getDiSEqC11Port());
        mCbxMotorType.setTag(mAntenna.getMotorType());

        refreshTPData(mCurrentSettingNetwork);
    }

    private void changeDiSEqCType(Antenna.EnDiSEqCType type) {
        if (null == type) {
            LogTool.e(LogTool.MSCAN, "==== changeDiSEqCType  error === type= null");
            return;
        }
        LogTool.d(LogTool.MSCAN, "==== changeDiSEqCType === type=" + type);

        boolean diseqc10 = ((Antenna.EnDiSEqCType.DISEQC10 == type) || (Antenna.EnDiSEqCType.DISEQC1110 == type));
        mCbxDiSEqC10.setEnabled(diseqc10);
        mCbxDiSEqC10.setFocusable(diseqc10);
        // If not support version 1.0,then set the value is NONE.<br>
        if (!diseqc10) {
            mAntenna.setDiSEqC10Port(Antenna.EnDiSEqC10Port.NONE);
        }

        boolean diseqc11 = ((Antenna.EnDiSEqCType.DISEQC11 == type) || (Antenna.EnDiSEqCType.DISEQC1110 == type));
        mCbxDiSEqC11.setEnabled(diseqc11);
        mCbxDiSEqC11.setFocusable(diseqc11);
        if (!diseqc11) {
            mAntenna.setDiSEqC11Port(Antenna.EnDiSEqC11Port.NONE);
        }

    }

    private void refreshTPData(DVBSNetwork satellite) {
        if (null != satellite) {
            LinkedHashMap<String, Object> mapTPs = new LinkedHashMap<String, Object>();
            List<Multiplex> lstTPs = satellite.getMultiplexes();
            for (Multiplex multiplex : lstTPs) {
                DVBSTransponder tp = (DVBSTransponder) multiplex;
                mapTPs.put(String.format(Locale.getDefault(), "%d/%d/%1s", tp.getFrequency() / TP_UNIT_RATE,
                        tp.getSymbolRate() / TP_UNIT_RATE, tp.getPolarity() == EnPolarity.VERTICAL ? "V" : "H"), tp);
            }
            mCbxTp.setData(mapTPs);

            // If the TP has data, start frequency.
            if (!mbResumeLockTag) {
                if (!lstTPs.isEmpty()) {
                    connectMultiplex(lstTPs.get(0));
                } else {
                    connectMultiplex(null);
                }
            }
        } else {
            mCbxTp.setData(null);
        }
    }

    /**
     * Show or hide the locking prompt dialog box.<br>
     */
    private void showConnectDialog(boolean bShow) {
        LogTool.v(LogTool.MSCAN, "showConnectDialog  " + bShow);
        if (null == mTunerCntPrgDlg) {
            mTunerCntPrgDlg = new ProgressDialog(mContext);
            mTunerCntPrgDlg.setTitle(getString(R.string.str_install_locking));
            mTunerCntPrgDlg.setMessage(getString(R.string.str_please_wait));
            mTunerCntPrgDlg.setIndeterminate(true);
            mTunerCntPrgDlg.setCancelable(false);
            mTunerCntPrgDlg.setCanceledOnTouchOutside(false);
        }
        if (bShow) {
            if (!mTunerCntPrgDlg.isShowing()) {
                mTunerCntPrgDlg.show();
            }
        } else {
            mTunerCntPrgDlg.dismiss();
        }
    }

    private void connectMultiplex(Multiplex tp) {
        boolean bMotorUsed = (EnMotorType.NONE != mAntenna.getMotorType());

        mRefreshQS.setTunerConnectAttr(0, bMotorUsed); // 0 means async connect
        if ((null == mCurrentSettingNetwork) || (null == tp)) {
            mRefreshQS.tunerConnectState(null, null, null);
            mRefreshQS.setBarZero();
            return;
        }

        mCrtMultiplex = tp;
        Tuner tuner = mCurrentSettingNetwork.getTuner();

        // If the motor type is None locking relatively quickly, without the pop-up dialog box
        if (EnMotorType.NONE == mAntenna.getMotorType()) {
            mRefreshQS.tunerConnectState(tuner, mCrtMultiplex, null);
        } else {
            mRefreshQS.setTunerConnectAttr(2000, true); // wait connect timeout is 2s
            mRefreshQS.tunerConnectState(tuner, mCrtMultiplex, mCntHandler);
        }
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        return KeyDoResult.DO_NOTHING;
    }

    @Override
    public boolean isCanStartScan() {
        boolean bRet = (null != mCurrentSettingNetwork);
        if (super.isAdded()) {
            // If the currently selected effective satellite, then set satellite search data
            if (bRet) {
                List<Network> lstNetwork = new ArrayList<Network>();
                lstNetwork.add(mCurrentSettingNetwork);
                ((DTVApplication) getActivity().getApplication()).setScanParam(lstNetwork);
            } else {
                MyToast.makeText(mContext, getResources().getString(R.string.str_install_network_noselect),
                        MyToast.LENGTH_LONG).show();
                ((DTVApplication) getActivity().getApplication()).setScanParam(null);
            }
        }
        return bRet;
    }

    @Override
    public boolean isNetworkScan() {
        return true;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }

    private int updateMotorType(Motor.EnMotorType type) {
        int nRet = -1;
        if (0 == mAntenna.setMotorType(type)) {
            LogTool.d(LogTool.MSCAN, " ==== update type nRet=" + nRet + ",type=" + type);
            mIsChange = true;
            nRet = 0;
        }
        return nRet;
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        int nRet = 0;
        if (null == mAntenna) {
            return;
        }
        switch (arg0.getId()) {
            case R.id.id_antennaseting_satellite: {
                mLastSettingNetwork = mCurrentSettingNetwork;
                if (mIsChange) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            mLastSettingNetwork.getAntenna().save();
                        }
                    };
                    TaskUtil.post(runnable);
                }
                mIsChange = false;
                mbResumeLockTag = false;
                if (!mbLockTunerRun) {
                    mLockTunerHandler.removeCallbacks(mLockTunerRunnable); // delete delay lock function
                }
                refreshAntennaConfigData((DVBSNetwork) arg0.getTag());
                break;
            }
            case R.id.id_antennaseting_tp: {
                if (!mbLockTunerRun) {
                    mLockTunerHandler.removeCallbacks(mLockTunerRunnable); // delete delay lock function
                }
                connectMultiplex((Multiplex) mCbxTp.getTag());
                break;
            }
            case R.id.id_antennaseting_lnbtype: {
                LNBData lnbData = (LNBData) mCbxLnbType.getTag();
                nRet = mAntenna.setLNBData(lnbData);
                // If the KU band, 22K switch can automatically, can not be provided to other.<br>
                boolean canClick = ((EnLNBBand.LNB_BAND_KU != lnbData.getLNBBAND())
                        || ((EnLNBBand.LNB_BAND_KU == lnbData.getLNBBAND()) && (lnbData.getHighLO() == lnbData.getLowLO())));
                mCbx22k.setEnabled(canClick);
                mCbx22k.setFocusable(canClick);
                if (!canClick) {
                    mAntenna.set22KSwitch(Antenna.EnSwitchType.NONE);
                    mCbx22k.setTag(mAntenna.get22KSwitch());
                }
                connectMultiplex((Multiplex) mCbxTp.getTag());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_lnbpwr: {
                nRet = mAntenna.setLNBPower((EnLNBPowerSwitch) mCbxLnbPwr.getTag());
                connectMultiplex((Multiplex) mCbxTp.getTag());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_22K: {
                nRet = mAntenna.set22KSwitch((EnSwitchType) mCbx22k.getTag());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_dsqctype: {
                LogTool.d(LogTool.MSCAN, "puai ====DsqcType= " + mCbxDiSEqCType.getTag());
                nRet = mAntenna.setDiSEqCType((Antenna.EnDiSEqCType) mCbxDiSEqCType.getTag());
                changeDiSEqCType(mAntenna.getDiSEqCType());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_dieqc10: {
                nRet = mAntenna.setDiSEqC10Port((Antenna.EnDiSEqC10Port) mCbxDiSEqC10.getTag());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_dieqc11: {
                nRet = mAntenna.setDiSEqC11Port((Antenna.EnDiSEqC11Port) mCbxDiSEqC11.getTag());
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
            case R.id.id_antennaseting_motortype: {
                Motor.EnMotorType type = (Motor.EnMotorType) mCbxMotorType.getTag();
                nRet = updateMotorType(type);
                mIsChange = mIsChange || (0 == nRet);
                break;
            }
        }
        if (0 != nRet) {
            MyToast.makeText(mContext, getResources().getString(R.string.str_set_fail), MyToast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.id_antennasetting_search == v.getId()) {
            LogTool.d(LogTool.MSCAN, " ==== antenna setting search ====");
            if (null != mScanMainWnd) {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
            }
        }
    }

    private boolean isExist() {
        return mIsExist;
    }

    private boolean isTunerLocking() {
        boolean bRet = false;
        if (null != mRefreshQS) {
            bRet = mRefreshQS.isConnecting();
        }
        return bRet;
    }

    // Asynchronous display locking prompt dialog box.<br>
    static class TunerCntHandler extends Handler {
        WeakReference<AntennaFragment> mActivity;

        TunerCntHandler(AntennaFragment parent) {
            mActivity = new WeakReference<AntennaFragment>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            if ((null == mActivity) || (null == mActivity.get())) {
                return;
            }

            LogTool.d(LogTool.MSCAN, " ==== connect msg.what=" + msg.what);
            if ((RefreshQS.MESSAGE_CONNECT_SHOW_DIALOG == msg.what)
                    && (mActivity.get().isTunerLocking())
                    && (!mActivity.get().isExist())) {
                mActivity.get().showConnectDialog(true);
            } else if (RefreshQS.MESSAGE_CONNECT_CLOSE_DIALOG == msg.what) {
                mActivity.get().showConnectDialog(false);
            }
        }
    }
}
