
package com.hisilicon.higallery.control;

//import java.io.FileInputStream;
//import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.higallery.GalleryAdapter;
import com.hisilicon.higallery.R;
import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.core.GalleryCore.Callback;
import com.hisilicon.higallery.core.GalleryCore.CallbackWithUrl;
import com.hisilicon.higallery.core.GalleryCore.Sliding;
import com.hisilicon.higallery.core.GalleryCore.SlidingShow;
import com.hisilicon.higallery.core.GalleryCore.ViewMode;
import com.hisilicon.higallery.load.FileScanner;
import com.hisilicon.higallery.utils.FileType;
import com.hisilicon.higallery.utils.Utils;
import com.hisilicon.higallery.ui.GifView;
import java.io.File;
import java.math.BigDecimal;
import android.os.Bundle;
import android.os.Message;
import android.graphics.BitmapFactory.Options;
import com.hisilicon.higallery.core.BitmapDecodeUtils;

@SuppressWarnings("deprecation")
public class ExplorerController implements Controller, Sliding ,SlidingShow{

    public static final int SCAN_FINISH = 0;
    public static final int SCAN_FAILED = 1;
    public static final int DISMISS_THUM_VIEW = 2;
    public static final int DISMISS_DETAIL_VIEW = 3;
    public static final int UPDATE_LIST = 4;
    public static final int SLIDING_NEXT = 5;
    public static final int GIF_START_PLAY = 6;
    public static final int DETAILS_INFO = 7;

    private GalleryCore mGalleryCore;
    private Gallery mGallery;
    private GalleryAdapter mGalleryAdapter;
    private ProgressDialog mDialog;
    private String mCurrentPicturePath;
    private ArrayList<String> mFilePath;
    private int mFileSize;
    private boolean mThumViewShowing = true;
    private boolean mThumViewColsed = false;
    private int mCurrentPosition;
    private Context mContext;
    private Callback mCallback;
    private CallbackWithUrl mCallbackWithUrl;
    private TextView mImgInfo;
    private Handler mInfoHandler;
    private boolean mIsProcessing = false;
    private boolean mIsInitDone = false;
    private boolean gifDecodeDone = false;
    private boolean mScalingOrRotating = false;
    private GestureDetector mDetector;
    private ImageView mFailImage;
    private GifView mGifImage = null;
    boolean mShowingFailed;
    ProgressDialog mLoadingDialog;
    boolean mOnControl;
    Toast mTipToast;
    public boolean isGifImage;
    private long slidingDelay = 0;
    private Context ct = null;
    private int isQuick = 0;

    static final int TYPE_NORMAL_k = 0;
    static final int TYPE_NORMAL_2k = 1;
    static final int TYPE_NORMAL_S4k = 2;
    static final int TYPE_NORMAL_4k = 3;
    static final int TYPE_NORMAL_8k = 4;

    static final String TAG = "ExplorerController";

    private boolean gifDecodeState = false;

    private int mPicturePosition;
    public boolean mIsSliding = false;

