package com.hisilicon.tvui.installtion;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.tvui.R;

public class GridItemAdapter extends BaseAdapter
{
    Context context;
    private LayoutInflater layoutInflater;
    private List<String> list;

    public GridItemAdapter(Activity activity, Handler handler, List<String> list)
    {
        super();
        context = activity;
        if (list != null)
        {
            this.list = list;
        }
        else
        {
            this.list = new ArrayList<String>();
        }
        layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getCount()
    {
         return list.size();
    }

    @Override
    public Object getItem(int position)
    {
        return list.get(position);
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
        FileItemHolder fileItemHolder;
        if (convertView == null)
        {
            convertView = layoutInflater.inflate(R.layout.grid_item, null);
            fileItemHolder = new FileItemHolder();
            fileItemHolder.text_context = (TextView) convertView.findViewById(R.id.text_context);
            convertView.setTag(fileItemHolder);
        }
        else
        {
            fileItemHolder = (FileItemHolder) convertView.getTag();
        }
        fileItemHolder.text_context.setText(list.get(position));
        return convertView;
    }

}
class FileItemHolder
{
    public TextView text_context;
}

