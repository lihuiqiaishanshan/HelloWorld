
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
public class NetUpdateFailedView extends LinearLayout implements
        View.OnFocusChangeListener {
    private Handler mHandler;
    // private Context mContext;
    // button of confirm
    private Button confirmBtn;

    // private LogicFactory mLogicFactory;

    public NetUpdateFailedView(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.net_update_failed, this);
        confirmBtn = (Button) findViewById(R.id.net_failed_btn);
        confirmBtn.setOnClickListener(new OnClickListener() {

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
