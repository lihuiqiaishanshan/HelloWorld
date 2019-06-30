package com.download;

public interface DownLoadListener {
    void onLoadSuccess(String filePath);
    void onLoadFailed(String filePath,Exception e);
}

