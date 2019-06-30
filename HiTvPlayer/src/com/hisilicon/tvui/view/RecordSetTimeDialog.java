package com.hisilicon.tvui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;

import com.hisilicon.tvui.R;

public class RecordSetTimeDialog extends Dialog
{
    private EditText mHourEditText;
    private EditText mMinEditText;
    private TextWatcher mTextWatcher = null;
    private TextWatcher mTextWatcherHour = null;
    private TextWatcher mTextWatcherToNext = null;
    private OnRecordConfirmDialogListener mConfirmDialogListener;

    public void setRecordConfirmDialogListener(OnRecordConfirmDialogListener dialogListener)
    {
        mConfirmDialogListener = dialogListener;
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

    public RecordSetTimeDialog(Context context, int theme)
    {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_time_dialog);
        initView();
    }

    private void initView()
    {
        Button mOKButton = (Button) this.findViewById(R.id.record_time_yes);
        Button mCancelButton = (Button) this.findViewById(R.id.record_time_no);
        mHourEditText = (EditText) this.findViewById(R.id.record_time_hour);
        mMinEditText = (EditText) this.findViewById(R.id.record_time_min);

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
                if (null != mConfirmDialogListener) {
                    mConfirmDialogListener.onCheck(OnRecordConfirmDialogListener.OK);
                }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mConfirmDialogListener) {
                    mConfirmDialogListener.onCheck(OnRecordConfirmDialogListener.CANCEL);
                }
            }
        });
    }

    public interface OnRecordConfirmDialogListener
    {
        int OK = 0;
        int CANCEL = -1;

        void onCheck(int which);
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
}
