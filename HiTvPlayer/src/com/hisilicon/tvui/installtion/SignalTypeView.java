package com.hisilicon.tvui.installtion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.constant.EnumSystemTvSystem;
import com.hisilicon.android.tvapi.impl.SystemSettingImpl;
import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTransmissionTypeFilter;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.ScanType.EnFrequencyTable;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;


public class SignalTypeView extends BaseView implements IScanSubWnd, OnClickListener, OnComboxSelectChangeListener {
    private static final String TAG = "SignalTypeView";

    private static final int TUNER_SUPPORTED_MODE_MAXNUM = 4;
    private static final int SUPPORTED_TUNER_MAXNUM = 4;
    public static final String TUNER_INFO_NUM = "u32TunerNum";
    public static final String TUNER_NUM_SECTION = "tunernum";
    public static final int DEFAULT_TUNER_INFO_NUM = 1;

    private TunerDevice[] mTunerDevice = new TunerDevice[SUPPORTED_TUNER_MAXNUM];
    private Combox mCbxScanMode = null;
    private Combox mCbxTunerSignal = null;
    private Combox mCbxTunerDevice = null;
    private Combox mCbxAutoScanMode = null;
    private Combox mOneSegCombox = null;
    private LinearLayout mOneSegLayout = null;
    // Service Type
    private Combox mServiceTypeCombox = null;
    private EnTVRadioFilter mTVRadioFilter = EnTVRadioFilter.ALL;

    private Combox mCbxCableFormat = null;
    private LinearLayout mCableFormatLayout = null;

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;

    private int mCurrentTunerDeviceID = -1;
    private int mCurrentTunerSignal = -1;
    private EnNetworkType mNetworkTypeSelect = EnNetworkType.NONE;
    private EnScanMode mScanModeSelect = EnScanMode.NONE;

    private int mOneSegFlag = 0;
    private EnFrequencyTable mFrequencyTable = EnFrequencyTable.ATSC_AUTO;

    LinkedHashMap<String, Object> mMapSignalType = null;
    LinkedHashMap<String, Object> mTerMapTunerDevice = null;
    LinkedHashMap<String, Object> mDtmbMapTunerDevice = null;
    LinkedHashMap<String, Object> mCableMapTunerDevice = null;
    LinkedHashMap<String, Object> mIsdbtMapTunerDevice = null;
    LinkedHashMap<String, Object> mAtscTMapTunerDevice = null;
    LinkedHashMap<String, Object> mAtscCabMapTunerDevice = null;

    //List<Network> mLstNetwork = null;
    List<Network> mLstTerNetwork = null;
    List<Network> mLstDtmbNetwork = null;
    List<Network> mLstCableNetwork = null;
    List<Network> mLstIsdbtNetwork = null;
    List<Network> mLstAtscTNetwork = null;
    List<Network> mLstAtscCabNetwork = null;

    public enum EnScanMode {
        NONE("NONE"),
        DTV_ONLY("DTV"),
        ATV_ONLY("ATV"),
        DTV_ATV("DTV+ATV");

        private final String m_szMode;

        EnScanMode(String m_szMode) {
            this.m_szMode = m_szMode;
        }

        public String toString() {
            return m_szMode;
        }
    }

    public enum EnAutoScanMode {
        UPDATE("UPDATE"),
        REINSTALL("RE-INSTALL");

        private final String m_szMode;

        EnAutoScanMode(String m_szMode) {
            this.m_szMode = m_szMode;
        }

        public String toString() {
            return m_szMode;
        }
    }

    public enum EnServiceType {
        ALL("ALL"),
        DTV("DTV"),
        RADIO("RADIO");

        private final String m_szMode;

        EnServiceType(String m_szMode) {
            this.m_szMode = m_szMode;
        }
        public String toString() {
            return m_szMode;
        }
    }

