package com.hisilicon.explorer.loader;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.utils.FileInfoUtils;
import com.hisilicon.explorer.utils.MenuUtils;
import com.hisilicon.explorer.utils.MimeTypes;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 建立对象
 * 设置路径(setLoadPath) ps：其中还可以设置过滤类型LoaderType 和 过滤的关键字filter key
 * 还可以设置排序方法(setSortType) 按时间排序，按名称排序，按大小排序
 * 然后调用run方法获取当前列表的所有文件
 */
public class FileInfoLoader extends BaseFileLoader<List<FileInfo>> {

    //需要加载文件路径
    private String root;
    //分类文件的类型默认是ALL
    private int mType;
    private IProgress<List<FileInfo>> iProgress;
    //文件过滤的关键字
    private String filterKey;
    //排序的类型
    private int sortType;
    //方便拿取类型进行分类的map
    private static HashMap<String, Integer> mimeTypeMap = new HashMap<String, Integer>();
    //取消文件加载
    private boolean mIsCancelFileLoader = false;

    private final static String IMG_MIMETYPE_START = "image";
    private final static String AUDIO_MIMETYPE_START = "audio";
    private final static String VIDEO_MIMETYPE_START = "video";

    //定义三种分类类型
    static {
        mimeTypeMap.put(AUDIO_MIMETYPE_START, MenuUtils.FileFilterType.AUDIO);
        mimeTypeMap.put(VIDEO_MIMETYPE_START, MenuUtils.FileFilterType.VIDEO);
        mimeTypeMap.put(IMG_MIMETYPE_START, MenuUtils.FileFilterType.IMAGE);
    }

    public FileInfoLoader(IProgress<List<FileInfo>> iProgress) {
        this.iProgress = iProgress;
    }

    /**
     * 设置加载路径setLoadPath
     * 设置排序类型setSortType
     * 然后调用run方法加载数据
     *
     * @param root      需要查看的根目录
     * @param type      加载的文件类型LoaderType
     * @param filterKey 加载需要过滤的关键字 为空即不过滤
     */
    public void setLoadPath(String root, int type, String filterKey) {
        this.root = root;
        this.mType = type;
        this.filterKey = filterKey;
    }

    /**
     * 设置排序类型
     *
     * @param sortType 设置的排序类型SortType
     */
    public void setSortType(int sortType) {
        this.sortType = sortType;
    }

    @Override
    public List<FileInfo> load() {
        if (iProgress != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    iProgress.onLoading();
                }
            });
        }
        List<FileInfo> fileInfos = getFileInfos();
        switch (sortType) {
            case MenuUtils.FileSortType.LASTMODIFYTIME:
                return FileInfoUtils.sortFileByModifyTime(fileInfos);
            case MenuUtils.FileSortType.FILESIZE:
                return FileInfoUtils.sortFileBySize(fileInfos);
            case MenuUtils.FileSortType.FILENAME:
                return FileInfoUtils.sortFileByName(fileInfos);
            default:
                return fileInfos;
        }
    }

    @Override
    public void run() {
        super.run();
        mIsCancelFileLoader = false;
    }

    @Override
    public void loadFail() {
        if (iProgress != null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    iProgress.loadFail();
                }
            });
        }
    }

    @Override
    public void loadSuccess(List<FileInfo> fileInfo) {
        if (iProgress != null) {
            iProgress.loadSuccess(fileInfo);
        }
    }

    /**
     * 先通过listFile获取文件，然后通过MimeTypes获取mimety然后根据mimeType设定默认的icon
     *
     * @return 已经分类好的文件
     */
    private List<FileInfo> getFileInfos() {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        if (!TextUtils.isEmpty(root)) {
            File rootFile = new File(root);
            if (rootFile.isDirectory()) {
                //获取过滤后的文件
                File[] files = rootFile.listFiles(new ClassfyFileFilter(mType, filterKey));
                if (null != files) {
                    for (File f : files) {
                        if (mIsCancelFileLoader) {
                            mIsCancelFileLoader = false;
                            return fileInfos;
                        }
                        FileInfo fileInfo = new FileInfo();
                        fileInfo.setDisplayName(f.getName());
                        fileInfo.setLastModified(f.lastModified());
                        fileInfo.setSize(f.length());
                        fileInfo.setPath(f.getPath());
                        fileInfo.setMimeTypes(MimeTypes.getMimeTypesFile(f));
                        fileInfo.setDefaultIcon(FileInfoUtils.getFileDefaultIcon(fileInfo.getMimeTypes()));
                        fileInfos.add(fileInfo);
                    }
                }
            }
        }
        return fileInfos;
    }

    public void cancelLoadFile() {
        mIsCancelFileLoader = true;
    }

    /**
     * 通过memetype过滤出文件
     */
    class ClassfyFileFilter implements FileFilter {

        private int type;
        private String key;

        public ClassfyFileFilter(int type, String key) {
            this.type = type;
            this.key = key;
        }

        @Override
        public boolean accept(File f) {
            if (f.isHidden()) {
                return false;
            }
            if (type == MenuUtils.FileFilterType.ALL || f.isDirectory()) {
                return true;
            }
            String path = f.getPath();
            String mimeTypeFromPath = MimeTypes.getMimeTypeFromPath2(path);
            if (TextUtils.isEmpty(mimeTypeFromPath)) {
                mimeTypeFromPath = MimeTypes.getMimeTypeByStream(path);
            }
            if (!TextUtils.isEmpty(mimeTypeFromPath)) {
                String[] split = mimeTypeFromPath.split("/");
                int temptype;
                if (mimeTypeMap.get(split[0]) == null) {
                    temptype = MenuUtils.FileFilterType.OTHER;
                } else {
                    temptype = mimeTypeMap.get(split[0]);
                }
                if (!TextUtils.isEmpty(key)) {
                    if (path.contains(key) && type == temptype) {
                        return true;
                    }
                } else {
                    if (type == temptype) {
                        return true;
                    }
                }

            }
            return false;
        }
    }

    /**
     * 排序类型
     *//*
    public enum LoaderSortType {
        DEFAULT,
        FILENAME,
        FILESIZE,
        LASTMODIFYTIME
    }

    *//**
     * 分类类型
     *//*
    public enum LoaderType {
        ALL,
        IMAGE,
        AUDIO,
        VIDEO
    }*/
}
