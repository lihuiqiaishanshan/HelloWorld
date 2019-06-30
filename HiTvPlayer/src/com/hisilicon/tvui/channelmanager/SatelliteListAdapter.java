package com.hisilicon.tvui.channelmanager;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.dtv.network.DVBSNetwork;
import com.hisilicon.dtv.network.Network;
import com.hisilicon.tvui.R;

public class SatelliteListAdapter extends BaseAdapter
{
    private List<Network> mList = null;
    private LayoutInflater mLayoutInflater = null;

    public SatelliteListAdapter(Context context, List<Network> list)
    {
        mList = list;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        if (null != mList)
        {
            return mList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0)
    {
        if (null != mList)
        {
            return mList.get(arg0);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int arg0)
    {
        if (null != mList)
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
        TextView mSatNameTextview = null;
        if (arg1 == null)
        {
            arg1 = mLayoutInflater.inflate(R.layout.channel_edit_satellite_list_item, null);
            mSatNameTextview = (TextView) arg1.findViewById(R.id.textView_channel_edit_sat_list_item_name);
        }
        else
        {
            mSatNameTextview = (TextView) arg1.getTag();
        }
        DVBSNetwork tempSatellite = (DVBSNetwork) mList.get(arg0);
        if (null != tempSatellite)
        {
            String name = tempSatellite.getName();
            mSatNameTextview.setText(name);
        }
        arg1.setTag(mSatNameTextview);
        return arg1;
    }

}
