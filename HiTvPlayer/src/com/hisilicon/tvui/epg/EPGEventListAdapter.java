package com.hisilicon.tvui.epg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.epg.EPGEvent;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;

/**
 * Defined adapter for SCH program event list ListView in EPG module.<br>
 *
 * @author z00209628
 * @see EPGEvent
 */
public class EPGEventListAdapter extends BaseAdapter
{
    private LayoutInflater mLayoutInflater = null;
    private ArrayList<EPGEvent> mEventList;
    private DTV mDTV = null;

    private class ViewHolder
    {
        public TextView mNameTextview = null;
        public TextView mTimeTextview = null;
    }

    public EPGEventListAdapter(Context context, ArrayList<EPGEvent> eventList)
    {
        LogTool.d(LogTool.MEPG, "EPGEventListAdapter(...)");
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        this.mEventList = eventList;
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
    }

    public void setEventList(ArrayList<EPGEvent> eventList)
    {
        mEventList = eventList;
        if (null == eventList || 0 == eventList.size())
        {
            mEventList = null;
        }

        notifyDataSetChanged();
    }

    public ArrayList<EPGEvent> getEventList()
    {
        return mEventList;
    }

    @Override
    public int getCount()
    {
        if (null != mEventList)
        {
            LogTool.d(LogTool.MEPG, "mEventList.size() = " + mEventList.size());
            return mEventList.size();
        }
        else
        {
            return 1;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if (null != mEventList)
        {
            return mEventList.get(position);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int position)
    {
        int id = 0;

        if (null != mEventList)
        {
            EPGEvent event = mEventList.get(position);
            if (null != event)
            {
                id = event.getEventId();
            }
        }

        LogTool.d(LogTool.MEPG, "id = " + id);
        return id;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        LogTool.v(LogTool.MEPG, "position = " + position);
        ViewHolder mHolder = null;
        if (view == null)
        {
            mHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.epg_event_list, null);
            mHolder.mTimeTextview = (TextView) view.findViewById(R.id.tv_epg_event_time);
            mHolder.mNameTextview = (TextView) view.findViewById(R.id.tv_epg_event_name);
        }
        else
        {
            mHolder = (ViewHolder) view.getTag();
        }

        if (null != mEventList)
        {
            EPGEvent event = mEventList.get(position);

            // Show empty string as default action
            String startDateString = "";
            String endDateString = "";
            String name = "";

            if (null != event)
            {
                // Obtain program event name,time and detail description informations while object
                // not
                // null
                SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.getDefault());
                Calendar startCal = event.getStartTimeCalendar();
                if (null != startCal) {
                    format.setTimeZone(startCal.getTimeZone());
                    startDateString = format.format(startCal.getTime());
                }
                if (startDateString.length() > 0)
                {
                    // Add separate string between start time and end time while start time string
                    // is not empty
                    startDateString += "-";
                }

                Calendar endCal = event.getEndTimeCalendar();
                if (null != endCal) {
                    format.setTimeZone(endCal.getTimeZone());
                    endDateString = format.format(endCal.getTime());
                }

                name = event.getEventName();
                if (null == name)
                {
                    name = "";
                }
            }

            mHolder.mTimeTextview.setText(String.format("%s%s", startDateString, endDateString));
            mHolder.mNameTextview.setText(name);
        }
        else
        {
            mHolder.mTimeTextview.setText("");
            mHolder.mNameTextview.setText(R.string.epg_event_none);
        }

        view.setTag(mHolder);

        return view;
    }

}
