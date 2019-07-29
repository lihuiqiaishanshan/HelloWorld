package com.li.demo.share.share;

import iapp.eric.utils.base.StringOperations;
import iapp.eric.utils.base.Timestamp;
import iapp.eric.utils.base.Trace;
import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.konka.android.net.NetworkUtils;
import com.konka.android.net.samba.IOnSmbResult;
import com.konka.android.net.samba.SambaManager;
import com.konka.android.net.samba.SambaManager.IOnMountListener;
import com.konka.android.net.samba.SmbAuthentication;
import com.konka.android.net.samba.SmbDevice;
import com.konka.android.net.samba.SmbShareFolder;
import com.konka.android.net.samba.SmbStatus;

/**
 * 
 * Created on: 2014-1-10
 * 
 * @brief 为家庭共享提供数据
 * @author Li Huiqi
 * @date Lastest modified on: 2014-1-10
 * @version V1.0.0.00
 * 
 * 
 */
public class ShareProvider implements IOnSmbResult, IOnMountListener {
	private SambaManager mSmbManager = null;
	private static ShareProvider mShareProvider = null;
	private Handler mHandler;
	private List<String> mHostNameList = new ArrayList<String>();
	private static List<String> mHostList = new ArrayList<String>();
	public static List<SmbDevice> mDeviceList = new ArrayList<SmbDevice>();
	public static SmbDevice mSmbDevice = null;
	public static SmbDevice sLastSmbDevice = null;
	public ArrayList<SmbShareFolder> mCurrentFolderList = new ArrayList<SmbShareFolder>();
	private boolean mLogin = false; 

	private ShareProvider(Context c) {
		if (c == null) {
			throw new RuntimeException("[SHARE]param app is null");
		}
		mSmbManager = SambaManager.getInstance(c);
		mSmbManager.setOnMountListener(this);
	}

