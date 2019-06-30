package com.hisilicon.tvui.record;

import java.text.DecimalFormat;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.tvui.R;
import com.hisilicon.dtv.DTV;

import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;

import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.DTVDigitalClock;

public class BookArriveDialog extends Dialog implements View.OnClickListener
{
    private Context mContext = null;

    private Button mOKButton = null;
    private long mTipDuration = 0;
    private ChannelManager mChannelManager = null;

    private boolean mCanStartBookTask = true;

    private boolean mBookArrive = false;

    private static final int DEFAULT_REMIND_DURATION = 8;
    private long mLastTime = 0;
    private int mDuration = 0;
    private int mChannelID = 0;
    private int mType = 0;
    private int mBookTastID = 0;

    public BookArriveDialog(Context context, int duration, int channelID, int type, int taskID)
    {
        super(context, R.style.DIM_STYLE);
        mContext = context;
        mDuration = duration;
        mChannelID = channelID;
        mType = type;
        mBookTastID = taskID;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.x = 0;
        lp.y = 0;
        lp.alpha = 1;
        this.getWindow().setAttributes(lp);
    }

    private void startBookTask()
    {
        LogTool.d(LogTool.MREC, "onStop mBookArrive = " + mBookArrive + " mCanStartBookTask = " + mCanStartBookTask);
        if (mCanStartBookTask)
        {
            mCanStartBookTask = false;

            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            if(null != activityManager.getRunningTasks(1))
            {
                String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                LogTool.d(LogTool.MREC, "startBookTask  runningActivity = " + runningActivity);
                if (!runningActivity.equals("com.hisilicon.tvui.play.MainActivity"))
                {
                    // Jump to DTVPlayerActivity
                    Intent dtvPlayerIntent = new Intent();
                    dtvPlayerIntent.setClass(mContext, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putInt(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY, mBookTastID);

                    //get change source
                    Channel bookedChannel = mChannelManager.getChannelByID(mChannelID);
                    int dstSource = -1;
                    if (bookedChannel != null)
                    {
                        //get change source
                        EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
                        LogTool.d(LogTool.MPLAY, "startBookTask bookNetworkType : " + bookNetworkType);
                        if(bookNetworkType == EnNetworkType.CABLE)
                        {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                        }
                        else if(bookNetworkType == EnNetworkType.TERRESTRIAL)
                        {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DVBT;
                        }
                        else if(bookNetworkType == EnNetworkType.DTMB)
                        {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DTMB;
                        }
                        else if(bookNetworkType == EnNetworkType.ISDB_TER)
                        {
                            dstSource = halApi.EnumSourceIndex.SOURCE_ISDBT;
                        }

                        LogTool.d(LogTool.MPLAY, "startBookTask book channelID : " + mChannelID + "startBookTask dstSource: " + dstSource);
                    }

                    dtvPlayerIntent.putExtra("SourceName", dstSource);

                    dtvPlayerIntent.putExtras(b);

                    CommonDef.startActivityEx(mContext, dtvPlayerIntent);
                    // Send book task broadcast
                }
            }
        }

        if (!mBookArrive)
        {
            mBookArrive = true;
        }
        dismiss();
    }

    private void sendArriveBroadcast()
    {
        TaskUtil.post(new Runnable() {
            public void run() {
                /* 延迟500ms发送，防止MainActivity中接收不到  */
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    LogTool.e(LogTool.MREC, "sendArriveBroadcast failed: " + e);
                }
                Intent bookArriveIntent = new Intent();
                bookArriveIntent.setAction(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CommonValue.DTV_BOOK_ID, mBookTastID);
                bundle.putInt(CommonValue.DTV_BOOK_CHANNEL_ID, mChannelID);
                bundle.putInt(CommonValue.DTV_BOOK_DURATION, mDuration);
                bundle.putInt(CommonValue.DTV_BOOK_TYPE, mType);
                bookArriveIntent.putExtras(bundle);
                CommonDef.sendBroadcastEx(mContext, bookArriveIntent);
                mBookArrive = false;
                LogTool.d(LogTool.MREC, "sendArriveBroadcast will start ");
            }
        });
    }

    protected void onStop()
    {
        if (mBookArrive)
        {
            sendArriveBroadcast();
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        LogTool.d(LogTool.MREC, "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.book_time_arrive);

        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mChannelManager = mDTV.getChannelManager();

        initLinearLayout();
    }

    private void initLinearLayout()
    {
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_arrive_title);
        LinearLayout mTipLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_arrive_ok_cancel);
        mLinearLayout.setVisibility(View.VISIBLE);

        mTipLinearLayout.setVisibility(View.VISIBLE);

        initTipDuration();

        mOKButton = (Button) findViewById(R.id.btn_book_time_arrive_ok);
        initOKButton();

        initDigitalClock();
    }

    private void initTipDuration()
    {
        mTipDuration = DEFAULT_REMIND_DURATION;
    }

    private void initDigitalClock()
    {
        DTVDigitalClock mDigitalClock = (DTVDigitalClock) findViewById(R.id.dc_book_arrive_time);
        mDigitalClock.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mTipDuration > 0) {
                    if (SystemClock.elapsedRealtime() - mLastTime > 500) {
                        mTipDuration--;
                    }
                } else {
                    LogTool.d(LogTool.MREC, "initDigitalClock");
                    startBookTask();
                }

                mLastTime = SystemClock.elapsedRealtime();
                setOKButtonText();
            }

        });
    }

    private void setOKButtonText()
    {
        DecimalFormat format = new DecimalFormat("00");
        String ok = mContext.getResources().getString(R.string.book_time_alarm_ok);
        mOKButton.setText(String.format("%s(%s)", ok, format.format(mTipDuration)));
    }

    private void initOKButton()
    {
        setOKButtonText();
        mOKButton.setOnClickListener(this);
        mOKButton.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LogTool.d(LogTool.MREC, "keyCode = " + keyCode);
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
        {
            dismiss();
            break;
        }
        default:
        {
            break;
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
        case R.id.btn_book_time_arrive_ok:
        {
            startBookTask();
        }
        default:
        {
            break;
        }
        }
    }
}
