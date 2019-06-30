
package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.LogHelper;
import com.hisilicon.launcher.view.setting.SystemUpdateDialog.UpdateCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

/**
 * Enter the Dialog network upgrade
 *
 * @author wangchuanjian
 */
public class NetUpdate extends LinearLayout {
    private static final String TAG = "NetUpdate";
    private Handler mHandler;
    private Context mContext;
    // private LogicFactory mLogicFactory;
    // button of cancel
    private Button mSystemCancelBtn;
    // text of netUpdate
    private TextView mNetUpdateText;
    // seekBar of netUpdate
    private SeekBar mNetUpdateSeekBar;
    // DownloadManager object
    // private DownloadChangeObserver downloadObserver;
    // last download id
    private long mLastDownloadId = 0;
    private int fileSizeIdx = -1;
    // file path of server
    private static String serverFilePath = null;
    // file name
    private String fileName = "update.zip";
    private static final int SpaceIsNotEnough = 10002;
    private static final int SdcardNotExist = 10003;
    private DownloadTask downloadTask;
    private ConnectivityManager connectivityManager;
    private Handler mUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SpaceIsNotEnough:
                    Toast.makeText(mContext, R.string.sdcard_available_space_not_enough, Toast
                            .LENGTH_SHORT).show();
                    break;
                case SdcardNotExist:
                    Toast.makeText(mContext, R.string.sdcard_not_exist, Toast.LENGTH_SHORT).show();
                    Message message = mHandler.obtainMessage();
                    message.what = SystemUpdateDialog.DOWNLOAD_CANCEL;
                    mHandler.sendMessageDelayed(message, 10);
                    break;
            }
        }
    };

    public NetUpdate(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        registerNetBroadCast();
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.net_update, this);
        mNetUpdateSeekBar = (SeekBar) findViewById(R.id.net_update_seekbar);
        mNetUpdateText = (TextView) findViewById(R.id.net_update_text);
        mSystemCancelBtn = (Button) findViewById(R.id.net_update_cancel_btn);
        mSystemCancelBtn.requestFocus();
        mNetUpdateSeekBar.setProgress(0);
        mNetUpdateText.setText("" + 0 + "%");
        mSystemCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogHelper.v(TAG, "mSystemCancelBtn");
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DOWNLOAD_CANCEL;
                mHandler.sendMessageDelayed(message, 10);
            }
        });
        if (null == serverFilePath) {
            stopUpdate(false);
        }
        LogHelper.d(TAG, " serverFilePath = " + serverFilePath);
        boolean res = mkdir(Environment.getExternalStorageDirectory().getPath());
        if (res) {// Environment.DIRECTORY_DOWNLOADS)
            deleteFile();
            downloadTask = new DownloadTask();
            downloadTask.executeOnExecutor(Executors.newCachedThreadPool());;
        } else {
            LogHelper.w(TAG, "can't find sdcard");
        }

    }

    private void registerNetBroadCast() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            NetworkRequest request = new NetworkRequest.Builder().build();
            connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                connectivityManager.registerNetworkCallback(request, networkCallback);
            }
        }
    }

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager
            .NetworkCallback() {

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            LogHelper.w(TAG, "onLost");
            Message message = mHandler.obtainMessage();
            message.what = SystemUpdateDialog.DOWNLOAD_CANCEL;
            mHandler.sendMessageDelayed(message, 10);
        }
    };
    public static void setserverFilePath(String serverpath) {
        serverFilePath = serverpath;
        LogHelper.d(TAG, "serverFilePath:" + serverFilePath);
    }

    public final void stopUpdate(boolean success) {
        LogHelper.d(TAG, "stopUpdate:" + success);
        if (!success) {
            deleteFile();
            if (null != downloadTask) {
                downloadTask.cancel(true);
                downloadTask = null;
            }
        }
        unRegisterNetBroadCast();
    }

    public void unRegisterNetBroadCast() {
        if (connectivityManager != null && networkCallback != null && Build.VERSION.SDK_INT >= Build
                .VERSION_CODES.LOLLIPOP) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }
    private UpdateCallback mUpdateCallback = null;

    public void setUpdateCallback(UpdateCallback callback) {
        mUpdateCallback = callback;
    }

    private void deleteFile() {
        synchronized (SystemUpdateDialog.mDownloadLock) {
            File cancelFile = new File(Environment.getExternalStorageDirectory()
                    .getPath(), fileName);
            LogHelper.d(TAG, "delete file=:" + cancelFile + " Remove LastDownloadId :  " +
                    mLastDownloadId);
            if (cancelFile.exists()) {
                boolean deleteSucce = cancelFile.delete();
                LogHelper.v(TAG, "delete file =" + fileName + ":" + deleteSucce);
            }
        }

    }

    /**
     * Check the SD space is enough
     *
     * @param
     * @return
     */
    private boolean checkSDCard() {
        LogHelper.d(TAG, "totoal file =" + fileSizeIdx + "sdfreesize ="
                + getSDFreeSize());
        if (fileSizeIdx == -1)
            return false;
        if (fileSizeIdx >= getSDFreeSize()) {
            // available space not enough
            mUpdateHandler.sendEmptyMessage(SpaceIsNotEnough);
            return false;
        }

        return true;
    }

    /**
     * Gets the remaining space
     *
     * @return
     */
    public long getSDFreeSize() {
        // Get the SD card file path
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        // To obtain a single data block size (Byte)
        long blockSize = sf.getBlockSize();
        // The amount of free data blocks
        long freeBlocks = sf.getAvailableBlocks();
        // Returns the size of the SD card free
        return freeBlocks * blockSize - 10 * 1024 * 1024; // unit Byte leave 10M
    }

    /**
     * Create folder
     *
     * @param folderName
     * @return
     */
    private boolean mkdir(String folderName) {
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            // deleteFile();
            File folder = new File(folderName);
            return (folder.exists() && folder.isDirectory()) ? true : folder
                    .mkdirs();
        } else {
            // sd not exist
            mUpdateHandler.sendEmptyMessage(SdcardNotExist);
            return false;
        }

    }

    private class DownloadTask extends AsyncTask<Void, Integer, Boolean> {
        private final int CONNECT_TIMEOUT = 3000;
        private HttpURLConnection conn = null;
        InputStream input = null;
        OutputStream output = null;

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String downLoadFileName = "update.zip";
                URL url = new URL(serverFilePath);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Accept-Encoding", "identity");
                conn.setConnectTimeout(CONNECT_TIMEOUT);
                conn.connect();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return false;
                }
                fileSizeIdx = conn.getContentLength();
                LogHelper.d(TAG, "Size of download file:" + fileSizeIdx);
                // download the file
                boolean isSpaceEnough = checkSDCard();
                if (isSpaceEnough) {
                    // fileName changed to downLoadFileName because codedex rectifies
                    File updateFile = new File(Environment.getExternalStorageDirectory().getPath
                            (), downLoadFileName);
                    LogHelper.d(TAG, "updateFile path : " + updateFile.getAbsolutePath());
                    output = new FileOutputStream(updateFile);
                    input = conn.getInputStream();
                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            output.close();
                            input.close();
                            conn.disconnect();
                            return false;
                        }
                        total += count;
                        // publishing the progress....
                        if (fileSizeIdx > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileSizeIdx));
                        output.write(data, 0, count);
                    }
                    output.close();
                    input.close();
                    conn.disconnect();
                    return true;
                }
            } catch (Exception e) {
                LogHelper.e(TAG, e.getMessage());
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (isCancelled()) {
                return;
            }
            mNetUpdateSeekBar.setProgress(values[0]);
            mNetUpdateText.setText("" + values[0] + "%");
        }

        @Override
        protected void onPostExecute(Boolean isSuss) {
            if (isSuss) {
                mNetUpdateSeekBar.setProgress(100);
                mNetUpdateText.setText("" + 100 + "%");
                stopUpdate(true);
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DIALOG_GOTO_NEXT;
                mHandler.sendMessageDelayed(message, 100);
            } else {
                if (mUpdateCallback != null) {
                    mUpdateCallback.onDownloadFailed();
                }
            }
        }
    }

}
