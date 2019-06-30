
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

/** The system upgrade */
public class SystemUpdateView extends LinearLayout implements
        View.OnFocusChangeListener {
    private Context mContext;
    // Control NetSettingDialog display content
    private Handler mHandler;


    // text of local update
    private TextView localUpdateText;
    // text of new update
    private TextView newUpdateText;
    // list of textView
    private TextView[] mTextList;

    public SystemUpdateView(Context context, Handler handler, int focus) {
        super(context);
        mContext = context;
        mHandler = handler;
        // mLogicFactory = new LogicFactory(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        View parent = inflater.inflate(R.layout.system_update_view, this);
        initView(parent, focus);
    }
    private void initView(View parent, int focus) {
        localUpdateText = (TextView) parent.findViewById(R.id.update_local_txt);
        localUpdateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                message.what = NetSettingDialog.SYSTEM_LOCAL_UPDATE;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
        localUpdateText.setOnFocusChangeListener(this);

        newUpdateText = (TextView) parent.findViewById(R.id.update_net_txt);
        newUpdateText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                 message.what = NetSettingDialog.SELECT_UPDATE_VERSION;
                 mHandler.sendMessageDelayed(message, 100);
            }
        });
        newUpdateText.setOnFocusChangeListener(this);

        mTextList = new TextView[] {
                localUpdateText, newUpdateText
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
