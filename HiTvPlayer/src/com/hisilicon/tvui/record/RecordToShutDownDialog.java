package com.hisilicon.tvui.record;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.Util;

public class RecordToShutDownDialog extends Dialog {
    private Context mContext = null;

    private Button mShutDown = null;
    private Button mNoAction = null;
    private Button mScrennOff = null;
    private LinearLayout linearLayoutScreen = null;

    private OnRecordToShutDownDialogListener onRecordToShutDownDialogListener;

    public RecordToShutDownDialog(Context context) {
        super(context, R.style.dialog_transparent_fullScreen);
        mContext = context;
        setOwnerActivity((Activity)context);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.alpha = 1;
        this.getWindow().setAttributes(lp);
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity=Gravity.BOTTOM;
        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height= WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.privateFlags |= Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogTool.d(LogTool.MREC, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_shutdown_dialog);
        initLinearLayout();
    }

    private void initLinearLayout() {
        mShutDown = (Button) findViewById(R.id.btn_record_shutdown);
        mNoAction = (Button) findViewById(R.id.btn_record_noAction);
        mScrennOff = (Button) findViewById(R.id.btn_record_screen_off);
        linearLayoutScreen = (LinearLayout) findViewById(R.id.llay_screen_on);
        initOKButton();
    }
    public void setOnRecordToShutDownDialogListener(OnRecordToShutDownDialogListener onRecordToShutDownDialogListener)
    {
        this.onRecordToShutDownDialogListener = onRecordToShutDownDialogListener;
    }

    public void setLinearLayoutScreenGone() {
        if (linearLayoutScreen != null) {
            linearLayoutScreen.setVisibility(View.GONE);
        }
    }

    public void setLinearLayoutScreenVisible() {
        if (linearLayoutScreen != null) {
            linearLayoutScreen.setVisibility(View.VISIBLE);
        }
    }
    private void initOKButton() {
        mShutDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRecordToShutDownDialogListener != null) {
                    onRecordToShutDownDialogListener.onCheck(OnRecordToShutDownDialogListener.SHUTDOWN);
                }
            }
        });
        mNoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRecordToShutDownDialogListener != null) {
                    onRecordToShutDownDialogListener.onCheck(OnRecordToShutDownDialogListener.NO_ACTION);
                }
            }
        });
        mScrennOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onRecordToShutDownDialogListener != null) {
                    onRecordToShutDownDialogListener.onCheck(OnRecordToShutDownDialogListener.SCREEN_OFF);
                }
            }
        });
        mShutDown.requestFocus();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogTool.d(LogTool.MREC, "keyCode = " + keyCode);
        if(linearLayoutScreen.getVisibility()==View.VISIBLE){
            switch (keyCode) {
                case KeyValue.DTV_KEYVALUE_POWER:{
                    if (onRecordToShutDownDialogListener != null) {
                        onRecordToShutDownDialogListener.onCheck(OnRecordToShutDownDialogListener.COMEBACK);
                    }
                }
                default: {
                    return true;
                }
            }
        }else if(linearLayoutScreen.getVisibility()==View.GONE){
            switch (keyCode) {
                case KeyValue.DTV_KEYVALUE_BACK:{
                    dismiss();
                }
                default: {
                    break;
                }
        }
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface OnRecordToShutDownDialogListener {
        int SHUTDOWN = 1;
        int NO_ACTION = 2;
        int SCREEN_OFF = 3;
        int COMEBACK = 4;
        void onCheck(int which);
    }

}
