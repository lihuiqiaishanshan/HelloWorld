
package com.hisilicon.launcher.interfaces;

import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.android.tvapi.Audio;
import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.android.tvapi.constant.EnumSoundEftParam;
import com.hisilicon.android.tvapi.constant.EnumSoundAdvEftParam;

public class AudioInterface {

    private static final String TAG = "AudioInterface";

    /**
     * set external amplifier MUTE
     */
    public static Audio getAudioManager() {
        return HitvManager.getInstance().getAudio();
    }

    public static int enableAmplifierMute(boolean bmute) {
        LogHelper.d(TAG, "enableAmplifierMute(boolean bmute = " + bmute
                + ") begin");

        int value = getAudioManager().enableAmplifierMute(bmute);

        LogHelper.d(TAG, "enableAmplifierMute(boolean bmute = " + bmute
                + ") end value = " + value);
        return value;
    }

    /**
     * set the ARC switch
     */
    public static int enableARC(boolean onoff) {
        LogHelper.d(TAG, "enableARC(boolean onoff = " + onoff + ") begin");

        int value = getAudioManager().enableARC(onoff);

        LogHelper.d(TAG, "enableARC(boolean onoff = " + onoff + ") end value = "
                + value);
        return value;
    }

    /**
     * set the volume switch
     */
    public static int enableAVC(boolean onoff) {
        LogHelper.d(TAG, "enableAVC(boolean onoff = " + onoff + ") begin");

        int value = getAudioManager().setEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_AVC_ONOFF, onoff ? 1 : 0);

