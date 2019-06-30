
package com.hisilicon.higallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Gallery;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.higallery.control.Controller;
import com.hisilicon.higallery.control.EventController;
import com.hisilicon.higallery.control.ExplorerController;
import com.hisilicon.higallery.control.RotateController;
import com.hisilicon.higallery.control.ScaleController;
import com.hisilicon.higallery.control.SlidingController;
import com.hisilicon.higallery.core.GalleryCore;
import com.hisilicon.higallery.utils.Utils;

import com.download.DownLoadTask;
import com.download.DownLoadListener;

import android.os.Handler;
import android.os.Message;
import java.io.File;


@SuppressWarnings("deprecation")
public class NetworkHiGallery extends Activity implements OnTouchListener {

    private static String TAG = "HiGallery";
    private static int VIDEO_LAYER_WIDTH = 4096;
    private static int VIDEO_LAYER_HEIGHT = 2160;
    private static final int INIT = 0x500;
    private String mFilePath = "";
    private int limits = 10;
    private boolean mPlayMusicOpened = false;
    static final int TYPE_NORMAL_k = 0;
    static final int TYPE_NORMAL_2k = 1;
    static final int TYPE_NORMAL_S4k = 2;
    static final int TYPE_NORMAL_4k = 3;
    static final int TYPE_NORMAL_8k = 4;
    private GalleryCore mGalleryCore;
    // private SurfaceView mSurfaceView;
    private EventController mEventController;
    private LinearLayout mInfoLayout;
    private ExplorerController mExploreController;
    private boolean onCreateFlag = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            View lastView = null;
            switch (msg.what) {
                case Utils.SHOW_INFO:
                    lastView = mInfoLayout.getChildAt(1);
                    if (lastView != null) {
                        mInfoLayout.removeView(lastView);
                    }
                    View view = (View) msg.obj;
                    mInfoLayout.addView(view, 1);
                    mHandler.removeMessages(Utils.DISMISS_INFO);
                    mHandler.sendEmptyMessageDelayed(Utils.DISMISS_INFO, 3500);
                    break;
                case Utils.DISMISS_INFO:
                    lastView = mInfoLayout.getChildAt(1);
                    if (lastView != null) {
                        mInfoLayout.removeView(lastView);
                    }
                    break;
                case INIT:
                    init((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hi_gallery);

        onCreateFlag = false;
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri.getScheme().equals("file")) {
                mHandler.removeMessages(INIT);
                Message msg = mHandler.obtainMessage(INIT);
                msg.obj = uri.getPath();
                mHandler.sendMessageDelayed(msg, 1);
                onCreateFlag = true;
            }
        }

