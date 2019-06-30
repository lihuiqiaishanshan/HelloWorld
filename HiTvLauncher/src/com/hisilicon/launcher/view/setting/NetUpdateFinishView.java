
package com.hisilicon.launcher.view.setting;

import java.io.File;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.os.RecoverySystem;
import java.io.IOException;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.LogHelper;
import com.hisilicon.launcher.util.TaskUtil;

import android.os.SystemProperties;
/**
 * The download is complete, whether to upgrade Dialog
 *
 * @author wangchuanjian
 */
public class NetUpdateFinishView extends LinearLayout implements
        View.OnFocusChangeListener {
    private static Context mContext;
    private Handler mHandler;
    // button of OK
    private Button mSystemOKBtn;
    // button of cancel
    private Button mSystemCannelBtn;

    // private LogicFactory mLogicFactory;

    public NetUpdateFinishView(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.net_update_finish, this);
        mSystemOKBtn = (Button) findViewById(R.id.update_ok_btn);
        mSystemOKBtn.requestFocus();
        mSystemOKBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Restart the computer to complete the upgrade
                Message msg = mUpdateHandler.obtainMessage(0);
                mUpdateHandler.sendMessage(msg);
            }
        });
        mSystemCannelBtn = (Button) findViewById(R.id.update_cancel_btn);
        mSystemCannelBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DIALOG_CLOSE;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
    }

    private static final String TAG = "Update";

    /**
     * handler of update
     */
    final static Handler mUpdateHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: {
                    String path = Environment.getExternalStorageDirectory()
                            .getPath();// "/mnt/sda/sda1" ; //+ "/"+fileName

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
}
