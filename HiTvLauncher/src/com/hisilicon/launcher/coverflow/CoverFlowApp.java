
package com.hisilicon.launcher.coverflow;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import com.hisilicon.launcher.util.LogHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;

/**
 * the view that achieved five images rotate themselves
 *
 * @author li_bin
 */
@SuppressLint("DrawAllocation")
public class CoverFlowApp extends LinearLayout {

    private static final String TAG = "CoverFlowApp";
    public  ImageView sImageView1 = null;
    public  ImageView sImageView2 = null;
    public  ImageView sImageView3 = null;
    public  ImageView sImageView4 = null;
    public  ImageView sImageView5 = null;
    private PositionLogic mPositionLogic;
    private AnimableControl mAnimableControl1;
    private AnimableControl mAnimableControl2;
    private AnimableControl mAnimableControl3;
    private AnimableControl mAnimableControl4;
    private AnimableControl mAnimableControl5;
    private List<AnimableControl> mControlList;
    private CoverFlowLayout mMyLinearLayout;
    private View mView;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int mAnimateDuration;
    private int mDelayTime;
    private boolean mFlag;
    private int mWidth;
    private int mHeight;
    private Point mCenter;
    private int[] mCardSpace = {
            383, 240, 70, 100, 243
    };

    @SuppressWarnings("static-access")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        LogHelper.d(TAG, "onLayout:" + l + ":" + t + ":" + r + ":" + b);
        mWidth = r - l;
        mHeight = b - t;
        mCenter = new Point(l + mWidth / 2, t + (t + mHeight / 2));
        float[] positions = {
                mCenter.x - mCardSpace[0],
                mCenter.x - mCardSpace[1], mCenter.x - mCardSpace[2],
                mCenter.x + mCardSpace[3], mCenter.x + mCardSpace[4]
        };
        for (int i = 0; i < positions.length; i++) {
            LogHelper.d(TAG, "positions:" + i + ":" + positions[i]);
        }
        mPositionLogic = new PositionLogic(positions);
        if (!mFlag) {
            mFlag = true;
            mAnimableControl1 = new AnimableControl(sImageView1, mPositionLogic
                    .getPlist().get(0));
            mAnimableControl2 = new AnimableControl(sImageView2, mPositionLogic
                    .getPlist().get(1));
            mAnimableControl3 = new AnimableControl(sImageView3, mPositionLogic
                    .getPlist().get(2));
            mAnimableControl4 = new AnimableControl(sImageView4, mPositionLogic
                    .getPlist().get(3));
            mAnimableControl5 = new AnimableControl(sImageView5, mPositionLogic
                    .getPlist().get(4));

            mAnimableControl1.setDuration(mAnimateDuration);
            mAnimableControl2.setDuration(mAnimateDuration);
            mAnimableControl3.setDuration(mAnimateDuration);
            mAnimableControl4.setDuration(mAnimateDuration);
            mAnimableControl5.setDuration(mAnimateDuration);

            mControlList.add(mAnimableControl1);
            mControlList.add(mAnimableControl2);
            mControlList.add(mAnimableControl3);
            mControlList.add(mAnimableControl4);
            mControlList.add(mAnimableControl5);
            mMyLinearLayout.setControlList(mControlList);
            startAni();
            mHandler.sendMessageDelayed(mHandler.obtainMessage(0), mDelayTime);
        }
        super.onLayout(changed, l, t, r, b);
    }

    public CoverFlowApp(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs,
                R.styleable.CoverFlow);
        mAnimateDuration = typedArray.getInteger(
                R.styleable.CoverFlow_animateDuration, 500);
        mDelayTime = typedArray.getInteger(R.styleable.CoverFlow_delayTime,
                3000);
        int imageArrayID = typedArray.getResourceId(
                R.styleable.CoverFlow_images, -1);
        TypedArray images = getResources().obtainTypedArray(imageArrayID);
        LogHelper.d(TAG, "cover flow images:" + images + ": delayTime:"
                    + mDelayTime);
        initView(images);
        typedArray.recycle();
    }

    public CoverFlowApp(Context context) {
        super(context);
        this.mContext = context;
        initView(null);
    }

    /**
     * handler for refresh views with animation
     */
    @SuppressLint("HandlerLeak")
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if(mPositionLogic == null)
                    {
                      return;
                    }
                    refreshPosition();
                    mMyLinearLayout.invalidate();
                    startAni();
                    mHandler.sendMessageDelayed(mHandler.obtainMessage(0),
                            mDelayTime);
                    break;

                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    /**
     * Initialize all views
     *
     * @param images
     */
    @SuppressWarnings({
            "deprecation", "null"
    })
    private void initView(TypedArray images) {

        mLayoutInflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mLayoutInflater.inflate(R.layout.coverflow_app, this);
        mMyLinearLayout = (CoverFlowLayout) mView.findViewById(R.id.container);

        sImageView1 = (ImageView) mView.findViewById(R.id.app_first);
        sImageView2 = (ImageView) mView.findViewById(R.id.app_second);
        sImageView3 = (ImageView) mView.findViewById(R.id.app_third);
        sImageView4 = (ImageView) mView.findViewById(R.id.app_four);
        sImageView5 = (ImageView) mView.findViewById(R.id.app_five);

        if (null != images && images.length() > 4) {
            sImageView1.setBackgroundDrawable(images.getDrawable(0));
            sImageView2.setBackgroundDrawable(images.getDrawable(1));
            sImageView3.setBackgroundDrawable(images.getDrawable(2));
            sImageView4.setBackgroundDrawable(images.getDrawable(3));
            sImageView5.setBackgroundDrawable(images.getDrawable(4));
            images.recycle();
        }

        mControlList = new ArrayList<AnimableControl>();
    }

    public void onDestroy() {
        mPositionLogic = null;
        mHandler = null;
    }

    /**
     * start animation
     */
    private void startAni() {
        mAnimableControl1.startAnimation();
        mAnimableControl2.startAnimation();
        mAnimableControl3.startAnimation();
        mAnimableControl4.startAnimation();
        mAnimableControl5.startAnimation();
    }

    /**
     * refresh position of all views
     */
    private void refreshPosition() {
        mAnimableControl1.getNextPosition(mPositionLogic);
        mAnimableControl2.getNextPosition(mPositionLogic);
        mAnimableControl3.getNextPosition(mPositionLogic);
        mAnimableControl4.getNextPosition(mPositionLogic);
        mAnimableControl5.getNextPosition(mPositionLogic);
    }

}
