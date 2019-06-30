package com.hisilicon.tvui.setting;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.hal.halApi;

public class SourceLockDialog extends Dialog
{
    private final Context mContext;
    private boolean [] mSourcSelected;
    private String[] mSourceName;
    private List<Integer> mSrcList = new ArrayList<Integer>();

    public SourceLockDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list);
        initView();
    }

    private void initView()
    {
        mSrcList = halApi.getSourceList();
        mSourceName = mContext.getResources().getStringArray(R.array.source_value);
        mSourcSelected = new boolean[mSrcList.size()];
        for(int i = 0; i< mSrcList.size(); i++)
        {
            mSourcSelected[i] = halApi.getSrcLockEnable(mSrcList.get(i));
        }
        TextView mLock_title = (TextView) findViewById(R.id.dialog_list_title);
        ListView mSource_list = (ListView) findViewById(R.id.dialog_list);
        mLock_title.setText(R.string.setting_source_lock);
        SourceLockAdapter mSourceLockAdapter = new SourceLockAdapter();
        mSource_list.setAdapter(mSourceLockAdapter);
        mSource_list.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mSourcSelected[arg2] = !mSourcSelected[arg2];
                //reset source state
                halApi.setSrcLockEnable(mSrcList.get(arg2), mSourcSelected[arg2]);
                ((SourceLockAdapter) arg0.getAdapter()).notifyDataSetChanged();
            }

        });
    }

    private class SourceLockAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return mSrcList.size();
        }

        @Override
        public Object getItem(int arg0)
        {
            return mSourceName[(mSrcList.get(arg0))];
        }

        @Override
        public long getItemId(int arg0)
        {
            return arg0;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2)
        {
            ViewHolder holder = null;

            if (convertView == null)
            {
                holder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.dialog_list_item, null);
                holder.text = (TextView) convertView.findViewById(R.id.dialog_list_text);
                holder.check = (CheckBox) convertView.findViewById(R.id.dialog_list_checkimg);
                convertView.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.text.setText(mSourceName[(mSrcList.get(arg0))]);
            holder.check.setChecked(mSourcSelected[arg0]);
            return convertView;
        }

        class ViewHolder
        {
            public TextView text;
            public CheckBox check;
        }
    }
}
