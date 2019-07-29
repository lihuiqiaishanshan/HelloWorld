package com.li.demo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;

public class CameraManager {
	private static CameraManager instance = null;
	
	private Context context;

	private CameraManager() {
		
	}

	public static CameraManager getInstance() {
		if (instance == null) {
			instance = new CameraManager();
		}
		return instance;
	}
	
	public void register(Context context,PictureCallBack p){
		this.context = context;
	}
	
	public void unRegister(){
		
	}
	
	public void takePictureOfTv(int width,int height){
		KKCommonManager.getInstance(context).takePictureofTV(width, height,new TakePictureCallback() {
			
			@Override
			public void onPictureTaken(Bitmap arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}

}
