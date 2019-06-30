
package com.hisilicon.launcher.interfaces;

import java.util.ArrayList;

import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.android.tvapi.HitvManager;
import com.hisilicon.android.tvapi.Picture;
import com.hisilicon.android.tvapi.vo.ColorTempInfo;
import com.hisilicon.launcher.util.Constant;

public class PictureInterface {

    private static final String TAG = "PictureInterface";

    /**
     * Set the backlight switch
     */
    public static Picture getPictureManager() {
        return HitvManager.getInstance().getPicture();
    }

    public static int enableBacklight(boolean onoff) {
        LogHelper.d(TAG, "enableBacklight(boolean onoff = " + onoff + ") begin");

        int value = getPictureManager().enableBacklight(onoff);

        LogHelper.d(TAG, "enableBacklight(boolean onoff = " + onoff
                + ")   end value = " + value);
        return value;
    }

    /**
     * Set the blue level expansion
     */
    public static int enableBlueExtend(boolean onoff) {
        LogHelper.d(TAG, "enableBlueExtend(boolean onoff = " + onoff + ") begin");

        int value = getPictureManager().enableBlueExtend(onoff);

        LogHelper.d(TAG, "enableBlueExtend(boolean onoff = " + onoff
                + ")  end value = " + value);
        return value;
    }

    /**
     * Design of dynamic contrast
     */
    public static int enableDCI(boolean onoff) {
        LogHelper.d(TAG, "enableDCI(boolean onoff = " + onoff + ") begin");

        int value = getPictureManager().enableDCI(onoff);

        LogHelper.d(TAG, "enableDCI(boolean onoff = " + onoff + ")  end value = "
                + value);
        return value;
    }

    /**
     * Design of dynamic backlight
     */
    public static int enableDynamicBL(boolean onoff) {
        LogHelper.d(TAG, "enableDynamicBL(boolean onoff = " + onoff + ") begin");

        int value = getPictureManager().enableDynamicBL(onoff);

        LogHelper.d(TAG, "enableDynamicBL(boolean onoff = " + onoff
                + ")  end value = " + value);
        return value;
    }

    /**
     * Set the static frame
     */
    public static int enableFreeze(boolean onoff) {
        LogHelper.d(TAG, "enableFreeze(boolean onoff = " + onoff + ") begin");

        int value = getPictureManager().enableFreeze(onoff);

        LogHelper.d(TAG, "enableFreeze(boolean onoff = " + onoff
                + ")  end value = " + value);
        return value;
    }

    /**
     * Setting up the game model
     */
    public static int enableGameMode(boolean bEnable) {
        LogHelper.d(TAG, "enableGameMode(boolean bEnable = " + bEnable
                + ") begin");

        int value = getPictureManager().enableGameMode(bEnable);

        LogHelper.d(TAG, "enableGameMode(boolean bEnable = " + bEnable
                + ")  end value = " + value);
        return value;
    }

    /**
     * Set the rate of recurrence
     */
    public static int enableOverscan(boolean bEnable) {
        LogHelper.d(TAG, "enableOverscan(boolean bEnable = " + bEnable
                + ") begin");

        int value = getPictureManager().enableOverscan(bEnable);

        LogHelper.d(TAG, "enableOverscan(boolean bEnable = " + bEnable
                    + ")  end value = " + value);
        return value;
    }

    /**
     * Gets the current ratio model
     */
    public static int getAspect() {
        LogHelper.d(TAG, "getAspect() begin");

        int value = getPictureManager().getAspect();

        LogHelper.d(TAG, "getAspect()  end value = " + value);
        return value;
    }

    /**
     * The Timming supports Aspect only on the main screen
     */
    public static ArrayList<Integer> getAvailAspectList() {
        LogHelper.d(TAG, "getAvailAspectList() begin");

        ArrayList<Integer> value = getPictureManager().getAvailAspectList();

        LogHelper.d(TAG, "getAvailAspectList()  end value = " + value);
        return value;
    }

    /**
     * Get backlight
     */
    public static int getBacklight() {
        LogHelper.d(TAG, "getBacklight() begin");

        int value = getPictureManager().getBacklight();

        LogHelper.d(TAG, "getBacklight()  end value = " + value);
        return value;
    }

    /**
     * Acquiring the brightness
     */
    public static int getBrightness() {
        LogHelper.d(TAG, "getBrightness() begin");

        int value = getPictureManager().getBrightness();

        LogHelper.d(TAG, "getBrightness()  end value = " + value);
        return value;
    }

