package com.hisilicon.tvui.pvr;

import java.util.Calendar;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hisilicon.dtv.pvrfileplay.PVRFilePlayer;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.view.DigtalEditText;
import com.hisilicon.tvui.view.DigtalEditText.OnDigtalEditTextChangeListener;
import com.hisilicon.tvui.view.MyToast;
import com.hisilicon.tvui.util.LogTool;

public class PvrSeekDialog extends Dialog implements OnDigtalEditTextChangeListener {
    private Context mContext;
    private DigtalEditText mPvrSeekHour;
    private DigtalEditText mPvrSeekMinute;
    private DigtalEditText mPvrSeekSecond;

    private int mBeginHour;
    private int mBeginMinute;
    private int mBeginSecond;

    private final static int HOUR_SECONDS = 3600;
    private final static int MINUTE_SECONDS = 60;
    private final static int DAY_HOURS = 24;

    private PVRFilePlayer mPlayer;

    public PvrSeekDialog(Context context, int theme, PVRFilePlayer mPlayer) {
        super(context, theme);
        mContext = context;
        this.mPlayer = mPlayer;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timeshift_seek);
        initView();
        mPvrSeekHour.requestFocus();
    }

    private void refreshBeginTime() {
        int nowTime = mPlayer.getCurrentPosition();
        mBeginHour = (nowTime % (60 * 60 * 24)) / (60 * 60);
        mBeginMinute = (nowTime % (60 * 60)) / 60;
        mBeginSecond = nowTime % 60;
        return;
    }


    private boolean isSelectedSeekTimeRight() {
        int playTimeInt = mBeginHour * HOUR_SECONDS + mBeginMinute * MINUTE_SECONDS + mBeginSecond;
        int mTotalTime = mPlayer.getPVRFileInfo().getDuration();
        if (playTimeInt > mTotalTime || playTimeInt < 0) {
            return false;
        } else {
            return true;
        }
    }

    private void initEditText() {
        refreshBeginTime();
        mPvrSeekHour.setText(String.format("%02d", mBeginHour));
        mPvrSeekMinute.setText(String.format("%02d", mBeginMinute));
        mPvrSeekSecond.setText(String.format("%02d", mBeginSecond));
    }

    @Override
    public void onDigtalEditTextChange(DigtalEditText editText, String strText) {
        if (editText.getText().toString().length() == 2) {
            View v = editText.focusSearch(View.FOCUS_RIGHT);
            if (v != null) {
                v.requestFocus(View.FOCUS_RIGHT);
            }
        }
    }

    private void initView() {
        mPvrSeekHour = (DigtalEditText) this.findViewById(R.id.timeshift_seek_hour);
        mPvrSeekMinute = (DigtalEditText) this.findViewById(R.id.timeshift_seek_minute);
        mPvrSeekSecond = (DigtalEditText) this.findViewById(R.id.timeshift_seek_second);
        mPvrSeekHour.setOnDigtalEditTextChangeListener(this);
        mPvrSeekMinute.setOnDigtalEditTextChangeListener(this);
        mPvrSeekSecond.setOnDigtalEditTextChangeListener(this);
        Button mPvrSeekOK = (Button) this.findViewById(R.id.timeshift_seek_ok);
        initEditText();
        mPvrSeekOK.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String timeshiftSeekHour = mPvrSeekHour.getText().toString();
                String timeshiftSeekMinute = mPvrSeekMinute.getText().toString();
                String timeshiftSeekSecond = mPvrSeekSecond.getText().toString();
                if ((timeshiftSeekHour == null) || (0 == timeshiftSeekHour.length())) {
                    mPvrSeekHour.requestFocus();
                    return;
                }
                if ((timeshiftSeekMinute == null) || (0 == timeshiftSeekMinute.length())) {
                    mPvrSeekMinute.requestFocus();
                    return;
                }
                if ((timeshiftSeekSecond == null) || (0 == timeshiftSeekSecond.length())) {
                    mPvrSeekSecond.requestFocus();
                    return;
                }
                mBeginHour = Integer.parseInt(timeshiftSeekHour);
                mBeginMinute = Integer.parseInt(timeshiftSeekMinute);
                mBeginSecond = Integer.parseInt(timeshiftSeekSecond);
                if (!isSelectedSeekTimeRight()) {
                    MyToast.makeText(mContext, mContext.getResources().getString(R.string.seek_time_error), MyToast.LENGTH_LONG).show();
                    initEditText();
                    return;
                } else {
                    int playTimeInt = mBeginHour * HOUR_SECONDS + mBeginMinute * MINUTE_SECONDS + mBeginSecond;
                    if (mPlayer != null) {
                        mPlayer.seekTo(playTimeInt);
                    }
                    PvrSeekDialog.this.cancel();
                }

            }

        });

    }

}
