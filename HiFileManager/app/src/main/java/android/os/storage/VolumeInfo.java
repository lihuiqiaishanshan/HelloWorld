/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;
import android.provider.DocumentsContract;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.DebugUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.io.CharArrayWriter;
import java.io.File;
import java.util.Comparator;
import java.util.Objects;

/**
 * Information about a storage volume that may be mounted. A volume may be a
 * partition on a physical {@link DiskInfo}, an emulated volume above some other
 * storage medium, or a standalone container like an ASEC or OBB.
 * <p>
 * Volumes may be mounted with various flags:
 * <ul>
 * <li>{@link #MOUNT_FLAG_PRIMARY} means the volume provides primary external
 * storage, historically found at {@code /sdcard}.
 * <li>{@link #MOUNT_FLAG_VISIBLE} means the volume is visible to third-party
 * apps for direct filesystem access. The system should send out relevant
 * storage broadcasts and index any media on visible volumes. Visible volumes
 * are considered a more stable part of the device, which is why we take the
 * time to index them. In particular, transient volumes like USB OTG devices
 * <em>should not</em> be marked as visible; their contents should be surfaced
 * to apps through the Storage Access Framework.
 * </ul>
 *
 * @hide
 */
public class VolumeInfo implements Parcelable {
    public static final String ACTION_VOLUME_STATE_CHANGED =
            "android.os.storage.action.VOLUME_STATE_CHANGED";
    public static final String EXTRA_VOLUME_ID =
            "android.os.storage.extra.VOLUME_ID";
    public static final String EXTRA_VOLUME_STATE =
            "android.os.storage.extra.VOLUME_STATE";

    /** Stub volume representing internal private storage */
    public static final String ID_PRIVATE_INTERNAL = "private";
    /** Real volume representing internal emulated storage */
    public static final String ID_EMULATED_INTERNAL = "emulated";

    public static final int TYPE_PUBLIC = 0;
    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_EMULATED = 2;
    public static final int TYPE_ASEC = 3;
    public static final int TYPE_OBB = 4;

    public static final int STATE_UNMOUNTED = 0;
    public static final int STATE_CHECKING = 1;
    public static final int STATE_MOUNTED = 2;
    public static final int STATE_MOUNTED_READ_ONLY = 3;
    public static final int STATE_FORMATTING = 4;
    public static final int STATE_EJECTING = 5;
    public static final int STATE_UNMOUNTABLE = 6;
    public static final int STATE_REMOVED = 7;
    public static final int STATE_BAD_REMOVAL = 8;

    public static final int MOUNT_FLAG_PRIMARY = 1 << 0;
    public static final int MOUNT_FLAG_VISIBLE = 1 << 1;

    private static SparseArray<String> sStateToEnvironment = new SparseArray<String>();
    private static ArrayMap<String, String> sEnvironmentToBroadcast = new ArrayMap<String, String>();
    private static SparseIntArray sStateToDescrip = new SparseIntArray();

    /** vold state */
    public final String id;
    public final int type;
    public final DiskInfo disk;
    public final String partGuid;
    public int mountFlags = 0;
    public int mountUserId = -1;
    public int state = STATE_UNMOUNTED;
    public String fsType;
    public String fsUuid;
    public String fsLabel;
    public String path;
    public String internalPath;

    //HISILICON add begin
    //VolumeInfo, add devType. devType means device type. such as: USB2.0, USB3.0, SDCARD, SATA
    public String devType;
    //HISILICON add end

    public VolumeInfo(String id, int type, DiskInfo disk, String partGuid) {
        this.id = checkNotNull(id);
        this.type = type;
        this.disk = disk;
        this.partGuid = partGuid;
    }

    //HISILICON add begin
    //VolumeInfo ,Add param devType into VolumeInfo constrcture function.
    public VolumeInfo(String id, int type, DiskInfo disk, String partGuid, String devType) {
        this.id = checkNotNull(id);
        this.type = type;
        this.disk = disk;
        this.partGuid = partGuid;
        this.devType = devType;
    }
    //HISILICON add end

    public VolumeInfo(Parcel parcel) {
        id = parcel.readString();
        type = parcel.readInt();
        if (parcel.readInt() != 0) {
            disk = DiskInfo.CREATOR.createFromParcel(parcel);
        } else {
            disk = null;
        }
        partGuid = parcel.readString();
        mountFlags = parcel.readInt();
        mountUserId = parcel.readInt();
        state = parcel.readInt();
        fsType = parcel.readString();
        fsUuid = parcel.readString();
        fsLabel = parcel.readString();
        path = parcel.readString();
        internalPath = parcel.readString();

        //HISILICON add begin
        //VolumeInfo ,read devType from parcel.
        devType = parcel.readString();
        //HISILICON add end
    }

    private <T> T checkNotNull(final T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }


    @Override
    public String toString() {
        final CharArrayWriter writer = new CharArrayWriter();
        return writer.toString();
    }


    @Override
    public VolumeInfo clone() throws CloneNotSupportedException {
        super.clone();
        final Parcel temp = Parcel.obtain();
        try {
            writeToParcel(temp, 0);
            temp.setDataPosition(0);
            return CREATOR.createFromParcel(temp);
        } finally {
            temp.recycle();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof VolumeInfo) {
            return Objects.equals(id, ((VolumeInfo) o).id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public static final Creator<VolumeInfo> CREATOR = new Creator<VolumeInfo>() {
        @Override
        public VolumeInfo createFromParcel(Parcel in) {
            return new VolumeInfo(in);
        }

        @Override
        public VolumeInfo[] newArray(int size) {
            return new VolumeInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(id);
        parcel.writeInt(type);
        if (disk != null) {
            parcel.writeInt(1);
            disk.writeToParcel(parcel, flags);
        } else {
            parcel.writeInt(0);
        }
        parcel.writeString(partGuid);
        parcel.writeInt(mountFlags);
        parcel.writeInt(mountUserId);
        parcel.writeInt(state);
        parcel.writeString(fsType);
        parcel.writeString(fsUuid);
        parcel.writeString(fsLabel);
        parcel.writeString(path);
        parcel.writeString(internalPath);

        //HISILICON add begin
        //VolumeInfo , write devType into parcel.
        parcel.writeString(devType);
        //HISILICON Add end
    }
}
