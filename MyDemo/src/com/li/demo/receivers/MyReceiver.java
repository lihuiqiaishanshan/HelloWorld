package com.li.demo.receivers;

import com.li.demo.service.MainService;

import iapp.eric.utils.base.Trace;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.view.KeyEvent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
		if (action.equals("com.li.demo.alarm")) {
			Trace.Info("###收到闹钟广播");
			Toast.makeText(context, "stop", Toast.LENGTH_LONG).show();
			Intent i = new Intent(context, MainService.class);
			context.stopService(i);
		} else if (action.equals("com.konka.tv.hotkey.service.MUTE")) {
			Trace.Info("#######com.konka.tv.hotkey.service.MUTE");
			Intent i = new Intent(context, MainService.class);
			i.putExtra("cmd", "docibn");
			context.startService(i);

//			new Thread() { // 不可在主线程中调用
//				public void run() {
//					try {
//						Instrumentation inst = new Instrumentation();
//						inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//
//			}.start();

		}else if(action.equals("com.konka.livelauncher.login_sucess")){
			Trace.Info("###lhq com.konka.livelauncher.login_sucess");
			
		}

	}

}
