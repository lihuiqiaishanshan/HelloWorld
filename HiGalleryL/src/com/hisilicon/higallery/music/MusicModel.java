package com.hisilicon.higallery.music;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * MusicModel
 */
public class MusicModel implements Parcelable {
    private int id;
    private String path;
    private String title;
    private long size;
    private long AddedTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getAddedTime() {
        return AddedTime;
    }

    public void setAddedTime(long addedTime) {
        AddedTime = addedTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(path);
        out.writeString(title);
        out.writeLong(size);
        out.writeLong(AddedTime);
    }

    public static final Parcelable.Creator<MusicModel> CREATOR
             = new Parcelable.Creator<MusicModel>() {
        @Override
        public MusicModel createFromParcel(Parcel in) {
            MusicModel musicModel = new MusicModel();
            musicModel.setId(in.readInt());
            musicModel.setPath(in.readString());
            musicModel.setTitle(in.readString());
            musicModel.setSize(in.readLong());
            musicModel.setAddedTime(in.readLong());
            return musicModel;
        }

        @Override
        public MusicModel[] newArray(int size) {
            return new MusicModel[size];
        }
    };
}
