
package com.hisilicon.higallery.core;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

import android.os.SystemProperties;

import android.graphics.Matrix;


public class BitmapDecodeUtils {
    private static final String TAG = "BitmapDecodeUtils";
    public static final int WIDTH_2K = 1920;
    public static final int HEIGHT_2K = 1080;
    public static final int WIDTH_4K = 3840;
    public static final int HEIGTH_4K = 2160;

    private static int MAX_WIDTH = WIDTH_4K;
    private static int MAX_HEIHGT = HEIGTH_4K;

    private static GalleryCore mGallery;

    static final int TYPE_NORMAL_1k = 0;
    static final int TYPE_NORMAL_2k = 1;
    static final int TYPE_NORMAL_S4k = 2;
    static final int TYPE_NORMAL_4k = 3;
    static final int TYPE_NORMAL_8k = 4;



    public static Options getOptions(String imagePath) {
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false;
        return options;
    }

    public static Bitmap getOrigionBitmap(String imagePath) {
        Options options = getOptions(imagePath);

        int width = options.outWidth;
        int height = options.outHeight;
        if (width <= 0 || height <= 0) {
            return null;
        }
        int type = mGallery.getFormat();
        switch(type){
            case TYPE_NORMAL_1k:
                MAX_WIDTH = 1280;
                MAX_HEIHGT =720;
			break;
            case TYPE_NORMAL_2k:
				MAX_WIDTH = 1920;
                MAX_HEIHGT =1080;
            break;
			case TYPE_NORMAL_S4k:
				MAX_WIDTH = 3840;
				MAX_HEIHGT =2160;
            break;
			case TYPE_NORMAL_4k:
				MAX_WIDTH = 4096;
				MAX_HEIHGT =2160;
            break;
			case TYPE_NORMAL_8k:
				MAX_WIDTH = 7680;
				MAX_HEIHGT =4320;
            break;
			default:
				MAX_WIDTH = 3840;
				MAX_HEIHGT =2160;
			break;
        }

        int imageSide = Math.max(width, height);
        int maxSideLength = Math.max(MAX_WIDTH, MAX_HEIHGT);
        int simpleSize = 1;
        while (imageSide > maxSideLength) {
            simpleSize <<= 1;
        }
        options.inSampleSize = simpleSize;
        return loadBitmap(imagePath, options,mGallery);
    }

    public static Bitmap getThumBitmap(GalleryCore gallery, String imagePath, int width, int height) {
        mGallery = gallery;
        Options options = getOptions(imagePath);
        int simpleSize = 1;

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        if (imageWidth * imageHeight > 4000*4000)
            return null;
        if (imageWidth <= 0 || imageHeight <= 0 || width <= 0 || height <= 0) {
            return null;
        }
        if (imageWidth > width || imageHeight > height) {
            int widthRatio = imageWidth / width;
            int heightRatio = imageHeight / height;
            simpleSize = Math.min(widthRatio, heightRatio);
        }
        options.inSampleSize = simpleSize;

        return loadBitmap(imagePath, options,mGallery);
    }

    public static Bitmap getThumBitmap(Resources res, int resId, int width, int height) {
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inJustDecodeBounds = false;
        int simpleSize = 1;

        int imageWidth = options.outWidth;
        int imageHeight = options.outHeight;
        if (imageWidth <= 0 || imageHeight <= 0 || width <= 0 || height <= 0) {
            return null;
        }
        if (imageWidth > width || imageHeight > height) {
            int widthRatio = imageWidth / width;
            int heightRatio = imageHeight / height;
			simpleSize = Math.min(widthRatio, heightRatio);
        }
        options.inSampleSize = simpleSize;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(res, resId, options);
        } catch (OutOfMemoryError e) {
            Log.e(TAG, "decode resource OutOfMemoryError"+ resId);
        }
        return bitmap;
    }

    public static Bitmap loadBitmap(String path, Options options,GalleryCore gallery) {
        Bitmap bitmap = loadBitmap(path,options);
        if(null != bitmap){
            return getScaledBitmap(path,bitmap,mGallery);
        }
        return null;
    }

    public static Bitmap loadBitmap(String path, Options options) {
        Bitmap bitmap = null;
        if (null != path && !"".equals(path)) {
            int simpleSize = options.inSampleSize;
            while (true) {
                try {
                    options.inSampleSize = simpleSize;
                    bitmap = BitmapFactory.decodeFile(path, options);
                    break;
                } catch (OutOfMemoryError e) {
                    simpleSize *= 2;
                    Log.d(TAG, "decode bitmap OOM for" + path, e);
                }
            }
        }
        return bitmap;
    }
     public static Bitmap getScaledBitmap(String path,Bitmap bm,GalleryCore gallery) {
        int orientation = mGallery.getBitmapOrientation(path);
        int rotateDegree = 0;
        boolean mirror = false;

        switch(orientation) {
            case 8:
                rotateDegree += 270;
                break;
            case 7:
                mirror = true;
                rotateDegree += 270;
                break;
            case 6:
                rotateDegree += 90;
                break;
            case 5:
                mirror = true;
                rotateDegree += 90;
                break;
            case 4:
                mirror = true;
                rotateDegree += 180;
                break;
            case 3:
                rotateDegree += 180;
                break;
            case 2:
                mirror = true;
                break;
            default:
                break;
        }
        Bitmap bmRet = null;
        if (rotateDegree != 0 || mirror){
            Matrix matrix = new Matrix();
            matrix.setRotate(rotateDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            if(mirror){
                matrix.postScale(-1,1);
                matrix.postTranslate(bm.getWidth(),0);
            }
            bmRet = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        else{
            bmRet = bm;
        }
        return bmRet;
    }
}
