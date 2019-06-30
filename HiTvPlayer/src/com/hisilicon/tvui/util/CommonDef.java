package com.hisilicon.tvui.util;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.UserHandle;

public class CommonDef
{
    public static void removeStickyBroadcastEx(Context mContext, Intent intent)
    {
        mContext.removeStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public static void sendBroadcastEx(Context mContext, Intent intent)
    {
        mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
    }

    public static void sendStickyBroadcastEx(Context mContext, Intent intent)
    {
        mContext.sendStickyBroadcastAsUser(intent, UserHandle.ALL);
    }

    public static void startActivityEx(Context mContext, Intent intent)
    {
        mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    public static void startServiceEx(Context mContext, Intent intent)
    {
        mContext.startServiceAsUser(intent, UserHandle.CURRENT);
    }

    public static void bindServiceEx(Context mContext, Intent service, ServiceConnection conn, int flags)
    {
        mContext.bindServiceAsUser(service, conn, flags, UserHandle.CURRENT);
    }
}
