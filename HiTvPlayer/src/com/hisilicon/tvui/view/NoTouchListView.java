package com.hisilicon.tvui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class NoTouchListView extends ListView
{
    public NoTouchListView(Context context)
    {
        super(context);
    }

    public NoTouchListView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public NoTouchListView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onTouchModeChanged(boolean isInTouchMode)
    {
    }
}
