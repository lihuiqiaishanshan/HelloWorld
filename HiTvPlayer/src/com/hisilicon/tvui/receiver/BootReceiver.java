package com.hisilicon.tvui.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hisilicon.tvui.service.DTVService;
import com.hisilicon.tvui.util.CommonDef;
import com.hisilicon.tvui.util.LogTool;

public class BootReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context arg0, Intent arg1)
    {
        String action = arg1.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent serviceIntent = new Intent(arg0, DTVService.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            LogTool.i(LogTool.MREC, "HiDTVPlayer receiver start service");
            //arg0.startService(serviceIntent);
            CommonDef.startServiceEx(arg0, serviceIntent);
        }
    }
}
