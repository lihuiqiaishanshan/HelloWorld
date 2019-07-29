package com.li.demo.test;

import java.util.ArrayList;
import com.konka.android.tv.KKCommonManager;
import com.konka.android.tv.common.KKTVCamera.TakePictureCallback;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

public class CaptureManager {

	private static final String TAG = "CaptureManager";

	private static CaptureManager instance = null;

	private java.util.List<CaptureModel> list = null;

	private int mWidth = -1;
	private int mHeight = -1;

	private CaptureManager() {

	}

	public static CaptureManager getInstance() {
		if (instance == null) {
			instance = new CaptureManager();
		}
		return instance;
	}

	public void takePicture(Context context, int width, int height, PictureCallback pc) {
		if (pc == null) {
			Log.e(TAG,"###takepicture pl == null");
			return;
		}
		CaptureModel cm = new CaptureModel();
		cm.setHeight(height);
		cm.setWidth(width);
		cm.setListener(pc);
		if (list == null) {
			list = new ArrayList<CaptureModel>();
			list.add(cm);
			mWidth = width;
			mHeight = height;
			takePictureOfTV(context, width, height);
		} else {
			list.add(cm);
		}
		if (width > mWidth | height > mHeight) {
			Log.e(TAG,"### param too large width:" + width + "mwidth:" + mWidth + "height:" + height + "mHeight:" + mHeight);
			if (pc != null) {
				pc.onPicture(null);
			}
			return;
		}
	}

	public interface PictureCallback {
		void onPicture(Bitmap bitmap);
	}

	private void takePictureOfTV(Context context, int width, int height) {
		KKCommonManager.getInstance(context).takePictureofTV(mWidth, mHeight, new TakePictureCallback() {

			@Override
			public void onPictureTaken(Bitmap arg0) {
				// TODO Auto-generated method stub
				if (list == null) {
					Log.e(TAG,"###lhq list is null");
					return;
				}
				if (list.size() == 0) {
					Log.e(TAG,"###lhq list is 0");
					list = null;
					list = null;
					mWidth = -1;
					mHeight = -1;
					return;
				}
				int size = list.size();
				Log.e(TAG,"###list size" + size);

				list.get(0).getListener().onPicture(arg0);
				if (size == 1) {
					Log.e(TAG,"###lhq only one");
					list = null;
					mWidth = -1;
					mHeight = -1;
					return;
				}
				for (int i = 1; i < size; i++) {
					CaptureModel cm = list.get(i);
					int height = cm.getHeight();
					int width = cm.getWidth();
					if (height != mHeight || width != mWidth) {
						Log.e(TAG,"###lhq need process bitmap width:" + width + "mwidth:" + mWidth + "height:" + height + "mHeight:" + mHeight);
						Bitmap result = processBitmap(width, height, arg0);
						cm.getListener().onPicture(result);
					} else {
						Log.e(TAG,"###lhq same as first one");
						cm.getListener().onPicture(arg0);
					}
				}
				list = null;
				mWidth = -1;
				mHeight = -1;
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

}
