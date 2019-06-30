package com.hisilicon.tvui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hisilicon.tvui.R;

/**
 * <p>the adapter file for Drop-down box，used for the display items of Drop-down box<br>
 * @author z00184946
 *
 */
public class PopupAdapter extends BaseAdapter
{
    private Context mContext;
    private static final float POPUPLINEHEIGHT = 30;
    private String[] mKey = null;
    private TypedArray mPopupArray;

    public PopupAdapter(Context context, String[] key, TypedArray typedArray)
    {
        this.mContext = context;
        this.mKey = key;
        this.mPopupArray = typedArray;
    }

    public int getCount()
    {
        // TODO Auto-generated method stub
        return mKey.length;
    }

    public Object getItem(int position)
    {
        // TODO Auto-generated method stub
        return mKey[position];
    }

    public long getItemId(int position)
    {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // TODO Auto-generated method stub
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        if (convertView == null)
        {
            convertView = mInflater.inflate(R.layout.view_combox_listitem, null);
        }
        int popupHeight = (int) POPUPLINEHEIGHT;
        if (null != mPopupArray)
        {
            popupHeight = (int) mPopupArray.getDimension(R.styleable.popupWindow_lineHeight, POPUPLINEHEIGHT);
        }
        String text = mKey[position];
        TextView textView = (TextView) convertView.findViewById(R.id.id_comm_cmb_lstvalue);
        textView.setHeight(popupHeight);
        textView.setText(text);
        return convertView;
    }

}
