package com.hisilicon.explorer.loader;

/**
 */

public interface IProgress<T> {
    void onLoading();
    void loadSuccess(T a);
    void loadFail();
}
