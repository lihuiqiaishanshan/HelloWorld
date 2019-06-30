package com.hisilicon.tvui.installtion;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hisilicon.dtv.hardware.EnTunerStatus;
import com.hisilicon.dtv.hardware.Tuner;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;

import java.lang.ref.WeakReference;

public class RefreshQS {
    // Delay frequency interval display dialog box
    public static final int DELAY_SHOW_DIALOG_SPACE = 100;

    // Show or hide the tuner locking hint dialog box message ID
    // deal by mConnectNotifyHandler
    private Handler mConnectNotifyHandler = null;
    public static final int MESSAGE_CONNECT_SHOW_DIALOG = 1;
    public static final int MESSAGE_CONNECT_CLOSE_DIALOG = 2;

    private static int REFRESH_INTERVAL_500 = 500;
    private static int REFRESH_INTERVAL = 1000;
    private static int CONNECT_INTERVAL = 2000;

    //deal by TunerCntMsgHandler
    private TunerCntMsgHandler mMsgHandler;
    private static int MESSAGE_TUNER_CONNECT = 1;
    private static int MESSAGE_TUNER_SET_BAR_ZERO = 2;
    private static int MESSAGE_TUNER_REFRESH_BAR = 3;

    private View mParentView;
    private ProgressBar mPBarQuality;
    private ProgressBar mPBarStrength;
    private TextView mTxtQuality;
    private TextView mTxtStrength;

    private Tuner mTuner = null;
    private Multiplex mCrtMultiplex = null;
    private int mLastMultiplexID = 0;

    private boolean mIsStartRefresh = false;
    private boolean mIsConnected = false;
    private boolean mIsConnecting = false;
    private boolean mAlwaysZero = false;
    private int mConnectTimeOut = 0;
    private boolean mIsMotorParamUsed = false;
    private RefreshQualityThread mRefreshThread;

    public RefreshQS(View parent) {
        mParentView = parent;
        mPBarStrength = (ProgressBar) mParentView.findViewById(R.id.id_antennaseting_signalstrength);
        mPBarQuality = (ProgressBar) mParentView.findViewById(R.id.id_antennaseting_signalquality);
        mTxtQuality = (TextView) mParentView.findViewById(R.id.id_antennaseting_qualitynum);
        mTxtStrength = (TextView) mParentView.findViewById(R.id.id_antennaseting_strengthnum);
        setBarZero();
        mMsgHandler = new TunerCntMsgHandler(this);
        mRefreshThread = new RefreshQualityThread();
    }

    private int getValidateValue(int nValue) {
        if (nValue < 0) {
            return 0;
        }
        if (nValue > 100) {
            return 100;
        }
        return nValue;
    }

    public void setBarZero() {
        mTxtQuality.setText("0%");
        mPBarStrength.setProgress(0);
        mTxtStrength.setText("0%");
        mPBarQuality.setProgress(0);
    }

    public void refreshBar() {
        int nQ = getValidateValue(mTuner.getSignalQuality());
        int nS = getValidateValue(mTuner.getSignalStrength());

        mPBarQuality.setProgress(nQ);
        mTxtQuality.setText(mParentView.getResources().getString(R.string.str_percent, nQ));

        mPBarStrength.setProgress(nS);
        mTxtStrength.setText(mParentView.getResources().getString(R.string.str_percent, nS));
    }

    public void open() {
        mIsStartRefresh = true;
        setBarZero();
        mIsConnected = false;
        LogTool.d(LogTool.MSCAN, " ==== RefreshQS open === ");
    }

    public void close() {
        mIsStartRefresh = false;
        mMsgHandler.removeMessages(MESSAGE_TUNER_CONNECT);
        mMsgHandler.removeCallbacks(mRefreshThread);
        LogTool.d(LogTool.MSCAN, " ==== RefreshQS close === ");
    }

    public boolean isConnecting() {
        return mIsConnecting;
    }

