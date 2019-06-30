package com.hisilicon.explorer.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.SambaItemInfo;

import java.util.ArrayList;
import java.util.List;

/**
 */

public class SambaServerLoader extends BaseFileLoader<List<SambaItemInfo>> {

    private Context mContext;
    private IProgress<List<SambaItemInfo>> iProgress;

    private SambaItemInfo defaultItem;

    private int searchType;

    private String servername;

    public static final int SEARCH_FROM_DB = 0;
    public static final int SEARCH_FROM_SERVER = 1;
    public static final int SEARCH_FROM_SERVER_NAME = 2;

    private boolean mIsCancelSearch = true;

    private final String uriString = "content://com.hisilicon.explorer.serverprovider/samba";
    private static final String ID = "_id";
    private static final String SERVER_IP = "server_ip";

    private static final String NICK_NAME = "nick_name";

    private static final String SERVER_NAME = "server_name";

    private static final String WORK_PATH = "work_path";

    private static final String ACCOUNT = "account";

    private static final String PASSWORD = "password";

    public SambaServerLoader(Context mContext) {
        this.mContext = mContext;
        defaultItem = new SambaItemInfo();
        defaultItem.setIconId(R.drawable.mainfile);
        defaultItem.setType(SambaItemInfo.TYPE_SEARCH);
        defaultItem.setNickName(mContext.getResources().getString(R.string.all_network));
    }

    public void setiProgress(IProgress iProgress) {
        this.iProgress = iProgress;
    }

    public void cancelSearchSamba() {
        mIsCancelSearch = true;
    }

    @Override
    public void run() {
        super.run();
    }

    /**
     * 设置搜索类型 0 从数据库后去 1 从nfsserver获取
     *
     * @param searchType 设置搜索类型
     */
    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public void setServerServerName(String servername) {
        this.servername = servername;
    }

