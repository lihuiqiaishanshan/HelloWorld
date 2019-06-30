package com.hisilicon.android.videoplayer.activity.base;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.widget.Toast;
import android.view.KeyEvent;

import com.hisilicon.android.videoplayer.R;

/**
 * MenuBaseActivity: business independent
 */
public class BaseActivity extends Activity {
    protected ProgressDialog mProgressDialog;

    protected void enableStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog().build());
    }

    protected void openActivity(Class<?> pClass) {
        Intent _Intent = new Intent();
        _Intent.setClass(this, pClass);
        startActivity(_Intent);
    }

    protected LayoutInflater getInflater() {
        LayoutInflater _Inflater = LayoutInflater.from(this);
        return _Inflater;
    }

    /**
     * get single operation dialog
     *
     * @param pTitleId         dialog title id
     * @param pMessageId       dialog message id
     * @param pOnClickListener dialog click listener
     * @return dialog
     */
    protected AlertDialog getSingleOperationDialog(int pTitleId, int pMessageId,
                                                   DialogInterface.OnClickListener pOnClickListener) {
        return new AlertDialog.Builder(this).setTitle(pTitleId).setMessage(pMessageId)
                .setPositiveButton(android.R.string.ok, pOnClickListener).setCancelable(false).show();
    }

    /**
     * get double operation dialog
     *
     * @param pTitleId         dialog title id
     * @param pMessageId       dialog message id
     * @param pOnClickListener dialog click listener
     * @return dialog
     */
    protected AlertDialog getDoubleOperationDialog(int pTitleId, int pMessageId,
                                                   DialogInterface.OnClickListener pOnClickListener) {
        return new AlertDialog.Builder(this).setTitle(pTitleId).setMessage(pMessageId)
                .setPositiveButton(android.R.string.yes, pOnClickListener)
                .setNegativeButton(android.R.string.no, pOnClickListener).setCancelable(true).show();
    }

    protected void setDialogMessage(CharSequence message) {
        if (mProgressDialog != null) {
            mProgressDialog.setMessage(message);
        }
    }

    protected void showProgressDialog(int pTitleId, int pMessageId) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
        }

        mProgressDialog.setTitle(pTitleId);
        mProgressDialog.setMessage(getString(pMessageId));
        //mProgressDialog.setCancelable(false);
        mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            long mExitTime = 0;

            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {//Exit HiVideoPlayer when loading timeout
                if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    if ((System.currentTimeMillis() - mExitTime) > 2000) {
                        // if more than 2000ms,don't exit.
                        Toast.makeText(getApplicationContext(), getString(R.string.dialogExitToast), Toast.LENGTH_SHORT).show();
                        mExitTime = System.currentTimeMillis();
                    } else {
                        dismissProgressDialog();
                        finish();
                    }
                    return true;
                }
                return false;
            }
        });
        mProgressDialog.show();
    }

    protected void dismissProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    protected int getSystemResId(String idName, String type) {
        return Resources.getSystem().
                getIdentifier(idName, type, "android");
//               getIdentifier("VideoView_error_text_invalid_progressive_playback",
//                       "string", "android");
    }
}