    /**
     * Gets the color temperature
     */
    public static int getColorTemp() {
        LogHelper.d(TAG, "getColorTemp() begin");

        int value = getPictureManager().getColorTemp();

        LogHelper.d(TAG, "getColorTemp()  end value = " + value);
        return value;
    }

    /**
     * get ColorTemp
     */
    public static ColorTempInfo getColorTempPara() {
        LogHelper.d(TAG, "getColorTempPara() begin");

        ColorTempInfo value = getPictureManager().getColorTempPara();

        LogHelper.d(TAG, "getColorTempPara()  end value = " + value);
        return value;
    }

    /**
     * Gets the contrast
     */
    public static int getContrast() {
        LogHelper.d(TAG, "getContrast() begin");

        int value = getPictureManager().getContrast();

        LogHelper.d(TAG, "getContrast()  end value = " + value);
        return value;
    }

    /**
     * Gets the block noise reduction of De-blocking strength
     */
    public static int getDeBlocking() {
        LogHelper.d(TAG, "getDeBlocking() begin");

        int value = getPictureManager().getDeBlocking();

        LogHelper.d(TAG, "getDeBlocking()  end value = " + value);
        return value;
    }

    /**
     * Gets the store mode
     */
    public static boolean getDemoMode(int mode) {
        LogHelper.d(TAG, "getDemoMode() begin");

        boolean value = getPictureManager().getDemoMode(mode);

        LogHelper.d(TAG, "getDemoMode()  end value = " + value);
        return value;
    }

    /**
     * Gets the mosquito noise removal de-ringing strength
     */
    public static int getDeRinging() {
        LogHelper.d(TAG, "getDeRinging() begin");

        int value = getPictureManager().getDeRinging();

        LogHelper.d(TAG, "getDeRinging()  end value = " + value);
        return value;
    }

    /**
     * Get film model
     */
    public static int getFilmMode() {
        LogHelper.d(TAG, "getFilmMode() begin");

        int value = getPictureManager().getFilmMode();

        LogHelper.d(TAG, "getFilmMode()  end value = " + value);
        return value;
    }

    /**
     * Gets the flesh tone
     */
    public static int getFleshTone() {
        LogHelper.d(TAG, "getFleshTone() begin");

        int value = getPictureManager().getFleshTone();

        LogHelper.d(TAG, "getFleshTone()  end value = " + value);
        return value;
    }

    /**
     * Gets a HDMI color range
     */
    public static int getHDMIColorRange() {
        LogHelper.d(TAG, "getHDMIColorRange() begin");

        int value = getPictureManager().getHDMIColorRange();

        LogHelper.d(TAG, "getHDMIColorRange()  end value = " + value);
        return value;
    }

    /**
     * get hue
     */
    public static int getHue() {
        LogHelper.d(TAG, "getHue() begin");

        int value = getPictureManager().getHue();

        LogHelper.d(TAG, "getHue()  end value = " + value);
        return value;
    }

    /**
     * to obtain the motion compensation
     */
    public static int getMEMCLevel() {
        LogHelper.d(TAG, "getMEMCLevel() begin");

        int value = getPictureManager().getMEMCLevel();

        LogHelper.d(TAG, "getMEMCLevel()  end value = " + value);
        return value;
    }

    /**
     * get noise reduction
     */
    public static int getNR() {
        LogHelper.d(TAG, "getNR() begin");

        int value = getPictureManager().getNR();

        LogHelper.d(TAG, "getNR()  end value = " + value);
        return value;
    }

    /**
     * get picture model
     */
    public static int getPictureMode() {
        LogHelper.d(TAG, "getPictureMode() begin");

        int value = getPictureManager().getPictureMode();

        LogHelper.d(TAG, "getPictureMode()  end value = " + value);
        return value;
    }

    /**
     * get saturation
     */
    public static int getSaturation() {
        LogHelper.d(TAG, "getSaturation() begin");

        int value = getPictureManager().getSaturation();

        LogHelper.d(TAG, "getSaturation()  end value = " + value);
        return value;
    }

    public static int getSharpness() {
        LogHelper.d(TAG, "getSharpness() begin");

        int value = getPictureManager().getSharpness();

        LogHelper.d(TAG, "getSharpness()  end value = " + value);
        return value;
    }

    /**
     * get backlight
     */
    public static boolean isBacklightEnable() {
        LogHelper.d(TAG, "isBacklightEnable() begin");

        boolean value = getPictureManager().isBacklightEnable();

        LogHelper.d(TAG, "isBacklightEnable()  end value = " + value);
        return value;
    }

