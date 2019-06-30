package com.hisilicon.android.videoplayer.utils;
/**
 * Judging what player is used to achieve
 */
public class PlayerJugdment {
    private static final String KEY_TRUE = "true";
    private static final String KEY_FALSE = "false";

    /**
     * HiPLayer
     * @return
     */
    public static boolean isHiPlayer() {
        return KEY_TRUE.equals(SystemProperties.get("media.hp.ff.hiplayer", "false"));
    }
    /**
     * Gstreamer
     * @return
     */
    public static boolean isGstreamer() {
        return KEY_TRUE.equals(SystemProperties.get("media.gst.enable", "false"));
    }
}
