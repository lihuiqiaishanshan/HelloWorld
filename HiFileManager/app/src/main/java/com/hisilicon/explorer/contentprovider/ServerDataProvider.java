package com.hisilicon.explorer.contentprovider;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.db.DBHelper;
import com.hisilicon.explorer.model.NFSItemInfo;
import com.hisilicon.explorer.service.INFSAidlService;
import com.hisilicon.explorer.service.ISambaAidlService;
import com.hisilicon.explorer.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 */

public class ServerDataProvider extends ContentProvider {

    private String TAG = ServerDataProvider.class.getSimpleName();

    private Context mContext;
    DBHelper mDbHelper = null;
    SQLiteDatabase db = null;
    public static final String AUTOHORITY = "com.hisilicon.explorer.serverprovider";
    public static final int Samba_Code = 2;
    private static final UriMatcher mMatcher;
    private ISambaAidlService smbClient;


    static {
        mMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mMatcher.addURI(AUTOHORITY, "samba", Samba_Code);
        mMatcher.addURI(AUTOHORITY, "samba/#", Samba_Code);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        mDbHelper = new DBHelper(getContext(), DBHelper.DATABASE_NAME, null, DBHelper.DATABASE_VERSION);
        db = mDbHelper.getWritableDatabase();
        startSambaService();
        return false;
    }


    private void startSambaService() {
        Intent intent = new Intent();
        intent.setClassName("com.hisilicon.explorer", "com.hisilicon.explorer.service.SambaService");
        getContext().startService(intent);
        Intent bindIntent = new Intent();
        bindIntent.setClassName("com.hisilicon.explorer", "com.hisilicon.explorer.service.SambaService");
        getContext().bindService(bindIntent, sambaServerConnection, Context.BIND_AUTO_CREATE);
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String table = getTableName(uri);
        return db.query(table, projection, selection, selectionArgs, null, null, sortOrder, null);
    }

    @Override
    public String getType(Uri uri) {
        return getTableName(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LogUtils.LOGD(TAG, "insert uri: " + uri.toString());
        String table = getTableName(uri);
        db.insert(table, null, values);
        mContext.getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LogUtils.LOGD(TAG, "delete uri: " + uri.toString());
        String table = getTableName(uri);
        int delete = db.delete(table, selection, selectionArgs);
        if (delete > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return delete;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        LogUtils.LOGD(TAG, "update uri: " + uri.toString());
        String table = getTableName(uri);
        int update = db.update(table, values, selection, selectionArgs);
        if (update > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return update;
    }

    private String getTableName(Uri uri) {
        String tableName = null;
        switch (mMatcher.match(uri)) {
            case Samba_Code:
                tableName = DBHelper.SAMBA_TABLE_NAME;
                break;
        }
        return tableName;
    }


    ServiceConnection sambaServerConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            smbClient = ISambaAidlService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            smbClient = null;
        }
    };


    /**
     * 获取samba的所有服务器地址
     *
     * @return 包含地址的字符串
     */
    private String getSmbServices() {
        if (null == smbClient) {
            return "";
        }
        try {
            String workgroups = smbClient.getWorkgroups();
            if (TextUtils.isEmpty(workgroups) || "ERROR".equals(workgroups)) {
                return "";
            } else {
                return workgroups;
            }
        } catch (RemoteException e) {
            LogUtils.LOGE(TAG, "getSmbServices RemoteException!");
        }
        return "";
    }

    /**
     * 挂载服务器
     *
     * @param address    ip地址
     * @param workpath   工作路径
     * @param mountpoint 挂载点
     * @param user       用户名
     * @param password   密码
     * @return 是否挂载成功
     */
    private boolean smbMount(String address, String workpath, String mountpoint, String user, String password) {
        if (smbClient == null) {
            return false;
        }
        try {
            return smbClient.UImount(address, workpath.toUpperCase(), mountpoint, user, password);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 卸载挂载节点
     *
     * @param mountPonit 挂载节点
     * @return 是否卸载成功
     */
    private boolean smbUmountPoint(String mountPonit) {
        if (smbClient == null) {
            return false;
        }
        try {
            return smbClient.myUmount(mountPonit);
        } catch (RemoteException e) {
            LogUtils.LOGE(TAG, "smbUmountPoint RemoteException!!");
        }
        return false;
    }

    /**
     * 通过路径获取smb的挂载节点
     *
     * @param path 挂载路径
     * @return 挂载节点
     */
    private String getSmbMountPoint(String path) {
        if (null == smbClient) {
            return "";
        }
        try {
            String point = smbClient.getMountPoint(path.toUpperCase());
            if (TextUtils.isEmpty(point) || "ERROR".equals(point)) {
                return "";
            } else {
                return point;
            }
        } catch (RemoteException e) {
            LogUtils.LOGE(TAG, "getSmbMountPoint RemoteException!!");
        }
        return "";
    }

    /**
     * 通过ip获取ip下的所有可以挂载列表
     *
     * @param servername ip地址
     * @return 返回可以挂载的列表
     */
    private String getSmbShareFolders(String servername, String account, String pwd) {
        if (null == smbClient) {
            return "";
        }
        try {
            String detailsBy = smbClient.getDetailsBy(servername, account, pwd);
            if (TextUtils.isEmpty(detailsBy) || "NT_STATUS_ACCESS_DENIED".equals(detailsBy)) {
                return "";
            } else {
                return detailsBy;
            }
        } catch (RemoteException e) {
            LogUtils.LOGE(TAG, "getSmbShareFolders RemoteException!!");
        }
        return "";
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        //以下为samba相关
        //这3个接口都为sambaclient所调用，不需要smbtree
        if ("smbmount".equals(method)) {
            String address = extras.getString("address", "");
            String workpath = extras.getString("workpath", "");
            String mountpoint = extras.getString("mountpoint", " ");
            String user = extras.getString("user", "g");
            String password = extras.getString("password", "");
            Bundle bundle = new Bundle();
            bundle.putBoolean("mountresult", smbMount(address, workpath, mountpoint, user, password));
            return bundle;
        } else if ("smbumountpoint".equals(method)) {
            String mountpoint = extras.getString("mountpoint", "");
            Bundle bundle = new Bundle();
            bundle.putBoolean("umountresult", smbUmountPoint(mountpoint));
            return bundle;
        } else if ("getSmbMountPoint".equals(method)) {
            String path = extras.getString("path", "");
            Bundle bundle = new Bundle();
            bundle.putString("getmountpoint", getSmbMountPoint(path));
            return bundle;
        }
        //以下接口为smbtree接口
        else if ("getSmbServers".equals(method)) {
            Bundle bundle = new Bundle();
            bundle.putString("smbservers", getSmbServices());
            return bundle;
        } else if ("getSmbShareFolders".equals(method)) {
            String servername = extras.getString("servername", "");
            String account = extras.getString("account", "");
            String pwd = extras.getString("pwd", "");
            Bundle bundle = new Bundle();
            bundle.putString("getsharefolders", getSmbShareFolders(servername, account, pwd));
            return bundle;
        }
        return super.call(method, arg, extras);
    }
}
