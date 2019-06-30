package com.hisilicon.android.videoplayer.utils;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.content.Context;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.text.TextUtils;

import com.hisilicon.android.videoplayer.model.Common;

/**
 * Query subtitle information according to file hash and file name
 *
 * @author tobyPan
 */
public class ShooterHttpDownload {
    private static final String TAG = "ShooterHttpDownload";
    private DownloadResultListener mDownloadListener = null;
    private HttpDownloadThread mHttpDownloadThread = null;

    public ShooterHttpDownload(DownloadResultListener ls) {
        mDownloadListener = ls;
    }

    public void httpDownloadSubTitie(String url, String path, Context context) {
        if (context != null && !TextUtils.isEmpty(url) && !TextUtils.isEmpty(path)) {
            mHttpDownloadThread = new HttpDownloadThread(url, path, context);
            mHttpDownloadThread.start();
        }
    }

    public void stopDownloadSubTitie() {
        if (mHttpDownloadThread != null)
            mHttpDownloadThread.yield();
    }

    private class HttpDownloadThread extends Thread {
        private String mURL = "";
        private String mPath = "";
        private Context mContext = null;

        HttpDownloadThread(String url, String path, Context context) {
            this.mURL = url;
            this.mPath = path;
        }

        /**
         * Trust every server - don't check for any certificate
         */
        private void trustAllHosts() {
            final String TAG = "trustAllHosts";
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {

                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }

                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    LogTool.i(TAG, "checkClientTrusted");
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    LogTool.i(TAG, "checkServerTrusted");
                }
            }};

            // Install the all-trusting trust manager
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new java.security.SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                LogTool.e(e.toString());
            }
        }

        @Override
        public void run() {
            trustAllHosts();

            File file = new File(mPath);
            if (file.exists())
                file.delete();

            LogTool.i(TAG, "mURL:" + mURL);
            LogTool.i(TAG, "mPath:" + mPath);

            ThreadPolicy tp = ThreadPolicy.LAX;
            StrictMode.setThreadPolicy(tp);

            int bytesum = 0;
            int byteread = 0;

            URL url = null;
            try {
                url = new URL(mURL);
            } catch (MalformedURLException e) {
                LogTool.e(e.toString());
                return;
            }

            URLConnection conn = null;
            InputStream inStream = null;
            FileOutputStream fs = null;
            try {
                conn = url.openConnection();
                inStream = conn.getInputStream();
                if (!Common.isSecurePath(mPath)) {
                    LogTool.e(TAG, "mPath:Path Manipulation");
                    return;
                }
                fs = mContext.openFileOutput(mPath, Context.MODE_PRIVATE);

                byte[] buffer = new byte[1204];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    LogTool.i(TAG, "Downloaded:" + bytesum);
                    fs.write(buffer, 0, byteread);
                }
                return;
            } catch (FileNotFoundException e) {
                mPath = null;
                LogTool.e(e.toString());
                return;
            } catch (IOException e) {
                mPath = null;
                LogTool.e(e.toString());
                return;
            } finally {
                mDownloadListener.onDownloadResult(mContext.getFilesDir() + "/" + mPath);
                try {
                    if (fs != null) {
                        fs.close();
                        fs = null;
                    }
                } catch (IOException e) {
                    LogTool.e(e.toString());
                }
            }
        }
    }

    public static interface DownloadResultListener {
        void onDownloadResult(String path);
    }
}
