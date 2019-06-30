package com.hisilicon.android.videoplayer.activity.adapter;

import com.hisilicon.android.videoplayer.utils.SystemProperties;

/**
 * Created on 2018/6/29.
 */

public class VersionControl {
    /**
     * 系统Brand
     */
    public final static String PRODUCT_BRAND = "ro.product.brand";

    /**
     * Dpt brand
     */
    public final static String BRAND_HIDPT = "HiDPT";
    /**
     * Stb brand
     */
    public final static String BRAND_HISTP = "HiSTB";


    public static boolean isIptvEnable() {
        String iptvEnable = SystemProperties.get("ro.product.target", "aosp");

        if ("telecom".equals(iptvEnable) || "unicom".equals(iptvEnable))
            return true;
        else
            return false;
    }

    public static boolean isOttEnable() {
        String ottEnable = SystemProperties.get("ro.product.target", "aosp");

        if ("ott".equals(ottEnable))
            return true;
        else
            return false;
    }
}
