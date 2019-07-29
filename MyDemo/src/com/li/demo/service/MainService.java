package com.li.demo.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Random;

import iapp.eric.utils.base.Trace;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.li.demo.model.CaptureModel;
import com.li.demo.service.ITakePicRemote;

;

public class MainService extends Service {

	private MyBinder iBinder = new MyBinder();

	private java.util.List<CaptureModel> list = null;

	private int mWidth = -1;
	private int mHeight = -1;

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
		
		 new Thread() { // 不可在主线程中调用
		 public void run() {
		 try {
		 Thread.sleep(5000);
		 Instrumentation inst = new Instrumentation();
		 inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);

		 } catch (Exception e) {
		 e.printStackTrace();
		 }
		 }
		
		 }.start();
		if (intent == null) {
			return START_STICKY;
		}

//		if (intent.hasExtra("cmd")) {
//			if (intent.getStringExtra("cmd").equals("dovoole")) {
//				Trace.Info("###dovoole");
//				
//				new Thread(new Runnable() {
//					
//					@Override
//					public void run() {
//						
//						for(int i = 22;i<=99;i++){
//							String cmd = "busybox killall com.voole.epg";
//							String rmcmd = "rm -r /data/data/com.voole.epg/files";
//							execAsRoot(cmd);
//							Trace.Info("###kill first time");
//							
//
//							try {
//								Thread.sleep(4000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							execAsRoot(cmd);
//							Trace.Info("###kill second time");
//							execAsRoot(rmcmd);
//							try {
//								Thread.sleep(4000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							execAsRoot(cmd);
//							Trace.Info("###kill third time");
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							
//							cmd = "netcfg eth0 hwaddr aa:bb:cc:dd:e0:"+i;
//							
//							execAsRoot(cmd);
//							Trace.Info("###mac");
//							
//							 Intent ii =
//							 getPackageManager().getLaunchIntentForPackage("com.voole.epg");
//							 startActivity(ii);
//							 
//							 try {
//								Thread.sleep(10000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							 
//								 Instrumentation inst = new Instrumentation();
//								 
//								 inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
//								 try {
//									Thread.sleep(3000);
//								} catch (InterruptedException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
////								 inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
////								 try {
////									Thread.sleep(3000);
////								} catch (InterruptedException e) {
////									// TODO Auto-generated catch block
////									e.printStackTrace();
////								}
//								 inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
//								 try {
//									Thread.sleep(7000);
//								} catch (InterruptedException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//								 inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
//								 try {
//									Thread.sleep(30000);
//								} catch (InterruptedException e) {
//									// TODO Auto-generated catch block
//									e.printStackTrace();
//								}
//						}
//					}
//				}).start();
//					
////					su(new String[]{"busybox killall com.konka.multimedia"});
//					
//
//					
//					
//					
//					
//					
//					
//					
//					
//					
//					
////					Runtime.getRuntime().exec("netcfg eth0 hwaddr aa:bb:cc:dd:ee:cc");
////					Process p1 = Runtime.getRuntime().exec(cmd);
//
//					
//					
////					Runtime.getRuntime().exec(new String[]{"/system/bin/sh","-c", cmd});
//					
//					
////					Process p1 = Runtime.getRuntime().exec("su");
////					OutputStream os = p1.getOutputStream();
////					DataOutputStream dos = new DataOutputStream(os);
////					dos.writeBytes("netcfg eth0 hwaddr aa:bb:cc:dd:ee:cc\n");
////					dos.writeBytes("exit\n");
////					dos.flush();
////					int i = p1.waitFor();
////					Trace.Info("###lhq"+i);
////					Process p2 = Runtime.getRuntime().exec("busybox ifconfig eth0 hw ether 66:66:66:66:01:11");
////					BufferedReader in = new BufferedReader(new InputStreamReader(p1.getInputStream()));
////					String line = null;
////					while ((line = in.readLine()) != null) {
////						s += line + "/n";
////					}
//					
//
//				// for (int i = 0; i <= 50; i++) {
//				//
//				// try {
//				// Process p =
//				// Runtime.getRuntime().exec("busybox killall com.voole.epg");
//				// Thread.sleep(50);
//				// } catch (Exception e) {
//				// // TODO Auto-generated catch block
//				// e.printStackTrace();
//				// }
//				// try {
//				// Runtime.getRuntime().exec("busybox killall com.voole.epg");
//				// Thread.sleep(50);
//				// } catch (Exception e) {
//				// // TODO Auto-generated catch block
//				// e.printStackTrace();
//				// }
//				//
//				// try {
//				// Runtime.getRuntime().exec("busybox killall com.voole.epg");
//				// Thread.sleep(50);
//				// } catch (Exception e) {
//				// // TODO Auto-generated catch block
//				// e.printStackTrace();
//				// }
//				// Intent ii =
//				// getPackageManager().getLaunchIntentForPackage("com.voole.epg");
//				// startActivity(ii);
//				// new Thread() { // 不可在主线程中调用
//				// public void run() {
//				// try {
//				// Thread.sleep(5000);
//				// Instrumentation inst = new Instrumentation();
//				// inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
//				// Thread.sleep(1000);
//				// inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
//				// Thread.sleep(30000);
//				// } catch (Exception e) {
//				// e.printStackTrace();
//				// }
//				// }
//				//
//				// }.start();
//				//
//				// }
//
//			}else if(intent.getStringExtra("cmd").equals("docibn")){
//				Trace.Info("###do cibn");
//				
//				new Thread(new Runnable() {
//					
//					String cmd = "busybox killall cn.cibntv.ott";
//					String rmcmd1 = "rm -r /data/data/cn.cibntv.ott/files";
//					String rmcmd2 = "rm -r /data/data/cn.cibntv.ott/shared_prefs";
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						for(int i = 0;i<400;i++){
//							Trace.Info("###"+i);
//							
//							execAsRoot(cmd);
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e2) {
//								// TODO Auto-generated catch block
//								e2.printStackTrace();
//							}
//							
//							execAsRoot(cmd);
//							
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e2) {
//								// TODO Auto-generated catch block
//								e2.printStackTrace();
//							}
//							
//							
//							execAsRoot(rmcmd1);
//							
//							try {
//								Thread.sleep(1000);
//							} catch (InterruptedException e2) {
//								// TODO Auto-generated catch block
//								e2.printStackTrace();
//							}
//							
//							execAsRoot(rmcmd2);
//							
//							
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//							
//							
//							
//							String id = "a4e3a347cc6dd";
//							Random rd = new Random();
//							int random = rd.nextInt(899)+100;
//							String value = id+random;
//							Trace.Info("###lhq random"+value);
//							Secure.putString(getContentResolver(), Secure.ANDROID_ID, value);
//							String m_szAndroidID = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
//							Trace.Info("###lhq android_id"+m_szAndroidID);
//							
//							 Intent ii = getPackageManager().getLaunchIntentForPackage("cn.cibntv.ott");
//							 startActivity(ii);
//							 
//							 
//							 try {
//								Thread.sleep(10000);
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//							 
//							 
//							
//						}
//						
//						
//					}
//				}).start();
//				
//				
//
//				
//				
//
//				
//				
//				
//				
//				
//				
//			}else {
//				ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//				String packageName = am.getRunningTasks(2).get(0).topActivity.getPackageName();
//				Trace.Info("###lhq packagename = :" + packageName);
//			}
//		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	
	private void execAsRoot(String cmd){
		
		Trace.Info("###lhq cmd:"+cmd);
		java.lang.Process p1;
		try {
			p1 = Runtime.getRuntime().exec("/system/bin/su");
			OutputStream os = p1.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
			dos.writeBytes(cmd+"\n");
			dos.writeBytes("exit\n");
			dos.flush();
			int i = p1.waitFor();
			Trace.Info("###lhq"+i);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
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
		Trace.Info("###onLowMemory");
		super.onLowMemory();
	}

	private PictureListener mpc = null;

	public class MyBinder extends ITakePicRemote.Stub {

		/* show只是测试方法，可以不要 */
		public void show() {
			Toast.makeText(MainService.this, "MyName is MusicPlayerService", Toast.LENGTH_LONG).show();
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
		public void takePicture(int width, int height, PictureListener pl) throws RemoteException {
			// TODO Auto-generated method stub
			if (pl == null) {
				Trace.Fatal("###takepicture pl == null");
				return;
			}
			CaptureModel cm = new CaptureModel();
			cm.setHeight(height);
			cm.setWidth(width);
			cm.setListener(pl);
			if (list == null) {
				list = new ArrayList<CaptureModel>();
				list.add(cm);
				mWidth = width;
				mHeight = height;
				takePictureOfTV(width, height);
			} else {
				list.add(cm);
			}
			if (width > mWidth | height > mHeight) {
				Trace.Fatal("### param too large width:" + width + "mwidth:" + mWidth + "height:" + height + "mHeight:"
						+ mHeight);
				if (pl != null) {
					pl.onChange(null);
				}
				return;
			}

		}
	}

	private synchronized void takePictureOfTV(int width, int height) {
		KKCommonManager.getInstance(getApplicationContext()).takePictureofTV(mWidth, mHeight,
				new TakePictureCallback() {

					@Override
					public void onPictureTaken(Bitmap arg0) {
						// TODO Auto-generated method stub
						if (list == null) {
							Trace.Fatal("###lhq list is null");
							return;
						}
						// if (list.size() == 0) {
						// Trace.Fatal("###lhq list is 0");
						// list = null;
						// return;
						// }
						int size = list.size();
						Trace.Info("###list size" + size);

						try {
							list.get(0).getListener().onChange(arg0);
							if (size == 1) {
								Trace.Info("###lhq only one");
								return;
							}
							for (int i = 1; i < size; i++) {
								CaptureModel cm = list.get(i);
								int height = cm.getHeight();
								int width = cm.getWidth();
								if (height != mHeight || width != mWidth) {
									Trace.Fatal("###lhq need process bitmap width:" + width + "mwidth:" + mWidth
											+ "height:" + height + "mHeight:" + mHeight);
									Bitmap result = processBitmap(width, height, arg0);
									cm.getListener().onChange(result);
								} else {
									Trace.Info("###lhq same as first one");
									cm.getListener().onChange(arg0);
								}
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							list = null;
							mWidth = -1;
							mHeight = -1;
						} finally {
							list = null;
							mWidth = -1;
							mHeight = -1;
						}
					}
				});
	}

	private Bitmap processBitmap(int newWidth, int newHeight, Bitmap bm) {
		try {
			float width = bm.getWidth();
			float height = bm.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			matrix.postScale(scaleWidth, scaleHeight);
			Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, (int) width, (int) height, matrix, true);
			return newBitmap;
		} catch (Exception e) {
			return bm;
		}
	}
	
	
	private static boolean su(String[] cmd) {
		boolean flag = false;
		try {
			Process p = Runtime.getRuntime().exec("su");
			InputStream outs = p.getInputStream();
			InputStreamReader isrout = new InputStreamReader(outs);
			BufferedReader brout = new BufferedReader(isrout);
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("echo test\n");
			os.flush();
			if (brout.readLine() != null) {
				for (int i = 0; i < cmd.length; i++) {
					os.writeBytes(cmd[i] + "\n");
				}
				os.flush();
				flag = true;
			} else {
				flag = false;
			}
			// p.destroy();
			outs.close();
			isrout.close();
			brout.close();
			os.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			flag = false;
		}
		return flag;

	}

}
