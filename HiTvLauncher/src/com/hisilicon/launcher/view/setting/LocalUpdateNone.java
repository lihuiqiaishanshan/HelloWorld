
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
 * Enter the local update
 *
 * @author wangchuanjian
 */
public class LocalUpdateNone extends LinearLayout implements
        View.OnFocusChangeListener {
    private Handler mHandler;
    // private Context mContext;
    // button of confirm
    private Button confirmBtn;

    // private LogicFactory mLogicFactory;

    public LocalUpdateNone(Context context, Handler handle) {
        super(context);
        mContext = context;
        mHandler = handle;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.local_update_none, this);
        confirmBtn = (Button) findViewById(R.id.no_update_btn);
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
