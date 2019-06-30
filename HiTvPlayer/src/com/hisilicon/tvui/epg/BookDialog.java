package com.hisilicon.tvui.epg;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookManager;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.book.EnTaskType;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.record.RecordingListActivity;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;
import com.hisilicon.tvui.view.MyToast;

public class BookDialog extends Dialog implements OnComboxSelectChangeListener, View.OnClickListener {
    private TextView mChannelTextView = null;

    private Context mContext = null;

    private EditText mNameEditText = null;

    private EditText mStartDateYearEditText = null;

    private EditText mStartDateMonthEditText = null;

    private EditText mStartDateDayEditText = null;

    private EditText mStartTimeHourEditText = null;

    private EditText mStartTimeMinuteEditText = null;

    private EditText mStartTimeSecondEditText = null;

    private EditText mDurationHourEditText = null;

    private EditText mDurationMinuteEditText = null;

    private EditText mDurationSecondEditText = null;

    private TextWatcher mTextWatcher = null;

    private TextWatcher mTextWatcherRight = null;

    private TextWatcher mTextWatcherDown = null;

    private Combox mTypeCombox = null;

    private Combox mCycleCombox = null;

    private BookManager mBookManager = null;

    private BookTask mBookTask = null;

    private TimeManager mTimeManager = null;

    private RecordingListActivity mRecordingListActivity = null;

    private String mChannelName = null;

    private Date mStartDate = null;

    private Calendar mStartDateCalendar = null;

    private long mDuration = 0;

    private long mSaveDuration = 0;

    private final Calendar mDurationCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    private static final int MAX_BOOK_NAME_LENGTH = 64;

    private static final long DEFAULT_DURATION = 3600000;

    private static final long ONE_MINUTE = 60000;

    private static final long ONE_DAY = 86400000;

    private static final long SATRT_DATE_DELAY_TIME = 5 * 60 * 1000;

    private static final int BOOK_EVENT_OVERDUE_RET = -3;

    public BookDialog(Context context, RecordingListActivity recordingListActivity, BookTask bookTask, int x, int y, float alpha) {
        super(context, R.style.DIM_STYLE);

        this.mContext = context;

        this.mRecordingListActivity = recordingListActivity;

        this.mBookManager = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME).getBookManager();

        this.mBookTask = bookTask;
        if (bookTask != null) {
            mStartDateCalendar = bookTask.getStartDateCalendar();
            if (mStartDateCalendar == null) {
                mStartDateCalendar = Calendar.getInstance();
            }
        }
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

        mTimeManager = mDTV.getNetworkManager().getTimeManager();

        if (null == mBookTask) {
            mBookTask = mBookManager.createTask();
        }
        LogTool.d(LogTool.MEPG, "mBookTask:id = " + mBookTask.getId());

        Date date = new Date(0);
        this.mDurationCalendar.setTime(date);

