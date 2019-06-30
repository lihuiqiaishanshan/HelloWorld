package com.hisilicon.tvui.channelmanager;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LogTool;


public class ChannelEditListAdapter extends BaseAdapter
{
    private ChannelList mChannelList = null;
    private LayoutInflater layoutInflater = null;
    private ViewHolder holder;

    private class ViewHolder
    {
        public TextView channelNumberTextview = null;
        public TextView channelNameTextview = null;
        public TextView channelAudioPIDTextview = null;
        public TextView channelVideoPIDTextview = null;
        public TextView channelPCRPIDTextview = null;
    }

    public ChannelEditListAdapter(Context context, ChannelList tempList)
    {
        mChannelList = tempList;
        if (null != context)
        {
            this.layoutInflater = LayoutInflater.from(context);
        }
        holder = null;
    }

    @Override
    public int getCount()
    {
        if (null != mChannelList)
        {
            return mChannelList.getChannelCount();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0)
    {
        if (null != mChannelList)
        {
            return mChannelList.getChannelByIndex(arg0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int arg0)
    {
        if (null != mChannelList)
        {
            return mChannelList.getChannelByIndex(arg0).getChannelID();
        }
        else
        {
            return 0;
        }
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2)
    {
        if (arg1 == null)
        {
            holder = new ViewHolder();
            arg1 = layoutInflater.inflate(R.layout.channel_edit_channel_list_item, null);
            holder.channelNameTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_tp_list_item_name);
            holder.channelNumberTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_tp_list_item_id);
            holder.channelAudioPIDTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_tp_list_item_audio_pid);
            holder.channelVideoPIDTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_tp_list_item_video_pid);
            holder.channelPCRPIDTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_tp_list_item_pcr_pid);
        }
        else
        {
            holder = (ViewHolder) arg1.getTag();
        }

        Channel tempChannel = mChannelList.getChannelByIndex(arg0);
        DecimalFormat fourDig = new DecimalFormat("0000");
        if (null != tempChannel)
        {
            LogTool.v(LogTool.MCHANNEL, "position = " + arg0);
            String number = fourDig.format(tempChannel.getChannelNo());
            holder.channelNumberTextview.setText(number);
            String name = tempChannel.getChannelName();
            holder.channelNameTextview.setText(name);
            LogTool.v(LogTool.MCHANNEL, "name = " + name);
            int audioPid = tempChannel.getAudioPID();
            holder.channelAudioPIDTextview.setText(String.valueOf(audioPid));
            int videoPid = tempChannel.getVideoPID();
            holder.channelVideoPIDTextview.setText(String.valueOf(videoPid));
            int pcrPid = tempChannel.getPCRPID();
            holder.channelPCRPIDTextview.setText(String.valueOf(pcrPid));
        }

        arg1.setTag(holder);

        return arg1;
    }

}
