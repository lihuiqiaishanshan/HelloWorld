package com.hisilicon.explorer.utils;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.model.RootInfo;
import com.hisilicon.explorer.model.GroupInfo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 */

/**
 * 文件对象操作类
 */
public class FileInfoUtils {

    private static final HashMap<String, Integer> defaultIconMap = new HashMap<String, Integer>();

    private final static String IMG_MIMETYPE_START = "image";
    private final static String AUDIO_MIMETYPE_START = "audio";
    private final static String VIDEO_MIMETYPE_START = "video";
    private final static String DIERCTORY_MIMETYP_START = "directory";
    private final static String DVD_MIMETYPE = "video/dvd";
    private final static String BD_MIMETYPE = "video/bd";
    private final static String APK_MIMETYPE = "application/vnd.android.package-archive";
    private final static String UNKNOW_MIMETYP = "*/*";
    private final static int OTHER_FILE_ICON = R.drawable.otherfile;

    static {
        defaultIconMap.put(IMG_MIMETYPE_START, R.drawable.imgfile);
        defaultIconMap.put(AUDIO_MIMETYPE_START, R.drawable.mp3file);
        defaultIconMap.put(VIDEO_MIMETYPE_START, R.drawable.vediofile);
        defaultIconMap.put(DIERCTORY_MIMETYP_START, R.drawable.folder_file);
        defaultIconMap.put(DVD_MIMETYPE, R.drawable.dvdfile);
        defaultIconMap.put(BD_MIMETYPE, R.drawable.bdfile);
        defaultIconMap.put(APK_MIMETYPE, R.drawable.list);
        defaultIconMap.put(UNKNOW_MIMETYP, OTHER_FILE_ICON);
    }

    public static int getFileDefaultIcon(String mimetype) {
        if (defaultIconMap.containsKey(mimetype)) {
            return defaultIconMap.get(mimetype);
        } else {
            String mimeTypeStart = mimetype.split("/")[0];
            if (defaultIconMap.containsKey(mimeTypeStart)) {
                return defaultIconMap.get(mimeTypeStart);
            } else {
                return OTHER_FILE_ICON;
            }
        }
    }

    public static List<FileInfo> sortFileByModifyTime(final List<FileInfo> list) {
        Collections.sort(list, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                long rel = o1.getLastModified() - o2.getLastModified();
                if (rel > 0) {
                    return 1;
                }
                if (rel == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }

    public static List<FileInfo> sortFileBySize(List<FileInfo> list) {
        Collections.sort(list, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                long rel = o1.getSize() - o2.getSize();
                if (rel > 0) {
                    return 1;
                }
                if (rel == 0) {
                    return 0;
                } else {
                    return -1;
                }
            }
        });
        return list;
    }

    public static List<FileInfo> sortFileByName(List<FileInfo> list) {
        Collections.sort(list, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                File file1 = new File(o1.getPath());
                File file2 = new File(o2.getPath());
                if (file1.isDirectory()&&!file2.isDirectory()){
                    return -1;
                }else if (!file1.isDirectory()&&file2.isDirectory()){
                    return 1;
                }
                return o1.getDisplayName().compareTo(o2.getDisplayName());
            }
        });
        return list;
    }

    public static List<GroupInfo> sortFileByPath(List<GroupInfo> list){
        for (GroupInfo groupInfos:list){
            List<RootInfo> rootInfos = groupInfos.getRoots();
            Collections.sort(rootInfos, new Comparator<RootInfo>() {
                @Override
                public int compare(RootInfo o1, RootInfo o2) {
                    int i = o1.getPath().replaceAll("\\d+","").compareTo(o2.getPath().replaceAll("\\d+",""));
                    if (i == 0) {
                        return o1.getPath().length() - o2.getPath().length();
                    }
                    return i;
                }
            });
        }
        return list;
    }

}