    public SignalTypeView(BaseActivity arg0) {
        super((LinearLayout) arg0.findViewById(R.id.ly_singal_type_select));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd) arg0;
        mNetworkTypeSelect = mNetworkManager.getCurrentNetworkType();
        mScanModeSelect = EnScanMode.NONE;
        initView();
    }

    private void initView() {
        mCbxTunerSignal = mParentWnd.findViewById(R.id.id_dvb_tuner_signal);
        mCbxTunerDevice = mParentWnd.findViewById(R.id.id_dvb_tuner_device);
        mCbxScanMode = mParentWnd.findViewById(R.id.id_dvb_scan_mode);
        mCbxAutoScanMode = mParentWnd.findViewById(R.id.id_auto_scan_mode);
        mServiceTypeCombox = mParentWnd.findViewById(R.id.id_service_cab_format);
        Button mBtnTunerSet = mParentWnd.findViewById(R.id.id_dvb_tuner_set_btn);

        mOneSegCombox = mParentWnd.findViewById(R.id.id_isdbt_oneseg);
        mOneSegLayout = mParentWnd.findViewById(R.id.id_layout_isdbt_oneseg);
        mCbxCableFormat = mParentWnd.findViewById(R.id.id_atsc_cab_format);
        mCableFormatLayout = mParentWnd.findViewById(R.id.id_layout_atsc_cab_format);
        mCbxTunerSignal.setOnSelectChangeListener(this);
        mCbxTunerDevice.setOnSelectChangeListener(this);
        mCbxScanMode.setOnSelectChangeListener(this);
        mCbxAutoScanMode.setOnSelectChangeListener(this);
        mServiceTypeCombox.setOnSelectChangeListener(this);
        mBtnTunerSet.setOnClickListener(this);
        mCbxCableFormat.setOnSelectChangeListener(this);
        mOneSegCombox.setOnSelectChangeListener(this);

        LinkedHashMap<String, Object> mapScanMode = new LinkedHashMap<String, Object>();
        LinkedHashMap<String, Object> mapAutoScanMode = new LinkedHashMap<String, Object>();

        // Choosing different auto scan type in different areas
        int areaCode = SystemSettingImpl.getInstance().getTvSystem();
        int curSourceId = TvSourceManager.getInstance().getCurSourceId(0);
        if (areaCode == EnumSystemTvSystem.TVSYSTEM_ISDBT && curSourceId == EnumSourceIndex.SOURCE_ATV) {
            mapScanMode.put(EnScanMode.ATV_ONLY.toString(), EnScanMode.ATV_ONLY);
        } else if ((areaCode == EnumSystemTvSystem.TVSYSTEM_ISDBT
                && curSourceId == EnumSourceIndex.SOURCE_ISDBT)
                || areaCode == EnumSystemTvSystem.TVSYSTEM_ATSC) {
            mapScanMode.put(EnScanMode.DTV_ATV.toString(), EnScanMode.DTV_ATV);
        } else {
            mapScanMode.put(EnScanMode.DTV_ONLY.toString(), EnScanMode.DTV_ONLY);
            mapScanMode.put(EnScanMode.ATV_ONLY.toString(), EnScanMode.ATV_ONLY);
            mapScanMode.put(EnScanMode.DTV_ATV.toString(), EnScanMode.DTV_ATV);
        }

        mapAutoScanMode.put(EnAutoScanMode.UPDATE.toString(), EnAutoScanMode.UPDATE);
        mapAutoScanMode.put(EnAutoScanMode.REINSTALL.toString(), EnAutoScanMode.REINSTALL);

        mCbxScanMode.setData(mapScanMode);
        mCbxAutoScanMode.setData(mapAutoScanMode);
        if (1 == mDtvConfig.getInt("u32ScanResultMode", 1)) {
            mCbxAutoScanMode.setText(EnAutoScanMode.UPDATE.toString());
        } else {
            mCbxAutoScanMode.setText(EnAutoScanMode.REINSTALL.toString());
        }
        // set service type data
        LinkedHashMap<String, Object> mapServiceType = new LinkedHashMap<String, Object>();
        mapServiceType.put(EnServiceType.ALL.toString(), EnServiceType.ALL);
        mapServiceType.put(EnServiceType.RADIO.toString(), EnServiceType.RADIO);
        mapServiceType.put(EnServiceType.DTV.toString(), EnServiceType.DTV);
        mServiceTypeCombox.setData(mapServiceType);
        mServiceTypeCombox.setText(EnServiceType.ALL.toString());
    }

    private void initTunerDeviceMaps() {
        //mLstNetwork = mNetworkManager.getNetworks(EnNetworkType.SATELLITE);
        mLstTerNetwork = mNetworkManager.getNetworks(EnNetworkType.TERRESTRIAL);
        mLstDtmbNetwork = mNetworkManager.getNetworks(EnNetworkType.DTMB);
        mLstCableNetwork = mNetworkManager.getNetworks(EnNetworkType.CABLE);
        mLstIsdbtNetwork = mNetworkManager.getNetworks(EnNetworkType.ISDB_TER);
        mLstAtscTNetwork = mNetworkManager.getNetworks(EnNetworkType.ATSC_T);
        mLstAtscCabNetwork = mNetworkManager.getNetworks(EnNetworkType.ATSC_CAB);
        mMapSignalType = new LinkedHashMap<>();
        mCableMapTunerDevice = new LinkedHashMap<>();
        mDtmbMapTunerDevice = new LinkedHashMap<>();
        mTerMapTunerDevice = new LinkedHashMap<>();
        mIsdbtMapTunerDevice = new LinkedHashMap<>();
        mAtscTMapTunerDevice = new LinkedHashMap<>();
        mAtscCabMapTunerDevice = new LinkedHashMap<>();
        initTunerDevice(EnNetworkType.CABLE, mCableMapTunerDevice, mLstCableNetwork);
        initTunerDevice(EnNetworkType.DTMB, mDtmbMapTunerDevice, mLstDtmbNetwork);
        initTunerDevice(EnNetworkType.TERRESTRIAL, mTerMapTunerDevice, mLstTerNetwork);
        initTunerDevice(EnNetworkType.ISDB_TER, mIsdbtMapTunerDevice, mLstIsdbtNetwork);
        initTunerDevice(EnNetworkType.ATSC_T, mAtscTMapTunerDevice, mLstAtscTNetwork);
        initTunerDevice(EnNetworkType.ATSC_CAB, mAtscCabMapTunerDevice, mLstAtscCabNetwork);
    }

    private void initTunerDevice(EnNetworkType networkType,
                                 LinkedHashMap<String, Object> tunerDevice, List<Network> lstNetwork) {
        if (null != lstNetwork && lstNetwork.size() > 0) {
            mMapSignalType.put(networkType.toString(), networkType);
            int count = lstNetwork.size();
            for (int i = 0; i < count; i++) {
                Network obj = lstNetwork.get(i);
                int tunerid = obj.getTuner().getTunerID();
                if (!tunerDevice.containsValue(tunerid)) {
                    tunerDevice.put("Tuner" + tunerid, tunerid);
                }
                LogTool.d(LogTool.MSCAN, "network====" + networkType.toString() + "networkType====" + networkType + "tunerId====" + obj.getTuner().getTunerID());
            }
        } else {
            LogTool.d(LogTool.MSCAN, String.format(" %s network is null, if support %s please check database",
                    networkType.toString(), networkType.toString()));
        }
    }

    private void initData() {
        initTunerDeviceMaps();
        EnNetworkType curSignalType = mNetworkManager.getCurrentNetworkType();
        mNetworkTypeSelect = curSignalType;
        LogTool.d(LogTool.MSCAN, "curSignalType===" + curSignalType);
        mCbxTunerSignal.setData(mMapSignalType);
        if (curSignalType == EnNetworkType.CABLE && null != mCableMapTunerDevice && mCableMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mCableMapTunerDevice);
            mCurrentTunerDeviceID = mLstCableNetwork.get(0).getTuner().getTunerID();
        } else if (curSignalType == EnNetworkType.TERRESTRIAL && null != mTerMapTunerDevice && mTerMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mTerMapTunerDevice);
            mCurrentTunerDeviceID = mLstTerNetwork.get(0).getTuner().getTunerID();
        } else if (curSignalType == EnNetworkType.DTMB && null != mDtmbMapTunerDevice && mDtmbMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mDtmbMapTunerDevice);
            mCurrentTunerDeviceID = mLstDtmbNetwork.get(0).getTuner().getTunerID();
        } else if (curSignalType == EnNetworkType.ISDB_TER && null != mIsdbtMapTunerDevice && mIsdbtMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mIsdbtMapTunerDevice);
            mCurrentTunerDeviceID = mLstIsdbtNetwork.get(0).getTuner().getTunerID();
        } else if (curSignalType == EnNetworkType.ATSC_T && null != mAtscTMapTunerDevice && mAtscTMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mAtscTMapTunerDevice);
            mCurrentTunerDeviceID = mLstAtscTNetwork.get(0).getTuner().getTunerID();
        } else if (curSignalType == EnNetworkType.ATSC_CAB && null != mAtscCabMapTunerDevice && mAtscCabMapTunerDevice.size() > 0) {
            mCbxTunerDevice.setData(mAtscCabMapTunerDevice);
            mCurrentTunerDeviceID = mLstAtscCabNetwork.get(0).getTuner().getTunerID();
        } else {
            if (null != mCableMapTunerDevice && mCableMapTunerDevice.size() > 0) {
                mCbxTunerDevice.setData(mCableMapTunerDevice);
                mCurrentTunerDeviceID = mLstCableNetwork.get(0).getTuner().getTunerID();
            } else if (null != mDtmbMapTunerDevice && mDtmbMapTunerDevice.size() > 0) {
                mCbxTunerDevice.setData(mDtmbMapTunerDevice);
                mCurrentTunerDeviceID = mLstDtmbNetwork.get(0).getTuner().getTunerID();
            } else if (null != mTerMapTunerDevice && mTerMapTunerDevice.size() > 0) {
                mCbxTunerDevice.setData(mTerMapTunerDevice);
                mCurrentTunerDeviceID = mLstTerNetwork.get(0).getTuner().getTunerID();
            } else if (null != mIsdbtMapTunerDevice && mIsdbtMapTunerDevice.size() > 0) {
                mCbxTunerDevice.setData(mIsdbtMapTunerDevice);
                mCurrentTunerDeviceID = mLstIsdbtNetwork.get(0).getTuner().getTunerID();
            }
        }

        mCurrentTunerSignal = curSignalType.getValue();
        mCbxTunerSignal.setTag(curSignalType);

        LinkedHashMap<String, Object> mapOneSegType = new LinkedHashMap<String, Object>();
        mapOneSegType.put(mParentWnd.getString(R.string.str_off), false);
        mapOneSegType.put(mParentWnd.getString(R.string.str_on), true);
        mOneSegCombox.setData(mapOneSegType);
        mOneSegCombox.setOnSelectChangeListener(this);
        mOneSegFlag = mDtvConfig.getInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_OFF);
        if (CommonValue.ONESEG_FLAG_OFF == mOneSegFlag) {
            mOneSegCombox.setTag(false);
        } else {
            mOneSegCombox.setTag(true);
        }
        if (EnNetworkType.ISDB_TER == mNetworkTypeSelect) {
            mOneSegLayout.setVisibility(View.VISIBLE);
        } else {
            mOneSegLayout.setVisibility(View.GONE);
        }

        LinkedHashMap<String, Object> mapCableFormatType = new LinkedHashMap<String, Object>();
        mapCableFormatType.put(mParentWnd.getString(R.string.atsc_cable_format_auto), EnFrequencyTable.ATSC_AUTO);
        mapCableFormatType.put(mParentWnd.getString(R.string.atsc_cable_format_std), EnFrequencyTable.ATSC_STD);
        mapCableFormatType.put(mParentWnd.getString(R.string.atsc_cable_format_hrc), EnFrequencyTable.ATSC_HRC);
        mapCableFormatType.put(mParentWnd.getString(R.string.atsc_cable_format_irc), EnFrequencyTable.ATSC_IRC);
        mCbxCableFormat.setData(mapCableFormatType);
        mCbxCableFormat.setOnSelectChangeListener(this);
        if (EnNetworkType.ATSC_CAB == mNetworkTypeSelect) {
            mCableFormatLayout.setVisibility(View.VISIBLE);
        } else {
            mCableFormatLayout.setVisibility(View.GONE);
        }

        if (EnScanMode.NONE == mScanModeSelect) {
            if (halApi.isATVSource()) {
                mScanModeSelect = EnScanMode.ATV_ONLY;
                mCbxTunerSignal.setEnabled(false);
                mCbxTunerSignal.setFocusable(false);
                mCbxTunerDevice.setEnabled(false);
                mCbxTunerDevice.setFocusable(false);
                mOneSegCombox.setEnabled(false);
                mOneSegCombox.setFocusable(false);
            } else {
                mScanModeSelect = EnScanMode.DTV_ONLY;
            }
        }
        mCbxScanMode.setTag(mScanModeSelect);

        LogTool.d(LogTool.MSCAN, "initData end to setting install");
        return;
    }

    public EnNetworkType getSelectNetworkType() {
        LogTool.v(LogTool.MSCAN, "getSelectNetworkType=" + mNetworkTypeSelect);
        return mNetworkTypeSelect;
    }

    @Override
    public void show() {
        initData();
        super.show();
        mCbxScanMode.requestFocus();
    }

    @Override
    public void hide() {
        super.hide();
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
    public void onComboxSelectChange(View view, String strText, Object obj, int index) {
        TunerDevice tmpTunerIDToChange = null;

        switch (view.getId()) {
            case R.id.id_dvb_scan_mode:
                mScanModeSelect = (EnScanMode) mCbxScanMode.getTag();
                if (null != mCbxScanMode.getTag()) {
                    if (mCbxScanMode.getTag() == EnScanMode.ATV_ONLY) {
                        mCbxTunerSignal.setEnabled(false);
                        mCbxTunerSignal.setFocusable(false);
                        mCbxTunerDevice.setEnabled(false);
                        mCbxTunerDevice.setFocusable(false);
                        mOneSegCombox.setEnabled(false);
                        mOneSegCombox.setFocusable(false);
                    } else {
                        mCbxTunerDevice.setEnabled(true);
                        mCbxTunerDevice.setFocusable(true);
                        mOneSegCombox.setEnabled(true);
                        mOneSegCombox.setFocusable(true);
                    }
                }
                break;
            case R.id.id_dvb_tuner_signal:
                if (null != mCbxTunerSignal.getTag()) {
                    LogTool.v(LogTool.MSCAN, " onComboxSelectChange id_dvb_tuner_signal");

                    EnNetworkType tmpSignalToChange = (EnNetworkType) mCbxTunerSignal.getTag();

                    if ((null != mNetworkManager) && (null != tmpSignalToChange)) {
                        mNetworkTypeSelect = tmpSignalToChange;

                        if (tmpSignalToChange == EnNetworkType.CABLE && mCableMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mCableMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        } else if (tmpSignalToChange == EnNetworkType.TERRESTRIAL && mTerMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mTerMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        } else if (tmpSignalToChange == EnNetworkType.DTMB && mDtmbMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mDtmbMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        } else if (tmpSignalToChange == EnNetworkType.ISDB_TER && mIsdbtMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mIsdbtMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        } else if (tmpSignalToChange == EnNetworkType.ATSC_T && mAtscTMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mAtscTMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        } else if (tmpSignalToChange == EnNetworkType.ATSC_CAB && mAtscCabMapTunerDevice.size() > 0) {
                            mCbxTunerDevice.setData(mAtscCabMapTunerDevice);
                            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                        }
                        if (EnNetworkType.ISDB_TER == mNetworkTypeSelect) {
                            mOneSegLayout.setVisibility(View.VISIBLE);
                        } else {
                            mOneSegLayout.setVisibility(View.GONE);
                        }
                        if (EnNetworkType.ATSC_CAB == mNetworkTypeSelect) {
                            mCableFormatLayout.setVisibility(View.VISIBLE);
                        } else {
                            mCableFormatLayout.setVisibility(View.GONE);
                        }
                    }
                }
                break;
            case R.id.id_dvb_tuner_device:
                if (null != mCbxTunerDevice.getTag()) {
                    mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
                    LogTool.v(LogTool.MSCAN, "onComboxSelectChange id_dvb_tuner_device");
                }
                break;
            case R.id.id_isdbt_oneseg:
                if (!((Boolean) mOneSegCombox.getTag()) && CommonValue.ONESEG_FLAG_OFF != mOneSegFlag) {
                    mDtvConfig.setInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_OFF);
                    mOneSegFlag = CommonValue.ONESEG_FLAG_OFF;
                } else if ((Boolean) mOneSegCombox.getTag() && CommonValue.ONESEG_FLAG_ON != mOneSegFlag) {
                    mDtvConfig.setInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_ON);
                    mOneSegFlag = CommonValue.ONESEG_FLAG_ON;
                }
                break;
            case R.id.id_auto_scan_mode:
                if (null != mCbxAutoScanMode.getTag()) {
                    if (mCbxAutoScanMode.getTag() == EnAutoScanMode.UPDATE) {
                        mDtvConfig.setInt("u32ScanResultMode", 1);
                    } else if (mCbxAutoScanMode.getTag() == EnAutoScanMode.REINSTALL) {
                        mDtvConfig.setInt("u32ScanResultMode", 3);
                    }
                }
                break;
            case R.id.id_atsc_cab_format:
                if (null != mCbxCableFormat.getTag()) {
                    mFrequencyTable = (EnFrequencyTable) mCbxCableFormat.getTag();
                }
                break;
            case R.id.id_service_cab_format:
                if (null != mServiceTypeCombox.getTag()) {
                    Object tag = mServiceTypeCombox.getTag();
                    if (tag == EnServiceType.ALL){
                        mTVRadioFilter = EnTVRadioFilter.ALL;
                    }else if (tag == EnServiceType.DTV){
                        mTVRadioFilter = EnTVRadioFilter.TV;
                    }else {
                        mTVRadioFilter = EnTVRadioFilter.RADIO;
                    }
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View arg0) {
        EnNetworkType tmpSignalToChange = (EnNetworkType) mCbxTunerSignal.getTag();

        if (null != mCbxTunerDevice.getTag()) {
            mCurrentTunerDeviceID = (Integer) mCbxTunerDevice.getTag();
        }

        if (null != tmpSignalToChange) {
            mCurrentTunerSignal = tmpSignalToChange.getValue();
        }

        if (R.id.id_dvb_tuner_set_btn == arg0.getId()) {
            List<EnNetworkType> lstNetworkType = new ArrayList<EnNetworkType>();
            EnNetworkType enRFNetworkType = EnNetworkType.RF;
            if (EnScanMode.DTV_ONLY == mCbxScanMode.getTag()) {
                if (mNetworkTypeSelect == EnNetworkType.RF) {
                    mNetworkTypeSelect = (EnNetworkType) mCbxTunerSignal.getTag();
                }
                lstNetworkType.add(mNetworkTypeSelect);
            } else if (EnScanMode.ATV_ONLY == mCbxScanMode.getTag()) {
                lstNetworkType.add(enRFNetworkType);
            } else if (EnScanMode.DTV_ATV == mCbxScanMode.getTag()) {
                if (mNetworkTypeSelect == EnNetworkType.RF) {
                    mNetworkTypeSelect = (EnNetworkType) mCbxTunerSignal.getTag();
                }
                lstNetworkType.add(mNetworkTypeSelect);
                lstNetworkType.add(enRFNetworkType);
            }
            ScanType scanType = null;
            if (EnNetworkType.ISDB_TER == mNetworkTypeSelect) {
                scanType = new ScanType();
                scanType.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                scanType.enableNit(false);
                scanType.setFTAFilter(EnScrambleFilter.ALL);
                scanType.setTVRadioFilter(EnTVRadioFilter.ALL);
                if (!((Boolean) mOneSegCombox.getTag())) {
                    scanType.setTransmissionTypeFilter(EnTransmissionTypeFilter.FIX);
                } else {
                    scanType.setTransmissionTypeFilter(EnTransmissionTypeFilter.ALL);
                }
            }
            if (EnNetworkType.ATSC_CAB == mNetworkTypeSelect) {
                scanType = new ScanType();
                scanType.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                scanType.setFrequencyTable(mFrequencyTable);
            }
            if (EnNetworkType.ATSC_T == mNetworkTypeSelect) {
                scanType = new ScanType();
                scanType.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                scanType.setFrequencyTable(EnFrequencyTable.ATSC_IRC);
            }
            ((DTVApplication) mParentWnd.getApplication()).setScanNetworkType(lstNetworkType);
            ((DTVApplication) mParentWnd.getApplication()).setmTvRadioFilter(mTVRadioFilter);
            if (null != scanType) {
                ((DTVApplication) mParentWnd.getApplication()).setScanType(mNetworkTypeSelect, scanType);
            }

            if (null != mScanMainWnd) {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_NEXT_STEP, null);
            }
            if (null != mCbxAutoScanMode.getTag()) {
                if (mCbxAutoScanMode.getTag() == EnAutoScanMode.UPDATE) {
                    mDtvConfig.setInt("u32ScanResultMode", 1);
                } else if (mCbxAutoScanMode.getTag() == EnAutoScanMode.REINSTALL) {
                    mDtvConfig.setInt("u32ScanResultMode", 3);
                }
            }
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
        LogTool.d(LogTool.MINSTALL, "SignalTypeView onKeyDown . KeyCode = " + keyCode);
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

    private class TunerModeInfo {
        public int mSignalType;
        public int mModeStatus;
    }

    private class TunerDevice {
        public int mModeSupportNum;
        public boolean bHasTunerDevice;
        public int mTunerStatus;
        public TunerModeInfo[] mModeInfo = new TunerModeInfo[TUNER_SUPPORTED_MODE_MAXNUM];
    }
}
