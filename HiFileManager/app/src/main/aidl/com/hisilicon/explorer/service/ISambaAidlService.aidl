// ISambaAidlService.aidl
package com.hisilicon.explorer.service;

// Declare any non-default types here with import statements

interface ISambaAidlService {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
    String getWorkgroups();
    boolean UImount(String address, String workpath,String mountpoint,String user,String password);
    boolean myUmount(String path);
    String getMountPoint(String path);
    String getDetailsBy(String servername,String account,String pwd);
}