        // Set layout parameters of this dialog
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        this.getWindow().setGravity(Gravity.RIGHT);
        lp.x = x;
        lp.y = y;
        lp.alpha = alpha;
        this.getWindow().setAttributes(lp);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.epg_book_dialog);

        mChannelTextView = (TextView) findViewById(R.id.tv_epg_book_channel);
        initChannelNameView();

        mNameEditText = (EditText) findViewById(R.id.et_epg_book_name);
        setEditTextSelectAll(mNameEditText);
        initNameView();

        mTypeCombox = (Combox) findViewById(R.id.cbx_epg_book_type);
        initTypeCombox();

        mCycleCombox = (Combox) findViewById(R.id.cbx_epg_book_cycle);
        initCycleCombox();

        initStartDateCalendar();

        mStartDateYearEditText = (EditText) findViewById(R.id.et_epg_book_start_date_year);
        setEditTextInputLimit(mStartDateYearEditText, 1, 9999);
        setEditTextSelectAll(mStartDateYearEditText);

        mStartDateMonthEditText = (EditText) findViewById(R.id.et_epg_book_start_date_month);
        setEditTextInputLimit(mStartDateMonthEditText, 1, 12);
        setEditTextSelectAll(mStartDateMonthEditText);

        mStartDateDayEditText = (EditText) findViewById(R.id.et_epg_book_start_date_day);
        setEditTextInputLimit(mStartDateDayEditText, 1, mStartDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        setEditTextSelectAll(mStartDateDayEditText);

        mStartTimeHourEditText = (EditText) findViewById(R.id.et_epg_book_start_time_hour);
        setEditTextInputLimit(mStartTimeHourEditText, 0, 23);
        setEditTextSelectAll(mStartTimeHourEditText);

        mStartTimeMinuteEditText = (EditText) findViewById(R.id.et_epg_book_start_time_minute);
        setEditTextInputLimit(mStartTimeMinuteEditText, 0, 59);
        setEditTextSelectAll(mStartTimeMinuteEditText);

        mStartTimeSecondEditText = (EditText) findViewById(R.id.et_epg_book_start_time_second);
        setEditTextInputLimit(mStartTimeSecondEditText, 0, 59);
        setEditTextSelectAll(mStartTimeSecondEditText);

        initStartDateView();
        setEditTextInputToRight(mStartDateYearEditText, 4);
        setEditTextInputToRight(mStartDateMonthEditText, 2);
        setEditTextInputToDown(mStartDateDayEditText, 2);
        setEditTextInputToRight(mStartTimeHourEditText, 2);
        setEditTextInputToRight(mStartTimeMinuteEditText, 2);
        setEditTextInputToDown(mStartTimeSecondEditText, 2);

        initDurationCalendar();

        mDurationHourEditText = (EditText) findViewById(R.id.et_epg_book_duration_hour);
        setEditTextInputLimit(mDurationHourEditText, 0, 23);
        setEditTextSelectAll(mDurationHourEditText);

        mDurationMinuteEditText = (EditText) findViewById(R.id.et_epg_book_duration_minute);
        setEditTextInputLimit(mDurationMinuteEditText, 0, 59);
        setEditTextSelectAll(mDurationMinuteEditText);

        mDurationSecondEditText = (EditText) findViewById(R.id.et_epg_book_duration_second);
        setEditTextInputLimit(mDurationSecondEditText, 0, 59);
        setEditTextSelectAll(mDurationSecondEditText);

        initDurationView();

        setEditTextInputToRight(mDurationHourEditText, 2);
        setEditTextInputToRight(mDurationMinuteEditText, 2);
        setEditTextInputToDown(mDurationSecondEditText, 2);

        Button mBookButton = (Button) findViewById(R.id.btn_epg_book_ok);
        mBookButton.setOnClickListener(this);

        Button mCancelButton = (Button) findViewById(R.id.btn_epg_book_cancel);
        mCancelButton.setOnClickListener(this);
    }

    private void initChannelNameView() {
        ChannelManager channelManager = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME).getChannelManager();
        Channel channel = channelManager.getChannelByID(mBookTask.getChannelId());
        if (null != channel) {
            mChannelName = channel.getChannelName();
        }

        if (null == mChannelName) {
            mChannelName = "";
        }

        mChannelTextView.setText(mChannelName);
    }

    private void initNameView() {
        String mName = mBookTask.getName();

        if ((null == mName) || (mName.isEmpty())) {
            mName = mChannelName;
        }

        if (mName.length() > MAX_BOOK_NAME_LENGTH) {
            mName = mName.substring(0, MAX_BOOK_NAME_LENGTH - 1);
        }

        mNameEditText.setText(mName);

        mNameEditText.requestFocus();
    }

    private void initTypeCombox() {
        LinkedHashMap<String, Object> typeMap = new LinkedHashMap<String, Object>();
        String[] typeArray = mContext.getResources().getStringArray(R.array.type_array);
        typeMap.put(typeArray[EnTaskType.RECORD.ordinal()], EnTaskType.RECORD);
        typeMap.put(typeArray[EnTaskType.PLAY.ordinal()], EnTaskType.PLAY);
        mTypeCombox.setData(typeMap);
        EnTaskType enTaskType = mBookTask.getType();
        if ((null == enTaskType) || (EnTaskType.UNKNOW == enTaskType)) {
            mBookTask.setType((EnTaskType) mTypeCombox.getTag());
        } else {
            mTypeCombox.setText(typeArray[enTaskType.ordinal()]);
        }
        mTypeCombox.setOnSelectChangeListener(this);
    }

    private void initCycleCombox() {
        LinkedHashMap<String, Object> cycleMap = new LinkedHashMap<String, Object>();
        String[] cycleArray = mContext.getResources().getStringArray(R.array.cycle_array);
        cycleMap.put(cycleArray[EnTaskCycle.ONETIME.ordinal()], EnTaskCycle.ONETIME);
        cycleMap.put(cycleArray[EnTaskCycle.DAILY.ordinal()], EnTaskCycle.DAILY);
        cycleMap.put(cycleArray[EnTaskCycle.WEEKLY.ordinal()], EnTaskCycle.WEEKLY);
        mCycleCombox.setData(cycleMap);
        EnTaskCycle enTaskCycle = mBookTask.getCycle();
        if ((null == enTaskCycle) || (EnTaskCycle.UNKNOW == enTaskCycle)) {
            mBookTask.setCycle((EnTaskCycle) mCycleCombox.getTag());
        } else {
            mCycleCombox.setText(cycleArray[enTaskCycle.ordinal()]);
        }
        mCycleCombox.setOnSelectChangeListener(this);
    }

    private void initStartDateCalendar() {
        mStartDate = mBookTask.getStartDateCalendar().getTime();
        Calendar currentCalendar = mTimeManager.getCalendarTime();
        Date currentDate = null;
        if (currentCalendar == null) {
            currentDate = new Date();
        } else {
            currentDate = currentCalendar.getTime();
        }
        if ((null == mStartDate) || (mStartDate.before(currentDate) || mStartDate.equals(currentDate))) {
            long startDateMillisecond = (currentDate.getTime()) + SATRT_DATE_DELAY_TIME;
            long durationOffsetMillisecond = -1;
            if (null != mStartDate) {
                durationOffsetMillisecond = startDateMillisecond - mStartDate.getTime();
            }

            mStartDate = new Date(startDateMillisecond);
            if (durationOffsetMillisecond > 0) {
                int durationOffset = ((int) durationOffsetMillisecond) / 1000;
                int duration = mBookTask.getDuration() - durationOffset;
                if (duration > 0) {
                    mBookTask.setDuration(duration);
                    LogTool.d(LogTool.MEPG, "mBookTask.setDuration(" + duration + ")");
                }
            }
        }
        LogTool.d(LogTool.MEPG, "mStartDate = " + mStartDate.toString());

        mStartDateCalendar.setTime(mStartDate);
    }

    private void initStartDateView() {
        initStartDateYearEditText();
        initStartDateMonthEditText();
        initStartDateDayEditText();
        initStartTimeEditText(mStartTimeHourEditText, Calendar.HOUR_OF_DAY);
        initStartTimeEditText(mStartTimeMinuteEditText, Calendar.MINUTE);
        initStartTimeEditText(mStartTimeSecondEditText, Calendar.SECOND);
    }

    private void initStartDateYearEditText() {
        int year = mStartDateCalendar.get(Calendar.YEAR);
        DecimalFormat format = new DecimalFormat("0000");
        mStartDateYearEditText.setText(format.format(year));
    }

    private void initStartDateMonthEditText() {
        int month = mStartDateCalendar.get(Calendar.MONTH);
        month += 1;
        DecimalFormat format = new DecimalFormat("00");
        mStartDateMonthEditText.setText(format.format(month));
    }

    private void initStartDateDayEditText() {
        int day = mStartDateCalendar.get(Calendar.DAY_OF_MONTH);
        DecimalFormat format = new DecimalFormat("00");
        mStartDateDayEditText.setText(format.format(day));
    }

    private void initStartTimeEditText(EditText editText, int constant) {
        int text = mStartDateCalendar.get(constant);
        DecimalFormat format = new DecimalFormat("00");
        editText.setText(format.format(text));
    }

    private void initDurationCalendar() {
        mDuration = (long) mBookTask.getDuration() * 1000;

        if (mDuration < 0) {
            mDuration = DEFAULT_DURATION;
        }

        long durationMillisecond = mDuration;

        Date durationDate = new Date(durationMillisecond);

        mDurationCalendar.setTime(durationDate);
    }

    private void initDurationView() {
        if (null != mRecordingListActivity) {
            EnTaskType enTaskType = (EnTaskType) mTypeCombox.getTag();
            if ((null != enTaskType) && (EnTaskType.PLAY == enTaskType)) {
                enableDurationView(false);
            }
        }

        initDurationTextView(mDurationHourEditText, Calendar.HOUR_OF_DAY);
        initDurationTextView(mDurationMinuteEditText, Calendar.MINUTE);
        initDurationTextView(mDurationSecondEditText, Calendar.SECOND);
    }

    private void initDurationTextView(TextView textView, int constant) {
        if (null != mRecordingListActivity) {
            EnTaskType enTaskType = (EnTaskType) mTypeCombox.getTag();
            if ((null != enTaskType) && (EnTaskType.PLAY == enTaskType)) {
                textView.setFocusable(false);
                textView.setFocusableInTouchMode(false);
            }
        }
        int text = mDurationCalendar.get(constant);
        DecimalFormat format = new DecimalFormat("00");
        textView.setText(format.format(text));
    }

    private void setEditTextInputLimit(final EditText editText, final int min, final int max) {
        if (null != mTextWatcher) {
            editText.removeTextChangedListener(mTextWatcher);
        }

        mTextWatcher = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                int maximum = max;
                if (R.id.et_epg_book_start_date_day == editText.getId()) {
                    maximum = mStartDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                }

                if (!editable.toString().isEmpty()) {
                    int value = Integer.parseInt(editable.toString());
                    if (value < min) {
                        editText.setText(String.valueOf(min));
                        editText.selectAll();
                    } else if (value > maximum) {
                        editText.setText(String.valueOf(maximum));
                        editText.selectAll();
                    }
                } else {
                    editText.setText(String.valueOf(min));
                    editText.selectAll();
                }

                doWhileInput(editText.getId());
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

        };
        editText.addTextChangedListener(mTextWatcher);
    }

    private void setEditTextInputToRight(final EditText editTextFrom, final int maxLength) {
        if (null != mTextWatcherRight) {
            editTextFrom.removeTextChangedListener(mTextWatcherRight);
        }

        mTextWatcherRight = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextFrom.isFocused()) {
                    if (editTextFrom.getText().toString().length() >= maxLength) {
                        View v = editTextFrom.focusSearch(View.FOCUS_RIGHT);
                        if (v != null) {
                            v.requestFocus(View.FOCUS_RIGHT);
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

        };
        editTextFrom.addTextChangedListener(mTextWatcherRight);
    }

    private void setEditTextInputToDown(final EditText editTextFrom, final int maxLength) {
        if (null != mTextWatcherDown) {
            editTextFrom.removeTextChangedListener(mTextWatcherDown);
        }

        mTextWatcherDown = new TextWatcher() {

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTextFrom.isFocused()) {
                    if (editTextFrom.getText().toString().length() >= maxLength) {
                        View v = editTextFrom.focusSearch(View.FOCUS_DOWN);
                        if (v != null) {
                            v.requestFocus(View.FOCUS_DOWN);
                        }
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3) {

            }

        };
        editTextFrom.addTextChangedListener(mTextWatcherDown);
    }

    private void doWhileInput(int id) {
        switch (id) {
            case R.id.et_epg_book_start_date_year: {
                setStartDateYear();
                break;
            }
            case R.id.et_epg_book_start_date_month: {
                setStartDateMonth();
                break;
            }
            case R.id.et_epg_book_start_date_day: {
                setStartDateDay();
                break;
            }
            case R.id.et_epg_book_start_time_hour: {
                setStartTimeHour();
                break;
            }
            case R.id.et_epg_book_start_time_minute: {
                setStartTimeMinute();
                break;
            }
            case R.id.et_epg_book_start_time_second: {
                setStartTimeSecond();
                break;
            }
            case R.id.et_epg_book_duration_hour: {
                setDurationHour();
                break;
            }
            case R.id.et_epg_book_duration_minute: {
                setDurationMinute();
                break;
            }
            case R.id.et_epg_book_duration_second: {
                setDurationSecond();
                break;
            }
            default: {
                break;
            }
        }

    }

    private void setStartDateYear() {
        String yearString = mStartDateYearEditText.getText().toString();
        if (!yearString.isEmpty()) {
            int year = Integer.parseInt(yearString);
            int day = mStartDateCalendar.get(Calendar.DAY_OF_MONTH);
            mStartDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
            mStartDateCalendar.set(Calendar.YEAR, year);

            int maxDay = mStartDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (day > maxDay) {
                day = maxDay;
            }
            mStartDateCalendar.set(Calendar.DAY_OF_MONTH, day);
            mStartDateDayEditText.setText(String.valueOf(day));
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setStartDateMonth() {
        String monthString = mStartDateMonthEditText.getText().toString();
        if (!monthString.isEmpty()) {
            int month = Integer.parseInt(monthString) - 1;
            int day = mStartDateCalendar.get(Calendar.DAY_OF_MONTH);
            mStartDateCalendar.set(Calendar.DAY_OF_MONTH, 1);
            mStartDateCalendar.set(Calendar.MONTH, month);

            int maxDay = mStartDateCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (day > maxDay) {
                day = maxDay;
                mStartDateDayEditText.setText(String.valueOf(day));
            }
            mStartDateCalendar.set(Calendar.DAY_OF_MONTH, day);
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setStartDateDay() {
        String dayString = mStartDateDayEditText.getText().toString();
        if (!dayString.isEmpty()) {
            int day = Integer.parseInt(dayString);
            mStartDateCalendar.set(Calendar.DAY_OF_MONTH, day);
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setStartTimeHour() {
        String hourString = mStartTimeHourEditText.getText().toString();
        if (!hourString.isEmpty()) {
            int hour = Integer.parseInt(hourString);
            mStartDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setStartTimeMinute() {
        String minuteString = mStartTimeMinuteEditText.getText().toString();
        if (!minuteString.isEmpty()) {
            int minute = Integer.parseInt(minuteString);
            mStartDateCalendar.set(Calendar.MINUTE, minute);
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setStartTimeSecond() {
        String secondString = mStartTimeSecondEditText.getText().toString();
        if (!secondString.isEmpty()) {
            int second = Integer.parseInt(secondString);
            mStartDateCalendar.set(Calendar.SECOND, second);
        }

        mStartDate = mStartDateCalendar.getTime();
    }

    private void setDurationHour() {
        String hourString = mDurationHourEditText.getText().toString();
        if (!hourString.isEmpty()) {
            int hour = Integer.parseInt(hourString);
            mDurationCalendar.set(Calendar.HOUR_OF_DAY, hour);
        }

        setDuration();
    }

    private void setDurationMinute() {
        String minuteString = mDurationMinuteEditText.getText().toString();
        if (!minuteString.isEmpty()) {
            int minute = Integer.parseInt(minuteString);
            mDurationCalendar.set(Calendar.MINUTE, minute);
        }

        setDuration();
    }

    private void setDurationSecond() {
        String secondString = mDurationSecondEditText.getText().toString();
        if (!secondString.isEmpty()) {
            int second = Integer.parseInt(secondString);
            mDurationCalendar.set(Calendar.SECOND, second);
        }

        setDuration();
    }

    private void setDuration() {
        // More than a day's rounding off
        mDuration = mDurationCalendar.getTime().getTime() > ONE_DAY ? mDurationCalendar.getTime().getTime() % ONE_DAY : mDurationCalendar.getTime().getTime();
        LogTool.d(LogTool.MEPG, "mDuration = " + mDuration);
        if ((EnTaskType.RECORD == mBookTask.getType()) && (mDuration < ONE_MINUTE)) {
            int second = 0;
            int min = 0;
            int hour = 0;
            mDuration = ONE_MINUTE;
            mDuration += mDurationCalendar.getTimeInMillis();
            Date date = new Date(mDuration);
            mDurationCalendar.setTime(date);
            hour = mDurationCalendar.get(Calendar.HOUR_OF_DAY);
            min = mDurationCalendar.get(Calendar.MINUTE);
            second = mDurationCalendar.get(Calendar.SECOND);
            mDurationHourEditText.setText(String.valueOf(hour));
            mDurationMinuteEditText.setText(String.valueOf(min));
            mDurationSecondEditText.setText(String.valueOf(second));
        }
    }

    private void setEditTextSelectAll(final EditText editText) {
        editText.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    editText.selectAll();
                } else {
                    doAfterInput(view.getId());
                }
            }

        });
    }

    private void doAfterInput(int id) {
        switch (id) {
            case R.id.et_epg_book_start_date_year: {
                initStartDateYearEditText();
                break;
            }
            case R.id.et_epg_book_start_date_month: {
                initStartDateMonthEditText();
                break;
            }
            case R.id.et_epg_book_start_date_day: {
                initStartDateDayEditText();
                break;
            }
            case R.id.et_epg_book_start_time_hour: {
                initStartTimeEditText(mStartTimeHourEditText, Calendar.HOUR_OF_DAY);
                break;
            }
            case R.id.et_epg_book_start_time_minute: {
                initStartTimeEditText(mStartTimeMinuteEditText, Calendar.MINUTE);
                break;
            }
            case R.id.et_epg_book_start_time_second: {
                initStartTimeEditText(mStartTimeSecondEditText, Calendar.SECOND);
                break;
            }
            case R.id.et_epg_book_duration_hour: {
                //initDurationView();
                initDurationTextView(mDurationHourEditText, Calendar.HOUR_OF_DAY);
                break;
            }
            case R.id.et_epg_book_duration_minute: {
                //initDurationView();
                initDurationTextView(mDurationMinuteEditText, Calendar.MINUTE);
                break;
            }
            case R.id.et_epg_book_duration_second: {
                //initDurationView();
                initDurationTextView(mDurationSecondEditText, Calendar.SECOND);
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onComboxSelectChange(View view, String str, Object obj, int i) {
        switch (view.getId()) {
            case R.id.cbx_epg_book_type: {
                EnTaskType type = (EnTaskType) obj;
                mBookTask.setType(type);

                Date date = new Date(0);
                if (EnTaskType.PLAY == type) {
                    if (mDuration > date.getTime()) {
                        mSaveDuration = mDuration;
                    }

                    enableDurationView(false);
                } else {
                    date = new Date(mSaveDuration);

                    enableDurationView(true);
                }

                mDurationCalendar.setTime(date);

                initDurationView();

                break;
            }
            case R.id.cbx_epg_book_cycle: {
                mBookTask.setCycle((EnTaskCycle) obj);

                break;
            }
            default: {
                break;
            }
        }
    }

    private void enableDurationView(boolean b) {
        mDurationHourEditText.setFocusable(b);
        mDurationHourEditText.setFocusableInTouchMode(b);

        mDurationMinuteEditText.setFocusable(b);
        mDurationMinuteEditText.setFocusableInTouchMode(b);

        mDurationSecondEditText.setFocusable(b);
        mDurationSecondEditText.setFocusableInTouchMode(b);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_epg_book_ok: {
                dismiss();
                Calendar calendar = mTimeManager.getCalendarTime();
                Date date = null;
                if (calendar == null) {
                    date = new Date();
                } else {
                    int month = calendar.get(Calendar.MONTH) + 1;
                    String dateString = " " + calendar.get(Calendar.YEAR) + "-"
                            + month + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " "
                            + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                            + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND) + " ";
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    try {
                        date = sdf.parse(dateString);
                    } catch(ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (mStartDate.before(date)) {
                    MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_date_invalid), MyToast.LENGTH_SHORT).show();
                    LogTool.d(LogTool.MEPG, "mStartDate:" + mStartDate + "  data:" + date);
                } else {
                    setBookTask();
                    LogTool.d(LogTool.MEPG, "mStartDate:" + mStartDate + "  data:" + date + "    mBookTask:" + mBookTask.toString());
                    int ret = 0;
                    if (null == mRecordingListActivity) {
                        ret = addBookTask();
                        showBookResult(ret);
                        LogTool.d(LogTool.MEPG, "addBookTask result:" + ret);
                    } else {
                        ret = mBookManager.updateTask(mBookTask);
                        showUpdateBookResult(ret);
                        LogTool.d(LogTool.MEPG, "updateTask result:" + ret);
                    }
                }
                break;
            }
            case R.id.btn_epg_book_cancel: {
                dismiss();

                break;
            }
            default: {
                break;
            }
        }
    }

    private void setBookTask() {
        if (null != mNameEditText.getText()) {
            LogTool.d(LogTool.MEPG, "mName = " + mNameEditText.getText().toString());
            mBookTask.setName(mNameEditText.getText().toString());
        } else {
            mBookTask.setName("");
        }

        if (mBookTask.getEventId() < 0) {
            mBookTask.setEventId(0);
        }

        LogTool.d(LogTool.MEPG, "mStartDate" + mStartDate.toString());

        //Date tmp = mStartDate;
        //long millsecond = tmp.getTime() - ((long) mTimeManager.getTimeZone() * 1000);
        //tmp.setTime(millsecond);
        //mBookTask.setStartDate(tmp);

        //mBookTask.setStartDate(mStartDate);
        mBookTask.setStartDateCalendar(mStartDateCalendar);

        LogTool.d(LogTool.MEPG, "mDuration = " + mDuration);
        int duration = ((int) mDuration) / 1000;
        mBookTask.setDuration(duration);

        mBookTask.setEnable(true);
    }

    private int addBookTask() {
        int ret = mBookManager.addTask(mBookTask);
        LogTool.d(LogTool.MEPG, "addTask result:" + ret);
        if (ret >= 0) {
            mBookTask.setId(ret);
        }

        return ret;
    }

    private void showBookResult(int ret) {
        LogTool.d(LogTool.MEPG, "showBookResult ret = " + ret);
        if (ret >= 0) {
            MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_success_tip), MyToast.LENGTH_LONG).show();

            if (null != mRecordingListActivity) {
                mRecordingListActivity.updateBookListView();
            }
        } else {
            ArrayList<BookTask> conflictBookTaskList = (ArrayList<BookTask>) mBookManager.findConflictTasks(mBookTask);

            if (BOOK_EVENT_OVERDUE_RET == ret) {
                MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_time_error_tip), MyToast.LENGTH_LONG).show();
            } else if ((null != conflictBookTaskList) && (!conflictBookTaskList.isEmpty())) {
                LogTool.d(LogTool.MEPG, "mConflictBookTasksList.size() = " + conflictBookTaskList.size());
                BookConflictTipDialog bookConflictTipDialog = new BookConflictTipDialog(mContext, mRecordingListActivity, mBookManager, conflictBookTaskList,
                        mBookTask);
                bookConflictTipDialog.show();
            } else {
                MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_failure_tip), MyToast.LENGTH_LONG).show();
            }
        }
    }

    private void showUpdateBookResult(int ret) {
        LogTool.d(LogTool.MEPG, "showUpdateBookResult ret = " + ret);
        if (ret == 0) {
            MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_update_success_tip), MyToast.LENGTH_LONG).show();

            if (null != mRecordingListActivity) {
                mRecordingListActivity.updateBookListView();
            }
        } else {
            ArrayList<BookTask> conflictBookTaskList = (ArrayList<BookTask>) mBookManager.findConflictTasks(mBookTask);
            if (BOOK_EVENT_OVERDUE_RET == ret) {
                MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_time_error_tip), MyToast.LENGTH_LONG).show();
            } else if ((null != conflictBookTaskList) && (!conflictBookTaskList.isEmpty())) {
                LogTool.d(LogTool.MEPG, "update mConflictBookTasksList.size() = " + conflictBookTaskList.size());
                BookConflictTipDialog bookConflictTipDialog = new BookConflictTipDialog(mContext, mRecordingListActivity, mBookManager, conflictBookTaskList,
                        mBookTask);
                bookConflictTipDialog.show();
            } else {
                MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_update_failure_tip), MyToast.LENGTH_LONG).show();
            }
        }
    }

}
