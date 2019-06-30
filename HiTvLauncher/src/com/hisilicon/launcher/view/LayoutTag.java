
package com.hisilicon.launcher.view;

import java.io.Serializable;

import android.view.View;

public class LayoutTag implements Serializable {

    private static final long serialVersionUID = 1L;
    private int mLayoutNum = 0;
    private int mPos = 0;
    private View mView = null;
    private Object obj = null;
    private int mLine = 0;

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        this.mView = view;
    }

    public int getLayoutNum() {
        return mLayoutNum;
    }

    public void setLayoutNum(int layoutNum) {
        mLayoutNum = layoutNum;
    }

    public int getmPos() {
        return mPos;
    }

    public void setmPos(int mPos) {
        this.mPos = mPos;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public int getmLine() {
        return mLine;
    }

    public void setmLine(int mLine) {
        this.mLine = mLine;
    }

}
