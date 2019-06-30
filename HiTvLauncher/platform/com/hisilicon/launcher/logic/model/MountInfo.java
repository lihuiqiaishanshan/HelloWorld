
package com.hisilicon.launcher.logic.model;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IStorageManager;
import com.hisilicon.launcher.util.LogHelper;
import android.os.storage.VolumeInfo;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;

/**
 * the info of local upgrade
 *
 */
public class MountInfo {
    private static final String TAG = "MountInfo";
    public String[] path = new String[64];
    public int[] type = new int[64];
    public String[] label = new String[64];
    public String[] partition = new String[64];
    public int index = 0;

    public MountInfo(Context context) {
        try {
            // support for DevType
            IBinder service = ServiceManager.getService("mount");
            LogHelper.i(TAG, " service =  "+service);
            if (service != null) {
                IStorageManager storageManager = IStorageManager.Stub.asInterface(service);
                VolumeInfo[] list = storageManager.getVolumes(0);
                List<VolumeInfo> mountList = new ArrayList<VolumeInfo>();
                for(int i = 0; i < list.length ; i++){
                   if(2 == list[i].state) { //means only add deivces which state equals to mounted
                       mountList.add(list[i]);
                   }
                }

                VolumeInfo sdcard = new VolumeInfo("sdcard",0,null,null,"SDCARD");
                sdcard.internalPath = "/mnt/sdcard";
                sdcard.state = VolumeInfo.STATE_MOUNTED;
                mountList.add(sdcard);

                index = mountList.size();
                LogHelper.d(TAG, " index =  "+index);
                for (int i = 0; i < index; i++) {
                   path[i] = mountList.get(i).internalPath;
                   if(null == path[i]){
                       continue;
                   }
                   label[i] = path[i].substring(path[i].lastIndexOf('/')+1);
                   partition[i] = label[i];
                   String typeStr = mountList.get(i).devType;
                   if(typeStr == null){
                      typeStr = "USB2.0";
                   }
                   if (path[i].contains("/mnt/sdcard")||path[i].contains("/storage/emulated/0")) {
                       type[i] = 3;
                   } else if (typeStr.equals("SDCARD")) {
                       type[i] = 3;
                   } else if (typeStr.equals("SATA")) {
                       type[i] = 2;
                   } else if (typeStr.equals("USB2.0")) {
                       type[i] = 0;
                   } else if (typeStr.equals("USB3.0")) {
                       type[i] = 1;
                   } else if (typeStr.equals("UNKOWN")) {
                       type[i] = 4;
                   } else if (typeStr.equals("CD-ROM")) {
                       type[i] = 5;
                   }
                }
            }

        } catch (RemoteException e) {
            LogHelper.e(TAG,"Error:" + e.getMessage());
        }
    }
}
