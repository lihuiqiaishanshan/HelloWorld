package com.li.demo.ui;

import iapp.eric.utils.base.Trace;

import com.li.demo.R;
import com.li.demo.music.CoverListener;
import com.li.demo.music.MusicUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class PicTest extends Activity{
	
	private ImageView iv1 = null;
	
    //监听图片缩放
    private ScaleGestureDetector mScaleDetector;
    //监听图片移动
    private GestureDetector mGestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pictest);
		iv1 = (ImageView) findViewById(R.id.iv1);
//		Bitmap bg = BitmapFactory.decodeResource(getResources(),
//				R.drawable.test);
//		MusicUtils.getCover("mnt/sdcard/1.mp3", 500,500, new CoverListener() {
//			
//			@Override
//			public void onSucess(String url, Bitmap cover, Bitmap bg) {
//				// TODO Auto-generated method stub
//				Trace.Info("###lhq cover:" + (cover == null));
//				iv1.setImageBitmap(cover);
//				
//			}
//		});
		
		
		mScaleDetector = new ScaleGestureDetector(this, new SimpleScaleListenerImpl());
		mGestureDetector = new GestureDetector(this, new SimpleGestureListenerImpl());
		
	}
	
	
	private boolean isMove = false;
	
	private float mScale = 1.0f;
	
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Trace.Info("###ontouch");
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_POINTER_DOWN:
			Trace.Info("###lhq down count:" + event.getPointerCount());
			if(event.getPointerCount() == 2){
				isMove = false;
			}
			
			break;
			
		case MotionEvent.ACTION_POINTER_UP:
			
			Trace.Info("###lhq up count:" + event.getPointerCount());
			
			if(event.getPointerCount() == 2){
				isMove = true;
			}
			
			break;

		default:
			break;
		}
        //双指缩放
        mScaleDetector.onTouchEvent(event);
        //单指移动
        mGestureDetector.onTouchEvent(event);
 
        return true;
	}



	 //缩放
    private class SimpleScaleListenerImpl extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scale = detector.getScaleFactor();
            //缩放倍数范围：0.3～3
            Trace.Info("###lhq scale:"+ scale);
            mScale*=scale;
            if(mScale < 1.0f){
            	mScale = 1.0f;
            }
            Trace.Info("###lhq scale1:"+ mScale);
 
            return true;
        }
    }
    
    
	 //平移
    private class SimpleGestureListenerImpl extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	if(!isMove){
        		return true;
        	}
        	
        	
        	Trace.Info("###lhq x:"+ distanceX);
        	
        	Trace.Info("###lhq y:"+ distanceY);
           
 
            return true;
        }
    }
    
    


	



}
