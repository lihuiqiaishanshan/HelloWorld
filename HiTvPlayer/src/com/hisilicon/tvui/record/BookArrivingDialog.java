package com.hisilicon.tvui.record;

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
import android.view.WindowManager;;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.play.MainActivity;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.view.DTVDigitalClock;
import com.hisilicon.tvui.view.MyToast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Remind BooK Dialog,Reminder 30 seconds before arrival.
 */
public class BookArrivingDialog extends Dialog {
    private Context mContext = null;

    private long mTipDuration = 0;
    private ChannelManager mChannelManager = null;

    private boolean mCanStartBookTask = true;

    private boolean mBookArrive = false;

    private static final int DEFAULT_REMIND_DURATION = 30;
    private long mLastTime = 0;
    private int mDuration = 0;
    private int mChannelID = 0;
    private int mType = 0;
    private int mBookTastID = 0;

    private TextView mChannelTextView = null;
    private TextView mNameTextView = null;
    private TextView mStartTimeTextView = null;
    private TextView mDurationTextView = null;
    private TextView mTipTextView = null;
    public DTV mDTV = null;
    public BookTask currentBookTask = null;

    public BookArrivingDialog(Context context, int duration, int channelID, int type, int taskID) {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogTool.d(LogTool.MREC, "BookArriving onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_time_arriving);
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mChannelManager = mDTV.getChannelManager();
        currentBookTask = mDTV.getBookManager().getTaskByID(mBookTastID);
        initTextView();
        initLinearLayout();
    }

    private void initTextView() {
        mChannelTextView = (TextView) findViewById(R.id.tv_book_time_arriving_channel);
        mNameTextView = (TextView) findViewById(R.id.tv_book_time_arriving_name);
        mStartTimeTextView = (TextView) findViewById(R.id.tv_book_time_arriving_start_time);
        mDurationTextView = (TextView) findViewById(R.id.tv_book_time_arriving_duration);
        mTipTextView = (TextView) findViewById(R.id.btn_book_time_arriving_tip);
        if (null != currentBookTask) {
            initChannelTextView();
            initNameTextView();
            initStartTimeTextView();
        }
        initDurationTextView();
    }

    private void initLinearLayout() {
        LinearLayout mLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_arriving_title);
        LinearLayout mTipLinearLayout = (LinearLayout) findViewById(R.id.llay_book_time_arriving_ok_cancel);
        mLinearLayout.setVisibility(View.VISIBLE);
        mTipLinearLayout.setVisibility(View.VISIBLE);
        initTipDuration();
        initTipText();
        initDigitalClock();
    }

    private void initTipDuration() {
        mTipDuration = DEFAULT_REMIND_DURATION;
    }

