package com.li.demo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {
    public static Bitmap getBitmapFromFile(String filePath, int width, int height) {

        BitmapFactory.Options opts = null;
        if (width > 0 && height > 0) {
            opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, opts);
            // 计算图片缩放比例
            final int minSideLength = Math.min(width, height);
            opts.inSampleSize = computeSampleSize(opts, minSideLength, width * height);
            opts.inJustDecodeBounds = false;
            opts.inInputShareable = true;
            opts.inPurgeable = true;
        }
        try {
            return BitmapFactory.decodeFile(filePath, opts);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);

        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }

        return roundedSize;
    }
    
    public static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }
        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
    
	public static void recycleBitmap(Bitmap bmp){
		if (bmp != null && !bmp.isRecycled()) {
			bmp.recycle();
			bmp = null;
			System.gc();
		}
	}
	
	public static void savePic(byte [] data, File file) throws IOException {
		// File file = new File(path);
		FileOutputStream oStream = new FileOutputStream(file);
		oStream.write(data);
		oStream.flush();
		oStream.close();
	}
}
