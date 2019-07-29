package com.li.demo;

import iapp.eric.utils.base.Audio;
import iapp.eric.utils.base.Trace;
import iapp.eric.utils.metadata.SongInfo;
import iapp.eric.weather.sdk.WeatherApi;
import iapp.eric.weather.sdk.WeatherErrorCode;
import iapp.eric.weather.sdk.model.AlertWeatherInfo;
import iapp.eric.weather.sdk.model.ForecastWeatherInfo;
import iapp.eric.weather.sdk.model.IWeatherSDKListener;
import iapp.eric.weather.sdk.model.LiveWeatherInfo;
import iapp.eric.weather.sdk.model.WeatherSDKResult;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.konka.advert.KKAdManager;
import com.konka.advert.data.AdInfo;
import com.konka.advert.data.GetAdInfoListener;
import com.konka.advert.data.ResultCode;
import com.konka.android.net.ethernet.EthernetDevInfo.CONN_MODE;
import com.konka.android.net.ethernet.EthernetManager;
import com.konka.android.system.KKConfigManager;
import com.konka.android.system.KKConfigManager.EN_KK_SYSTEM_CONFIG_KEY_BOOLEAN;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.KKCommonManager.EN_KK_CAPTURE_MODE;
import com.konka.android.tv.KKCommonManager.EN_KK_CONFIGURATION_INFO_TYPE;
import com.konka.android.tv.KKFactoryManager;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;
import com.konka.tvpay.KKPayClient;
import com.konka.tvpay.data.builder.KonkaPayOrderBuilder;
import com.kuyun.imagetools.ImageResizeTools;
import com.kuyun.imagetools.ImageResizeTools.FORMAT;
import com.li.demo.bluetoothgatt.DeviceScanActivity;
import com.li.demo.contentprovider.MyContacts;
import com.li.demo.contentprovider.ProviderContacts;
import com.li.demo.database.DataBaseHelper;
import com.li.demo.map.BaiduMapOverlayItemsActivity;
import com.li.demo.map.MapDemo;
import com.li.demo.meminfo.MemInfoUtils;
import com.li.demo.model.SearchCondition;
import com.li.demo.myview.BarrageMsg;
import com.li.demo.myview.BarrageMsg.GroupMsg;
import com.li.demo.myview.BarrageSurfaceView;
import com.li.demo.myview.MScroll;
import com.li.demo.service.MainService;
import com.li.demo.ui.CPActivity;
import com.li.demo.ui.GridViewActivity;
import com.li.demo.ui.ImageTestAct;
import com.li.demo.ui.ImageViewActivity;
import com.li.demo.ui.ManageActivity;
import com.li.demo.ui.PicTest;
import com.umeng.analytics.MobclickAgent;

