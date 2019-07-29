package com.li.demo.music;

import iapp.eric.utils.custom.model.APIC;
import iapp.eric.utils.metadata.Mp3;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

public class MusicUtils {
	
	/**
	 * 柔化效果(高斯模糊)(优化后比上面快三倍)
	 * 
	 * @param bmp
	 * @return
	 */
	public static Bitmap blurImageAmeliorate(Bitmap bmp) {
		long start = System.currentTimeMillis();
		// 高斯矩阵
		int[] gauss = new int[] { 1, 2, 1, 2, 4, 2, 1, 2, 1 };

		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;

		int pixColor = 0;

		int newR = 0;
		int newG = 0;
		int newB = 0;

		int delta = 30; // 值越小图片会越亮，越大则越暗

		int idx = 0;
		int[] pixels = new int[width * height];
		bmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int i = 1, length = height - 1; i < length; i++) {
			for (int k = 1, len = width - 1; k < len; k++) {
				idx = 0;
				for (int m = -1; m <= 1; m++) {
					for (int n = -1; n <= 1; n++) {
						pixColor = pixels[(i + m) * width + k + n];
						pixR = Color.red(pixColor);
						pixG = Color.green(pixColor);
						pixB = Color.blue(pixColor);

						newR = newR + (int) (pixR * gauss[idx]);
						newG = newG + (int) (pixG * gauss[idx]);
						newB = newB + (int) (pixB * gauss[idx]);
						idx++;
					}
				}

				newR /= delta;
				newG /= delta;
				newB /= delta;

				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));

				pixels[i * width + k] = Color.argb(255, newR, newG, newB);

				newR = 0;
				newG = 0;
				newB = 0;
			}
		}

		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		long end = System.currentTimeMillis();
		Log.d("may", "used time=" + (end - start));
		return bitmap;
	}
	
	
	private static Handler mhandler = new Handler(){
		
		
	};
	
	
	public static void getCover(final String url ,final int w, final int h,final CoverListener listener){
		if(TextUtils.isEmpty(url)){
			listener.onSucess(url,null,null);
			return;
		}

		mhandler.post((new Runnable() {
			@Override
			public void run() {

				Mp3 m = null;
				try {
					m = new Mp3(url, false, true);
				} catch (Exception e) {
					e.printStackTrace();
					listener.onSucess(url,null,null);
					return;
				}

				if (m != null && null != m.getTagID3V2()){

					if (null != m.getTagID3V2().getTagFrame().get("APIC")) {
						APIC apic = (APIC) (m.getTagID3V2().getTagFrame()
								.get("APIC").getContent());
						if(apic != null && apic.pictureData != null){
							final Bitmap bm = BitmapFactory.decodeByteArray(apic.pictureData,
									0, apic.pictureData.length);
							int width = bm.getWidth();
							int height = bm.getHeight();
							Matrix matrix = new Matrix();
							float scaleWidth = ((float) w) / width;
							float scaleHeight = ((float) h) / height;
							matrix.postScale(scaleWidth, scaleHeight);
							Bitmap newBitmap = Bitmap.createBitmap(bm, 0, 0, (int) width,
									(int) height, matrix, true);
									listener.onSucess(url,newBitmap,null);
							return;

						}

					}

				}
				listener.onSucess(url,null,null);
			}
		}));






	}
}









