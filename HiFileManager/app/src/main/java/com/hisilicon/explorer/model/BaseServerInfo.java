package com.hisilicon.explorer.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 */

public class BaseServerInfo implements Parcelable{
    private int iconId;
    private String nickName;

    public BaseServerInfo() {
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

    protected BaseServerInfo(Parcel in) {
        iconId = in.readInt();
        nickName = in.readString();
    }

    public static final Creator<BaseServerInfo> CREATOR = new Creator<BaseServerInfo>() {
        @Override
        public BaseServerInfo createFromParcel(Parcel in) {
            return new BaseServerInfo(in);
        }

        @Override
        public BaseServerInfo[] newArray(int size) {
            return new BaseServerInfo[size];
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
    }
}