import android.R.integer;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IInterface;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.BitmapFactory.Options;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.bluetooth.BluetoothGattCharacteristic;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements OnClickListener {
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button button6;
	private Button button7;
	private Button button8;
	private Button button9;
	private Button button10;
	private Button button11;
	private Button button12;
	private Button button13;
	private Button button14;
	private Button button15;
	private Button button16;
	private Button button17;
	private Button button18;
	private Button button19;
	private Button button20;
	private BarrageSurfaceView scroll;
	private ImageView iv = null;
	private AutoCompleteTextView act = null;

	private KKPayClient mKKPayClient;

	public static final String TAG = "lhq";

	String[] str = { "g", "ddddd", "各", "大" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Trace.setTag("lhq");
		Trace.Info("###activity oncreate");
		setContentView(R.layout.activity_main);
		
		Intent i = new Intent(MainActivity.this,PicTest.class);
		startActivity(i);
		

	
		
		
//		Intent i = new Intent();
//		ComponentName cn = new ComponentName("com.guoanbn.myzone.clouddisk",
//		"com.guoanbn.myzone.clouddisk.MainActivity");
//		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		i.setComponent(cn);
//		startActivity(i);
		
		
		
		// mKKPayClient = new KKPayClient(this);

		// String file = "/data/misc/konka/test.txt";
		// // String mac = getWifiMacAddr(getApplicationContext());
		// String mac = "";
		// try {
		// mac = loadFileAsString(file);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// Trace.Info("###lhq mac addr"+mac);

		// String cmd = "netcfg eth0 hwaddr aa:bb:cc:dd:ee:cc";
		// java.lang.Process p1;
		// try {
		// p1 = Runtime.getRuntime().exec("/system/bin/su");
		// OutputStream os = p1.getOutputStream();
		// DataOutputStream dos = new DataOutputStream(os);
		// dos.writeBytes(cmd+"\n");
		// dos.writeBytes("exit\n");
		// dos.flush();
		// int i = p1.waitFor();
		// Trace.Info("###lhq"+i);
		// } catch (Exception e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// String type =
		// KKCommonManager.getInstance(getApplicationContext()).getSeries();
		// Trace.Info("###lhq"+type);

		// String id = "a4e3a347cc6dd";
		// Random rd = new Random();
		// int random = rd.nextInt(900)+100;
		// String value = id+random;
		// Trace.Info("###lhq random"+value);
		// Secure.putString(getContentResolver(), Secure.ANDROID_ID, value);
		// String m_szAndroidID = Secure.getString(getContentResolver(),
		// Secure.ANDROID_ID);
		// Trace.Info("###lhq android_id"+m_szAndroidID);
		WeatherApi.init(getApplicationContext(), weatherSDKListener, false);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		scroll = new BarrageSurfaceView(MainActivity.this);
		scroll.setRefresh_time(5);
		scroll.setScroll_pixel(10);
		scroll.setmTextSize(50f);
		scroll.setmBackGround(R.color.transparent);
		// ArrayList<String> list = new ArrayList<String>();
		// list.add("发弹幕");
		// scroll.setText_list(list);
		LayoutParams p = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		addContentView(scroll, p);

		button1 = (Button) findViewById(R.id.contentProvider);
		button1.setOnClickListener(this);
		// button1.setOnKeyListener(new OnKeyListener() {
		//
		// @Override
		// public boolean onKey(View v, int keyCode, KeyEvent event) {
		// // TODO Auto-generated method stub
		// if(event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER){
		// Trace.Info("呵呵");
		// }
		// return true;
		// }
		// });
		button2 = (Button) findViewById(R.id.dialog_test);
		button2.setOnClickListener(this);

		button3 = (Button) findViewById(R.id.get_contentProvider);
		button3.setOnClickListener(this);
		button4 = (Button) findViewById(R.id.yonghuguanlian);
		button4.setOnClickListener(this);
		button5 = (Button) findViewById(R.id.sqlitetest);
		button5.setOnClickListener(this);
		button6 = (Button) findViewById(R.id.baidumap);
		button6.setOnClickListener(this);
		button7 = (Button) findViewById(R.id.cloud_folder);
		button7.setOnClickListener(this);
		button8 = (Button) findViewById(R.id.picTest);
		button8.setOnClickListener(this);
		button9 = (Button) findViewById(R.id.gridTest);
		button9.setOnClickListener(this);
		button10 = (Button) findViewById(R.id.bitmap_config_test);
		button10.setOnClickListener(this);
		button11 = (Button) findViewById(R.id.start_act);
		button11.setOnClickListener(this);
		button12 = (Button) findViewById(R.id.select_text);
		button12.setOnClickListener(this);
		button13 = (Button) findViewById(R.id.alpha_test);
		button13.setOnClickListener(this);
		button14 = (Button) findViewById(R.id.draw_pic);
		button14.setOnClickListener(this);
		button15 = (Button) findViewById(R.id.alarm_test);
		button15.setOnClickListener(this);
		button16 = (Button) findViewById(R.id.broadcast_test);
		button16.setOnClickListener(this);
		button17 = (Button) findViewById(R.id.meminfo_test);
		button17.setOnClickListener(this);
		button18 = (Button) findViewById(R.id.add_list);
		button18.setOnClickListener(this);
		button19 = (Button) findViewById(R.id.lrc_test);
		button19.setOnClickListener(this);
		button20 = (Button) findViewById(R.id.weather_test);
		button20.setOnClickListener(this);
		myHandler.sendEmptyMessage(1);

		act = (AutoCompleteTextView) findViewById(R.id.textview);
		ArrayAdapterZh<String> aa = new ArrayAdapterZh<String>(this, android.R.layout.simple_dropdown_item_1line, str);
		act.setAdapter(aa);
		act.setThreshold(1);
		act.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		// AdvertSDKManager.init(this);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void getContentProvider() {
		Cursor c = managedQuery(ProviderContacts.CONTENT_URI, null, null, null, ProviderContacts._ID);
		if (c.moveToFirst()) {
			int idxID = c.getColumnIndex(ProviderContacts._ID);
			int idxName = c.getColumnIndex(ProviderContacts.CITY);
			int idxNumber = c.getColumnIndex(ProviderContacts.WEATHER);
			int idxEmail = c.getColumnIndex(ProviderContacts.LOWTEMP);
			// Iterator the records
			do {
				System.out.println(c.getInt(idxID));
				System.out.println(c.getString(idxName));
				System.out.println(c.getString(idxNumber));
				System.out.println(c.getString(idxEmail));
			} while (c.moveToNext());
		}
		c.close();
	}

	Bitmap bm;

	private BluetoothAdapter mBluetoothAdapter;

	private BluetoothDevice mDevice = null;

	private boolean mScanning;

	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA";
	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String address = device.getAddress();
					if ("C7:48:5E:2B:CE:DA".equalsIgnoreCase(address)) {
						mDevice = device;
					}
					Trace.Info("###lhq" + device.getName());
					Trace.Info("###lhq" + device.getAddress());
					ParcelUuid[] uuids = device.getUuids();
					if (uuids != null) {
						Trace.Info("###lhq" + uuids.toString());

					} else {
						Trace.Info("###lhq uuid null");
					}

				}
			});
		}
	};

	// Stops scanning after 10 seconds.
	private static final long SCAN_PERIOD = 4000;

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					mScanning = false;
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.contentProvider: {
			Trace.Info("###lhq");

			// Intent i = new Intent("com.konka.multimedia.action.PLAY_MOVIE");
			// i.setPackage("com.konka.multimedia");
			// ArrayList<String> list =new ArrayList<String>();
			// list.add("/mnt/sdcard/Movies/1.mp4");
			// i.putStringArrayListExtra("paths", list);
			// i.putExtra("show_break_point", false);
			// i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(i);
			
			
//			 Intent i6 = new Intent();
//			 i6.setAction("konka.action.START_MM");
//			 i6.putExtra("startFlag", true);
//			 i6.putExtra("diskPath", "/storage/udisk/sda2");
//			 i6.putExtra("st", "apk");
//			 startActivity(i6);
//			 
//			 
//			 MimeTypeMap.getFileExtensionFromUrl("");

//			Intent intent = new Intent();
//			intent.setAction("yixue.action.START_OPEN");
//			intent.setData(Uri.parse("yixue://?action=13&test=http://material.konka.com?test=4&test=haha"));
//			startActivity(intent);


			// Intent bleIntent = new Intent(this,PlayeActivity.class);
			// startActivity(bleIntent);

			// KKAdManager.getInstance().getAdInfo(3005101, new
			// GetAdInfoListener() {
			//
			// @Override
			// public void onResult(int arg0, List<AdInfo> data) {
			// // TODO Auto-generated method stub
			// Trace.Info("###lhq"+arg0+"###"+(data.size()));
			// if(arg0 == 0){
			// if(data != null && data.size() != 0){
			// Trace.Info("###lhq"+data.get(0).getAdId());
			// Trace.Info("###lhq"+data.get(0).getSource());
			//
			// }
			// }
			//
			// }
			// });

			// BluetoothManager bluetoothManager =
			// (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			// mBluetoothAdapter = bluetoothManager.getAdapter();
			// Trace.Info("###lhq"+mBluetoothAdapter.isEnabled());
			//
			// scanLeDevice(mBluetoothAdapter.isEnabled());

			// mHandler.post(r)

			// String sss = PinYin.getPinYin("你好世界");
			// Trace.Info("###lhq" + MainActivity.this.getClass().getName());
			// sss = getDeviceInfo(getApplicationContext());
			// Trace.Info("###lhq" + sss);
			// MobclickAgent.onEvent(getApplicationContext(), "IFENG_HAHA",
			// "sdfdsfdsf");
			// PackageInfo pi = null;
			// try {
			// pi =
			// getPackageManager().getPackageInfo("com.konka.kkmetrowidget.ifengweatherwidget4metro",
			// 0);
			// } catch (NameNotFoundException e2) {
			// // TODO Auto-generated catch block
			// e2.printStackTrace();
			// }
			// Toast.makeText(getApplicationContext(), pi.versionCode+"",
			// Toast.LENGTH_LONG).show();
			// try {
			// Class.forName("com.konka.android.media.KKMediaPlayer");
			// } catch (ClassNotFoundException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			// new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// String url =
			// "http://test.box.api.3g.youku.com/ykew/konka/huanggang/filter?pid=bceff1e60d1cdd4e";
			// String jsonString = HttpUtils.doGet(url,
			// null).get(HttpUtils.RETURN);
			// SearchCondition sc = JSON.parseObject(jsonString,
			// SearchCondition.class);
			// List<String> grade = sc.getResults().getEdu_grade();
			// List<String> subject = sc.getResults().getEdu_subject();
			// List<String> book = sc.getResults().getEdu_book();
			// for(String s : grade){
			// Trace.Info("grade:"+s);
			// }
			// for(String s : subject){
			// Trace.Info("subject:"+s);
			// }
			// for(String s : book){
			// Trace.Info("book:"+s);
			// }
			//
			// }
			// }).start();

			// try {
			// Class c = Class.forName("android.os.SystemProperties");
			// Method m = c.getMethod("set", String.class,String.class);
			// m.invoke(null, "haha","test");
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }

			// char [] c = new char[]{'a','b'};
			//
			//
			//
			// try {
			// Class cc = Class.forName("java.lang.String");
			// Method m = cc.getMethod("valueOf", char[].class);
			// Trace.Info((String)m.invoke(null, c));
			// } catch (Exception e) {
			// // TODO: handle exception
			// }

			// Intent i = new Intent("android.intent.action.VIEW");
			// i.setDataAndType(Uri.parse("http://hls01.ott.disp.cibntv.net/2016/06/07/633816af26e54474bd2d1b6dca2d4538/0ed32c7464b8ff7a1115e61069e742d1.m3u8?k=e4fb05f99ece51ff06f82fc44a00ba1a&channel=cibn&t=1468987559&ttl=86400"),
			// "video/*");
			// startActivity(i);

			// String type =
			// KKCommonManager.getInstance(getApplicationContext()).getSeries();
			// Trace.Info("###lhq"+type);

			// boolean isSupportDolby = false;
			//
			// try {
			// isSupportDolby =
			// KKConfigManager.getInstance(getApplicationContext()).getBooleanConfig(EN_KK_SYSTEM_CONFIG_KEY_BOOLEAN.SUPPORT_DOLBY_CERTIFICATION);
			// } catch (Error e) {
			// // TODO: handle exception
			// isSupportDolby = false;
			// }

			// Intent i = new Intent("konka.game.action.START");
			// i.putExtra("start_type", "start_app");
			// sendBroadcast(i);

			// Intent i = new Intent(this, MainService.class);
			// i.putExtra("cmd", "docibn");
			// startService(i);

			// KKCommonManager.getInstance(getApplicationContext()).takePictureofTV(200,
			// 100,
			// new TakePictureCallback() {
			//
			// @Override
			// public void onPictureTaken(Bitmap arg0) {
			// // TODO Auto-generated method stub
			// Trace.Info("###lhq" + (arg0 != null));
			//
			// }
			// });

			// Intent i = new Intent();
			// i.setAction("yixue.action.START_OPEN");
			// i.setData(Uri.parse("yixue://?action=1"));
			// startActivity(i);

