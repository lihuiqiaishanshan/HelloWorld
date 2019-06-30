// INFSAidlService.aidl
package com.hisilicon.explorer.service;
import java.util.List;
// Declare any non-default types here with import statements

interface INFSAidlService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    String getWorkgroups();
    String getShareFolders(String ip);
    boolean mount(String path, String mountPoint);
    boolean unmount(String mntPoint);
    String getMountPoint(String path);
}
