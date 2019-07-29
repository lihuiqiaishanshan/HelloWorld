package com.li.demo;

import iapp.eric.utils.base.Trace;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageUtils {
	public static Bitmap downloadImg(String url) {
		Bitmap bitmap = null;
		try {
			URL u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
			conn.disconnect();
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean saveBitmap(Bitmap bitmap, String savePath) {
		try {
			isExist(savePath);
			File file = new File(savePath);//
			savePic(bitmap, file);

			return true;
		} catch (IOException e) {
			return false;
		}

	}
	
	private static void isExist(String path) {
		File file = new File(path);

		// 如果目标文件所在的目录不存在，则创建父目录
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdirs()) {
			}
		}

		// 判断文件夹是否存在,如果不存在则创建文件夹
		if (file.exists()) {
			// file.mkdir();
			file.delete();
		}

		try {
			file.createNewFile();
		} catch (IOException e) {
			Trace.Debug("###createNewFile, Exception = " + e);
		}

	}
	
	private static void savePic(Bitmap bmp, File file) throws IOException {
		// File file = new File(path);
		FileOutputStream oStream = new FileOutputStream(file);
		bmp.compress(Bitmap.CompressFormat.JPEG, 90, oStream); //
		oStream.flush();
		oStream.close();
	}
}