    @Override
    public List<SambaItemInfo> load() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                iProgress.onLoading();
            }
        });
        if (searchType == SEARCH_FROM_SERVER) {
            //仅仅给搜索使用，再次进入搜索将其值改为false
            mIsCancelSearch = false;
            return getDataFromSambaServer();
        } else if (searchType == SEARCH_FROM_SERVER_NAME) {
            return getDataFromSambaServerServerName(servername);
        } else {
            return getDataFromDataBase();
        }
    }

    public List<SambaItemInfo> getDataFromDataBase() {
        Uri parse = Uri.parse(uriString);
        List<SambaItemInfo> sambaItemInfos = new ArrayList<SambaItemInfo>();
        sambaItemInfos.add(defaultItem);
        Cursor cursor = mContext.getContentResolver().query(parse, new String[]{ID, NICK_NAME, SERVER_NAME, WORK_PATH, SERVER_IP, ACCOUNT, PASSWORD}, null, null, null);
        if (null != cursor) {
        while (cursor.moveToNext()) {
            SambaItemInfo sambaItemInfo = new SambaItemInfo();
            sambaItemInfo.setIconId(R.drawable.folder_file);
            //sambaItemInfo.setNickName("\\\\"+cursor.getString(cursor.getColumnIndex(NICK_NAME))+ "\\" +cursor.getString(cursor.getColumnIndex(WORK_PATH)));
            //添加为ip+workpath
            sambaItemInfo.setNickName(cursor.getString(cursor.getColumnIndex(NICK_NAME)));
            sambaItemInfo.setServerIp(cursor.getString(cursor.getColumnIndex(SERVER_IP)));
            sambaItemInfo.setShortId(cursor.getInt(cursor.getColumnIndex(ID)));
            sambaItemInfo.setAccount(cursor.getString(cursor.getColumnIndex(ACCOUNT)));
            sambaItemInfo.setPwd(cursor.getString(cursor.getColumnIndex(PASSWORD)));
            sambaItemInfo.setWorkPath(cursor.getString(cursor.getColumnIndex(WORK_PATH)));
            sambaItemInfo.setServerName(cursor.getString(cursor.getColumnIndex(SERVER_NAME)));
            sambaItemInfo.setType(SambaItemInfo.TYPE_ITEM);
            sambaItemInfos.add(sambaItemInfo);
        }
        cursor.close();
        }
        return sambaItemInfos;
    }

    /**
     * 通过smbtree获取所有的服务器
     *
     * @return 服务器列表
     */
    private List<SambaItemInfo> getSmbServers() {
        List<SambaItemInfo> list = new ArrayList<SambaItemInfo>();
        Uri uri = Uri.parse(Config.getInstance().SERVERPROVIDERURI);
        Bundle bundle = mContext.getContentResolver().call(uri, "getSmbServers", null, null);
        String workgroup = bundle.getString("smbservers");
        if (!TextUtils.isEmpty(workgroup)) {
            String[] workgroups = workgroup.split("\\|");
            // Dividing the information
            // CNcomment:对信息进行分割
            for (int i = 0; i < workgroups.length; i++) {
                SambaItemInfo sambaItemInfo = new SambaItemInfo();
                // Analytical information for each pc
                // CNcomment:对每个pc信息进行解析
                String[] details = workgroups[i].split(":");
                String trimStr = details[0].trim();
                String pcName = trimStr.substring(trimStr.lastIndexOf("\\") + 1, trimStr.length());
                sambaItemInfo.setNickName(pcName);
                sambaItemInfo.setServerName(pcName);
                sambaItemInfo.setType(SambaItemInfo.TYPE_ITEM);
                if (details.length == 2) {
                    sambaItemInfo.setInfos(details[1].trim());
                } else if (details.length == 1) {
                    sambaItemInfo.setInfos("No Details");
                }
                sambaItemInfo.setIconId(R.drawable.mainfile);
                list.add(sambaItemInfo);
            }
            return list;
        }
        return list;
    }

    /**
     * 通过获取到的服务器名通过smbtree获取其子目录
     *
     * @param servername 服务器名
     * @return 获取的的目录列表
     */
    private List<SambaItemInfo> getSmbShareFolders(String servername) {
        List<SambaItemInfo> list = new ArrayList<SambaItemInfo>();
        Bundle extras = new Bundle();
        extras.putString("servername", servername);
        Uri uri = Uri.parse(Config.getInstance().SERVERPROVIDERURI);
        Bundle bundle = mContext.getContentResolver().call(uri, "getSmbShareFolders", null, extras);
        String detailGroup = bundle.getString("getsharefolders");
        String[] details = detailGroup.split("\\|");
        String[] ipDetail = details[0].split(":");
        for (int i = 1; i < details.length; i++) {
            SambaItemInfo sambaItemInfo = new SambaItemInfo();
            sambaItemInfo.setServerIp(ipDetail[1]);
            String[] dirDetails = details[i].split(":");
            String dir = dirDetails[0].trim();
            String dirName = dir.substring(dir.lastIndexOf("\\") + 1, dir.length());
            sambaItemInfo.setWorkPath(dirName);
            sambaItemInfo.setNickName(dirName);
            sambaItemInfo.setServerName(servername);
            sambaItemInfo.setType(SambaItemInfo.TYPE_ITEM);
            sambaItemInfo.setIconId(R.drawable.folder_file);
            list.add(sambaItemInfo);
        }
        return list;
    }

    public List<SambaItemInfo> getDataFromSambaServer() {
        List<SambaItemInfo> smbServers = getSmbServers();
        if (mIsCancelSearch || searchType == SEARCH_FROM_DB) {
            return null;
        }
        return smbServers;
    }

    public List<SambaItemInfo> getDataFromSambaServerServerName(String severname) {
        List<SambaItemInfo> smbShareFolders = getSmbShareFolders(severname);
        if (searchType == SEARCH_FROM_DB) {
            return null;
        }
        return smbShareFolders;
    }

    @Override
    public void loadFail() {
        if (null != iProgress) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    iProgress.loadFail();
                }
            });
        }
    }

    @Override
    public void loadSuccess(List<SambaItemInfo> sambaItemInfos) {
        if (null != iProgress) {
            iProgress.loadSuccess(sambaItemInfos);
        }
    }
}