	public static ShareProvider getInstance(Context c) {
		if (null == mShareProvider) {
			mShareProvider = new ShareProvider(c);
		}
		return mShareProvider;
	}

	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}

	public void setSambaManager(SambaManager smbManager) {
		this.mSmbManager = smbManager;
	}

	public SambaManager getSambaManager() {
		return this.mSmbManager;
	}
	
	public boolean pingHost(SmbDevice smbDevice){
	    String remotePath = smbDevice.remotePath();
        String currentIp = remotePath.substring(2);//当前的ip
        if(!NetworkUtils.pingHost(currentIp, 1)){
            return false;
        }
        return true;
	}

	/**
	 * @brief 获取主机设备列表
	 */
	public List<SmbDevice> getHostDevice() {
		Trace.Info("[SHARE]getHostDevice!!");
//		mDeviceList.clear();
		mDeviceList = mSmbManager.getDeviceList();
		for(SmbDevice d : mDeviceList){
			Trace.Info("[SHARE]ip:"+d.getAddress());
		}
		Collections.sort(mDeviceList, new SortByAddress());
		return mDeviceList;
	}
	
	public void resetHostList() {
		if(null != mDeviceList){
			mDeviceList.clear();
		}
	}

	public void searchHostNameOne(final SmbDevice device, final int pos, final int size){
		new Thread(new Runnable() {
			@Override
			public void run() {
				Trace.Info("[SHARE]find one host name start...");
				if(pos >= size) return;
				String name = null;
				try {
					name = device.getHostName();// 获取计算机名
				} catch (UnknownHostException e) {
					e.printStackTrace();
				}
				if(null == name){
					name = device.getAddress();
				}
				Message msg = new Message();
				msg.arg1 = pos;
				msg.arg2 = size;
				Bundle bundle = new Bundle();
				msg.setData(bundle);
				mHandler.sendMessage(msg);
				Trace.Info("[SHARE]found name: ["+device.getAddress()+"]"+name);
			}
		}).start();
	}
	
	public void sortList(List<String> list, List<SmbDevice> deviceList){

		HashMap<String, SmbDevice> devMap = new HashMap<String, SmbDevice>();
		int count = 0;
		for(String name : list){
			devMap.put(name, deviceList.get(count));
			count++;
		}
		Object[] devKeys = devMap.keySet().toArray();
		Arrays.sort(devKeys, new SortByName());
		deviceList.clear();
		list.clear();
		for(Object key : devKeys){
			Trace.Info("key:"+key.toString());
			deviceList.add(devMap.get((String)key));
			list.add((String)key);
		}
	}
	
	public void sambaLogin(String username, String password) {
		Trace.Info("[SHARE]login... ip = " + mSmbDevice.getAddress());
		Timestamp.timerStart();
		if (username.length() >= 1 && password.length() >= 1) {
			// 如果用户有输入，就认为用户设置了账户和密码
			SmbAuthentication auth = new SmbAuthentication(username, password);
			mSmbDevice.setAuth(auth);
		} else {
			mSmbDevice.setAuth(null);
		}
		Timestamp.timerEnd("[SHARE]login time");
		mSmbDevice.setOnSmbResult(this);
		mCurrentFolderList.clear();
		mCurrentFolderList = mSmbDevice.getSharefolderList();
		if(null == mCurrentFolderList){
			Trace.Warning("[SHARE]fail getting file！！！");
		}
		if(0 == mCurrentFolderList.size()){
			Trace.Warning("[SHARE]got no file！！！");
		}
		for (SmbShareFolder sf : mCurrentFolderList) {
			Trace.Info("[SHARE]" + sf.getFileName());
			Trace.Info("[SHARE]" + sf.getPath());
		}
		Timestamp.timerStart();
		mountDevice();
		Timestamp.timerEnd("[SHARE]mount time");
	}
	
	public void mountDevice(){
		if(null != mSmbManager){
			mSmbManager.mount(mSmbDevice);
		}
	}

	public void login(String name, String password){
		new Thread(new SambaLoginThread(name, password)).start();
	}

	class SambaLoginThread implements Runnable{
		String mUserName;
		String mPassword;
		public SambaLoginThread(String username, String password){
			this.mUserName = username;
			this.mPassword = password;
		}
		@Override
		public void run(){
			//umount last smbDevice
			if(sLastSmbDevice != null){
				Timestamp.timerStart();
				mSmbManager.unmount(sLastSmbDevice);
				sLastSmbDevice = null;
				Timestamp.timerEnd("[SHARE]unmount time");
			}
			sambaLogin(mUserName, mPassword);
		}
	}
	
	public void setSmbDevice(int pos) {
		if (mDeviceList.size() != 0) {
			mSmbDevice = mDeviceList.get(pos);
		}
	}
	
	public void setSmbDeviceNull() {
		mSmbDevice = null;
	}
	
	public SmbDevice getSmbDevice() {
		return mSmbDevice;
	}

	@Override
	public void onSmbResult(SmbStatus resultStatus) {
		mLogin = true;
		switch (resultStatus) {
		case SMB_STATUS_OK:
			Trace.Info("[SHARE]SMB_STATUS_OK");
//			mHandler.sendEmptyMessage(Constant.SEARCH_FOLDER_SUCCESS);
			break;
		case SMB_STATUS_LOGON_FAILURE:
			mLogin = false;
			break;
		case SMB_STATUS_FAILED:
			Trace.Info("[SHARE]SMB_STATUS_FAILED");
//			mHandler.sendEmptyMessage(Constant.SEARCH_FOLDER_FAIL);
			break;
		}
	}
	
	@Override
	public void onMount(SmbDevice device, SmbStatus resultState){
		switch(resultState){
		case SMB_STATUS_FAILED:
			Trace.Info("[SHARE]onMount-->SMB_STATUS_FAILED. ip=" + device.getAddress());
//			mHandler.sendEmptyMessage(Constant.MOUNT_DEVICE_FAIL);
			break;
		case SMB_STATUS_OK:
			Trace.Info("[SHARE]onMount-->SMB_STATUS_OK. ip=" + device.getAddress());
//			mHandler.sendEmptyMessage(Constant.MOUNT_DEVICE_SUCCESS);
			break;
		default:
			break;
		}

		if(mLogin){
			mLogin = false;
			if(null == mCurrentFolderList){
				Trace.Warning("[SHARE]fail getting file！！！");
			}else if(0 == mCurrentFolderList.size()){
				Trace.Warning("[SHARE]got no file！！！");
			}else{
			}
		}
	}
	
	@Override
	public void onUnmount(SmbDevice device, SmbStatus resultState){
		switch(resultState){
		case SMB_STATUS_FAILED:
			Trace.Info("[SHARE]onUnmount-->SMB_STATUS_FAILED. ip=" + device.getAddress());
			break;
		case SMB_STATUS_OK:
			Trace.Info("[SHARE]onUnmount-->SMB_STATUS_OK. ip=" +device.getAddress());
			break;
		default:
			break;
		}
	}

	public List<String> getHostNameList() {
		return mHostNameList;
	}

	public void setHostNameList(List<String> hostNameList) {
		mHostNameList = hostNameList;
	}
	
	public void resetHostNameList() {
		if(null != mHostNameList){
			mHostNameList.clear();
		}
	}
	
	public void setHostIpList(List<String> hostList) {
		mHostNameList = hostList;
	}
	
	public void resetHostIpList() {
		if(null != mHostList){
			mHostList.clear();
		}
	}
	
	public List<String> getHostIpList() {
		return mHostList;
	}
	
	public List<SmbDevice> getDeviceList() {
		return mDeviceList;
	}

	public void setDeviceList(List<SmbDevice> deviceList) {
		mDeviceList = deviceList;
	}

	public ArrayList<SmbShareFolder> getShareFolderList() {
		return this.mCurrentFolderList;
	}

	/**
	 * 按照文件树结构列出当前路径下的子文件
	 * 
	 * @param path
	 * @param eType
	 */
