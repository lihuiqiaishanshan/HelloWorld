package com.hisilicon.android.videoplayer.menu;

import android.annotation.IdRes;

/**
 * Created on 2018/6/29.
 */

public class MenuKeyValuePair {
    /**
     * 判断Menu功能的Key
     */
    private String key;
    /**
     * 菜单功能ResId
     */
    private int valueResId;
//    /**
//     * 英文功能菜单（除中文外的其他语言用英文表示）
//     */
//    private String valueOther;
//    /**
//     * 中文功能菜单
//     */
//    private String valueCN;

    private int imgResId = -1;

    public MenuKeyValuePair(String key, @IdRes int resID) {
        this.key = key;
        this.valueResId = resID;
    }

    public MenuKeyValuePair(String key, @IdRes int resID, int imgResId) {
        this.key = key;
        this.valueResId = resID;
        this.imgResId = imgResId;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getValueResId() {
        return valueResId;
    }

    public void setValueResId(int valueResId) {
        this.valueResId = valueResId;
    }

    public int getImgResId() {
        return imgResId;
    }

    public void setImgResId(int imgResId) {
        this.imgResId = imgResId;
    }
}
