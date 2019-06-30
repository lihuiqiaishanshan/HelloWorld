
package com.hisilicon.launcher.view.setting;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.launcher.R;

/**
 * Network settings that dialog window contents 4: get the IP, set the IP,
 * please set the correct IP, is being connected, the connection failed
 */
public class NetStateDialog extends Dialog {

    public final static int MSG_CLEAR = 0;
    public final static int CONNECTING = 1;
    public final static int CONNECT_FAILED = 2;
    public final static int SETTING_IP = 3;
    public final static int SET_IP_FAILED = 4;
    public final static int GETTING_IP = 5;
    public final static int PPPOE_AUTH_FAILED = 6;

    private Context mContext;
    private Handler mHandler;

    private TextView mStateText;
    private Button mPositiveBtn;
    private ImageView mProgressImg;
    private static volatile NetStateDialog mNetStateDialog = null;

    private NetStateDialog(Context context, Handler handler, int what) {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mHandler = handler;
        setContentView(R.layout.net_state);
        initView();
    }

    /**
     * Create dialog box
     */
    public static NetStateDialog createDialog(Context context, Handler handler,
            int what) {
        if (mNetStateDialog == null) {
            synchronized (NetStateDialog.class) {
                if (mNetStateDialog == null) {
                    mNetStateDialog = new NetStateDialog(context, handler, what);
                }
            }
        }
        return mNetStateDialog;
    }

    private void initView() {
        mStateText = (TextView) findViewById(R.id.net_state_txt);
        mPositiveBtn = (Button) findViewById(R.id.net_state_btn);
        mProgressImg = (ImageView) findViewById(R.id.setting_connecting_img);
        mPositiveBtn.requestFocus();
        mPositiveBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Message message = mHandler.obtainMessage();
                message.what = MSG_CLEAR;
                mHandler.sendMessageDelayed(message, 100);
            }
        });
    }

    public void refreshView(int what) {
        switch (what) {
            case CONNECTING:
                mStateText.setText(R.string.net_connecting);
                mProgressImg.setVisibility(View.VISIBLE);
                mPositiveBtn.setVisibility(View.GONE);
                break;
            case CONNECT_FAILED:
                mStateText.setText(R.string.net_connect_failed);
                mProgressImg.setVisibility(View.GONE);
                mPositiveBtn.setVisibility(View.VISIBLE);
                mPositiveBtn.requestFocus();
                break;
            case SETTING_IP:
                mStateText.setText(R.string.net_setting_ip);
                mProgressImg.setVisibility(View.VISIBLE);
                mPositiveBtn.setVisibility(View.GONE);
                break;
            case SET_IP_FAILED:
                mStateText.setText(R.string.net_set_ip_failed);
                mProgressImg.setVisibility(View.GONE);
                mPositiveBtn.setVisibility(View.VISIBLE);
                mPositiveBtn.requestFocus();
                break;
            case GETTING_IP:
                mStateText.setText(R.string.net_getting_ip);
                mProgressImg.setVisibility(View.VISIBLE);
                mPositiveBtn.setVisibility(View.GONE);
                break;
            case PPPOE_AUTH_FAILED:
                mStateText.setText(R.string.net_connect_pppoe_auth_failed);
                mProgressImg.setVisibility(View.GONE);
                mPositiveBtn.setVisibility(View.VISIBLE);
                mPositiveBtn.requestFocus();
                break;
            default:
                break;
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        if (mNetStateDialog == null) {
            return;
        }
        ImageView imageView = (ImageView) mNetStateDialog
                .findViewById(R.id.setting_connecting_img);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageView
                .getBackground();
        animationDrawable.start();
    }
}