//			 ComponentName cn = new ComponentName("com.konka.multimedia",
//			 "com.konka.multimedia.modules.MusicActivity");
//			 intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			 intent1.setComponent(cn);
//			 startActivity(intent1);

			// Intent intent1 = new Intent();
			// intent1.setPackage("com.cmcc.miguvideotv");
			// intent1.setData(Uri.parse("miguvideo://miguvideo?action=5&contentId=623273652&returnType=back"));
			// startActivity(intent1);
			//
			//
			// Trace.Info("###lhq"+Build.DEVICE);
			// String device = Build.DEVICE;
			// if(device.contains("mgtv")){
			// Trace.Info("###lhq ok");
			// }

			// KKCommonManager mKKCommonManager =
			// KKCommonManager.getInstance(getApplicationContext());
			// String a =
			// mKKCommonManager.getStringConfigurationInfo(EN_KK_CONFIGURATION_INFO_TYPE.SYSTEM_INFO_TYPE,
			// "KONKA_TV");
			// Trace.Info("###lhq"+a);

			// sendBroadcast(new Intent("com.konka.livelauncher.start_login"));

			// KKFactoryManager.getInstance(getApplicationContext()).writeMac(macStrToByte("66:66:66:88:99:99"));

			// String hardware = Build.HARDWARE;
			// String model = Build.MODEL;
			// int version = Build.VERSION.SDK_INT;
			// Trace.Info("###hardware:"+hardware+"model:"+model+"version:"+version);

		}
			break;
		case R.id.dialog_test: {

			mDevice.connectGatt(this, false, new BluetoothGattCallback() {
				@Override
				public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
					String intentAction;
					if (newState == BluetoothProfile.STATE_CONNECTED) {
						intentAction = ACTION_GATT_CONNECTED;
						mConnectionState = STATE_CONNECTED;
						// broadcastUpdate(intentAction);
						Log.i(TAG, "Connected to GATT server.");
						Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());

					} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
						intentAction = ACTION_GATT_DISCONNECTED;
						mConnectionState = STATE_DISCONNECTED;
						Log.i(TAG, "Disconnected from GATT server.");
						// broadcastUpdate(intentAction);
					}
				}

				@Override
				// New services discovered
				public void onServicesDiscovered(BluetoothGatt gatt, int status) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						// broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
					} else {
						Log.w(TAG, "onServicesDiscovered received: " + status);
					}
				}

				@Override
				// Result of a characteristic read operation
				public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
						int status) {
					if (status == BluetoothGatt.GATT_SUCCESS) {
						// broadcastUpdate(ACTION_DATA_AVAILABLE,
						// characteristic);
					}
				}
			});

			// String publicKey =
			// "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkWftIDKA0eBHj+dxyfatMzcOvq/adBQfUk1HKIHBpQQnx4D7qvU/ki0Ku2dIByihBdYf93fUgW9UYvVNmNyQdQWC2Y2RxzL53pGN+zvZj/6YJUy9Wk7/vVt7AhKFHQG8rfikeAiJYA1idnHb+LvdrJg8TPUr4EDRSAiLbVMAzqQIDAQAB";
			//
			// // 易学生成购买订单接口
			// Map<String, String> m1 = new HashMap<String, String>();
			// m1.put("deviceid", "KONKA0000000843T1A03");
			// m1.put("userid", "64928");
			// m1.put("username", "13242902333");
			// m1.put("comboid", "4");
			// m1.put("comboname", "黄冈教育月套餐");
			// m1.put("price", "0.01");
			// m1.put("channel_tv", "1");
			// m1.put("cp_private_info", "wu");
			//
			// // // //易学获取购买商品接口
			// // Map<String, String> m1 = new HashMap<String, String>();
			// // m1.put("deviceid", "KONKA0000000605T1A03");
			// // m1.put("userid", "101010");
			//
			// // String
			// //
			// json="{\"userid\":\"test\",\"deviceid\":\"KONKA0000000605T1A03\",\"username\":\"test\",\"comboid\":\"105\",\"comboname\":\"黄冈名师\",\"price\":\"0.01\",\"channel_tv\":\"KONKA_TV\",\"cp_private_info\":\"a\"}";
			// // m1.put("userid", "64928");
			// // m1.put("deviceid", "test");
			//
			// Gson g = new Gson();
			// String json = g.toJson(m1).trim();
			// json = json.trim();
			// Trace.Info("###lhq" + json);
			//
			// try {
			// byte[] enst = RSAUtils.encryptByPublicKey(json.getBytes(),
			// publicKey);
			// String encrypted_params = Base64Utils.encode(enst);// 加密结果。传给服务器。
			// Trace.Info("###lhq" + encrypted_params);
			// } catch (Exception e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			//
			// String s1 =
			// "jwgozyQbRz5odnWlT/sGeRaNAA/tXDIZkg3rHUPDM/feRVTgzz+ih8VPVETvDiXfbfmqbN+W3dZqG5DxqlaG8njZ4lzBDhYhRkJs08F/R6253bq7RRZuP7czzk79njuVbteCY+rkjbjcHnjO++cYm9mwHN0FYpsEYFHrXaOu5NFeMhDLiaW8uQtiqwTjylsB5ZTqMv/Yz25nVv5jY8d8KEHfM4hxU4uHXn6IJdSOUcjHr2Sai5MrCiheyV7uQG78pJbK1XAS7KiCeqLjt3qC2FHINIVHvuzzIy4GEkFE/IuKgVlSjvqN2T4JhvApRE3VlkMGS0ub92XTHIS2nZBmOBKzWtAQXTvJbijgfR/dd37yVVcVGsH6n0gsVidoMAaxCulaxMF1gTRpbWwPErrxcqTO1DdAa3jnNk2WOFX66oYrRD+lapqqdt4yw477xWb7PFHds5clxitY84iY1MZwoCMxCwcVoW0hPW/krvNvTeVgm1vZ1Jgn2StJXa/POPeC";
			//
			// byte[] encrypt_params = Base64Utils.decode(s1);
			// try {
			// String dest_params = new
			// String(RSAUtils.decryptByPublicKey(encrypt_params, publicKey));
			// Trace.Info("testRSA##" + dest_params.trim());
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			// KonkaPayOrderBuilder orderBuilder = new
			// KonkaPayOrderBuilder().setCpId("1000000000000001")
			// .setAppId("1000000000000003").setGoodsId("10000000000010").setGoodsName("测试商品")
			// .setCpOrderId("4444444444").setPrice(0.01f).setPayAmount(1).setAppUserId("555555555")
			// .setDistributionChannels("1").setCpPrivateInfo("8888888888").setNotifyUrl("www.konka.com")
			// .setSign("jygtliewkybgtugt49432uy95th42v");
			// mKKPayClient.pay(this, orderBuilder);

			// ProgressDialog pd = new ProgressDialog(MainActivity.this);
			// pd.setOnCancelListener(new OnCancelListener() {
			//
			// @Override
			// public void onCancel(DialogInterface dialog) {
			// System.out.println("取消了");
			// }
			// });
			// pd.show();

			// Intent i111 = new Intent("com.konka.ie.KuyunService");
			// startService(i111);

			// BarrageMsg msg = new BarrageMsg();
			// msg.groupId = 1;
			// msg.groupName = "测试群" + 1;
			// msg.nickName = "落无痕";
			// msg.avatarUrl =
			// "http://q.qlogo.cn/qqapp/222222/5E47CBFC315E02CA4A464D70A35AED5D/100";
			// msg.msgList = new ArrayList<BarrageMsg.GroupMsg>();
			//
			// BarrageMsg.GroupMsg groupMsg = new BarrageMsg.GroupMsg();
			// // groupMsg2.msgType = 3;
			// // groupMsg2.msgContent =
			// //
			// "http://grouptalk.c2c.qq.com/?ver=0&rkey=3070020101046930670201010201010204b594fe3104243149674b4a4d6934656b624223376a704456486538736b4864575635516c5075685738760203e69e44042e00000003766572000000013100000008636861747479706500000001310000000866696c657479706500000001310400&filetype=1";
			//
			// groupMsg.msgType = /* BarrageContext.MSG_TEXT */1;
			// groupMsg.msgContent = "发弹幕";
			//
			// msg.msgList.add(groupMsg);
			// BarrageMsg.GroupMsg groupMsg1 = new BarrageMsg.GroupMsg();
			// // groupMsg2.msgType = 3;
			// // groupMsg2.msgContent =
			// //
			// "http://grouptalk.c2c.qq.com/?ver=0&rkey=3070020101046930670201010201010204b594fe3104243149674b4a4d6934656b624223376a704456486538736b4864575635516c5075685738760203e69e44042e00000003766572000000013100000008636861747479706500000001310000000866696c657479706500000001310400&filetype=1";
			//
			// groupMsg1.msgType = /* BarrageContext.MSG_TEXT */2;
			// groupMsg1.msgContent = "hehe";
			// msg.msgList.add(groupMsg1);
			// scroll.addBMsg(msg);
			//
		}
			break;

		case R.id.get_contentProvider:
			Trace.Info("###lhq get_contentProvider");
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					KKCommonManager.getInstance(getApplicationContext()).takePictureofTV(200, 100,
							new TakePictureCallback() {

								@Override
								public void onPictureTaken(Bitmap arg0) {
									// TODO Auto-generated method stub
									Trace.Info("###lhq" + (arg0 != null));

								}
							}, EN_KK_CAPTURE_MODE.CURRENT_ALL);
				}
			}).start();

			// getContentProvider();
			break;
		case R.id.yonghuguanlian:
			Intent i1 = new Intent(MainActivity.this, ManageActivity.class);
			startActivity(i1);
			break;
		case R.id.sqlitetest:
			DataBaseHelper dh = new DataBaseHelper(getApplicationContext());
			dh.getReadableDatabase();
			break;
		case R.id.baidumap:
			Intent i2 = new Intent(MainActivity.this, MapDemo.class);
			startActivity(i2);
			break;
		case R.id.cloud_folder:

			break;
		case R.id.picTest:
			getNewPic();
			break;
		case R.id.gridTest:
			Intent i3 = new Intent(MainActivity.this, GridViewActivity.class);
			startActivity(i3);
			break;
		case R.id.bitmap_config_test:
			Intent i4 = new Intent(MainActivity.this, ImageViewActivity.class);
			startActivity(i4);
			break;
		case R.id.start_act:
			final String coverUrl = "http://imgcache.qq.com/music/photo/album/46/albumpic_124246_0.jpg";

			// new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub
			// HttpGet get = new HttpGet(coverUrl);
			// // 取得HttpClient 对象
			// HttpClient httpclient = new DefaultHttpClient();
			// try {
			// // 请求httpClient ，取得HttpRestponse
			// HttpResponse httpResponse = httpclient
			// .execute(get);
			// if (httpResponse.getStatusLine()
			// .getStatusCode() == HttpStatus.SC_OK) {
			// // 取得相关信息 取得HttpEntiy
			// HttpEntity httpEntity = httpResponse
			// .getEntity();
			// // 获得一个输入流
			// InputStream is = httpEntity.getContent();
			// System.out.println(is.available());
			// System.out.println("Get, Yes!");
			// Bitmap bitmap = BitmapFactory
			// .decodeStream(is);
			// if(bitmap != null){
			// Trace.Info("###lhq 得到网络下载专辑图片");
			// }
			// is.close();
			// }
			//
			// } catch (ClientProtocolException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			// }
			// }).start();

			// try {
			// Trace.Info("###lhq haha url" + coverUrl);
			// HttpURLConnection conn = (HttpURLConnection) new
			// URL(coverUrl).openConnection();
			// Trace.Info("###lhq haha1" + conn.getInputStream().available());
			// } catch (MalformedURLException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }

			Intent i10 = new Intent(Intent.ACTION_VIEW);
			i10.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			String fileNamePath = "/mnt/usb/sda1/音乐/Songs/1.mp3";
			Uri uri = Uri.parse("file://" + fileNamePath);
			// Uri uri=Uri.fromFile(filemusic);
			i10.setDataAndType(uri, "application/vnd.ms-powerpoint");
			startActivity(i10);

			// Intent intent = new Intent();
			// intent.putExtra("FLAG", 10);
			// intent.setAction("com.konka.multimedia.MusicPlayer");
			// sendBroadcast(intent);
			// Trace.Info("musicplayer send broadcast :" + 10);

			// startActivity();

