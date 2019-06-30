
package com.hisilicon.launcher.model;

import android.view.View;

public class WidgetType {

    public static final int TYPE_SELECTOR = 0;
    public static final int TYPE_PROGRESS = 1;
    public static final int TYPE_TEXTVIEW = 2;
    public static final int TYPE_ONLYSELECTOR = 3;
    public static final int TYPE_LONG_TEXT = 4;

    public interface AccessOnClickInterface {

        void onClickEvent(View v);

    }

    public interface AccessSysValueInterface {

        int getSysValue();

        int setSysValue(int i);
    }

    public interface AccessProgressInterface {
        int getProgress();

        int setProgress(int i);
    }

    public interface Refreshable {
        void refreshUI();

        WidgetType getWidgetType();

        boolean getIsFocus();
    }

    private int mType;
    private String mName;
    private int[] data;
    private String mInfo;
    private Object mTag;

    public Object getTag() {
        return mTag;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    private String onlySelectorName;

    public String getOnlySelectorName() {
        return onlySelectorName;
    }

    public void setOnlySelectorName(String onlySelectorName) {
        this.onlySelectorName = onlySelectorName;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String info) {
        this.mInfo = info;
    }

    /**
     * progress of the maximum value
     */
    private int maxProgress = 100;

    /**
     * progress of the bottom layer and the interfacial offset
     */
    private int offset = 0;

    /**
     * Goal setting options can be selected: used in SRS
     */
    private boolean isEnable = true;

    public boolean isEnable() {
        return isEnable;
    }

    public void setEnable(boolean isEnable) {
        this.isEnable = isEnable;
    }

    private AccessOnClickInterface mAccessOnClickInterface;

    private AccessSysValueInterface mAccessSysValueInterface;

    private AccessProgressInterface mAccessProgressInterface;

    public WidgetType() {
        super();
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        this.mType = type;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public int[] getData() {
        return data;
    }

    public void setData(int[] data) {
        this.data = data;
    }

    public int getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public AccessOnClickInterface getmAccessOnClickInterface() {
        return mAccessOnClickInterface;
    }

    public void setmAccessOnClickInterface(
            AccessOnClickInterface mAccessOnClickInterface) {
        this.mAccessOnClickInterface = mAccessOnClickInterface;
    }

    public AccessSysValueInterface getmAccessSysValueInterface() {
        return mAccessSysValueInterface;
    }

    public void setmAccessSysValueInterface(
            AccessSysValueInterface mAccessSysValueInterface) {
        this.mAccessSysValueInterface = mAccessSysValueInterface;
    }

    public AccessProgressInterface getmAccessProgressInterface() {
        return mAccessProgressInterface;
    }

    public void setmAccessProgressInterface(
            AccessProgressInterface mAccessProgressInterface) {
        this.mAccessProgressInterface = mAccessProgressInterface;
    }
}
