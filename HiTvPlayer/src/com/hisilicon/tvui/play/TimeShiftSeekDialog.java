package com.hisilicon.tvui.play;

import java.util.Calendar;
import java.util.Date;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.view.DigtalEditText;
import com.hisilicon.tvui.view.DigtalEditText.OnDigtalEditTextChangeListener;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.tvui.util.LogTool;

public class TimeShiftSeekDialog extends Dialog implements OnDigtalEditTextChangeListener
{
    private Context mContext;
    private DigtalEditText mTimeshiftSeekHour;
    private DigtalEditText mTimeshiftSeekMinute;
    private DigtalEditText mTimeshiftSeekSecond;
    private TimeshiftView mDtvTimeShift;
    private int mBeginHour;
    private int mBeginMinute;
    private int mBeginSecond;
    private int mRecordHour;
    private int mRecordMinute;
    private int mRecordSecond;
    private int mSeekHour;
    private int mSeekMinute;
    private int mSeekSecond;
    private final static int HOUR_SECONDS = 3600;
    private final static int MINUTE_SECONDS = 60;
    private final static int DAY_HOURS = 24;

    public TimeShiftSeekDialog(Context context, int theme, TimeshiftView timeShift)
    {
        super(context, theme);
        mContext = context;
        mDtvTimeShift = timeShift;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeshift_seek);
        initView();
        mTimeshiftSeekHour.requestFocus();
    }

    private void refreshBeginTime()
    {
        Calendar calendar = mDtvTimeShift.getStartTimeCal();
        mBeginHour = calendar.get(Calendar.HOUR_OF_DAY);
        mBeginMinute = calendar.get(Calendar.MINUTE);
        mBeginSecond = calendar.get(Calendar.SECOND);
        return;
    }

    private void refreshRecordTime()
    {
        Calendar calendar = mDtvTimeShift.getRecordEndTimeCal();
        mRecordHour = calendar.get(Calendar.HOUR_OF_DAY);
        mRecordMinute = calendar.get(Calendar.MINUTE);
        mRecordSecond = calendar.get(Calendar.SECOND);
        return;
    }

    private boolean isSelectedSeekTimeRight()
    {
        int playTimeInt = mSeekHour * HOUR_SECONDS + mSeekMinute * MINUTE_SECONDS + mSeekSecond;
        refreshBeginTime();
        refreshRecordTime();
        int beginTimeInt = mBeginHour * HOUR_SECONDS + mBeginMinute * MINUTE_SECONDS + mBeginSecond;
        int recordTimeInt = mRecordHour * HOUR_SECONDS + mRecordMinute * MINUTE_SECONDS + mRecordSecond;
        if (mRecordHour < mBeginHour)
        {
            recordTimeInt = (mRecordHour + DAY_HOURS) * HOUR_SECONDS + mRecordMinute * MINUTE_SECONDS + mRecordSecond;
        }
        if (mSeekHour < mBeginHour)
        {
            mSeekHour = mSeekHour + DAY_HOURS;
            playTimeInt = mSeekHour * HOUR_SECONDS + mSeekMinute * MINUTE_SECONDS + mSeekSecond;
        }
        if ((playTimeInt > recordTimeInt) || (playTimeInt < beginTimeInt))
        {
            return false;
        }
        return true;
    }

    private void initEditText()
    {
        refreshBeginTime();
        mTimeshiftSeekHour.setText(String.format("%02d", mBeginHour));
        mTimeshiftSeekMinute.setText(String.format("%02d", mBeginMinute));
        mTimeshiftSeekSecond.setText(String.format("%02d", mBeginSecond));
    }

    @Override
    public void onDigtalEditTextChange(DigtalEditText editText, String strText)
    {
        if (editText.getText().toString().length() == 2)
        {
            View v = editText.focusSearch(View.FOCUS_RIGHT);
            if (v != null)
            {
                v.requestFocus(View.FOCUS_RIGHT);
            }
        }
    }

    private void initView()
    {
        mTimeshiftSeekHour = (DigtalEditText) this.findViewById(R.id.timeshift_seek_hour);
        mTimeshiftSeekMinute = (DigtalEditText) this.findViewById(R.id.timeshift_seek_minute);
        mTimeshiftSeekSecond = (DigtalEditText) this.findViewById(R.id.timeshift_seek_second);
        mTimeshiftSeekHour.setOnDigtalEditTextChangeListener(this);
        mTimeshiftSeekMinute.setOnDigtalEditTextChangeListener(this);
        mTimeshiftSeekSecond.setOnDigtalEditTextChangeListener(this);
        Button mTimeshiftSeekOK = (Button) this.findViewById(R.id.timeshift_seek_ok);
        initEditText();
        mTimeshiftSeekOK.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String timeshiftSeekHour = mTimeshiftSeekHour.getText().toString();
                String timeshiftSeekMinute = mTimeshiftSeekMinute.getText().toString();
                String timeshiftSeekSecond = mTimeshiftSeekSecond.getText().toString();
                if ((timeshiftSeekHour == null) || (0 == timeshiftSeekHour.length())) {
                    mTimeshiftSeekHour.requestFocus();
                    return;
                }
                if ((timeshiftSeekMinute == null) || (0 == timeshiftSeekMinute.length())) {
                    mTimeshiftSeekMinute.requestFocus();
                    return;
                }
                if ((timeshiftSeekSecond == null) || (0 == timeshiftSeekSecond.length())) {
                    mTimeshiftSeekSecond.requestFocus();
                    return;
                }
                mSeekHour = Integer.parseInt(timeshiftSeekHour);
                mSeekMinute = Integer.parseInt(timeshiftSeekMinute);
                mSeekSecond = Integer.parseInt(timeshiftSeekSecond);
                if (!isSelectedSeekTimeRight()) {
                    MyToast.makeText(mContext, mContext.getResources().getString(R.string.seek_time_error), MyToast.LENGTH_LONG).show();
                    initEditText();
                    return;
                } else {
                    Calendar calendar = mDtvTimeShift.getStartTimeCal();
                    int mOrgBeginHour = calendar.get(Calendar.HOUR_OF_DAY);
                    int mOrgBeginMinute = calendar.get(Calendar.MINUTE);
                    int mOrgBeginSecond = calendar.get(Calendar.SECOND);
                    long time = (long)(mSeekHour - mOrgBeginHour) * HOUR_SECONDS
                            + (long)(mSeekMinute - mOrgBeginMinute) * MINUTE_SECONDS
                            + (mSeekSecond - mOrgBeginSecond);
                    LogTool.w(LogTool.MREC, "seek time = " + time );
                    if (!mDtvTimeShift.seek(time)) {
                        MyToast.makeText(mContext, mContext.getResources().getString(R.string.seek_error), MyToast.LENGTH_LONG).show();
                    }
                    TimeShiftSeekDialog.this.cancel();
                    mDtvTimeShift.updateTimeshiftTime();
                    mDtvTimeShift.setTimeShiftInforbarVisible(true);
                }

            }

        });

    }

}
