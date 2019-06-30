
package com.hisilicon.higallery.control;

import android.content.Context;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.core.GalleryCore.Rotation;
import com.hisilicon.higallery.utils.Utils;
import android.util.Log;

public class RotateController implements Controller {
    private GalleryCore mGalleryCore;
    private Context mContext;
    private int mRotation = 0;
    private Handler mInfoHandler;

    static final String TAG = "RotateController";

    public RotateController(GalleryCore galleryCore, Context context, Handler handler) {
        mGalleryCore = galleryCore;
        mContext = context;
        mInfoHandler = handler;
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
                Log.d(TAG, "HiGalleryL::rotate left::-90::OK");
                rotateLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                Log.d(TAG, "HiGalleryL::rotate right::90::OK");
                rotateRight();
                return true;
        }
        return false;
    }

    @Override
    public boolean onMotionEvent(MotionEvent event) {
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return false;
    }

    @Override
    public void startControl() {
        mRotation = 0;
        Utils.showInfo(mContext, mInfoHandler, Utils.ROTATE_MODE);
        mGalleryCore.reset();
    }

    @Override
    public void stopControl() {
        mGalleryCore.resetScaleLevel();
        mInfoHandler.sendEmptyMessage(Utils.DISMISS_INFO);
    }

    private void rotateLeft() {
        mRotation = (((mRotation - 90) % 360) + 360) % 360;
        mGalleryCore.rotate(Rotation.ROTATION_270);
    }

    private void rotateRight() {
        mRotation = (mRotation + 90) % 360;
        mGalleryCore.rotate(Rotation.ROTATION_90);
    }

    public int getRotationDegree(){
        return mRotation;
    }
}
