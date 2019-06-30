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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnScrambleFilter;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTransmissionTypeFilter;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.DVBTChannelDot;
import com.hisilicon.dtv.network.DVBTNetwork;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.dtv.network.Multiplex.EnVersionType;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.dtv.network.ScanType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.base.DTVApplication;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.dtv.play.EnStopType;

public class DVBTManualScanView extends BaseView implements IScanSubWnd, OnClickListener, OnComboxSelectChangeListener
{
    private static final String TAG = "DVBTManualScanView";

    private Button          mStartScanScanBtn = null;
    private Combox          mFrqCombox = null;
    private Combox          mBandwidthCombox = null;
    private Combox          mVersionCombox = null;
    private Combox          mOneSegCombox = null;
    private LinearLayout    mVersionLayout = null;
    private LinearLayout    mOneSegLayout = null;

    private TextView        mSignalQualityTextView = null;
    private SeekBar         mSignalQualityProgressBar = null;

    private BaseActivity    mParentWnd = null;
    private IScanMainWnd    mScanMainWnd = null;

    private int             mOneSegFlag = 0;
    private DVBTNetwork     mDVBTNetwork = null;
    private DVBTChannelDot  mCrtMultiplex = null; //get from db
    private EnNetworkType   mCurNetworkType = null;
    private Handler         mQualityHandler = new Handler();

    private static final int MILLISECONDSECOND = 1000;

    public DVBTManualScanView(BaseActivity arg0)
    {
        super((LinearLayout)arg0.findViewById(R.id.ly_dvbt_manual_type));
        mParentWnd = arg0;
        mScanMainWnd = (IScanMainWnd)arg0;

        initView();
    }

    private void initView()
    {
        mStartScanScanBtn = (Button) mParentWnd.findViewById(R.id.id_dvbt_start_scan_btn);
        mFrqCombox = (Combox)mParentWnd.findViewById(R.id.id_dvbt_frmindex);
        mBandwidthCombox = (Combox)mParentWnd.findViewById(R.id.id_dvbt_scan_bandwidth_combox);
        mVersionCombox = (Combox) mParentWnd.findViewById(R.id.id_dvbt_version);
        mOneSegCombox = (Combox)mParentWnd.findViewById(R.id.id_isdbt_oneseg);
        mSignalQualityProgressBar = (SeekBar)mParentWnd.findViewById(R.id.dvbtfreq_qaulity_seekbar);
        mSignalQualityTextView = (TextView)mParentWnd.findViewById(R.id.dvbtfreq_qaulity_value_txt);
        mVersionLayout = (LinearLayout)mParentWnd.findViewById(R.id.id_layout_dvbt_version);
        mOneSegLayout = (LinearLayout)mParentWnd.findViewById(R.id.id_layout_isdbt_oneseg);

        mStartScanScanBtn.setOnClickListener(this);
        mFrqCombox.setOnSelectChangeListener(this);
    }

    private void initData()
    {
        String strForm = mParentWnd.getString(R.string.str_install_dvbt_id);
        String strTip = null;
        mCurNetworkType = mNetworkManager.getCurrentNetworkType();

        List<Network> mLstNetwork = mNetworkManager.getNetworks(mCurNetworkType, mDTV.getCountry());
        if (null == mLstNetwork || mLstNetwork.size() <= 0)
        {
            mLstNetwork = mNetworkManager.getNetworks(mCurNetworkType);
        }

        if (null != mLstNetwork && mLstNetwork.size() > 0)
        {
            mDVBTNetwork = (DVBTNetwork)mLstNetwork.get(0);
        }

        mOneSegFlag = mDtvConfig.getInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_OFF);
        if (CommonValue.ONESEG_FLAG_OFF == mOneSegFlag)
        {
            mOneSegCombox.setTag(false);
        }
        else
        {
            mOneSegCombox.setTag(true);
        }

