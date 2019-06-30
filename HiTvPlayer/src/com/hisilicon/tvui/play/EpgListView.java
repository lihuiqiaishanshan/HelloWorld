package com.hisilicon.tvui.play;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.dtv.epg.EPGEventFilter;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.epg.BookDialog;
import com.hisilicon.tvui.epg.DescriptionDialog;
import com.hisilicon.tvui.epg.EPGEventListAdapter;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.MyToast;

public class EpgListView extends BaseView implements OnItemClickListener
{
    private final MainActivity mMainActivity;
    private final ListView mListView;
    private static final int MAXIMUM_EVENT_NUMBER = 10000;
    private static final int MAX_EPG_DAY = 8;
    private BookDialog mBookDialog = null;
    private DescriptionDialog dialog = null;
    private static final long ONE_DAY_MILLS = 24 * 60 * 60 * 1000;
    private int mDayOffset = 0;
    private Channel mCurChn = null;
    private EPGEventListAdapter epgListAdapter = null;
    private final TextView mTextViewEpgDate;
    private final TextView mTextViewTipGreen;
    private final TextView mTextViewTipRed;
    private final TextView mTextViewTipYellow;
    private final TextView mTextViewTipBlue;

    public EpgListView(MainActivity arg0)
    {
        super((LinearLayout) arg0.findViewById(R.id.EpgListLayout));
        mMainActivity = arg0;
        mListView = (ListView) mMainActivity.findViewById(R.id.EpgListView);
        mTextViewEpgDate = (TextView) mMainActivity.findViewById(R.id.textViewEpgDate);

        mTextViewTipGreen = (TextView) mMainActivity.findViewById(R.id.TipGreen);
        mTextViewTipRed = (TextView) mMainActivity.findViewById(R.id.TipRed);
        mTextViewTipYellow = (TextView) mMainActivity.findViewById(R.id.TipYellow);
        mTextViewTipBlue = (TextView) mMainActivity.findViewById(R.id.TipBlue);

        mListView.setOnItemClickListener(this);
        epgListAdapter = new EPGEventListAdapter(mMainActivity, null);
        mListView.setAdapter(epgListAdapter);
    }

