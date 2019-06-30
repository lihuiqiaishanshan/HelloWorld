/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.os.storage;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class representing a storage volume
 * @hide
 */
public class ExtraInfo implements Parcelable {
    public String mMountPoint;
    public String mUUID;
    public String mDevType;
    public int mDevNode;
    public String mDiskLabel;
    public String mLabel;

    public ExtraInfo () {
        mMountPoint = null;
        mUUID = null;
        mDevType = null;
        mDevNode = 0;
        mDiskLabel = null;
        mLabel = null;
    }

    private ExtraInfo(String mountPoint, String uuid, String devType, int devNode, String diskLabel, String label) {
        mMountPoint = mountPoint;
        mUUID = uuid;
        mDevType = devType;
        mDevNode = devNode;
        mDiskLabel = diskLabel;
        mLabel = label;
    }

    public static final Creator<ExtraInfo> CREATOR =
        new Creator<ExtraInfo>() {
            public ExtraInfo createFromParcel(Parcel in) {
                String mountPoint = in.readString();
                String uuid = in.readString();
                String devType = in.readString();
                int devNode = in.readInt();
                String diskLabel = in.readString();
                String label = in.readString();
                return new ExtraInfo(mountPoint, uuid, devType, devNode, diskLabel, label);
            }

            public ExtraInfo[] newArray(int size) {
                return new ExtraInfo[size];
            }
        };

    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(mMountPoint);
        parcel.writeString(mUUID);
        parcel.writeString(mDevType);
        parcel.writeInt(mDevNode);
        parcel.writeString(mDiskLabel);
        parcel.writeString(mLabel);
    }

    public int describeContents() {
        return 0;
    }
}
