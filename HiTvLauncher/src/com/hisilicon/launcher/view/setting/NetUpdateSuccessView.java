
package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.launcher.R;

/**
 * Enter the local update, detects the update package Dialog, temporarily not
 * used
 *
 * @author wangchuanjian
 */
public class NetUpdateSuccessView extends LinearLayout implements
        View.OnFocusChangeListener {
    // private Context mContext;
    private Handler mHandler;
    // button of Ok
    private Button mSystemOKBtn;
    // button of Cancel
    private Button mSystemCannelBtn;

    // private LogicFactory mLogicFactory;

    public NetUpdateSuccessView(Context context, Handler handler) {
        super(context);
        mContext = context;
        mHandler = handler;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.net_update_success, this);
        mSystemOKBtn = (Button) findViewById(R.id.update_ok_btn);
        mSystemOKBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Message message = mHandler.obtainMessage();
                message.what = SystemUpdateDialog.NET_UPDATE;
                mHandler.sendMessageDelayed(message, 100);
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

    @Override
    public void onFocusChange(View v, boolean hasFocus) {

    }
}
