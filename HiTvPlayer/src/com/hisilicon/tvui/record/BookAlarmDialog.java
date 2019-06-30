package com.hisilicon.tvui.record;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookManager;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.DTVDigitalClock;


public class BookAlarmDialog extends Dialog implements View.OnClickListener
{
    private int mBookId = 0;
    private Context mContext = null;

    private TextView mChannelTextView = null;
    private TextView mNameTextView = null;
    private TextView mStartTimeTextView = null;
    private TextView mDurationTextView = null;

    private Button mOKButton = null;
    private Button mCancelButton = null;

    private BookManager mBookManager = null;
    private BookTask mBookTask = null;
    private ChannelManager mChannelManager = null;

    private TimeManager mTimeManager = null;

    private long mTipDuration = 0;

    private boolean mCanStartBookTask = true;
    private boolean mKeyback = false;
    private boolean mBookAlarm = false;
    private long mLastTime = 0;

    private static final int DEFAULT_REMIND_DURATION = 10;

    private static final int DEFAULT_TIP_DURATION = 10;

    public BookAlarmDialog(Context context, int bookId)
    {
        super(context, R.style.DIM_STYLE);
        mContext = context;
        mBookId = bookId;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.x = 0;
        lp.y = 0;
        lp.alpha = 1;
        this.getWindow().setAttributes(lp);
    }

