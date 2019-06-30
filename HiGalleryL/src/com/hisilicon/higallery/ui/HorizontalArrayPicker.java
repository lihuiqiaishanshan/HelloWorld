
package com.hisilicon.higallery.ui;

import android.content.Context;
import android.content.res.Resources;
//import android.renderscript.Program.TextureType;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

import com.hisilicon.higallery.R;

public class HorizontalArrayPicker extends Button {
    private String[] mDisplayNames;
    private int[] mValues;
    private int mIndex;
    String mFormatString;
    private OnDetachedFromWindowListener mOnDetachedFromWindowListener;

    public HorizontalArrayPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        mFormatString = res.getString(R.string.picker_string);
        setGravity(Gravity.FILL_VERTICAL | Gravity.CENTER);
        setSingleLine(true);
        //setEllipsize(TextUtils.TruncateAt.END_SMALL);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            event.startTracking();
            return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            prev();
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            next();
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            int halfWidth = getWidth() / 2;
            int x = (int) event.getX();
            if (x > halfWidth) {
                next();
            } else {
                prev();
            }
        }
        return super.onTouchEvent(event);
    }

    public void setArray(String[] names, int[] values) {
        if (names == null || values == null
                || names.length != values.length
                || names.length <= 0) {
            throw new IllegalArgumentException("Invalide arrays");
        }

        mDisplayNames = names;
        mValues = values;
        updateText();
    }

    private void updateText() {
        String left = "";
        String middle = "";
        String right = "";

        if (mValues.length == 1) {
            middle = mDisplayNames[0];
        } else if (mIndex <= 0) {
            mIndex = 0;
            middle = mDisplayNames[mIndex];
            right = mDisplayNames[mIndex + 1];
        } else if (mIndex >= mValues.length - 1) {
            mIndex = mValues.length - 1;
            middle = mDisplayNames[mIndex];
            left = mDisplayNames[mIndex - 1];
        } else {
            left = mDisplayNames[mIndex - 1];
            middle = mDisplayNames[mIndex];
            right = mDisplayNames[mIndex + 1];
        }
        String text = String.format(mFormatString, left, middle, right);
        CharSequence s = Html.fromHtml(text);
        setText(s);
    }

    private void next() {
        if (mIndex >= mValues.length - 1)
            return;

        mIndex++;
        updateText();
    }

    private void prev() {
        if (mIndex <= 0)
            return;

        mIndex--;
        updateText();
    }

    public int getValue() {
        return mValues[mIndex];
    }

    public void setValue(int v) {
        for (int i = 0; i < mValues.length; i++) {
            if (mValues[i] == v) {
                mIndex = i;
                break;
            }
        }
        updateText();
    }

    public void onDetachedFromWindow(){
        if(mOnDetachedFromWindowListener != null){
            mOnDetachedFromWindowListener.onDeteached();
        }
    }

    public void setonDetachedFromWindowListener(OnDetachedFromWindowListener mListener){
        mOnDetachedFromWindowListener = mListener;
    }

    public interface OnDetachedFromWindowListener{
        public void onDeteached();
    }

}
