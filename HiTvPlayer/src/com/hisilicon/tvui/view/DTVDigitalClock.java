package com.hisilicon.tvui.view;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Calendar;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.network.si.TimeManager;
import com.hisilicon.tvui.base.CommonValue;

/**
 * the DigitalClock is different from the system DigitalClock,the System DigitalClock is display the time type
 * based on the system configure,the DigitalClock display different type through the configure of the user.
 * the user can set different displayed type by setHourType method.
 * the DigitalClock support two display type :12/24 mode.
 *
 */
public class DTVDigitalClock extends TextView
{
    public enum EnHourType
    {
        Hour12, Hour24,
    }

    private EnHourType mHourType = null;
    private final static String m12 = "K:mm:ss aa";//h:mm:ss aa
    private final static String m24 = "yyyy-MM-dd H:mm:ss";//k:mm:ss
    private final static String defaultTime = "--:--";
    private TimeManager mTimeManager = null;
    private Runnable mTicker;
    private Handler mHandler;
    private Handler mInfoHandler;
    private boolean mTickerStopped = false;

    private String mFormat = m24;
    private String mTextViewTime = "--:--";
    private Runnable updatetime = new Runnable() {
        public void run() {
            if (mTickerStopped) {
                return;
            }

            Calendar ca = mTimeManager.getCalendarTime();
            if (null != ca) {
                SimpleDateFormat dateFormat = new SimpleDateFormat(mFormat);
                dateFormat.setTimeZone(ca.getTimeZone());
                mTextViewTime = dateFormat.format(ca.getTime());
            } else {
                mTextViewTime = defaultTime;
            }
            mInfoHandler.postDelayed(updatetime, 1000);
        }
    };

    public DTVDigitalClock(Context context)
    {
        super(context);
        initClock(context);
    }

    public DTVDigitalClock(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initClock(context);
    }

    private void initClock(Context context)
    {
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mTimeManager = mDTV.getNetworkManager().getTimeManager();
        mInfoHandler = new Handler();
        mInfoHandler.post(updatetime);
    }

    public void setHourType(EnHourType enHourType)
    {
        mHourType = enHourType;
    }

    public EnHourType getHourType()
    {
        return mHourType;
    }

    @Override
    protected void onAttachedToWindow()
    {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        /**
         * requests a tick on the next hard-second boundary
         */
        mTicker = new Runnable()
        {
            public void run()
            {
                if (mTickerStopped)
                    return;
                setText(mTextViewTime);
                invalidate();
                long now = SystemClock.uptimeMillis();
                long next = now + (1000 - now % 1000);
                mHandler.postAtTime(mTicker, next);
            }
        };
        mHandler.post(mTicker);
    }

    @Override
    protected void onDetachedFromWindow()
    {
        mTickerStopped = true;
        super.onDetachedFromWindow();
    }
}
