
package com.hisilicon.higallery.control;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.core.GalleryCore.Callback;
import com.hisilicon.higallery.core.GalleryCore.Direction;
import com.hisilicon.higallery.ui.ScaleThunbView;
import com.hisilicon.higallery.utils.Utils;

public class ScaleController implements Controller, Callback {

    private static final float SCALE_MULTIPLE = 2;
    private static final int MOVE_STEP = 60;

    private GalleryCore mGalleryCore;
    private Context mContext;
    private Handler mInfoHandler;
    private ScaleThunbView mScaleThunbView;
    private int mRotationDegree = 0;
    static final String TAG = "ScaleController";

    public ScaleController(GalleryCore galleryCore, Context context, Handler handler) {
        mGalleryCore = galleryCore;
        mContext = context;
        mInfoHandler = handler;
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        boolean ret;
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_CHANNEL_UP:
            case KeyEvent.KEYCODE_ZOOM_IN:
                ret = mGalleryCore.zoom(SCALE_MULTIPLE);
                if(ret) {
                    Log.d(TAG, "HiGalleryL::zoomIn 2 times");
                } else {
                    Log.d(TAG, "HiGalleryL::already zoomIn to the largest");
                }
                return true;
            case KeyEvent.KEYCODE_CHANNEL_DOWN:
            case KeyEvent.KEYCODE_ZOOM_OUT:
                ret = mGalleryCore.zoom(1 / SCALE_MULTIPLE);
                if(ret) {
                    Log.d(TAG, "HiGalleryL::zoomOut 1/2 times");
                } else {
                    Log.d(TAG, "HiGalleryL::already zoomOut to the smallest");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ret = mGalleryCore.move(Direction.LEFT, MOVE_STEP);
                if(ret) {
                    Log.d(TAG, "HiGalleryL::move to left");
                } else {
                    Log.d(TAG, "HiGalleryL::already move to the most left");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ret = mGalleryCore.move(Direction.RIGHT, MOVE_STEP);
                if(ret) {
                     Log.d(TAG, "HiGalleryL::move to right");
                } else {
                     Log.d(TAG, "HiGalleryL::already move to the most right");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                ret = mGalleryCore.move(Direction.UP, MOVE_STEP);
                if(ret) {
                     Log.d(TAG, "HiGalleryL::move to up");
                } else {
                     Log.d(TAG, "HiGalleryL::already move to the most up");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                ret = mGalleryCore.move(Direction.DOWN, MOVE_STEP);
                if(ret) {
                      Log.d(TAG, "HiGalleryL::move to down");
                } else {
                     Log.d(TAG, "HiGalleryL::already move to the most down");
                }
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
        // TODO Auto-generated method stub
        Utils.showInfo(mContext, mInfoHandler, Utils.SCALE_MODE);
        mGalleryCore.setCallback(this);
        Point size = new Point();
        mGalleryCore.getDisplaySize(size);
        mScaleThunbView = new ScaleThunbView(mContext, size, setScaleDegree(), setMirror(), mGalleryCore.getCurrentPath());
        mScaleThunbView.setDrawnFrame(mGalleryCore.getShownFrame(), 1.0f);
        mScaleThunbView.show();
    }
    private int setScaleDegree(){
        String path = mGalleryCore.getCurrentPath();
        int orientation = mGalleryCore.getBitmapOrientation(path);
        int rotateDegree = 0;
        switch(orientation){
            case 7:
              rotateDegree += 270;
            break;
            case 6:
              rotateDegree += 90;
            break;
            case 4:
              rotateDegree += 180;
            break;
            case 3:
              rotateDegree += 180;
            break;
            case 5:
              rotateDegree += 90;
            break;
            case 8:
              rotateDegree += 270;
            break;
           default:
               break;
        }
        mRotationDegree = (rotateDegree + mRotationDegree)%360;
        return mRotationDegree;
    }
    private boolean setMirror(){
        String path = mGalleryCore.getCurrentPath();
        int orientation = mGalleryCore.getBitmapOrientation(path);
        boolean mirror = false;

        switch(orientation){
            case 2:
            case 4:
            case 5:
            case 7:
                mirror = true;
                break;
            default:
                break;
        }
        return mirror;
    }

    @Override
    public void stopControl() {
        mGalleryCore.resetScaleLevel();
        mInfoHandler.sendEmptyMessage(Utils.DISMISS_INFO);
        mScaleThunbView.hide();
        mScaleThunbView = null;
        mRotationDegree = 0;
    }

    @Override
    public void onReceiveCMD(int cmd, Object obj) {
        if (cmd == GalleryCore.CMD_SHOWN_FRAME_CHANGED) {
            float ScaleLevel = (float) obj;
            if (mScaleThunbView != null)
                mScaleThunbView.setDrawnFrame(mGalleryCore.getShownFrame(), ScaleLevel);
        }
    }
    public void setRotationgDegree(int rotationDegree){
        mRotationDegree = rotationDegree;
    }


}
