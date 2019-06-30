package com.hisilicon.tvui.installtion;


import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.dtv.play.EnStopType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.DigtalEditText;
import com.hisilicon.tvui.view.MyToast;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ATSCManualScanView extends BaseView implements IScanSubWnd, View.OnClickListener, Combox.OnComboxSelectChangeListener, View.OnFocusChangeListener {
    private static final String TAG = "ATSCManualScanView";
    private static final int ATSC_T_FRE_MIN_VALUE = 2;
    private static final int ATSC_T_FRE_MAX_VALUE = 69;
    private static final int ATSC_C_FRE_MIN_VALUE = 1;
    private static final int ATSC_C_FRE_MAX_VALUE = 135;

    private Button mStartScanScanBtn = null;
    private Combox mCbxTunerSignal = null;
    private DigtalEditText mFreqEditText = null;

    private TextView mSignalQualityTextView = null;
    private SeekBar mSignalQualityProgressBar = null;

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;

    private Network mNetwork = null;
    private Multiplex mMultiplex = null;
    private EnNetworkType mCurNetworkType = null;
    private Handler mQualityHandler = new Handler();
    private int currentFreq = 2;

    private static final int MILLISECOND = 1000;

    LinkedHashMap<String, Object> mMapSignalType = null;

    public ATSCManualScanView(BaseActivity arg0) {
        super((LinearLayout) arg0.findViewById(R.id.ly_atsc_manual_scan));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd) arg0;

        initView();
    }

    private void initView() {
        mCbxTunerSignal = mParentWnd.findViewById(R.id.id_atsc_tuner_signal);
        mFreqEditText = mParentWnd.findViewById(R.id.id_atsc_freq);
        mSignalQualityProgressBar = mParentWnd.findViewById(R.id.atscfreq_qaulity_seekbar);
        mSignalQualityTextView = mParentWnd.findViewById(R.id.atscfreq_qaulity_value_txt);
        mStartScanScanBtn = mParentWnd.findViewById(R.id.id_atsc_start_scan_btn);

        mCbxTunerSignal.setOnSelectChangeListener(this);
        mFreqEditText.setOnFocusChangeListener(this);
        mFreqEditText.setOnDigtalEditTextChangeListener(mOnDigtalEditTextChangeListener);
        mFreqEditText.setText(ATSC_T_FRE_MIN_VALUE + "");
        mStartScanScanBtn.setOnClickListener(this);
    }

    private void initData() {

        mMapSignalType = new LinkedHashMap<>();
        mMapSignalType.put(EnNetworkType.ATSC_T.toString(), EnNetworkType.ATSC_T);
        mMapSignalType.put(EnNetworkType.ATSC_CAB.toString(), EnNetworkType.ATSC_CAB);
        mCbxTunerSignal.setData(mMapSignalType);

        mCurNetworkType = mNetworkManager.getCurrentNetworkType();
        mCbxTunerSignal.setTag(mCurNetworkType);

        LogTool.v(LogTool.MSCAN, "initData networkType = " + mCurNetworkType);

        List<Network> mLstNetwork = mNetworkManager.getNetworks(mCurNetworkType);
        if (null != mLstNetwork && mLstNetwork.size() > 0) {
            mNetwork = mLstNetwork.get(0);
            mMultiplex = mNetwork.createTmpMultiplex();
            mMultiplex.setFrequency(currentFreq);
        }

        if (null != mNetwork) {
            mTunerConnect();
        } else {
            LogTool.w(LogTool.MSCAN, " mNetwork is null, please check database");
            MyToast.makeText(mParentWnd, R.string.database_error, MyToast.LENGTH_SHORT).show();
            mStartScanScanBtn.setClickable(false);
        }
    }

    @Override
    public void show() {
        initData();
        if (mQualityHandler.hasCallbacks(mRefreshQuality)) {
            mQualityHandler.removeCallbacks(mRefreshQuality);
        }
        mQualityHandler.post(mRefreshQuality);
        super.show();
    }

    @Override
    public void hide() {
        if (mQualityHandler.hasCallbacks(mRefreshQuality))
            mQualityHandler.removeCallbacks(mRefreshQuality);
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
        if (R.id.id_atsc_start_scan_btn == arg0.getId()) {
            List<EnNetworkType> lstNetworkType = new ArrayList<>();
            lstNetworkType.add(mCurNetworkType);
            ((DTVApplication) mParentWnd.getApplication()).setScanNetworkType(lstNetworkType);

            ScanType type = new ScanType();
            type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
            type.setFrequencyTable(ScanType.EnFrequencyTable.ATSC_AUTO);

            List<Multiplex> lstMultiplex = new ArrayList<>();
            lstMultiplex.add(mMultiplex);

            LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
            mNetwork.setScanMultiplexes(lstMultiplex);

            List<Network> mLstScanNetwork = new ArrayList<>();
            mLstScanNetwork.add(mNetwork);
            ((DTVApplication) mParentWnd.getApplication()).setScanParam(mLstScanNetwork);
            ((DTVApplication) mParentWnd.getApplication()).setScanType(mCurNetworkType, type);

            if (null != mScanMainWnd) {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
            }
        }
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index) {
        if (arg0.getId() == R.id.id_atsc_tuner_signal) {
            if (null != mCbxTunerSignal.getTag()) {
                LogTool.v(LogTool.MSCAN,
                        " onComboxSelectChange id_atsc_tuner_signal");

                EnNetworkType tmpSignalToChange = (EnNetworkType) mCbxTunerSignal
                        .getTag();

                if ((null != mNetworkManager) && (null != tmpSignalToChange)) {
                    mNetworkManager.setCurrentNetworkType(tmpSignalToChange);
                    mCurNetworkType = tmpSignalToChange;

                    List<Network> mLstNetwork = mNetworkManager.getNetworks(mCurNetworkType);
                    {
                        mNetwork = mLstNetwork.get(0);
                    }
                    mMultiplex = mNetwork.createTmpMultiplex();
                    refreshFrequencyView(mCurNetworkType);
                }
            }
        }
    }

    private void refreshFrequencyView(EnNetworkType networkType) {
        String strStrValue = "" + mFreqEditText.getText();
        int value = Integer.parseInt(strStrValue);

        boolean bValidValue = true;
        if (EnNetworkType.ATSC_T == networkType) {
            if (value < ATSC_T_FRE_MIN_VALUE) {
                value = ATSC_T_FRE_MIN_VALUE;
                bValidValue = false;
            } else if (value > ATSC_T_FRE_MAX_VALUE) {
                value = ATSC_T_FRE_MAX_VALUE;
                bValidValue = false;
            }
        } else {
            if (value < ATSC_C_FRE_MIN_VALUE) {
                value = ATSC_C_FRE_MIN_VALUE;
                bValidValue = false;
            } else if (value > ATSC_C_FRE_MAX_VALUE) {
                value = ATSC_C_FRE_MAX_VALUE;
                bValidValue = false;
            }
        }
        currentFreq = value;
        mFreqEditText.setText(currentFreq + "");
        if (!bValidValue) {
            MyToast.makeText(mParentWnd, R.string.str_atsc_validate_input, MyToast.LENGTH_LONG).show();
        }
        LogTool.v(LogTool.MSCAN, "setText " + value);
    }

    @Override
    public void onFocusChange(View arg0, boolean arg1) {
        if (arg1) {
            String strStrValue = "" + mFreqEditText.getText();
            currentFreq = Integer.parseInt(strStrValue);
            LogTool.v(LogTool.MSCAN, "onFocusChange currentFreq= " + currentFreq);
        }

        if (!arg1) {
            int nValue = 0;
            boolean bValidValue = true;

            String strStrValue = "" + mFreqEditText.getText();
            nValue = Integer.parseInt(strStrValue);
            if (mCurNetworkType == EnNetworkType.ATSC_T
                    && ((ATSC_T_FRE_MAX_VALUE < nValue) || (ATSC_T_FRE_MIN_VALUE > nValue))) {
                bValidValue = false;
                mFreqEditText.setText(currentFreq + "");
                LogTool.d(LogTool.MSCAN, "onFocusChange mFreqEditText invalid "
                        + nValue);
            } else if (mCurNetworkType == EnNetworkType.ATSC_CAB
                    && ((ATSC_C_FRE_MAX_VALUE < nValue) || (ATSC_C_FRE_MIN_VALUE > nValue))) {
                bValidValue = false;
                mFreqEditText.setText(currentFreq + "");
                LogTool.d(LogTool.MSCAN, "onFocusChange mFreqEditText invalid "
                        + nValue);
            }

            LogTool.d(LogTool.MSCAN, "onFocusChange nValue=" + nValue);

            if (!bValidValue) {
                MyToast.makeText(mParentWnd, R.string.str_atsc_validate_input, MyToast.LENGTH_LONG).show();
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
        LogTool.d(LogTool.MINSTALL, "ATSCManualScanView keyDispatch . KeyCode = " + keyCode);
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

    private final Runnable mRefreshQuality = new Runnable() {
        @Override
        public void run() {
            setSignalView();
            mQualityHandler.postDelayed(mRefreshQuality, MILLISECOND);
        }
    };

    private void mTunerConnect() {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                Tuner mTuner = mNetwork.getTuner();
                if (null != mTuner) {
                    mTuner.connect(mMultiplex, 0, false);
                    {
                        if (mQualityHandler.hasCallbacks(mRefreshQuality)) {
                            mQualityHandler.removeCallbacks(mRefreshQuality);
                        }
                        mQualityHandler.post(mRefreshQuality);
                    }
                }
            }
        });
    }

    private void setSignalView() {
        Tuner mTuner = mNetwork.getTuner();
        if (null != mTuner) {
            LogTool.v(LogTool.MPLAY, "getModulation = " + mTuner.getModulation());
            int mSignalQuality = mTuner.getSignalQuality();
            mSignalQuality = mSignalQuality > 0 ? (mSignalQuality + getRandom(-3, 3)) : 0;
            mSignalQuality = mSignalQuality < 100 ? mSignalQuality : 100;
            mSignalQuality = mSignalQuality > 0 ? mSignalQuality : 0;
            mSignalQualityTextView.setText(Integer.toString(mSignalQuality) + "%");
            mSignalQualityProgressBar.setProgress(mSignalQuality);
        } else {
            mSignalQualityTextView.setText("0%");
            mSignalQualityProgressBar.setProgress(0);
        }
    }

    private int getRandom(int min, int max) {
        SecureRandom mRandom = new SecureRandom();
        int r = mRandom.nextInt(max - min);
        return r + min;
    }

    DigtalEditText.OnDigtalEditTextChangeListener mOnDigtalEditTextChangeListener = new DigtalEditText.OnDigtalEditTextChangeListener() {
        @Override
        public void onDigtalEditTextChange(DigtalEditText editText,
                                           String strText) {
            LogTool.v(LogTool.MSCAN, " onDigitalEditTextChange text = " + strText);
            if (mMultiplex != null) {
                mMultiplex.setFrequency(Integer.parseInt(strText));
            }

            mPlayer.stop(EnStopType.BLACKSCREEN);
            mPlayer.releaseResource(0);

            if (mNetwork != null) {
                mTunerConnect();
            }
        }

    };
}
