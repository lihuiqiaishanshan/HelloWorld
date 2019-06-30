
package com.hisilicon.launcher.view.setting;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.Refreshable;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.LogHelper;

/**
 * The view is suitable for launcher settings, used to set the brightness value,
 * volume value etc..。
 *
 * @author wang_chuanjian HiSi.ltd <br>
 */
public class ProgressView extends LinearLayout implements Refreshable {
    private static final String TAG = "ProgressView";
    private Context mContext;
    private SeekBar mProgressBar;
    // text of option
    private TextView mOptionText;
    // text of progress
    private TextView mProgressText;
    // flag of is Focus
    private boolean isOnFocus;
    // type of widget
    private WidgetType mWidgetType;
    // private int progress;
    private CustomSettingView mCustomSettingView;
    // list of WidgetType
    private List<WidgetType> mWidgettypeList = null;

    @SuppressLint("ResourceAsColor")
    public ProgressView(CustomSettingView customSettingView, Context context,
            WidgetType widgetType, List<WidgetType> li) {
        super(context);
        mContext = context;
        this.mCustomSettingView = customSettingView;
        mWidgettypeList = li;
        mWidgetType = widgetType;
        if (mWidgetType == null
                || mWidgetType.getmAccessProgressInterface() == null) {
            throw new NullPointerException(
                    "mWidgetType is null || mWidgetType.getmAccessProgressInterface() is null");
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());

        inflater.inflate(R.layout.progress_view, this);
        mOptionText = (TextView) findViewById(R.id.option_txt);
        mOptionText.setText(widgetType.getName());
        LogHelper.d(TAG, widgetType.getName() + ", length="
                + widgetType.getName().length());
        mProgressText = (TextView) findViewById(R.id.num_txt);
        int progress = mWidgetType.getmAccessProgressInterface().getProgress();
        mProgressText.setText("  " + (progress + mWidgetType.getOffset()));

        mProgressBar = (SeekBar) findViewById(R.id.progress_seekbar);
        mProgressBar.setMax(mWidgetType.getMaxProgress());
        mProgressBar.setProgress(progress);
        mProgressBar.setProgressDrawable(getResources().getDrawable(
                R.drawable.seek_bar_progress_focus));
        mProgressBar.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // The removal of circulating cycle, unlock
                    // if (mCustomSettingView.onKey(v, keyCode, event)) {
                    // return true;
                    // }
                    int progress = mProgressBar.getProgress();
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            if (isOnFocus) {
                                progress--;
                                if (progress >= 0) {
                                    mProgressBar.setProgress(progress);
                                    mWidgetType.getmAccessProgressInterface()
                                            .setProgress(progress);
                                    mProgressText.setText("  "
                                            + (progress + mWidgetType.getOffset()));
                                    return true;
                                }
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (isOnFocus) {
                                progress++;
                                if (progress > mWidgetType.getMaxProgress()) {
                                    progress = mWidgetType.getMaxProgress();
                                }
                                if (progress <= mWidgetType.getMaxProgress() + 1) {
                                    mProgressBar.setProgress(progress);
                                    mWidgetType.getmAccessProgressInterface()
                                            .setProgress(progress);
                                    mProgressText.setText("  "
                                            + (progress + mWidgetType.getOffset()));
                                    return true;
                                }
                            } else {
                                mProgressBar.setFocusable(true);
                            }
                            break;
                        case KeyEvent.KEYCODE_BACK:
                            LogHelper.d(TAG, "on key back !");
                            break;

                        default:
                            break;
                    }
                }
                LogHelper.d(TAG, mProgressBar.getProgress() + "");
                return false;
            }
        });

        mProgressBar.setOnFocusChangeListener(new OnFocusChangeListener() {

            @SuppressLint("NewApi")
            @Override
            public void onFocusChange(View arg0, boolean hasFocus) {
                isOnFocus = hasFocus;
                if (hasFocus) {
                    if (mWidgettypeList.size() > 1) {
                        findViewById(R.id.progess_layout)
                                .setBackgroundResource(
                                        R.drawable.launcher_set_focus);
                    } else {
                        findViewById(R.id.seek).setBackgroundResource(
                                R.drawable.button_transparent);
                    }

                    mProgressBar.setProgressDrawable(getResources()
                            .getDrawable(R.drawable.seek_bar_progress));
                    mOptionText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[3]));
                    mProgressText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[3]));
                } else {
                    findViewById(R.id.progess_layout).setBackgroundResource(
                            R.drawable.button_transparent);
                    mProgressBar.setProgressDrawable(getResources()
                            .getDrawable(R.drawable.seek_bar_progress_focus));
                    mOptionText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[0]));
                    mProgressText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[0]));
                }
                LogHelper.d(TAG, "onFocusChange flag = " + isOnFocus);
            }
        });
        mProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar arg0) {
                mProgressBar.setFocusable(true);
                mProgressBar.setFocusableInTouchMode(true);
                mProgressBar.requestFocus();
            }

            @Override
            public void onProgressChanged(SeekBar arg0, int progress, boolean fromUser) {
                mProgressText.setText("  "
                        + (mProgressBar.getProgress() + mWidgetType.getOffset()));
                if (fromUser) {
                    mWidgetType.getmAccessProgressInterface()
                        .setProgress(progress);
                }
            }
        });
    }

    @Override
    public void refreshUI() {
        int progress = mWidgetType.getmAccessProgressInterface().getProgress();
        LogHelper.d(TAG, "refreshUI this = " + this + " progress = "
                + progress);
        mProgressBar.setProgress(progress);
        mProgressText.setText("  " + progress);
    }

    @Override
    public WidgetType getWidgetType() {
        return mWidgetType;
    }

    @Override
    public boolean getIsFocus() {
        return isOnFocus;
    }
}