    private void tunerConnect() {
        if (null == mTuner || mAlwaysZero) {
            return;
        }
        if (null == mCrtMultiplex) {
            mTuner.disconnect();
            return;
        }

        if ((mLastMultiplexID != mCrtMultiplex.getID()) || (mCrtMultiplex.getID() == -1)) {
            setBarZero();
            mIsConnected = false;
        }

        if (null != mConnectNotifyHandler) {
            mConnectNotifyHandler.removeMessages(MESSAGE_CONNECT_SHOW_DIALOG);
            mConnectNotifyHandler.removeMessages(MESSAGE_CONNECT_CLOSE_DIALOG);
            // Frequency locking, may be more time-consuming, so need to display boxes
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    LogTool.v(LogTool.MSCAN, "start connect, mConnectTimeOut:" + mConnectTimeOut + " " +
                            "mIsMotorParamUsed:" + mIsMotorParamUsed);
                    mIsConnecting = true;
                    if (mConnectTimeOut < 500 && mConnectTimeOut > 0) {
                        mConnectTimeOut = 500;
                    }
                    mConnectNotifyHandler.sendEmptyMessage(MESSAGE_CONNECT_SHOW_DIALOG);
                    mTuner.connect(mCrtMultiplex, mConnectTimeOut, mIsMotorParamUsed);
                    LogTool.v(LogTool.MSCAN, "start connect end status " + mIsConnected);

                    mIsConnecting = false;
                    mConnectNotifyHandler.removeMessages(MESSAGE_CONNECT_SHOW_DIALOG);
                    mConnectNotifyHandler.sendEmptyMessage(MESSAGE_CONNECT_CLOSE_DIALOG);
                    mIsConnected = (EnTunerStatus.LOCK == mTuner.getTunerStatus());
                    if (mIsConnected) {
                        mMsgHandler.sendEmptyMessage(MESSAGE_TUNER_REFRESH_BAR);
                    }
                    mMsgHandler.postDelayed(mRefreshThread, REFRESH_INTERVAL_500);
                }
            };
            TaskUtil.post(runnable);
        } else {
            mIsConnecting = true;
            LogTool.v(LogTool.MSCAN, "no handler, start connect, mConnectTimeOut:" + mConnectTimeOut + " " +
                    "mIsMotorParamUsed: " + mIsMotorParamUsed);
            LogTool.v(LogTool.MSCAN,
                    "mCrtMultiplex:" + mCrtMultiplex.getFrequency() + " mIsMotorParamUsed: " + mIsMotorParamUsed);
            if (mConnectTimeOut < 500 && mConnectTimeOut > 0) {
                mConnectTimeOut = 500;
            }
            mTuner.connect(mCrtMultiplex, mConnectTimeOut, mIsMotorParamUsed);
            mMsgHandler.postDelayed(mRefreshThread, REFRESH_INTERVAL_500);
        }
    }

    public boolean isConnected() {
        return mIsConnected;
    }

    public void setTunerConnectAttr(int connectTimeOut, boolean bMotorParamUsed) {
        mConnectTimeOut = connectTimeOut;
        mIsMotorParamUsed = bMotorParamUsed;
    }

    public void setBarZeroAlways(boolean alwaysZero) {
        setBarZero();
        mAlwaysZero = alwaysZero;
    }

    public void tunerConnectState(Tuner tuner, Multiplex multiplex, Handler connectNotifyHandler) {
        mIsConnecting = false;
        mMsgHandler.removeMessages(MESSAGE_TUNER_CONNECT);
        mMsgHandler.removeCallbacks(mRefreshThread);
        if (null != mConnectNotifyHandler) {
            mConnectNotifyHandler.removeMessages(MESSAGE_CONNECT_SHOW_DIALOG);
            mConnectNotifyHandler.removeMessages(MESSAGE_CONNECT_CLOSE_DIALOG);
        }
        mConnectNotifyHandler = connectNotifyHandler;
        if (mCrtMultiplex != null) {
            mLastMultiplexID = mCrtMultiplex.getID();
        }
        mCrtMultiplex = multiplex;
        mTuner = tuner;
        LogTool.d(LogTool.MSCAN, " ==== tunerConnectState === multiplex=" + multiplex);
        mMsgHandler.sendEmptyMessage(MESSAGE_TUNER_CONNECT);
    }

    private boolean isStartRefresh() {
        return mIsStartRefresh;
    }

    public class RefreshQualityThread implements Runnable {

        @Override
        public void run() {
            if (mAlwaysZero) {
                mMsgHandler.sendEmptyMessage(MESSAGE_TUNER_SET_BAR_ZERO);
                return;
            }
            if (!mIsStartRefresh) {
                return;
            }
            try {
                mIsConnected = (EnTunerStatus.LOCK == mTuner.getTunerStatus());
                if (!mIsConnected) {
                    mMsgHandler.sendEmptyMessage(MESSAGE_TUNER_SET_BAR_ZERO);
                    mMsgHandler.postDelayed(this, REFRESH_INTERVAL_500);
                } else {
                    mMsgHandler.sendEmptyMessage(MESSAGE_TUNER_REFRESH_BAR);
                    mMsgHandler.postDelayed(this, REFRESH_INTERVAL);
                }
            } catch (Exception e) {
                LogTool.e(LogTool.MSCAN, " ==== RefreshQualityThread === e=" + e);
            }
        }
    }

    static class TunerCntMsgHandler extends Handler {
        WeakReference<RefreshQS> mParent;

        TunerCntMsgHandler(RefreshQS parent) {
            mParent = new WeakReference<RefreshQS>(parent);
        }

        @Override
        public void handleMessage(Message msg) {
            if (MESSAGE_TUNER_CONNECT == msg.what) {
                if ((null != mParent) && (null != mParent.get()) && (mParent.get().isStartRefresh())) {
                    mParent.get().tunerConnect();
                }
            } else if (MESSAGE_TUNER_SET_BAR_ZERO == msg.what) {
                if ((null != mParent) && (null != mParent.get())) {
                    mParent.get().setBarZero();
                }
            } else if (MESSAGE_TUNER_REFRESH_BAR == msg.what) {
                if ((null != mParent) && (null != mParent.get())) {
                    mParent.get().refreshBar();
                }
            }
        }
    }
}
