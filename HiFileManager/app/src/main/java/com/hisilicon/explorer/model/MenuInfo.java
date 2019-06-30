package com.hisilicon.explorer.model;

/**
 */

public class MenuInfo {
    //当前menu的功能类型
    private int mMenuFuctionType;
    //当前menu的名字id
    private int resTitle;
    //当前menu的icon
    private int resIcon;
    //当前menu里边的文字资源id
    private int resArrays;

    public int getResIcon() {
        return resIcon;
    }

    public void setResIcon(int resIcon) {
        this.resIcon = resIcon;
    }

    public int getmMenuFuctionType() {
        return mMenuFuctionType;
    }

    public void setmMenuFuctionType(int mMenuFuctionType) {
        this.mMenuFuctionType = mMenuFuctionType;
    }

    public int getResTitle() {
        return resTitle;
    }

    public void setResTitle(int resTitle) {
        this.resTitle = resTitle;
    }

    public int getResArrays() {
        return resArrays;
    }

    public void setResArrays(int resArrays) {
        this.resArrays = resArrays;
    }

    public interface MenuFuctionType {
        //新建文件
        int NEW_FOLDER = 0;
        //搜索文件
        int SEARCH = 1;
        //过滤文件
        int FILE_FILTER = 2;
        //显示文件类型（列表或缩略图）
        int SHOW_STYLE = 3;
        //文件操作 PS:粘贴、复制、剪切、删除
        int OPTIONS = 4;
        //文件排序类型 PS:按时间排序、按大小排序、按名字排序
        int FILE_SORT = 5;
        //文件预览：仅仅显示文件名字 大小 文件类型 最后修改时间
        int PREVIEW = 6;
    }

    public interface  MenuFuctionTypeForItem{
        //新建item
        int NEW = 1;
        //编辑item
        int EDIT = 2;
        //删除item
        int DELETE = 3;
        //添加快速item
        int ADDSHORTCUT = 4;
    }

    public interface IMenuClick{
        void menuClick(int type,MenuInfo menuInfo);
    }

    //文件操作回调
    public interface IFileOperate {
        void menuNewFolder(String name);
        void menuSearchFile(String keywrod);
        void menuOperationFile(int type);
        void menuFilterFile(int type);
        void menuShowStyle(int type);
        void menuSortFile(int type);
        void menuPreview();
    }

    public interface IServerOperate<T> {
        void serverAdd(T t);
        void serverEdit(T t);
        void serverAddShortCut(T t);
    }
}