//	public void list(String path, LIST_TYPE eType) {
//		CommonResult result = new CommonResult();
//		List<CommonFileInfo> list = new ArrayList<CommonFileInfo>();
//		if(path.equals(Constant.LAN_ROOT_PATH)){
//			if(null != mCurrentFolderList && 0 != mCurrentFolderList.size()){
//				for(SmbShareFolder sf : mCurrentFolderList){
//					String foldPath = sf.getFileName();
//					foldPath = Constant.LAN_ROOT_PATH + "/" + foldPath.substring(0, foldPath.length()-1);
//					File f = new File(foldPath);
//					Trace.Info("[SHARE]sharefolder path: "+foldPath);
//					list.add(new CommonFileInfo(f));
//				}
//				result.code = CommonResult.OK;
//				result.data = list;
//			}else{
//				result.data = new String("invalid file");
//			}
//		}else{
//			File parent = new File(path);
//			if (parent.exists() && parent.isDirectory()) {
//				File[] subFiles = parent.listFiles();
//				if (subFiles != null && subFiles.length > 0) {
//					for (int i = 0; i < subFiles.length; i++) {
//						switch (eType) {
//						case ALL:
//							list.add(new CommonFileInfo(subFiles[i]));
//							break;
//						case FILE_ONLY:
//							if (subFiles[i].isFile()) {
//								list.add(new CommonFileInfo(subFiles[i]));
//							}
//							break;
//						case FOLDER_ONLY:
//							if (subFiles[i].isDirectory()) {
//								list.add(new CommonFileInfo(subFiles[i]));
//							}
//							break;
//						default:
//							break;
//						}
//
//					}
//				}
//				result.code = CommonResult.OK;
//				result.data = list;
//			} else {
//				result.data = new String("invalid file");
//			}
//		}
//
//		Message msg = Message.obtain();
//		msg.what = Constant.MSG_LIST;
//		msg.obj = result;
//		mGlobalApp.dispachMessage(msg);
//		// GlobalData分发消息MSG_LIST返回结果
//	}

	/**
	 * 递归列出指定类型的文件（视频、音频、文档等）,非文件树结构 默认从数据库读取缓存数据，然后再进行扫描盘符，二次校正
	 * 
	 * @param eMediaType
	 *            : 特别文件类型
	 * @param dirs
	 *            : 各个盘符的根目录集合
	 * @param listParent
	 *            : 是否列出指定文件的父文件夹？true:按两层显示;false:按一层显示
	 * 
	 */
