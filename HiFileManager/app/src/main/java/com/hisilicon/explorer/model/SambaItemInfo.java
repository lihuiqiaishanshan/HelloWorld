package com.hisilicon.explorer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 */

public class SambaItemInfo extends BaseServerInfo implements Parcelable{
    //图标资源文件
    private int iconId;
    //显示的title
    private String nickName;
    //服务器ip
    private String serverIp;
    //工作路径
    private String workPath;
    //挂载节点
    private String mountPoint;
    private String account;
    private String pwd;
    //服务器名
    private String serverName;
    //信息
    private String infos;

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

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getInfos() {
        return infos;
    }

    public void setInfos(String infos) {
        this.infos = infos;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getShortId() {
        return shortId;
    }

    public void setShortId(int shortId) {
        this.shortId = shortId;
    }

    private int type;
    private int shortId;

    public SambaItemInfo() {
    }

    //表明这个对象点击后是search操作
    public static final int TYPE_SEARCH = 0;
    //表明这个是个普通的item
    public static final int TYPE_ITEM = 1;

    protected SambaItemInfo(Parcel in) {
        iconId = in.readInt();
        nickName = in.readString();
        serverIp = in.readString();
        workPath = in.readString();
        mountPoint = in.readString();
        account = in.readString();
        pwd = in.readString();
        serverName = in.readString();
        infos = in.readString();
        type = in.readInt();
        shortId = in.readInt();
    }

    public static final Creator<SambaItemInfo> CREATOR = new Creator<SambaItemInfo>() {
        @Override
        public SambaItemInfo createFromParcel(Parcel in) {
            return new SambaItemInfo(in);
        }

        @Override
        public SambaItemInfo[] newArray(int size) {
            return new SambaItemInfo[size];
        }
    };

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
        dest.writeString(account);
        dest.writeString(pwd);
        dest.writeString(serverName);
        dest.writeString(infos);
        dest.writeInt(type);
        dest.writeInt(shortId);
    }
}