    private void startBookTask()
    {
        LogTool.d(LogTool.MREC, "startBookTask mCanStartBookTask = " + mCanStartBookTask + " mBookAlarm = " + mBookAlarm);

        if (mKeyback)
        {
            return;
        }

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
                    b.putInt(CommonValue.DTV_BOOK_ALARM_REMINDE_PLAY, mBookTask.getId());
                    dtvPlayerIntent.putExtras(b);

                    //get change source
                    int channelID = mBookTask.getChannelId();
                    Channel bookedChannel = mChannelManager.getChannelByID(channelID);
                    int dstSource = -1;
                    if (bookedChannel != null)
                    {
                        //get change source
                        EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
                        LogTool.d(LogTool.MPLAY, " bookNetworkType : " + bookNetworkType);
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

                        LogTool.d(LogTool.MPLAY, " book channelID : " + channelID + "dstSource: " + dstSource);
                    }

                    dtvPlayerIntent.putExtra("SourceName", dstSource);
                    CommonDef.startActivityEx(mContext, dtvPlayerIntent);
                    // Send book task broadcast
                }
            }
        }

        if (!mBookAlarm)
        {
            mBookAlarm = true;
        }
        dismiss();
    }

    private void sendRemindBroadcast()
    {
        TaskUtil.post(new Runnable() {
            public void run() {
                /* 延迟500ms发送，防止MainActivity中接收不到 */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    LogTool.e(LogTool.MREC, "sendRemaindBroadcast failed: " + e);
                }
                Intent bookArriveIntent = new Intent();
                bookArriveIntent.setAction(CommonValue.DTV_BOOK_ALARM_REMINDE_PLAY);
                Bundle bundle = new Bundle();
                bundle.putInt(CommonValue.DTV_BOOK_ID, mBookTask.getId());
                bundle.putInt(CommonValue.DTV_BOOK_CHANNEL_ID, mBookTask.getChannelId());
                bundle.putInt(CommonValue.DTV_BOOK_DURATION, mBookTask.getDuration());
                bundle.putInt(CommonValue.DTV_BOOK_TYPE, mBookTask.getType().ordinal());
                bookArriveIntent.putExtras(bundle);
                CommonDef.sendBroadcastEx(mContext, bookArriveIntent);
                mBookAlarm = false;
                LogTool.d(LogTool.MREC, "will start ");
            }
        });
    }

    protected void onStop()
    {
        LogTool.d(LogTool.MREC, "onStop mBookAlarm = " + mBookAlarm + " mCanStartBookTask = " + mCanStartBookTask);
        if (mBookAlarm)
        {
            sendRemindBroadcast();
        }
        else
        {
            //detele or update this event
            EnTaskCycle mCycle = mBookTask.getCycle();
            BookTask tmpTask = mBookManager.getTaskByID(mBookTask.getId());
            if(null != tmpTask)
            {
                if (mCycle == EnTaskCycle.WEEKLY)
                {
                    Date StartDate = tmpTask.getStartDate();
                    //delay one day
                    long milsecond = StartDate.getTime() + 24 * 60 * 60 * 1000 * 7;
                    StartDate.setTime(milsecond);
                    tmpTask.setStartDate(StartDate);
                    tmpTask.setEnable(true);
                    mBookManager.updateTask(tmpTask);
                }
                else if (mCycle == EnTaskCycle.DAILY)
                {
                    Date StartDate = tmpTask.getStartDate();
                    long milsecond = StartDate.getTime() + 24 * 60 * 60 * 1000;//delay one day
                    StartDate.setTime(milsecond);
                    tmpTask.setStartDate(StartDate);
                    tmpTask.setEnable(true);
                    mBookManager.updateTask(tmpTask);
                }
            }
            else
            {
                LogTool.d(LogTool.MREC, "deleteTask");
                mBookManager.deleteTask(mBookTask);
            }
        }
        LogTool.d(LogTool.MREC, "onStop mBookAlarm = " + mBookAlarm);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        LogTool.d(LogTool.MREC, "onCreate");

        setContentView(R.layout.book_time_alarm);

        initDTV();

        initBookTask();

        initLinearLayout();
    }

    private void initDTV()
    {
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mBookManager = mDTV.getBookManager();
        mChannelManager = mDTV.getChannelManager();
        mTimeManager = mDTV.getNetworkManager().getTimeManager();

    }

    private void initBookTask()
    {
        mBookTask = mBookManager.getTaskByID(mBookId);
    }

    private void initLinearLayout()
    {
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_alarm);
        LinearLayout mTipLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_alarm_tip);
        if (null == mBookTask)
        {
            mCanStartBookTask = false;

            mLinearLayout.setVisibility(View.GONE);

            mTipLinearLayout.setVisibility(View.VISIBLE);

            mTipDuration = DEFAULT_TIP_DURATION;

            initButton(View.GONE);
        }
        else
        {
            mLinearLayout.setVisibility(View.VISIBLE);

            mTipLinearLayout.setVisibility(View.GONE);

            initTipDuration();

            initTextView();

            initButton(View.VISIBLE);
        }

        initDigitalClock();
    }

    private void initTipDuration()
    {
        Date startDate = mBookTask.getStartDate();
        Date currentDate = mTimeManager.getTime();

        if (currentDate == null)
        {
            currentDate = new Date();
        }

        if ((null != startDate) && (startDate.after(currentDate)))
        {
            mTipDuration = startDate.getTime() - currentDate.getTime();
            mTipDuration /= 1000;

            if (mTipDuration > DEFAULT_REMIND_DURATION)
            {
                mTipDuration = DEFAULT_REMIND_DURATION;
            }
        }
        else
        {
            mTipDuration = DEFAULT_REMIND_DURATION;
        }
    }

    private void initTextView()
    {
        mChannelTextView = (TextView) findViewById(R.id.tv_book_time_alarm_channel);
        initChannelTextView();

        mNameTextView = (TextView) findViewById(R.id.tv_book_time_alarm_name);
        initNameTextView();

        mStartTimeTextView = (TextView) findViewById(R.id.tv_book_time_alarm_start_time);
        initStartTimeTextView();

        mDurationTextView = (TextView) findViewById(R.id.tv_book_time_alarm_duration);
        initDurationTextView();
    }

    // TODO:去掉
    private void initDigitalClock()
    {
        DTVDigitalClock mDigitalClock = (DTVDigitalClock) findViewById(R.id.dc_book_alarm_time);
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

    private void initChannelTextView()
    {
        String channelName = null;

        int channelId = mBookTask.getChannelId();

        Channel channel = mChannelManager.getChannelByID(channelId);
        if (null != channel)
        {
            channelName = channel.getChannelName();
        }

        if (null == channelName)
        {
            channelName = "";
        }

        mChannelTextView.setText(channelName);
    }

    private void initNameTextView()
    {
        String name = mBookTask.getName();

        if (null == name)
        {
            name = "";
        }

        mNameTextView.setText(name);
    }

    private void initStartTimeTextView()
    {
        String startTime = null;

        Date startDate = mBookTask.getStartDate();
        if (null != startDate)
        {
            long millsecond = startDate.getTime();// + mTimeManager.getTimeZone() * 1000;
            startDate.setTime(millsecond);
            LogTool.d(LogTool.MREC, "mBookTask.getStartDate() = " + startDate.toString());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            startTime = format.format(startDate);
        }

        if (null == startTime)
        {
            startTime = "";
        }

        mStartTimeTextView.setText(startTime);
    }

    private void initDurationTextView()
    {
        int d = mBookTask.getDuration();

        Date date = mTimeManager.secondToDate(d);

        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        String duration = format.format(date);

        if (null == duration)
        {
            duration = "";
        }

        mDurationTextView.setText(duration);
    }

    private void initButton(int visible)
    {
        mOKButton = (Button) findViewById(R.id.btn_book_time_alarm_ok);
        mOKButton.setOnClickListener(this);
        initOKButton();

        mCancelButton = (Button) findViewById(R.id.btn_book_time_alarm_cancel);
        initCancelButton(visible);
    }

    private void initOKButton()
    {
        setOKButtonText();
        mOKButton.requestFocus();
    }

    private void initCancelButton(int visible)
    {
        mCancelButton.setOnClickListener(this);
        mCancelButton.setVisibility(visible);
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
        case R.id.btn_book_time_alarm_ok:
        {
            startBookTask();
            dismiss();
            break;
        }
        case R.id.btn_book_time_alarm_cancel:
        {
            dismiss();
            break;
        }
        default:
        {
            break;
        }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        LogTool.d(LogTool.MREC, "keyCode = " + keyCode);
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_BACK:
        {
            LogTool.d(LogTool.MREC, "onKeyDown mBookAlarm = " + mBookAlarm);
            mKeyback = true;
            break;
        }
        default:
        {
            break;
        }
        }
        return super.onKeyDown(keyCode, event);
    }
}