//	public void listWithSpecificMediaType(final MultimediaType eMediaType,
//			final List<String> roots, boolean listParent) {
//
//		int size = roots.size();
//
//		// 所有盘符的文件列表
//		List<CommonFileInfo> allList = new ArrayList<CommonFileInfo>();
//		for (int i = 0; i < size; i++) {
//			// 单个盘符的文件列表
//			List<CommonFileInfo> list = null;
//			if (listParent) {
//				// =====================两层展示============================
//				list = new ArrayList<CommonFileInfo>();
//				List<File> parents = null;
//				// 递归全盘扫描
//				try {
//					parents = getSpecificParents(new File(roots.get(i)),
//							eMediaType);
//				} catch (Exception e) {
//					parents = null;
//				}
//
//				if (parents != null) {
//					int num = parents.size();
//					// 不添加LOST.DIR
//					String lostDirPath = roots.get(i) + "/" + Constant.LOST_DIR;
//					String kkPath = roots.get(i) + "/" + Constant.APP_DIR;
//					String tmpPath;
//					for (int j = 0; j < num; j++) {
//						tmpPath = parents.get(j).getAbsolutePath();
//						if (tmpPath.equals(roots.get(i))) {
//							// 根路径文件夹剔除，加载根目录符合条件的子文件
//							List<CommonFileInfo> subs = getSpecificFiles(
//									parents.get(j), eMediaType, false);
//							if (subs != null && subs.size() > 0)
//								list.addAll(subs);
//						} else if (tmpPath.equals(lostDirPath)
//								|| tmpPath.equals(kkPath)) {
//							continue;
//						} else {
//							list.add(new CommonFileInfo(parents.get(j)));
//						}
//					}
//				}
//			} else {
//				// ========================== 一层展示
//				// ==============================
//				// 递归扫描盘符
//				list = getSpecificFiles(new File(roots.get(i)), eMediaType,
//						true);
//			}
//
//			if (list != null && list.size() > 0) {
//				allList.addAll(list);
//			}
//		}
//
//		CommonResult result = new CommonResult();
//		result.code = CommonResult.OK;
//		result.data = allList;
//		result.data2 = eMediaType;
//
//		Message msg = Message.obtain();
//		msg.what = Constant.MSG_LIST_SPECIFIC_MEDIATYPE;
//		msg.obj = result;
//		mGlobalApp.dispachMessage(msg);
//	}

	/**
	 * 两层展示特定文件一 列出特定文件的父文件夹
	 */
//	private List<File> getSpecificParents(File dir, MultimediaType eMediaType) {
//		File[] children = dir.listFiles();
//
//		// 设为File类型方便列表contains判断，提高效率
//		// 最终返回的list个数远小于子文件总和，再循环一次转CommonFileInfo
//		List<File> list = null;
//		List<File> tmp = null;
//
//		if (children != null && children.length > 0
//				&& !dir.getAbsolutePath().contains(Constant.RECYCLE)) {
//			list = new ArrayList<File>();
//
//			for (int i = 0; i < children.length; i++) {
//				if (children[i].isDirectory()) {// 如果文件是文件夹
//					tmp = getSpecificParents(children[i], eMediaType);
//				} else {
//					if (eMediaType == Utils.getMmt(children[i]
//							.getAbsolutePath())) {
//						if (!list.contains(children[i].getParentFile())) {
//							list.add(children[i].getParentFile());
//						} else
//							continue;
//					} else
//						continue;
//				}
//
//				if (null != tmp && tmp.size() > 0) {
//					list.addAll(tmp);
//					tmp = null;
//				}
//			}
//		}
//		return list;
//	}

	/**
	 * 两层展示特定文件二 列出目录下的指定子文件，不包含子文件夹，非递归
	 */
//	public List<CommonFileInfo> getSpecificFiles(File dir,
//			MultimediaType eMediaType) {
//		return getSpecificFiles(dir, eMediaType, false);
//	}

	/**
	 * 列出特定类型的所有子文件
	 * 
	 * @param rec
	 *            : 是否递归
	 */
