package com.li.demo;

//<Wonkatek Software> 
////****************************************************************************** 
// Wonkatek Software 
// Copyright (c) 2010 - 2012 Shenzhen Wonkatek Network Co., LTD. All rights reserved. 
// All software, firmware and related documentation herein ("Wonkatek Software") are 
// intellectual property of Shenzhen Wonkatek Network Co., LTD. ("Wonkatek") and protected by 
// law, including, but not limited to, copyright law and international treaties. 
// Any use, modification, reproduction, retransmission, or republication of all 
// or part of Wonkatek Software is expressly prohibited, unless prior written 
// permission has been granted by Wonkatek. 
//****************************************************************************** 
//<Wonkatek Software>

import com.konka.android.util.LogPrint;
import com.mstar.android.camera.MCamera;
import com.mstar.android.tvapi.common.TvManager;
import com.mstar.android.tvapi.common.exception.TvCommonException;
import com.mstar.android.tvapi.common.vo.PanelProperty;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Handler;
import com.konka.android.tv.KKCommonManager.EN_KK_CAPTURE_MODE;

/**
 * the camera which is used to take photo for TV
 * 
 * @author crabby.wang
 * @version V1.0
 * 
 */
public class KKTVCamera implements PictureCallback {

	private final String TAG = "KKTVCamera";
	private int mPictureWidth;
	private int mPictureHeight;
	private android.hardware.Camera mCameraDevice;
	private TakePictureCallback takePictureCallback;
	private boolean isReady;
	private Handler mHandler;

	public KKTVCamera() {
		mPictureWidth = 0;
		mPictureHeight = 0;
		mCameraDevice = null;
		takePictureCallback = null;
		isReady = true;
	}

	/**
	 * 拍照
	 * 
	 * @param width
	 *            照片的宽度
	 * @param height
	 *            照片的高度
	 * @return true 执行成功；false 执行失败
	 * @hide
	 */
    public boolean takePicture(int width, int height, TakePictureCallback callback) {
        stop();
        mPictureWidth = width;
        mPictureHeight = height;
        if (mPictureWidth > 1920) {
            mPictureWidth = 1920;
        }
        if (mPictureHeight > 1080) {
            mPictureHeight = 1080;
        }
        takePictureCallback = callback;
        PanelProperty mstarProperty = null;
        try {
            mstarProperty = TvManager.getInstance().getPictureManager().getPanelWidthHeight();
        } catch (TvCommonException e) {
            LogPrint.error(TAG,
                    "get an exceptiong when executing the function getPanelWidthHeight.");
            e.printStackTrace();
        }
        int cameraId = 7;
        if (mstarProperty != null) {
            if (mstarProperty.width > 1920) {
                cameraId = 6;
            }
        }
        System.out.println("mWidth=" + mPictureWidth + " mHeight=" + mPictureHeight);
        if (safeCameraOpen(cameraId)) {
            Parameters parameters = mCameraDevice.getParameters();
            parameters.setPictureSize(mPictureWidth, mPictureHeight);
            parameters.setPreviewSize(mPictureWidth, mPictureHeight);
            parameters.set("traveling-res", MCamera.Parameters.E_TRAVELING_RES_1920_1080);
            parameters.set("traveling-mode", MCamera.Parameters.E_TRAVELING_ALL_VIDEO);
            parameters.set("traveling-mem-format",
                    MCamera.Parameters.E_TRAVELING_MEM_FORMAT_YUV422_YUYV);
            parameters.set("traveling-frame-rate", 0);
            safeTakePicture(parameters);
            return true;
        } else {
            return false;
        }

    }
    
    
    private void safeTakePicture(Parameters parameters) {
        mCameraDevice.setParameters(parameters);
        try {
            mCameraDevice.startPreview();
            mCameraDevice.takePicture(null, null, this);
        } catch (Exception e) {
            tellListenerHasError();
            stop();
            LogPrint.debug(TAG, "take picture fail");
        }
    }

    
    
