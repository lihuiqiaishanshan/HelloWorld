package com.hisilicon.higallery.utils;

import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;

public class FileType {

    private static final String TAG = "BitmapDecodeUtils";

    public static boolean isGifImage(String filePath) {
        boolean result = false;
        String head = getFileHeader(filePath);
        if(null != head && head.equals("474946"))
            result = true;
        else
            result = false;
        return result;
    }

    public static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            int ret = is.read(b, 0, b.length);
            if(ret == -1){
                Log.e(TAG,"read the afterbody");
            }
            value = bytesToHexString(b);
        } catch (Exception e) {
            Log.e(TAG,"run in Exception");
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG,"in run",e);
                }
            }
        }
        Log.d("FileType" , "getFileHeader =" + value);
        return value;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }
}
