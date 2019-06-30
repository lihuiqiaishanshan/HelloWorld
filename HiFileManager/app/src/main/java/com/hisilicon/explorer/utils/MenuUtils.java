package com.hisilicon.explorer.utils;


import android.content.Context;

import java.io.Serializable;

/**
 */

public class MenuUtils {

    private static Context mContext;

    private static final String FILE_FLTER_TYPE_KEY = "filefiltertypekey";
    private static final String FILE_SHOW_TYPE_KEY = "fileshowtypekey";
    private static final String FILE_SORT_TYPE_KEY = "filesorttypekey";

    public static void init(Context context) {
        mContext = context;
    }

    public static void setFileFilterType(int fileFilterType) {
        SPUtils.put(mContext, FILE_FLTER_TYPE_KEY, fileFilterType);
    }

    public static int getFileFilterType() {
        return (Integer) SPUtils.get(mContext,FILE_FLTER_TYPE_KEY,FileFilterType.ALL);
    }

    public static void setFileShowType(int fileShowType) {
        SPUtils.put(mContext, FILE_SHOW_TYPE_KEY, fileShowType);
    }

    public static int getFileShowType() {
        return (Integer) SPUtils.get(mContext,FILE_SHOW_TYPE_KEY,FileShowType.LIST);
    }

    public static void setFileSortType(int fileSortType) {
        SPUtils.put(mContext, FILE_SORT_TYPE_KEY, fileSortType);
    }

    public static int getFileSortType() {
        return (Integer) SPUtils.get(mContext,FILE_SORT_TYPE_KEY,FileSortType.DEFAULT);
    }

    public interface FileSortType{
        int DEFAULT = 0;
        int FILENAME = 1;
        int FILESIZE = 2;
        int LASTMODIFYTIME = 3;
    }

    public interface FileShowType{
        int LIST = 0;
        int THUMBNAIL = 1;
    }

    public interface FileFilterType{
        int ALL = 0;
        int IMAGE = 1;
        int AUDIO = 2;
        int VIDEO = 3;
        int OTHER = 4;
    }

    public interface FileOperationType{
        int COPY = 0;
        int CUT = 1;
        int DELETE = 2;
        int RENAME = 3;
        int PASTE = 4;
    }

}
