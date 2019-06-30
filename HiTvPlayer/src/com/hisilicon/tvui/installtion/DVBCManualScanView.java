package com.hisilicon.tvui.installtion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.security.SecureRandom;

import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.hardware.EnModulation;
import com.hisilicon.dtv.hardware.EnTunerStatus;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.DVBCChannelDot;
import com.hisilicon.dtv.network.DVBCNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.DigtalEditText;
import com.hisilicon.tvui.view.DigtalEditText.OnDigtalEditTextChangeListener;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.dtv.play.EnStopType;


public class DVBCManualScanView extends BaseView implements IScanSubWnd, OnClickListener, OnFocusChangeListener, OnComboxSelectChangeListener
{
    private static final String TAG = "DVBCManualScanView";

    private static final int SYMBOLRATE_MIN_VALUE = 900000;
    private static final int SYMBOLRATE_MAX_VALUE = 7200000;
    private static final int FRE_MIN_VALUE = 113000;
    private static final int FRE_MAX_VALUE = 866000;

    private static final int DEFAULT_FREQ = 698;
    private static final int DEFAULT_RATE = 6875;
    private static final int TP_UNIT_RATE = 1000;

    private static final int MILLISECONDSECOND = 1000;

    private DigtalEditText mFreqEditText = null;
    private DigtalEditText mRateEditText = null;
    private Combox mQamCombox = null;
    private TextView mSignalQualityTextView = null;
    private SeekBar  mSignalQualityProgressBar = null;

    private BaseActivity mParentWnd = null;
    private IScanMainWnd mScanMainWnd = null;

    private int currentFreq = 698;
    private int currentRate = 6875;
    DVBCNetwork   mDvbcNetwork = null;
    DVBCChannelDot mCrtMultiplex = null;
    private Handler mQualityHandler = new Handler();

    public DVBCManualScanView(BaseActivity arg0)
    {
        super((LinearLayout)arg0.findViewById(R.id.ly_dvbc_manual_scan));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd)arg0;

