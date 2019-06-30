package com.hisilicon.android.videoplayer.model;

/**
 * Created on 2018/7/2.
 */
public class PlayerController {
    /**
     * 当前播放码流路径
     */
    private String currPlayPath;
    /**
     *
     */
    private String currPath;
    private boolean is3DMode = false;
    private boolean is3DOutput = false;
    private boolean is3DTiming = false;
    private int originalFmt = 0;
    private int mvcType = 0;
    private boolean isFirstClick = false;
    private String currName;
    private long currSize;
    private int currId;
    private String currMode;


    private int dolbyCertification;
    private int dolbyRangeInfo;
    private int formatAdaption;
    private int mVC3DAdapte;


    private boolean rewindOrForward;
    private boolean isSeekWhenPlaying;

    private int format;

    private boolean isMkvVideo;

    private int subCount;
    private int mCurrMode ;

    private boolean isSubtitleOn = true ;


    public String getCurrPlayPath() {
        return currPlayPath;
    }

    public void setCurrPlayPath(String currPlayPath) {
        this.currPlayPath = currPlayPath;
    }

    public String getCurrPath() {
        return currPath;
    }

    public void setCurrPath(String currPath) {
        this.currPath = currPath;
    }

    public boolean is3DMode() {
        return is3DMode;
    }

    public void setIs3DMode(boolean is3DMode) {
        this.is3DMode = is3DMode;
    }

    public boolean is3DOutput() {
        return is3DOutput;
    }

    public void setIs3DOutput(boolean is3DOutput) {
        this.is3DOutput = is3DOutput;
    }

    public boolean is3DTiming() {
        return is3DTiming;
    }

    public void setIs3DTiming(boolean is3DTiming) {
        this.is3DTiming = is3DTiming;
    }

    public int getOriginalFmt() {
        return originalFmt;
    }

    public void setOriginalFmt(int originalFmt) {
        this.originalFmt = originalFmt;
    }

    public int getMvcType() {
        return mvcType;
    }

    public void setMvcType(int mvcType) {
        this.mvcType = mvcType;
    }

    public boolean isFirstClick() {
        return isFirstClick;
    }

    public void setFirstClick(boolean firstClick) {
        isFirstClick = firstClick;
    }

    public String getCurrName() {
        return currName;
    }

    public void setCurrName(String currName) {
        this.currName = currName;
    }

    public long getCurrSize() {
        return currSize;
    }

    public void setCurrSize(long currSize) {
        this.currSize = currSize;
    }

    public int getCurrId() {
        return currId;
    }

    public void setCurrId(int currId) {
        this.currId = currId;
    }

    public String getCurrMode() {
        return currMode;
    }

    public void setCurrMode(String currMode) {
        this.currMode = currMode;
    }

    public int getDolbyCertification() {
        return dolbyCertification;
    }

    public void setDolbyCertification(int dolbyCertification) {
        this.dolbyCertification = dolbyCertification;
    }

    public int getDolbyRangeInfo() {
        return dolbyRangeInfo;
    }

    public void setDolbyRangeInfo(int dolbyRangeInfo) {
        this.dolbyRangeInfo = dolbyRangeInfo;
    }

    public int getFormatAdaption() {
        return formatAdaption;
    }

    public void setFormatAdaption(int formatAdaption) {
        this.formatAdaption = formatAdaption;
    }

    public int getmVC3DAdapte() {
        return mVC3DAdapte;
    }

    public void setmVC3DAdapte(int mVC3DAdapte) {
        this.mVC3DAdapte = mVC3DAdapte;
    }

    public boolean isRewindOrForward() {
        return rewindOrForward;
    }

    public void setRewindOrForward(boolean rewindOrForward) {
        this.rewindOrForward = rewindOrForward;
    }

    public boolean isSeekWhenPlaying() {
        return isSeekWhenPlaying;
    }

    public void setSeekWhenPlaying(boolean seekWhenPlaying) {
        isSeekWhenPlaying = seekWhenPlaying;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public boolean isMkvVideo() {
        return isMkvVideo;
    }

    public void setMkvVideo(boolean mkvVideo) {
        isMkvVideo = mkvVideo;
    }

    public int getSubCount() {
        return subCount;
    }

    public void setSubCount(int subCount) {
        this.subCount = subCount;
    }

    public int getmCurrMode() {
        return mCurrMode;
    }

    public void setmCurrMode(int mCurrMode) {
        this.mCurrMode = mCurrMode;
    }

    public boolean isSubtitleOn() {
        return isSubtitleOn;
    }

    public void setSubtitleOn(boolean subtitleOn) {
        isSubtitleOn = subtitleOn;
    }
}
