//package com.li.demo.ui;
//
//import iapp.eric.utils.base.Trace;
//
//import java.io.IOException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.util.Collections;
//import java.util.List;
//
//import com.li.demo.R;
//import android.app.Activity;
//import android.content.ComponentName;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.View.OnFocusChangeListener;
//import android.view.View.OnKeyListener;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//
//public class TestActivity extends Activity {
//
//	private LinearLayout l1;
//	private ImageView b1;
//	private ImageView b2;
//	private ImageView b3;
//
//	private Button button1;
//
//	private int mFocus = 0;
//	private int mselect = 0;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		// TODO Auto-generated method stub
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.test);
//		l1 = (LinearLayout) findViewById(R.id.layout1);
//		b1 = (ImageView) findViewById(R.id.button1);
//		b2 = (ImageView) findViewById(R.id.button2);
//		b3 = (ImageView) findViewById(R.id.button3);
//
//		b1.setImageResource(R.drawable.main_top_local_s);
//		b2.setImageResource(R.drawable.top_line_short);
//		b3.setImageResource(R.drawable.top_line_short);
//
//		l1.setOnKeyListener(new OnKeyListener() {
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				// TODO Auto-generated method stub
//				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//						if (mFocus == 0) {
//							b2.setImageResource(R.drawable.cloud_selector);
//							b1.setImageResource(R.drawable.top_line_short);
//							b3.setImageResource(R.drawable.top_line_short);
//							mFocus = 1;
//						} else if (mFocus == 1) {
//							b3.setImageResource(R.drawable.samba_selector);
//							b1.setImageResource(R.drawable.top_line_short);
//							b2.setImageResource(R.drawable.top_line_short);
//							mFocus = 2;
//						} else if (mFocus == 2) {
//							return false;
//						}
//						return true;
//					} else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
//						if (mFocus == 0) {
//							return false;
//						} else if (mFocus == 1) {
//							b1.setImageResource(R.drawable.local_selector);
//							b2.setImageResource(R.drawable.top_line_short);
//							b3.setImageResource(R.drawable.top_line_short);
//							mFocus = 0;
//						} else if (mFocus == 2) {
//							b2.setImageResource(R.drawable.cloud_selector);
//							b1.setImageResource(R.drawable.top_line_short);
//							b3.setImageResource(R.drawable.top_line_short);
//							mFocus = 1;
//						}
//					}
//				}
//
//				return false;
//			}
//		});
//		button1 = (Button) findViewById(R.id.button);
//
//		button1.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				// Intent intent = new Intent();
//				// intent.putExtra("cmdName", "Usb_ShowFiles");
//				// intent.setAction("konka.voice.control.action.USB_CONTEXT");
//				// sendBroadcast(intent);
//
//				// ComponentName componet = new ComponentName(
//				// "com.konka.multimedia",
//				// "com.konka.multimedia.modules.BrowseTabActivity");
//				//
//				// Intent i = new Intent();
//				// i.setComponent(componet);
//				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				// startActivity(i);
//				
//				//startService(new Intent("com.li.demo.main_service"));
//				String urlStr = "http://maps.googleapis.com/maps/api/geocode/json?latlng=22.551666,113.92516&sensor=false&language=zh_cn";
//				URL url;
//					try {
//						url = new URL(urlStr);
//						try {
//							Trace.Info("###lhq hehe");
//							HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//							Trace.Info("###lhq haha");
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					} catch (MalformedURLException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				
//			
//
//
//			}
//		});
//
//	}
//
//	/**
//	 * 根据应用程序的名字打开应用程序?打开失??回false
//	 * 
//	 * @param AppName
//	 * @return ?回值类型
//	 */
//	public static boolean startAppByAppName(String AppName, Context context) {
//
//		if (AppName == null) {
//
//			return false;
//		}
//
//		// 优先从应用程序??直接找应用程序名
//		List<PackageInfo> packs = context.getPackageManager()
//				.getInstalledPackages(0);
//		for (int i = 0; i < packs.size(); i++) {
//
//			PackageInfo p = packs.get(i);
//			if (AppName.equals(p.applicationInfo.loadLabel(
//					context.getPackageManager()).toString())) {
//
//				// 取到打开应用??的intent
//				Intent iT = context.getPackageManager()
//						.getLaunchIntentForPackage(p.packageName);
//				Log.d("ttttt", "intent to open app = " + iT);
//				try {
//					context.startActivity(iT);
//				} catch (Exception e) {
//					// TODO: handle exception
//					return false;
//				}
//				return true;
//			}
//		}
//
//		// 尝?从?在lancher显示的activity列???查找
//		List<ResolveInfo> mAllApps;
//
//		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//
//		// 符合上?条件的全?查出来,并且排序
//		PackageManager mPackageManager = context.getPackageManager();
//		mAllApps = mPackageManager.queryIntentActivities(mainIntent, 0);
//		Collections.sort(mAllApps, new ResolveInfo.DisplayNameComparator(
//				mPackageManager));
//		for (int i = 0; i < mAllApps.size(); i++) {
//			ResolveInfo res = mAllApps.get(i);
//			String appNameString = res.loadLabel(mPackageManager).toString();
//			if (AppName.equals(appNameString) == true) {
//
//				// 找到,发intent启动
//				// ?应用的包名和主Activity
//				String pkg = res.activityInfo.packageName;
//				String cls = res.activityInfo.name;
//
//				Trace.Info("AppName =" + AppName + "package name = " + pkg
//						+ ",class = " + cls);
//				ComponentName componet = new ComponentName(pkg, cls);
//
//				Intent iT = new Intent();
//				iT.setComponent(componet);
//				iT.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//				try {
//					context.startActivity(iT);
//				} catch (Exception e) {
//					// TODO: handle exception
//					e.printStackTrace();
//					return false;
//				}
//
//				return true;
//			}
//		}
//
//		return false;
//	}
//
//}
