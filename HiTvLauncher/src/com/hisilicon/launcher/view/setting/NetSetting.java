
package com.hisilicon.launcher.view.setting;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.launcher.R;

/**
 * Network settings menu two
 *
 * @author huyq
 */
public class NetSetting extends LinearLayout implements
        View.OnFocusChangeListener {

    private Context mContext;
    // Control NetSettingDialog display content
    private Handler mHandler;
    // private LogicFactory mLogicFactory;

    // text of Ethernet
    private TextView mEtherText;
    // text of state
    private TextView mStateText;
    // list of text
    private TextView[] mTextList;

    public NetSetting(Context context, Handler handler, int focus) {
        super(context);
        mContext = context;
        mHandler = handler;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        View parent = inflater.inflate(R.layout.setting_net, this);
        initView(parent, focus);
    }

    /**
     * The initialization of view
     *
     * @param parent
     * @param focus
     */
    private void initView(View parent, int focus) {
        mEtherText = (TextView) parent.findViewById(R.id.ether_setting_txt);
        mEtherText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                message.what = NetSettingDialog.FLAG_ETHER;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
        mEtherText.setOnFocusChangeListener(this);

        mStateText = (TextView) parent.findViewById(R.id.net_state_txt);
        mStateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                message.what = NetSettingDialog.FLAG_STATE;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
        mStateText.setOnFocusChangeListener(this);

        mTextList = new TextView[] {
                mEtherText, mStateText
        };
        mTextList[focus].requestFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            v.setBackgroundResource(R.drawable.launcher_set_focus);
            ((TextView) v).setTextColor(Color.parseColor(mContext
                    .getResources().getStringArray(R.array.text_color)[3]));
        } else {
            v.setBackgroundResource(R.drawable.button_transparent);
            ((TextView) v).setTextColor(Color.parseColor(mContext
                    .getResources().getStringArray(R.array.text_color)[0]));

        }
    }

}
