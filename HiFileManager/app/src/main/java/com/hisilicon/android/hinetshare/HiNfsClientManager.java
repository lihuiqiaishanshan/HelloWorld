package com.hisilicon.android.hinetshare;

import com.hisilicon.android.hinetshare.Jni;

/**
* HiNfsClientManager interface<br>
*/
public class HiNfsClientManager
{
    public HiNfsClientManager() {
        init();
    }

    /**
     * Synchronize the client nfs list.
     */
    public int init() {
        if(Jni.existsAndLoad()) {
            return nfsInit();
        }
        return 0;
    }

    /**
     * Get server list string.
     * @param
     * @return ips separated by "|"<br>
     */
    public native String getWorkgroups();

    /**
     * Get server folders by given ip.
     * @param ip<br>
     * @return folders separated by "|"
     */
    public native String getShareFolders(String ip);

    /**
     * Mount nfs.
     * @param path format: ip/folder<br>
     * @return 0:success -1:fail
     */
    public native int mount(String path, String mountPoint);

    /**
     * Unmount nfs.
     * @param mntPoint geted by getMountPoint
     * @return 0:success -1:fail
     */
    public native int unmount(String mntPoint);

    /**
     * getMountPoint by path.
     * @param path format: ip/folder
     * @return mntPoint
     */
    public native String getMountPoint(String path);

    public static native final int nfsInit();
}
