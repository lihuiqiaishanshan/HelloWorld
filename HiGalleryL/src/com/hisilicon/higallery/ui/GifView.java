package com.hisilicon.higallery.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.hisilicon.higallery.util.GifDecoder;

public class GifView extends View{
    private static String TAG = "GifView";
    private GifDecoder gDecoder;
    private boolean isStop = false;
    private int delta = 1;

    private Bitmap bmp;
    private InputStream is;

    private android.graphics.Rect src = new android.graphics.Rect();
    private android.graphics.Rect dst = new android.graphics.Rect();

    private GifShowThread updateTimer;
    private String path;


    /**
     *  construct - refer for java
     * @param context
     */
    public GifView(Context context) {
        this(context, null);

    }

    /**
     *  construct - refer for xml
     * @param context
     * @param attrs
     */
    public GifView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDelta(1);
    }

    /**
     * stop - recycle resource(bimap,Inputstream)
     * @param stop
     */
    public void setStop() {
        if(updateTimer != null){
            try{
                updateTimer.setStop(true);
                updateTimer.join();
                updateTimer = null;
            }catch(InterruptedException e){
               Log.e(TAG,e.toString(),e);
            }
        }

        synchronized(GifView.class){
            if(gDecoder != null){
                gDecoder.interruptDecode();
                gDecoder.bitmapRecycle();
            }

            if(bmp != null){
               bmp.recycle();
               bmp = null;
            }
        }
    }

    /**
     * start
     */
    public void setStart() {
        if(updateTimer != null && updateTimer.isAlive()){
            updateTimer.setStop(true);
            try {
                updateTimer.join();
                updateTimer = null;
            } catch (InterruptedException e) {
                Log.e(TAG,"in run",e);
            }
        }

        updateTimer = new GifShowThread();
        updateTimer.start();
    }

    public void setStopFlag(boolean flag) {
        if (updateTimer != null) {
            updateTimer.setStop(flag);
        }
    }

    public int getFrameCount(){
        if(null != gDecoder) {
            return gDecoder.getFrameCount();
        }
        else {
            return 0;
        }
    }

    /**
     * Through the subscript the zoomed image display
     * @param id
     */
    public boolean setSrc(String path) {
        if(bmp != null){
            bmp.recycle();
            bmp = null;
        }
        if(gDecoder == null){
        }else{
            gDecoder.reset();
            gDecoder = null;
        }
        this.path = path;
        gDecoder = new GifDecoder();

        try {
            is = new FileInputStream(path);
            if(gDecoder != null){
                gDecoder.read(is);
                    bmp = gDecoder.getImage();// first
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG,"in run FileNotFoundException");
        }

        if (gDecoder.err()) {
            return false;
        }

        this.postInvalidate();

        return true;
    }

    public String getSrc()
    {
        return path;
    }

    public boolean decodeBitmapFromNet(String path){
        if(bmp != null){
            bmp.recycle();
            bmp = null;
        }
        if(gDecoder == null){
        }else{
            gDecoder.reset();
            //gDecoder.resetFrame();
        }
        gDecoder = new GifDecoder();
        try {
            is = new URL(path).openStream();
            if(gDecoder != null){
                gDecoder.read(is);
                    bmp = gDecoder.getImage();
            }
        } catch (MalformedURLException e) {
            Log.e(TAG,"in run",e);
        } catch (IOException e) {
            Log.e(TAG,"in run",e);
        }
        this.postInvalidate();
        return true;
    }

    public void setDelta(int time) {
        delta = time;
    }

    public int getPlayTimeEachFrame(){
        if(gDecoder != null){
            return Math.max(100, gDecoder.nextDelay()) / delta;
        }
        return 0;
    }

    public int getPlayTime(){
        if(gDecoder != null){
            return gDecoder.getFrameCount() * getPlayTimeEachFrame();
        }
        return 0;
    }

/*  @Override
    public void layout(int arg0, int arg1, int arg2, int arg3) {
        super.layout(0, 0, 1920, 1080);
    }
*/

    protected void onDraw(Canvas canvas) {
        synchronized(GifView.class){
            if (bmp != null && !bmp.isRecycled()) {
                Paint paint = new Paint();
                src.left = 0;
                src.top = 0;
                src.bottom = bmp.getHeight();
                src.right = bmp.getWidth();
                dst.left = 0;
                dst.top = 0;
                dst.bottom = this.getHeight();
                dst.right = this.getWidth();

                center();
                canvas.drawBitmap(bmp, src, dst, paint);
                bmp = gDecoder.nextBitmap();
            }
        }
    }

    protected void center() {
        bmp = resizeDownIfTooBig(bmp, false);
        float height = bmp.getHeight();
        float width = bmp.getWidth();
        float deltaX = 0, deltaY = 0;
        int viewHeight = getHeight();
        if (height <= viewHeight) {
            deltaY = (viewHeight - height) / 2 - src.top;
        }  else if (src.top > 0) {
            deltaY = -src.top;
        } else if (src.bottom < viewHeight) {
            deltaY = getHeight() - src.bottom;
        }

        int viewWidth = getWidth();
        if (width <= viewWidth) {
            deltaX = (viewWidth - width) / 2 - src.left;
        } else if (src.left > 0) {
            deltaX = -src.left;
        } else if (src.right < viewWidth) {
            deltaX = viewWidth - src.right;
        }

        dst.top = src.top + (int)deltaY;
        dst.left = src.left + (int)deltaX;
        dst.bottom = bmp.getHeight() + (int)deltaY;
        dst.right = bmp.getWidth() + (int)deltaX;

        src.left = 0;
        src.top = 0;
        src.bottom = bmp.getHeight();
        src.right = bmp.getWidth();
    }

    // Resize the bitmap if each side is >= targetSize * 2
    private Bitmap resizeDownIfTooBig(Bitmap bitmap, boolean recycle) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        float scale = Math.min((float) getWidth() / srcWidth,
                      (float) getHeight() / srcHeight);
        if (scale > 1.0f) {
            return bitmap;
        }
        return resizeBitmapByScale(bitmap, scale, recycle);
    }

    private Bitmap resizeBitmapByScale(Bitmap bitmap, float scale, boolean recycle) {
        int width = Math.round(bitmap.getWidth() * scale);
        int height = Math.round(bitmap.getHeight() * scale);
        if (width == bitmap.getWidth() && height == bitmap.getHeight()) {
            return bitmap;
        }
        Bitmap target = Bitmap.createBitmap(width, height, getConfig(bitmap));
        Canvas canvas = new Canvas(target);
        canvas.scale(scale, scale);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        if (recycle) {
            bitmap.recycle();
        }
        return target;
    }

    private Bitmap.Config getConfig(Bitmap bitmap) {
        Bitmap.Config config = bitmap.getConfig();
        if (config == null) {
            config = Bitmap.Config.ARGB_8888;
        }
        return config;
    }

    class GifShowThread extends Thread {
        private volatile boolean isStop = false;

        @Override
        public void run() {
            while (!isStop) {
                GifView.this.postInvalidate();
                try {
                    Thread.sleep(getPlayTimeEachFrame());
                } catch (InterruptedException e) {
                    Log.e(TAG,"in run",e);
                }
            }

        }

        public void setStop(boolean stop){
            this.isStop = stop;
        }
    }
}
