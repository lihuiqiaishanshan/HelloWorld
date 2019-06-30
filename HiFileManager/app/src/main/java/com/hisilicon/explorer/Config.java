package com.hisilicon.explorer;

import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.utils.MenuUtils;
import com.hisilicon.explorer.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 */

public class Config {

    private boolean mIsIPTV = false;
    private int mThreadPollSize = 10;
    private boolean mIsDebug = true;
    private static Config mConfig = null;
    //默认用文件名排序
    private int fileSortType = MenuUtils.FileSortType.FILENAME;
    private int fileFilterType;
    private int fileShowType;
    private boolean mIsHiDPT;
    private List<MenuInfo> menuInfoList;
    //for file cut and copy
    //copy or cut
    private int fileOperateType;
    private List<FileInfo> fileOperateList;
    //操作service的provider地址
    public final String SERVERPROVIDERURI = "content://com.hisilicon.explorer.serverprovider";

    public int getFileOperateType() {
        return fileOperateType;
    }

    public void setFileOperateType(int fileOperateType) {
        this.fileOperateType = fileOperateType;
    }

    public List<FileInfo> getFileOperateList() {
        return fileOperateList;
    }

    public void setFileOperateList(List<FileInfo> fileOperateList) {
        this.fileOperateList = fileOperateList;
    }

    public List<MenuInfo> getMenuInfoList() {
        return menuInfoList;
    }

    public boolean ismIsHiDPT() {
        return mIsHiDPT;
    }

    public void setmIsHiDPT(boolean mIsHiDPT) {
        this.mIsHiDPT = mIsHiDPT;
    }

    private Config() {
        configFileMenu();
    }

    public static Config getInstance() {
        synchronized (Config.class) {
            if (mConfig == null) {
                mConfig = new Config();
            }
            return mConfig;
        }
    }

    public boolean ismIsDebug() {
        return mIsDebug;
    }

    public void setIsDebug(boolean debug) {
        this.mIsDebug = debug;
    }

    public int getmThreadPollSize() {
        return mThreadPollSize;
    }

    public void setmThreadPollSize(int mThreadPollSize) {
        this.mThreadPollSize = mThreadPollSize;
    }

    public boolean ismIsIPTV() {
        return mIsIPTV;
    }

    public void setmIsIPTV(boolean mIsIPTV) {
        this.mIsIPTV = mIsIPTV;
    }

    public int getFileShowType() {
        return fileShowType;
    }

    public void setFileShowType(int fileShowType) {
        this.fileShowType = fileShowType;
    }

    public int getFileFilterType() {
        return fileFilterType;
    }

    public void setFileFilterType(int fileFilterType) {
        this.fileFilterType = fileFilterType;
    }

    public int getFileSortType() {
        return fileSortType;
    }

    public void setFileSortType(int fileSortType) {
        this.fileSortType = fileSortType;
    }

    /**
     * 配置menu显示对象
     */
    private void configFileMenu() {
        menuInfoList = new ArrayList<MenuInfo>();

        //文件操作
        MenuInfo oprationMenuInfo = new MenuInfo();
        oprationMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.OPTIONS);
        oprationMenuInfo.setResTitle(R.string.operation);
        oprationMenuInfo.setResArrays(R.array.operate_method);
        oprationMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //新建文件
        MenuInfo newFileMenuInfo = new MenuInfo();
        newFileMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.NEW_FOLDER);
        newFileMenuInfo.setResTitle(R.string.new_dir);
        newFileMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //搜索文件
        MenuInfo searchMenuInfo = new MenuInfo();
        searchMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.SEARCH);
        searchMenuInfo.setResTitle(R.string.search);
        searchMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //过滤文件
        MenuInfo filterMenuInfo = new MenuInfo();
        filterMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.FILE_FILTER);
        filterMenuInfo.setResTitle(R.string.filter_but);
        filterMenuInfo.setResArrays(R.array.filter_method);
        filterMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //显示文件方式
        MenuInfo showStyleMenuInfo = new MenuInfo();
        showStyleMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.SHOW_STYLE);
        showStyleMenuInfo.setResTitle(R.string.show_but);
        showStyleMenuInfo.setResArrays(R.array.show_method);
        showStyleMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //文件排序
        MenuInfo fileSortMenuInfo = new MenuInfo();
        fileSortMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.FILE_SORT);
        fileSortMenuInfo.setResTitle(R.string.sort_but);
        fileSortMenuInfo.setResArrays(R.array.sort_method);
        fileSortMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        //文件预览
        MenuInfo previewMenuInfo = new MenuInfo();
        previewMenuInfo.setmMenuFuctionType(MenuInfo.MenuFuctionType.PREVIEW);
        previewMenuInfo.setResTitle(R.string.preview);
        previewMenuInfo.setResIcon(R.drawable.menu_fullscreen);

        if (Utils.isIptvEnable()) {
            menuInfoList.add(oprationMenuInfo);
            menuInfoList.add(newFileMenuInfo);
            menuInfoList.add(searchMenuInfo);
            menuInfoList.add(filterMenuInfo);
            menuInfoList.add(showStyleMenuInfo);
            menuInfoList.add(fileSortMenuInfo);
            menuInfoList.add(previewMenuInfo);
        } else {
            menuInfoList.add(newFileMenuInfo);
            menuInfoList.add(searchMenuInfo);
            menuInfoList.add(filterMenuInfo);
            menuInfoList.add(showStyleMenuInfo);
        }
    }

    public void refreshPastMenuList() {
        if (Utils.isIptvEnable()) {
            menuInfoList.get(0).setResArrays(R.array.operate_method_paste);
        }
    }

    public void refreshNormalMenuList() {
        if (Utils.isIptvEnable()) {
            menuInfoList.get(0).setResArrays(R.array.operate_method);
        }
    }


}
