
package com.hisilicon.launcher.view.setting;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.hisilicon.launcher.util.LogHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.RecoverySystem;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.hal.halApi;
import com.hisilicon.launcher.util.TaskUtil;

/**
 * Enter the local update, detects the update package Dialog, temporarily not
 * used
 *
 * @author wangchuanjian
 */
public class LocalUpdate extends LinearLayout implements
        View.OnFocusChangeListener {
    private static final String TAG = "LocalUpdate";
    private static Context mContext;
    private Handler mHandler;

    // button of OK
    private Button mSystemOKBtn;
    // button of Cancel
    private Button mSystemCancelBtn;

    // update data path
    private static String sPath;

    public LocalUpdate(Context context, Handler handle, String path) {
        super(context);
        mContext = context;
        mHandler = handle;
        sPath = path;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.local_update, this);
        mSystemOKBtn = (Button) findViewById(R.id.update_btn);
        mSystemCancelBtn = (Button) findViewById(R.id.update_cancel_btn);
        mSystemOKBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Message msg = mUpdateHandler.obtainMessage(0);
                mUpdateHandler.sendMessage(msg);
                LogHelper.d(TAG, "mPath:" + sPath);
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DIALOG_CLOSE;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
        mSystemCancelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DIALOG_CLOSE;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
    }

    /**
     * handler of update
     */
    final static Handler mUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    String path = sPath;
                    File updatefile = new File(path + "/update.zip");
                    LogHelper.d(TAG,"directly go to installPackage, and path ="+path);
                    TaskUtil.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                RecoverySystem.installPackage(mContext, updatefile);
                            } catch (IOException e) {
                                LogHelper.e(TAG, "install pkg error.");
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }

    private static void writeUuid(String path){
        if(path.equals("/mnt/sdcard")){
            return;
        }
        StringBuilder sb = new StringBuilder();
        Map<String, ArrayList<String>> map = new HashMap<>();
        map = halApi.getUuidList();
        LogHelper.d(TAG,"uuidlist size ="+map.size());
        if(map.size() == 0){
            return;
        }
        String uuid = map.get(path).get(0);
        String type = map.get(path).get(1);
        sb.append("UUID=\""+ uuid + "\"; TYPE=\""+ type +"\"");
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream("/cache/recovery/uuid");
            outputStream.write(sb.toString().getBytes());
            LogHelper.d(TAG,"write success");
        } catch (FileNotFoundException e) {
            LogHelper.d(TAG,"fileNotFound");
        } catch (IOException e) {
            LogHelper.d(TAG,"IOException");
        } finally{
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            }catch (IOException e) {
                LogHelper.e(TAG, "close outputStream failed: ", e);
            }
        }
    }

}
