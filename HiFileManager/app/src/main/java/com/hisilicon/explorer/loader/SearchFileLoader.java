package com.hisilicon.explorer.loader;

import android.os.Handler;
import android.os.Looper;

import com.hisilicon.explorer.model.FileInfo;
import com.hisilicon.explorer.utils.FileInfoUtils;
import com.hisilicon.explorer.utils.FileUtils;
import com.hisilicon.explorer.utils.MimeTypes;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */

public class SearchFileLoader extends BaseFileLoader<List<FileInfo>> {
    private String dirPath;
    private String key;
    private boolean BREAKSEARCH = false;
    private IProgress<List<FileInfo>> iProgress;

    public SearchFileLoader(String dirPath, String key) {
        this.dirPath = dirPath;
        this.key = key;
    }

    public void setSearchFileInfoLoaderLinstenter(IProgress<List<FileInfo>> iProgress) {
        this.iProgress = iProgress;
    }

    @Override
    public List<FileInfo> load() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                iProgress.onLoading();
            }
        });
        return searchFiles(dirPath, key);
    }

    private List<FileInfo> searchFiles(String dirPath, String key) {
        File dir = new File(dirPath);
        List<FileInfo> result = new ArrayList();
        FileUtils.SearchFilter filter = new FileUtils.SearchFilter(key);
        File[] filesFiltered = dir.listFiles(filter), filesAll = dir.listFiles();
        if (filesFiltered != null) {
            List<File> files = Arrays.asList(filesFiltered);
            ArrayList<FileInfo> tempFileInfo = new ArrayList<FileInfo>();
            for (File f : files) {
                FileInfo fileInfo = new FileInfo();
                fileInfo.setDisplayName(f.getName());
                fileInfo.setLastModified(f.lastModified());
                fileInfo.setSize(f.length());
                fileInfo.setPath(f.getPath());
                fileInfo.setMimeTypes(MimeTypes.getMimeTypesFile(f));
                fileInfo.setDefaultIcon(FileInfoUtils.getFileDefaultIcon(fileInfo.getMimeTypes()));
                tempFileInfo.add(fileInfo);
            }
            result.addAll(tempFileInfo);
            if (BREAKSEARCH) {
                return result;
            }
        }
        if (filesAll != null) {
            for (File file : filesAll) {
                if (file.isDirectory()) {
                    List<FileInfo> deeperList = searchFiles(file.getPath(), key);
                    result.addAll(deeperList);
                    if (BREAKSEARCH) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public void cancelSearch() {
        BREAKSEARCH = true;
    }

    @Override
    public void run() {
        super.run();
        BREAKSEARCH = false;
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
    public void loadSuccess(List<FileInfo> fileInfos) {
        if (iProgress != null) {
            iProgress.loadSuccess(fileInfos);
        }
    }
}
