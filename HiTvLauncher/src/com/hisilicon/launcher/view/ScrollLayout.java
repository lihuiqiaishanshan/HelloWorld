
package com.hisilicon.launcher.view;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.SystemProperties;
import android.util.AttributeSet;

import com.hisilicon.launcher.MyApplication;
import com.hisilicon.launcher.util.LogHelper;

import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Scroller;

import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.MyAppActivity;
import com.hisilicon.launcher.interfaces.ShowAbleInterface;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.view.MainPageApp;

import static com.hisilicon.launcher.util.Constant.DATA_LOCAL;

/**
 * @author janey
 */
public class ScrollLayout extends ViewGroup {
    // mCurScreen 当前中央显示的第几屏  即(childCount - 1) / 2
    private int mCurScreen = 0;
    // SNAP_VELOCITY
    private static final int SNAP_VELOCITY = 0;

    private static final String TAG = "ScrollLayout";
    //
    private static final int TOUCH_STATE_REST = 0;
    //
    private static final int TOUCH_STATE_SCROLLING = 1;
    private static final int SCROLL_ANIM_DURATION = 250;

    private float mLastMotionX;
    //
    private Scroller mScroller;

    private int mTouchSlop;

    private int mTouchState = TOUCH_STATE_REST;
    //
    private VelocityTracker mVelocityTracker;

    private int mWidth;
    //
    private Camera mCamera;
    private Matrix mMatrix;
    //
    private float angle = 0;

