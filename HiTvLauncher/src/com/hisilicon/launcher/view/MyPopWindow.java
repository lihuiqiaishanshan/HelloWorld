
package com.hisilicon.launcher.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Util;

public class MyPopWindow extends AlertDialog implements
        View.OnFocusChangeListener, View.OnClickListener {

    private Context mContext;
    // dialog box prompts
    private String mResource[] = null;

    private ResolveInfo mResolveInfo = null;

    private MyPopWindow mMyPopWindow = null;
    private PackageManager mPackManager = null;

    public MyPopWindow(Context context, final String resource[],
            final ResolveInfo info) {
        super(context);
        mContext = context;
        mResource = resource;
        mResolveInfo = info;
        mPackManager = mContext.getPackageManager();
        mMyPopWindow = this;
    }

    // Remove data
    private Handler mDataHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Util.CLEAR_USER_DATA:
                    Util.showToast(mContext, R.string.clear_data_suc);
                    break;
                case Util.NOT_CLEAR_USER_DATA:
                    Util.showToast(mContext, R.string.clear_data_fail);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * Initialize the View dialog box
     */
    public void init() {
        if (mResource == null || mResource.length <= 0 || mResolveInfo == null) {
            return;
        }

        int width = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getWidth();
        int height = ((Activity) mContext).getWindowManager()
                .getDefaultDisplay().getHeight();
        LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setBackgroundResource(R.drawable.app_manage_pop_window_bg);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView txtView = new TextView(mContext);
        LayoutParams txtParams = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT, 1);
        txtView.setFocusable(false);
        CharSequence label = mResolveInfo.loadLabel(mPackManager);
        if(label != null) {
            txtView.setText(label.toString());
        } else {
            txtView.setText("");
        }
        txtView.setTextColor(Color.WHITE);
        txtView.setTextSize(25);
        txtParams.topMargin = 10;
        txtParams.bottomMargin = 10;
        txtView.setLayoutParams(txtParams);
        txtView.setGravity(Gravity.CENTER);
        linearLayout.addView(txtView);
        for (int i = 0; i < mResource.length; i++) {
            Button but = new Button(mContext);
            LayoutParams butParams = new LayoutParams(LayoutParams.FILL_PARENT,
                    LayoutParams.FILL_PARENT, 1);
            butParams.leftMargin = 50;
            butParams.topMargin = 5;
            butParams.bottomMargin = 20;
            butParams.rightMargin = 50;
            but.setLayoutParams(butParams);
            but.setText(mResource[i]);
            but.setTextColor(Color.GRAY);
            but.setTextSize(20);
            linearLayout.addView(but);
            but.setTag(i);
            but.setBackgroundResource(R.drawable.app_manage_button_selector);
            but.setOnClickListener(this);
            but.setOnFocusChangeListener(this);
            String txtString = mContext.getString(R.string.clear_data);
            if (txtString.equals(mResource[i])) {
                ApplicationInfo appInfo = mResolveInfo.activityInfo.applicationInfo;
                if ((appInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_ALLOW_CLEAR_USER_DATA)) == ApplicationInfo.FLAG_SYSTEM) {
                    but.setEnabled(false);
                }
            }
        }
        setContentView(linearLayout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_MENU) {
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        MyPopWindow.this.dismiss();
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            ((Button) v).setTextColor(Color.WHITE);
        } else {
            ((Button) v).setTextColor(Color.GRAY);
        }
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        if (mResolveInfo == null) {
            mMyPopWindow.dismiss();
            return;
        }
        ApplicationInfo appInfo = mResolveInfo.activityInfo.applicationInfo;
        switch (position) {
        // uninstall
            case 0:
                Util.unLoad(mContext, mResolveInfo);
                break;
            // stop
            case 1:

                Util.forceStopPackage(mContext, appInfo.packageName);
                break;
            // clearDefault
            case 2:
                Util.clearDefault(mContext, appInfo.packageName);
                break;
            // clearData
            case 3:
                Util.clearData(mContext, mResolveInfo.activityInfo.applicationInfo, mDataHandler);
                break;
            default:
                break;
        }
        mMyPopWindow.dismiss();

    }

}
