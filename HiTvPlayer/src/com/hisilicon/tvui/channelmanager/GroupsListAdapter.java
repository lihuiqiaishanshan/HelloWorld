package com.hisilicon.tvui.channelmanager;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.dtv.channel.ChannelList;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;

public class GroupsListAdapter extends BaseAdapter
{
    private List<ChannelList> mAllGroups = null;
    private LayoutInflater mLayoutInflater = null;
    private final Context mContext;

    public GroupsListAdapter(Context context, List<ChannelList> allGroups)
    {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mAllGroups = allGroups;
    }

    @Override
    public int getCount()
    {
        if (null != mAllGroups)
        {
            return mAllGroups.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0)
    {
        if (null != mAllGroups)
        {
            return mAllGroups.get(arg0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int arg0)
    {
        if (null != mAllGroups)
        {
            return arg0;
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
        TextView mGroupNameTextview = null;
        if (arg1 == null)
        {
            arg1 = mLayoutInflater.inflate(R.layout.channel_edit_satellite_list_item, null);
            mGroupNameTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_sat_list_item_name);
        }
        else
        {
            mGroupNameTextview = (TextView) arg1.getTag();
        }
        ChannelList tempChannelList = mAllGroups.get(arg0);
        if (null != tempChannelList)
        {
            String name = tempChannelList.getListName();
            if (name.equals(CommonValue.ALLLIST_NAME))
            {
                name = mContext.getResources().getString(R.string.all);
            }
            mGroupNameTextview.setText(name);
        }
        arg1.setTag(mGroupNameTextview);
        return arg1;
    }

}
