
package com.hisilicon.launcher.coverflow;

import android.widget.ImageView;

/**
 * @author huyq
 */
public class AnimableControl {

    // imageView object
    private ImageView mImageView;
    // position of imageView
    private Postion mPostion;
    // duration of animation
    private int mDuration = 500;

    public AnimableControl(ImageView imageView, Postion postion) {
        this.mImageView = imageView;
        this.mPostion = postion;
    }

    /**
     * start animation
     */
    public void startAnimation() {
        mImageView.animate().x(mPostion.getX()).scaleX(mPostion.getScale())
                .scaleY(mPostion.getScale()).setDuration(mDuration).start();
    }

    /**
     * get next position of imageView
     *
     * @param logic
     */
    public void getNextPosition(PositionLogic logic) {
        mPostion = logic.getNextPosion(mPostion);
    }

    /**
     * get current position of image
     *
     * @return
     */
    public Postion getPosition() {
        return mPostion;
    }

    /**
     * get image
     *
     * @return
     */
    public ImageView getImageView() {
        return mImageView;
    }

    /**
     * set image
     *
     * @param imageView
     */
    public void setImageView(ImageView imageView) {
        this.mImageView = imageView;
    }

    @Override
    public String toString() {
        return "AnimableControl [imageView=" + mImageView.getId() + ", p="
                + mPostion + "]";
    }

    /**
     * get duration of animation
     *
     * @return
     */
    public int getDuration() {
        return mDuration;
    }

    /**
     * set duration of animation
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.mDuration = duration;
    }

}
