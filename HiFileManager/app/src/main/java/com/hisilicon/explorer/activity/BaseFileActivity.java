package com.hisilicon.explorer.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.utils.LogUtils;
import com.hisilicon.explorer.utils.SystemProperties;
import com.hisilicon.explorer.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 */

public abstract class BaseFileActivity extends BaseActivity {

    private final String TAG = BaseFileActivity.class.getSimpleName();

    public static final int USB_STATE_CHANGE = 1;
    public static final int NETWORK_STATE_CHANGE = 2;

    protected boolean isNetworkFile = false;
    protected String mBDISOName = "";
    protected boolean mIsSupportBD = false;
    protected String mBDISOPath = "";

    //取消进度条原因
    private int dismissProgressReason = 0;

    //加载本地挂载路径信息
    protected final int LOCALSDCARDLOADING = 1;
    //加载文件列表
    protected final int FILELISTLOADING = 2;
    //搜索文件
    protected final int SEARCHFILELIST = 3;
    //加载smb server列表
    protected final int GETSMBSERVER = 7;

    /* for open file start*/
    private final String VIDEO_PLAYER_PACKAGE_PATH = "com.hisilicon.android.videoplayer";
    private final String VIDEO_PLAYER_COMPLETE_SERVICE_PATH = "com.hisilicon.android.videoplayer.activity.MediaFileListService";
    private final String GALLERY_PACKAGE_PATH = "com.hisilicon.higallery";
    private final String GALLERY_NETWORK_COMPLETE_PATH = "com.hisilicon.higallery.NetworkHiGallery";
    private final String GALLERY_THUNDERSOFT_PACKAGE_PATH = "com.thundersoft.higallery";
    private final String GALLERY_THUNDERSOFT_COMPLETE_PATH = "com.thundersoft.higallery.HiGallery";
    private final String GALLERY3D_THUNDERSOFT_PACKAGE_PATH = "com.hisilicon.android.gallery3d";
    private final String GALLERY3D_THUNDERSOFT_COMPLETE_SERVICE_PATH = "com.hisilicon.android.gallery3d.list.ImageFileListService";
    private final String INTENT_TAG_SUB_FLAG = "subFlag";
    private final String INTENT_TAG_PATH = "path";
    private final String INTENT_PATH_FLAG = "pathFlag";
    private final String INTENT_ACTION_VIEW = "android.intent.action.VIEW";
    private final String INTENT_ACTION_PACKAGE = "android.intent.action.INSTALL_PACKAGE";
    private final String APK_MIMETYPE = "application/vnd.android.package-archive";
    private final String INTENT_TAG_SORT_COUNT = "sortCount";
    private final String MIMETYPE_AUDIO_START = "audio";
    private final String MIMETYPE_AUDIO = "audio/*";
    private final String MIMETYPE_IMAGE_START = "image";
    private final String MIMETYPE_IMAGE = "image/*";
    private final String MIMETYPE_VIDEO_START = "video";
    private final String MIMETYPE_VIDEO = "video/*";
    private final String SP_MUSIC_PATH = "musicPath";
    private final String SP_MUSIC_PATH_S = "music_path";
    /* for open file end*/
    private ProgressDialog progressDialog;
    private AlertDialog sureDialog;
    private NetworkConnectChangedReceiver mNetworkChangeListener = new NetworkConnectChangedReceiver();

    //用户点击了取消progress按钮
    private boolean bUserClickProgressReturn = false;

    @Override
    public void init() {
        initView();
        initData();
        getUSB();
        mIsSupportBD = true;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("MEDIA_PLAY_UVMOS_MONITOR_MESSAGE");
        registerReceiver(mNetworkChangeListener, filter);
    }

    public abstract int getLayoutId();

    public abstract void initView();

    protected abstract void initData();