    /**
     * gets the blue level expansion
     */
    public static boolean isBlueExtendEnable() {
        LogHelper.d(TAG, "isBlueExtendEnable() begin");

        boolean value = getPictureManager().isBlueExtendEnable();

        LogHelper.d(TAG, "isBlueExtendEnable()  end value = " + value);
        return value;
    }

    /**
     * get dynamic contrast
     */
    public static boolean isDCIEnable() {
        LogHelper.d(TAG, "isDCIEnable() begin");

        boolean value = getPictureManager().isDCIEnable();

        LogHelper.d(TAG, "isDCIEnable()  end value = " + value);
        return value;
    }

    /**
     * get dynamic backlight
     */
    public static boolean isDynamicBLEnable() {
        LogHelper.d(TAG, "isDynamicBLEnable() begin");

        boolean value = getPictureManager().isDynamicBLEnable();

        LogHelper.d(TAG, "isDynamicBLEnable()  end value = " + value);
        return value;
    }

    /**
     * get static frame
     */
    public static boolean isFreezeEnable() {
        LogHelper.d(TAG, "isFreezeEnable() begin");

        boolean value = getPictureManager().isFreezeEnable();

        LogHelper.d(TAG, "isFreezeEnable()  end value = " + value);
        return value;
    }

    /**
     * get game model
     */
    public static boolean isGameModeEnable() {
        LogHelper.d(TAG, "isGameModeEnable() begin");

        boolean value = getPictureManager().isGameModeEnable();

        LogHelper.d(TAG, "isGameModeEnable()  end value = " + value);
        return value;
    }

    /**
     * get enable reproducibility rate state
     */
    public static boolean isOverscanEnable() {
        LogHelper.d(TAG, "isOverscanEnable() begin");

        boolean value = getPictureManager().isOverscanEnable();

        LogHelper.d(TAG, "isOverscanEnable()  end value = " + value);
        return value;
    }

    /**
     * set the display proportions model
     */
    public static int setAspect(int aspect, boolean mute) {
        LogHelper.d(TAG, "setAspect(int aspect = " + aspect + ", boolean mute ="
                + mute + ") begin");

        int value = getPictureManager().setAspect(aspect, mute);

        LogHelper.d(TAG, "setAspect(int aspect =" + aspect + ", boolean mute ="
                + mute + ")  end value = " + value);
        return value;
    }

    /**
     * set backlight
     */
    public static int setBacklight(int backlight) {
        LogHelper.d(TAG, "setBacklight(int backlight = " + backlight + ") begin");

        int value = getPictureManager().setBacklight(backlight);

        LogHelper.d(TAG, "setBacklight(int backlight = " + backlight
                + ")  end value = " + value);
        return value;
    }

    /**
     * set brightness
     */
    public static int setBrightness(int brightness) {
        LogHelper.d(TAG, "setBrightness(int brightness = " + brightness
                + ") begin");

        int value = getPictureManager().setBrightness(brightness);

        LogHelper.d(TAG, "setBrightness(int brightness = " + brightness
                + ")  end value = " + value);
        return value;
    }

    public static int setColorTemp(int colortemp) {
        LogHelper.d(TAG, "setColorTemp(int colortemp = " + colortemp + ") begin");

        int value = getPictureManager().setColorTemp(colortemp);

        LogHelper.d(TAG, "setColorTemp(int colortemp = " + colortemp
                + ") end value = " + value);
        return value;
    }

    /**
     * set ColorTemp RGB GAIN and OFFSET Offset range function factory parameter
     * 0-100
     */
    public static int setColorTempPara(ColorTempInfo stColorTemp) {
        LogHelper.d(TAG, "setColorTempPara(ColorTempInfo stColorTemp = "
                + stColorTemp + ") begin");

        int value = getPictureManager().setColorTempPara(stColorTemp);

        LogHelper.d(TAG, "setColorTempPara(ColorTempInfo stColorTemp = "
                + stColorTemp + ") end value = " + value);
        return value;
    }

    /**
     * set contrast
     */
    public static int setContrast(int contrast) {
        LogHelper.d(TAG, "setContrast(int contrast = " + contrast + ") begin");

        int value = getPictureManager().setContrast(contrast);

        LogHelper.d(TAG, "setContrast(int contrast=" + contrast
                + ") end value = " + value);
        return value;
    }

