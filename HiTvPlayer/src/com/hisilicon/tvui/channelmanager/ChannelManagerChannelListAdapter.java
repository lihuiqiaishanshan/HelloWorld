package com.hisilicon.tvui.channelmanager;

import java.text.DecimalFormat;

import android.annotation.SuppressLint;
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
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.constant.EnumSystemTvSystem;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.channel.Channel;
import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.dtv.channel.EnFavTag;
import com.hisilicon.dtv.channel.EnTagType;
import com.hisilicon.dtv.network.EnNetworkType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;

public class ChannelManagerChannelListAdapter extends BaseAdapter
{
    private ChannelList mChannelList = null;
    private LayoutInflater mLayoutInflater = null;
    private SparseBooleanArray mSparseBooleanArray;

    private class ViewHolder
    {
        public CheckBox channelChexkBox = null;
        public ImageView caImageView = null;
        public TextView channelNumberTextview = null;
        public TextView channelNameTextview = null;
        public ImageView favImageView = null;
        public ImageView skipImageView = null;
        public ImageView lockImageView = null;
    }

    public ChannelManagerChannelListAdapter(Context context, ChannelList tempChannelList)
    {
        mChannelList = tempChannelList;
        if (null != context)
        {
            mLayoutInflater = LayoutInflater.from(context);
        }
    }

    public void setChannelList(ChannelList channelList)
    {
        mChannelList = channelList;
        notifyDataSetChanged();
    }

    public void setSparseBooleanArray(SparseBooleanArray sparseBooleanArray)
    {
        mSparseBooleanArray = sparseBooleanArray;
        notifyDataSetChanged();
    }

    public ChannelList getChannelList()
    {
        return mChannelList;
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder mViewHolder = null;
        if (convertView == null)
        {
            mViewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.channel_manager_channel_list_item, null);
            mViewHolder.channelChexkBox = (CheckBox) convertView.findViewById(R.id.checkBox_channelmanager_list_item);
            mViewHolder.caImageView = (ImageView) convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_ca);
            mViewHolder.channelNameTextview = (TextView) convertView.findViewById(R.id.textView_channel_manager_channel_list_item_name);
            mViewHolder.channelNumberTextview = (TextView) convertView.findViewById(R.id.textView_channel_manager_channel_list_item_no);
            mViewHolder.favImageView = (ImageView) convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_fav);
            mViewHolder.skipImageView = (ImageView) convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_skip);
            mViewHolder.lockImageView = (ImageView) convertView.findViewById(R.id.imageView_channel_manager_channel_list_item_lock);
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }

        Channel tempChannel = mChannelList.getChannelByIndex(position);
        DecimalFormat fourDig = new DecimalFormat(CommonValue.FORMAT_STR);
        if (null != tempChannel)
        {
            int choiceMode = ((ListView) parent).getChoiceMode();
            int mMajorMinorSnFlag = 0;
            String number;

            DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
            mMajorMinorSnFlag = mDTV.getConfig().getInt(CommonValue.MAJOR_MINOR_SN_FLAG, CommonValue.MAJOR_MINOR_SN_DISABLE);

            if (ListView.CHOICE_MODE_NONE == choiceMode)
            {
                mViewHolder.channelChexkBox.setVisibility(View.INVISIBLE);
                mViewHolder.channelChexkBox.setChecked(false);
            }
            else
            {
                mViewHolder.channelChexkBox.setVisibility(View.VISIBLE);
                if (null != mSparseBooleanArray)
                {
                    boolean isChecked = mSparseBooleanArray.get(position);
                    mViewHolder.channelChexkBox.setChecked(isChecked);
                }
            }

            LogTool.v(LogTool.MCHANNEL, "position = " + position);
            if(tempChannel.getNetworkType() == EnNetworkType .RF)
            {
                number = fourDig.format(tempChannel.getChannelNo());
            } else {
                if (CommonValue.MAJOR_MINOR_SN_DISABLE == mMajorMinorSnFlag && EnumSourceIndex.SOURCE_ATSC != TvSourceManager.getInstance().getCurSourceId(0)) {
                    number = fourDig.format(tempChannel.getChannelNo());
                } else {
                    int majorSN = (tempChannel.getLCN() >> 16) & 0xffff;
                    int minorSN = tempChannel.getLCN() & 0xffff;
                    number = String.format("%2d-%2d", majorSN, minorSN);
                }
            }
            mViewHolder.channelNumberTextview.setText(number);
            String name = tempChannel.getChannelName();
            mViewHolder.channelNameTextview.setText(name);
            LogTool.v(LogTool.MCHANNEL, "name = " + name);
            if (1 == tempChannel.getCaTag())
            {
                mViewHolder.caImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mViewHolder.caImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getFavTag() != null && tempChannel.getFavTag().contains(EnFavTag.FAV_ALL))
            {
                mViewHolder.favImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mViewHolder.favImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getTag(EnTagType.HIDE))
            {
                mViewHolder.skipImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mViewHolder.skipImageView.setVisibility(View.INVISIBLE);
            }

            if (tempChannel.getTag(EnTagType.LOCK))
            {
                mViewHolder.lockImageView.setVisibility(View.VISIBLE);
            }
            else
            {
                mViewHolder.lockImageView.setVisibility(View.INVISIBLE);
            }
        }

        convertView.setTag(mViewHolder);

        return convertView;
    }

}
