package com.hisilicon.tvui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.Util;

public class ConfirmDialog extends Dialog
{
    private String tipMessage;
    private String tiptitle;
    private OnConfirmDialogListener mConfirmDialogListener;

    public void setConfirmDialogListener(OnConfirmDialogListener dialogListener)
    {
        mConfirmDialogListener = dialogListener;
    }

    public ConfirmDialog(Context context, int theme, String title, String tip, float alpha)
    {
        super(context, theme);
        tipMessage = tip;
        tiptitle = title;
        WindowManager.LayoutParams lp=this.getWindow().getAttributes();
        lp.alpha = alpha;
        this.getWindow().setAttributes(lp);
        if (tiptitle.equals(getContext().getResources().getString(R.string.pvr_ensure_stop_title))) {
            lp.privateFlags |= Util.HI_PRIVATE_FLAG_INTERCEPT_POWER_KEY | Util.HI_PRIVATE_FLAG_INTERCEPT_HOME_KEY;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (tiptitle.equals(getContext().getResources().getString(R.string.pvr_ensure_stop_title))) {
            switch (keyCode) {
                case KeyValue.DTV_KEYVALUE_POWER:
                case KeyValue.DTV_KEYVALUE_SOURCE:
                    return true;
                default: {
                    return super.onKeyDown(keyCode, event);
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_dialog);
        initView();
    }

    private void initView()
    {
        TextView mTextTitle = (TextView) this.findViewById(R.id.confirm_title);
        TextView mTextView = (TextView) this.findViewById(R.id.confirm_tip);
        Button mOKButton = (Button) this.findViewById(R.id.confirm_yes);
        Button mCancelButton = (Button) this.findViewById(R.id.confirm_no);
        mTextView.setText(tipMessage);
        mTextTitle.setText(tiptitle);
        mOKButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mConfirmDialogListener) {
                    mConfirmDialogListener.onCheck(OnConfirmDialogListener.OK);
                }
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (null != mConfirmDialogListener) {
                    mConfirmDialogListener.onCheck(OnConfirmDialogListener.CANCEL);
                }
            }
        });
    }

    public interface OnConfirmDialogListener
    {
        int OK = 0;
        int CANCEL = -1;

        void onCheck(int which);
    }

}
