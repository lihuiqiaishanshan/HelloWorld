package com.li.demo.ui;

import java.util.List;
import java.util.Map;

import iapp.eric.utils.base.Trace;

import com.li.demo.R;
import com.li.demo.model.Video;
import com.li.demo.rockitv.JsonListen;
import com.li.demo.rockitv.RockiData;
import com.li.demo.rockitv.RockiTvJson;
import com.li.demo.rockitv.RockiVideo;
import com.li.demo.rockitv.RockiVideoListener;
import com.li.demo.rockitv.VideoModel;
import com.li.demo.rockitv.VideoNode;
import com.li.demo.rockitv.rockiBindListener;
import com.rockitv.android.IRemote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

/**
 * 
 * Created on: 2014-7-4
 * 
 * @brief test video info from adot
 * @author Li Huiqi
 * @date Lastest modified on: 2014-7-4
 * @version V1.0.0.00
 * 
 */

public class AdotInfoTest extends Activity {

	private IRemote remote = null;

	private VideoModel video = null;

	private Button button1 = null;

	private String title = "";
	private String url = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.adot_test);
		button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// String url =
				// "http://v.youku.com/v_show/id_XMjA2MzczNTA0.html";
				// Intent intent = new Intent();
				// intent.setClassName("com.rockitv.android",
				// "com.rockitv.android.ui.AdotPlayer");
				// intent.setData(Uri.parse(url));
				// startActivity(intent);
				
//				finish();
//				//测试优酷播放
//				Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse("ykew://detail?showid=cc04251a962411de83b1&cats=电影"));
//				startActivity(i);
			}
		});
		
		Trace.Info("onclick");
		String url = "http://v.youku.com/v_show/id_XNDQ0MTQ1Mzk2.html";
		String url1 = "http://v.youku.com/v_show/id_XMTE5NTU1NzA4.html";
		String url2 = "http://www.letv.com/ptv/vplay/1624322.html";
//		RockiVideo.getInstance().playVideo(AdotInfoTest.this, url1);

		RockiVideo.getInstance().bindService(getApplicationContext(), new rockiBindListener() {

			@Override
			public void onBind() {
				// TODO Auto-generated method stub
				RockiVideo.getInstance().getVideoList(getApplicationContext(), "cctv10", new RockiVideoListener() {

					@Override
					public void onVideo(List<Video> list) {
						// TODO Auto-generated method stub
						// 先判断list是否为空 为空则绑定失败
						 for (Video v : list) {
						 Trace.Info("###" + v.getName());
						 Trace.Info("###" + v.getPicurl());
						 Trace.Info("###" + v.getSummary());
						 Trace.Info("###" + v.getUrl());
						 }
					}
				});
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		RockiVideo.getInstance().unBindService(getApplicationContext());
		super.onDestroy();
	}
}
