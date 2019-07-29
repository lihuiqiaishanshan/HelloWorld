package com.li.demo.ui;

import iapp.eric.utils.base.Trace;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.li.demo.R;
import com.li.demo.Utils;

@SuppressLint("NewApi")
public class ImageViewActivity extends Activity {
	private ImageView iv;
	private MyView mView;
	private Bitmap bm;
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				iv.setImageBitmap(bm);
				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.imageview_activity);
		initView();
		mView.initText(getWindowManager());
		Intent mm = new Intent("konka.action.START_MM"); 
		startActivity(mm);
		mView.startScroll();
		float pxValue = 20f;
		float fontScale = getApplicationContext().getResources()
				.getDisplayMetrics().scaledDensity;
		Trace.Info("fontScale的值" + fontScale);
		float sp = pxValue / fontScale + 0.5f;
		Trace.Info("sp的值" + sp);
		String path = Utils.getExternalStorage(getApplicationContext()).get(0).getPath()+"/zhuanji.jpg";
		Bitmap zhuanji = BitmapFactory.decodeFile(path);
//		int newWidth = Utils.dip2px(getApplicationContext(), 174);
//		int newHeight = Utils.dip2px(getApplicationContext(), 174);
//		Bitmap newBitmap = null;
//		if (null != zhuanji) {
//			float width = zhuanji.getWidth();
//			float height = zhuanji.getHeight();
//			Matrix matrix = new Matrix();
//			float scaleWidth = ((float) newWidth) / width;
//			float scaleHeight = ((float) newHeight) / height;
//			matrix.postScale(scaleWidth, scaleHeight);
//			newBitmap = Bitmap.createBitmap(zhuanji, 0, 0, (int) width,
//					(int) height, matrix, true);
//		}
		Bitmap newBitmap = zhuanji;
		int left = Utils.dip2px(getApplicationContext(), 64);
		int top = Utils.dip2px(getApplicationContext(), 63);
		if (null != newBitmap) {
			Trace.Info("专辑图片bitmap不为空");
			Bitmap bg = BitmapFactory.decodeResource(getResources(),
					R.drawable.zhuanji_out);
			Trace.Info("bgwidth-->"+bg.getWidth());
			Trace.Info("bgheight-->"+bg.getHeight());
			bm = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(),
					Config.ARGB_8888);
			bm.setDensity(240);
			Log.e("qhc","bm  density"+bm.getDensity());
			Canvas canvas = new Canvas(bm);
			Log.e("qhc","newBitmap"+newBitmap.getDensity());
			newBitmap.setDensity(240);
			Log.e("qhc","bg"+bg.getDensity());
			
			Log.e("qhc","densitydpi"+getApplicationContext().getResources()
						.getDisplayMetrics().densityDpi);
			Log.e("qhc","density"+getApplicationContext().getResources()
					.getDisplayMetrics().density);
			canvas.drawBitmap(newBitmap, left, top, null);
			canvas.drawBitmap(bg, 0, 0, null);
			Trace.Info("width-->"+bm.getWidth());
			Trace.Info("height-->"+bm.getHeight());
			iv.setImageBitmap(bm);
		}
//		LinearLayout l1 = (LinearLayout) findViewById(R.id.main);
//		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(451, 
//                ViewGroup.LayoutParams.WRAP_CONTENT);
//		TextView tv1 = new TextView(this); 
//		tv1.setLayoutParams(lp);
//		tv1.setText("呵呵第三轮福建师大李开复收到了附近收到了附近收到了附近上的浪费吉林省地方上的浪费螺丝钉发牢骚的杰弗里斯克的飞机螺丝钉机发牢骚大家宽松的空间上的上的浪费机上的浪费记录锁定发牢骚的苏打绿上的浪费");
//		l1.addView(tv1);
		
		

		// BitmapFactory.decodeResource(getResources(), R.id.iv, opts);
		// new Thread(new MyImageThread()).start();

	}

	private void initView() {
		iv = (ImageView) findViewById(R.id.iv);
		mView = (MyView) findViewById(R.id.textView);

	}

	class MyImageThread implements Runnable {

		@Override
		public void run() {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			bm = BitmapFactory.decodeResource(getResources(),
					R.drawable.ic_launcher, opts);
			mHandler.sendEmptyMessage(0);

		}
	}

}
