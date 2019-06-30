package com.hisilicon.explorer.model;

/**
 */

public class RootInfo {
    private String mUUID;
    private String mDevType;
    private String mLabel;
    private String path;
    private String mAlias;

    public RootInfo() {
    }

    public RootInfo(String mUUID, String mDevType, String mLabel, String path) {
        this.mUUID = mUUID;
        this.mDevType = mDevType;
        this.mLabel = mLabel;
        this.path = path;
    }

    public String getmUUID() {
        return mUUID;
    }

    public void setmUUID(String mUUID) {
        this.mUUID = mUUID;
    }

    public String getmDevType() {
        return mDevType;
    }

    public void setmDevType(String mDevType) {
        this.mDevType = mDevType;
    }

    public String getmLabel() {
        return mLabel;
    }

    public void setmLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getmAlias() {
        return mAlias;
    }

    public void setmAlias(String mAlias) {
        this.mAlias = mAlias;
    }
}