//			 Intent i6 = new Intent();
//			 i6.setAction("konka.action.START_MM");
//			 i6.putExtra("startFlag", true);
//			 i6.putExtra("diskPath", "data/misc/konka/");
//			 startActivity(i6);

			// MediaPlayer mp = new MediaPlayer();
			// try {
			// mp.setDataSource("data/misc/konka/1.mp3");
			// mp.prepare();
			// } catch (IllegalArgumentException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// } catch (SecurityException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// } catch (IllegalStateException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// } catch (IOException e1) {
			// // TODO Auto-generated catch block
			// e1.printStackTrace();
			// }
			//
			// mp.start();

			// Intent i6 = new Intent(getApplicationContext(),
			// TestActivity.class);
			// startActivity(i6);

			break;
		case R.id.alpha_test:
			Intent i5 = new Intent(getApplicationContext(), ImageTestAct.class);
			startActivity(i5);
			break;
		case R.id.draw_pic:

			// 画专辑图片
			// Intent i6 = new Intent(getApplicationContext(),
			// ImageViewActivity.class);
			// startActivity(i6);

			// 主activity隐士启动
			// Intent mm = new Intent("konka.action.START_MM");
			// startActivity(mm);

			// 台标识别读取系统配置
			Boolean m = false;
			try {
				Context context = createPackageContext("com.konka.systemsetting", Context.CONTEXT_IGNORE_SECURITY);

				File dir = new File("/data/data/" + context.getPackageName() + "/shared_prefs/");
				if (!dir.exists()) {
					System.out.println("        mkdirs:" + dir.mkdirs());
				}
				dir.setReadable(true, false);
				dir.setWritable(true, false);
				dir.setExecutable(true, false);

				SharedPreferences sharedPreferences = context.getSharedPreferences(context.getPackageName(),
						Context.MODE_MULTI_PROCESS);

				/***** 读取 *****/
				m = sharedPreferences.getBoolean("smart_push", true);
				System.out.println("        smart_push:" + m);

				/***** 设置 *****/
				// 需要 android:process="system"
				// android:sharedUserId="android.uid.system" 权限
				Editor edit = sharedPreferences.edit();
				edit.putBoolean("smart_push", false);
				System.out.println("    set    smart_push:" + edit.commit());

			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}

			break;
		case R.id.alarm_test:
			// 之前的闹钟测试 先注释掉 测试optionIn
			// AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			// Intent amintent = new Intent("com.li.demo.alarm");
			// PendingIntent operation = PendingIntent.getBroadcast(
			// getApplicationContext(), 1, amintent,
			// PendingIntent.FLAG_CANCEL_CURRENT);
			// PendingIntent operation1 = PendingIntent.getBroadcast(
			// getApplicationContext(), 2, amintent,
			// PendingIntent.FLAG_CANCEL_CURRENT);
			// long date = new Date().getTime()+60000;
			// am.set(AlarmManager.RTC_WAKEUP,date, operation);
			// am.set(AlarmManager.RTC_WAKEUP,date, operation1);

			// OptionIn oi = new OptionIn();
			// oi.content = "您是否接收本场比赛互动数据？";
			// oi.key = "OK键接收，返回键不接收";
			// Intent hehe = new Intent("com.konka.ie.message");
			// hehe.putExtra("launchMode", "option_in");
			// hehe.putExtra("param_option_in", oi);
			// startActivity(hehe);

			// Vote vote = new Vote();
			// vote.title = "支持哪个队？";
			//
			// VoteOption vo1 = new VoteOption();
			// vo1.content = "中国";
			// vote.optionList.add(vo1);
			//
			// VoteOption vo2 = new VoteOption();
			// vo2.content = "日本";
			// vote.optionList.add(vo2);
			//
			// VoteOption vo3 = new VoteOption();
			// vo3.content = "美国";
			// vote.optionList.add(vo3);
			//
			// Intent hehe = new Intent("com.konka.ie.message");
			// hehe.putExtra("launchMode", "vote");
			// hehe.putExtra("param_vote", vote);
			// startActivity(hehe);

			break;

		case R.id.broadcast_test:
			Intent i11 = new Intent("com.konka.multimedia.BG_PLAY_CONTROL");
			i11.putExtra("com.konka.multimedia.PLAY_CONTROL.COMMAND", "com.konka.multimedia.PLAY_CONTROL.PAUSE");
			sendBroadcast(i11);
			break;
		case R.id.meminfo_test:
			String s = new MemInfoUtils(MainActivity.this).getSystemAvaialbeMemorySize();
			Trace.Info("###" + s);
			break;
		case R.id.add_list:

			// ArrayList<String> list = new ArrayList<String>();
			// list.add("发弹幕");
			// scroll.addText_list(list);
			// scroll.addBarrageMsg(new BarrageMsg());
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Bitmap bm = ImageUtils.downloadImg("http://r2.ykimg.com/05160000548A88F367379F43EC0DD809");
					ImageUtils.saveBitmap(bm, "/mnt/usb/sda1/111.jpg");
				}
			}).start();

			break;
		case R.id.lrc_test:
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					Trace.Info("11111111111111111111111111111");
					Audio mAudio = new Audio();
					SongInfo si = mAudio.searchSongInfo("过火", "张信哲");
					Trace.Info("2222222222222222222222222" + (si == null));
				}
			}).start();

			break;
		case R.id.weather_test:

			WeatherApi.init(getApplicationContext(), new IWeatherSDKListener() {

				@Override
				public void onSuccess(WeatherSDKResult result) {
					// TODO Auto-generated method stub
					switch (result.weatherType) {
					case WeatherApi.WT_INIT:
						WeatherApi.getInstance().getLive();
						break;
					case WeatherApi.WT_LIVE_WEATHER:
						LiveWeatherInfo lwi = (LiveWeatherInfo) result.data;

						if (null != lwi) {
							WeatherApi.getInstance().getForecastByCityCode(lwi.cityId);
						} else {
							Trace.Debug("lwi null");
						}

						break;
					case WeatherApi.WT_FORECAST_WEATHER:
						ForecastWeatherInfo fwi = (ForecastWeatherInfo) result.data;
						if (null != fwi) {
							Trace.Debug(fwi.toString());
						} else {
							Trace.Info("###get forecast fail");
						}
						break;
					default:
						break;
					}
				}

				@Override
				public void onFailure(WeatherSDKResult arg0) {
					// TODO Auto-generated method stub

				}
			}, false);
			break;
		default:
			break;
		}
	}

	public void startActivity() {
		Intent intent = new Intent();
		intent.setAction("com.konka.multimedia.action.PLAY_IMAGE");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ArrayList<String> paths = new ArrayList<String>();
		paths.add("/mnt/usb/sda1/呵呵/DSC00582.JPG");
		paths.add("/mnt/usb/sda1/呵呵/DSC00679.JPG");
		intent.putStringArrayListExtra("paths", paths);
		intent.putExtra("index", 1);
		startActivity(intent);
	}

	public void getNewPic() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				String srcPath = "/mnt/usb/sda1/pictest";
				String newPath = "/mnt/usb/sda1/newpic";
				File[] fs = new File(srcPath).listFiles();
				for (int i = 0; i < fs.length; i++) {
					File[] fs1 = fs[i].listFiles();
					if (null != fs1 || fs1.length != 0) {
						for (int j = 0; j < fs1.length; j++) {
							// BitmapFactory.Options opts = new
							// BitmapFactory.Options();
							// opts.inSampleSize = 2;
							// Log.d("lhq", "缩放的倍数"+opts.inSampleSize);
							Bitmap bm = BitmapFactory.decodeFile(fs1[j].getAbsolutePath());
							float width = bm.getWidth();
							float height = bm.getHeight();
							Matrix matrix = new Matrix();
							float scaleWidth = ((float) 640) / width;
							float scaleHeight = ((float) 540) / height;
							matrix.postScale(scaleWidth, scaleHeight);
							Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, (int) width, (int) height, matrix, true);

							String fileName = fs1[j].getAbsolutePath().replaceAll(srcPath, "");

							String path = newPath + fileName;
							Log.d("lhq", "path=" + path);
							mkdir(path);
							File file = new File(path);
							// if(file.exists()){
							// //将已有图片删除掉
							// file.delete();
							// }
							try {
								file.createNewFile();
								FileOutputStream fos = new FileOutputStream(file);
								newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
								fos.flush();
								fos.close();
							} catch (Exception e) {
								// TODO: handle exception
								e.printStackTrace();
							}
						}
					}

				}

			}
		}).start();

	}

	// 自动创建父目录
	public static void mkdir(String path) {
		File fd = null;
		String parent = (new File(path)).getParent();
		try {
			fd = new File(parent);
			if (!fd.exists()) {
				fd.mkdirs();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			fd = null;
		}
	}

	class MyObserver extends ContentObserver {

		public MyObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
		}

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Trace.Info("###activity onResume");
		MobclickAgent.onResume(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Trace.Info("###activity onPause");
		MobclickAgent.onPause(this);
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		Trace.Info("###activity onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Trace.Info("###activity onDestroy");
		mKKPayClient.destroy();
		super.onDestroy();
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			bm.recycle();
			// Intent i = new Intent(MainActivity.this, CPActivity.class);
			// startActivity(i);
			super.handleMessage(msg);
		}

	};

	private IWeatherSDKListener weatherSDKListener = new IWeatherSDKListener() {

		@Override
		public void onSuccess(WeatherSDKResult result) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onFailure(WeatherSDKResult r) {
			// TODO Auto-generated method stub

		}
	};

	public static String getDeviceInfo(Context context) {
		try {
			org.json.JSONObject json = new org.json.JSONObject();
			android.telephony.TelephonyManager tm = (android.telephony.TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);

			String device_id = tm.getDeviceId();

			android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);

			String mac = wifi.getConnectionInfo().getMacAddress();
			json.put("mac", mac);

			if (TextUtils.isEmpty(device_id)) {
				device_id = mac;
			}

			if (TextUtils.isEmpty(device_id)) {
				device_id = android.provider.Settings.Secure.getString(context.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
			}

			json.put("device_id", device_id);

			return json.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Handler myHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
		}

	};

	/**
	 * get wifi mac address
	 * 
	 * @param context
	 * @return ifi mac address
	 */
	private String getWifiMacAddr(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo.getMacAddress().equals("")) {
				return null;
			}
			return wifiInfo.getMacAddress();
		} else {
			return null;
		}
	}

	/**
	 * get mac address
	 * 
	 * @param context
	 * @return mac address
	 */
	// public String getMacAddr(Context context) {
	// EthernetManager mEthManager =
	// EthernetManager.getInstance(context.getApplicationContext());
	// EthernetDevInfo mEthInfo = mEthManager.getConfig();
	// if (mEthInfo == null) {
	// mEthInfo = new EthernetDevInfo();
	// mEthInfo.setConnectMode(CONN_MODE.CONN_MODE_DHCP);
	// }
	// return mEthInfo.getMacAddress();
	// }

	/*
	 * Load file content to String
	 */
	private String loadFileAsString(String filePath) throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
		}
		reader.close();
		return fileData.toString();
	}

	// 写文件
	public void writeFile(String fileName, String write_str) throws IOException {

		File file = new File(fileName);

		FileOutputStream fos = new FileOutputStream(file);

		byte[] bytes = write_str.getBytes();

		fos.write(bytes);

		fos.close();
	}

	private byte[] macStrToByte(String macStr) {
		byte[] macByte = new byte[6];
		String[] array = macStr.split(":");
		for (int i = 0; i < array.length; i++) {
			macByte[i] = (byte) (Integer.valueOf(array[i], 16) & 0xFF);
		}
		return macByte;
	}

}
