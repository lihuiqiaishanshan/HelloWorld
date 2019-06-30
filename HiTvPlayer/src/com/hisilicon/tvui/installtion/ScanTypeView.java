package com.hisilicon.tvui.installtion;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.hardware.EnModulation;
import com.hisilicon.dtv.network.DVBCChannelDot;
import com.hisilicon.dtv.network.DVBTChannelDot;
import com.hisilicon.dtv.network.DVBTNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.DigtalEditText;
import com.hisilicon.tvui.view.MyToast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ScanTypeView extends BaseView implements IScanSubWnd, OnClickListener,
        OnComboxSelectChangeListener, OnFocusChangeListener {
    private static final String TAG = "ScanTypeView";

    // Frequency, symbol rate minimum setting value.
    private static final int SYMBOLRATE_MIN_VALUE = 900000;
    private static final int SYMBOLRATE_MAX_VALUE = 7200000;
    private static final int FRE_MIN_VALUE = 113000;
    private static final int DEFAULT_FREQ_VALUE = 113;
    private static final int FRE_MAX_VALUE = 866000;
    private static final int MAX_NETWORK_ID = 65535;

    private static final int DEFAULT_FREQ = 698;
    private static final int DEFAULT_RATE = 6875;
    private static final int TP_UNIT_RATE = 1000;

    private static final int FULL_STEP = 8000;
    private static final int BLIND_STEP = 1000;

    private Combox mScanTypeCombox = null;
    private Button mStartScanScanBtn = null;

    private LinearLayout mDvbcLayout = null;
    private EditText mRateEditText = null;
    private Combox mQamCombox = null;

    private LinearLayout mDvbtLayout = null;
    private Combox mFrqCombox = null;
    private Combox mBandwidthCombox = null;

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;

    private int currentFreq = 698;
    private int currentRate = 6875;
    private EnNetworkType mNetworkTypeSelect = EnNetworkType.CABLE;
    // networkId and frequency about
    private LinearLayout networkIdLayout;
    private LinearLayout frequencyLayout;
    private TextView tvNid;
    private EditText etNid = null;
    private InputDialog dialog = null;
    private TextView tvFrequency;
    private TextView tvFreqUnit;

    public enum EnDvbcAutoScanType {
        DVBC_NIT_SCAN,
        DVBC_FULL_SCAN,
        DVBC_BLIND_SCAN,
        DVBC_QUICK_SCAN
    }

    public enum EnDvbtAutoScanType {
        DVBT_NIT_SCAN,
        DVBT_FULL_SCAN
    }

    public ScanTypeView(BaseActivity arg0) {
        super((LinearLayout) arg0.findViewById(R.id.ly_auto_scan_type));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd) arg0;

        mDvbcLayout = mParentWnd.findViewById(R.id.dvbc_type_select_lay);
        mDvbtLayout = mParentWnd.findViewById(R.id.dvbt_type_select_lay);
        mStartScanScanBtn = mParentWnd.findViewById(R.id.id_scan_type_btn);
        networkIdLayout = mParentWnd.findViewById(R.id.ll_dvbc_nid);
        frequencyLayout = mParentWnd.findViewById(R.id.ll_dvbc_freq);
        tvNid = mParentWnd.findViewById(R.id.tv_dvbc_nid);
        tvNid.setOnClickListener(this);
        tvFrequency = mParentWnd.findViewById(R.id.tv_dvbc_freq);
        tvFrequency.setOnClickListener(this);
        tvFreqUnit = mParentWnd.findViewById(R.id.tv_dvbc_freq_unit);

    }

    private void initDvbcView() {
        mScanTypeCombox = mParentWnd.findViewById(R.id.id_dvbc_scan_type_combox);

        mRateEditText = (DigtalEditText) mParentWnd.findViewById(R.id.id_dvbc_rate);
        mQamCombox = mParentWnd.findViewById(R.id.id_dvbc_qam);

        LinkedHashMap<String, Object> mapScanType = new LinkedHashMap<>();
        mapScanType.put(mParentWnd.getString(R.string.str_install_nit_scan), EnDvbcAutoScanType.DVBC_NIT_SCAN);
        mapScanType.put(mParentWnd.getString(R.string.str_install_quick_scan), EnDvbcAutoScanType.DVBC_QUICK_SCAN);
        mapScanType.put(mParentWnd.getString(R.string.str_install_blind_scan), EnDvbcAutoScanType.DVBC_BLIND_SCAN);
        mapScanType.put(mParentWnd.getString(R.string.str_install_full_scan), EnDvbcAutoScanType.DVBC_FULL_SCAN);

        mScanTypeCombox.setData(mapScanType);
        mScanTypeCombox.setTag(EnDvbcAutoScanType.DVBC_FULL_SCAN);
        mScanTypeCombox.setOnSelectChangeListener(this);

        mStartScanScanBtn.setOnClickListener(this);
        mRateEditText.setOnFocusChangeListener(this);
        mRateEditText.setText(DEFAULT_RATE + "");

        LinkedHashMap<String, Object> mapQamType = new LinkedHashMap<>();
        mapQamType.put(EnModulation.QAM16.toString(), EnModulation.QAM16);
        mapQamType.put(EnModulation.QAM32.toString(), EnModulation.QAM32);
        mapQamType.put(EnModulation.QAM64.toString(), EnModulation.QAM64);
        mapQamType.put(EnModulation.QAM128.toString(), EnModulation.QAM128);
        mapQamType.put(EnModulation.QAM256.toString(), EnModulation.QAM256);
        mQamCombox.setData(mapQamType);
        mQamCombox.setTag(EnModulation.QAM64);
        mQamCombox.setOnSelectChangeListener(this);

        if (EnDvbcAutoScanType.DVBC_NIT_SCAN == mScanTypeCombox.getTag()
                || EnDvbcAutoScanType.DVBC_QUICK_SCAN == mScanTypeCombox.getTag()) {
            mQamCombox.setEnabled(true);
            mQamCombox.setFocusable(true);
            mRateEditText.setEnabled(true);
            mRateEditText.setFocusable(true);
            //nit and quick can choose networkId and Frequency
            networkIdLayout.setVisibility(View.VISIBLE);
            frequencyLayout.setVisibility(View.VISIBLE);
            tvNid.setEnabled(true);
            tvNid.setFocusable(true);
            tvFrequency.setEnabled(true);
            tvFrequency.setFocusable(true);
        } else {
            mQamCombox.setEnabled(false);
            mQamCombox.setFocusable(false);
            mRateEditText.setEnabled(false);
            mRateEditText.setFocusable(false);

            networkIdLayout.setVisibility(View.GONE);
            frequencyLayout.setVisibility(View.GONE);
            tvNid.setEnabled(false);
            tvNid.setFocusable(false);
            tvFrequency.setEnabled(false);
            tvFrequency.setFocusable(false);
        }
    }

    private void initDvbtView() {
        mScanTypeCombox = mParentWnd.findViewById(R.id.id_dvbt_scan_type_combox);
        mFrqCombox = mParentWnd.findViewById(R.id.id_dvbt_frmindex);
        mBandwidthCombox = mParentWnd.findViewById(R.id.id_dvbt_scan_bandwidth_combox);

        mStartScanScanBtn.setOnClickListener(this);

        LinkedHashMap<String, Object> mapScanType = new LinkedHashMap<>();
        mapScanType.put(mParentWnd.getString(R.string.str_install_nit_scan), EnDvbtAutoScanType.DVBT_NIT_SCAN);
        mapScanType.put(mParentWnd.getString(R.string.str_install_full_scan), EnDvbtAutoScanType.DVBT_FULL_SCAN);

        mScanTypeCombox.setData(mapScanType);
        mScanTypeCombox.setTag(EnDvbtAutoScanType.DVBT_FULL_SCAN);
        mScanTypeCombox.setOnSelectChangeListener(this);

        LinkedHashMap<String, Object> mapBandwidthType = new LinkedHashMap<>();
        mapBandwidthType.put("5000 KHz", EnBandwidth.BW5000);
        mapBandwidthType.put("6000 KHz", EnBandwidth.BW6000);
        mapBandwidthType.put("7000 KHz", EnBandwidth.BW7000);
        mapBandwidthType.put("8000 KHz", EnBandwidth.BW8000);
        mBandwidthCombox.setData(mapBandwidthType);
        mBandwidthCombox.setOnSelectChangeListener(this);
        mBandwidthCombox.setTag(EnBandwidth.BW7000);

        DVBTNetwork mDVBTNetwork = null;
        List<Network> mLstNetwork = mNetworkManager.getNetworks(mNetworkTypeSelect, mDTV.getCountry());

        String strForm = mParentWnd.getString(R.string.str_install_dvbt_id);
        String strTip = null;
        if (null == mLstNetwork || mLstNetwork.size() <= 0) {
            mLstNetwork = mNetworkManager.getNetworks(mNetworkTypeSelect);
        }

        if (null != mLstNetwork && mLstNetwork.size() > 0) {
            mDVBTNetwork = (DVBTNetwork) mLstNetwork.get(0);

            if (null != mDVBTNetwork) {
                LinkedHashMap<String, Object> mapFreq = new LinkedHashMap<String, Object>();
                List<Multiplex> listMult = mDVBTNetwork.getPresetMultiplexes();
                if (listMult.size() > 0) {
                    DVBTChannelDot mCrtMultiplex = (DVBTChannelDot) listMult.get(0);
                    mBandwidthCombox.setTag(EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()));

                    LogTool.d(LogTool.MSCAN, "dtvChDot getFrequency= =" + mCrtMultiplex.getFrequency() + "  " + mCrtMultiplex.getVersion() + " "
                            + mCrtMultiplex.getBandWidth() + " " + EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()) + " " + mCrtMultiplex.getModulation());

                    for (int i = 0; i < listMult.size(); i++) {
                        Multiplex obj = listMult.get(i);
                        strTip = String.format(strForm, i, obj.getFrequency());
                        mapFreq.put(strTip, obj);
                        LogTool.d(LogTool.MSCAN, "fre id=" + obj.getID() + ",frm=" + obj.getFrequency());
                    }

                    mFrqCombox.setData(mapFreq);
                    mFrqCombox.setTag(mCrtMultiplex);
                    mBandwidthCombox.setTag(EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()));
                }
            }
            mFrqCombox.setOnSelectChangeListener(this);

            if (EnDvbtAutoScanType.DVBT_NIT_SCAN == mScanTypeCombox.getTag()) {
                mFrqCombox.setEnabled(true);
                mFrqCombox.setFocusable(true);
                mBandwidthCombox.setEnabled(true);
            } else {
                mFrqCombox.setEnabled(false);
                mFrqCombox.setFocusable(false);
                mBandwidthCombox.setEnabled(false);
            }
        } else {
            LogTool.w(LogTool.MSCAN, " mDVBTNetwork is null, please check database=");
            MyToast.makeText(mParentWnd, R.string.database_error, MyToast.LENGTH_SHORT).show();
            mStartScanScanBtn.setClickable(false);
        }
    }


    public void setScanNetworkType(EnNetworkType networkType) {
        mNetworkTypeSelect = networkType;
    }

    @Override
    public void show() {
        super.show();

        if (EnNetworkType.CABLE == mNetworkTypeSelect) {
            mDvbtLayout.setVisibility(View.GONE);
            mDvbcLayout.setVisibility(View.VISIBLE);
            initDvbcView();
        } else {
            mDvbcLayout.setVisibility(View.GONE);
            mDvbtLayout.setVisibility(View.VISIBLE);
            initDvbtView();
        }
        mScanTypeCombox.requestFocus();
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
    public void onClick(View arg0) {
        String countryCode = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "MYS");
        if (R.id.id_scan_type_btn == arg0.getId()) {
            if (!checkInputValid(mNetworkTypeSelect)) {
                MyToast.makeText(mParentWnd, mParentWnd.getString(R.string.str_dvbc_validate_input), MyToast.LENGTH_LONG).show();
                return;
            }

            ScanType type = new ScanType();

            type.setFTAFilter(EnScrambleFilter.ALL);
            type.setTVRadioFilter(((DTVApplication) mParentWnd.getApplication()).getmTvRadioFilter());
            // 1st, get network from Application
            List<Network> lstScanNetwok = ((DTVApplication) mParentWnd.getApplication()).getScanParamNetwork();
            Network dvbNetwork = null;

            int i = 0;
            if (null != lstScanNetwok && lstScanNetwok.size() > 0) {
                for (i = 0; i < lstScanNetwok.size(); i++) {
                    Network network = lstScanNetwok.get(i);
                    if (network.getNetworkType() == mNetworkTypeSelect) {
                        dvbNetwork = network;
                        break;
                    }
                }
            }

            if (null == dvbNetwork) {
                // 2nd, if failed in 1st, get network by country
                List<Network> mLstNetwork = mNetworkManager.getNetworks(mNetworkTypeSelect, mDTV.getCountry());
                if (null == mLstNetwork || mLstNetwork.size() <= 0) {
                    // 3rd, if failed in 2nd, get first network from all network list
                    mLstNetwork = mNetworkManager.getNetworks(mNetworkTypeSelect);
                }

                if (null != mLstNetwork && mLstNetwork.size() > 0) {
                    dvbNetwork = mLstNetwork.get(0);
                }
            }

            if (null != dvbNetwork) {
                if (EnDvbtAutoScanType.DVBT_NIT_SCAN == mScanTypeCombox.getTag()) {
                    type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                    type.enableNit(true);
                    setNitScanParam(mNetworkTypeSelect, dvbNetwork);
                } else if (EnDvbcAutoScanType.DVBC_NIT_SCAN == mScanTypeCombox.getTag()) {
                    type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                    type.enableNit(true);
                    setNitScanParam(mNetworkTypeSelect, dvbNetwork);
                } else if (EnDvbcAutoScanType.DVBC_FULL_SCAN == mScanTypeCombox.getTag()) {
                    if (countryCode.equalsIgnoreCase("CHN")) {
                        type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                        type.enableNit(false);
                    } else {
                        type.setBaseType(ScanType.EnBaseScanType.STEP);
                        type.enableNit(false);
                        type.setFrequencyStep(FULL_STEP);
                        setDvbcStepScanParam(dvbNetwork);
                    }
                } else if (EnDvbcAutoScanType.DVBC_QUICK_SCAN == mScanTypeCombox.getTag()) {
                    List<Multiplex> lstMultiplex = new ArrayList<>();
                    String strSymRate = mRateEditText.getText().toString();
                    EnModulation enMod = (EnModulation) mQamCombox.getTag();
                    int symRate = Integer.parseInt(strSymRate) * TP_UNIT_RATE;
                    DVBCChannelDot mDvbcMultiplex = (DVBCChannelDot) dvbNetwork.createTmpMultiplex();
                    mDvbcMultiplex.setSymbolRate(symRate);
                    mDvbcMultiplex.setModulation(enMod);
                    lstMultiplex.add(mDvbcMultiplex);
                    // Auto and specific frequency points correspond to
                    // AUTO_FULL_FIRST and SINGLE_MULTI respectively
                    if (tvFrequency.getText().toString().trim()
                            .equals(mParentWnd.getString(R.string.atsc_cable_format_auto))) {
                        type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL_FIRST);
                    } else {
                        type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
                        mDvbcMultiplex.setFrequency(Integer.parseInt(tvFrequency.getText().toString().trim()) * TP_UNIT_RATE);
                    }
                    // If networkId is not set, there is no need to setType
                    if (!tvNid.getText().toString().trim()
                            .equals(mParentWnd.getString(R.string.atsc_cable_format_auto))) {
                        type.setNetworkId(Integer.parseInt(tvNid.getText().toString().trim()));
                    }
                    // quick scan need open sdt scan
                    type.enableSdt(true);
                    type.enableNit(false);
                    dvbNetwork.setScanMultiplexes(lstMultiplex);
                } else if (EnDvbcAutoScanType.DVBC_BLIND_SCAN == mScanTypeCombox.getTag()) {
                    type.setBaseType(ScanType.EnBaseScanType.STEP);
                    type.enableNit(false);
                    type.setFrequencyStep(BLIND_STEP);
                    setDvbcStepScanParam(dvbNetwork);
                } else if (EnDvbtAutoScanType.DVBT_FULL_SCAN == mScanTypeCombox.getTag()) {
                    type.setBaseType(ScanType.EnBaseScanType.AUTO_FULL);
                    type.enableNit(false);
                }

                if (null == lstScanNetwok || lstScanNetwok.size() <= 0) {
                    lstScanNetwok = new ArrayList<>();
                    lstScanNetwok.add(dvbNetwork);
                    ((DTVApplication) mParentWnd.getApplication()).setScanParam(lstScanNetwok);
                }

                ((DTVApplication) mParentWnd.getApplication()).setScanType(dvbNetwork.getNetworkType(), type);
            }

            if (null != mScanMainWnd) {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_NEXT_STEP, null);
            }
        }
        if (tvFrequency == arg0) {
            initInputDialog(true);
        }
        if (tvNid == arg0) {
            initInputDialog(false);
        }

    }

    private void initInputDialog(final boolean isFreq) {
        View view = View.inflate(mParentWnd, R.layout.dialog_scan_type_nid, null);
        dialog = new InputDialog(mParentWnd, view, R.style.inputDialog);
        dialog.setCancelable(false);
        dialog.show();
        etNid = view.findViewById(R.id.id_dvbc_nid_source);
        Button cancel = view.findViewById(R.id.btn_dialog_cancel);
        Button confirm = view.findViewById(R.id.btn_dialog_confirm);
        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(etNid.getText())) {
                    dialog.dismiss();
                    return;
                }
                if (isFreq) {
                    String strFreq = etNid.getText().toString();
                    int freq = Integer.parseInt(strFreq) * TP_UNIT_RATE;
                    if ((FRE_MAX_VALUE < freq) || (FRE_MIN_VALUE > freq)) {
                        Toast.makeText(mParentWnd, mParentWnd.getString(R.string.str_dvbc_validate_input), Toast.LENGTH_SHORT).show();
                        etNid.setText("" + DEFAULT_FREQ_VALUE);
                    }
                    tvFrequency.setText(etNid.getText().toString().trim());
                    tvFreqUnit.setVisibility(View.VISIBLE);
                } else {
                    int temporaryNid = Integer.parseInt(etNid.getText().toString().trim());
                    if (temporaryNid < 0 || temporaryNid > MAX_NETWORK_ID) {
                        Toast.makeText(mParentWnd, mParentWnd.getString(R.string.str_dvbc_nid_input), Toast.LENGTH_SHORT).show();
                        etNid.setText("0");
                    }
                    tvNid.setText(etNid.getText().toString().trim());
                }
                dialog.dismiss();
            }
        });
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        if (R.id.id_dvbc_scan_type_combox == arg0.getId()) {
            if (EnDvbcAutoScanType.DVBC_NIT_SCAN == mScanTypeCombox.getTag() ||
                    EnDvbcAutoScanType.DVBC_QUICK_SCAN == mScanTypeCombox.getTag()) {
                mQamCombox.setEnabled(true);
                mQamCombox.setFocusable(true);
                mRateEditText.setEnabled(true);
                mRateEditText.setFocusable(true);

                //nit and quick can choose networkId and Frequency
                networkIdLayout.setVisibility(View.VISIBLE);
                frequencyLayout.setVisibility(View.VISIBLE);
                tvNid.setEnabled(true);
                tvNid.setFocusable(true);
                tvFrequency.setEnabled(true);
                tvFrequency.setFocusable(true);
            } else {
                mQamCombox.setEnabled(false);
                mQamCombox.setFocusable(false);
                mRateEditText.setEnabled(false);
                mRateEditText.setFocusable(false);
                networkIdLayout.setVisibility(View.GONE);
                frequencyLayout.setVisibility(View.GONE);
                tvNid.setEnabled(false);
                tvNid.setFocusable(false);
                tvFrequency.setEnabled(false);
                tvFrequency.setFocusable(false);
            }
        } else if (R.id.id_dvbt_scan_type_combox == arg0.getId()) {
            if (EnDvbtAutoScanType.DVBT_NIT_SCAN == mScanTypeCombox.getTag()) {
                mFrqCombox.setEnabled(true);
                mFrqCombox.setFocusable(true);
                mBandwidthCombox.setEnabled(false);
                mBandwidthCombox.setFocusable(false);
            } else {
                mFrqCombox.setEnabled(false);
                mFrqCombox.setFocusable(false);
                mBandwidthCombox.setEnabled(false);
            }
        } else if (R.id.id_dvbt_frmindex == arg0.getId()) {
            Multiplex mult = (Multiplex) mFrqCombox.getTag();
            LogTool.d(LogTool.MSCAN, "id_dvbt_frmindex obj=" + mult);
            if (null != mult) {
                DVBTChannelDot crtMultiplex = (DVBTChannelDot) mult;
                mBandwidthCombox.setTag(EnBandwidth.valueOf(crtMultiplex.getBandWidth()));
            }
        }
    }

    @Override
    public void onFocusChange(View arg0, boolean arg1) {
        if (arg1) {
            if (arg0.getId() == R.id.id_dvbc_rate) {
                String strStrValue = "" + mRateEditText.getText();
                currentRate = Integer.parseInt(strStrValue);
            } else {
                return;
            }
            LogTool.d(LogTool.MSCAN, "onFocusChange currentFreq= " + currentFreq + "onFocusChange currentRate= " + currentRate);
        }

        if (!arg1) {
            int nValue = 0;
            boolean bValidValue = true;
            if (arg0.getId() == R.id.id_dvbc_rate) {
                String strStrValue = "" + mRateEditText.getText();
                nValue = Integer.parseInt(strStrValue) * TP_UNIT_RATE;
                if ((SYMBOLRATE_MAX_VALUE < nValue) || (SYMBOLRATE_MIN_VALUE > nValue)) {
                    bValidValue = false;
                    mRateEditText.setText(currentRate + "");
                    LogTool.d(LogTool.MSCAN, "mRateEditText invalid " + nValue);
                }
            } else {
                return;
            }
            LogTool.d(LogTool.MSCAN, "mRateEditText nValue=" + nValue);

            if (!bValidValue) {
                MyToast.makeText(mParentWnd, R.string.str_dvbc_validate_input, MyToast.LENGTH_LONG).show();
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

    private void editFreq(boolean isAdd) {
        String freqSource = tvFrequency.getText().toString().trim();
        int frequency = freqSource.equals(mParentWnd.getString(R.string.atsc_cable_format_auto)) ? 0 : Integer.parseInt(freqSource);
        if (isAdd) {
            frequency++;
            if (frequency * TP_UNIT_RATE > FRE_MAX_VALUE) {
                tvFrequency.setText(mParentWnd.getString(R.string.atsc_cable_format_auto));
                tvFreqUnit.setVisibility(View.GONE);
            } else {
                tvFrequency.setText(frequency + "");
                tvFreqUnit.setVisibility(View.VISIBLE);
            }
        } else {
            frequency--;
            if (frequency * TP_UNIT_RATE < FRE_MIN_VALUE) {
                tvFrequency.setText(mParentWnd.getString(R.string.atsc_cable_format_auto));
                tvFreqUnit.setVisibility(View.GONE);
            } else {
                tvFrequency.setText(frequency + "");
                tvFreqUnit.setVisibility(View.VISIBLE);
            }
        }
    }

    private void editNid(boolean isAdd) {
        String nidSource = tvNid.getText().toString().trim();
        int nid = nidSource.equals(mParentWnd.getString(R.string.atsc_cable_format_auto)) ? 0 : Integer.parseInt(nidSource);
        if (isAdd) {
            nid++;
            if (nid > MAX_NETWORK_ID) {
                tvNid.setText(mParentWnd.getString(R.string.atsc_cable_format_auto));
            } else {
                tvNid.setText(nid + "");
            }
        } else {
            nid--;
            if (nid < 0) {
                tvNid.setText(mParentWnd.getString(R.string.atsc_cable_format_auto));
            } else {
                tvNid.setText(nid + "");
            }
        }
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mScanMainWnd = parent;
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        LogTool.d(LogTool.MINSTALL, "ScanTypeView onKeyDown . KeyCode = " + keyCode);
        switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_DPAD_LEFT: {
                if (tvNid.isFocused()) {
                    editNid(false);
                } else if (tvFrequency.isFocused()) {
                    editFreq(false);
                }
                return KeyDoResult.DO_OVER;
            }
            case KeyValue.DTV_KEYVALUE_DPAD_RIGHT: {
                if (tvNid.isFocused()) {
                    editNid(true);
                } else if (tvFrequency.isFocused()) {
                    editFreq(true);
                }
                return KeyDoResult.DO_OVER;
            }
            default:
                break;
        }
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

    boolean checkInputValid(EnNetworkType networkType) {
        if (EnNetworkType.CABLE == networkType) {
            String strFreq = tvFrequency.getText().toString();
            if (strFreq.equals(mParentWnd.getString(R.string.atsc_cable_format_auto))) {
                strFreq = DEFAULT_FREQ + "";
            }
            String strSymRate = mRateEditText.getText().toString();
            int freq = Integer.parseInt(strFreq) * TP_UNIT_RATE;
            int symRate = Integer.parseInt(strSymRate) * TP_UNIT_RATE;
            return (SYMBOLRATE_MAX_VALUE >= symRate) && (SYMBOLRATE_MIN_VALUE <= symRate)
                    && (FRE_MAX_VALUE >= freq) && (FRE_MIN_VALUE <= freq);
        }
        return true;
    }

    private void setNitScanParam(EnNetworkType networkType, Network network) {
        if (null == network) {
            return;
        }

        if (EnNetworkType.CABLE != networkType && EnNetworkType.TERRESTRIAL != networkType) {
            return;
        }

        List<Multiplex> lstMultiplex = new ArrayList<>();

        if (EnNetworkType.CABLE == networkType) {
            String strSymRate = mRateEditText.getText().toString();
            EnModulation enMod = (EnModulation) mQamCombox.getTag();

            int freq = DEFAULT_FREQ * TP_UNIT_RATE;
            int symRate = Integer.parseInt(strSymRate) * TP_UNIT_RATE;

            DVBCChannelDot mDvbcMultiplex = (DVBCChannelDot) network.createTmpMultiplex();
            mDvbcMultiplex.setFrequency(freq);
            mDvbcMultiplex.setSymbolRate(symRate);
            mDvbcMultiplex.setModulation(enMod);
            lstMultiplex.add(mDvbcMultiplex);
        } else if (EnNetworkType.TERRESTRIAL == networkType) {
            Multiplex multiplx = (Multiplex) mFrqCombox.getTag();
            lstMultiplex.add(multiplx);
        }

        LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
        network.setScanMultiplexes(lstMultiplex);
    }

    private void setDvbcStepScanParam(Network dvbcNetwork) {
        DVBCChannelDot mStartMultiplex = null;
        DVBCChannelDot mEndMultiplex = null;

        if (null != dvbcNetwork) {
            String strSymRate = mRateEditText.getText().toString();
            EnModulation enMod = (EnModulation) mQamCombox.getTag();
            int symRate = Integer.parseInt(strSymRate) * TP_UNIT_RATE;

            List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();

            mStartMultiplex = (DVBCChannelDot) dvbcNetwork.createTmpMultiplex();
            mStartMultiplex.setFrequency(FRE_MIN_VALUE);
            mStartMultiplex.setSymbolRate(symRate);
            mStartMultiplex.setModulation(enMod);
            lstMultiplex.add(mStartMultiplex);

            mEndMultiplex = (DVBCChannelDot) dvbcNetwork.createTmpMultiplex();
            mEndMultiplex.setFrequency(FRE_MAX_VALUE);
            mEndMultiplex.setSymbolRate(symRate);
            mEndMultiplex.setModulation(enMod);
            lstMultiplex.add(mEndMultiplex);

            LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
            dvbcNetwork.setScanMultiplexes(lstMultiplex);
        }
    }

}
