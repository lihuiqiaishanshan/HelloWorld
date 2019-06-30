package com.hisilicon.tvui.record;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.tvui.R;

/**
 * Defined adapter for channel list ListView in EPG module.<br>
 *
 * @author z00209628
 * @see ChannelList
 */
public class RecordedListAdapter extends BaseAdapter
{
    private LayoutInflater mLayoutInflater = null;

    private ArrayList<RecordedFile> mRecordedFileList = null;

    private class ViewHolder
    {
        //public TextView mDateTextview = null;
        public TextView mNameTextview = null;
        public ImageView mImageView = null;
    }

    public RecordedListAdapter(Context context, ArrayList<RecordedFile> recordedFileList)
    {
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
        }

        this.mRecordedFileList = recordedFileList;
    }

    @Override
    public int getCount()
    {
        if (null != mRecordedFileList)
        {
            return mRecordedFileList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if (null != mRecordedFileList)
        {
            return mRecordedFileList.get(position);
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
        ViewHolder mHolder = null;
        if (null == view)
        {
            mHolder = new ViewHolder();
            view = mLayoutInflater.inflate(R.layout.recording_list_recorded_list, null);
            mHolder.mNameTextview = (TextView) view.findViewById(R.id.tv_recorded_name);
            //mHolder.mDateTextview = (TextView) view.findViewById(R.id.tv_recorded_date);
            mHolder.mImageView = (ImageView) view.findViewById(R.id.iv_recorded_radio);
        }
        else
        {
            mHolder = (ViewHolder) view.getTag();
        }

        if (null != mRecordedFileList)
        {
            RecordedFile recordedFile = mRecordedFileList.get(position);
            mHolder.mNameTextview.setText(recordedFile.getName());
            //mHolder.mDateTextview.setText(recordedFile.getDate());

            if (recordedFile.isRadio())
            {
                mHolder.mImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                //mHolder.mImageView.setVisibility(View.INVISIBLE);
                mHolder.mImageView.setVisibility(View.VISIBLE);
            }
        }

        view.setTag(mHolder);

        return view;
    }
}
