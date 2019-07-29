package com.li.demo;

import iapp.eric.utils.base.Trace;

import java.util.List;

import com.konka.advert.KKAdManager;
import com.konka.advert.data.AdInfo;
import com.konka.advert.data.GetAdInfoListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

public class PlayeActivity extends Activity{
	
	private ImageView iv;
	
	private String url;
	
	private Handler mHanlder = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0:
				iv.setImageBitmap(BitmapFactory.decodeFile(url));
				break;
			case 1:
				Intent i = getIntent();
				Uri uri = i.getData();
				String action = uri.getQueryParameter("action");
				String param = uri.getQueryParameter("param");
				Trace.Info("###action:"+action+"param:"+param);
				
		        Intent intent = new Intent();
		        intent.setData(Uri.parse(param));
		        intent.setPackage("com.cmcc.miguvideotv");
		        startActivity(intent);
		        finish();
				break;

			default:
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);
		iv = (ImageView) findViewById(R.id.iv);
		

		
		
		
		KKAdManager.getInstance().getAdInfo(3005102, new GetAdInfoListener() {
			
			@Override
			public void onResult(int arg0, List<AdInfo> data) {
				// TODO Auto-generated method stub
				Trace.Info("###lhq"+arg0+"###"+(data.size()));
				if(arg0 == 0){
					if(data != null && data.size() != 0){
						Trace.Info("###lhq"+data.get(0).getAdId());
						url = data.get(0).getSource();
						Trace.Info("###lhq"+data.get(0).getSource());
						mHanlder.sendEmptyMessage(0);
						mHanlder.sendEmptyMessageDelayed(1, 15000);
						
					}
				}
				
			}
		});
	}
	

	/**
	 * @Brief:
	 * @Author: lihuiqi
	 * @Version: V1.00 （版本号）
	 * @Create Date: 2018-5-14下午8:23:33
	 */
	
	
	

}