//	private List<CommonFileInfo> getSpecificFiles(File dir,
//			MultimediaType eMediaType, boolean rec) {
//
//		File[] children = dir.listFiles();
//
//		List<CommonFileInfo> list = null;
//		List<CommonFileInfo> tmp = null;
//
//		if (children != null && children.length > 0
//				&& !dir.getAbsolutePath().contains(Constant.RECYCLE)) {
//			// 如果文件夹下有文件
//			list = new ArrayList<CommonFileInfo>();
//
//			for (int i = 0; i < children.length; i++) {
//				if (children[i].isDirectory()) {// 如果文件是文件夹
//					if (rec) {
//						tmp = getSpecificFiles(children[i], eMediaType, rec);
//					}
//				} else {
//					if (eMediaType == Utils.getMmt(children[i]
//							.getAbsolutePath())) {
//						list.add(new CommonFileInfo(children[i]));
//					} else {
//						continue;
//					}
//				}
//
//				if (null != tmp && tmp.size() > 0) {
//					list.addAll(tmp);
//					tmp = null;
//				}
//			}
//		}
//		return list;
//	}
	
	private class SortByAddress implements Comparator<SmbDevice> {   
		@Override
	    public int compare(SmbDevice sd1, SmbDevice sd2) {
			String l = sd1.getAddress();
			String r = sd2.getAddress();
			
//			int i1 = l.lastIndexOf(".");
//			int i2 = r.lastIndexOf(".");
//			l = l.substring(i1+1);
//			r = r.substring(i2+1);
//			Trace.Info("l:"+l);
//			Trace.Info("r:"+r);
//			int il = Integer.parseInt(l);
//			int ir = Integer.parseInt(r);
//			Trace.Info("il:"+il);
//			Trace.Info("ir:"+ir);
//			
//			if(i1 - ir > 0) return 1;
//			else if(i1 - ir == 0) return 0;
//			else return -1;
			
			return compareString(l, r);
	    }   
	}
	
	private class SortByName implements Comparator<Object> {   
		@Override
	    public int compare(Object o1, Object o2) {
			String l = (String)o1;
			String r = (String)o2;
			
			return compareString(l, r);
	    }   
	}

	private int compareString(String s1, String s2){

		//待比较的数字
		int lNum = 0,rNum = 0;
		//待比较的文字
		String lText = null,rText = null;
		
		String l = s1;
		String r = s2;
		
		try {
			//两个比较的字串存在共同的特性才进行特殊比较
			if(StringOperations.endsWithDigit(l) && StringOperations.endsWithDigit(r)){
				String lNumStr = StringOperations.getEndDigit(l);
				String rNumStr = StringOperations.getEndDigit(r);
				lNum = Integer.parseInt(lNumStr);
				rNum = Integer.parseInt(rNumStr);
				lText = l.substring(0, l.indexOf(lNumStr));
				rText = r.substring(0, r.indexOf(rNumStr));
			}else if(StringOperations.startWithDigit(l) && StringOperations.startWithDigit(r)){
				String lNumStr = StringOperations.getStartDigit(l);
				String rNumStr = StringOperations.getStartDigit(r);
				lNum = Integer.parseInt(lNumStr);
				rNum = Integer.parseInt(rNumStr);
				lText = l.substring(lNumStr.length());
				rText = r.substring(rNumStr.length());
			}else{
				//正常比较
//				Constant.trace("正常比较1");
				return l.compareToIgnoreCase(r);
			}
		} catch (NumberFormatException e) {
			//数字字符串超长，超过整型范围则按正常比较
			return l.compareToIgnoreCase(r);
		}
		/**
		 * 1、同时为null，则比较的名称是纯数字
		 */
		if((lText == null && rText == null) || lText.compareTo(rText) == 0){
			//文字部分一致
			if(lNum == rNum){
				return 0;
			}else{
				//默认升序
				return(lNum > rNum ? 1 : -1);
			}
		}
		
		//正常比较
//		Constant.trace("正常比较2");
		return l.compareToIgnoreCase(r);
	}
}
