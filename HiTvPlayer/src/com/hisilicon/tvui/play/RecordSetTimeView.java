package com.hisilicon.tvui.play;

import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.pvr.Constant;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TimeUtil;

import java.util.Calendar;

public class RecordSetTimeView extends BaseView
{
    private MainActivity mMainActivity;

    private EditText mHourEditText;
    private EditText mMinEditText;
    private TextWatcher mTextWatcher = null;
    private TextWatcher mTextWatcherHour = null;
    private TextWatcher mTextWatcherToNext = null;
    private LinearLayout mAllLinearLayout = null;
    private OnRecordClick mOnRecordClick = null;
    private int repeatFrequency=0;
    private int SUP_PVR_SECOND=120;
    public RecordSetTimeView(MainActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.ly_record_time_set));
        mAllLinearLayout = (LinearLayout) arg0.findViewById(R.id.ly_record_time_set);
        mMainActivity = arg0;
        initView();
    }

    public int getRecordTimeHour()
    {
        String hourString = mHourEditText.getText().toString();
        int hour = Integer.parseInt(hourString);

        return hour;
    }

    public int getRecordTimeMin()
    {
        String minString = mMinEditText.getText().toString();
        int min = Integer.parseInt(minString);

        return min;
    }

    /* 回调函数，开始录制时调用。由上层实现  */
    public interface OnRecordClick
    {
        void onRecord(int duration);
    }

    public void setRecordListener(OnRecordClick dialogListener)
    {
        mOnRecordClick = dialogListener;
    }

    private void initView()
    {
        Button mOKButton = (Button) mMainActivity.findViewById(R.id.record_time_yes);
        Button mCancelButton = (Button) mMainActivity.findViewById(R.id.record_time_no);
        mHourEditText = (EditText) mMainActivity.findViewById(R.id.record_time_hour);
        mMinEditText = (EditText) mMainActivity.findViewById(R.id.record_time_min);

        setEditTextInputLimit(mMinEditText, 0, 59);
        setEditTextInputHour(mHourEditText, 1);
        toNextWidget(mHourEditText);
        toDownWidget(mMinEditText);
        setEditTextSmallMin(mHourEditText, 1);
        setEditTextSmallMin(mMinEditText, 1);
        setEditTextSelectAll(mHourEditText);
        setEditTextSelectAll(mMinEditText);
        mOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                int minutes = calendar.get(Calendar.MINUTE);
                int seconds = calendar.get(Calendar.SECOND);
                int nowTime = hours * 3600 + minutes * 60 + seconds;

                int powerOffTime = Settings.System.getInt(mMainActivity.getContentResolver(),
                        Constant.SETTING_POWEROFF_TIME, 0);
                int time = (powerOffTime - nowTime) > 0 ? (powerOffTime - nowTime) : 0;
                repeatFrequency = Settings.System.getInt(mMainActivity.getContentResolver(),
                        Constant.SETTING_POWEROFF_REPEAT, 0);
                LogTool.i(LogTool.MPLAY, "bootTime=" + TimeUtil.getBootTime()+";powerOffTime"+ powerOffTime+";nowTime"+nowTime);
                if(TimeUtil.getBootTime()<SUP_PVR_SECOND||(time<SUP_PVR_SECOND&&time>0)){
                    Toast.makeText(mMainActivity,mMainActivity.getResources().getString(R.string.pvr_rec_offoron_sup),Toast.LENGTH_SHORT).show();
                }else {
                int hour = getRecordTimeHour();
                int min = getRecordTimeMin();
                int mduration = (hour * 60 * 60) + (min * 60);
                /* Start record */
                mAllLinearLayout.setVisibility(View.GONE);
                mOnRecordClick.onRecord(mduration);
                }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAllLinearLayout.setVisibility(View.GONE);
            }
        });
    }

    private void setEditTextInputLimit(final EditText editText, final int min, final int max)
    {
        if (null != mTextWatcher)
        {
            editText.removeTextChangedListener(mTextWatcher);
        }

        mTextWatcher = new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable editable)
            {
                int maximum = max;

                if (!editable.toString().isEmpty())
                {
                    int value = Integer.parseInt(editable.toString());
                    if (value > maximum)
                    {
                        editText.setText(String.valueOf(maximum));
                        editText.selectAll();
                    }
                }
                else
                {
                    editText.setText(String.valueOf(min));
                    editText.selectAll();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

        };
        editText.addTextChangedListener(mTextWatcher);
    }

    private void setEditTextInputHour(final EditText editText, final int min)
    {
        if (null != mTextWatcherHour)
        {
            editText.removeTextChangedListener(mTextWatcherHour);
        }

        mTextWatcherHour = new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable editable)
            {

                if (editable.toString().isEmpty())
                {
                    editText.setText(String.valueOf(min));
                    editText.selectAll();
                }

            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

        };
        editText.addTextChangedListener(mTextWatcherHour);
    }

    private void toNextWidget(final EditText editText)
    {
        if (null != mTextWatcherToNext)
        {
            editText.removeTextChangedListener(mTextWatcherToNext);
        }

        mTextWatcherToNext = new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable editable)
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

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

        };
        editText.addTextChangedListener(mTextWatcherToNext);
    }

    private void toDownWidget(final EditText editText)
    {
        if (null != mTextWatcherToNext)
        {
            editText.removeTextChangedListener(mTextWatcherToNext);
        }

        mTextWatcherToNext = new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable editable)
            {
                if (editText.getText().toString().length() == 2)
                {
                    View v = editText.focusSearch(View.FOCUS_DOWN);
                    if (v != null)
                    {
                        v.requestFocus(View.FOCUS_DOWN);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

        };
        editText.addTextChangedListener(mTextWatcherToNext);
    }

    private void setEditTextSmallMin(final EditText editText, final int min)
    {
        if (null != mTextWatcherToNext)
        {
            editText.removeTextChangedListener(mTextWatcherToNext);
        }

        mTextWatcherToNext = new TextWatcher()
        {

            @Override
            public void afterTextChanged(Editable editable)
            {

                if (!editable.toString().isEmpty())
                {
                    int valueHour = Integer.parseInt(mHourEditText.getText().toString());
                    int valueMin = Integer.parseInt(mMinEditText.getText().toString());
                    if (0 == (valueHour + valueMin))
                    {
                        mMinEditText.setText(String.valueOf(min));
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int arg1, int arg2, int arg3)
            {

            }

        };
        editText.addTextChangedListener(mTextWatcherToNext);
    }

    private void setEditTextSelectAll(final EditText editText)
    {
        editText.setOnFocusChangeListener(new OnFocusChangeListener()
        {

            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if (hasFocus)
                {
                    editText.selectAll();
                }
            }

        });
    }

    public boolean onListViewKeyDown(int keyCode, android.view.KeyEvent event)
    {
        if (KeyValue.DTV_KEYVALUE_BACK == keyCode)
        {
            hide();
        }
        return false;
    }
}
