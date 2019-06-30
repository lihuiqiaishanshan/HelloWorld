package com.hisilicon.tvui.pvr;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.LanguageMap;


/**
 * dialog list adapter
 * fit subtitle or audio list to subtitle or audio dialog
 * @author  h00217063
 * @since  1.0
 */
public class DialogListAdapter extends BaseAdapter
{
    private List<String> list;
    private LayoutInflater inflater;
    private int pos;
    private LanguageMap mLanguageMap;
    private boolean mbIsSubtAdapter;
    private Context mContext;

    public List<String> getList()
    {
        return list;
    }

    public void setList(List<String> list)
    {
        this.list = list;
    }

    public void setList(String[] str)
    {
        if ((str != null) && (str.length > 0))
        {
            if (this.list != null)
            {
                this.list.clear();
            }
            else
            {
                list = new ArrayList<String>();
            }

            for (int i = 0; i < str.length; i++)
            {
                list.add(str[i]);
            }
        }
        else
        {
            if (this.list != null)
            {
                this.list.clear();
            }
        }
    }

    public int getPos()
    {
        return pos;
    }

    public void setPos(int pos)
    {
        this.pos = pos;
    }

    public DialogListAdapter(Context context, List<String> list, boolean bIsSubtAdapter)
    {
        this.list = list;
        mLanguageMap = new LanguageMap(context);
        inflater = LayoutInflater.from(context);
        mbIsSubtAdapter = bIsSubtAdapter;
        mContext = context;
    }

    public DialogListAdapter(Context context, String[] str, boolean bIsSubtAdapter)
    {
        list = new ArrayList<String>();
        for (int i = 0; i < str.length; i++)
        {
            list.add(str[i]);
        }
        mLanguageMap = new LanguageMap(context);
        inflater = LayoutInflater.from(context);
        mbIsSubtAdapter = bIsSubtAdapter;
        mContext = context;
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
        ViewHolder holder = null;

        if (convertView == null)
        {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.dialog_list_item, null);
            holder.text = (TextView) convertView.findViewById(R.id.dialog_list_text);
            holder.check = (ImageView) convertView.findViewById(R.id.dialog_list_checkimg);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }

        //holder.text.setText(list.get(position));

        if (mbIsSubtAdapter)
        {
            if (0 == position)
            {
                holder.text.setText(mContext.getResources().getString(R.string.close_subtitle));

            }
            else
            {
                holder.text.setText(mLanguageMap.getLanguage(list.get(position)));
            }

        }
        else
        {
            holder.text.setText(mLanguageMap.getLanguage(list.get(position)));
        }

        if (pos == position)
        {
            holder.check.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.check.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    class ViewHolder
    {
        public TextView text;
        public ImageView check;
    }
}
