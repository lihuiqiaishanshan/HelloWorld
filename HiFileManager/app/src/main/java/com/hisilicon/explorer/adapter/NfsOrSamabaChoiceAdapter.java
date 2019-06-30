package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.BaseServerInfo;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.NFSItemInfo;

import java.util.List;

/**
 */

public class NfsOrSamabaChoiceAdapter<T extends BaseServerInfo> extends BaseAdapter{
    private Context mContext;
    private List<T> list;

    public NfsOrSamabaChoiceAdapter(Context mContext, List<T> list) {
        this.mContext = mContext;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder itemHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_multiple_choice, null);
            itemHolder = new ViewHolder();
            itemHolder.ctv = (CheckedTextView) convertView.findViewById(R.id.ctv_multiple_choice);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        T itemInfo = list.get(position);
        itemHolder.ctv.setCompoundDrawablesWithIntrinsicBounds(itemInfo.getIconId(),0,0,0);
        itemHolder.ctv.setText(itemInfo.getNickName());
        return convertView;
    }

    class ViewHolder{
        CheckedTextView ctv;
    }
}
