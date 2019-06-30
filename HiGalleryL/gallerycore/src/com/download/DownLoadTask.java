package com.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.io.InputStream;
import java.lang.OutOfMemoryError;
import java.net.HttpURLConnection;
import java.net.URL;
import android.content.Context;
import android.os.Environment;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownLoadTask extends AsyncTask<String, Void, Void>{

    private static final String TAG = "DownLoadTask";
    private static FileOutputStream fops;
    private static InputStream inStream;
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_FAILED = 1;
    private Context ctx;

    private DownLoadListener listener;
    String filePath = "";

    public DownLoadTask(DownLoadListener listener, Context ctx){
        this.listener = listener;
        this.ctx =ctx;
    }

    @Override
    protected Void doInBackground(String... params) {
        String downLoadUrl = params[0];
        byte[] btImg = getImageFromNetByUrl(params[0]);
        if(null != btImg && btImg.length > 0){
            String fileName =downLoadUrl.substring(downLoadUrl.lastIndexOf("/") + 1);
            filePath = writeImageToDisk(btImg, fileName, ctx);
            listener.onLoadSuccess(filePath);
        }else{
            Log.i("DownLoadTask","no contents");
        }
        return null;
    }

    public String writeImageToDisk(byte[] img, String fileName, Context ctx){
        try {
            //filePath = getExternalCacheDir(ctx).getPath() + fileName;
            File file = new File (getExternalCacheDir(ctx),"/"+fileName);
            filePath = file.getPath();
            Log.i(TAG,"File.getPath();"+file.getPath());
            //File file = new File(filePath);
            fops = new FileOutputStream(file);
            fops.write(img);
            fops.flush();
            Log.i(TAG,"have writen into sdcard");
        }catch (FileNotFoundException e) {
            listener.onLoadFailed(filePath,e);
            Log.e(TAG,"in run FileNotFoundException");
        }catch (IOException e) {
            listener.onLoadFailed(filePath,e);
            Log.e(TAG,"in run IOException");
        }finally {
            try {
                fops.close();
            }catch (IOException e) {
                Log.e(TAG,"in run IOException");
            }
        }
        return filePath;
    }

    public byte[] getImageFromNetByUrl(String strUrl){
        try {
            URL url = new URL(strUrl);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            inStream = conn.getInputStream();
            byte[] btImg = readInputStream(inStream);
            return btImg;
        }catch (MalformedURLException e) {
            listener.onLoadFailed(filePath,e);
            Log.e(TAG,"in run",e);
        }catch (IOException e){
            listener.onLoadFailed(filePath,e);
            Log.e(TAG,"in run",e);
        }finally{
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }catch (IOException e) {
                Log.e(TAG,"in run IOException");
            }
        }
        return null;
    }

    public byte[] readInputStream(InputStream inStream) throws IOException{
        Log.i(TAG,"download readInputStream");
        int bufferSize = 1024;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[bufferSize];
            int len = 0;
            while( (len=inStream.read(buffer)) != -1 ){
                if(this.isCancelled())
                {
                  Log.i(TAG,"download canceled");
                  return null;
                }
                outStream.write(buffer, 0, len);
            }
        }catch (OutOfMemoryError e){
            Log.e(TAG,"OutOfMemory");
        } finally {
            inStream.close();
        }
        return outStream.toByteArray();
    }

    public static File getExternalCacheDir(Context context) {
        if (context == null){
            Log.e(TAG,"context is null");
            return null;
        }
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                //Log.e("Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Log.e("DownLoadTask","Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }
}
