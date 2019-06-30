
package com.hisilicon.launcher.view.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.hisilicon.launcher.R;

/**
 * The system upgrade pop-up box
 *
 * @author wangchuanjian
 */
public class SystemUpdateDialog extends Dialog {
    private Context mContext;
    // private int mFlag;
    private String mLocalPath;
    // private LogicFactory mLogicFactory = null;

    // The local updating without upgrade package state
    public final static int LOCAL_UPDATE_NONE = 1;
    // Local update upgrade package state
    public final static int LOCAL_UPDATE_HAVE = 2;
    // The network upgrade progress state
    public final static int NET_UPDATE = 3;
    // Restore factory settings - > restore user settings
    public final static int USER_BACK = 4;
    // close Dialog
    public final static int DIALOG_CLOSE = 5;
    // Restore factory settings - > System Restore
    public final static int SYSTEM_BACK = 6;
    // No network upgrade package
    public final static int NET_NO_UPDATE = 7;
    // Network upgrade package
    public final static int NET_HAVE_UPDATE = 8;
    // The network upgrade, upgrade package, into the next dialog
    public final static int DIALOG_GOTO_NEXT = 9;

    // Netupdate download failed
    public final static int DOWNLOAD_FAILED = 10;
    public final static int DOWNLOAD_CANCEL = 11;
    private NetUpdate npView = null;
    public static final Object mDownloadLock = new Object();
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == SystemUpdateDialog.DIALOG_CLOSE) {
                if (npView != null) {
                    npView.stopUpdate(false);
                }
                dismiss();
            } else if (msg.what == SystemUpdateDialog.NET_UPDATE) {
                getWindow().getAttributes().width = (int) mContext.getResources().getDimension(R.dimen.dimen_600px);
                getWindow().getAttributes().height = (int) mContext.getResources().getDimension(R.dimen.dimen_200px);
                getWindow().setAttributes(getWindow().getAttributes());
                npView = new NetUpdate(mContext, mHandler);
                npView.setUpdateCallback(mUpdateCallback);
                setContentView(npView);
                setOnDismissListener(new DialogInterface.OnDismissListener() {

                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // TODO Auto-generated method stub
                        if (npView != null) {
                            npView.stopUpdate(false);
                        }
                    }
                });
            } else if (msg.what == SystemUpdateDialog.DIALOG_GOTO_NEXT) {
                changeDialogLength();
                setContentView(new NetUpdateFinishView(mContext, mHandler));
                npView = null;
            } else if (msg.what == SystemUpdateDialog.DOWNLOAD_FAILED) {
                Toast.makeText(mContext, mContext.getString(R.string.toast_download_failed),
                        Toast.LENGTH_LONG).show();
                dismiss();
            }else if(msg.what == SystemUpdateDialog.DOWNLOAD_CANCEL)
            {
                dismiss();
            }
        }
    };

    public interface UpdateCallback {
        void onDownloadFailed();
    }

    private UpdateCallback mUpdateCallback = new UpdateCallback() {
        @Override
        public void onDownloadFailed() {
            mHandler.sendEmptyMessage(DOWNLOAD_FAILED);
        }
    };

    public SystemUpdateDialog(Context context, int flag, String path) {
        super(context, R.style.Translucent_NoTitle);
        mContext = context;
        mLocalPath = path;
        setContent(flag);
    }

    /**
     * set content view by flag
     *
     * @param flag
     */
    private void setContent(int flag) {
        switch (flag) {
            case LOCAL_UPDATE_NONE:
                // The local updating without upgrade package state
                changeDialogLength();
                setContentView(new LocalUpdateNone(mContext, mHandler));
                break;
            case LOCAL_UPDATE_HAVE:
                // Local time to upgrade package
                changeDialogLength();
                setContentView(new LocalUpdate(mContext, mHandler, mLocalPath));
                break;
            case USER_BACK:
                // Restore user settings
                setContentView(new UserBack(mContext, mHandler));
                break;
            case SYSTEM_BACK:
                // System restore
                setContentView(new SystemBack(mContext, mHandler));
                break;
            case NET_NO_UPDATE:
                // Network upgrade no upgrade package
                changeDialogLength();
                setContentView(new NetUpdateFailedView(mContext, mHandler));
                break;
            case NET_HAVE_UPDATE:
                // A network upgrade package
                changeDialogLength();
                setContentView(new NetUpdateSuccessView(mContext, mHandler));
                break;
            default:
                break;
        }
    }

    /**
     * Change the size of the dialog
     */
    public void changeDialogLength() {
        getWindow().getAttributes().width = (int) mContext.getResources().getDimension(R.dimen.dimen_400px);
        getWindow().getAttributes().height = (int) mContext.getResources().getDimension(R.dimen.dimen_350px);
        getWindow().setAttributes(getWindow().getAttributes());
    }
}
