package com.hisilicon.android.videoplayer.model;

public class ModeMkvInfo {

    private long chapterUID;

    private int chapterTimeStart;

    private int chapterTimeEnd;

    private String chapTitle;

    private String chapLanguage;

    public long getChapterUID() {
        return chapterUID;
    }

    public void setChapterUID(long chapterUID) {
        this.chapterUID = chapterUID;
    }

    public int getChapterTimeStart() {
        return chapterTimeStart;
    }

    public void setChapterTimeStart(int chapterTimeStart) {
        this.chapterTimeStart = chapterTimeStart;
    }

    public int getChapterTimeEnd() {
        return chapterTimeEnd;
    }

    public void setChapterTimeEnd(int chapterTimeEnd) {
        this.chapterTimeEnd = chapterTimeEnd;
    }

    public String getChapTitle() {
        return chapTitle;
    }

    public void setChapTitle(String chapTitle) {
        this.chapTitle = chapTitle;
    }

    public String getChapLanguage() {
        return chapLanguage;
    }

    public void setChapLanguage(String chapLanguage) {
        this.chapLanguage = chapLanguage;
    }

}