package com.hisilicon.tvui.play.cc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.dtv.network.service.ClosedCaptionList;
import com.hisilicon.dtv.network.service.EnClosedCaptionType;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LanguageMap;
import com.hisilicon.tvui.util.LogTool;

/**
 *
 *  Defined adapter for subtitle ListView.<br>
 *
 */
public class ClosedCaptionListAdapter extends BaseAdapter
{
    private LayoutInflater mLayoutInflater = null;

    private ClosedCaptionList mCCList;

    private int mPosition = 0;

    private Context mContext;

    private class ViewHolder
    {
        private TextView miv_cc_item_text = null;

        private ImageView miv_cc_checkbox = null;
    }

    public ClosedCaptionListAdapter(Context context, ClosedCaptionList CCList)
    {
        if (null != context)
        {
            this.mLayoutInflater = LayoutInflater.from(context);
            mContext = context;
        }
        this.mCCList = CCList;

    }

    //set cc list
    public void setCCList(ClosedCaptionList ccList)
    {
        LogTool.v(LogTool.MSUBTITLE, "setCCList ");
        mCCList = ccList;
        notifyDataSetChanged();
    }

    //get cc list
    public ClosedCaptionList getCCList()
    {
        LogTool.v(LogTool.MSUBTITLE, "getCCList");
        return mCCList;
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

    @Override
    public int getCount()
    {
        return mCCList.getCCList().size();
    }

    @Override
    public Object getItem(int position)
    {
        return mCCList.getCCList().get(position);
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
        LogTool.v(LogTool.MSUBTITLE, "getView");

        ViewHolder mHolder = null;
        if (convertView == null)
        {
            mHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.cc_listitem, null);

            mHolder.miv_cc_item_text = (TextView) convertView.findViewById(R.id.tv_cc_item_text);
            mHolder.miv_cc_checkbox = (ImageView) convertView.findViewById(R.id.iv_cc_item_check);
            convertView.setTag(mHolder);
        }
        else
        {
            mHolder = (ViewHolder) convertView.getTag();
        }

        LogTool.v(LogTool.MSUBTITLE, "ccListType = " + mCCList.getListType());
        /*list language name*/
        if (0 == position)
        {
            mHolder.miv_cc_item_text.setText(mContext.getResources().getString(R.string.str_off));
        }
        else
        {
            mHolder.miv_cc_item_text.setText(mCCList.getCCList().get(position).getLanguageCode());

            /*set arib list on item*/
            if (EnClosedCaptionType.CCARIB == mCCList.getListType())
            {
                mHolder.miv_cc_item_text.setText(mContext.getResources().getString(R.string.str_on));

            }
        }

        /*select status flag image*/
        if (mPosition == position)
        {
            mHolder.miv_cc_checkbox.setVisibility(View.VISIBLE);
        }
        else
        {
            mHolder.miv_cc_checkbox.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
