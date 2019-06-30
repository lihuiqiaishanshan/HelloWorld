package com.hisilicon.tvui.channelmanager;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.android.tv.TvSourceManager;
import com.hisilicon.android.tvapi.SourceManager;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.EnFavTag;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;

import java.text.DecimalFormat;
import java.util.List;

public class ChannelManagerAdapter extends BaseAdapter {

    private LayoutInflater mLayoutInflater = null;
    private SparseBooleanArray mSparseBooleanArray;
    private List<Channel> mChannelList;

    private class ViewHolder {
        CheckBox channelChexkBox = null;
        ImageView caImageView = null;
        TextView channelNumberTextview = null;
        TextView channelNameTextview = null;
        ImageView favImageView = null;
        ImageView skipImageView = null;
        ImageView lockImageView = null;
    }

    ChannelManagerAdapter(Context context, List<Channel> mChannelList) {
        this.mChannelList = mChannelList;
        if (null != context) {
            mLayoutInflater = LayoutInflater.from(context);
        }
    }

    void setChannelList(List<Channel> mChannelList) {
        this.mChannelList = mChannelList;
        notifyDataSetChanged();
    }

    void setSparseBooleanArray(SparseBooleanArray sparseBooleanArray) {
        mSparseBooleanArray = sparseBooleanArray;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mChannelList.size();
    }

    @Override
    public Object getItem(int i) {
        return mChannelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mViewHolder = null;
        if (convertView == null) {
            mViewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.channel_manager_channel_list_item, null);
            mViewHolder.channelChexkBox = convertView.findViewById(R.id.checkBox_channelmanager_list_item);
            mViewHolder.caImageView = convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_ca);
            mViewHolder.channelNameTextview = convertView.findViewById(R.id.textView_channel_manager_channel_list_item_name);
            mViewHolder.channelNumberTextview = convertView.findViewById(R.id.textView_channel_manager_channel_list_item_no);
            mViewHolder.favImageView = convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_fav);
            mViewHolder.skipImageView = convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_skip);
            mViewHolder.lockImageView = convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_lock);
        } else {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        Channel tempChannel = mChannelList.get(position);
        DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
        if (tempChannel != null) {
            int choiceMode = ((ListView) parent).getChoiceMode();
            int mMajorMinorSnFlag = 0;
            String number;

            DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
            mMajorMinorSnFlag = mDTV.getConfig().getInt(CommonValue.MAJOR_MINOR_SN_FLAG, CommonValue.MAJOR_MINOR_SN_DISABLE);

            if (ListView.CHOICE_MODE_NONE == choiceMode) {
                mViewHolder.channelChexkBox.setVisibility(View.INVISIBLE);
                mViewHolder.channelChexkBox.setChecked(false);
            } else {
                mViewHolder.channelChexkBox.setVisibility(View.VISIBLE);
                if (null != mSparseBooleanArray) {
                    boolean isChecked = mSparseBooleanArray.get(position);
                    mViewHolder.channelChexkBox.setChecked(isChecked);
                }
            }

            LogTool.v(LogTool.MCHANNEL, "position = " + position);
            if (null != tempChannel.getBelongNetwork() && null != tempChannel.getBelongNetwork().getNetworkType()
                    && tempChannel.getBelongNetwork().getNetworkType() == EnNetworkType.RF) {
                number = String.format("%2d-0", tempChannel.getChannelNo());
            } else {
                int majorSN = (tempChannel.getLCN() >> 16) & 0xffff;
                int minorSN = tempChannel.getLCN() & 0xffff;
                number = String.format("%2d-%2d", majorSN, minorSN);
            }
            mViewHolder.channelNumberTextview.setText(number);
            String name = tempChannel.getChannelName();
            mViewHolder.channelNameTextview.setText(name);
            LogTool.v(LogTool.MCHANNEL, "name = " + name);
            if (1 == tempChannel.getCaTag()) {
                mViewHolder.caImageView.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.caImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getFavTag() != null && tempChannel.getFavTag().contains(EnFavTag.FAV_ALL)) {
                mViewHolder.favImageView.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.favImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getTag(EnTagType.HIDE)) {
                mViewHolder.skipImageView.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.skipImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getTag(EnTagType.LOCK)) {
                mViewHolder.lockImageView.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.lockImageView.setVisibility(View.INVISIBLE);
            }

        }

        convertView.setTag(mViewHolder);
        return convertView;
    }
}
