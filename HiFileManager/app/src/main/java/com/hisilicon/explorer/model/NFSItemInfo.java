package com.hisilicon.explorer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 */

public class NFSItemInfo extends BaseServerInfo  implements Parcelable{
    private int iconId;
    private String nickName;
    private String serverIp;
    private String workPath;
    private String mountPoint;
    private String infos;

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    private int shortId;
    private int type;

    //表明这个对象点击后是search操作
    public static final int TYPE_SEARCH = 0;
    //表明这个是个普通的item
    public static final int TYPE_ITEM = 1;

    public NFSItemInfo() {
    }

    protected NFSItemInfo(Parcel in) {
        iconId = in.readInt();
        nickName = in.readString();
        serverIp = in.readString();
        workPath = in.readString();
        mountPoint = in.readString();
        shortId = in.readInt();
        type = in.readInt();
        infos = in.readString();
    }

    public static final Creator<NFSItemInfo> CREATOR = new Creator<NFSItemInfo>() {
        @Override
        public NFSItemInfo createFromParcel(Parcel in) {
            return new NFSItemInfo(in);
        }

        @Override
        public NFSItemInfo[] newArray(int size) {
            return new NFSItemInfo[size];
        }
    };

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getIconId() {
        return iconId;
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getWorkPath() {
        return workPath;
    }

    public void setWorkPath(String workPath) {
        this.workPath = workPath;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public int getShortId() {
        return shortId;
    }

    public void setShortId(int shortId) {
        this.shortId = shortId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(iconId);
        dest.writeString(nickName);
        dest.writeString(serverIp);
        dest.writeString(workPath);
        dest.writeString(mountPoint);
        dest.writeInt(shortId);
        dest.writeInt(type);
        dest.writeString(infos);
    }
}
