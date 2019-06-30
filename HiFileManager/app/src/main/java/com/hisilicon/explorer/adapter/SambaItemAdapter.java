package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.NFSItemInfo;
import com.hisilicon.explorer.model.SambaItemInfo;

import java.util.List;

/**
 */

public class SambaItemAdapter extends BaseAdapter {
    private Context mContext;
    private List<SambaItemInfo> list;

    public SambaItemAdapter(Context mContext, List<SambaItemInfo> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder itemHolder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_file_row, null);
            itemHolder = new ViewHolder();
            itemHolder.tv_title = (TextView) convertView.findViewById(R.id.text);
            itemHolder.iv_icon = (ImageView) convertView.findViewById(R.id.image_Icon);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        SambaItemInfo sambaItemInfo = list.get(position);
        itemHolder.tv_title.setText(sambaItemInfo.getNickName());
        itemHolder.iv_icon.setImageResource(sambaItemInfo.getIconId());
        return convertView;
    }

    class ViewHolder{
        ImageView iv_icon;
        TextView tv_title;
    }
}
