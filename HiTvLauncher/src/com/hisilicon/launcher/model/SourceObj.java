
package com.hisilicon.launcher.model;

public class SourceObj {
    // Source Id
    private int mSourceId;
    // Source Name
    private int mSourceName;
    // Source Icon
    private int mSourceIcon;
    // Source background
    private int mSourceBg;
    // Source is available or not
    private boolean isAvail;

    /**
     * get source id
     *
     * @return
     */
    public int getSourceId() {
        return mSourceId;
    }

    /**
     * set source id
     *
     * @param sourceId
     */
    public void setSourceId(int sourceId) {
        this.mSourceId = sourceId;
    }

    /**
     * get source name
     *
     * @return
     */
    public int getSourceName() {
        return mSourceName;
    }

    /**
     * set source name
     *
     * @param sourceName
     */
    public void setSourceName(int sourceName) {
        this.mSourceName = sourceName;
    }

    /**
     * get source icon
     *
     * @return
     */
    public int getSourceIcon() {
        return mSourceIcon;
    }

    /**
     * set source icon
     *
     * @param sourceIcon
     */
    public void setSourceIcon(int sourceIcon) {
        this.mSourceIcon = sourceIcon;
    }

    /**
     * get source background
     *
     * @return
     */
    public int getSourceBg() {
        return mSourceBg;
    }

    /**
     * set source background
     *
     * @param sourceBg
     */
    public void setSourceBg(int sourceBg) {
        this.mSourceBg = sourceBg;
    }

    /**
     * get Source is available or not
     *
     * @return
     */
    public boolean isAvail() {
        return isAvail;
    }

    /**
     * set Source is available or not
     *
     * @param isAvail
     */
    public void setAvail(boolean isAvail) {
        this.isAvail = isAvail;
    }

}
