
package com.hisilicon.launcher.logic.model;

import java.util.ArrayList;
import java.util.List;
import com.hisilicon.launcher.util.LogHelper;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.Environment;
import android.os.StatFs;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;

/**
 * system info
 *
 * @author wangchuanjian
 */
public class SystemInfoLogic implements InterfaceLogic {
    private final static String CPU_INFO = "/proc/cpuinfo";
    private final static String GPU_INFO = "/sys/module/mali/parameters/mali_pp_scale_cores";
    private final static String TAG = "SystemInfoLogic";
    private Context mContext;
    private String[] mInfoValueArr;
    Resources res;
    public SystemInfoLogic(Context mContext) {
        super();
        this.mContext = mContext;
        res = mContext.getResources();
        mInfoValueArr = getSystemInif();
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        WidgetType mInfo = null;
        // CPU Performance
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[0]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // Access to the CPU performance information time, the information of
        // setInfo
        mInfo.setInfo(mInfoValueArr[0]);
        mWidgetList.add(mInfo);
        // GPU Performance
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[1]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // Access to the CPU performance information time, the information of
        // setInfo
        mInfo.setInfo(mInfoValueArr[1]);
        mWidgetList.add(mInfo);

        // Memory
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[2]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // Access to the memory information time, the information of setInfo
        mInfo.setInfo(mInfoValueArr[2]);
        mWidgetList.add(mInfo);

        // Storage space
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[3]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // Access to the storage of spatial information, the information of
        // setInfo
        mInfo.setInfo(mInfoValueArr[3]);
        mWidgetList.add(mInfo);

        // Android version
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[4]);
        mInfo.setType(WidgetType.TYPE_TEXTVIEW);
        // Access to Android version information, the information of setInfo
        mInfo.setInfo(mInfoValueArr[4]);
        mWidgetList.add(mInfo);

        // Software version
        mInfo = new WidgetType();
        mInfo.setName(res.getStringArray(R.array.system_info)[5]);
        mInfo.setType(WidgetType.TYPE_LONG_TEXT);
        // Access to the software version information time, the information of
        // setInfo
        // mInfo.setInfo(res.getStringArray(R.array.system_info_value)[5]);
        mInfo.setInfo(Build.DISPLAY);
        mWidgetList.add(mInfo);

        return mWidgetList;
    }

    @Override
    public void setHandler(Handler handler) {
    }

    @Override
    public void dismissDialog() {
        // TODO Auto-generated method stub
    }

    private String[] getSystemInif(){
        String [] arr = new String[5];
        arr[0] = getCpuNumber();
        arr[1] = getGpuNumber();
        arr[2] = getMemorySize();
        arr[3] = getSDCardMemory();
        arr[4] = res.getStringArray(R.array.system_info_value)[1]+ " "+
                SystemProperties.get("ro.build.version.release","0");
        return arr;
    }

    private String getCpuNumber() {
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int line = 0;
        try {
            fileInputStream = new FileInputStream(CPU_INFO);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            String tempString = null;
            byte[] buffer = new byte[4096];
            int len;
            while ((len = bufferedInputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            String result = byteArrayOutputStream.toString();
            String [] ret = result.split("\n");
            for (int i = 0;i < ret.length;i++){
                tempString = ret[i];
                if (tempString.indexOf("processor") == 0) {
                    if(tempString.charAt(tempString.length()-1) >= '0'
                            && tempString.charAt(tempString.length()-1) <= '9'){
                        line++;
                    }
                }
            }
        } catch (IOException e) {
            LogHelper.e(TAG, "getCpuNumberFailed: " + e.getMessage());
        } finally {
            if (fileInputStream != null) {
                 try {
                     fileInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (bufferedInputStream != null) {
                 try {
                     bufferedInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (byteArrayOutputStream != null) {
                 try {
                     byteArrayOutputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
        }
        return line+" "+res.getStringArray(R.array.system_info_value)[0];
    }

    private String getGpuNumber() {
        String tempchars = "2";
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            fileInputStream = new FileInputStream(GPU_INFO);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[4096];
            int len;
            while((len = bufferedInputStream.read(buffer)) != -1){
                byteArrayOutputStream.write(buffer, 0, len);
            }
            String result = byteArrayOutputStream.toString();
            String [] ret = result.split("\n");
            tempchars = ret[0];
        } catch (IOException e) {
            LogHelper.e(TAG, "getGpuNumber failed: " + e.getMessage());
        } finally {
             if (fileInputStream != null) {
                 try {
                     fileInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (bufferedInputStream != null) {
                 try {
                     bufferedInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (byteArrayOutputStream != null) {
                 try {
                     byteArrayOutputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
         }
         return tempchars+" "+res.getStringArray(R.array.system_info_value)[0];
    }
    private String getMemorySize(){
        String MemorySize = "1 G";
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            fileInputStream = new FileInputStream("/proc/meminfo");
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            byte[] buffer = new byte[4096];
            int len;
            while((len = bufferedInputStream.read(buffer)) != -1){
                byteArrayOutputStream.write(buffer, 0, len);
            }
            String result = byteArrayOutputStream.toString();
            String [] ret = result.split("\n");
            String s = ret[0];
            if(s != null) {
                s = s.substring(s.lastIndexOf(':')+1, s.lastIndexOf('k'));
                float size = new Float(s.replaceAll(" ", ""));
                if(size > 1000000){
                    float b = (float)(Math.round(((float)size)/1024/1024*100))/100;
                    MemorySize = b +" G";
                }else{
                    MemorySize = size/1024+" M";
                }
            }
        } catch (IOException e) {
            LogHelper.e(TAG, "getMemorySize failed: " + e.getMessage());
        } finally {
             if (fileInputStream != null) {
                 try {
                     fileInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (bufferedInputStream != null) {
                 try {
                     bufferedInputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
             if (byteArrayOutputStream != null) {
                 try {
                     byteArrayOutputStream.close();
                 } catch (IOException e) {
                     LogHelper.e(TAG, e.getMessage());
                 }
             }
         }
         return MemorySize;
    }
    public String getSDCardMemory() {
        String MemorySize = "1 G";
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long size = bSize * bCount;
            if(size > 1000000000){
                float b = (float)(Math.round(((float)size)/1024/1024/1024*100))/100;
                MemorySize = b +" G";
            }else{
                MemorySize = size/1024/1024+" M";
            }
        }
        return MemorySize;
    }
}
