
package com.hisilicon.higallery.control;

import android.view.KeyEvent;
import android.view.MotionEvent;

public interface Controller {
    boolean onKeyEvent(KeyEvent event);

    boolean onMotionEvent(MotionEvent event);

    boolean dispatchKeyEvent(KeyEvent event);

    void startControl();

    void stopControl();
}
