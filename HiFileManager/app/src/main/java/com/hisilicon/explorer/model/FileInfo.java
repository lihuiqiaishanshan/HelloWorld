package com.hisilicon.explorer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 */

public class FileInfo implements Parcelable{

    private String path;
    public String displayName;
    private String mimeTypes;
    private long lastModified;
    private int defaultIcon;

    protected FileInfo(Parcel in) {
        path = in.readString();
        displayName = in.readString();
        mimeTypes = in.readString();
        lastModified = in.readLong();
        defaultIcon = in.readInt();
        size = in.readLong();
    }

    public static final Creator<FileInfo> CREATOR = new Creator<FileInfo>() {
        @Override
        public FileInfo createFromParcel(Parcel in) {
            return new FileInfo(in);
        }

        @Override
        public FileInfo[] newArray(int size) {
            return new FileInfo[size];
        }
    };

    public int getDefaultIcon() {
        return defaultIcon;
    }

    public void setDefaultIcon(int defaultIcon) {
        this.defaultIcon = defaultIcon;
    }

    private long size;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public FileInfo() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getMimeTypes() {
        return mimeTypes;
    }

    public void setMimeTypes(String mimeTypes) {
        this.mimeTypes = mimeTypes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(path);
        dest.writeString(displayName);
        dest.writeString(mimeTypes);
        dest.writeLong(lastModified);
        dest.writeInt(defaultIcon);
        dest.writeLong(size);
    }
}