        LogHelper.d(TAG, "enableAVC(boolean onoff = " + onoff + ") end value = "
                + value);
        return value;
    }

    /**
     * Get AVC Enable
     */
    public static boolean isAVCEnable() {
        LogHelper.d(TAG, "isAVCEnable() begin");

        boolean value = getAudioManager().getEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_AVC_ONOFF) == 1 ? true : false;

        LogHelper.d(TAG, "isAVCEnable() end value = " + value);
        return value;
    }

    /**
     * sets the SRS audio switch
     */
    public static int EnableSRS(boolean onoff) {
        LogHelper.d(TAG, "enableSRS(boolean onoff = " + onoff + ") begin");

        int value = getAudioManager().setAdvancedEffectParameter(EnumSoundAdvEftParam.E_SRS_ONOFF,
                onoff ? 1 : 0);

        LogHelper.d(TAG, "enableSRS(boolean onoff = " + onoff + ") end value = "
                + value);
        return value;
    }

    /**
     * Get SRS Enable
     */
    public static boolean isSRSEnable() {
        LogHelper.d(TAG, "isSRSEnable() begin");

        boolean value = getAudioManager().getAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_SRS_ONOFF) == 1 ? true : false;

        LogHelper.d(TAG, "isSRSEnable() end value = " + value);
        return value;
    }

    /**
     * set the SRS curve bass switch
     */
    public static int enableSRSBass(boolean onoff) {
        LogHelper.d(TAG, "enableSRSBass(boolean onoff = " + onoff + ") begin");

        int value = getAudioManager().setAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_SRS_TRUEBASS_ONOFF, onoff ? 1 : 0);

        LogHelper.d(TAG, "enableSRSBass(boolean onoff = " + onoff
                + ") end value = " + value);
        return value;
    }

    /**
     * Get SRS Base Enable
     */
    public static boolean isSRSBassEnable() {
        LogHelper.d(TAG, "isSRSBassEnable() begin");

        boolean value = getAudioManager().getAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_SRS_TRUEBASS_ONOFF) == 1 ? true : false;

        LogHelper.d(TAG, "isSRSBassEnable() end value = " + value);
        return value;
    }

    /**
     * set the SRS curve treble switch
     */
    public static int enableSRSTreble(boolean onoff) {
        LogHelper.d(TAG, "enableSRSTreble(boolean onoff = " + onoff + ") begin");

        int value = getAudioManager().setAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_SRS_TRUEDIALOG_ONOFF, onoff ? 1 : 0);

        LogHelper.d(TAG, "enableSRSTreble(boolean onoff = " + onoff
                + ") end value = " + value);
        return value;
    }

    /**
     * Get SRS Treble Enable
     */
    public static boolean isSRSTrebleEnable() {
        LogHelper.d(TAG, "isSRSTrebleEnable() begin");

        boolean value = getAudioManager().getAdvancedEffectParameter(
                EnumSoundAdvEftParam.E_SRS_TRUEDIALOG_ONOFF) == 1 ? true : false;

        LogHelper.d(TAG, "isSRSTrebleEnable() end value = " + value);
        return value;
    }

    /**
     * Enable SubWoofer
     */
    public static int enableSubWoofer(boolean onoff) {
        LogHelper.d(TAG, "enableSubWoofer = " + onoff);

        int value = getAudioManager().enableSubWoofer(onoff);

        LogHelper.d(TAG, " return enableSubWoofer = " + value);

        return value;
    }

    /**
     * Get SubWoofer Enable
     */
    public static boolean isSubWooferEnable() {
        LogHelper.d(TAG, "isSubWooferEnable() begin");

        boolean value = getAudioManager().isSubWooferEnable();
        LogHelper.d(TAG, "isSubWooferEnable() end value = " + value);
        return value;
    }

    public static int setAVsync(int ms) {
        LogHelper.d(TAG, "setAVsync(int ms = " + ms + ")  begin");

        int value = getAudioManager().setAVsync(ms);

        LogHelper.d(TAG, "setAVsync(int ms = " + ms + ")  end value = " + value);
        return value;
    }

    /**
     * setting out of sync
     */
    public static int getAVsync() {
        LogHelper.d(TAG, "getAVsync() begin");

        int value = getAudioManager().getAVsync();

        LogHelper.d(TAG, "getAVsync() end value = " + value);
        return value;
    }

    /**
     * Set Audio Balance
     */
    public static int setBalance(int balance) {
        LogHelper.d(TAG, "setBalance(int balance = " + balance + ")  begin");

        int value = getAudioManager().setEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_BALANCE, balance);

        LogHelper.d(TAG, "setBalance(int balance = " + balance
                + ")  end value = " + value);
        return value;
    }

    /**
     * access channel balance
     */
    public static int getBalance() {
        LogHelper.d(TAG, "getBalance() begin");

        int value = getAudioManager().getEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_BALANCE);

        LogHelper.d(TAG, "getBalance() end value = " + value);
        return value;
    }

    /**
     * Set Bass
     */
    public static int setBass(int gain) {
        LogHelper.d(TAG, "setBass(int gain = " + gain + ")  begin");

        int value = getAudioManager().setEffectParameter(EnumSoundEftParam.E_SOUND_SET_PARAM_BASS,
                gain);

        LogHelper.d(TAG, "setBass(int gain = " + gain + ")  end value = " + value);
        return value;
    }

    /**
     * gets the bass
     */
    public static int getBass() {
        LogHelper.d(TAG, "getBass() begin");

        int value = getAudioManager().getEffectParameter(EnumSoundEftParam.E_SOUND_SET_PARAM_BASS);

        LogHelper.d(TAG, "getBass() end value = " + value);
        return value;
    }

    /**
     * Set EQ
     */
    public static int setEQ(int band, int gain) {
        LogHelper.d(TAG, "setEQ(int band = " + band + ", int gain = " + gain
                + ")  begin");

        int value = getAudioManager().setEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_BAND0_LEVEL + band, gain);

        LogHelper.d(TAG, "setEQ(int band =" + band + ", int gain = " + gain
                + ")  end value = " + value);
        return value;
    }

    /**
     * access to the specified frequency band gain
     */
    public static int getEQ(int band) {
        LogHelper.d(TAG, "getEQ(int band = " + band + ") begin");

        int value = getAudioManager().getEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_BAND0_LEVEL + band);

        LogHelper.d(TAG, "getEQ(int band = " + band + ") end value = " + value);
        return value;
    }

    /**
     * get sound field model
     */
    public static int getHangMode() {
        LogHelper.d(TAG, "getHangMode() begin");

        int value = getAudioManager().getHangMode();

        LogHelper.d(TAG, "getHangMode() end value = " + value);
        return value;
    }

    /**
     * gets the input volume
     */
    public static int getInputVolume() {
        LogHelper.d(TAG, "getInputVolume() begin");

        int value = getAudioManager().getInputVolume();

        LogHelper.d(TAG, "getInputVolume() end value = " + value);
        return value;
    }

    /**
     * access to the specified channel mute switch
     */
    public static boolean getMute(int channel) {
        LogHelper.d(TAG, "getMute(int channel = " + channel + ") begin");

        boolean value = getAudioManager().getMute(channel);

        LogHelper.d(TAG, "getMute(int channel = " + channel + ") end value = "
                + value);
        return value;
    }

    /**
     * get sound mode
     */
    public static int getSoundMode() {
        LogHelper.d(TAG, "getSoundMode() begin");

        int value = getAudioManager().getSoundMode();

        LogHelper.d(TAG, "getSoundMode() end value = " + value);
        return value;
    }

    /**
     * gets the stereo mode
     */
    public static int getStereoMode() {
        LogHelper.d(TAG, "getSPDIFOutput() begin");

        int value = getAudioManager().getStereoMode();

        LogHelper.d(TAG, "getStereoMode() end value = " + value);
        return value;
    }

    /**
     * to obtain external amplifier bass volume
     */
    public static int getSubWooferVolume() {
        LogHelper.d(TAG, "getSPDIFOutput() begin");

        int value = getAudioManager().getSubWooferVolume();

        LogHelper.d(TAG, "getSubWooferVolume() end value = " + value);
        return value;
    }

    /**
     * gets the treble
     */
    public static int getTreble() {
        LogHelper.d(TAG, "getSPDIFOutput() begin");

        int value = getAudioManager()
                .getEffectParameter(EnumSoundEftParam.E_SOUND_SET_PARAM_TREBLE);

        LogHelper.d(TAG, "getTreble() end value = " + value);
        return value;
    }

    /**
     * gets the subwoofer volume
     */
    /*
     * public static int getTruBassVol() { if (Constant.LOG_TAG) { Log.d(TAG,
     * "getSPDIFOutput() begin"); } int value =
     * getAudioManager().getTruBassVol(); if (Constant.LOG_TAG) { Log.d(TAG,
     * "getTruBassVol() end value = " + value); } return value; }
     */

    /**
     * get voice enhancement
     */
    /*
     * public static int getTruDialog() { if (Constant.LOG_TAG) { Log.d(TAG,
     * "getSPDIFOutput() begin"); } int value =
     * getAudioManager().getTruDialog(); if (Constant.LOG_TAG) { Log.d(TAG,
     * "getTruDialog() end value = " + value); } return value; }
     */

    /**
     * access to the specified channel volume
     */
    public static int getVolume(int channel) {
        LogHelper.d(TAG, "getVolume(int channel= " + channel + ") begin");

        int value = getAudioManager().getVolume(channel);

        LogHelper.d(TAG, "getVolume(int channel= " + channel + ") end value = "
                + value);
        return value;
    }

    /**
     * to obtain external amplifier Mute state
     */
    public static boolean isAmplifierMute() {
        LogHelper.d(TAG, "isAmplifierMute() begin");

        boolean value = getAudioManager().isAmplifierMute();

        LogHelper.d(TAG, "isAmplifierMute() end value = " + value);
        return value;
    }

    /**
     * gets a ARC switch
     */
    public static boolean isARCEnable() {
        LogHelper.d(TAG, "isARCEnable() begin");

        boolean value = getAudioManager().isARCEnable();

        LogHelper.d(TAG, "isARCEnable() end value = " + value);
        return value;
    }

    /**
     * set sound mode
     */
    public static int setHangMode(int hangmode) {
        LogHelper.d(TAG, "setHangMode(int hangmode = " + hangmode + ")  begin");

        int value = getAudioManager().setHangMode(hangmode);

        LogHelper.d(TAG, "setHangMode(int hangmode = " + hangmode
                + ")  end value = " + value);
        return value;
    }

    /**
     * set the input volume
     */
    public static int setInputVolume(int vol) {
        LogHelper.d(TAG, "setInputVolume(int vol = " + vol + ")  begin");

        int value = getAudioManager().setInputVolume(vol);

        LogHelper.d(TAG, "setInputVolume(int vol = " + vol + ")  end value = "
                + value);
        return value;
    }

    /**
     * sets the specified channel mute switch
     */
    public static int setMute(int channel, boolean onoff) {
        LogHelper.d(TAG, "setMute(int channel = " + channel
                + ", boolean onoff = " + onoff + ")  begin");

        int value = getAudioManager().setMute(channel, onoff);

        LogHelper.d(TAG, "setMute(int channel = " + channel
                + ", boolean onoff = " + onoff + ")  end value = " + value);
        return value;
    }


    /**
     * set sound mode
     */
    public static int setSoundMode(int sndmode) {
        LogHelper.d(TAG, "setSoundMode(int sndmode = " + sndmode + ")  begin");

        int value = getAudioManager().setSoundMode(sndmode);

        LogHelper.d(TAG, "setSoundMode(int sndmode = " + sndmode
                + ") end value = " + value);
        return value;
    }

    public static int setStereoMode(int stereo) {
        LogHelper.d(TAG, "setStereoMode(int stereo = " + stereo + ")  begin");

        int value = getAudioManager().setStereoMode(stereo);

        LogHelper.d(TAG, "setStereoMode(int stereo = " + stereo
                + ") end value = " + value);
        return value;
    }

    /**
     * treble
     */
    public static int setTreble(int gain) {
        LogHelper.d(TAG, "setTreble(int gain = " + gain + ")  begin");

        int value = getAudioManager().setEffectParameter(
                EnumSoundEftParam.E_SOUND_SET_PARAM_TREBLE, gain);

        LogHelper.d(TAG, "setTreble(int gain = " + gain + ") end value = "
                + value);
        return value;
    }

    /**
     * Set SubWoofer Volume
     */

    public static int setSubWooferVolume(int vol) {
        LogHelper.d(TAG, "setSubWooferVolume(int vol = " + vol + ")  begin");

        int value = getAudioManager().setSubWooferVolume(vol);

        LogHelper.d(TAG, "setSubWooferVolume(int vol = " + vol + ") end value = "
                + value);
        return value;
    }

    /**
     * sets the specified channel volume
     */
    public static int setVolume(int channel, int vol) {
        LogHelper.d(TAG, "setVolume(int channel = " + channel + ", int vol  = "
                + vol + ")  begin");

        int value = getAudioManager().setVolume(channel, vol);

        LogHelper.d(TAG, "setVolume(int channel = " + channel + ", int vol = "
                + vol + ") end value = " + value);
        return value;
    }
}
