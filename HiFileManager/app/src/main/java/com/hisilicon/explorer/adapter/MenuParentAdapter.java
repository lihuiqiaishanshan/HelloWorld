package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.MenuInfo;

import java.util.List;

/**
 */

public class MenuParentAdapter extends BaseAdapter {

    private Context mContext;
    private List<MenuInfo> list;

    public MenuParentAdapter(Context mContext, List<MenuInfo> list) {
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
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_menu_parent, null);
            itemHolder = new ViewHolder();
            itemHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_menu_title);
            itemHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_menu_icon);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        MenuInfo menuInfo = list.get(position);
        itemHolder.tv_title.setText(mContext.getResources().getString(menuInfo.getResTitle()));
        itemHolder.iv_icon.setImageResource(menuInfo.getResIcon());
        return convertView;
    }

    class ViewHolder {
        TextView tv_title;
        ImageView iv_icon;
    }
}