    protected abstract void stateChange(int state, boolean bstate, Intent intent);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(usbReceiver);
        unregisterReceiver(mNetworkChangeListener);
    }

    private void getUSB() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    /**
     * 显示进度度画框
     *
     * @param showreason 显示进度对话框原因
     */
    protected void showProgressDialog(int showreason) {
        if (null == progressDialog) {
            progressDialog = new ProgressDialog(this, R.style.MyDialogStyle);
            progressDialog.setOnKeyListener(onProgressKeyListener);
            progressDialog.setOnDismissListener(onProgressDismissListener);
        }
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        dismissProgressReason = showreason;
        progressDialog.show();
    }

    //单目前是点击返回并且，progress dialog处于消失状态，回调用户主动取消progress
    DialogInterface.OnDismissListener onProgressDismissListener = new DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            if (bUserClickProgressReturn) {
                bUserClickProgressReturn = false;
                progressCancelByUserListener(dismissProgressReason);
            }
        }
    };

    DialogInterface.OnKeyListener onProgressKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                bUserClickProgressReturn = true;
            }
            return false;
        }
    };

    /**
     * 取消进度对话框
     */
    protected void dismissProgressDialog() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * 进度框取消回调
     *
     * @param operate 正在进行什么操作
     */
    abstract void progressCancelByUserListener(int operate);

    /**
     * 将路径转换成对应的uri
     *
     * @param path
     */
    private Uri transformUri(String path) {
        return Uri.fromFile(new File(path));
       /* if (Build.VERSION.SDK_INT >= 24) {
            return FileProvider.getUriForFile(this, "com.hisilicon.explorer.fileprovider", new File(path));
        } else {
            return Uri.fromFile(new File(path));
        }*/
    }

    /* open the file method */
    /* CNcomment: 打开文件方法 */
    public void openFile(BaseFileActivity activity, FileInfo f) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String type = f.getMimeTypes();
        if (activity.getIntent().getBooleanExtra(INTENT_TAG_SUB_FLAG, false)) {
            intent.setClassName(VIDEO_PLAYER_PACKAGE_PATH, VIDEO_PLAYER_COMPLETE_SERVICE_PATH);
            intent.putExtra(INTENT_TAG_PATH, f.getPath());
            intent.putExtra(INTENT_PATH_FLAG, false);
            activity.startActivity(intent);
            activity.finish();
            return;
        } else if (type.split("/")[0].equals(MIMETYPE_AUDIO_START)) {
            type = MIMETYPE_AUDIO;
            intent.setType(type);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(transformUri(f.getPath()), type);
        } else if (type.split("/")[0].equals(MIMETYPE_IMAGE_START)) {
            type = MIMETYPE_IMAGE;
            if (Config.getInstance().ismIsHiDPT()) {
                /*for DPT after 811, uses 4kgallery, others use HiGalleryL*/
                if (Build.VERSION.SDK_INT >= 26) {
                    intent.setAction(INTENT_ACTION_VIEW);
                    intent.setDataAndType(transformUri(f.getPath()), type);
                    intent.setClassName(GALLERY_PACKAGE_PATH, GALLERY_NETWORK_COMPLETE_PATH);
                } else {
                    //HiGalleryL
                    intent.setClassName(GALLERY_THUNDERSOFT_PACKAGE_PATH, GALLERY_THUNDERSOFT_COMPLETE_PATH);
                    intent.setAction(INTENT_ACTION_VIEW);
                    intent.setDataAndType(transformUri(f.getPath()), MIMETYPE_IMAGE);
                    activity.startActivity(intent);
                    return;
                }
            } else {
                /*for other platform, push a chooser*/
                //intent.setType(type);
                //intent.setAction(Intent.ACTION_VIEW);
                //intent.setDataAndType(Uri.fromFile(f), type);
                /* default use hisigallery same with 4.4 */
                if (Build.VERSION.SDK_INT >= 26) {
                    intent.setAction(INTENT_ACTION_VIEW);
                    intent.setDataAndType(transformUri(f.getPath()), type);
                    intent.setClassName(GALLERY_PACKAGE_PATH, GALLERY_NETWORK_COMPLETE_PATH);
                } else {
                    intent.setClassName(GALLERY3D_THUNDERSOFT_PACKAGE_PATH, GALLERY3D_THUNDERSOFT_COMPLETE_SERVICE_PATH);
                    SharedPreferences p = getSharedPreferences(SP_MUSIC_PATH, MODE_PRIVATE);
                    String path = p.getString(SP_MUSIC_PATH_S, "");
                    intent.putExtra(INTENT_TAG_PATH, path);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(transformUri(f.getPath()), MIMETYPE_IMAGE);
                    startService(intent);
                    return;
                }
            }
        } else if (type.equals("video/iso") || type.equals("video/dvd")) {
            intent.setClassName(VIDEO_PLAYER_PACKAGE_PATH, VIDEO_PLAYER_COMPLETE_SERVICE_PATH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "video/iso";
            //如果是目录就不用Uri.parseFile()
            if (new File(f.getPath()).exists() && new File(f.getPath()).isDirectory()) {
                intent.setDataAndType(Uri.parse(f.getPath()), type);
            } else {
                intent.setDataAndType(transformUri(f.getPath()), type);
            }
            intent.putExtra(INTENT_TAG_SORT_COUNT, Config.getInstance().getFileSortType());
            activity.startService(intent);
            return;
        } else if (type.equals("video/bd")) {
            intent.setClassName(VIDEO_PLAYER_PACKAGE_PATH, VIDEO_PLAYER_COMPLETE_SERVICE_PATH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            type = "video/bd";
            intent.setDataAndType(Uri.parse(f.getPath()), type);
            intent.putExtra(INTENT_TAG_SORT_COUNT, Config.getInstance().getFileSortType());
            activity.startService(intent);
            return;
        } else if (type.split("/")[0].equals(MIMETYPE_VIDEO_START)) {
            type = MIMETYPE_VIDEO;
            intent.setClassName(VIDEO_PLAYER_PACKAGE_PATH, VIDEO_PLAYER_COMPLETE_SERVICE_PATH);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(transformUri(f.getPath()), type);
            intent.putExtra(INTENT_TAG_SORT_COUNT, Config.getInstance().getFileSortType());
            activity.startService(intent);
            return;
        } else if (type.equals(APK_MIMETYPE)) {
            /*intent.setClassName("com.android.packageinstaller",
            "com.android.packageinstaller.PackageInstallerActivity");*/
            intent.setAction(INTENT_ACTION_PACKAGE);
            intent.setDataAndType(transformUri(f.getPath()), APK_MIMETYPE);
        } else if (f.getPath().toLowerCase().contains(".bdmv")) {
            if (mIsSupportBD) {
                launchHiBDPlayer(f.getPath());
            }
            return;
        } else {
            intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(INTENT_TAG_SORT_COUNT, Config.getInstance().getFileSortType());
            intent.setDataAndType(transformUri(f.getPath()), type);
        }
        if (!activity.getIntent().getBooleanExtra(INTENT_TAG_SUB_FLAG, false)) {
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(BaseFileActivity.this, getString(R.string.no_file_support), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "not find activity : ", e);
            }
        }
    }

    protected void launchHiBDPlayer(String path) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("bluray://"));
        intent.putExtra(INTENT_TAG_PATH, path);
        intent.putExtra("isNetworkFile", isNetworkFile);
        int index = mBDISOName.lastIndexOf(".");
        if (index != -1) {
            intent.putExtra("BDISOName", mBDISOName.substring(0, index));
        }
        intent.putExtra("BDISOPath", mBDISOPath);
        startActivity(intent);
    }

    public boolean isServerReachable(String ip) {
        boolean reachable = false;
        try {
            InetAddress address = InetAddress.getByName(ip);
            reachable = address.isReachable(5000);
        } catch (UnknownHostException e) {
            LogUtils.LOGE(TAG, "InetAddress.getByName : ", e);
        } catch (SecurityException e) {
            LogUtils.LOGE(TAG, "InetAddress.getByName : ", e);
        } catch (IOException e) {
            LogUtils.LOGE(TAG, "InetAddress.isReachable : ", e);
        } catch (IllegalArgumentException e) {
            LogUtils.LOGE(TAG, "InetAddress.isReachable : ", e);
        }
        return reachable;
    }

    public void showNetworkNotify() {
        if (sureDialog != null && sureDialog.isShowing()) {
            sureDialog.dismiss();
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("网络未连接");
        alert.setMessage("请先连接网络");
        alert.setCancelable(false);
        alert.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        sureDialog.dismiss();
                        finish();
                    }
                });
        sureDialog = alert.create();
        sureDialog.show();
    }

    public void dismissNetworkNotify() {
        if (sureDialog != null && sureDialog.isShowing()) {
            sureDialog.dismiss();
        }
    }

    private void checkNetworkStatus() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.isConnected()) {
                stateChange(NETWORK_STATE_CHANGE, true, null);
            } else {
                stateChange(NETWORK_STATE_CHANGE, false, null);
            }
        } else {   // not connected to the internet
            stateChange(NETWORK_STATE_CHANGE, false, null);

        }
    }

    public class NetworkConnectChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                checkNetworkStatus();
            }else if("MEDIA_PLAY_UVMOS_MONITOR_MESSAGE".equals(intent.getAction())){
                intent.getStringExtra("");
            }
        }
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    stateChange(USB_STATE_CHANGE, false, intent);
                } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    stateChange(USB_STATE_CHANGE, true, intent);
                }
            }
        }
    };
}
