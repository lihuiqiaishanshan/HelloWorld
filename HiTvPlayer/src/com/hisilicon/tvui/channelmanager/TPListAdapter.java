package com.hisilicon.tvui.channelmanager;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.dtv.network.DVBSTransponder;
import com.hisilicon.dtv.network.DVBSTransponder.EnPolarity;
import com.hisilicon.dtv.network.Multiplex;
import com.hisilicon.tvui.R;

public class TPListAdapter extends BaseAdapter
{
    private static final String VIRGULE = "/";
    private static final String H = "H";
    private static final String V = "V";

    private List<Multiplex> mTpList = null;
    private LayoutInflater mLayoutInflater = null;

    public TPListAdapter(Context context, List<Multiplex> tPs)
    {
        mTpList = tPs;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount()
    {
        if (null != mTpList)
        {
            return mTpList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if (null != mTpList)
        {
            return mTpList.get(position);
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int position)
    {
        if (null != mTpList)
        {
            return mTpList.get(position).getID();
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
        TextView mTextViewChannelEditTpListItemName = null;
        if (convertView == null)
        {
            convertView = mLayoutInflater.inflate(R.layout.channel_edit_tp_list_item, null);
            mTextViewChannelEditTpListItemName = (TextView) convertView.findViewById(R.id.textView_channel_edit_tp_list_item_name);
        }
        else
        {
            mTextViewChannelEditTpListItemName = (TextView) convertView.getTag();
        }
        DVBSTransponder tempTp = (DVBSTransponder) mTpList.get(position);
        if (null != tempTp)
        {
            StringBuffer name = new StringBuffer(tempTp.getFrequency() / 1000 + VIRGULE);

            if (EnPolarity.HORIZONTAL == tempTp.getPolarity())
            {
                name.append(H + VIRGULE);
            }
            else if (EnPolarity.VERTICAL == tempTp.getPolarity())
            {
                name.append(V + VIRGULE);
            }
            else
            {
                name.append(VIRGULE);
            }

            name.append(tempTp.getSymbolRate() / 1000);
            mTextViewChannelEditTpListItemName.setText(name);
        }
        convertView.setTag(mTextViewChannelEditTpListItemName);
        return convertView;
    }

}
