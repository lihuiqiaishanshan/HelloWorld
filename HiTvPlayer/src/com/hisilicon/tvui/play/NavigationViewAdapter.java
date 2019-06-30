package com.hisilicon.tvui.play;

import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.tvui.R;

public class NavigationViewAdapter extends BaseAdapter
{
    private static final String NAVIBTN_IMAGE_BG = "itemImage";
    private static final String NAVIBTN_IMAGE_ON = "itemImageOn";
    private static final String NAVIBTN_TEXT_LBL = "itemText";

    private ArrayList<HashMap<String, Object>> mNaviItemsList;
    private LayoutInflater mLayoutInflater = null;

    private class ViewHolder
    {

        public FrameLayout mBgImageFrameLayout = null;
        public ImageView mBgImageView = null;
        public TextView mIconNameTextView = null;
    }

    private int mSelected = -1;

    /**
     * Notify which position should zoom out the image.
     * @param id
     */
    public void notifyDataSetChanged(int id)
    {
        mSelected = id;
        super.notifyDataSetChanged();
    }

    public NavigationViewAdapter(Context context, ArrayList<HashMap<String, Object>> naviBtnItems)
    {
        if (null != context)
        {
            mLayoutInflater = LayoutInflater.from(context);
        }
        mNaviItemsList = naviBtnItems;
    }

    @Override
    public int getCount()
    {
        if (null != mNaviItemsList)
        {
            return mNaviItemsList.size();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public Object getItem(int position)
    {
        if (null != mNaviItemsList)
        {
            return mNaviItemsList.get(position);
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
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder mHolder = null;
        if (convertView == null)
        {
            mHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.navigation_list_item, null);
            mHolder.mBgImageFrameLayout = (FrameLayout) convertView.findViewById(R.id.fy_item_bg);
            mHolder.mBgImageView = (ImageView) convertView.findViewById(R.id.iv_item_imag);
            mHolder.mIconNameTextView = (TextView) convertView.findViewById(R.id.tv_item_text);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }
        final HashMap<String, Object> itemMap = mNaviItemsList.get(position);

        // Selected position.
        if (mSelected == position)
        {
            // Show big size icon image.
            mHolder.mBgImageFrameLayout.setBackgroundResource((Integer) itemMap.get(NAVIBTN_IMAGE_ON));
            // Dismiss normal size icon image.
            mHolder.mBgImageView.setBackgroundResource(0);
        }
        else
        {
            // Dismiss big size icon image.
            mHolder.mBgImageFrameLayout.setBackgroundResource(0);
            // Show normal size icon image.
            mHolder.mBgImageView.setBackgroundResource((Integer) itemMap.get(NAVIBTN_IMAGE_BG));
        }
        mHolder.mIconNameTextView.setText((String) itemMap.get(NAVIBTN_TEXT_LBL));
        convertView.setTag(mHolder);

        return convertView;
    }

}
