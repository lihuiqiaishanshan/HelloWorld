package com.li.demo.service;

import iapp.eric.utils.base.Trace;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;
import com.li.demo.Utils;
import com.li.demo.meminfo.MemInfoUtils;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import com.li.demo.service.ITakePicRemote;;

public class CopyOfMainService extends Service {
	
	

	private int pid = 0;

	private int pss = 0;

	public static final String MYLOGFILEName = "meminfo.txt";
	
	private MemInfoUtils mu;
	
	private MyBinder iBinder = new MyBinder();

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return iBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Trace.Info("MainService start flags = " + flags + "startId = " + startId);
//		mu = new MemInfoUtils(getApplicationContext());
//		if (pid == 0) {
//			pid = mu.getMyPid();
//		}
//		mHandler.postDelayed(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				takePicture();
//			}
//		}, 5000);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		initLogFile();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
	}
	
	private PictureListener mpc = null;
	
	public class MyBinder extends ITakePicRemote.Stub {

		/* show只是测试方法，可以不要 */
		public void show() {
			Toast.makeText(CopyOfMainService.this, "MyName is MusicPlayerService", Toast.LENGTH_LONG).show();
		}



		@Override
		public void unregister() throws RemoteException {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void register() throws RemoteException {
			// TODO Auto-generated method stub
			
		}



		@Override
		public void takePicture(int width,int height,PictureListener pl) throws RemoteException {
			// TODO Auto-generated method stub
			takePictureOfTV(width,height);
			
		}
	}

	private void takePictureOfTV(int width,int height) {
		KKCommonManager.getInstance(getApplicationContext()).takePictureofTV(720, 576, new TakePictureCallback() {

			@Override
			public void onPictureTaken(Bitmap arg0) {
				// TODO Auto-generated method stub
				try {
					mpc.onChange(arg0);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
//				mHandler.postDelayed(new Runnable() {
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						takePicture();
//					}
//				}, 5000);
//				String sysFree = mu.getSystemAvaialbeMemorySize();
//				if (pid != 0) {
//					pss = mu.getRunningAppProcessInfo(pid);
//				}
//				String log = sysFree + "\t\t" + pss+"\t";
//				writeLogToSDCard(log);

			}
		});
	}

	/**
	 * 根据应用程序的名字打开应用程序?打开失??回false
	 * 
	 * @param AppName
	 * @return ?回值类型
	 */
	public boolean startAppByAppName(String AppName, Context context) {

		if (AppName == null) {

			return false;
		}

		// 优先从应用程序??直接找应用程序名
		List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packs.size(); i++) {

			PackageInfo p = packs.get(i);
			if (AppName.equals(p.applicationInfo.loadLabel(context.getPackageManager()).toString())) {

				// 取到打开应用??的intent
				Intent iT = context.getPackageManager().getLaunchIntentForPackage(p.packageName);
				Log.d("ttttt", "intent to open app = " + iT);
				try {
					context.startActivity(iT);
				} catch (Exception e) {
					// TODO: handle exception
					return false;
				}
				return true;
			}
		}

		// 尝?从?在lancher显示的activity列???查找
		List<ResolveInfo> mAllApps;

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		// 符合上?条件的全?查出来,并且排序
		PackageManager mPackageManager = context.getPackageManager();
		mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
		Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(mPackageManager));
		for (int i = 0; i < mAllApps.size(); i++) {

			ResolveInfo res = mAllApps.get(i);
			String appNameString = res.loadLabel(mPackageManager).toString();
			if (AppName.equals(appNameString) == true) {

				// 找到,发intent启动
				// ?应用的包名和主Activity
				String pkg = res.activityInfo.packageName;
				String cls = res.activityInfo.name;

				Trace.Info("AppName =" + AppName + "package name = " + pkg + ",class = " + cls);
				ComponentName componet = new ComponentName(pkg, cls);

				Intent iT = new Intent();
				iT.setComponent(componet);
				iT.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				try {
					context.startActivity(iT);
				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
					return false;
				}

				return true;
			}
		}

		return false;
	}

	private void initLogFile() {
		List<com.li.demo.model.LocalDiskInfo> l = Utils.getExternalStorage(getApplicationContext());
		if (null != l && l.size() != 0) {
			String MYLOG_PATH_SDCARD_DIR = l.get(0).getPath() + "/meminfo";
			if (!new File(MYLOG_PATH_SDCARD_DIR).exists()) {
				new File(MYLOG_PATH_SDCARD_DIR).mkdir();
				File file = new File(MYLOG_PATH_SDCARD_DIR, MYLOGFILEName);

				try {
					FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
					BufferedWriter bufWriter = new BufferedWriter(filerWriter);
					bufWriter.write("时间" + "\t\t" + "应用内存使用" + "\t" + "系统内存剩余\t");
					bufWriter.newLine();
					bufWriter.close();
					filerWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	// 向存储设备中写入日志文件（这里记录每个操作的耗时）
	private void writeLogToSDCard(String log) {
		Date nowtime = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
		String writeDate = sdf.format(nowtime);
		String MYLOG_PATH_SDCARD_DIR = Utils.getExternalStorage(getApplicationContext()).get(0).getPath() + "/meminfo";
		if (!new File(MYLOG_PATH_SDCARD_DIR).exists()) {
			new File(MYLOG_PATH_SDCARD_DIR).mkdir();
		}
		File file = new File(MYLOG_PATH_SDCARD_DIR, MYLOGFILEName);

		try {
			FileWriter filerWriter = new FileWriter(file, true);// 后面这个参数代表是不是要接上文件中原来的数据，不进行覆盖
			BufferedWriter bufWriter = new BufferedWriter(filerWriter);
			bufWriter.write(writeDate + "\t" + log);
			bufWriter.newLine();
			bufWriter.close();
			filerWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Bitmap processBitmap(int newWidth,int newHeight,Bitmap bm){
		try {
			float width = bm.getWidth();
			float height = bm.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, (int) width,
					(int) height, matrix, true);
			return newBitmap;
		} catch (Exception e) {
			return bm;
		}
	}

}
