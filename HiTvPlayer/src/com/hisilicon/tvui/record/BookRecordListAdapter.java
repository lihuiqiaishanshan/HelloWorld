package com.hisilicon.tvui.record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;

/**
 * Defined adapter for channel list ListView in EPG module.<br>
 *
 * @author z00209628
 * @see ChannelList
 */
public class BookRecordListAdapter extends BaseAdapter
{
    private Context mContext = null;

    private LayoutInflater mLayoutInflater = null;

    private ArrayList<BookTask> mBookTaskList = null;

    private DTV mDTV = null;

    private ChannelManager mChannelManager = null;

    private class ViewHolder
    {
        public TextView mDateTextview = null;
        public TextView mDurationTextview = null;
        public TextView mChannelTextview = null;
        public TextView mNameTextview = null;
        public TextView mCycleTextview = null;
        public CheckBox mCheckBox = null;
    }

    private ViewHolder mHolder = null;

    public BookRecordListAdapter(Context context, ArrayList<BookTask> bookTaskList)
    {
        this.mContext = context;

        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        this.mBookTaskList = bookTaskList;

        initDTV();
    }

    private void initDTV()
    {
        mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

        mChannelManager = mDTV.getChannelManager();
    }

    @Override
    public int getCount()
    {
        if (null != mBookTaskList)
        {
            return mBookTaskList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if (null != mBookTaskList)
        {
            return mBookTaskList.get(position);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, ViewGroup viewGroup)
    {
        if (null == view)
        {
            mHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.recording_list_record_list, null);
            mHolder.mDateTextview = (TextView) view.findViewById(R.id.tv_record_date);
            mHolder.mDurationTextview = (TextView) view.findViewById(R.id.tv_record_duration);
            mHolder.mChannelTextview = (TextView) view.findViewById(R.id.tv_record_channel);
            mHolder.mNameTextview = (TextView) view.findViewById(R.id.tv_record_name);
            mHolder.mCycleTextview = (TextView) view.findViewById(R.id.tv_record_cycle);
            mHolder.mCheckBox = (CheckBox) view.findViewById(R.id.cb_record_enable);
        }
        else
        {
            mHolder = (ViewHolder) view.getTag();
        }

        if (null != mBookTaskList)
        {
            BookTask mBookTask = mBookTaskList.get(position);
            if (null != mBookTask && mBookTask.getStartDateCalendar() != null) {
                int year = mBookTask.getStartDateCalendar().get(Calendar.YEAR);
                int month = mBookTask.getStartDateCalendar().get(Calendar.MONTH) + 1;
                int day = mBookTask.getStartDateCalendar().get(Calendar.DAY_OF_MONTH);
                int hour = mBookTask.getStartDateCalendar().get(Calendar.HOUR_OF_DAY);
                int min = mBookTask.getStartDateCalendar().get(Calendar.MINUTE);
                int second = mBookTask.getStartDateCalendar().get(Calendar.SECOND);

                String startDate = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " + String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", second);
                LogTool.d(LogTool.MEPG, "book list startDate= " + mBookTask.isEnable());
                mHolder.mDateTextview.setText(startDate);
                int duration = mBookTask.getDuration();
                long durationMillisecond = ((long) duration) * 1000;
                Date durationDate = new Date(durationMillisecond);
                SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                durationFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                mHolder.mDurationTextview.setText(durationFormat.format(durationDate));

                int channelId = mBookTask.getChannelId();
                Channel channel = mChannelManager.getChannelByID(channelId);
                if (null != channel)
                {
                    String channelName = channel.getChannelName();
                    if (null == channelName)
                    {
                        channelName = "";
                    }
                    mHolder.mChannelTextview.setText(channelName);
                }

                String name = mBookTask.getName();
                if (null == name)
                {
                    name = "";
                }
                mHolder.mNameTextview.setText(name);

                EnTaskCycle enTaskCycle = mBookTask.getCycle();
                String[] cycleArray = mContext.getResources().getStringArray(R.array.cycle_array);
                String cycle = null;
                if (null != enTaskCycle)
                {
                    cycle = cycleArray[enTaskCycle.ordinal()];
                }
                else
                {
                    cycle = "";
                }
                mHolder.mCycleTextview.setText(cycle);

                LogTool.d(LogTool.MEPG, "mBookTask.isEnable() = " + mBookTask.isEnable());
                if (mBookTask.isEnable())
                {
                    mHolder.mCheckBox.setSelected(true);
                }
                else
                {
                    mHolder.mCheckBox.setSelected(false);
                }
            }
        }

        view.setTag(mHolder);

        return view;
    }

    public void setCheckBox(boolean isChecked)
    {
        if (null != mHolder.mCheckBox)
        {
            mHolder.mCheckBox.setSelected(isChecked);
        }
    }
}