    private Thread DetailsThread;

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @SuppressWarnings("unchecked")
        public void handleMessage(android.os.Message msg) {
            if (mImgInfo.getVisibility() == View.VISIBLE) {
                mHandler.sendEmptyMessageDelayed(DISMISS_DETAIL_VIEW, 3500);
            }
            switch (msg.what) {
                case SCAN_FINISH:
                    mDialog.dismiss();
                    mFilePath = (ArrayList<String>) msg.obj;
                    mFileSize = mFilePath.size();
                    initAdapter();
                    break;
                case UPDATE_LIST:
                    if (mGalleryAdapter != null)
                        mGalleryAdapter.notifyDataSetChanged();
                    break;
                case SCAN_FAILED:
                    mDialog.dismiss();
                    break;
                case DISMISS_THUM_VIEW:
                    showThumView(false);
                    mImgInfo.setVisibility(View.GONE);
                    break;
                case DISMISS_DETAIL_VIEW:
                    mImgInfo.setVisibility(View.GONE);
                    Log.d(TAG, "HiGalleryL::Hide detail view");
                    break;
                case SLIDING_NEXT:
                    showNext(slidingDelay);
                    break;
                case GIF_START_PLAY:
                    gifDecodeDone = true;
                    mGifImage.setStart();
                    //mGifImage.setVisibility(View.VISIBLE);//setVisibility can only be call from thread which created the view
                    ShowGifView();
                    break;
                case DETAILS_INFO:
                    String name = msg.getData().getString("name");
                    String size = msg.getData().getString("size");
                    String imagePath = msg.getData().getString("imagePath");
                    int width = msg.getData().getInt("width");
                    int height = msg.getData().getInt("height");

                    String details = mContext.getString(R.string.details);
                    mImgInfo.setText(String.format(details, name, width, height, size, imagePath));
                    mImgInfo.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };

    public ExplorerController(GalleryCore galleryCore, Context context, Gallery gallery,
            String filePath, TextView info, Handler handler) {
        mGalleryCore = galleryCore;
        mGallery = gallery;
        mGallery.setFocusable(false);
        mGallery.setFocusableInTouchMode(false);
        mContext = context;
        mImgInfo = info;
        mThumViewColsed = !(Utils.getThumbnail(context));

        Activity activity = (Activity)context;
        ct = context;
        mFailImage = (ImageView)activity.findViewById(R.id.fail_img);
        mGifImage = (GifView)activity.findViewById(R.id.view_gif);
        mInfoHandler = handler;
        mDetector = new GestureDetector(context, new MyGestureListener());

        mGallery.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mHandler.removeMessages(DISMISS_THUM_VIEW);
                mHandler.sendEmptyMessageDelayed(DISMISS_THUM_VIEW, 3500);
                mCurrentPosition = arg2;
                if(!mIsProcessing) {
                    mIsProcessing = true;
                   // mCurrentPosition = arg2;
                    mPicturePosition = arg2;
                    mCurrentPicturePath = mFilePath.get(mCurrentPosition);
                    viewImage(mCurrentPicturePath);
                }
            }
        });

        mCallbackWithUrl = new CallbackWithUrl() {
            @Override
            public void onReceiveCMDWithUrl(int cmd, Parcel parcel) {
                parcel.setDataPosition(0);
                boolean result = (parcel.readInt()==1);
                String url = parcel.readString();
                int hdrOrErr = parcel.readInt();
                int err = parcel.readInt();
                if (cmd == GalleryCore.CMD_INIT_COMPLETED) {
                    if (result && mCurrentPicturePath != null) {
                        mIsInitDone = true;
                        mIsProcessing = true;
                        viewImage(mCurrentPicturePath);
                    }
                } else if (cmd == GalleryCore.CMD_VIEW_COMPLETED) {
                    mFailImage.setVisibility(View.INVISIBLE);
                    mGifImage.setVisibility(View.INVISIBLE);
                    mShowingFailed = !result;
                    mIsProcessing = false;
                    ShowGifView();
                    picPosition();
                    checkError(result, url, hdrOrErr);
                    showDecode();
                    mShowingFailed = !result;
                    if (mLoadingDialog != null)
                        mLoadingDialog.dismiss();
                }
            }
        };
        mTipToast = Toast.makeText(context, R.string.already_last, Toast.LENGTH_SHORT);
        initView();
        scanFile(filePath);
    }

    public void picPosition(){
        if(mCurrentPosition > mPicturePosition && mCurrentPosition > 0 ){
            if(mPicturePosition == mFileSize - 1 && mCurrentPosition + 1 > mFileSize){
                mCurrentPosition = mFileSize - 1;
                return;
            }
            next();
        }else if(mCurrentPosition < mPicturePosition){
            if(mPicturePosition == 0 && mCurrentPosition < 0){
                mCurrentPosition = 0;
                return;
            }
            prev();
        }
    }

    public void checkError(boolean result, String url, int hdrOrErr){
        if(!result && hdrOrErr == GalleryCore.CMD_DECODE_OUT_OF_MEMORY_ERROR){
            mFailImage.setVisibility(View.VISIBLE);
            // this toast had been defined into the GalleryImple.java
            //mTipToast = Toast.makeText(ct, "ERR : Have not enough memory size to decode this pic", Toast.LENGTH_SHORT);
            //mTipToast.show();
        }
        else if (!result && hdrOrErr != GalleryCore.CMD_CANCELED) {
            mFailImage.setVisibility(View.VISIBLE);
            String fileType = url.substring(url.lastIndexOf(".") + 1, url.length()).toUpperCase();
            if(!fileType.equals("JPG") && !fileType.equals("JPEG") && !fileType.equals("PNG") && !fileType.equals("BMP") && !fileType.equals("GIF")){
                mTipToast = Toast.makeText(ct, R.string.file_not_support, Toast.LENGTH_SHORT);
                mTipToast.show();
                Log.d(TAG, "onReceiveCMDWithUrl CMD_VIEW_COMPLETED FailImage [" + url + "] fileType="+fileType+" ERR : File type is not supported");
            }

        } else {
            mFailImage.setVisibility(View.INVISIBLE);
            if(result)
                Log.d(TAG, "onReceiveCMDWithUrl CMD_VIEW_COMPLETED success [" + url + "]");
            else
                Log.d(TAG, "onReceiveCMDWithUrl CMD_VIEW_COMPLETED canceled [" + url + "]");
        }

    }
    private void showDecode(){
      int decode = mGalleryCore.getDecode();
      switch(decode){
          case 1:
              showToast(R.string.decode_failed);
              break;
          case 2:
              showToast(R.string.no_enough_memory);
              break;
          case 3:
              showToast(R.string.decode_failed);
              break;
         default:
              break;
        }
    }

    private void showToast(String error){
            Toast.makeText(ct, error, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int resource){
            Toast.makeText(mContext, resource, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyEvent(KeyEvent event) {
        mHandler.removeMessages(DISMISS_THUM_VIEW);
        mHandler.sendEmptyMessageDelayed(DISMISS_THUM_VIEW, 3500);

        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (!mThumViewShowing) {
                    showThumView(true);
                    mGallery.setSelection(mCurrentPosition);
                } else {
                    showThumView(false);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mImgInfo.getVisibility() != View.VISIBLE) {
                    mHandler.removeMessages(DISMISS_DETAIL_VIEW);
                    getDetails(mContext, mCurrentPicturePath, mHandler, mImgInfo);
                    Log.d(TAG, "HiGalleryL::Show detail view");
                } else {
                    mImgInfo.setVisibility(View.GONE);
                    Log.d(TAG, "HiGalleryL::Hide detail view");
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    mCurrentPosition --;
                    quickShow();
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    isQuick =0;
                    if(mIsProcessing){
                        break;
                    }else{
                        prev();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    mCurrentPosition ++;
                    quickShow();
                }
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    isQuick = 0;
                    if(mIsProcessing){
                           break;
                    }else{
                        next();
                    }
                }
                return true;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                return true;
        }
        return false;
    }

    public void quickShow() {
        if(mTipToast != null)
        {
            mTipToast.cancel();
            mTipToast = Toast.makeText(ct, R.string.already_last, Toast.LENGTH_SHORT);
        }

        if(mCurrentPosition >= 0 && mCurrentPosition + 1 <= mFileSize){
            mGallery.setSelection(mCurrentPosition);
            isQuick ++;
            if(isQuick >1 && !mThumViewShowing) {
                showThumView(true);
                Log.d(TAG, "HiGalleryL::Show thumber view");
            }
        }else if(mCurrentPosition < 0){
            mTipToast.setText(R.string.already_first);
            mTipToast.show();
        }else if (mCurrentPosition + 1 > mFileSize) {
            mTipToast.setText(R.string.already_last);
            mTipToast.show();
        }
    }

    @Override
    public boolean onMotionEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return onKeyEvent(event);
    }

    @Override
    public void startControl() {
        mGalleryCore.setCallback(mCallback);
        mGalleryCore.setCallbackWithUrl(mCallbackWithUrl);
        showThumView(true);
        Log.d(TAG, "HiGalleryL::Show thumber view");
        Utils.showInfo(mContext, mInfoHandler, Utils.EXPLORE_MODE);
        mOnControl = true;
        if (isGifImage && mGifImage.getFrameCount() > 1
            && Utils.getProductLine(mContext).equals("DPT")) {
            mGifImage.setVisibility(View.VISIBLE);
        }

    }

    public void setIsSliding(boolean isSliding){
        mIsSliding = isSliding;
    }

    public void setScalingOrRotating(boolean scalingOrRotating){
        mScalingOrRotating = scalingOrRotating;
    }


    @Override
    public void stopControl() {
        showThumView(false);
        Log.d(TAG, "HiGalleryL::Hide thumber view");
        mInfoHandler.sendEmptyMessage(Utils.DISMISS_INFO);
        mOnControl = false;
    }

    private void showThumView(boolean show) {
        if(mThumViewColsed){
            mThumViewShowing = false;
        }else{
            mThumViewShowing = show;
        }
        if (mThumViewShowing) {
            mGallery.setVisibility(View.VISIBLE);
            mHandler.removeMessages(DISMISS_THUM_VIEW);
            mHandler.sendEmptyMessageDelayed(DISMISS_THUM_VIEW, 3500);
        } else {
            mGallery.setVisibility(View.GONE);
            mThumViewShowing = false;
        }
    }

    private void initView() {
        WindowManager mWM = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point screenSize = new Point();
        mWM.getDefaultDisplay().getSize(screenSize);
        int screenWidth = screenSize.x;
        int screenHeight = screenSize.y;
        int size = screenWidth < screenHeight ? screenHeight / 8 : screenWidth / 8;
        mGalleryAdapter = new GalleryAdapter(mContext, null, mGalleryCore, mGallery, size);
        if(false == mThumViewColsed){
            mGallery.setAdapter(mGalleryAdapter);
        }
    }

    private void scanFile(String filePath) {
        mCurrentPicturePath = filePath;
        if(mCurrentPicturePath.length() != 0) {
            String parentPath = mCurrentPicturePath.substring(0, mCurrentPicturePath.lastIndexOf("/"));
            FileScanner scanner = new FileScanner(mHandler, parentPath);
            new Thread(scanner).start();

            mDialog = new ProgressDialog(mContext);
            mDialog.setMessage(mContext.getString(R.string.scanning));
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            //mDialog.show();
        }
    }

    private void initAdapter() {
        if (mFilePath != null) {
            mCurrentPosition = mFilePath.indexOf(mCurrentPicturePath);
            mPicturePosition = mCurrentPosition;
            mGalleryAdapter.updateDate(mFilePath);
            mGallery.setSelection(mCurrentPosition);
        }
    }

    private void next() {
        synchronized(this){
        SharedPreferences SharedPreferences = mContext.getSharedPreferences("sliding_control", 0);
        int picAnimType = SharedPreferences.getInt("animation_animtype", 1);
        int slidingAnimType = SharedPreferences.getInt("sliding_animtype", 1);
        if (getAnimation(picAnimType) == GalleryCore.AnimType.ANIM_RANDOM ){
            if (mIsSliding) {
                mGalleryCore.setAnimationType(getAnimation(slidingAnimType), 1000);
            } else {
                mGalleryCore.setAnimationType(getAnimation(picAnimType), 1000);
            }
        }
        if(mFileSize <= 0){
            return ;
        }
        if ((!mIsProcessing && mCurrentPosition <= mFileSize - 1 && mCurrentPosition >= 0 && mPicturePosition != mFileSize - 1) || (mIsSliding && mPicturePosition == mFileSize -1)) {
            //mCurrentPosition++;
            mPicturePosition = mCurrentPosition;
            Log.d(TAG, "mPicturePosition = "+mPicturePosition);
            mCurrentPicturePath = mFilePath.get(mCurrentPosition);
            mIsProcessing = true;
            viewImage(mCurrentPicturePath);
            mImgInfo.setVisibility(View.GONE);
            mGallery.setSelection(mCurrentPosition);
        } else if (mCurrentPosition >= mFileSize){
            mCurrentPosition = mFileSize -1;
            if(mPicturePosition != mFileSize -1 && !mIsProcessing){
                mPicturePosition = mFileSize -1;
                mCurrentPicturePath = mFilePath.get(mPicturePosition);
                mIsProcessing = true;
                viewImage(mCurrentPicturePath);
                mImgInfo.setVisibility(View.GONE);
            }
        }
        }
    }

    private void prev() {
        synchronized(this){
        SharedPreferences SharedPreferences = mContext.getSharedPreferences("sliding_control", 0);
        int picAnimType = SharedPreferences.getInt("animation_animtype", 1);
        int slidingAnimType = SharedPreferences.getInt("sliding_animtype", 1);
        if (getAnimation(picAnimType) == GalleryCore.AnimType.ANIM_RANDOM ){
            if (mIsSliding) {
                mGalleryCore.setAnimationType(getAnimation(slidingAnimType), 1000);
            } else {
                mGalleryCore.setAnimationType(getAnimation(picAnimType), 1000);
            }
        }
        if(mFileSize <= 0){
            return ;
        }
        if (!mIsProcessing && mCurrentPosition >= 0 && mCurrentPosition <= mFileSize -1 && mPicturePosition != 0) {
            mPicturePosition = mCurrentPosition;
            Log.d(TAG, "mPicturePosition = "+mPicturePosition);
            mCurrentPicturePath = mFilePath.get(mCurrentPosition);
            mIsProcessing = true;
            viewImage(mCurrentPicturePath);
            mImgInfo.setVisibility(View.GONE);
            mGallery.setSelection(mCurrentPosition);
        } else if (mCurrentPosition < 0){
            mCurrentPosition = 0;
            if(mPicturePosition != 0 && !mIsProcessing){
                mPicturePosition = 0;
                mCurrentPicturePath = mFilePath.get(mPicturePosition);
                mIsProcessing = true;
                viewImage(mCurrentPicturePath);
                mImgInfo.setVisibility(View.GONE);
            }
        }
        }
    }

        GalleryCore.AnimType getAnimation(int type) {
        switch (type) {
            case 0:
                return GalleryCore.AnimType.ANIM_NONE;
            case 1:
                return GalleryCore.AnimType.ANIM_SCALE;
            case 2:
                return GalleryCore.AnimType.ANIM_SLIDE;
            case 3:
                return GalleryCore.AnimType.ANIM_FADE;
            default:
                return GalleryCore.AnimType.ANIM_RANDOM;
        }
    }

    public void showNext(long delay) {
        slidingDelay = delay;
        mGalleryCore.setCallback(mCallback);
        mGalleryCore.setCallbackWithUrl(mCallbackWithUrl);
        if (mCurrentPosition + 1 > mFileSize - 1) {
            mCurrentPosition = -1;
        }
        Log.d(TAG, "show next mIsProcessing" + mIsProcessing);
        mCurrentPosition++;
        next();
        if(!mIsProcessing && isGifImage){
            Log.d(TAG, "gif start control" + mIsProcessing);
            mHandler.removeMessages(SLIDING_NEXT);
            mHandler.sendEmptyMessageDelayed(SLIDING_NEXT, slidingDelay);
        }
    }

    public void stopGifPlay() {
        mHandler.removeMessages(SLIDING_NEXT);
    }

    public void recycle() {
        mGalleryAdapter.recycle();
        mGifImage.setStop();
        gifDecodeState = true;
    }

    private void viewImage(String path) {
        mGifImage.setStop();
        mGalleryCore.viewImage(path, ViewMode.AUTO_MODE);
        mScalingOrRotating = false;
        if(FileType.isGifImage(path))
        {
            isGifImage = true;
            showGif(path);
            Log.d(TAG, "viewImage gif mIsProcessing" + mIsProcessing);
        }
        else {
            isGifImage = false;
        }

        if (mOnControl && mLoadingDialog != null)
            mLoadingDialog.show();
    }

    private void showGif(final String path){
        mGifImage.setStopFlag(false);
        //gifDecodeState = false;
        gifDecodeDone = false;
        Thread mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized(ExplorerController.class){
                if(gifDecodeState) {
                    return;
                }
                gifDecodeState = true;
                boolean res = mGifImage.setSrc(path);
                gifDecodeState = false;

                if (!res) {
                    Log.e(TAG, "failed to decode gif");
                    return;
                }
                mHandler.sendEmptyMessage(GIF_START_PLAY);
                }
            }
        });
        mThread.start();
    }

    public void HideGifView() {
        mGifImage.setVisibility(View.INVISIBLE);
    }

    public void ShowGifView() {
        if(!mIsProcessing && gifDecodeDone && mGifImage.getFrameCount() > 1){
            gifDecodeDone = false;
            if(!mScalingOrRotating)
                mGifImage.setVisibility(View.VISIBLE);
        }
    }

    class MyGestureListener extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1 == null || e2 == null) {
                return false;
            }
            if (e1.getX() - e2.getX() > 0 && Math.abs(velocityX) > 200) {
                mCurrentPosition = mCurrentPosition + 1;
                if(mCurrentPosition + 1 > mFileSize){
                    mTipToast.setText(R.string.already_last);
                    mTipToast.show();
                }
                next();
            } else if (e2.getX() - e1.getX() > 0 && Math.abs(velocityX) > 200) {
                mCurrentPosition = mCurrentPosition - 1;
                if(mCurrentPosition  < 0 ){
                    mTipToast.setText(R.string.already_first);
                    mTipToast.show();
                }
                prev();
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if(mThumViewColsed){
                mThumViewShowing = false;
            }
            if (!mThumViewShowing) {
                showThumView(true);
                Log.d(TAG, "HiGalleryL::Show thumber view");
                mGallery.setSelection(mCurrentPosition);
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            showThumView(false);
            Log.d(TAG, "HiGalleryL::Hide thumber view");
            mSlidingShow.startSlidingShow();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }
    }

    private SlidingShow mSlidingShow;

    public void setSliding(SlidingShow slidingShow) {
        mSlidingShow = slidingShow;
    }

    public void startSlidingShow(){;}
    public void showMenu(){;}
    /*public interface SlidingShow {
        //void startSlidingShow();

        //void showMenu();
    }*/

    public void getDetails(final Context context, final String imagePath, Handler handler,
            final TextView textView) {
        if(DetailsThread != null && DetailsThread.isAlive()){
            try {
                DetailsThread.join();
                DetailsThread = null;
            } catch (InterruptedException e) {
                Log.e(TAG,"in run",e);
            }
        }

        DetailsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Options options = BitmapDecodeUtils.getOptions(imagePath);
                int width = options.outWidth;
                int height = options.outHeight;
                File file = new File(imagePath);
                String name = file.getName();
                String size;
                float length = (float)file.length() / 1024;
                if (length < 1024) {
                    BigDecimal bd = new BigDecimal(length);
                    length = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                    size = length + "KB";
                } else {
                    length /= 1024;
                    BigDecimal bd = new BigDecimal(length);
                    length = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
                    size = length + "MB";
                }

                Bundle bundle=new Bundle();
                bundle.putString("name", name);
                bundle.putString("size", size);
                bundle.putString("imagePath", imagePath);
                bundle.putInt("width", width);
                bundle.putInt("height", height);

                Message message = new Message();
                message.setData(bundle);
                message.what = DETAILS_INFO;
                handler.sendMessage(message);
                String details = context.getString(R.string.details);
                Log.d(TAG, "HiGalleryL::Picture Detail" + String.format(details, name, width, height, size, imagePath));
            }
        });
        DetailsThread.start();
    }

}