    /**
     * set De-blocking
     */
    public static int setDeBlocking(int dbLevel) {
        LogHelper.d(TAG, "setDeBlocking(int dbLevel = " + dbLevel + ") begin");

        int value = getPictureManager().setDeBlocking(dbLevel);

        LogHelper.d(TAG, "setDeBlocking(int dbLevel = " + dbLevel
                + ") end value = " + value);
        return value;
    }

    /**
     * set store model
     */
    public static int setDemoMode(int demomode, boolean onoff) {
        LogHelper.d(TAG, "setDemoMode(int demomode = " + demomode + " onoff = "
                + onoff + ")begin");

        int value = getPictureManager().setDemoMode(demomode, onoff);

        LogHelper.d(TAG, "setDemoMode(int demomode = " + demomode
                + ") end  onoff= " + onoff + " + value=" + value);
        return value;
    }

    /**
     * Set the mosquito noise removal de-ringing strength
     */
    public static int setDeRinging(int drLlevel) {
        LogHelper.d(TAG, "setDeRinging(int drLlevel =" + drLlevel + ") begin");

        int value = getPictureManager().setDeRinging(drLlevel);

        LogHelper.d(TAG, "setDeRinging(int drLlevel =" + drLlevel
                + ") end value = " + value);
        return value;
    }

    /**
     * set film model
     */
    public static int setFilmMode(int filmmode) {
        LogHelper.d(TAG, "setFilmMode(int filmmode = " + filmmode + ") begin");

        int value = getPictureManager().setFilmMode(filmmode);

        LogHelper.d(TAG, "setFilmMode(int filmmode = " + filmmode
                + ") end value = " + value);
        return value;
    }

    /**
     * setb flesh tone
     */
    public static int setFleshTone(int fleshtone) {
        LogHelper.d(TAG, "setFleshTone(int fleshtone = " + fleshtone + ") begin");

        int value = getPictureManager().setFleshTone(fleshtone);

        LogHelper.d(TAG, "setFleshTone(int fleshtone = " + fleshtone
                    + ") end value = " + value);
        return value;
    }

    /**
     * set HDMI color range
     */
    public static int setHDMIColorRange(int val) {
        LogHelper.d(TAG, "setHDMIColorRange(int val = " + val + ") begin");

        int value = getPictureManager().setHDMIColorRange(val);

        LogHelper.d(TAG, "setHDMIColorRange(int val = " + val + ") end value = "
                    + value);
        return value;
    }

    /**
     * set hue
     */
    public static int setHue(int hue) {
        LogHelper.d(TAG, "setHue(int hue = " + hue + " begin");

        int value = getPictureManager().setHue(hue);

        LogHelper.d(TAG, "setHue(int hue = " + hue + ") end value = " + value);
        return value;
    }

    /**
     * set MEMCLevel
     */
    public static int setMEMCLevel(int memclevel) {
        LogHelper.d(TAG, "setMEMCLevel(int memclevel = " + memclevel + ") begin");

        int value = getPictureManager().setMEMCLevel(memclevel);

        LogHelper.d(TAG, "setMEMCLevel(int memclevel = " + memclevel
                + ") end value = " + value);
        return value;
    }

    /**
     * set NR
     */
    public static int setNR(int nr) {
        LogHelper.d(TAG, "setNR(int nr = " + nr + ") begin");

        int value = getPictureManager().setNR(nr);

        LogHelper.d(TAG, "setNR(int nr = " + nr + ") end value = " + value);
        return value;
    }

    /**
     * set picture model
     */
    public static int setPictureMode(int picturemode) {
        LogHelper.d(TAG, "setPictureMode(int picturemode = " + picturemode
                + ") begin");

        int value = getPictureManager().setPictureMode(picturemode);

        LogHelper.d(TAG, "setPictureMode(int picturemode = " + picturemode
                + ") end value = " + value);
        return value;
    }

    public static int setSaturation(int saturation) {
        LogHelper.d(TAG, "setSaturation(int saturation = " + saturation
                + ") begin");

        int value = getPictureManager().setSaturation(saturation);

        LogHelper.d(TAG, "setSaturation(int saturation = " + saturation
                + ") end value = " + value);
        return value;
    }

    /**
     * set sharpness
     */
    public static int setSharpness(int sharpness) {
        LogHelper.d(TAG, "setSharpness(int sharpness = " + sharpness + ") begin");

        int value = getPictureManager().setSharpness(sharpness);

        LogHelper.d(TAG, "setSharpness(int sharpness = " + sharpness
                + " end value = " + value);
        return value;
    }
}
