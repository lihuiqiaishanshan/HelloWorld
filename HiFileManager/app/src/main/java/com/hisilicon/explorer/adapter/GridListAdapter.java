package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.utils.FileUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;

/**
 */

public class GridListAdapter extends BaseAdapter {

    private Context mContext;
    private List<FileInfo> fileInfos;
    private final String IMAGE_MIMETYPE_START = "image";

    public GridListAdapter(Context mContext, List<FileInfo> fileInfos) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_activity_grid_file_content, null);
            itemHolder = new ViewHolder();
            itemHolder.tv_title = (TextView) convertView.findViewById(R.id.tv_file_title);
            itemHolder.iv_icon = (ImageView) convertView.findViewById(R.id.iv_file_icon);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ViewHolder) convertView.getTag();
        }
        FileInfo fileInfo = fileInfos.get(position);
        Picasso.with(mContext).cancelRequest(itemHolder.iv_icon);
        itemHolder.tv_title.setText(fileInfo.getDisplayName());
        if (IMAGE_MIMETYPE_START.equals(fileInfo.getMimeTypes().split("/")[0])) {
            Picasso.with(mContext).load(new File(fileInfo.getPath())).config(Bitmap.Config.RGB_565).fit().centerCrop()
                    .placeholder(fileInfo.getDefaultIcon()).into(itemHolder.iv_icon);
        } else {
            itemHolder.iv_icon.setImageResource(fileInfo.getDefaultIcon());
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_title;
        ImageView iv_icon;
    }
}
