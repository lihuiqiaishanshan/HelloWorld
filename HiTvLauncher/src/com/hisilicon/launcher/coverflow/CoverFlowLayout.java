
package com.hisilicon.launcher.coverflow;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class CoverFlowLayout extends LinearLayout {

    List<AnimableControl> mControlList;

    public CoverFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        setChildrenDrawingOrderEnabled(true);
    }

    public CoverFlowLayout(Context context) {
        super(context);
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        for (int k = 0; k < childCount; k++) {
            if (mControlList.get(k).getPosition().getOrder() == i) {
                for (int j = 0; j < childCount; j++) {
                    if (getChildAt(j).getId() == mControlList.get(k)
                            .getImageView().getId()) {
                        return j;
                    }
                }
            }
        }

        return i;
    }

    public void setControlList(List<AnimableControl> controlList) {
        mControlList = controlList;
    }

}
