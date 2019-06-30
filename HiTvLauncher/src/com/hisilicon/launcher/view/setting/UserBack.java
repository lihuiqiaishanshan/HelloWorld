
package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.dtv.play.Player;
import com.hisilicon.dtv.play.PlayerManager;
import com.hisilicon.launcher.util.Constant;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.SystemSettingInterface;


/**
 * Enter the local update, detects the update package Dialog, temporarily not
 * used
 *
 * @author wangchuanjian
 */
public class UserBack extends LinearLayout implements
        View.OnFocusChangeListener {
    // private Context mContext;
    private Handler mHandler;
    // button of OK
    private Button mSystemOKBtn;
    // button of cancel
    private Button mSystemCancelBtn;

    private DTV mDTV = null;
    private DTVConfig mDtvConfig = null;
    private PlayerManager playerManager = null;
    protected Player mPlayer = null;

    // private LogicFactory mLogicFactory;

    public UserBack(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        mDTV = DTV.getInstance(Constant.DTV_PLUGIN_NAME);
        mDtvConfig = mDTV.getConfig();
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.user_back, this);
        mSystemOKBtn = (Button) findViewById(R.id.user_back_ok);
        mSystemCancelBtn = (Button) findViewById(R.id.user_back_cancel);
        mSystemOKBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                SystemSettingInterface.restoreDefault();
                mDtvConfig.restoreDefaultConfig();
                playerManager = mDTV.getPlayerManager();
                if (playerManager.getPlayers().size() > 0) {
                    mPlayer = playerManager.getPlayers().get(0);
                } else {
                    mPlayer = playerManager.createPlayer();
                }

                mPlayer.releaseResource(0);

                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.DIALOG_CLOSE;
                mHandler.sendMessageDelayed(message, 100);

                Toast.makeText(mContext.getApplicationContext(), R.string.restore_success,
                        Toast.LENGTH_LONG).show();
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
