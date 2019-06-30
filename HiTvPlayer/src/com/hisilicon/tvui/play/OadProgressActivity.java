package com.hisilicon.tvui.play;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.RecoverySystem;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.message.DTVMessage;
import com.hisilicon.dtv.message.IDTVListener;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.TaskUtil;
import com.hisilicon.tvui.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class OadProgressActivity extends Activity {
    private static final String DATA_PATH = "/data/vendor/dtvdata/update.zip";
    private static final int PERCENT_100 = 100;
    private static final int PERCENT_99 = 99;
    private DTV mDtv = null;
    private TextView mtxtVwPercent = null;
    private ProgressBar mProgressBar = null;
    private Context mContext = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        LogTool.d(LogTool.MSCAN, "===== onCreate =====");
        getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY);
        getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY);
        getWindow().addPrivateFlags(Util.HI_PRIVATE_FLAG_INTERCEPT_FUNC_KEY);
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.oad_progress);
        initCtrl();
    }

    private void initCtrl() {
        mDtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);

        mtxtVwPercent = (TextView) findViewById(R.id.id_oad_percentnum);
        mtxtVwPercent.setText("0%");
        mProgressBar = (ProgressBar) findViewById(R.id.id_oad_percent);
    }

    @Override
    public void onResume() {
        LogTool.i(LogTool.MSCAN, "===== onResume =====");
        super.onResume();
        mDtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_SCHEDULE, gOadListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_FINISH, gOadListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_ERROR, gOadListener, 0);
        mDtv.subScribeEvent(DTVMessage.HI_SVR_EVT_SSU_TIMEOUT, gOadListener, 0);
        mDtv.getOTA().startDownloadFile(DATA_PATH);
    }

    @Override
    public void onPause() {
        LogTool.i(LogTool.MSCAN, "===== onPause =====");
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_SCHEDULE, gOadListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_FINISH, gOadListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_ERROR, gOadListener);
        mDtv.unSubScribeEvent(DTVMessage.HI_SVR_EVT_SSU_TIMEOUT, gOadListener);
        LogTool.d(LogTool.MSCAN, "unSubScribeEvent ");

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LogTool.i(LogTool.MSCAN, "===== onDestroy =====");
        super.onDestroy();
    }

    IDTVListener gOadListener = new IDTVListener() {
        @Override
        public void notifyMessage(int messageID, int param1, int parm2, Object obj) {
            LogTool.d(LogTool.MSCAN, " messageID = " + messageID);
            switch (messageID) {
                case DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_SCHEDULE: {
                    int percent = param1 % PERCENT_100;
                    if ((0 == percent) && (param1 > PERCENT_99)) {
                        percent = PERCENT_100;
                    }
                    mProgressBar.setProgress(percent);
                    mtxtVwPercent.setText(percent + "%");
                    LogTool.d(LogTool.MSCAN, " notifyMessage SCHEDULE param1=" + param1);
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_FINISH: {
                    mDtv.getOTA().stopDownloadFile();
                    mProgressBar.setProgress(PERCENT_100);
                    mtxtVwPercent.setText(PERCENT_100 + "%");
                    new CopyFileTask().execute();
                    break;
                }
                case DTVMessage.HI_SVR_EVT_SSU_DOWNLOAD_ERROR:
                    LogTool.d(LogTool.MSCAN, "notifyMessage download failed");
                    mDtv.getOTA().stopDownloadFile();
                    File file = new File(DATA_PATH);
                    if (file.exists()) {
                        file.delete();
                    }
                    finish();
                    break;
                case DTVMessage.HI_SVR_EVT_SSU_TIMEOUT: {
                    LogTool.d(LogTool.MSCAN, " ssu timeOut");
                    Toast.makeText(OadProgressActivity.this,"SSU Download Data Timeout",Toast.LENGTH_LONG).show();
                    finish();
                    break;
                    }
                default:
                    break;
            }
        }
    };

    private class CopyFileTask extends AsyncTask<Void, Integer, Boolean> {
        private File oldFile;
        private File updateFile;

        private CopyFileTask() {
            LogTool.d(LogTool.MSCAN, "CopyFileTask start");
            oldFile = new File(DATA_PATH);
            updateFile = new File(Environment.getExternalStorageDirectory().getPath(), "update.zip");
            LogTool.d(LogTool.MSCAN, "updateFile path = " + updateFile.getAbsolutePath());
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            LogTool.d(LogTool.MSCAN, "doInBackground start");
            InputStream inStream = null;
            FileOutputStream outputStream = null;
            try {
                if (oldFile.exists()) {
                    LogTool.d(LogTool.MSCAN, "file exists");
                    inStream = new FileInputStream(oldFile);
                    outputStream = new FileOutputStream(updateFile);
                    byte[] buffer = new byte[4096];
                    int byteread = 0;
                    while ((byteread = inStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, byteread);
                    }
                    return true;
                }
            } catch (Exception e) {
                LogTool.d(LogTool.MSCAN, "copy file error : " + e.getLocalizedMessage());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                return false;
            } finally {
                try {
                    if (inStream != null) {
                        inStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            LogTool.d(LogTool.MSCAN, "onPostExecute aBoolean = " + aBoolean);
            if (oldFile.exists()) {
                oldFile.delete();
            }
            if (aBoolean) {
                localUpdate(updateFile);
            }
        }
    }

    private void localUpdate(File file) {
        LogTool.d(LogTool.MSCAN, "directly go to installPackage, and path = " + file.getAbsolutePath());
        TaskUtil.post(new Runnable() {
            @Override
            public void run() {
                try {
                    RecoverySystem.installPackage(mContext, file);
                } catch (IOException e) {
                    LogTool.e(LogTool.MSCAN, "install pkg error.");
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        LogTool.v(LogTool.MSCAN, "onKeyDown keycode =" + event.getKeyCode());
        return true;
    }
}
