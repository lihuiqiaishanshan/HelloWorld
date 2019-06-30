package com.hisilicon.android.videoplayer.activity.adapter;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.menu.MenuKeyValuePair;

import java.util.List;

public class MenuAdapter extends BaseAdapter {
    private Activity activity;
    private List<MenuKeyValuePair> menuItems;
    private int isAllEnable = 1;

    static class Holder {
        ImageView image;
        TextView text;
    }

    public MenuAdapter(Activity a, List<MenuKeyValuePair> items) {
        this.activity = a;
        this.menuItems = items;
    }

    public MenuAdapter(Activity activity, List<MenuKeyValuePair> menuItems, int isAllEnable) {
        this.activity = activity;
        this.menuItems = menuItems;
        this.isAllEnable = isAllEnable;
    }

    public int getCount() {
        return menuItems.size();
    }

    public Object getItem(int position) {
        return menuItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        Holder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.menu_list_item, null);
            holder = new Holder();
            holder.image = (ImageView) convertView.findViewById(R.id.menu_icon);
            holder.text = (TextView) convertView.findViewById(R.id.menu_item);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        MenuKeyValuePair item = menuItems.get(position);
        if (item.getImgResId() != -1) {
            holder.image.setImageResource(item.getImgResId());
        }
        holder.text.setText(item.getValueResId());
        if (isAllEnable != 0) {
            holder.text.setTextColor(Color.WHITE);
        } else if (isAllEnable == 0) {
            if (position == 0 || position == 7) {
                holder.text.setTextColor(Color.WHITE);
            } else {
                holder.text.setTextColor(Color.GRAY);
            }
        }
        return convertView;
    }

    @Override
    public boolean areAllItemsEnabled() {
        if (isAllEnable == 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        if (isAllEnable != 0) {
            return true;
        } else {
            if (position == 0 || position == 7) {
                return true;
            }
            return false;
        }
    }
}
