package com.hisilicon.android.videoplayer.activity.base;

import android.app.AlertDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.utils.LogTool;
import com.hisilicon.android.videoplayer.utils.SystemProperties;

/**
 * Created on 2018/6/29.
 */

public abstract class MenuBaseActivity extends FrameActivity {

    private AlertDialog pipVideoErrDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int layoutResID = getLayoutResID();
        if (layoutResID == -1) {
            LogTool.d("LayoutResId Error id = " + layoutResID);
            finish();
            return;
        }
        setContentView(layoutResID);
        initUi();
        setListener();
        initData();
    }

    /**
     * 获取布局文件
     *
     * @return
     */
    protected abstract int getLayoutResID();

    protected abstract void initUi();

    protected abstract void initData();

    protected abstract void setListener();

    public boolean isIptvEnable() {
        String iptvEnable = SystemProperties.get("ro.product.target", "aosp");
        if ("telecom".equals(iptvEnable) || "unicom".equals(iptvEnable))
            return true;
        else
            return false;
    }

    protected boolean isOttEnable() {
        String ottEnable = SystemProperties.get("ro.product.target", "aosp");
        if ("ott".equals(ottEnable))
            return true;
        else
            return false;
    }

    protected boolean isCn() {
        return getResources().getConfiguration().locale.getCountry().equals("CN");
    }

    protected void showOneDialog(int titleId) {
        pipVideoErrDialog = new AlertDialog.Builder(this).create();
        View view = getLayoutInflater().inflate(R.layout.layout_not_support, null);
        pipVideoErrDialog.setView(view, 0, 0, 0, 0);
        pipVideoErrDialog.show();
        pipVideoErrDialog.getWindow().setDimAmount(0);
        Display display = getWindowManager().getDefaultDisplay();
        Point _Point = new Point();
        display.getSize(_Point);
        int width = _Point.x;
        WindowManager.LayoutParams params = pipVideoErrDialog.getWindow().getAttributes();
        params.width = width - (width / 6);
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.CENTER;
        pipVideoErrDialog.getWindow().setAttributes(params);
        Button leftButton = (Button) view.findViewById(R.id.splash_dialog_left);
        Button rightButton = (Button) view.findViewById(R.id.splash_dialog_right);
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(titleId);
        TextView warnMessage = (TextView) view.findViewById(R.id.warnmessage);
        int messageId = getSystemResId("VideoView_error_title", "string");
        warnMessage.setText(messageId);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pipVideoErrDialog.dismiss();
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pipVideoErrDialog.dismiss();
                finish();
            }
        });
    }
}
