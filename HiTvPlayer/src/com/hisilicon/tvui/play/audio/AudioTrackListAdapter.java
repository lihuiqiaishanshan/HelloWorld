package com.hisilicon.tvui.play.audio;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.tvui.R;

public class AudioTrackListAdapter extends BaseAdapter
{
    private LayoutInflater mLayoutInflater = null;
    private int mPosition = 0;

    private ArrayList<String> mSelectList = new ArrayList<String>();

    private class ViewHolder
    {
        private TextView mtv_audiotrack_name = null;
        private ImageView miv_audiotrack_check = null;
    }

    public List<String> getSelectList()
    {
        return mSelectList;
    }

    public AudioTrackListAdapter(Context context)
    {
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
        }
        mSelectList.add("STEREO");
        mSelectList.add("DOUBLE_MONO");
        mSelectList.add("DOUBLE_LEFT");
        mSelectList.add("DOUBLE_RIGHT");
        mSelectList.add("EXCHANGE");
        mSelectList.add("ONLY_RIGHT");
        mSelectList.add("ONLY_LEFT");
        mSelectList.add("MUTED");
    }

    // 设置当前选中位置
    public void setCurrentPosition(int pose)
    {
        mPosition = pose;
    }

    // 获取当前选中位置
    public int getCurrentPosition()
    {
        return mPosition;
    }

    @Override
    public int getCount()
    {
        // TODO Auto-generated method stub
        return mSelectList.size();
    }

    @Override
    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return mSelectList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        ViewHolder holder = null;
        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.play_audio_track_list_item, null);
            holder.mtv_audiotrack_name = (TextView) convertView.findViewById(R.id.tv_audiotrack_name);
            holder.miv_audiotrack_check = (ImageView) convertView.findViewById(R.id.iv_audiotrack_check);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mtv_audiotrack_name.setText(mSelectList.get(position));

        if (mPosition == position)
        {
            holder.miv_audiotrack_check.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.miv_audiotrack_check.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}
