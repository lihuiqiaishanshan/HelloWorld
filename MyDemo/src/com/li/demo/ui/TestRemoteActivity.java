package com.li.demo.ui;

import iapp.eric.utils.base.Trace;
import com.konka.multimedia.modules.music.IRemote;
import com.li.demo.R;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public class TestRemoteActivity extends Activity {
	private IRemote iRemoteService = null;

	private MyServiceConnection conn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Trace.Info("###OnCreate");
		conn = new MyServiceConnection();
		// 创建一个指向RemoteService的intent
		Intent intent = new Intent();
		intent.setAction("com.konka.multimedia.MusicPlayerService");
		this.bindService(intent, conn, Service.BIND_AUTO_CREATE);

	}

	/**
	 * 实现ServiceConnection接口
	 * 
	 * @author Li
	 * 
	 */
	private class MyServiceConnection implements ServiceConnection {
		/**
		 * 和RemoteService绑定时系统回调这个方法
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// 此处不能使用强制转换, 应该调用Stub类的静态方法获得IRemoteService接口的实例对象
			iRemoteService = IRemote.Stub.asInterface(service);
//			try {
//				Trace.Info("###" + iRemoteService.isStart());
//			} catch (RemoteException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			try {
				Bitmap bm = iRemoteService.getBit();
				if(bm != null){
					Trace.Info("###hahaha");
					
				}
			} catch (RemoteException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Trace.Info("###service bind sucess");
			long dulcation = 0;
			try {
				dulcation = iRemoteService.getDulcation();
				Trace.Info("###" + dulcation + "歌手" + iRemoteService.getSinger() + "名字" + iRemoteService.getTitle());;
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/**
		 * 解除和RemoteService的绑定时系统回调这个方法
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// 解除和RemoteService的绑定后, 将iRemoteService设置为null.
			iRemoteService = null;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if (conn != null) {
			unbindService(conn);
		}
		super.onDestroy();
	}

}