    private void initDigitalClock() {
        DTVDigitalClock mDigitalClock = (DTVDigitalClock) findViewById(R.id.dc_book_arriving_time);
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
                    //End of countdown
                    LogTool.d(LogTool.MREC, "initDigitalClock");
                    startBookTask();
                }
                LogTool.d(LogTool.MREC, "mTipDuration=" + mTipDuration);
                mLastTime = SystemClock.elapsedRealtime();
                setOKButtonText();
            }

        });
    }

    private void startBookTask() {
        LogTool.d(LogTool.MREC, "onStop mBookArriving = " + mBookArrive + " mCanStartBookTask = " + mCanStartBookTask);
        if (mCanStartBookTask) {
            mCanStartBookTask = false;

            ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            if (null != activityManager.getRunningTasks(1)) {
                String runningActivity = activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
                LogTool.d(LogTool.MREC, "startBookTask  runningActivity = " + runningActivity);
                if (!runningActivity.equals("com.hisilicon.tvui.play.MainActivity")) {
                    // Jump to DTVPlayerActivity
                    Intent dtvPlayerIntent = new Intent();
                    dtvPlayerIntent.setClass(mContext, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putInt(CommonValue.DTV_BOOK_ALARM_ARRIVE_PLAY, mBookTastID);

                    //get change source
                    Channel bookedChannel = mChannelManager.getChannelByID(mChannelID);
                    int dstSource = -1;
                    if (bookedChannel != null) {
                        //get change source
                        EnNetworkType bookNetworkType = bookedChannel.getNetworkType();
                        LogTool.d(LogTool.MPLAY, "startBookTask bookNetworkType : " + bookNetworkType);
                        if (bookNetworkType == EnNetworkType.CABLE) {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DVBC;
                        } else if (bookNetworkType == EnNetworkType.TERRESTRIAL) {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DVBT;
                        } else if (bookNetworkType == EnNetworkType.DTMB) {
                            dstSource = halApi.EnumSourceIndex.SOURCE_DTMB;
                        } else if (bookNetworkType == EnNetworkType.ISDB_TER) {
                            dstSource = halApi.EnumSourceIndex.SOURCE_ISDBT;
                        }

                        LogTool.d(LogTool.MPLAY, "startBookTask book channelID : " + mChannelID + "startBookTask dstSource: " + dstSource);
                    }

                    dtvPlayerIntent.putExtra("SourceName", dstSource);

                    dtvPlayerIntent.putExtras(b);

                    CommonDef.startActivityEx(mContext, dtvPlayerIntent);
                }
            }
        }

        if (!mBookArrive) {
            mBookArrive = true;
        }
        dismiss();
    }

    private void initTipText() {
        setOKButtonText();
        mTipTextView.requestFocus();
        mTipTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteOrUpdateTask();
            }
        });
    }

    private void setOKButtonText() {
        DecimalFormat format = new DecimalFormat("00");
        String tip = mContext.getResources().getString(R.string.book_time_arriving_tip);
        String str = String.format("%s(%s)", tip, format.format(mTipDuration));
        LogTool.d(LogTool.MREC, "tip str = " + str);
        mTipTextView.setText(str);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogTool.d(LogTool.MREC, "KeyValue.keyCode = " + keyCode);
           switch (keyCode) {
            case KeyValue.DTV_KEYVALUE_DPAD_CENTER: {
                break;
            }
            default: {
                dismiss();
                break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }



    private void sendArriveBroadcast() {
        TaskUtil.post(new Runnable() {
            public void run() {
                /* Delay 3000ms sending to prevent MainActivity from receiving */
                try {
                    Thread.sleep(3000);
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

    private void initChannelTextView() {
        String channelName = null;

        int channelId = currentBookTask.getChannelId();

        Channel channel = mChannelManager.getChannelByID(channelId);
        if (null != channel) {
            channelName = channel.getChannelName();
        }

        if (null == channelName) {
            channelName = "";
        }

        mChannelTextView.setText(channelName);
    }

    private void initNameTextView() {
        String name = currentBookTask.getName();

        if (null == name) {
            name = "";
        }

        mNameTextView.setText(name);
    }

    private void initStartTimeTextView() {
        String startTime = "";

        Calendar startCalendar = currentBookTask.getStartDateCalendar();
        if (null != startCalendar) {
            int year = startCalendar.get(Calendar.YEAR);
            int month = startCalendar.get(Calendar.MONTH) + 1;
            int day = startCalendar.get(Calendar.DAY_OF_MONTH);
            int hour = startCalendar.get(Calendar.HOUR_OF_DAY);
            int min = startCalendar.get(Calendar.MINUTE);
            int second = startCalendar.get(Calendar.SECOND);

            startTime = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " + String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", second);
            LogTool.d(LogTool.MREC, "mBookTask.getStartDate() = " + startTime);
        }

        if (startTime.length() == 0) {
            startTime = "";
        }

        mStartTimeTextView.setText(startTime);
    }

    private void initDurationTextView() {
        int d = mDuration;
        String duration = secondToTime(d);
        if (null == duration) {
            duration = "";
        }
        mDurationTextView.setText(duration);
    }

    public void deleteOrUpdateTask() {
        EnTaskCycle mCycle = currentBookTask.getCycle();
        BookTask tmpTask = mDTV.getBookManager().getTaskByID(currentBookTask.getId());
        if (null != tmpTask) {
            if (mCycle == EnTaskCycle.WEEKLY) {
                Calendar calendar = tmpTask.getStartDateCalendar();
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                tmpTask.setStartDateCalendar(calendar);
                tmpTask.setEnable(true);
                mDTV.getBookManager().updateTask(tmpTask);
            } else if (mCycle == EnTaskCycle.DAILY) {
                Calendar calendar = tmpTask.getStartDateCalendar();
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                tmpTask.setStartDateCalendar(calendar);
                tmpTask.setEnable(true);
                mDTV.getBookManager().updateTask(tmpTask);
            } else if (mCycle == EnTaskCycle.ONETIME) {
                mDTV.getBookManager().deleteTask(currentBookTask);
                mBookArrive = false;
            }
        } else {
            LogTool.d(LogTool.MREC, "deleteTask");
            mDTV.getBookManager().deleteTask(currentBookTask);
            mBookArrive = false;
        }
    }

    protected void onStop() {
        if (mBookArrive) {
            sendArriveBroadcast();
        }
        deleteOrUpdateTask();
        LogTool.d(LogTool.MREC, "onStop mBookArriving = " + mBookArrive);
        super.onStop();
    }
     /**
     * second to time
     */
    public String secondToTime(int second) {
        LogTool.d(LogTool.MREC, "duration = " + second);
        int days = second / 86400;
        second = second % 86400;
        int hours = second / 3600;
        second = second % 3600;
        int minutes = second / 60;
        second = second % 60;
        String sDay = mContext.getResources().getString(R.string.epg_book_date_day);
        String sHours = mContext.getResources().getString(R.string.epg_book_date_hour);
        String sMinutes = mContext.getResources().getString(R.string.epg_book_date_minute);
        String sSecond = mContext.getResources().getString(R.string.epg_book_date_second);
        if (days > 0) {
            return days + sDay + hours + sHours + minutes + sMinutes + second + sSecond + "";
        } else {
            return hours + sHours + minutes + sMinutes + second + sSecond + "";
        }
    }
}
