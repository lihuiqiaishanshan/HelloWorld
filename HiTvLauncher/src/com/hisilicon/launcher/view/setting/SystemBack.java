
package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.launcher.util.LogHelper;
import android.os.RecoverySystem;
import java.io.IOException;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.factory.LogicFactory;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.interfaces.SystemSettingInterface;
import com.hisilicon.launcher.util.TaskUtil;

/**
 * Enter the local update, detects the update package Dialog, temporarily not
 * used
 *
 * @author wangchuanjian
 */
public class SystemBack extends LinearLayout implements
        View.OnFocusChangeListener {

    private static final String TAG = "SystemBack";

    private Context mContext;
    private Handler mHandler;
    private Button mSystemOKBtn;
    private Button mSystemCancelBtn;
    private LogicFactory mLogicFactory;
    private DTV mDTV = null;
    private DTVConfig mDtvConfig = null;
    private PlayerManager playerManager = null;
    protected Player mPlayer = null;


    public SystemBack(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        mLogicFactory = new LogicFactory(mContext);
        mDTV = DTV.getInstance(Constant.DTV_PLUGIN_NAME);
        mDtvConfig = mDTV.getConfig();
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.system_back, this);
        mSystemOKBtn = (Button) findViewById(R.id.back_ok_btn);
        mSystemCancelBtn = (Button) findViewById(R.id.back_cancel_btn);
        mSystemOKBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LogHelper.i(TAG, "factory reset!");
                SystemSettingInterface.restoreDefault();
                mDtvConfig.restoreDefaultConfig();

                playerManager = mDTV.getPlayerManager();
                if (playerManager.getPlayers().size() > 0) {
                    mPlayer = playerManager.getPlayers().get(0);
                } else {
                    mPlayer = playerManager.createPlayer();
                }

                mPlayer.releaseResource(0);
                TaskUtil.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RecoverySystem.rebootWipeUserData(mContext);
                        } catch (IOException e) {
                            LogHelper.e(TAG, "factory reset error.");
                        }
                    }
                });
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
    }
}
