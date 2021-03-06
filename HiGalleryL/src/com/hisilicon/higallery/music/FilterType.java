package com.hisilicon.higallery.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * music files' suffix
 */
public class FilterType {
    public static void filterTypeMusic(Context ctx) {
        SharedPreferences share = ctx.getSharedPreferences("OPERATE",
                Context.MODE_PRIVATE);
        Editor editor = share.edit();
        if (share.getBoolean("audioFlag", true)) {
            SharedPreferences.Editor shareAudio = ctx.getSharedPreferences(
                    "AUDIO", Context.MODE_PRIVATE).edit();
            shareAudio.putString("M3U", "M3U");
            shareAudio.putString("CUE", "CUE");
            shareAudio.putString("PLS", "PLS");
            shareAudio.putString("MP2", "MP2");
            shareAudio.putString("MP3", "MP3");
            shareAudio.putString("CDDA", "CDDA");
            shareAudio.putString("WMA", "WMA");
            shareAudio.putString("AAC", "AAC");
            shareAudio.putString("M4A", "M4A");
            shareAudio.putString("WAV", "WAV");
            shareAudio.putString("AIFF", "AIFF");
            shareAudio.putString("OGG", "OGG");
            shareAudio.putString("FLAC", "FLAC");
            shareAudio.putString("RA", "RA");
            shareAudio.putString("MKA", "MKA");
            shareAudio.putString("EC3", "EC3");
            shareAudio.putString("AC3", "AC3");
            shareAudio.putString("DTS", "DTS");
            shareAudio.putString("MLP", "MLP");
            shareAudio.putString("APE", "APE");
            shareAudio.putString("OGA", "OGA");
            shareAudio.putString("MID", "MID");
            shareAudio.putString("MIDI", "MIDI");
            shareAudio.putString("XMF", "XMF");
            shareAudio.putString("RTTTL", "RTTTL");
            shareAudio.putString("SMF", "SMF");
            shareAudio.putString("IMY", "IMY");
            shareAudio.putString("RTX", "RTX");
            shareAudio.putString("OTA", "OTA");
            shareAudio.putString("AMR", "AMR");
            shareAudio.putString("AWB", "AWB");

            shareAudio.putString("AEA", "AEA");
            shareAudio.putString("APC", "APC");
            shareAudio.putString("AU", "AU");
            shareAudio.putString("DAUD", "DAUD");
            shareAudio.putString("OMA", "OMA");
            shareAudio.putString("EAC3", "EAC3");
            shareAudio.putString("GSM", "GSM");
            shareAudio.putString("TRUEHD", "TRUEHD");
            shareAudio.putString("TTA", "TTA");
            shareAudio.putString("MPC", "MPC");
            shareAudio.putString("MPC8", "MPC8");

            editor.putBoolean("audioFlag", false);
            editor.commit();
            shareAudio.commit();
        }
    }
}
