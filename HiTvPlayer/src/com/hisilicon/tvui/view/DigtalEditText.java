package com.hisilicon.tvui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.hisilicon.tvui.R;

public class DigtalEditText extends EditText
{
    private int mMaxNumber = 0xFFFFFFFF;
    private int mMinNumber = 0xFFFFFFFF;
    private TextWatcher mTextWatcher = null;
    private OnDigtalEditTextChangeListener mOnDigtalEditTextChangeListener = null;
    private DigtalEditText mSelfObject = null;

    /**
     * Interface definition for a callback to be invoked when an action is performed on text change
     */
    public interface OnDigtalEditTextChangeListener
    {
        void onDigtalEditTextChange(DigtalEditText arg0, String strText);
    }

    public DigtalEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        getAttrValues(context, attrs);
        init();
        mSelfObject = this;
    }

    private void getAttrValues(Context context, AttributeSet attrs)
    {
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.digtaledittext);
        if (null == mTypedArray)
        {
            return;
        }
        mMaxNumber = mTypedArray.getInteger(R.styleable.digtaledittext_maxnumber, 0xFFFFFFFF);
        mMinNumber = mTypedArray.getInteger(R.styleable.digtaledittext_minnumber, 0xFFFFFFFF);
        mTypedArray.recycle();
    }

    private void init()
    {
        this.setOnFocusChangeListener(new OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View arg0, boolean arg1)
            {
                if ((arg1) && (null != arg0))
                {
                    ((EditText) arg0).selectAll();
                }
            }
        });

        mTextWatcher = new TextWatcher()
        {
            @Override
            public void afterTextChanged(Editable editable)
            {
                if (!editable.toString().isEmpty())
                {
                    int value = Integer.parseInt(editable.toString());
                    if (value < mMinNumber)
                    {
                        mSelfObject.removeTextChangedListener(mTextWatcher);
                        if(mSelfObject.getText().toString().isEmpty())
                            mSelfObject.setText(String.valueOf(mMinNumber));
                        mSelfObject.selectAll();
                        mSelfObject.addTextChangedListener(mTextWatcher);
                        return;
                    }
                    else if (value > mMaxNumber)
                    {
                        mSelfObject.removeTextChangedListener(mTextWatcher);
                        if(mSelfObject.getText().toString().isEmpty())
                            mSelfObject.setText(String.valueOf(mMaxNumber));
                        mSelfObject.selectAll();
                        mSelfObject.addTextChangedListener(mTextWatcher);
                        return;
                    }
                }
                else
                {
                    mSelfObject.removeTextChangedListener(mTextWatcher);
                    if(mSelfObject.getText().toString().isEmpty())
                        mSelfObject.setText(String.valueOf(mMinNumber));
                    mSelfObject.selectAll();
                    mSelfObject.addTextChangedListener(mTextWatcher);
                    return;
                }

                if (null != mOnDigtalEditTextChangeListener)
                {
                    mOnDigtalEditTextChangeListener.onDigtalEditTextChange(mSelfObject, editable.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                return;

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
            }
        };

        this.addTextChangedListener(mTextWatcher);
    }

    public void setOnDigtalEditTextChangeListener(OnDigtalEditTextChangeListener l)
    {
        mOnDigtalEditTextChangeListener = l;
    }
}
