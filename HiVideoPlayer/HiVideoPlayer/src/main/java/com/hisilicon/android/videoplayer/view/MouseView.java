package com.hisilicon.android.videoplayer.view;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.utils.LogTool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.View;
import android.util.AttributeSet;


public class MouseView extends View {

    private Canvas mCanvas;
    private Bitmap mBuffer;
    private Paint mPaint = null;
    private Paint mPaint_text = null;
    private int StrokeWidth = 5;
    //private Rect rect = new Rect(0,0,0,0);//
    private Thread mThread;
    private Boolean isThreadWork = false;
    private final static String TAG = "MouseView";
    private float mDown_x = 30.0f;
    private float mDown_y = 24.0f;
    private float mMove_x = 1700.0f;
    private float mMove_y = 900.0f;
    private int mDrawAlpha = 150;
    private Context mContext = null;

    public MouseView(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public MouseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    public MouseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

//    public MouseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//        mContext = context;
//        initView();
//    }


    private void initView() {
        mBuffer = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBuffer);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        LogTool.d(TAG, "initView !!!!!!!!!!!!!!!!!!!!!!!");
        isThreadWork = false;
        mThread = new Thread(new Runnable() {
            /* To make getFreeSpace faster next time. */
            public void run() {
                while (true) {
                    if (isThreadWork) {
                        if (mCanvas != null) {
                            mCanvas = new Canvas(mBuffer);
                        }
                        invalidate();
                        mDrawAlpha = 150;
//                        onDraw(mCanvas);
                        draw(mCanvas);
                    } else {
                        if (mCanvas != null) {
                            invalidate();
                            mDrawAlpha = 0;
//                            onDraw(mCanvas);
                            draw(mCanvas);
                        }
                        try {
                            Thread.sleep(100);
                        } catch (IllegalArgumentException e) {
                            LogTool.e(TAG, "Thread sleep IllegalArgumentException");
                        } catch (InterruptedException e) {
                            LogTool.e(TAG, "Thread sleep InterruptedException");
                        }
                    }
                }
            }
        });
        mThread.start();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null)
            mPaint = new Paint();
        mPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(StrokeWidth);
        mPaint.setColor(Color.RED);
        mPaint.setAlpha(mDrawAlpha);
        float left = mDown_x;
        float top = mDown_y;
        float right = mMove_x;
        float bottom = mMove_y;
        canvas.drawRect(left, top, right, bottom, mPaint);
    }

    protected void onDrawText(Canvas canvas) {
//        super.onDraw(canvas);
        draw(mCanvas);
        if (mPaint_text == null)
            mPaint_text = new Paint();
        mPaint_text.setColor(Color.RED);
        mPaint_text.setAlpha(mDrawAlpha);
        mPaint_text.setAntiAlias(true);
        mPaint_text.setFilterBitmap(true);
        mPaint_text.setTextSize(60);

        LogTool.d(TAG, "onDraw ,onDrawText!!!!!");

        canvas.drawText(mContext.getResources().getString(R.string.scalenosurport), 700, 300, mPaint_text);
    }

    public void setDown(float x, float y) {
        mDown_x = x;
        mDown_y = y;
    }

    public void setMove(float x, float y) {
        mMove_x = x;
        mMove_y = y;
    }

    public void setWorkStatus(boolean enable) {
        LogTool.d(TAG, "setWorkStatus :" + enable);
        isThreadWork = enable;
    }

    public int[] getRectPoint() {
        int[] point = new int[4];
        point[0] = (int) mDown_x;
        point[1] = (int) mDown_y;
        point[2] = (int) mMove_x;
        point[3] = (int) mMove_y;
        return point;
    }

    public boolean getWorkStatus() {
        return isThreadWork;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        LogTool.d(TAG, "onKeyDown event :" + event);
        if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT) || (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
        }
        return super.onKeyDown(keyCode, event);
    }
}
