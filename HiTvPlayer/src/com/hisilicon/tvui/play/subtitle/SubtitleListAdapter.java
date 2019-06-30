package com.hisilicon.tvui.play.subtitle;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.dtv.network.service.EnSubtComponentType;
import com.hisilicon.dtv.network.service.EnSubtitleType;
import com.hisilicon.dtv.network.service.SubtitleComponent;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LanguageMap;

/**
 *
 *  Defined adapter for subtitle ListView.<br>
 *
 */
public class SubtitleListAdapter extends BaseAdapter
{
    private LayoutInflater mLayoutInflater = null;

    private List<SubtitleComponent> mSubtitelList;

    private int mPosition = 0;

    private LanguageMap mLanguageMap;

    private Context mContext;

    private class ViewHolder
    {
        private TextView mtv_sub_title = null;

        private ImageView miv_sub_type = null;

        private ImageView miv_sub_checkbox = null;
    }

    public SubtitleListAdapter(Context context, List<SubtitleComponent> subtitleList)
    {
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
            mContext = context;
            mLanguageMap = new LanguageMap(context);
        }
        this.mSubtitelList = subtitleList;

    }

    // set current selected position
    public void setCurrentPosition(int pose)
    {
        mPosition = pose;
    }

    // get current selected position
    public int getCurrentPosition()
    {
        return mPosition;
    }

    public List<SubtitleComponent> getSubtitelList()
    {
        return mSubtitelList;
    }

    @Override
    public int getCount()
    {
        return mSubtitelList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mSubtitelList.get(position);
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
            convertView = mLayoutInflater.inflate(R.layout.subtitle_listitem, null);
            mHolder.mtv_sub_title = (TextView) convertView.findViewById(R.id.tv_sub_item_text);
            mHolder.miv_sub_type = (ImageView) convertView.findViewById(R.id.iv_sub_item_type);
            mHolder.miv_sub_checkbox = (ImageView) convertView.findViewById(R.id.iv_sub_item_check);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }

        // holder.mtv_sub_title.setText(mSubtitelList.get(position).getLanguageCode()+"("
        // +mSubtitelList.get(position).getSubtitleType().toString()+")");

        if (0 == position)
        {
            mHolder.mtv_sub_title.setText(mContext.getResources().getString(R.string.subtitle_off));
        }
        else
        {
            mHolder.mtv_sub_title.setText(mLanguageMap.getLanguage(mSubtitelList.get(position).getLanguageCode()));
        }
        if (EnSubtitleType.SUBTITLE == mSubtitelList.get(position).getSubtitleType())
        {

            if (EnSubtComponentType.HOH == mSubtitelList.get(position).getSubtComponentType())
            {
                mHolder.mtv_sub_title.setText(mHolder.mtv_sub_title.getText() + " (hoh)");
            }

            mHolder.miv_sub_type.setVisibility(View.INVISIBLE);
        }
        else
        {
            mHolder.miv_sub_type.setVisibility(View.VISIBLE);
        }
        if (mPosition == position)
        {
            mHolder.miv_sub_checkbox.setVisibility(View.VISIBLE);
        }
        else
        {
            mHolder.miv_sub_checkbox.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }
}
