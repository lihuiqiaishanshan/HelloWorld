package com.hisilicon.tvui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hisilicon.tvui.R;

public class FavChoicAdapter extends BaseAdapter
{
    private String[] mStr = null;
    private LayoutInflater mLayoutInflater = null;
    private SparseBooleanArray mSparseBooleanArray = null;

    private class ViewHolder
    {
        public CheckBox checkBox_fav_choic_item = null;
        public TextView favNameTextview = null;
    }

    public FavChoicAdapter(Context context, String[] str)
    {
        mStr = str;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public FavChoicAdapter(Context context, String[] str, SparseBooleanArray boolFavMultiChoiseDialog)
    {
        mStr = str;
        mLayoutInflater = LayoutInflater.from(context);
        mSparseBooleanArray = boolFavMultiChoiseDialog;
    }

    public void setFavTagStrings(String[] str)
    {
        mStr = str;
        notifyDataSetChanged();
    }

    public void setSparseBooleanArray(SparseBooleanArray sparseBooleanArray)
    {
        mSparseBooleanArray = sparseBooleanArray;
        notifyDataSetChanged();
    }

    public SparseBooleanArray getSparseBooleanArray()
    {
        return mSparseBooleanArray;
    }

    public String[] getFavTagStrings()
    {
        return mStr;
    }

    @Override
    public int getCount()
    {
        if (null != mStr)
        {
            return mStr.length;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int arg0)
    {
        if (null != mStr)
        {
            return mStr[arg0];
        }
        else
        {
            return null;
        }
    }

    @Override
    public long getItemId(int arg0)
    {
        if (null != mStr)
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder mViewHolder = null;
        if (convertView == null)
        {
            mViewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.add_to_fav_dialog_item, null);
            mViewHolder.favNameTextview = (TextView) convertView.findViewById(R.id.textView1);
            mViewHolder.checkBox_fav_choic_item = (CheckBox) convertView.findViewById(R.id.checkBox_fav_choic_item);
        }
        else
        {
            mViewHolder = (ViewHolder) convertView.getTag();
        }
        String tempstring = mStr[position];
        if (null != tempstring)
        {
            if (null != mSparseBooleanArray)
            {
                boolean isChecked = mSparseBooleanArray.get(position);
                mViewHolder.checkBox_fav_choic_item.setChecked(isChecked);
            }
            mViewHolder.favNameTextview.setText(tempstring);
        }
        convertView.setTag(mViewHolder);
        return convertView;
    }

}
