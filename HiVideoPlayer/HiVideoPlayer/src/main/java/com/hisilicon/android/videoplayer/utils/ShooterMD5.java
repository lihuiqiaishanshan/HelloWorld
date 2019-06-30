package com.hisilicon.android.videoplayer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

public class ShooterMD5 {
    public static final String TAG = "ShooterMD5";

    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest1 = null;
        MessageDigest digest2 = null;
        MessageDigest digest3 = null;
        MessageDigest digest4 = null;

        FileInputStream in1 = null;
        FileInputStream in2 = null;
        FileInputStream in3 = null;
        FileInputStream in4 = null;
        byte buffer[] = new byte[4096];
        int len = -1;
        try {
            int offset1 = 0;
            int offset2 = 0;
            int offset3 = 0;
            int offset4 = 0;
            digest1 = MessageDigest.getInstance("MD5");
            in1 = new FileInputStream(file);
            int length = in1.available();
            LogTool.i(TAG, "length:" + length);
            offset1 = 4096;
            offset2 = length / 3 * 2;
            offset3 = length / 3;
            offset4 = length - 8192;
            if (in1 != null) {
                in1.skip(offset1);
                len = in1.read(buffer, 0, 4096);
            }
            if (len != -1)
                digest1.update(buffer, 0, len);

            digest2 = MessageDigest.getInstance("MD5");
            in2 = new FileInputStream(file);
            if (in2 != null) {
                in2.skip(offset2);
                len = in2.read(buffer, 0, 4096);
            }
            if (len != -1)
                digest2.update(buffer, 0, len);

            digest3 = MessageDigest.getInstance("MD5");
            in3 = new FileInputStream(file);
            if (in3 != null) {
                in3.skip(offset3);
                len = in3.read(buffer, 0, 4096);
            }
            if (len != -1)
                digest3.update(buffer, 0, len);

            digest4 = MessageDigest.getInstance("MD5");
            in4 = new FileInputStream(file);
            if (in4 != null) {
                in4.skip(offset4);
                len = in4.read(buffer, 0, 4096);
            }
            if (len != -1)
                digest4.update(buffer, 0, len);
        } catch (Exception e) {
            LogTool.e(e.toString());
            return null;
        } finally {
            try {
                if (in1 != null) in1.close();
            } catch (Exception ignore) {
            }
            try {
                if (in2 != null) in2.close();
            } catch (Exception ignore) {
            }
            try {
                if (in3 != null) in3.close();
            } catch (Exception ignore) {
            }
            try {
                if (in4 != null) in4.close();
            } catch (Exception ignore) {
            }
        }
        BigInteger bigInt1 = new BigInteger(1, digest1.digest());
        LogTool.i(TAG, "bigInt1.toString(16):" + bigInt1.toString(16));
        BigInteger bigInt2 = new BigInteger(1, digest2.digest());
        LogTool.i(TAG, "bigInt2.toString(16):" + bigInt2.toString(16));
        BigInteger bigInt3 = new BigInteger(1, digest3.digest());
        LogTool.i(TAG, "bigInt3.toString(16):" + bigInt3.toString(16));
        BigInteger bigInt4 = new BigInteger(1, digest4.digest());
        LogTool.i(TAG, "bigInt4.toString(16):" + bigInt4.toString(16));
        return bigInt1.toString(16) + ";" + bigInt2.toString(16) + ";" + bigInt3.toString(16) + ";" + bigInt4.toString(16);
    }
}
