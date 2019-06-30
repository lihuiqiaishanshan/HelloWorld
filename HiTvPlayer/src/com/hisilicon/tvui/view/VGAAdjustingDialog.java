package com.hisilicon.tvui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.hisilicon.tvui.R;

/**
 * VGA automatically adjusts the success, failure pop-up box
 *
 * @author wangchuanjian
 *
 */
public class VGAAdjustingDialog extends Dialog {
    // close Dialog
    public final static int DIALOG_CLOSE = 5;
    // ADJUSTTING
    public final static int ADJUSTING = 0;
    // adjust success
    public final static int ADJUST_SUCCESS = 1;
    // adjust timeout
    public final static int ADJUST_TIMEOUT = 2;
    // adjust failed
    public final static int ADJUST_FAILED = 3;
    private Context mContext;

    /**
     * handler of dismiss dialog
     */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == VGAAdjustingDialog.DIALOG_CLOSE) {
                dismiss();
            }
        }
    };

    public VGAAdjustingDialog(Context context, int flag) {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        setContent(flag);
    }

    private void setContent(int flag) {
        // mAdjustFlag = flag;
        switch (flag) {
        case ADJUSTING:
            changeDialogLength(5000);
            Toast.makeText(mContext, R.string.adjusting, Toast.LENGTH_LONG).show();
            break;
        case ADJUST_TIMEOUT:
        case ADJUST_FAILED:
            // Automatic adjusting timeout or failure
            changeDialogLength(500);
            Toast.makeText(mContext, R.string.adjust_failed, Toast.LENGTH_LONG).show();
            break;
        case ADJUST_SUCCESS:
            // Automatic adjusting success
            changeDialogLength(500);
            Toast.makeText(mContext, R.string.adjust_success, Toast.LENGTH_LONG).show();
            break;

        default:
            break;
        }
    }

    /**
     * change dialog length
     */
    public void changeDialogLength(int timeout) {
        getWindow().getAttributes().width = (int) mContext.getResources().getDimension(R.dimen.dimen_600px);
        getWindow().getAttributes().height = (int) mContext.getResources().getDimension(R.dimen.dimen_600px);;
        getWindow().setAttributes(getWindow().getAttributes());
        mHandler.removeMessages(DIALOG_CLOSE);
        mHandler.sendEmptyMessageDelayed(DIALOG_CLOSE, timeout);
    }

}