        if (null != mDVBTNetwork)
        {
            LinkedHashMap<String, Object> mapFreq = new LinkedHashMap<String, Object>();
            List<Multiplex> listMult = mDVBTNetwork.getPresetMultiplexes();
            if (listMult.size() > 0)
            {
                Channel mcurChannel = mChnHistory.getCurrentChn(halApi.EnumSourceIndex.SOURCE_DVBT);
                if(mcurChannel != null)
                {
                    int  curChannelFre = mcurChannel.getBelongMultiplexe().getFrequency();
                    for(int index = 0 ; index < listMult.size() ; index ++)
                    {
                        if(listMult.get(index).getFrequency() == curChannelFre)
                        {
                            mCrtMultiplex = (DVBTChannelDot) listMult.get(index);
                        }
                     }
                }

                if (null == mCrtMultiplex)
                {
                    mCrtMultiplex = (DVBTChannelDot) listMult.get(0);
                }

                LogTool.d(LogTool.MSCAN, "dtvChDot getFrequency= =" + mCrtMultiplex.getFrequency() + "  " + mCrtMultiplex.getVersion() + " "
                        + mCrtMultiplex.getBandWidth() + " " + EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()) + " " + mCrtMultiplex.getModulation());
                for (int i = 0; i < listMult.size(); i++)
                {
                    Multiplex obj = listMult.get(i);
                    strTip = String.format(strForm, i, obj.getFrequency());
                    mapFreq.put(strTip, obj);
                    LogTool.d(LogTool.MSCAN, "fre id:" + obj.getID() + ",frm=" + obj.getFrequency());
                }

                mFrqCombox.setData(mapFreq);
                mFrqCombox.setTag(mCrtMultiplex);
                mTunerConnect();
            }
        }
        else
        {
            LogTool.w(LogTool.MSCAN, " mDVBTNetwork is null, please check database"  );
            MyToast.makeText(mParentWnd, R.string.database_error, MyToast.LENGTH_SHORT).show();
            mFrqCombox.setClickable(false);
            mBandwidthCombox.setClickable(false);
            mStartScanScanBtn.setClickable(false);
            mOneSegCombox.setClickable(false);
            return;
        }

        LinkedHashMap<String, Object> mapBandwidthType = new LinkedHashMap<String, Object>();
        mapBandwidthType.put("5000 KHz", EnBandwidth.BW5000);
        mapBandwidthType.put("6000 KHz", EnBandwidth.BW6000);
        mapBandwidthType.put("7000 KHz", EnBandwidth.BW7000);
        mapBandwidthType.put("8000 KHz", EnBandwidth.BW8000);
        mBandwidthCombox.setData(mapBandwidthType);
        mBandwidthCombox.setOnSelectChangeListener(this);
        mBandwidthCombox.setTag(EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()));
        LinkedHashMap<String, Object> mapOneSegType = new LinkedHashMap<String, Object>();
        mapOneSegType.put(mParentWnd.getString(R.string.str_off), false);
        mapOneSegType.put(mParentWnd.getString(R.string.str_on), true);
        mOneSegCombox.setData(mapOneSegType);
        mOneSegCombox.setOnSelectChangeListener(this);

        LinkedHashMap<String, Object> mapVersion = new LinkedHashMap<String, Object>();
        mapVersion.put("AUTO", EnVersionType.Version_all);
        mVersionCombox.setData(mapVersion);
        mCrtMultiplex.setVersion(EnVersionType.Version_all);
        mVersionCombox.setOnSelectChangeListener(this);
        mVersionCombox.setTag(mCrtMultiplex.getVersion());

        if (EnNetworkType.ISDB_TER == mCurNetworkType)
        {
            mOneSegLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            mOneSegLayout.setVisibility(View.GONE);
        }

        mVersionLayout.setVisibility(View.GONE);
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
        mFrqCombox.hide();
        if(mQualityHandler.hasCallbacks(mRefreshQuality))
            mQualityHandler.removeCallbacks(mRefreshQuality);
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
        if (R.id.id_dvbt_start_scan_btn == arg0.getId())
        {
            List<EnNetworkType> lstNetworkType = new ArrayList<EnNetworkType>();
            lstNetworkType.add(mCurNetworkType);
            ((DTVApplication)mParentWnd.getApplication()).setScanNetworkType(lstNetworkType);

            ScanType type = new ScanType();
            type.setBaseType(ScanType.EnBaseScanType.SINGLE_MULTI);
            type.enableNit(false);
            type.setFTAFilter(EnScrambleFilter.ALL);
            type.setTVRadioFilter(EnTVRadioFilter.ALL);
            if (EnNetworkType.ISDB_TER == mCurNetworkType)
            {
                LogTool.d(LogTool.MSCAN, "mOneSegCombox tag =" + mOneSegCombox.getTag());
                if (!((Boolean) mOneSegCombox.getTag()))
                {
                    type.setTransmissionTypeFilter(EnTransmissionTypeFilter.FIX);
                }
                else
                {
                    type.setTransmissionTypeFilter(EnTransmissionTypeFilter.ALL);
                }
            }

            List<Multiplex> lstMultiplex = new ArrayList<Multiplex>();
            lstMultiplex.add(mCrtMultiplex);

            LogTool.d(LogTool.MSCAN, "tp size =" + lstMultiplex.size());
            mDVBTNetwork.setScanMultiplexes(lstMultiplex);

            List<Network> mLstScanNetwork = new ArrayList<Network>();
            mLstScanNetwork.add(mDVBTNetwork);
            ((DTVApplication) mParentWnd.getApplication()).setScanParam(mLstScanNetwork);
            ((DTVApplication)mParentWnd.getApplication()).setScanType(mCurNetworkType, type);

            if (null != mScanMainWnd)
            {
                mScanMainWnd.sendMessage(IScanMainWnd.MSG_ID_READY_TO_SCAN, null);
            }
        }
    }

    @Override
    public void onComboxSelectChange(View arg0, String strText, Object obj, int index)
    {
        if (R.id.id_dvbt_frmindex == arg0.getId())
        {
            Multiplex mult = (Multiplex) mFrqCombox.getTag();
            LogTool.d(LogTool.MSCAN, "id_dvbt_frmindex obj= =" + mult);
            if (null != mult)
            {
                mCrtMultiplex = (DVBTChannelDot) mult;
                mBandwidthCombox.setTag(EnBandwidth.valueOf(mCrtMultiplex.getBandWidth()));

                mPlayer.stop(EnStopType.BLACKSCREEN);
                mPlayer.releaseResource(0);

                mTunerConnect();
            }
        }
        else if (R.id.id_dvbt_scan_bandwidth_combox == arg0.getId())
        {
            if (null != mBandwidthCombox.getTag())
            {
                LogTool.d(LogTool.MSCAN, "id_dvbt_scan_bandwidth_combox =" + ((EnBandwidth) mBandwidthCombox.getTag()).getValue());
                mCrtMultiplex.setBandWidth(((EnBandwidth) mBandwidthCombox.getTag()).getValue());

                mPlayer.stop(EnStopType.BLACKSCREEN);
                mPlayer.releaseResource(0);

                mTunerConnect();
            }
        }
        else if (R.id.id_dvbt_version == arg0.getId())
        {
            if (null != mVersionCombox.getTag())
            {
                mCrtMultiplex.setVersion(EnVersionType.Version_all);

                mPlayer.stop(EnStopType.BLACKSCREEN);
                mPlayer.releaseResource(0);

                mTunerConnect();
                //mCntHandler.removeMessages(HANDLE_LOCK_MESSAGE);
                //mCntHandler.sendEmptyMessage(HANDLE_LOCK_MESSAGE);
            }
        }
        else if (R.id.id_isdbt_oneseg == arg0.getId())
        {
            if (!((Boolean) mOneSegCombox.getTag()) && CommonValue.ONESEG_FLAG_OFF != mOneSegFlag)
            {
                mDtvConfig.setInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_OFF);
                mOneSegFlag = CommonValue.ONESEG_FLAG_OFF;
            }
            else if ((Boolean) mOneSegCombox.getTag() && CommonValue.ONESEG_FLAG_ON != mOneSegFlag)
            {
                mDtvConfig.setInt(CommonValue.ONESEG_FLAG, CommonValue.ONESEG_FLAG_ON);
                mOneSegFlag = CommonValue.ONESEG_FLAG_ON;
            }
        }
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
        LogTool.d(LogTool.MINSTALL, "DVBTManualScanView keyDispatch . KeyCode = " + keyCode);
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

    public void mTunerConnect()
    {
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                Tuner mTuner = mDVBTNetwork.getTuner();
                if (null != mTuner) {
                    mCrtMultiplex.setVersion(EnVersionType.Version_all);
                    mTuner.connect(mCrtMultiplex, 0, false);
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

    private void setSignalView()
    {
        Tuner mTuner = mDVBTNetwork.getTuner();
        if (null != mTuner)
        {
            LogTool.v(LogTool.MPLAY, "getModulation = " + mTuner.getModulation());
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

}