    private MainActivity mContext;
    private MyApplication myApplication;
    private float preX;
    private float curX;
    public ScrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);

    }

    public ScrollLayout(Context context) {
        this(context, null);
    }

    public ScrollLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = (MainActivity) context;
        mScroller = new Scroller(context, new AccelerateInterpolator(1.5f), true);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mCamera = new Camera();
        mMatrix = new Matrix();
        myApplication = (MyApplication) mContext.getApplicationContext();
        mCurScreen = myApplication.getCurScreenId();
    }

    @Override
    public void addView(View child) {
        super.addView(child);
    }

    @Override
    protected void attachViewToParent(View child, int index, LayoutParams params) {
        super.attachViewToParent(child, index, params);
    }

    @Override
    public void computeScroll() {

        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    /*
     *
     */
    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        final long drawingTime = getDrawingTime();
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            drawScreen(canvas, i, drawingTime);
        }
    }

    public void drawScreen(Canvas canvas, int screen, long drawingTime) {
        //
        final int width = getWidth();
        final int scrollWidth = screen * width;
        final int scrollX = this.getScrollX();
        //
        if (scrollWidth > scrollX + width || scrollWidth + width < scrollX) {
            return;
        }
        final View child = getChildAt(screen);
        final int faceIndex = screen;
        final float currentDegree = getScrollX() * (angle / getMeasuredWidth());
        final float faceDegree = currentDegree - faceIndex * angle;
        if (faceDegree > 90 || faceDegree < -90) {
            return;
        }
        final float centerX = (scrollWidth < scrollX) ? scrollWidth + width
                : scrollWidth;
        final float centerY = (float) getHeight() / 2;
        final Camera camera = mCamera;
        final Matrix matrix = mMatrix;
        canvas.save();
        camera.save();
        camera.rotateY(-faceDegree);
        camera.getMatrix(matrix);
        camera.restore();
        matrix.preTranslate(-centerX, -centerY);
        matrix.postTranslate(centerX, centerY);
        canvas.concat(matrix);
        drawChild(canvas, child, drawingTime);
        canvas.restore();
    }

    @Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        super.dispatchWindowFocusChanged(hasFocus);
    }

    @Override
    public void dispatchWindowVisibilityChanged(int visibility) {
        super.dispatchWindowVisibilityChanged(visibility);
    }

    public ShowAbleInterface getCurScreen() {
        LogHelper.d(TAG, "mCurScreen :" + mCurScreen);
        return (ShowAbleInterface) this.getChildAt(mCurScreen);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {

        super.onDetachedFromWindow();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        final float x = ev.getX();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                final int xDiff = (int) Math.abs(mLastMotionX - x);
                if (xDiff > mTouchSlop) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:

            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;

        }
        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View childView = getChildAt(i);
            if (childView.getVisibility() != View.GONE) {
                final int childWidth = childView.getMeasuredWidth();
                childView.layout(childLeft, 0, childLeft + childWidth,
                        childView.getMeasuredHeight());
                childLeft += childWidth;
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException(
                    "ScrollLayout only can run at EXACTLY mode!");
        }

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        scrollTo(mCurScreen * width, 0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction();
        final float x = event.getX();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                LogHelper.d(TAG, "event down!");
                preX = event.getX();
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }
                mLastMotionX = x;
                break;

            case MotionEvent.ACTION_MOVE:

                int deltaX = (int) (mLastMotionX - x);

                mLastMotionX = x;
                scrollBy(deltaX, 0);

                break;

            case MotionEvent.ACTION_UP:
                LogHelper.d(TAG, "event up");
                curX = event.getX();
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int) velocityTracker.getXVelocity();
                if (velocityX > SNAP_VELOCITY && mCurScreen > 0
                        && x - mLastMotionX > 10) {
                    // Fling enough to move left
                    LogHelper.d(TAG, "snap left");
                    snapToScreen(mCurScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY
                        && mCurScreen < getChildCount() - 1
                        && mLastMotionX - x > 10) {
                    // Fling enough to move right
                    LogHelper.d(TAG, "snap right");
                    snapToScreen(mCurScreen + 1);
                } else {
                    if (!String.valueOf(curX).equals(String.valueOf(preX))) {
                        snapToDestination();
                    }
                }
                mContext.switchSource();
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return true;

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {

        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {

        super.onWindowVisibilityChanged(visibility);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    private void setMWidth() {
        if (mWidth == 0) {
            mWidth = getWidth();
        }
    }

    /**
     * 子View循环右移，以显示左一个page
     */
    private void rotateRightSubViews() {
        int count = this.getChildCount();
        View view = getChildAt(count - 1);
        removeViewAt(count - 1);
        addView(view, 0);
    }

    /**
     * 子View循环左移,以显示右一个page
     */
    private void rotateLeftSubViews() {
        int count = this.getChildCount();
        View view = getChildAt(0);
        removeViewAt(0);
        addView(view, count - 1);
    }

    public void setMainPage(int curScreenId) {
        int index = SystemProperties.getInt("persist.sys.launcher.page", 0);
        LogHelper.i(TAG, "setMainPage curScreenId = " + curScreenId + ", new MainPage id = " + index);
        switch (index) {
            case MainPageApp.PAGENUM:
                setMainPageToAppScreen(curScreenId);
                break;
            case MainPageSetting.PAGENUM:
                setMainPageToSettingScreen(curScreenId);
                break;
            default:
                break;
        }
    }

    public void setMainPageToTVScreen(int curScreenId) {
        if (curScreenId == MainPageTv.PAGENUM) {
            return;
        }
        curScreenId = Math.max(0, Math.min(curScreenId, getChildCount() - 1));
        for (int i = 0; i < (getChildCount() + MainPageTv.PAGENUM - curScreenId); i++) {
            rotateLeftSubViews();
        }
        getCurScreen().onShow();

    }

    public void setMainPageToAppScreen(int curScreenId) {
        mCurScreen = MainPageApp.PAGENUM;
        myApplication.setCurScreenId(MainPageApp.PAGENUM);
        getCurScreen().onShow();
    }

    public void setMainPageToSettingScreen(int curScreenId) {
        mCurScreen = MainPageSetting.PAGENUM;
        myApplication.setCurScreenId(MainPageSetting.PAGENUM);
        getCurScreen().onShow();
    }

    /**
     * According to the position of current layout scroll to the destination
     * page.
     */

    public void snapToDestination() {
        setMWidth();
        final int destScreen = (getScrollX() + mWidth / 2) / mWidth;
        LogHelper.d(TAG, "snapToDestination destScreen = " + destScreen);
        snapToScreen(destScreen);
    }

    public void snapToScreen(int whichScreen) {
        MainActivity.setSnapLeftOrRight(true);
        setMWidth();
        int scrollX = getScrollX();
        if (scrollX == mWidth*mCurScreen) {
            if (whichScreen > getChildCount() - 1) {
                whichScreen = 0;
                mScroller.startScroll(getScrollX(), 0, -mWidth, 0, SCROLL_ANIM_DURATION);
            } else if (whichScreen < 0) {
                whichScreen = getChildCount() - 1;
                mScroller.startScroll(getScrollX(), 0, mWidth, 0, SCROLL_ANIM_DURATION);
            } else {
                if (whichScreen > mCurScreen) {
                    mScroller.startScroll(getScrollX(), 0, mWidth, 0, SCROLL_ANIM_DURATION);
                } else {
                    mScroller.startScroll(getScrollX(), 0, -mWidth, 0, SCROLL_ANIM_DURATION);
                }
            }
        } else {
            int delta = 0;
            int startX = 0;
            if (whichScreen > getChildCount() - 1) {
                whichScreen = getChildCount() - 1;
            } else if (whichScreen < 0) {
                whichScreen = 0;
            }
            int startWidth = whichScreen * mWidth;
            if (whichScreen > mCurScreen) {
                LogHelper.d(TAG, "whichScreen > mCurScreen " );
                delta = scrollX > 0 ? startWidth - scrollX : -scrollX;
            } else if (whichScreen < mCurScreen) {
                LogHelper.d(TAG, "whichScreen < mCurScreen " );
                delta = scrollX < (getChildCount()-1) * mWidth ? -scrollX : mCurScreen*mWidth - scrollX;
            } else {
                LogHelper.d(TAG, "else " );
                delta = startWidth - scrollX;
            }
            LogHelper.d(TAG, "delta =  " +delta);
            mScroller.startScroll(scrollX, 0, delta, 0, SCROLL_ANIM_DURATION);
        }
        mCurScreen = whichScreen;
        myApplication.setCurScreenId(mCurScreen);
        invalidate(); // Redraw the layout
        getCurScreen().onShow();
    }

    public int getCurrentScreen() {
        LogHelper.d(TAG, "getCurrentScreen :" + mCurScreen);
        return mCurScreen;
    }

    public boolean isFinished() {
        return mScroller.isFinished();
    }

}
