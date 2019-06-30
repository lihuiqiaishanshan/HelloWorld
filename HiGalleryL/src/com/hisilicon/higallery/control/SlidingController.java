
package com.hisilicon.higallery.control;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.core.GalleryCore.AnimType;
import com.hisilicon.higallery.core.GalleryCore.Sliding;

public class SlidingController implements Controller {
    private GalleryCore mGalleryCore;
    private AnimType mAnimType = AnimType.ANIM_RANDOM;
    private int mInterval;
    private Sliding mSliding;
    static final String TAG = "SlidingController";

    public SlidingController(GalleryCore galleryCore, Context context, Sliding sliding) {
        mGalleryCore = galleryCore;
        mSliding = sliding;
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
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
        mGalleryCore.reset();
        mGalleryCore.startSliding(mSliding, mAnimType, mInterval);
        Log.d(TAG, "HiGalleryL::startSliding::Interval " + mInterval + " mAnimType " + mAnimType);
    }

    @Override
    public void stopControl() {
        mGalleryCore.stopSliding();
        Log.d(TAG, "HiGalleryL::stopSliding");
        mGalleryCore.reset();
    }

    public void setAnimation(AnimType a) {
        mAnimType = a;
    }

    public void setInterval(int interval) {
        mInterval = interval;
    }
}
