package com.hisilicon.android.hinetshare;

import com.hisilicon.android.hinetshare.Jni;

/**
* SambaTreeNative interface<br>
*/
public class SambaTreeNative {

    public SambaTreeNative() {
        Jni.existsAndLoad();
    }

    /**
    * Get server list string.
    * @return servers
    */
    public native String getWorkgroups();

    /**
     * Obtained according to the specific working group.
     * @param workgroup<br>
     * @param name<br>
     * @param pass<br>
     * @return servers<br>
     */
    public native String getDetailsBy(String workgroup, String name, String pass);
}
