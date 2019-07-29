package com.li.demo.meminfo;


import java.util.List;

import iapp.eric.utils.base.Trace;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.text.format.Formatter;

/**
 * @brief 获取内存信息工具类
 * @author Li Huiqi
 *
 */

public class MemInfoUtils {
	private Context mContext = null;
	public MemInfoUtils(Context context){
		this.mContext = context;
	}
	
	public void test(){
		ActivityManager m = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		
	}
	
/**
 * @brief 获取系统可用内存
 */
	
	public String getSystemAvaialbeMemorySize(){
		ActivityManager m = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		m.getMemoryInfo(mi);
		return Formatter.formatFileSize(mContext, mi.availMem);
	}
	
	//getAppInfo
	public int getMyPid(){
		ActivityManager m = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> apps = m.getRunningAppProcesses();
		for(RunningAppProcessInfo app : apps){
			if(app.processName.equalsIgnoreCase("com.li.demo")){
				return app.pid;
			}
		}
		return 0;
	}
	
	
	public int getRunningAppProcessInfo(int pid){
		ActivityManager m = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		int [] pids = new int[1];
		pids[0] = pid;
		android.os.Debug.MemoryInfo [] mi = m.getProcessMemoryInfo(pids);
		int pss = mi[0].getTotalPss();
		Trace.Info("###mi[0].getTotalPss();"+ pss);
		return pss;
	}
	
	

}