        if(true == Utils.getBackgroundMusic(this)){
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri.getScheme().equals("file")) {
                    playMusicInBackGround(uri);
                }
            } else {
                //finish();
            }
        }
    }

    private void playMusicInBackGround(Uri musicData) {
        Intent intent = new Intent();
        intent.setClassName(getPackageName(),getPackageName()+".music.BackgroundMusicService");
        intent.setData(musicData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);
    }

    private void stopPlayMusic() {
        Intent intent = new Intent();
        intent.setClassName(getPackageName(),getPackageName()+".music.BackgroundMusicService");
        stopService(intent);
    }

    @Override
    public void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onPause() {
        if(!mFilePath.equals("")){
            File dir = new File(mFilePath);
            File[] files = dir.listFiles();
            if(null != files && files.length > limits) {
                for(File file :files){
                    if (file.getPath().endsWith(".nomedia")) {
                        continue;
                    }
                    file.delete();
                }
            }
        }
        mEventController.stopControl();
        if (mGalleryCore != null){
            mGalleryCore.deinit();
        }
        if (mExploreController != null){
            mExploreController.recycle();
        }
        onCreateFlag = false;
        super.onPause();
    }

    @Override
    protected void onResume() {
        Intent intent = getIntent();
        super.onResume();
        dealHttpIntent(getIntent());
        if(!onCreateFlag) {
            if (Intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uri = intent.getData();
                if (uri.getScheme().equals("file")) {
                    init(uri.getPath());
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        mEventController.stopControl();
        super.onDestroy();
    }

    private void init(String startPath) {
        Gallery gallery = (Gallery) findViewById(R.id.thumbnail_list);
        mGalleryCore = GalleryCore.getGallery(Looper.getMainLooper(), this);
        mGalleryCore.enablePQ(true);
        mPlayMusicOpened = Utils.getBackgroundMusic(this);

        if(Utils.getProductLine(this).equals("STB")){
            VIDEO_LAYER_WIDTH = 4096;
            VIDEO_LAYER_HEIGHT =2160;
        }else{
            int type = mGalleryCore.getFormat();
            if(TYPE_NORMAL_k == type) {
                VIDEO_LAYER_WIDTH = 1280;
                VIDEO_LAYER_HEIGHT = 720;
            }else if(TYPE_NORMAL_2k ==type){
                VIDEO_LAYER_WIDTH = 1920;
                VIDEO_LAYER_HEIGHT =1080;
            }else if(TYPE_NORMAL_S4k ==type)
            {
                VIDEO_LAYER_WIDTH = 3840;
                VIDEO_LAYER_HEIGHT =2160;
            }else if(TYPE_NORMAL_8k ==type)
            {
                VIDEO_LAYER_WIDTH = 7680;
                VIDEO_LAYER_HEIGHT =4320;
            }
        }

        mGalleryCore.init(VIDEO_LAYER_WIDTH, VIDEO_LAYER_HEIGHT);
        TextView textView = (TextView) findViewById(R.id.img_info);
        mInfoLayout = (LinearLayout) findViewById(R.id.info_layout);
        mExploreController = new ExplorerController(mGalleryCore, this, gallery, startPath,
                textView, mHandler);
        Controller[] controllers = new Controller[] {
                mExploreController, new ScaleController(mGalleryCore, this, mHandler),
                new RotateController(mGalleryCore, this, mHandler),
                new SlidingController(mGalleryCore, this, mExploreController)
        };
        String[] controllerNames = getResources().getStringArray(R.array.controllers);

        mEventController = new EventController(mGalleryCore, this, controllers, controllerNames);
        mEventController.startControl();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.picture_loading);
        mGalleryCore.setFailBitmap(bitmap);

        SharedPreferences SharedPreferences = getSharedPreferences("sliding_control", 0);
        int picAnimType = SharedPreferences.getInt("animation_animtype", 0);

        switch (picAnimType){
            case 0:
                mGalleryCore.setAnimationType((GalleryCore.AnimType.ANIM_NONE), 1000);
                break;
            case 1:
                mGalleryCore.setAnimationType((GalleryCore.AnimType.ANIM_SCALE), 1000);
                break;
            case 2:
                mGalleryCore.setAnimationType((GalleryCore.AnimType.ANIM_SLIDE), 1000);
                break;
            case 3:
                mGalleryCore.setAnimationType((GalleryCore.AnimType.ANIM_FADE), 1000);
                break;
            case 4:
                mGalleryCore.setAnimationType((GalleryCore.AnimType.ANIM_RANDOM), 1000);
                break;
            default:
                break;
        }

        // mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        // mSurfaceView.getHolder().addCallback(new Callback2() {
        //
        // @Override
        // public void surfaceDestroyed(SurfaceHolder holder) {
        // mGalleryCore.deinit();
        // }
        //
        // @Override
        // public void surfaceCreated(SurfaceHolder holder) {
        //
        // }
        //
        // @Override
        // public void surfaceChanged(SurfaceHolder holder, int format, int
        // width, int height) {
        // if (width > 0 && height > 0) {
        // mGalleryCore.initWithSurface(holder.getSurface(), width, height);
        // }
        // }
        //
        // @Override
        // public void surfaceRedrawNeeded(SurfaceHolder holder) {
        //
        // }
        // });
        //
        // mSurfaceView.setOnTouchListener(this);
    }

    @Override
    protected void onStart() {
        //mGalleryCore.enablePQ(true);
        super.onStart();
    }

    @Override
    protected void onStop() {
        //mGalleryCore.deinit();
        //mExploreController.recycle();
        super.onStop();
        if(true == mPlayMusicOpened){
            stopPlayMusic();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mEventController.onKeyEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(null == mEventController){
            return true;
        }
        return mEventController.onMotionEvent(event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mEventController.onMotionEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(null == mEventController){
            return true;
        }
        return mEventController.dispatchKeyEvent(event);
    }

    private void dealHttpIntent(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null) {
            finish();
            return;
        }

        if(uri.getScheme().equals("http") || uri.getScheme().equals("https"))
        {
            DownLoadTask downLoadTask = new DownLoadTask(new DownLoadListener() {
                @Override
                public void onLoadSuccess(String filePath) {
                    if(null != filePath) {
                        Log.d(TAG,"onSuccess");
                        mHandler.removeMessages(INIT);
                        Message msg = mHandler.obtainMessage(INIT);
                        msg.obj = filePath;
                        mHandler.sendMessageDelayed(msg, 1000);
                    }
                }
                @Override
                public void onLoadFailed(String filePath, Exception e) {
                    if(null != filePath) {
                        Log.d(TAG,"onFailed");
                        //Toast.makeText(NetworkHiGallery.this, e.toString(), Toast.LENGTH_SHORT).show();
                        mHandler.removeMessages(INIT);
                        Message msg = mHandler.obtainMessage(INIT);
                        msg.obj = filePath;
                        mHandler.sendMessageDelayed(msg, 1000);
                    }
                }
            },NetworkHiGallery.this);
            downLoadTask.execute(uri.toString());
            mFilePath = downLoadTask.getExternalCacheDir(NetworkHiGallery.this).getPath();
        }

    }
}
