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
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.hardware.Antenna;
import com.hisilicon.dtv.hardware.Motor;
import com.hisilicon.dtv.hardware.Motor.EnMotorMoveType;
import com.hisilicon.dtv.hardware.Motor.EnMotorType;
import com.hisilicon.dtv.hardware.MotorDiSEqC12;
import com.hisilicon.dtv.hardware.MotorUSALS;
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
import com.hisilicon.tvui.util.KeyValue;
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
 * Motor settings Fragment.<br>
 *
 * @author HiSilicon DTV stack software group
 * @see IScanMainWnd
 */
public class MotorFragment extends Fragment implements IScanSubWnd, OnComboxSelectChangeListener, OnClickListener,
        OnFocusChangeListener, OnKeyListener {
    // TP data unit conversion ratio
    private static final int TP_UNIT_RATE = 1000;
    private static final int INVALIDATE_VALUE = -1;
    private static final int LONGITUDE_LATITUDE_VALUE_RATE = 10;
    private static final String STR_LOCAL_LONGITUDE_TAG = "u32LocalLongitude";
    private static final String STR_LOCAL_LATITUDE_TAG = "u32LocalLatitude";
    private static final int MAX_LONGITUDE_MAX = 3600;
    private static final int MAX_LONGITUDE_LATI = 1800;

    private static final int MOTOR_MAX_COUNT = 64;

    // common data definition
    private boolean mIsChange = false;
    private IScanMainWnd mScanMainWnd = null;
    private Context mContext = null;

    private Combox mCbxSatellite = null;
    private Combox mCbxTP = null;
    private Combox mCbxMotorType = null;

    private MotorDiSEqC12 mMotorDiseqc12 = null;
    private MotorUSALS mMotorUSALS = null;

    DTV mDtv = null;

    //diseqc1.2
    private Button mBtnWest = null;
    private Button mBtnEast = null;
    private Button mBtnGotoPos = null;
    private Button mBtnScan = null;

    private Combox mCbxSatellitePositon = null;
    private Combox mCbxMotorMoveStep = null;

    private View mViewSatPos = null;
    private View mViewMotorMove = null;
    private View mViewSatrllite = null;
    private View mViewTransponder = null;

    //usals
    private Combox mCbxLongType = null;
    private Combox mCbxLatiType = null;
    private EditText mEdtLongitude = null;
    private EditText mEdtLatitude = null;
    private View mViewLongitude = null;
    private View mViewLatitude = null;
    private Button mBtnMotorSave = null;

    private EnMotorType menCurrentMotorType = EnMotorType.NONE;
    private EnMotorType menLastMotorType = EnMotorType.NONE;

    private RefreshQS mRefreshQS = null;
    private NetworkManager mNetworkManager = null;
    private DVBSNetwork mCurrentSetingNetwork = null;
    private DVBSNetwork mLastSetingNetwork = null;
    private Antenna mAntenna = null;

    private DTVConfig mConfig = null;

    private ProgressDialog mTunerCntPrgDlg = null;
    private TunerCntHandler mCntHandler = null;
    private Multiplex mCrtMultiplex = null;
    private boolean mMoveing = false;

    private boolean mbResumeLockTag = true;
    private Handler mLockTunerHandler = new Handler();
    private boolean mbLockTunerRun = false;
    private Runnable mLockTunerRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                if (mCurrentSetingNetwork != null) {
                    LinkedHashMap<String, Object> mapTPs = new LinkedHashMap<String, Object>();
                    List<Multiplex> lstTPs = mCurrentSetingNetwork.getMultiplexes();
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
        mDtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        if (mDtv == null) {
            LogTool.e(LogTool.MSCAN, " ==== dtv get instance error ===");
        }

        View v = inflater.inflate(R.layout.install_dvbs_motorsetting, container, false);
        mContext = inflater.getContext();

        mCbxSatellite = (Combox) v.findViewById(R.id.id_motorsetting_satellite);
        mCbxTP = (Combox) v.findViewById(R.id.id_motorsetting_tp);
        mCbxMotorType = (Combox) v.findViewById(R.id.id_motorsetting_motortype);
        mBtnMotorSave = (Button) v.findViewById(R.id.id_motor_position_save);

        mCbxSatellite.setOnSelectChangeListener(this);
        mCbxTP.setOnSelectChangeListener(this);
        mCbxMotorType.setOnSelectChangeListener(this);

        //diseqc1.2
        mBtnScan = (Button) v.findViewById(R.id.id_motorsetting_search);
        mBtnEast = (Button) v.findViewById(R.id.id_motor_move_east);
        mBtnWest = (Button) v.findViewById(R.id.id_motor_move_west);
        mBtnGotoPos = (Button) v.findViewById(R.id.id_motor12_goto_pos);
        mCbxSatellitePositon = (Combox) v.findViewById(R.id.id_motor_satellite_pos);
        mCbxMotorMoveStep = (Combox) v.findViewById(R.id.id_motor_move_step);
        mViewSatPos = (View) v.findViewById(R.id.layout_sat_pos);
        mViewMotorMove = (View) v.findViewById(R.id.layout_motor_move);
        mViewSatrllite = (View) v.findViewById(R.id.layout_motor_satellite);
        mViewTransponder = (View) v.findViewById(R.id.layout_motor_tp);
        initMotorDiseqc12Data();

        //usals
        mEdtLongitude = (EditText) v.findViewById(R.id.id_dvbs_motor_long);
        mEdtLatitude = (EditText) v.findViewById(R.id.id_dvbs_motor_lati);
        mCbxLongType = (Combox) v.findViewById(R.id.id_dvbs_motor_longtype);
        mCbxLatiType = (Combox) v.findViewById(R.id.id_dvbs_motor_latitype);
        mViewLongitude = (View) v.findViewById(R.id.layout_usals_long);
        mViewLatitude = (View) v.findViewById(R.id.layout_usals_lati);
        initMotorUsalsData();

        mRefreshQS = new RefreshQS(v);
        initCtrlData();
        ((DVBSInstallActivity) getActivity()).setCurrentIScanSubWnd(this);
        mScanMainWnd = (IScanMainWnd) getActivity();
        mCntHandler = new TunerCntHandler(this);
        return v;
    }

    private void initCtrlData() {
        LinkedHashMap<String, Object> mapMotorType = new LinkedHashMap<String, Object>();
        mapMotorType.put(mContext.getString(R.string.str_none), Motor.EnMotorType.NONE);
        mapMotorType.put(mContext.getString(R.string.str_install_diseqc12), Motor.EnMotorType.DISEQC12);
        mapMotorType.put(mContext.getString(R.string.str_install_usals), Motor.EnMotorType.USALS);
        mCbxMotorType.setData(mapMotorType);
        mBtnMotorSave.setOnClickListener(this);

    }

    private void initMotorUsalsData() {
        mEdtLongitude.setOnFocusChangeListener(this);
        mEdtLongitude.setOnKeyListener(this);
        mEdtLongitude.setFocusable(true);
        mEdtLongitude.setFocusableInTouchMode(true);

        mEdtLatitude.setOnFocusChangeListener(this);
        mEdtLatitude.setOnKeyListener(this);
        mEdtLatitude.setFocusable(true);
        mEdtLatitude.setFocusableInTouchMode(true);

        LinkedHashMap<String, Object> lstLongType = new LinkedHashMap<String, Object>();
        lstLongType.put(getResources().getString(R.string.str_install_long_east), true);
        lstLongType.put(getResources().getString(R.string.str_install_long_west), false);
        mCbxLongType.setData(lstLongType);

        LinkedHashMap<String, Object> lstLatiType = new LinkedHashMap<String, Object>();
        lstLatiType.put(getResources().getString(R.string.str_install_lati_north), true);
        lstLatiType.put(getResources().getString(R.string.str_install_lati_south), false);
        mCbxLatiType.setData(lstLatiType);

        mConfig = mDtv.getConfig();
        if (mConfig == null) {
            LogTool.e(LogTool.MSCAN, " ==== mConfig   null ===");
            return;
        }

        float lLong = mConfig.getInt(STR_LOCAL_LONGITUDE_TAG, 0);
        float lLati = mConfig.getInt(STR_LOCAL_LATITUDE_TAG, 0);

        String strLongType = getResources().getString(R.string.str_install_long_east);
        // Stored in the file data is converted, display, are converted into East longitude or west
        // longitude
        if (lLong > MAX_LONGITUDE_MAX / 2) {
            strLongType = getResources().getString(R.string.str_install_long_west);
            lLong = MAX_LONGITUDE_MAX - lLong;
        }

        mCbxLongType.setText(strLongType);
        mEdtLongitude.setText(String.valueOf(lLong / LONGITUDE_LATITUDE_VALUE_RATE));

        String strLatiType = getResources().getString(R.string.str_install_lati_north);
        if (lLati > MAX_LONGITUDE_LATI / 2) {
            strLatiType = getResources().getString(R.string.str_install_lati_south);
            lLati = MAX_LONGITUDE_LATI - lLati;
        }

        mCbxLatiType.setText(strLatiType);
        mEdtLatitude.setText(String.valueOf(lLati / LONGITUDE_LATITUDE_VALUE_RATE));
    }

    private void initMotorDiseqc12Data() {
        mBtnEast.setOnClickListener(this);
        mBtnWest.setOnClickListener(this);
        mBtnGotoPos.setOnClickListener(this);
        mBtnScan.setOnClickListener(this);

        LinkedHashMap<String, Object> mapPos = new LinkedHashMap<String, Object>();
        // Can goto to 0, but can not change the position of 0,so the position is start with 1;
        for (int i = 0; i < MOTOR_MAX_COUNT; i++) {
            mapPos.put("" + i, i);
        }
        mCbxSatellitePositon.setData(mapPos);
        mCbxSatellitePositon.setOnSelectChangeListener(this);

        LinkedHashMap<String, Object> mapMoveStep = new LinkedHashMap<String, Object>();
        mapMoveStep.put(EnMotorMoveType.CONTINUE.toString(), EnMotorMoveType.CONTINUE);
        mapMoveStep.put(EnMotorMoveType.FAST.toString(), EnMotorMoveType.FAST);
        mapMoveStep.put(EnMotorMoveType.SLOW.toString(), EnMotorMoveType.SLOW);
        mCbxMotorMoveStep.setData(mapMoveStep);
        mCbxMotorMoveStep.setOnSelectChangeListener(this);
    }

    private void setMotorVisible(EnMotorType enCrtMotorType) {
        int MotoDiseqc12Visibility = View.GONE;
        int MotoUsalsVisibility = View.GONE;
        int BtnScanVisibility = View.INVISIBLE;
        int BtnSaveVisibility = View.INVISIBLE;

        switch (enCrtMotorType) {
            case DISEQC12:
                LogTool.d(LogTool.MSCAN, " ==== DISEQC12 ====");
                MotoDiseqc12Visibility = View.VISIBLE;
                MotoUsalsVisibility = View.GONE;
                BtnScanVisibility = View.VISIBLE;
                BtnSaveVisibility = View.VISIBLE;
                mMotorDiseqc12 = (MotorDiSEqC12) mDtv.getHWManager().getMotor(EnMotorType.DISEQC12);
                mMotorUSALS = null;
                mRefreshQS.setBarZeroAlways(false);
                break;
            case USALS:
                LogTool.d(LogTool.MSCAN, " ==== USALS ====");
                MotoDiseqc12Visibility = View.GONE;
                MotoUsalsVisibility = View.VISIBLE;
                BtnScanVisibility = View.INVISIBLE;
                BtnSaveVisibility = View.VISIBLE;
                mRefreshQS.setBarZeroAlways(true);
                mMotorDiseqc12 = null;
                mMotorUSALS = (MotorUSALS) mDtv.getHWManager().getMotor(EnMotorType.USALS);
                break;
            case NONE:
                LogTool.d(LogTool.MSCAN, " ==== NONE ====");
                MotoDiseqc12Visibility = View.GONE;
                MotoUsalsVisibility = View.GONE;
                BtnScanVisibility = View.INVISIBLE;
                BtnSaveVisibility = View.INVISIBLE;
                mMotorDiseqc12 = null;
                mMotorUSALS = null;
                mRefreshQS.setBarZeroAlways(true);

        }
        //diseqc1.2
        mViewSatPos.setVisibility(MotoDiseqc12Visibility);
        mViewMotorMove.setVisibility(MotoDiseqc12Visibility);
        mViewTransponder.setVisibility(MotoDiseqc12Visibility);
        mBtnScan.setVisibility(BtnScanVisibility);
        //usals
        mViewLongitude.setVisibility(MotoUsalsVisibility);
        mViewLatitude.setVisibility(MotoUsalsVisibility);
        mBtnMotorSave.setVisibility(BtnSaveVisibility);
    }

    @Override
    public void onResume() {
        super.onResume();
        mbLockTunerRun = false;
        mbResumeLockTag = true;
        refreshSatellite();
        menLastMotorType = menCurrentMotorType;
        setMotorVisible(menCurrentMotorType);
        mRefreshQS.open();
        if (null != mMotorDiseqc12) {
            mMotorDiseqc12.setAutoRolationSwitch(false);
            mRefreshQS.setBarZeroAlways(false);
        } else {
            mRefreshQS.setBarZeroAlways(true);
        }
        if (null != mMotorUSALS) {
            mMotorUSALS.setAutoRolationSwitch(false);
        }
        mLockTunerHandler.postDelayed(mLockTunerRunnable, 1000); // delay 1s to run

    }

    @Override
    public void onPause() {
        if ((menCurrentMotorType == EnMotorType.NONE) && (menLastMotorType != EnMotorType.NONE)) {
            if (null != mCurrentSetingNetwork) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        mCurrentSetingNetwork.getAntenna().save();
                    }
                };
                TaskUtil.post(runnable);
            }
        }
        mCntHandler.removeMessages(RefreshQS.MESSAGE_CONNECT_SHOW_DIALOG);
        mCntHandler.removeMessages(RefreshQS.MESSAGE_CONNECT_CLOSE_DIALOG);
        if (mbLockTunerRun == false) {
            mLockTunerHandler.removeCallbacks(mLockTunerRunnable); // delete delay lock function
        }
        mRefreshQS.close();
        if (null != mMotorDiseqc12) {
            mMotorDiseqc12.setAutoRolationSwitch(true);
        }
        if (null != mMotorUSALS) {
            mMotorUSALS.setAutoRolationSwitch(true);
        }

        showConnectDialog(false);
        super.onPause();
    }

    private void checkTextChange(View v) {
        String strName = "";
        int nValue = INVALIDATE_VALUE;
        int nMaxValue = 0;

        if (v.getId() == R.id.id_dvbs_motor_long) {
            nValue = getValidateLong();
            nMaxValue = MAX_LONGITUDE_MAX / LONGITUDE_LATITUDE_VALUE_RATE / 2;
            strName = this.getString(R.string.str_install_local_long);
            LogTool.d(LogTool.MSCAN, "id_dvbs_motor13_long = " + nValue);
        } else if (v.getId() == R.id.id_dvbs_motor_lati) {
            nValue = getValidateLati();
            nMaxValue = MAX_LONGITUDE_LATI / LONGITUDE_LATITUDE_VALUE_RATE / 2;
            strName = this.getString(R.string.str_install_locat_lati);
            LogTool.d(LogTool.MSCAN, "id_dvbs_motor13_lati = " + nValue);
        } else {
            return;
        }

        LogTool.v(LogTool.MSCAN, "nValue=" + nValue);
        if (INVALIDATE_VALUE == nValue) {
            String strForm = this.getString(R.string.str_install_range_out);
            String strTip = String.format(strForm, strName, 0, nMaxValue);
            MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
        }
    }

    private void refreshSatellite() {
        mNetworkManager = mDtv.getNetworkManager();

        // Refresh the list of satellite
        List<Network> lstNetwork = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);
        LinkedHashMap<String, Object> lstSatellite = new LinkedHashMap<String, Object>();
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
        if ((null == satellite) || ((null != mCurrentSetingNetwork)
                && (mCurrentSetingNetwork.getID() == satellite.getID()))) {
            return;
        }
        mCurrentSetingNetwork = satellite;
        mAntenna = satellite.getAntenna();
        mScanMainWnd.setCrtSelectedNetwor(mCurrentSetingNetwork);
        menCurrentMotorType = mAntenna.getMotorType();
        LogTool.d(LogTool.MSCAN, " ====  menCurrentMotorType  =" + menCurrentMotorType);
        mCbxMotorType.setTag(menCurrentMotorType);
        if (menCurrentMotorType == EnMotorType.DISEQC12) {
            mMotorDiseqc12 = (MotorDiSEqC12) mDtv.getHWManager().getMotor(EnMotorType.DISEQC12);
            mMotorUSALS = null;
            mCbxSatellitePositon.setText("" + mAntenna.getMotorPositionID());
        } else if (menCurrentMotorType == EnMotorType.USALS) {
            mMotorDiseqc12 = null;
            mMotorUSALS = (MotorUSALS) mDtv.getHWManager().getMotor(EnMotorType.USALS);
        }
        mScanMainWnd.setCrtSelectedNetwor(mCurrentSetingNetwork);

        refreshTPData(mCurrentSetingNetwork);
    }

    ;

    private void refreshTPData(DVBSNetwork satellite) {
        if (null != satellite) {
            LinkedHashMap<String, Object> mapTPs = new LinkedHashMap<String, Object>();
            List<Multiplex> lstTPs = satellite.getMultiplexes();
            for (Multiplex multiplex : lstTPs) {
                DVBSTransponder tp = (DVBSTransponder) multiplex;
                mapTPs.put(String.format(Locale.getDefault(), "%d/%d/%1s", tp.getFrequency() / TP_UNIT_RATE,
                        tp.getSymbolRate() / TP_UNIT_RATE, tp.getPolarity() == EnPolarity.VERTICAL ? "V" : "H"), tp);
            }
            mCbxTP.setData(mapTPs);
        } else {
            mCbxTP.setData(null);
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
        if ((null == mCurrentSetingNetwork) || (null == tp)) {
            mRefreshQS.tunerConnectState(null, null, null);
            return;
        }
        mCrtMultiplex = tp;
        mRefreshQS.setTunerConnectAttr(0, false);
        if (null == mCurrentSetingNetwork) {
            mRefreshQS.tunerConnectState(null, null, null);
            return;
        }

        Tuner tuner = mCurrentSetingNetwork.getTuner();
        mRefreshQS.tunerConnectState(tuner, mCrtMultiplex, mCntHandler);
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        LogTool.d(LogTool.MSCAN, " ==== keyDispatch key  =" + keyCode);
        if (!super.isAdded()) {
            return KeyDoResult.DO_NOTHING;
        }
        if ((((DVBSInstallActivity) getActivity()).isFocused()) && (KeyValue.DTV_KEYVALUE_DPAD_RIGHT == keyCode)) {
            mCbxSatellite.requestFocus();
        }
        if ((mBtnEast.isFocused() || mEdtLongitude.isFocused() || mEdtLatitude.isFocused() || mBtnGotoPos.isFocused()
                || mCbxMotorMoveStep.isFocused()) && (KeyValue.DTV_KEYVALUE_DPAD_LEFT == keyCode)) {
            return KeyDoResult.DO_DONE_NEED_SYSTEM;
        }
        return KeyDoResult.DO_NOTHING;
    }

    @Override
    public boolean isCanStartScan() {
        boolean bRet = (null != mCurrentSetingNetwork);
        if (super.isAdded()) {
            // If the currently selected effective satellite, then set satellite search data
            if (bRet) {
                List<Network> lstNetwork = new ArrayList<Network>();
                lstNetwork.add(mCurrentSetingNetwork);
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
        int nRet = 0;
        if (0 == mAntenna.setMotorType(type)) {
            LogTool.d(LogTool.MSCAN, " ====  menCurrentMotorType  =" + mAntenna.getMotorType());

            LogTool.d(LogTool.MSCAN, " ==== update type nRet=" + nRet + ",type=" + type);
            mIsChange = mIsChange || (0 == nRet);
            menCurrentMotorType = type;
            LogTool.d(LogTool.MSCAN, " ====  menCurrentMotorType  =" + menCurrentMotorType);

        }
        return nRet;
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        int nRet = 0;
        LogTool.d(LogTool.MSCAN, " ==== onComboxSelectChange =");
        if (null == mAntenna) {
            return;
        }
        switch (arg0.getId()) {
            case R.id.id_motorsetting_satellite: {
                mLastSetingNetwork = mCurrentSetingNetwork;
                if (mIsChange) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            mLastSetingNetwork.getAntenna().save();
                        }
                    };
                    TaskUtil.post(runnable);
                }
                mIsChange = false;
                mbResumeLockTag = false;
                if (!mbLockTunerRun) {
                    mLockTunerHandler.removeCallbacks(mLockTunerRunnable); // delete delay lock funtion
                }
                refreshAntennaConfigData((DVBSNetwork) arg0.getTag());
                setMotorVisible(menCurrentMotorType);
                connectMultiplex((Multiplex) mCbxTP.getTag());
                break;
            }
            case R.id.id_motorsetting_tp: {
                if (!mbLockTunerRun) {
                    mLockTunerHandler.removeCallbacks(mLockTunerRunnable); // delete delay lock funtion
                }
                connectMultiplex((Multiplex) mCbxTP.getTag());
                break;
            }
            case R.id.id_motorsetting_motortype: {
                LogTool.d(LogTool.MSCAN, " ====  id_motorsetting_motortype ====");
                Motor.EnMotorType type = (Motor.EnMotorType) mCbxMotorType.getTag();
                nRet = updateMotorType(type);
                mIsChange = mIsChange || (0 == nRet);
                setMotorVisible(menCurrentMotorType);
                if (menCurrentMotorType == EnMotorType.DISEQC12) {
                    connectMultiplex((Multiplex) mCbxTP.getTag());
                }
                break;
            }
            case R.id.id_motor_satellite_pos: {
                LogTool.d(LogTool.MSCAN, " ====  id_motor_satellite_pos =");
                if ((null != mCurrentSetingNetwork) && (null != mMotorDiseqc12)) {
                    LogTool.d(LogTool.MSCAN, " ====  id_motor_satellite_pos =");
                    int nIndex = Integer.parseInt(mCbxSatellitePositon.getTag().toString());
                    mIsChange = 0 == mCurrentSetingNetwork.getAntenna().setMotorPositionID(nIndex);
                    //no need to move motor to this position
                    mMotorDiseqc12.gotoPos(nIndex);
                    mIsChange = true;
                }
                break;
            }
            case R.id.id_motor_move_step: {
                break;
            }
        }
        if (0 != nRet) {
            MyToast.makeText(mContext, getResources().getString(R.string.str_set_fail), MyToast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        LogTool.d(LogTool.MSCAN, " ==== motor noclick=");
        if (R.id.id_motorsetting_search == v.getId()) {
            LogTool.d(LogTool.MSCAN, " ==== id_antennasetting_search ====");
            if (null != mScanMainWnd) {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
            }
        } else if (R.id.id_motor_position_save == v.getId() && (null == mMotorDiseqc12)) {
            String strTip = this.getString(R.string.str_save_success);
            if (mIsChange) {
                int nLongValue = getValidateLong();
                if (INVALIDATE_VALUE == nLongValue) {
                    String strForm = this.getString(R.string.str_install_range_out);
                    strTip = String.format(strForm, this.getString(R.string.str_install_local_long), 0,
                            MAX_LONGITUDE_MAX / LONGITUDE_LATITUDE_VALUE_RATE / 2);
                    MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
                    return;
                }
                int nLatiValue = getValidateLati();

                if (INVALIDATE_VALUE == nLatiValue) {
                    String strForm = this.getString(R.string.str_install_range_out);
                    strTip = String.format(strForm, this.getString(R.string.str_install_locat_lati), 0,
                            MAX_LONGITUDE_LATI / LONGITUDE_LATITUDE_VALUE_RATE / 2);
                    MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
                    return;
                }
                if (null != mCurrentSetingNetwork) {
                    Runnable runnable = new Runnable() {
                        public void run() {
                            mCurrentSetingNetwork.getAntenna().save();
                        }
                    };
                    TaskUtil.post(runnable);
                }
                mIsChange = (0 != mConfig.setInt(STR_LOCAL_LONGITUDE_TAG, nLongValue))
                                || (0 != mConfig.setInt(STR_LOCAL_LATITUDE_TAG, nLatiValue));
                if (null != mMotorUSALS) {
                    LogTool.d(LogTool.MSCAN,
                            " button motor save usals " + mIsChange + " " + nLongValue + " " + nLatiValue);
                    mMotorUSALS.setLocalLatitude(nLatiValue);
                    mMotorUSALS.setLocalLongitude(nLongValue);
                }
                if (mIsChange) {
                    strTip = this.getString(R.string.str_save_fail);
                }
            }
            MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
        } else if (R.id.id_motor_position_save == v.getId() && (null != mMotorDiseqc12)) {
            LogTool.d(LogTool.MSCAN, "lgj button motor save");
            int nIndex = Integer.parseInt(mCbxSatellitePositon.getTag().toString());
            if (0 == nIndex) {
                MyToast.makeText(mContext, getResources().getString(R.string.str_can_not_save), Toast.LENGTH_LONG).show();
                return;
            }
            mMoveing = (0 != mMotorDiseqc12.stopMove());
            mBtnWest.setEnabled(true);
            mBtnEast.setEnabled(true);
            mBtnWest.setFocusable(true);
            mBtnEast.setFocusable(true);
            mBtnWest.setText(getResources().getString(R.string.str_install_to_west));
            mBtnEast.setText(getResources().getString(R.string.str_install_to_east));
            // save to db
            if (null != mCurrentSetingNetwork) {
                Runnable runnable = new Runnable() {
                    public void run() {
                        mCurrentSetingNetwork.getAntenna().save();
                    }
                };
                TaskUtil.post(runnable);
            }
            // save to motor
            if (mMotorDiseqc12.storePos(nIndex) != 0) {
                LogTool.d(LogTool.MSCAN, "lgj button motor save fail");
                MyToast.makeText(mContext, getResources().getString(R.string.str_save_fail), Toast.LENGTH_LONG).show();
            } else {
                LogTool.d(LogTool.MSCAN, "lgj button motor save success");
                MyToast.makeText(mContext, getResources().getString(R.string.str_save_success), Toast.LENGTH_LONG).show();
            }
        } else if (R.id.id_motor12_goto_pos == v.getId() && (null != mMotorDiseqc12)) {
            if (null != mCurrentSetingNetwork) {
                int nIndex = Integer.parseInt(mCbxSatellitePositon.getTag().toString());
                if (mMotorDiseqc12 != null) {
                    mMotorDiseqc12.gotoPos(nIndex);
                }
            }
        } else if (((v.getId() == R.id.id_motor_move_west) || (v.getId() == R.id.id_motor_move_east))
                && (mMotorDiseqc12 != null)) {
            boolean bIsWest = (v.getId() == R.id.id_motor_move_west);
            EnMotorMoveType moveType = (EnMotorMoveType) mCbxMotorMoveStep.getTag();
            LogTool.d(LogTool.MSCAN, "id_motor_limit_move_west=");

            if (mMoveing) {
                mMoveing = (0 != mMotorDiseqc12.stopMove());
                mBtnWest.setEnabled(true);
                mBtnEast.setEnabled(true);
                mBtnWest.setFocusable(true);
                mBtnEast.setFocusable(true);
                mBtnWest.setText(getResources().getString(R.string.str_install_to_west));
                mBtnEast.setText(getResources().getString(R.string.str_install_to_east));
            } else {
                mMoveing = (0 == mMotorDiseqc12.move(moveType, bIsWest));
                if (moveType != EnMotorMoveType.CONTINUE) {
                    mMoveing = false;
                } else {
                    if (true == bIsWest) {
                        mBtnEast.setEnabled(false);
                        mBtnEast.setFocusable(false);
                        mBtnWest.setText(getResources().getString(R.string.str_install_stop_move));
                    } else {
                        mBtnWest.setEnabled(false);
                        mBtnWest.setFocusable(false);
                        mBtnEast.setText(getResources().getString(R.string.str_install_stop_move));
                    }
                }
            }
        }
    }

    // If the effective return a valid longitude, if invalid return -1.
    private int getValidateLong() {
        int nRet = INVALIDATE_VALUE;

        String strStrValue = "" + mEdtLongitude.getText();
        LogTool.v(LogTool.MSCAN, "getValidateLong=" + strStrValue);

        if (strStrValue.equals("")) {
            return 0;
        }

        float nLongValue = (Float.parseFloat(strStrValue) * LONGITUDE_LATITUDE_VALUE_RATE);
        if (nLongValue > MAX_LONGITUDE_MAX / 2) {
            mEdtLongitude.setText(String.valueOf(MAX_LONGITUDE_LATI / LONGITUDE_LATITUDE_VALUE_RATE));

            return INVALIDATE_VALUE;
        }

        if (nLongValue == 0) {
            //mEdtLongitude.setText("" + 0);
            return 0;
        }

        if (!((Boolean) mCbxLongType.getTag())) {
            nLongValue = MAX_LONGITUDE_MAX - nLongValue;
        }
        if ((MAX_LONGITUDE_MAX > nLongValue) && (nLongValue >= 0)) {
            nRet = (int) nLongValue;
        }

        return nRet;
    }

    // If effective, return a valid latitude, if invalid return -1.
    private int getValidateLati() {
        int nRet = INVALIDATE_VALUE;
        String strStrValue = "" + mEdtLatitude.getText();

        if (strStrValue.equals("")) {
            return 0;
        }

        float nLatiValue = (Float.parseFloat(strStrValue) * LONGITUDE_LATITUDE_VALUE_RATE);
        if (nLatiValue > MAX_LONGITUDE_LATI / 2) {
            mEdtLatitude.setText(String.valueOf(MAX_LONGITUDE_LATI / (2 * LONGITUDE_LATITUDE_VALUE_RATE)));

            return INVALIDATE_VALUE;
        }

        if (nLatiValue == 0) {
            return 0;
        }

        if (!((Boolean) mCbxLatiType.getTag())) {
            nLatiValue = MAX_LONGITUDE_LATI - nLatiValue;
        }
        if ((MAX_LONGITUDE_LATI > nLatiValue) && (nLatiValue >= 0)) {
            nRet = (int) nLatiValue;
        }

        return nRet;
    }

    @Override
    public void onFocusChange(View arg0, boolean arg1) {
        LogTool.v(LogTool.MSCAN, "id = " + arg0.getId());
        LogTool.v(LogTool.MSCAN, "arg1 = " + arg1);

        if (!arg1) {
            String strName = "";
            int nValue = INVALIDATE_VALUE;
            int nMaxValue = 0;
            if (arg0.getId() == R.id.id_dvbs_motor_long) {
                String strStrValue = "" + mEdtLongitude.getText();
                LogTool.v(LogTool.MSCAN, "getValidateLong=" + strStrValue);

                /*if user input null str, put it to 0.0*/
                if (strStrValue.equals("")) {
                    float lLong = mConfig.getInt(STR_LOCAL_LONGITUDE_TAG, 0);

                    mEdtLongitude.setText(String.valueOf(lLong / LONGITUDE_LATITUDE_VALUE_RATE));
                }

                nValue = getValidateLong();
                nMaxValue = MAX_LONGITUDE_MAX / LONGITUDE_LATITUDE_VALUE_RATE / 2;
                strName = this.getString(R.string.str_install_local_long);
                LogTool.d(LogTool.MSCAN, "id_dvbs_motor13_long = " + nValue);

            } else if (arg0.getId() == R.id.id_dvbs_motor_lati) {
                String strStrValue = "" + mEdtLatitude.getText();

                /*if user input null str, put it to 0.0*/
                if (strStrValue.equals("")) {
                    float lLati = mConfig.getInt(STR_LOCAL_LATITUDE_TAG, 0);

                    mEdtLatitude.setText(String.valueOf(lLati / LONGITUDE_LATITUDE_VALUE_RATE));
                }

                nValue = getValidateLati();
                nMaxValue = MAX_LONGITUDE_LATI / LONGITUDE_LATITUDE_VALUE_RATE / 2;
                strName = this.getString(R.string.str_install_locat_lati);
                LogTool.d(LogTool.MSCAN, "id_dvbs_motor13_lati = " + nValue);
            } else {
                return;
            }

            LogTool.v(LogTool.MSCAN, "nValue=" + nValue);
            if (INVALIDATE_VALUE == nValue) {
                String strForm = this.getString(R.string.str_install_range_out);
                String strTip = String.format(strForm, strName, 0, nMaxValue);
                MyToast.makeText(mContext, strTip, MyToast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            // 0-9 key
            if ((KeyValue.DTV_KEYVALUE_0 <= keyCode) && (KeyValue.DTV_KEYVALUE_9 >= keyCode)) {
                mIsChange = true;
            } else if (KeyValue.DTV_KEYVALUE_DEL == keyCode) //del key
            {
                mIsChange = true;
            }
        }

        if (v.getId() == R.id.id_dvbs_motor_long) {
            String strStrValue = "" + mEdtLongitude.getText();
            if (!strStrValue.equals("")) {
                checkTextChange(v);
            }
        }

        if (v.getId() == R.id.id_dvbs_motor_lati) {
            String strStrValue = "" + mEdtLatitude.getText();
            if (!strStrValue.equals("")) {
                checkTextChange(v);
            }
        }

        LogTool.d(LogTool.MSCAN, "kecode=" + keyCode + ",mIsChange=" + mIsChange);
        return false;
    }

    // Asynchronous display locking prompt dialog box.<br>
    static class TunerCntHandler extends Handler {
        WeakReference<MotorFragment> mActivity;

        TunerCntHandler(MotorFragment parent) {
            mActivity = new WeakReference<MotorFragment>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            if ((null == mActivity) || (null == mActivity.get())) {
                return;
            }
        }
    }
}
