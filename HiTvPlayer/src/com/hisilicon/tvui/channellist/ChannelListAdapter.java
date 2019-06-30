package com.hisilicon.tvui.channellist;

import java.text.DecimalFormat;
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

import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnFavTag;
import com.hisilicon.dtv.channel.EnTVRadioFilter;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.LogTool;

public class ChannelListAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater = null;
    private ChannelList mDtvChannelList = null;
    private ChannelList mAtvChannelList = null;
    private List<Channel> mChannelList = new ArrayList<Channel>();
    private int mDtvNumber = 0;
    private int mAllTvNumber = 0;
    private int COUNT = 100;

    private class ViewHolder {
        public TextView channelNumberTextview = null; // Channel NO.
        public TextView channelNameTextview = null; // The name of the channel
        public ImageView IsFreeChannelImageView = null; // The icon for scramble
        public ImageView IsFavChannelImageView = null; // The icon for favorite
        public ImageView IsLockChannelImageView = null; // The icon for Lock
        public ImageView IsTvChannelImageView = null; // The icon for DTV or ATV
    }
    public ChannelListAdapter(Context context, ChannelList dtvChannelList, ChannelList atvChannelList) {
        mLayoutInflater = LayoutInflater.from(context);
        setChannelList(dtvChannelList,atvChannelList);
    }

    public void setChannelList(ChannelList dtvChannelList, ChannelList atvChannelList) {
        mChannelList.clear();
        mDtvNumber = 0;
        mDtvChannelList = dtvChannelList;
        mAtvChannelList = atvChannelList;
        if(dtvChannelList != null){
            int times = dtvChannelList.getChannelCount()/COUNT;
            int lastNum = dtvChannelList.getChannelCount() - COUNT*times;
            for (int i = 0;i<times;i++) {
                mChannelList.addAll(dtvChannelList.getChannels(i*COUNT,COUNT));
            }
            mChannelList.addAll(dtvChannelList.getChannels(times*COUNT,lastNum));
            mDtvNumber = mChannelList.size();
        }

        if(atvChannelList != null){
            int times = atvChannelList.getChannelCount()/COUNT;
            int lastNum = atvChannelList.getChannelCount() - COUNT*times;
            for (int i = 0;i<times;i++) {
                mChannelList.addAll(atvChannelList.getChannels(i*COUNT,COUNT));
            }
            mChannelList.addAll(atvChannelList.getChannels(times*COUNT,lastNum));
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        int allChannelNumber = 0;
        if (mChannelList != null) {
            allChannelNumber = mChannelList.size();
        }
        return allChannelNumber;
    }

    @Override
    public Object getItem(int arg0) {
        Channel tempChannel = mChannelList.get(arg0);
        if (null != tempChannel) {
            return tempChannel;
        }
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        Channel tempChannel = (Channel)getItem(arg0);
        if (null != tempChannel) {
            return tempChannel.getChannelID();
        }
        return 0;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        LogTool.v(LogTool.MCHANNEL, "getView() arg0 = " + arg0);
        ViewHolder holder = null;
        if (arg1 == null) {
            holder = new ViewHolder();
            arg1 = mLayoutInflater.inflate(R.layout.channel_list_item, null);
            holder.channelNameTextview = arg1.findViewById(R.id.channel_Name_TextView);
            holder.channelNumberTextview = arg1.findViewById(R.id.channel_Number_TextView);
            holder.IsFreeChannelImageView = arg1.findViewById(R.id.imageView_isFree);
            holder.IsFavChannelImageView = arg1.findViewById(R.id.imageView_isFav);
            holder.IsLockChannelImageView = arg1.findViewById(R.id.imageView_isLock);
            holder.IsTvChannelImageView = arg1.findViewById(R.id.imageView_tvType);
        } else {
            holder = (ViewHolder) arg1.getTag();
        }
        Channel tempChannel = (Channel)getItem(arg0);
        if (null != tempChannel) {
            String number;
            String name;
            DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
            List<Integer> sourceList = HitvManager.getInstance().getSourceManager().getSourceList();
            if (sourceList.contains(halApi.EnumSourceIndex.SOURCE_ATSC) || sourceList.contains(halApi.EnumSourceIndex.SOURCE_ISDBT)) {
                // North and South America need deal with the appearence of ChannelID
                if (tempChannel.getBelongNetwork().getNetworkType() == EnNetworkType.RF || arg0 >= mDtvNumber) {
                    number = String.format("%2d-0", tempChannel.getChannelNo());
                } else {
                    int majorSN = (tempChannel.getLCN() >> 16) & 0xffff;
                    int minorSN = tempChannel.getLCN() & 0xffff;
                    LogTool.v(LogTool.MCHANNEL, "LCN = " + tempChannel.getLCN());
                    number = String.format("%2d-%2d", majorSN, minorSN);
                }
            } else {
                number = fourDig.format(tempChannel.getChannelNo());
            }
            LogTool.v(LogTool.MCHANNEL, "number = " + number);
            holder.channelNumberTextview.setText(number);
            name = tempChannel.getChannelName();
            holder.channelNameTextview.setText(name);
            LogTool.v(LogTool.MCHANNEL, "name = " + name);

            holder.IsTvChannelImageView.setVisibility(View.VISIBLE);
            if (tempChannel.getNetworkType() == EnNetworkType.RF) {
                holder.IsTvChannelImageView.setImageResource(R.drawable.list_menu_img_atv_focus);
            } else {
                EnTVRadioFilter curPlayMode = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME).getChannelManager().getChannelServiceTypeMode();
                if (curPlayMode == EnTVRadioFilter.RADIO) {
                    holder.IsTvChannelImageView.setImageResource(R.drawable.list_menu_img_radio_focus);
                } else if (curPlayMode == EnTVRadioFilter.DATA) {
                    holder.IsTvChannelImageView.setImageResource(R.drawable.list_menu_img_data_focus);
                } else {
                    holder.IsTvChannelImageView.setImageResource(R.drawable.list_menu_img_dtv_focus);
                }

            }

            if (tempChannel.getTag(EnTagType.LOCK)) {
                holder.IsLockChannelImageView.setVisibility(View.VISIBLE);
            } else {
                holder.IsLockChannelImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.isScramble()) {
                holder.IsFreeChannelImageView.setVisibility(View.VISIBLE);
            } else {
                holder.IsFreeChannelImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getFavTag() != null && tempChannel.getFavTag().contains(EnFavTag.FAV_ALL)) {
                holder.IsFavChannelImageView.setVisibility(View.VISIBLE);
            } else {
                holder.IsFavChannelImageView.setVisibility(View.INVISIBLE);
            }

        }

        arg1.setTag(holder);

        return arg1;
    }

}
