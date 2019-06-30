package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.MenuInfo;

/**
 */

public class MenuListAdapter extends BaseAdapter {

    private Context mContext;
    private MenuInfo menuInfo;
    private String[] stringArray;


    public MenuListAdapter(Context mContext, MenuInfo menuInfo) {
        this.mContext = mContext;
        this.menuInfo = menuInfo;
        initData();
    }

    private void initData() {
        int resArrays = menuInfo.getResArrays();
        stringArray = mContext.getResources().getStringArray(resArrays);
    }

    @Override
    public int getCount() {
        if (null != stringArray)
            return stringArray.length;
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (null != stringArray && position < stringArray.length) {
            return stringArray[position];
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder itemHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_menu_list, null);
            itemHolder = new ViewHolder();
            itemHolder.ctv = (CheckedTextView) convertView.findViewById(R.id.ctv_menu);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        if (null != stringArray && stringArray.length > position) {
            String title = stringArray[position];
            if (!TextUtils.isEmpty(title)) {
                itemHolder.ctv.setText(stringArray[position]);
            }
        }
        return convertView;
    }

    class ViewHolder {
        CheckedTextView ctv;
    }
}
