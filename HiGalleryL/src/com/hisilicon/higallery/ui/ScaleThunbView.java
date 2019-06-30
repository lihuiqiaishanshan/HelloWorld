
package com.hisilicon.higallery.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.graphics.Matrix;

public class ScaleThunbView extends View {
    Point mDisplaySize;
    String mBitmapPath;
    Context mContext;
    Bitmap mThumbBitmap;
    Rect mScreenRect;
    Rect mBitmapRect;
    Rect mBitmapRectLast;
    float mScaleLevelLast;
    float mOutSideScale;
    int mWidth;
    int mHeight;
    Paint mPaint;
    WindowManager mWM;
    WindowManager.LayoutParams mParam;

    public ScaleThunbView(Context context, Point displaySize, int rotationDegree, boolean mirror, String path) {
        super(context);
        mContext = context;
        mDisplaySize = displaySize;
        mBitmapPath = path;
        mWM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        mWM.getDefaultDisplay().getSize(size);
        mWidth = size.x / 4;
        mHeight = size.y / 4;
        mScreenRect = new Rect(0, 0, mWidth, mHeight);
        mBitmapRect = new Rect();
        mBitmapRectLast = new Rect(0, 0, mWidth, mHeight);
        mScaleLevelLast = 1.0f;
        mOutSideScale = 1.0f;
        mThumbBitmap = getScaledBitmap(rotationDegree, mirror);
        mPaint = new Paint();
        mPaint.setColor(0xff0055aa);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(6);

        mParam = new WindowManager.LayoutParams();
        mParam.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParam.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        mParam.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mParam.gravity = Gravity.RIGHT | Gravity.BOTTOM;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xff000000);
        if(null != mThumbBitmap)
        {
            canvas.drawBitmap(mThumbBitmap, null, mBitmapRect, null);
        }
        canvas.drawRect(mScreenRect, mPaint);
    }

    Bitmap getScaledBitmap(int rotationDegree, boolean mirror) {
        Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mBitmapPath, options);
        int sampleSize = 1;
        int pic_outHeight;
        int pic_outWidth;

        if (rotationDegree == 90 || rotationDegree == 270){
            pic_outWidth = options.outHeight;
            pic_outHeight = options.outWidth;
        }
        else
        {
            pic_outWidth = options.outWidth;
            pic_outHeight = options.outHeight;
        }
        if (pic_outWidth > pic_outHeight) {
            sampleSize = pic_outWidth / mWidth + 1;
        } else {
            sampleSize = pic_outHeight / mHeight + 1;
        }
        options.inSampleSize = sampleSize;
        options.inJustDecodeBounds = false;
        Bitmap bm = BitmapFactory.decodeFile(mBitmapPath, options);
        Bitmap bm_r = null;
        if (rotationDegree != 0 || mirror){
            Matrix matrix = new Matrix();
            matrix.setRotate(rotationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
            if(mirror){
                matrix.postScale(-1,1);
                matrix.postTranslate(bm.getWidth(),0);
            }
            bm_r = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        else{
            bm_r = bm;
        }
        float w = bm_r.getWidth();
        float h = bm_r.getHeight();
        float scale = Math.min(mWidth / w, mHeight / h);
        w *= scale;
        h *= scale;
        int gapW = (int) ((mWidth - w) / 2);
        int gapH = (int) ((mHeight - h) / 2);
        mBitmapRect.left = gapW;
        mBitmapRect.right = mWidth - gapW;
        mBitmapRect.top = gapH;
        mBitmapRect.bottom = mHeight - gapH;
        return bm_r;
    }

    public void show() {
        mWM.addView(this, mParam);
    }

    public void hide() {
        mWM.removeView(this);
    }

    public void setDrawnFrame(Rect frame, float scaleLevel) {
        boolean outside = frame.width() > mDisplaySize.x || frame.height() > mDisplaySize.y;
        float scale = mDisplaySize.x / (float)mWidth;
        if (outside) {
            if (scaleLevel > mScaleLevelLast) {
                mOutSideScale = mOutSideScale * 2;
            } else if (scaleLevel < mScaleLevelLast) {
                mOutSideScale = mOutSideScale / 2;
            }
            mBitmapRect.left = mBitmapRectLast.left;
            mBitmapRect.right = mBitmapRectLast.right;
            mBitmapRect.top = mBitmapRectLast.top;
            mBitmapRect.bottom = mBitmapRectLast.bottom;

            mScreenRect.left = mBitmapRect.left + (int)((0 - frame.left) / mOutSideScale / scale);
            mScreenRect.top =  mBitmapRect.top + (int)((0 - frame.top) / mOutSideScale / scale);
            mScreenRect.right = mBitmapRect.right - (int)((frame.right - mDisplaySize.x) / mOutSideScale / scale);
            mScreenRect.bottom = mBitmapRect.bottom - (int)((frame.bottom - mDisplaySize.y) / mOutSideScale / scale);
        } else {
            mOutSideScale = 1.0f;
            mScreenRect.set(0, 0, mWidth, mHeight);
            mBitmapRect.left = (int) (frame.left / scale);
            mBitmapRect.top = (int) (frame.top / scale);
            mBitmapRect.right = (int) (frame.right / scale);
            mBitmapRect.bottom = (int) (frame.bottom / scale);

            mBitmapRectLast.left = mBitmapRect.left;
            mBitmapRectLast.top = mBitmapRect.top;
            mBitmapRectLast.right = mBitmapRect.right;
            mBitmapRectLast.bottom = mBitmapRect.bottom;
        }
        mScaleLevelLast = scaleLevel;
        invalidate();
    }

}
