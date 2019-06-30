package com.hisilicon.tvui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

public class MyToast
{
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    @SuppressLint("ShowToast")
    public static Toast makeText(Context context, String msg, int duration)
    {
        if (MyToast.mContext == context)
        {
            mToast.setText(msg);
        }
        else
        {
            MyToast.mContext = context;
            mToast = Toast.makeText(context, msg, duration);
        }

        return mToast;
    }

    @SuppressLint("ShowToast")
    public static Toast makeText(Context context, int resId, int duration)
    {
        if (MyToast.mContext == context)
        {
            mToast.setText(resId);
        }
        else
        {
            MyToast.mContext = context;
            mToast = Toast.makeText(context, resId, duration);
        }

        return mToast;
    }

    private static Toast mToast;
    private static Context mContext;
}