        initView();
    }

    private void initView()
    {
        Button mStartScanScanBtn = (Button) mParentWnd.findViewById(R.id.id_dvbc_start_scan_btn);
        mFreqEditText = (DigtalEditText)mParentWnd.findViewById(R.id.id_dvbc_freq);
        mRateEditText = (DigtalEditText)mParentWnd.findViewById(R.id.id_dvbc_rate);
        mQamCombox = (Combox)mParentWnd.findViewById(R.id.id_dvbc_qam);
        mSignalQualityProgressBar = (SeekBar)mParentWnd.findViewById(R.id.dvbcfreq_qaulity_seekbar);
        mSignalQualityTextView = (TextView)mParentWnd.findViewById(R.id.dvbcfreq_qaulity_value_txt);
        mStartScanScanBtn.setOnClickListener(this);
        mFreqEditText.setOnFocusChangeListener(this);
        mRateEditText.setOnFocusChangeListener(this);
        mFreqEditText.setOnDigtalEditTextChangeListener(mOnDigtalEditTextChangeListener);
        mRateEditText.setOnDigtalEditTextChangeListener(mOnDigtalEditTextChangeListener);
        mFreqEditText.setText(DEFAULT_FREQ + "");
        mRateEditText.setText(DEFAULT_RATE + "");
        LinkedHashMap<String, Object> mapQamType = new LinkedHashMap<String, Object>();
        mapQamType.put(EnModulation.QAM16.toString(), EnModulation.QAM16);
        mapQamType.put(EnModulation.QAM32.toString(), EnModulation.QAM32);
        mapQamType.put(EnModulation.QAM64.toString(), EnModulation.QAM64);
        mapQamType.put(EnModulation.QAM128.toString(), EnModulation.QAM128);
        mapQamType.put(EnModulation.QAM256.toString(), EnModulation.QAM256);
        mQamCombox.setData(mapQamType);
        mQamCombox.setTag(EnModulation.QAM64);
        mQamCombox.setOnSelectChangeListener(this);
    }

    private void initData()
    {
        List<Network> mLstNetwork = mNetworkManager.getNetworks(EnNetworkType.CABLE, mDTV.getCountry());
        if (null == mLstNetwork || mLstNetwork.size() <= 0)
        {
            mLstNetwork = mNetworkManager.getNetworks(EnNetworkType.CABLE);
        }

        if (null != mLstNetwork && mLstNetwork.size() > 0)
        {
            mDvbcNetwork = (DVBCNetwork)mLstNetwork.get(0);

            Channel mcurChannel = mChnHistory.getCurrentChn(halApi.EnumSourceIndex.SOURCE_DVBC);
            if(mcurChannel != null && mcurChannel.getNetworkType() == EnNetworkType.CABLE)
            {
                currentFreq = mcurChannel.getBelongMultiplexe().getFrequency()/1000;
                currentRate = ((DVBCChannelDot)mcurChannel.getBelongMultiplexe()).getSymbolRate()/1000;
                mFreqEditText.setText(currentFreq + "");
                mRateEditText.setText(currentRate + "");
            }
            if (mDvbcNetwork != null)
            {
                mCrtMultiplex = (DVBCChannelDot) mDvbcNetwork.createTmpMultiplex();
                mCrtMultiplex.setFrequency(currentFreq * 1000);
                mCrtMultiplex.setSymbolRate(currentRate * 1000);
                mCrtMultiplex.setModulation(EnModulation.QAM64);
                mTunerConnect();
            }
        }
        else
        {
            LogTool.w(LogTool.MSCAN, "mDVBTNetwork is null, please check database"  );
            MyToast.makeText(mParentWnd, R.string.database_error, MyToast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void show()
    {
        initData();
        if(mQualityHandler.hasCallbacks(mRefreshQuality))
        {
            mQualityHandler.removeCallbacks(mRefreshQuality);
        }
        mQualityHandler.post(mRefreshQuality);
        super.show();
    }

    @Override
    public void hide()
    {
        if(mQualityHandler.hasCallbacks(mRefreshQuality))
        {
            mQualityHandler.removeCallbacks(mRefreshQuality);
        }
        super.hide();
    }

    @Override
    public void toggle()
    {
        if (super.isShow())
        {
            hide();
        }
        else
        {
            show();
        }
    }

    @Override
    public void onClick(View arg0)
    {
        if (R.id.id_dvbc_start_scan_btn == arg0.getId())
        {
            String strFreq = mFreqEditText.getText().toString();
            String strSymRate = mRateEditText.getText().toString();
            EnModulation enMod = (EnModulation)mQamCombox.getTag();

            int freq = Integer.parseInt(strFreq) * TP_UNIT_RATE;
            int symRate = Integer.parseInt(strSymRate) * TP_UNIT_RATE;

            if ((SYMBOLRATE_MAX_VALUE < symRate) || (SYMBOLRATE_MIN_VALUE > symRate)
                    || (FRE_MAX_VALUE < freq) || (FRE_MIN_VALUE > freq))
            {
                MyToast.makeText(mParentWnd, mParentWnd.getString(R.string.str_dvbc_validate_input), MyToast.LENGTH_LONG).show();
                return;
            }

            List<EnNetworkType> lstNetworkType = new ArrayList<EnNetworkType>();
            lstNetworkType.add(EnNetworkType.CABLE);
            ((DTVApplication)mParentWnd.getApplication()).setScanNetworkType(lstNetworkType);

            ScanType type = new ScanType();
            type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
            type.enableNit(false);
            type.setFTAFilter(EnScrambleFilter.ALL);
            type.setTVRadioFilter(EnTVRadioFilter.ALL);

            if ((null != mDvbcNetwork) && (null != mCrtMultiplex))
            {
                mCrtMultiplex.setFrequency(freq);
                mCrtMultiplex.setSymbolRate(symRate);
                mCrtMultiplex.setModulation(enMod);

                List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();
                lstMultiplex.add(mCrtMultiplex);
                LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
                mDvbcNetwork.setScanMultiplexes(lstMultiplex);

                List<Network> mLstScanNetwork = new ArrayList<Network>();
                mLstScanNetwork.add(mDvbcNetwork);
                ((DTVApplication) mParentWnd.getApplication()).setScanParam(mLstScanNetwork);
                ((DTVApplication)mParentWnd.getApplication()).setScanType(EnNetworkType.CABLE, type);
            }

            if (null != mScanMainWnd)
            {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
            }
        }
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index)
    {
        if (R.id.id_dvbc_qam == arg0.getId())
        {
            if (null != mQamCombox.getTag())
            {
                LogTool.d(LogTool.MSCAN, "id_dvbc_qam =" + ((EnModulation) mQamCombox.getTag()).getValue());
                mCrtMultiplex.setModulation((EnModulation) mQamCombox.getTag());
                mTunerConnect();
            }
        }
    }

    @Override
    public void onFocusChange(View arg0, boolean arg1)
    {
        if (arg1)
        {
            if (arg0.getId() == R.id.id_dvbc_freq)
            {
                String strStrValue = "" + mFreqEditText.getText();
                currentFreq = Integer.parseInt(strStrValue);
            }
            else if (arg0.getId() == R.id.id_dvbc_rate)
            {
                String strStrValue = "" + mRateEditText.getText();
                currentRate = Integer.parseInt(strStrValue);
            }
            else
            {
                return;
            }
            LogTool.v(LogTool.MSCAN, "onFocusChange currentFreq= " + currentFreq + "onFocusChange currentRate= " + currentRate);
        }

        if (!arg1)
        {
            int nValue = 0;
            boolean bValidValue = true;
            if (arg0.getId() == R.id.id_dvbc_freq)
            {
                String strStrValue = "" + mFreqEditText.getText();
                nValue = Integer.parseInt(strStrValue) * TP_UNIT_RATE;
                if ((FRE_MAX_VALUE < nValue) || (FRE_MIN_VALUE > nValue))
                {
                    bValidValue = false;
                    mFreqEditText.setText(currentFreq + "");
                    LogTool.d(LogTool.MSCAN, "onFocusChange mFreqEditText invalid " + nValue);
                }
            }
            else if (arg0.getId() == R.id.id_dvbc_rate)
            {
                String strStrValue = "" + mRateEditText.getText();
                nValue = Integer.parseInt(strStrValue) * TP_UNIT_RATE;
                if ((SYMBOLRATE_MAX_VALUE < nValue) || (SYMBOLRATE_MIN_VALUE > nValue))
                {
                    bValidValue = false;
                    mRateEditText.setText(currentRate + "");
                    LogTool.d(LogTool.MSCAN, "onFocusChange mRateEditText invalid " + nValue);
                }
            }
            else
            {
                return;
            }
            LogTool.d(LogTool.MSCAN, "onFocusChange nValue=" + nValue);

            if (!bValidValue)
            {
                MyToast.makeText(mParentWnd, R.string.str_dvbc_validate_input, MyToast.LENGTH_LONG).show();
            }
        }
        return;
    }


    @Override
    public boolean isCanStartScan()
    {
        return true;
    }

    @Override
    public boolean isNetworkScan()
    {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent)
    {
        mScanMainWnd = parent;
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent)
    {
        LogTool.d(LogTool.MINSTALL, "DVBCManualScanView onKeyDown . KeyCode = " + keyCode);
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

    private final Runnable mRefreshQuality = new Runnable()
    {
        @Override
        public void run()
        {
            setSignalView();
            mQualityHandler.postDelayed(mRefreshQuality, MILLISECONDSECOND);
        }
    };

    private void setSignalView()
    {
        Tuner mTuner = mDvbcNetwork.getTuner();
        if (null != mTuner)
        {
            LogTool.d(LogTool.MPLAY, "setSignalView getModulation = " + mTuner.getModulation());
            int mSignalQuality = mTuner.getSignalQuality();
            mSignalQuality = mSignalQuality > 0 ? (mSignalQuality + getRandom(-3, 3)) : 0;
            mSignalQuality = mSignalQuality < 100 ? mSignalQuality : 100;
            mSignalQuality = mSignalQuality > 0 ? mSignalQuality : 0;
            mSignalQualityTextView.setText(Integer.toString(mSignalQuality) + "%");
            mSignalQualityProgressBar.setProgress(mSignalQuality);
        }
        else
        {
            mSignalQualityTextView.setText("0%");
            mSignalQualityProgressBar.setProgress(0);
        }
    }

    private int getRandom(int min, int max)
    {
        SecureRandom mRandom = new SecureRandom();
        int r = mRandom.nextInt(max - min);
        return r + min;
    }

    OnDigtalEditTextChangeListener mOnDigtalEditTextChangeListener = new OnDigtalEditTextChangeListener()
    {
        @Override
        public void onDigtalEditTextChange(DigtalEditText editText, String strText)
        {
            if(mCrtMultiplex != null)
            {
                if(editText.getId() == R.id.id_dvbc_freq)
                {
                    mCrtMultiplex.setFrequency(Integer.parseInt(strText)*1000);
                }
                else if(editText.getId() == R.id.id_dvbc_rate)
                {
                    mCrtMultiplex.setSymbolRate(Integer.parseInt(strText)*1000);
                }

                mPlayer.stop(EnStopType.BLACKSCREEN);
                mPlayer.releaseResource(0);

                mTunerConnect();
            }
        }
    };

    public void mTunerConnect()
    {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                Tuner mTuner = mDvbcNetwork.getTuner();
                if (null != mTuner) {
                    mTuner.connect(mCrtMultiplex, 0, false);
                    boolean mIsConnected = (EnTunerStatus.LOCK == mTuner.getTunerStatus());
                    if (mIsConnected) {
                        if (mQualityHandler.hasCallbacks(mRefreshQuality)) {
                            mQualityHandler.removeCallbacks(mRefreshQuality);
                        }
                        mQualityHandler.post(mRefreshQuality);
                    }
                }
            }
        });
    }
}
