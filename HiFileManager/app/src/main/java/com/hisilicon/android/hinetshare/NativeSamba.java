package com.hisilicon.android.hinetshare;

import com.hisilicon.android.hinetshare.Jni;

/**
* NativeSamba interface<br>
*/
public class NativeSamba {

    public NativeSamba() {
       Jni.existsAndLoad();
    }

    /**
     * start samba server.
     * @return int result 0:  sucess -1:  failed<br>
     */
    public native int startSambaServer();

    /**
     * stop samba server.
     * @return int result 0: sucess -1: failed<br>
     */
    public native int stopSambaServer();

    /**
     * set user.
     * @return int result 0: sucess -1: failed<br>
     */
    public native int setUser(String user);

    /**
     * set user's psd.
     * @return int result 0: sucess -1: failed<br>
     */
    public native int setPasswd(String passwd);

    /**
     * Set global parameters.
     * @param workgroup<br>
     * @param server_string<br>
     * @param security<br>
     * @param maxLogSize<br>
     * @param netBiosName<br>
     * @return
     */
    public native int setGlobal(String workgroup, String server_string, String security,
            int maxLogSize, String netBiosName);

    /**
     * New share.
     * @param head<br>
     * @param path<br>
     * @param createMask<br>
     * @param directoryMask<br>
     * @param available<br>
     * @param browseable<br>
     * @param writable<br>
     * @param validUsers<br>
     * @return
     */
    public native int addProperty(String head, String path, String createMask,
            String directoryMask, String available, String browseable, String writable,
            String validUsers);

    /**
     * Compiling shared configuration.
     * @param head<br>
     * @param path<br>
     * @param createMask<br>
     * @param directoryMask<br>
     * @param available<br>
     * @param browseable<br>
     * @param writable<br>
     * @param validUsers<br>
     * @return
     */
    public native int editShare(String head, String path, String createMask, String directoryMask,
            String available, String browseable, String writable, String validUsers);

    /**
     * Delete the configuration.
     * @param head Sharing the file name<br>
     * @return
     */
    public native int deleteShare(String head);

    /**
     * get Shared file name.
     * @return Shared file name<br>
     */
    public native String getProperty();

    /**
     * get Configuration parameters.
     * @param head Sharing the file name<br>
     * @return Configuration parameters<br>
     */
    public native String getParameter(String head);

    /**
     * get Samba process turned.
     * @return int 0 Indicates that the process is not open
     *             1 Indicates that the process is open<br>
     */
    public native int getSambaProcessState();

}