    private void tellListenerHasError() {
        if (null != takePictureCallback) {
            takePictureCallback.onPictureTaken(null);
        }
    }
	/**
	 * 拍照
	 * 
	 * @param width
	 *            照片的宽度
	 * @param height
	 *            照片的高度
	 * @param mode
	 *            截图模式
	 * @return true 执行成功；false 执行失败
	 * @hide
	 */
	public boolean takePicture(int width, int height,
			TakePictureCallback callback, EN_KK_CAPTURE_MODE mode) {
		if (!isReady) {
			LogPrint.debug(TAG, "the camera is not ready!!!");
			return false;
		}
		mPictureWidth = width;
		mPictureHeight = height;
		if (mPictureWidth > 1920) {
			mPictureWidth = 1920;
		}
		if (mPictureHeight > 1080) {
			mPictureHeight = 1080;
		}
		takePictureCallback = callback;

		mCameraDevice = android.hardware.Camera.open(6);
		System.out.println("mWidth=" + mPictureWidth + " mHeight="
				+ mPictureHeight);
		if (mCameraDevice != null) {
			isReady = false;
			// 先移除上一次的forceStop动作，否则在快速连续按截图时会出错。
			if (null == mHandler) {
				mHandler = new Handler();
			}
			mHandler.removeCallbacks(rForceStop);
			Parameters parameters = mCameraDevice.getParameters();
			parameters.setPictureSize(mPictureWidth, mPictureHeight);
			parameters.setPreviewSize(mPictureWidth, mPictureHeight);
			parameters.set("traveling-res",
					MCamera.Parameters.E_TRAVELING_RES_1920_1080);
			if (mode == EN_KK_CAPTURE_MODE.CURRENT_ALL) {
				parameters.set("traveling-mode",
						MCamera.Parameters.E_TRAVELING_ALL_VIDEO_WITH_OSD);
			} else if (mode == EN_KK_CAPTURE_MODE.CURRENT_VIDEO) {
				parameters
						.set("traveling-mode",
								MCamera.Parameters.E_TRAVELING_1ST_VIDEO_PROGRESSIVE_AUTO);
			} else if (mode == EN_KK_CAPTURE_MODE.ORIGINAL_VIDEO) {
				parameters.set("traveling-mode",
						MCamera.Parameters.E_TRAVELING_ALL_VIDEO);
			}
			parameters.set("traveling-mem-format",
					MCamera.Parameters.E_TRAVELING_MEM_FORMAT_YUV422_YUYV);
			parameters.set("traveling-speed",
					MCamera.Parameters.E_TRAVELING_SPEED_FAST);
			mCameraDevice.setParameters(parameters);
			mCameraDevice.startPreview();
			mCameraDevice.takePicture(null, null, this);
			mHandler.postDelayed(rForceStop, 5000);
			return true;
		} else {
			return false;
		}

	}
	
    private boolean safeCameraOpen(int id) {
        stop();
        try {
            mCameraDevice = android.hardware.Camera.open(id);
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            tellListenerHasError();
            LogPrint.debug(TAG, "open camera fail");
            e.printStackTrace();
        }
        return null != mCameraDevice;
    }

	Runnable rForceStop = new Runnable() {
		public void run() {
			if (!isReady) {
				LogPrint.debug("Time out for taking picture!!! force stopping!!!");
				stop();
			}
		}
	};

	/**
	 * 截屏完成后的清理工作，必须被调用
	 */
	private void stop() {
		if (mCameraDevice != null) {
			mCameraDevice.stopPreview();
			mCameraDevice.release();
			isReady = true;
		}
	}

	/**
	 * 拍照回调接口
	 * 
	 * @author crabby.wang
	 * 
	 */
	public interface TakePictureCallback {
		/**
		 * 当所抓拍的照片可用时会出发此回调函数。 照片以bitmap格式保存。
		 * 
		 * @param bitmap
		 *            照片
		 */
		void onPictureTaken(byte[] data);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		takePictureCallback.onPictureTaken(data);
		stop();
	}
}