    public void show(Channel chn) {
        super.show();
        mDayOffset = 0;
        mCurChn = chn;
        epgListAdapter.setEventList(getEventList(mCurChn, mDayOffset));
        mListView.setSelection(0);
        mTextViewEpgDate.setText(formatOffsetDate());
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, dvbPlayListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, dvbPlayListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH, dvbPlayListener, 0);
        mDTV.subScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH, dvbPlayListener, 0);

        mTextViewTipGreen.setText(mMainActivity.getString(R.string.epg_event_ok_tip));
        mTextViewTipRed.setText(mMainActivity.getString(R.string.epg_yellow_tip));
        mTextViewTipYellow.setText(mMainActivity.getString(R.string.epg_blue_tip));
        mTextViewTipBlue.setText(mMainActivity.getString(R.string.epg_red_tip));
    }

    public void showCurrentChannel() {
        show(ChannelHistory.getInstance().getCurrentChn(mMainActivity.mCurSourceId));
    }

    @Override
    public void hide()
    {
        epgListAdapter.setEventList(null);
        if (null != mBookDialog)
        {
            mBookDialog.dismiss();
        }

        if (null != dialog)
        {
            dialog.dismiss();
        }

        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED, dvbPlayListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH, dvbPlayListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH, dvbPlayListener);
        mDTV.unSubScribeEvent(DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_FREQ_FINISH, dvbPlayListener);

        mTextViewTipGreen.setText(mMainActivity.getString(R.string.cas));
        mTextViewTipRed.setText(mMainActivity.getString(R.string.alpha));
        mTextViewTipYellow.setText(mMainActivity.getString(R.string.group));
        mTextViewTipBlue.setText(mMainActivity.getString(R.string.epg_red_tip));

        super.hide();
    }

    private boolean isShowDefaultTime()
    {
        Date tmpDate = mTimeManager.getTime();
        int tdtStatus = mTimeManager.getSettingTDTStatus();
        boolean mbShowDefaultTime;
        if (tmpDate == null)
        {
            tmpDate = new Date();
            mbShowDefaultTime = tdtStatus == 1;
        }
        else
        {
            mbShowDefaultTime = false;
        }
        return mbShowDefaultTime;
    }

    /**
     * Formats the special date got by offset value using the special rules of this
     * format:"2013-04-01 Monday".<br>
     *
     * @return the formatted string.<br>
     */
    private String formatOffsetDate()
    {
        boolean mbShowDefaultTime = isShowDefaultTime();

        StringBuffer buffer = new StringBuffer();

        Calendar tmpCalendar = getDateByOffset(mDayOffset);
        /*2. set start day*/
        Date date = tmpCalendar.getTime();

        // Formats date using the special rules of this format:"2013-04-01"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        format.setTimeZone(tmpCalendar.getTimeZone());
        if (null != format.format(date) && !mbShowDefaultTime)
        {
            buffer.append(format.format(date));

            buffer.append(" ");
        }

        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        int weekday = ca.get(Calendar.DAY_OF_WEEK) - 1;

        String[] weekdayArray = mMainActivity.getResources().getStringArray(R.array.weekday_array);
        String weekdayString = weekdayArray[weekday];

        if (null != weekdayString && !mbShowDefaultTime)
        {
            buffer.append(weekdayString);
        }

        if (mbShowDefaultTime)
        {
            buffer.append("----:--:-- ");
            buffer.append(" ");
        }

        return buffer.toString();
    }

    private Calendar getDateByOffset(int dayOffSet)
    {
        Calendar ca = mTimeManager.getCalendarTime();
        if (null == ca)
        {
            ca = Calendar.getInstance();
        }
        long dateOffsetMills = dayOffSet * ONE_DAY_MILLS;
        long dateMills = ca.getTimeInMillis() + dateOffsetMills;
        ca.setTimeInMillis(dateMills);
        return ca;
    }

    /**
     * Gets current event list ListView adapter. <br>
     */
    @SuppressWarnings("deprecation")
    public ArrayList<EPGEvent> getEventList(Channel chn, int dayOffSet)
    {
        EPGEventFilter filter = new EPGEventFilter();
        filter.setChannel(chn);

        Calendar tmpCalendar = getDateByOffset(dayOffSet);
        Calendar startCalendar = (Calendar)tmpCalendar.clone();
        /*2. set start day*/
        if (0 != dayOffSet)
        {
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
        }

        filter.setStartTimeCalendar(startCalendar);

        /*3. set end day*/
        tmpCalendar.add(Calendar.DAY_OF_MONTH, +1);

        Calendar endCalendar = (Calendar)tmpCalendar.clone();
        endCalendar.set(Calendar.HOUR_OF_DAY, 0);
        endCalendar.set(Calendar.MINUTE, 0);
        endCalendar.set(Calendar.SECOND, 0);
        filter.setEndTimeCalendar(endCalendar);

        ArrayList<EPGEvent> list = (ArrayList<EPGEvent>) mEpg.getEvents(filter, 0, MAXIMUM_EVENT_NUMBER);
        return list;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        EPGEvent tmpEPGEvent = (EPGEvent) (arg0.getAdapter()).getItem(arg2);

        dialog = new DescriptionDialog(mMainActivity, R.style.dialog_transparent, tmpEPGEvent);

        dialog.show();
    }

    public boolean onListViewKeyDown(int keyCode, android.view.KeyEvent event)
    {
        if (KeyValue.DTV_KEYVALUE_BACK == keyCode)
        {
            hide();
        }
        switch (keyCode)
        {
        case KeyValue.DTV_KEYVALUE_DPAD_LEFT:
        {
            hide();
            break;
        }
        case KeyValue.DTV_KEYVALUE_RED:
        {
            mDayOffset--;
            if (mDayOffset < 0)
            {
                mDayOffset = MAX_EPG_DAY - 1;
            }

            epgListAdapter.setEventList(getEventList(mCurChn, mDayOffset));
            mTextViewEpgDate.setText(formatOffsetDate());
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_UP:
        {
            if (mListView.getSelectedItemPosition() == 0)
            {
                mListView.setSelection(mListView.getCount() - 1);
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_DPAD_DOWN:
        {
            if (mListView.getSelectedItemPosition() == (mListView.getCount() - 1))
            {
                mListView.setSelectionFromTop(0, 0);
            }
            break;
        }
        case KeyValue.DTV_KEYVALUE_SEARCH:
        {
            break;
        }
        case KeyValue.DTV_KEYVALUE_GREEN:
        case KeyValue.DTV_KEYVALUE_DPAD_RIGHT:
        {
            EPGEvent tmpEPGEvent = (EPGEvent) mListView.getSelectedItem();
            dialog = new DescriptionDialog(mMainActivity, R.style.dialog_transparent, tmpEPGEvent);
            dialog.show();
            break;
        }
        case KeyValue.DTV_KEYVALUE_YELLOW:
        {
            mDayOffset++;
            if (mDayOffset > MAX_EPG_DAY - 1)
            {
                mDayOffset = 0;
            }

            epgListAdapter.setEventList(getEventList(mCurChn, mDayOffset));
            mTextViewEpgDate.setText(formatOffsetDate());
            return true;
        }
        case KeyValue.DTV_KEYVALUE_BLUE:
        {
            EPGEvent tmpCurrentEPGEvent = (EPGEvent) mListView.getSelectedItem();
            if (!mCurChn.isScramble())
            {
                showBookDialog(tmpCurrentEPGEvent, mCurChn);
            }
            else
            {
                MyToast.makeText(mMainActivity, R.string.epg_book_scramble_cannot_book, MyToast.LENGTH_SHORT).show();
            }

            return true;
        }
        default:
            break;
        }
        return false;
    }

    public void showBookDialog(EPGEvent curEPGEvent, Channel chn)
    {
        mCurChn = chn;
        if (null == mCurChn)
        {
            return;
        }

        if (isShowDefaultTime())
        {
            MyToast.makeText(mMainActivity, R.string.epg_book_time_error_tip, MyToast.LENGTH_SHORT).show();
            return;
        }

        BookTask bookTask = mBookManager.createTask();
        bookTask.setChannelId(mCurChn.getChannelID());
        if (null != curEPGEvent)
        {
            bookTask.setEventId(curEPGEvent.getEventId());
            bookTask.setStartDate(curEPGEvent.getStartTime());
            bookTask.setStartDateCalendar(curEPGEvent.getStartTimeCalendar());
            bookTask.setDuration(curEPGEvent.getDuration());
            String eventName = curEPGEvent.getEventName();
            String name = eventName.trim();
            bookTask.setName(name);
        }
        else
        {
            EPGEvent mPresentEPGEvent = mEpg.getPresentEvent(mCurChn);
            if (mPresentEPGEvent != null && mPresentEPGEvent.getStartTimeCalendar() != null) {
                bookTask.setStartDateCalendar(mPresentEPGEvent.getStartTimeCalendar());
            } else {
                bookTask.setStartDateCalendar(Calendar.getInstance());
            }
        }

        mBookDialog = new BookDialog(mMainActivity, null, bookTask,
                (int) mMainActivity.getResources().getDimension(R.dimen.dimen_500px), 0, (float) 0.9);
        mBookDialog.show();
        WindowManager windowManager =mMainActivity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        WindowManager.LayoutParams lp = mBookDialog.getWindow().getAttributes();
        lp.height = (int)(display.getHeight());
        mBookDialog.getWindow().setAttributes(lp);
    }

    final IDTVListener dvbPlayListener = new IDTVListener()
    {
        @Override
        public void notifyMessage(int messageID, int param1, int param2, Object obj)
        {
            LogTool.d(LogTool.MPLAY, "messageID:" + messageID + " param1: " + param1 + " param2:" + param2);

            switch (messageID)
            {
            case DTVMessage.HI_SVR_EVT_EPG_PF_VERSION_CHANGED:
            case DTVMessage.HI_SVR_EVT_EPG_PF_CURR_PROGRAM_FINISH:
            case DTVMessage.HI_SVR_EVT_EPG_SCH_VERSION_CHANGED:
            case DTVMessage.HI_SVR_EVT_EPG_SCH_CURR_PROGRAM_FINISH:
            {
                refreshList();
                break;
            }
            default:
                break;
            }
        }
    };

    private void refreshList()
    {
        if (null != epgListAdapter) {
            mListView.setAdapter(epgListAdapter);
            mTextViewEpgDate.setText(formatOffsetDate());
            epgListAdapter.setEventList(getEventList(mCurChn, mDayOffset));
        }
    }
}
