package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.utils.FileUtils;

import java.util.List;


/**
 */

public class FileListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> fileInfos;

    public FileListAdapter(Context mContext, List<FileInfo> fileInfos) {
        this.mContext = mContext;
        this.fileInfos = fileInfos;
    }

    @Override
    public int getCount() {
        return fileInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return fileInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder itemHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_activity_list_file_content, null);
            itemHolder = new ViewHolder();
            itemHolder.tv_size = (TextView) convertView.findViewById(R.id.tv_file_size);
            itemHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_file_title);
            itemHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_file_icon);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        FileInfo fileInfo = fileInfos.get(position);
        itemHolder.tv_title.setText(fileInfo.getDisplayName());
        itemHolder.iv_icon.setImageResource(fileInfo.getDefaultIcon());
        itemHolder.tv_size.setText(FileUtils.convertToHumanReadableSize(mContext, fileInfo.getSize()));
        return convertView;
    }

    class ViewHolder {
        TextView tv_title;
        TextView tv_size;
        ImageView iv_icon;
    }
}
