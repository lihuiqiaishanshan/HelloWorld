package com.hisilicon.tvui.epg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.book.BookManager;
import com.hisilicon.dtv.book.BookTask;
import com.hisilicon.dtv.book.EnTaskCycle;
import com.hisilicon.dtv.book.EnTaskType;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelManager;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.record.RecordingListActivity;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.MyToast;

public class BookConflictTipDialog extends Dialog implements View.OnClickListener
{
    private TextView mResultTipTextView = null;

    private TextView mResultInfoTipTextView = null;

    private Context mContext = null;

    private BookManager mBookManager = null;

    private RecordingListActivity mRecordingListActivity = null;

    private ArrayList<BookTask> mConflictBookTasksList = null;

    private BookTask mBookTask = null;

    public BookConflictTipDialog(Context context, RecordingListActivity recordingListActivity, BookManager bookManager,
            ArrayList<BookTask> conflictBookTasksList, BookTask bookTask)
    {
        super(context, R.style.DIM_STYLE);

        this.mContext = context;

        this.mBookManager = bookManager;

        this.mRecordingListActivity = recordingListActivity;

        this.mConflictBookTasksList = conflictBookTasksList;

        this.mBookTask = bookTask;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.epg_book_result_dialog);

        super.onCreate(savedInstanceState);

        mResultTipTextView = (TextView) findViewById(R.id.tv_epg_book_result_tip);
        initialResultTipTextView();

        mResultInfoTipTextView = (TextView) findViewById(R.id.tv_epg_book_result_info_tip);
        initialResultInfoTipTextView();

        Button mExitButton = (Button) findViewById(R.id.btn_epg_book_exit);
        mExitButton.setOnClickListener(this);

        Button mGoOnButton = (Button) findViewById(R.id.btn_epg_book_goon);
        mGoOnButton.setOnClickListener(this);
    }

    private void initialResultTipTextView()
    {
        mResultTipTextView.setText(getContext().getResources().getString(R.string.epg_book_conflict_tip));
    }

    private void initialResultInfoTipTextView()
    {
        StringBuffer infoBuffer = new StringBuffer();

        int size = mConflictBookTasksList.size();
        LogTool.d(LogTool.MEPG, "mConflictBookTasksList.size() = " + size);
        for (int i = 0; i < size; i++)
        {
            infoBuffer.append(String.valueOf(i + 1)).append(".");

            BookTask bookTask = mConflictBookTasksList.get(i);

            getBookTaskInfo(bookTask, infoBuffer);
        }

        mResultInfoTipTextView.setText(infoBuffer.toString());

        mResultInfoTipTextView.requestFocus();
    }

    private void getBookTaskInfo(BookTask bookTask, StringBuffer infoBuffer)
    {
        String lineSeperator = System.getProperty("line.separator");

        if (bookTask != null) {
            int channelId = bookTask.getChannelId();
            DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
            ChannelManager channelManager = dtv.getChannelManager();
            Channel channel = channelManager.getChannelByID(channelId);
            if (null != channel)
            {
                String channelName = channel.getChannelName();
                if ((null != channelName) && (channelName.length() > 0))
                {
                    infoBuffer.append(getContext().getResources().getString(R.string.epg_book_channel));
                    infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                    infoBuffer.append(" ").append(channelName).append(lineSeperator);
                }
            }

            String name = bookTask.getName();
            if ((null != name) && (name.length() > 0))
            {
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_name));
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                infoBuffer.append(" ").append(name).append(lineSeperator);
            }
            if (bookTask.getStartDateCalendar() != null) {
                int year = bookTask.getStartDateCalendar().get(Calendar.YEAR);
                int month = bookTask.getStartDateCalendar().get(Calendar.MONTH) + 1;
                int day = bookTask.getStartDateCalendar().get(Calendar.DAY_OF_MONTH);
                int hour = bookTask.getStartDateCalendar().get(Calendar.HOUR_OF_DAY);
                int min = bookTask.getStartDateCalendar().get(Calendar.MINUTE);
                int second = bookTask.getStartDateCalendar().get(Calendar.SECOND);
                String startDate = year + "-" + String.format("%02d", month) + "-" + String.format("%02d", day) + " " + String.format("%02d", hour) + ":" + String.format("%02d", min) + ":" + String.format("%02d", second);
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_start_date));
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                infoBuffer.append(" ").append(startDate).append("  ");
            } else {
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_start_date));
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                infoBuffer.append(" ");
            }
            int duration = bookTask.getDuration();
            Date durationDate = new Date(((long) duration) * 1000);
            SimpleDateFormat durationFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            infoBuffer.append(getContext().getResources().getString(R.string.epg_book_duration));
            infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
            infoBuffer.append(" ").append(durationFormat.format(durationDate)).append(lineSeperator);

            EnTaskType type = bookTask.getType();
            if (null != type) {
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_type));
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                infoBuffer.append(" ").append(type.toString()).append("   ");
            }

            EnTaskCycle cycle = bookTask.getCycle();
            if (null != cycle) {
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_cycle));
                infoBuffer.append(getContext().getResources().getString(R.string.epg_book_seperate));
                infoBuffer.append(" ").append(cycle.toString()).append(lineSeperator);
            }
        }
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
        case R.id.btn_epg_book_goon:
        {
            dismiss();

            int size = mConflictBookTasksList.size();
            boolean isDeleted = true;
            for (int i = 0; i < size; i++)
            {
                BookTask bookTask = mConflictBookTasksList.get(i);

                int ret = mBookManager.deleteTask(bookTask);
                if (0 != ret)
                {
                    isDeleted = false;
                    break;
                }
            }

            if (isDeleted)
            {
                //when edit task,need delete the task self
                BookTask curTask = mBookManager.getTaskByID(mBookTask.getId());
                if (curTask != null)
                {
                    mBookManager.deleteTask(curTask);
                }

                int ret = mBookManager.addTask(mBookTask);
                if (ret >= 0)
                {
                    MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_success_tip), MyToast.LENGTH_LONG).show();

                }
                else
                {
                    MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_failure_tip), MyToast.LENGTH_LONG).show();
                }
                LogTool.d(LogTool.MEPG, "mBookManager.addTask:ret = " + ret);
            }
            else
            {
                MyToast.makeText(mContext, getContext().getResources().getString(R.string.epg_book_failure_tip), MyToast.LENGTH_LONG).show();
            }

            if (null != mRecordingListActivity)
            {
                mRecordingListActivity.updateBookListView();
            }

            break;
        }
        case R.id.btn_epg_book_exit:
        {
            dismiss();

            break;
        }
        default:
        {
            break;
        }
        }
    }
}
