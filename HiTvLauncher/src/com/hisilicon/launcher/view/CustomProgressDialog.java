
package com.hisilicon.launcher.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.Gravity;
import android.widget.ImageView;

import com.hisilicon.launcher.R;

/**
 * A custom schedule frame
 *
 * @author huyq
 */
public class CustomProgressDialog extends Dialog {

    // private Context mContext = null;
    private static CustomProgressDialog mCustomProgressDialog = null;

    public CustomProgressDialog(Context context) {
        super(context);
        // this.mContext = context;
    }

    public CustomProgressDialog(Context context, int theme) {
        super(context, R.style.Translucent_NoTitle);
        setContentView(R.layout.app_loading_dialog);
    }

    public static CustomProgressDialog createDialog(Context context) {

        mCustomProgressDialog = new CustomProgressDialog(context,
                R.style.CustomProgressDialog);
        // customProgressDialog.setContentView(R.layout.slt_cnt_type);
        mCustomProgressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        mCustomProgressDialog.setCancelable(false);
        mCustomProgressDialog.setCanceledOnTouchOutside(false);

        return mCustomProgressDialog;
    }

    public void onWindwFousChanged(boolean hasFocus) {
        if (hasFocus) {
            if (mCustomProgressDialog == null) {
                return;
            }
            ImageView imageView = (ImageView) mCustomProgressDialog
                    .findViewById(R.id.img);
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView
                    .getBackground();
            animationDrawable.start();
        }
    }

}
