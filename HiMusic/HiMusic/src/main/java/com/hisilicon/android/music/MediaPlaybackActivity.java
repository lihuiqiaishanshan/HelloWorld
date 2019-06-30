/*
 * Copyright (C) 2007 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hisilicon.android.music;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.android.music.MusicUtils.ServiceToken;
import com.hisilicon.android.music.util.Audio;
import com.hisilicon.android.music.util.Lyric;
import com.hisilicon.android.music.util.LyricView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;

/*for lint
public class MediaPlaybackActivity extends Activity implements MusicUtils.Defs,
    View.OnTouchListener, View.OnLongClickListener
*/
public class MediaPlaybackActivity extends Activity implements MusicUtils.Defs {
    private static final int USE_AS_RINGTONE = CHILD_MENU_BASE;
    private static final String TAG = "MediaPlaybackActivity";
    private static final Long OPERATING_SPACE_TIME = 500L;

    private boolean mSeeking = false;
    private boolean mPowerdown = true;
    private boolean mDeviceHasDpad;
    private long mStartSeekPos = 0;
    private long mLastSeekEventTime;
    private WakeLock mWakeLock;
    private IMediaPlaybackService mService = null;
    private RepeatingImageButton mPrevButton;
    private ImageButton mPauseButton;
    private RepeatingImageButton mNextButton;
    private ImageButton mRepeatButton;
    private ImageButton mShuffleButton;
    private ImageButton mQueueButton;
    private ImageButton mImageButton;
    private Worker mAlbumArtWorker;
    private AlbumArtHandler mAlbumArtHandler;
    private Toast mToast;
    private int mTouchSlop;
    private boolean mIsWindoDestory = false;

    //public static Lyric mLyric; //for fority
    private static LyricView lyricView;
    boolean isPlay = false;
    private Camera mCamera = new Camera();

    private ServiceToken mToken;
    public long mCurrentElapseTime = 0;

    private long mPosition;
    private boolean isPlaying = false;
    private static MediaFileList mediaFileList = null;
    private static MediaFileListService mediaFileListService = null;
    public MyServiceConnection conn = null;
    private String currPlayPath = null;
    private static String HOME = "";
    private boolean playImage;

    /**
     * UiUpdateThread is stop? , default is true in order to create a new thread.
     */
    private boolean isUiUpdateThreadStop = true;
    private Uri mUri = null;
    Bitmap mBitmap = null;

    public MediaPlaybackActivity() {
    }

    private BroadcastReceiver powerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action) {
                return;
            }

            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (mWakeLock != null) {
                    if (mWakeLock.isHeld()) {
                        mWakeLock.release();
                    }
                }

                if (mService == null) {
                    return;
                }

                if (mPowerdown) {
                    try {
                        if (mService.isPlaying()) {
                            acquireWakeLock();
                            isPlaying = true;
                            doPauseResume();
                            mWakeLock.release();
                        }
                    } catch (RemoteException ex) {
                        Log.e(TAG, "mPowerdown:RemoteException", ex);
                    }
                }

                try {
                    mPosition = mService.position();
                } catch (RemoteException ex) {
                    Log.e(TAG, "mService.position:RemoteException", ex);
                }
            }

            if (action.equals(Intent.ACTION_SCREEN_ON)) {
                mProgress.setProgress((int) (1000 * mPosition / mDuration));

                try {
                    if (mService != null && !mService.isPlaying()) {
                        if (isPlaying) {
                            doPauseResume();
                            isPlaying = false;
                        }
                    }
                } catch (RemoteException ex) {
                    Log.e(TAG, "scanBackward:RemoteException", ex);
                }
            }
        }
    };

    private void acquireWakeLock() {
        synchronized (mWakeLock) {
            try {
                mWakeLock.acquire();
            } catch (RuntimeException e) {
                Log.e(TAG, "RuntimeException in acquireWakeLock()", e);
            }
        }
    }

    private void registerpowerReceiver() {
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(Intent.ACTION_SCREEN_OFF);
        powerFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(powerReceiver, powerFilter);
    }

    private void registerUsbReceiver() {
        IntentFilter usbFilter = new IntentFilter();
        usbFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        usbFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        usbFilter.addDataScheme("file");
        registerReceiver(usbReceiver, usbFilter);
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (null == intent) {
                return;
            }
            String action = intent.getAction();
            if (null == action) {
                return;
            }

            if (action.equals(Intent.ACTION_MEDIA_REMOVED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                Log.i(TAG, "ACTION_MEDIA_REMOVED or ACTION_MEDIA_UNMOUNTED");
                Uri uri = intent.getData();
                if (null == uri) {
                    return;
                }
                String usbPath = uri.getPath();
                if (null == usbPath || null == mService) {
                    return;
                }
                String currentPath = null;
                try {
                    currentPath = mService.getPath();
                } catch (RemoteException e) {
                    Log.e(TAG, "Service get Path error", e);
                }
                if (null != currentPath && currentPath.startsWith(usbPath)) {
                    Log.d(TAG, "usb device has been removed");
                    ForceExitDialog(context);
                }
            }
        }
    };

    private void ForceExitDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        builder.setTitle(R.string.force_exit);
        builder.setMessage(getString(R.string.external_storage_removed));
        builder.setPositiveButton(R.string.delete_confirm_button_text, null);
        Dialog mDialog = builder.create();
        mDialog.setOnDismissListener(
                new OnDismissListener() {
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                    }
                }
        );
        mDialog.show();
    }

    public class MyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mediaFileListService = ((MediaFileListService.MyBinder) service)
                    .getService();
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mAlbumArtWorker = new Worker("album art worker");
        mAlbumArtHandler = new AlbumArtHandler(mAlbumArtWorker.getLooper());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.audio_player);
        Log.d(TAG,"HiMusic Oncreate New");
        mCurrentTime = (TextView) findViewById(R.id.currenttime);
        mTotalTime = (TextView) findViewById(R.id.totaltime);
        mProgress = (ProgressBar) findViewById(android.R.id.progress);
        mAlbum = (ImageView) findViewById(R.id.album);
        mArtistName = (RotateTextView) findViewById(R.id.artistname);
        mAlbumName = (RotateTextView) findViewById(R.id.albumname);
        mTrackName = (RotateTextView) findViewById(R.id.trackname);
        lyricView = (LyricView) findViewById(R.id.audio_lrc);
/*for lint
        View v = (View) mArtistName.getParent();
        v.setOnTouchListener(this);
        v.setOnLongClickListener(this);
        v = (View) mAlbumName.getParent();
        v.setOnTouchListener(this);
        v.setOnLongClickListener(this);
        v = (View) mTrackName.getParent();
        v.setOnTouchListener(this);
*/
        mItemImage = (ImageView) findViewById(R.id.ItemImage);
        mItemTitle = (TextView) findViewById(R.id.ItemTitle);
        mItemPath = (TextView) findViewById(R.id.ItemText);
        mPrevButton = (RepeatingImageButton) findViewById(R.id.prev);
        mPrevButton.setOnClickListener(mPrevListener);
        mPrevButton.setRepeatListener(mRewListener, 260);
        mPauseButton = (ImageButton) findViewById(R.id.pause);
        mPauseButton.requestFocus();
        mPauseButton.setOnClickListener(mPauseListener);
        mNextButton = (RepeatingImageButton) findViewById(R.id.next);
        mNextButton.setOnClickListener(mNextListener);
        mNextButton.setRepeatListener(mFfwdListener, 260);
        seekmethod = 1;
        mDeviceHasDpad = (getResources().getConfiguration().navigation == Configuration.NAVIGATION_DPAD);
        mQueueButton = (ImageButton) findViewById(R.id.curplaylist);
        mQueueButton.setOnClickListener(mQueueListener);
        mImageButton = (ImageButton) findViewById(R.id.curimagelist);
        mImageButton.setOnClickListener(mImageListener);
        if (isIptvEnable()) {
            mImageButton.setVisibility(View.VISIBLE);
        } else {
            mImageButton.setVisibility(View.GONE);
        }
        mShuffleButton = ((ImageButton) findViewById(R.id.shuffle));
        mShuffleButton.setOnClickListener(mShuffleListener);
        mRepeatButton = ((ImageButton) findViewById(R.id.repeat));
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        if (mProgress instanceof SeekBar) {
            SeekBar seeker = (SeekBar) mProgress;
            seeker.setOnSeekBarChangeListener(mSeekListener);
            seeker.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                    // TODO Auto-generated method stub
                    if (arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT || arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        try {
                            if (mService != null) {
                                //seek by Remote Controller, long press will cause noise. So pause when seek.
                                //Resume to play after seek no mater it was pause or playing before seek.
                                if (arg2.getAction() == KeyEvent.ACTION_DOWN) {
                                    if (mService.isPlaying()) {
                                        mService.pause();
                                    }
                                } else {
                                    if (!mService.isPlaying() &&
                                            !(Common.isLastMediaFile() &&
                                                    MediaPlaybackService.getPlayMode() == MediaPlaybackService.SEQUENCE_PLAYER &&
                                                    mService.position() == mService.duration() &&
                                                    arg2.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT)) {
                                        mService.play();
                                    }
                                }
                            }
                        } catch (RemoteException ex) {
                            Log.e(TAG, "onKey:RemoteException", ex);
                        }
                        long now = SystemClock.elapsedRealtime();
                        if ((now - mLastSeekEventTime) > 250) {
                            mLastSeekEventTime = now;
                            return false;
                        } else {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }
            });
        }

        mProgress.setMax(1000);
        if (SystemProperties.get("persist.suspend.mode").equals("deep_resume")) {
            registerpowerReceiver();
        }
        registerUsbReceiver();
        mTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        Intent intent = getIntent();
        mediaFileList = intent.getParcelableExtra("MediaFileList");

        if (mediaFileList != null && mediaFileList.getId() == 1) {
            Intent service = new Intent(Constants.ACTION);
            service.setClassName("com.hisilicon.android.music",
                    "com.hisilicon.android.music.MediaFileListService");
            conn = new MyServiceConnection();
            MediaPlaybackActivity.this.bindService(service, conn,
                    Context.BIND_AUTO_CREATE);
        }

        if (Environment.getExternalStorageDirectory().exists()) {
            HOME = Environment.getExternalStorageDirectory().toString()
                    + "/music";
        } else {
            HOME = "/var/music";
        }
        playImage = false;
        mToken = MusicUtils.bindToService(this, osc);
        startPlayback();
        if (mToken == null) {
            // something went wrong
            mHandler.sendEmptyMessage(QUIT);
        }

        updateTrackInfo();
        IntentFilter f = new IntentFilter();
        f.addAction(MediaPlaybackService.PLAYSTATE_CHANGED);
        f.addAction(MediaPlaybackService.META_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));
    }

    int mInitialX = -1;
    int mLastX = -1;
    int mTextWidth = 0;
    int mViewWidth = 0;
    boolean mDraggingLabel = false;
    /*for lint
        TextView textViewForContainer(View v) {
            View vv = v.findViewById(R.id.artistname);

            if (vv != null)
            { return (TextView) vv; }

            vv = v.findViewById(R.id.albumname);

            if (vv != null)
            { return (TextView) vv; }

            vv = v.findViewById(R.id.trackname);

            if (vv != null)
            { return (TextView) vv; }

            return null;
        }

        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            TextView tv = textViewForContainer(v);

            if (tv == null) {
                return false;
            }

            if (action == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(0xff606060);
                mInitialX = mLastX = (int) event.getX();
                mDraggingLabel = false;
            } else if (action == MotionEvent.ACTION_UP
                     || action == MotionEvent.ACTION_CANCEL) {
                v.performClick();
                v.setBackgroundColor(0);

                if (mDraggingLabel) {
                    Message msg = mLabelScroller.obtainMessage(0, tv);
                    mLabelScroller.sendMessageDelayed(msg, 1000);
                }
            } else if (action == MotionEvent.ACTION_MOVE) {
                if (mDraggingLabel) {
                    int scrollx = tv.getScrollX();
                    int x = (int) event.getX();
                    int delta = mLastX - x;

                    if (delta != 0) {
                        mLastX = x;
                        scrollx += delta;

                        if (scrollx > mTextWidth) {
                            // scrolled the text completely off the view to the left
                            scrollx -= mTextWidth;
                            scrollx -= mViewWidth;
                        }

                        if (scrollx < -mViewWidth) {
                            // scrolled the text completely off the view to the
                            // right
                            scrollx += mViewWidth;
                            scrollx += mTextWidth;
                        }

                        tv.scrollTo(scrollx, 0);
                    }

                    return true;
                }

                int delta = mInitialX - (int) event.getX();

                if (Math.abs(delta) > mTouchSlop) {
                    // start moving
                    mLabelScroller.removeMessages(0, tv);

                    // Only turn ellipsizing off when it's not already off, because
                    // it
                    // causes the scroll position to be reset to 0.
                    if (tv.getEllipsize() != null) {
                        tv.setEllipsize(null);
                    }

                    Layout ll = tv.getLayout();

                    // layout might be null if the text just changed, or ellipsizing
                    // was just turned off
                    if (ll == null) {
                        return false;
                    }

                    // get the non-ellipsized line width, to determine whether
                    // scrolling
                    // should even be allowed
                    mTextWidth = (int) tv.getLayout().getLineWidth(0);
                    mViewWidth = tv.getWidth();

                    if (mViewWidth > mTextWidth) {
                        tv.setEllipsize(TruncateAt.END);
                        v.cancelLongPress();
                        return false;
                    }

                    mDraggingLabel = true;
                    tv.setHorizontalFadingEdgeEnabled(true);
                    v.cancelLongPress();
                    return true;
                }
            }

            return false;
        }
        Handler mLabelScroller = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                TextView tv = (TextView) msg.obj;
                int x = tv.getScrollX();
                x = x * 3 / 4;
                tv.scrollTo(x, 0);

                if (x == 0) {
                    tv.setEllipsize(TruncateAt.END);
                } else {
                    Message newmsg = obtainMessage(0, tv);
                    mLabelScroller.sendMessageDelayed(newmsg, 15);
                }
            }
        };

        public boolean onLongClick(View view) {
            CharSequence title = null;
            String mime = null;
            String query = null;
            String artist;
            String album;
            String song;
            long audioid;

            if (mService == null) { return true; }
            try {
                artist = mService.getArtistName();
                album = mService.getAlbumName();
                song = mService.getTrackName();
                audioid = mService.getAudioId();
            } catch (RemoteException ex) {
                return true;
            } catch (NullPointerException ex) {
                // we might not actually have the service yet
                return true;
            }

            if (MediaStore.UNKNOWN_STRING.equals(album)
                && MediaStore.UNKNOWN_STRING.equals(artist) && song != null
                && song.startsWith("recording")) {
                // not music
                return false;
            }

            if (audioid < 0) {
                return false;
            }

            Cursor c = MusicUtils.query(this, ContentUris.withAppendedId(
                                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, audioid),
                                        new String[] { MediaStore.Audio.Media.IS_MUSIC }, null, null,
                                        null);
            boolean ismusic = true;

            if (c != null) {
                if (c.moveToFirst()) {
                    ismusic = c.getInt(0) != 0;
                }

                c.close();
            }

            if (!ismusic) {
                return false;
            }

            boolean knownartist = (artist != null)
                                  && !MediaStore.UNKNOWN_STRING.equals(artist);
            boolean knownalbum = (album != null)
                                 && !MediaStore.UNKNOWN_STRING.equals(album);

            if (knownartist && view.equals(mArtistName.getParent())) {
                title = artist;
                query = artist;
                mime = MediaStore.Audio.Artists.ENTRY_CONTENT_TYPE;
            } else if (knownalbum && view.equals(mAlbumName.getParent())) {
                title = album;

                if (knownartist) {
                    query = artist + " " + album;
                } else {
                    query = album;
                }

                mime = MediaStore.Audio.Albums.ENTRY_CONTENT_TYPE;
            } else if (view.equals(mTrackName.getParent()) || !knownartist
                     || !knownalbum) {
                if ((song == null) || MediaStore.UNKNOWN_STRING.equals(song)) {
                    // A popup of the form "Search for null/'' using ..." is pretty
                    // unhelpful, plus, we won't find any way to buy it anyway.
                    return true;
                }

                title = song;

                if (knownartist) {
                    query = artist + " " + song;
                } else {
                    query = song;
                }

                mime = "audio/*"; // the specific type doesn't matter, so don't
                // bother retrieving it
            } else {
                throw new RuntimeException("shouldn't be here");
            }

            title = getString(R.string.mediasearch, title);
            Intent i = new Intent();
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setAction(MediaStore.INTENT_ACTION_MEDIA_SEARCH);
            i.putExtra(SearchManager.QUERY, query);

            if (knownartist) {
                i.putExtra(MediaStore.EXTRA_MEDIA_ARTIST, artist);
            }

            if (knownalbum) {
                i.putExtra(MediaStore.EXTRA_MEDIA_ALBUM, album);
            }

            i.putExtra(MediaStore.EXTRA_MEDIA_TITLE, song);
            i.putExtra(MediaStore.EXTRA_MEDIA_FOCUS, mime);
            startActivity(Intent.createChooser(i, title));
            return true;
        }
    */
    private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
        public void onStartTrackingTouch(SeekBar bar) {
            mLastSeekEventTime = 0;
            mFromTouch = true;
        }

        public void onProgressChanged(SeekBar bar, int progress,
                                      boolean fromuser) {
            if (!fromuser || (mService == null)) {
                return;
            }

            mPosOverride = mDuration * progress / 1000;
            if (!mFromTouch) {
                try {
                    mService.seek(mPosOverride);
                } catch (RemoteException ex) {
                    Log.e(TAG, "onProgressChanged:RemoteException", ex);
                }
                refreshNow();
                mPosOverride = -1;
            }
        }

        public void onStopTrackingTouch(SeekBar bar) {
            try {//only seek after release mouse
                mService.seek(mPosOverride);
            } catch (RemoteException ex) {
                Log.e(TAG, "onStopTrackingTouch:RemoteException", ex);
            }

            mPosOverride = -1;
            mFromTouch = false;
        }
    };

    private View.OnClickListener mQueueListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                if (mService != null)
                    currPlayPath = mService.getPath();
            } catch (RemoteException ex) {
                Log.e(TAG, "mQueueListener:RemoteException", ex);
            }

            Intent intent = new Intent();
            intent.setClassName("com.hisilicon.android.music",
                    "com.hisilicon.android.music.FileListActivity");
            // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(intent.getData(), "audio/*");
            intent.putExtra("MediaFileList", mediaFileList);
            intent.putExtra("path", currPlayPath);
            startActivity(intent);
            Common.setisFileList(true);
            v.requestFocus();
        }
    };

    private View.OnClickListener mImageListener = new View.OnClickListener() {
        public void onClick(View v) {
            try {
                currPlayPath = mService.getPath();
            } catch (RemoteException ex) {
                Log.e(TAG, "mImageListener:RemoteException", ex);
            }
            playImage = true;
            currPlayPath = currPlayPath.substring(0, currPlayPath.lastIndexOf("/"));
            Intent intent = new Intent();
            intent.setClassName("com.hisilicon.android.music",
                    "com.hisilicon.android.music.ImageListAcvitity");
            intent.putExtra("path", currPlayPath);
            startActivityForResult(intent, PLAY_IMAGE);
            v.requestFocus();
        }
    };
    private View.OnClickListener mShuffleListener = new View.OnClickListener() {
        public void onClick(View v) {
            v.requestFocus();
            toggleShuffle(1);
        }
    };
    private View.OnClickListener mPauseListener = new View.OnClickListener() {
        public void onClick(View v) {
            v.requestFocus();
            doPauseResume();
        }
    };

    private View.OnClickListener mPrevListener = new View.OnClickListener() {
        public void onClick(View v) {
            v.requestFocus();
            playPrevMusic();
        }
    };

    private View.OnClickListener mNextListener = new View.OnClickListener() {
        public void onClick(View v) {
            v.requestFocus();
            playNextMusic();
        }
    };

    private void playPrevMusic() {
        if (mService == null) {
            return;
        }

        try {
            if (isOperatSpaceEnough()) {
                int mode = MediaPlaybackService.getPlayMode();
                if ((mode == MediaPlaybackService.SEQUENCE_PLAYER ||
                        mode == MediaPlaybackService.SINGLE_PLAYER ||
                        mode == MediaPlaybackService.SINGLE_CIRCULATE) &&
                        Common.isFirstMediaFile()) {
                    showToast(R.string.isthefirstfile);
                } else {
                    Common.setisPrevButton(true);
                    mService.prev();
                }
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "playPrevMusic:RemoteException", ex);
        }
    }

    ;

    private void playNextMusic() {
        if (mService == null) {
            return;
        }

        try {
            if (isOperatSpaceEnough()) {
                int mode = MediaPlaybackService.getPlayMode();
                if ((mode == MediaPlaybackService.SEQUENCE_PLAYER ||
                        mode == MediaPlaybackService.SINGLE_PLAYER ||
                        mode == MediaPlaybackService.SINGLE_CIRCULATE) &&
                        Common.isLastMediaFile()) {
                    showToast(R.string.isthelastfile);
                } else {
                    Common.setisPrevButton(false);
                    mService.next();
                }
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "playNextMusic:RemoteException", ex);
        }
    }

    ;

    private RepeatingImageButton.RepeatListener mRewListener = new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
            scanBackward(repcnt, howlong);
        }
    };

    private RepeatingImageButton.RepeatListener mFfwdListener = new RepeatingImageButton.RepeatListener() {
        public void onRepeat(View v, long howlong, int repcnt) {
            scanForward(repcnt, howlong);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (!playImage) {
            paused = true;
            if (mService != null) {
                try {
                    mService.pause();
                    Log.i(TAG, "onPause,so we pause music playing");
                } catch (RemoteException e) {
                    Log.e(TAG, "onPause", e);
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        mIsWindoDestory = false;
        Intent intent = getIntent();
        if (intent.getData() != null && ((Uri) intent.getData() != mUri)) {
            startPlayback();
        } else {
            if (mService != null) {
                try {
                    if (!mService.isPlaying()) {
                        mService.play();
                    }
                } catch (RemoteException e) {
                    Log.e(TAG, "onResume", e);
                }
            }
            long next = refreshNow();
            queueNextRefresh(next);
        }
        setPauseButtonImage();

    }

    @Override
    public void onDestroy() {
        if (!playImage) {
            if (mService != null) {
                try {
                    mService.stop();
                    Log.i(TAG, "onStop,so we stop music playing");
                } catch (RemoteException e) {
                    Log.e(TAG, "onStop", e);
                }
            }
            Common.setisFirstMediaFile(false);
            Common.setisLastMediaFile(false);
            Common.setisPrevButton(false);
            MusicUtils.unbindFromService(mToken);
            if (mService != null) {
                //if(!isIptvEnable()) {/*mark it,music will play in background when reveive message from hiplayer*/
                try {
                    mService.stopService();
                } catch (RemoteException ex) {
                    Log.e(TAG, "stopService:RemoteException", ex);
                }
                //}
            }
            mService = null;
        }
        mAlbumArtWorker.quit();
        mIsWindoDestory = true;
        unregisterReceiver(usbReceiver);
        if (SystemProperties.get("persist.suspend.mode").equals("deep_resume") && powerReceiver != null) {
            unregisterReceiver(powerReceiver);
        }

        if (conn != null) {
            unbindService(conn);
            Intent service = new Intent(Constants.ACTION);
            service.setClassName("com.hisilicon.android.music",
                    "com.hisilicon.android.music.MediaFileListService");
            stopService(service);
        }
        if (null != mHandler) {
            mHandler.removeMessages(REFRESH);
        }
        if (mStatusListener != null) {
            unregisterReceiver(mStatusListener);
        }

        super.onDestroy();
    }

    /*for lint
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);

            // Don't show the menu items if we got launched by path/filedescriptor,
            // or
            // if we're in one shot mode. In most cases, these menu items are not
            // useful in those modes, so for consistency we never show them in these
            // modes, instead of tailoring them to the specific file being played.
            if (MusicUtils.getCurrentAudioId() >= 0) {
                menu.add(0, GOTO_START, 0, R.string.goto_start).setIcon(
                    R.drawable.ic_menu_music_library);
                menu.add(0, PARTY_SHUFFLE, 0, R.string.party_shuffle); // icon will
                // be set in
                // onPrepareOptionsMenu()
                SubMenu sub = menu.addSubMenu(0, ADD_TO_PLAYLIST, 0,
                                              R.string.add_to_playlist).setIcon(
                                  android.R.drawable.ic_menu_add);
                // these next two are in a separate group, so they can be
                // shown/hidden as needed
                // based on the keyguard state
                menu.add(1, USE_AS_RINGTONE, 0, R.string.ringtone_menu_short)
                .setIcon(R.drawable.ic_menu_set_as_ringtone);
                menu.add(1, DELETE_ITEM, 0, R.string.delete_item).setIcon(
                    R.drawable.ic_menu_delete);
                Intent i = new Intent(
                    AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);

                if (getPackageManager().resolveActivity(i, 0) != null) {
                    menu.add(0, EFFECTS_PANEL, 0, R.string.effectspanel).setIcon(
                        R.drawable.ic_menu_eq);
                }

                return true;
            }

            return false;
        }

        @Override
        public boolean onPrepareOptionsMenu(Menu menu) {
            if (mService == null)
            { return false; }

            MenuItem item = menu.findItem(PARTY_SHUFFLE);

            if (item != null) {
                int shuffle = MusicUtils.getCurrentShuffleMode();

                if (shuffle == MediaPlaybackService.SHUFFLE_AUTO) {
                    item.setIcon(R.drawable.ic_menu_party_shuffle);
                    item.setTitle(R.string.party_shuffle_off);
                } else {
                    item.setIcon(R.drawable.ic_menu_party_shuffle);
                    item.setTitle(R.string.party_shuffle);
                }
            }

            item = menu.findItem(ADD_TO_PLAYLIST);

            if (item != null) {
                SubMenu sub = item.getSubMenu();
                MusicUtils.makePlaylistMenu(this, sub);
            }

            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            menu.setGroupVisible(1, !km.inKeyguardRestrictedInputMode());
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            Intent intent;

            try {
                switch (item.getItemId()) {
                    case GOTO_START:
                        intent = new Intent();
                        intent.setClass(this, MusicBrowserActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;

                    case USE_AS_RINGTONE: {
                            // Set the system setting to make this the current ringtone
                            if (mService != null) {
                                MusicUtils.setRingtone(this, mService.getAudioId());
                            }
                           return true;
                        }

                    case PARTY_SHUFFLE:
                        MusicUtils.togglePartyShuffle();
                        setShuffleButtonImage();
                        break;

                    case NEW_PLAYLIST: {
                            intent = new Intent();
                            intent.setClass(this, CreatePlaylist.class);
                            startActivityForResult(intent, NEW_PLAYLIST);
                            return true;
                        }

                    case PLAYLIST_SELECTED: {
                            long[] list = new long[1];
                            list[0] = MusicUtils.getCurrentAudioId();
                            long playlist = item.getIntent().getLongExtra("playlist", 0);
                            MusicUtils.addToPlaylist(this, list, playlist);
                            return true;
                        }

                    case DELETE_ITEM: {
                            if (mService != null) {
                                long[] list = new long[1];
                                list[0] = MusicUtils.getCurrentAudioId();
                                Bundle b = new Bundle();
                                String f;

                                if (android.os.Environment.isExternalStorageRemovable()) {
                                    f = getString(R.string.delete_song_desc,
                                                  mService.getTrackName());
                                } else {
                                    f = getString(R.string.delete_song_desc_nosdcard,
                                                  mService.getTrackName());
                                }

                                b.putString("description", f);
                                b.putLongArray("items", list);
                                intent = new Intent();
                                intent.setClass(this, DeleteItems.class);
                                intent.putExtras(b);
                                startActivityForResult(intent, -1);
                            }

                            return true;
                        }

                    case EFFECTS_PANEL: {
                            Intent i = new Intent(
                                AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                                       mService.getAudioSessionId());
                            startActivityForResult(i, EFFECTS_PANEL);
                            return true;
                        }
                }
            } catch (RemoteException ex) {
            }

            return super.onOptionsItemSelected(item);
        }
    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case NEW_PLAYLIST:
                Uri uri = intent.getData();

                if (uri != null) {
                    long[] list = new long[1];
                    list[0] = MusicUtils.getCurrentAudioId();
                    int playlist = Integer.parseInt(uri.getLastPathSegment());
                    MusicUtils.addToPlaylist(this, list, playlist);
                }

                break;
            case PLAY_IMAGE:
                playImage = true;
                Intent imageIntent = new Intent();
                imageIntent.setClassName(
                        "com.hisilicon.android.gallery3d",
                        "com.hisilicon.android.gallery3d.list.ImageFileListService");
                imageIntent.putExtra("path", intent.getStringExtra("path"));
                imageIntent.putExtra("fromMusic", intent.getBooleanExtra("fromMusic", true));
                imageIntent.setDataAndType(intent.getData(), "image/*");
                try {
                    startService(imageIntent);
                } catch (ActivityNotFoundException e) {
                    // TODO: 2018/8/15 跳转4K图库
                }
                break;
        }
    }

    private final int keyboard[][] = {
            {KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_E,
                    KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_Y,
                    KeyEvent.KEYCODE_U, KeyEvent.KEYCODE_I, KeyEvent.KEYCODE_O,
                    KeyEvent.KEYCODE_P,},
            {KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_D,
                    KeyEvent.KEYCODE_F, KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H,
                    KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L,
                    KeyEvent.KEYCODE_DEL,},
            {KeyEvent.KEYCODE_Z, KeyEvent.KEYCODE_X, KeyEvent.KEYCODE_C,
                    KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_N,
                    KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_COMMA,
                    KeyEvent.KEYCODE_PERIOD, KeyEvent.KEYCODE_ENTER}

    };

    private int lastX;
    private int lastY;

    private boolean seekMethod1(int keyCode) {
        if (mService == null) {
            return false;
        }

        for (int x = 0; x < 10; x++) {
            for (int y = 0; y < 3; y++) {
                if (keyboard[y][x] == keyCode) {
                    int dir = 0;

                    // top row
                    if (x == lastX && y == lastY) {
                        dir = 0;
                    } else if (y == 0 && lastY == 0 && x > lastX) {
                        dir = 1;
                    } else if (y == 0 && lastY == 0 && x < lastX) {
                        dir = -1;
                    }
                    // bottom row
                    else if (y == 2 && lastY == 2 && x > lastX) {
                        dir = -1;
                    } else if (y == 2 && lastY == 2 && x < lastX) {
                        dir = 1;
                    }
                    // moving up
                    else if (y < lastY && x <= 4) {
                        dir = 1;
                    } else if (y < lastY && x >= 5) {
                        dir = -1;
                    }
                    // moving down
                    else if (y > lastY && x <= 4) {
                        dir = -1;
                    } else if (y > lastY && x >= 5) {
                        dir = 1;
                    }

                    lastX = x;
                    lastY = y;

                    try {
                        mService.seek(mService.position() + dir * 5);
                    } catch (RemoteException ex) {
                        Log.e(TAG, "seekMethod1:RemoteException", ex);
                    }

                    refreshNow();
                    return true;
                }
            }
        }

        lastX = -1;
        lastY = -1;
        return false;
    }

    private boolean seekMethod2(int keyCode) {
        if (mService == null) {
            return false;
        }

        for (int i = 0; i < 10; i++) {
            if (keyboard[0][i] == keyCode) {
                int seekpercentage = 100 * i / 10;

                try {
                    mService.seek(mService.duration() * seekpercentage / 100);
                } catch (RemoteException ex) {
                    Log.e(TAG, "seekMethod2:RemoteException", ex);
                }

                refreshNow();
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.ACTION_DOWN:
                if (mPowerdown) {
                    doPauseResume();
                }

                break;

            case KeyEvent.KEYCODE_BACK:
                mPowerdown = false;
                break;
        }

        return super.onKeyUp(keyCode, event);
    }
/*for fority
    private boolean useDpadMusicControl() {
        if (mDeviceHasDpad
            && (mPrevButton.isFocused() || mNextButton.isFocused() || mPauseButton
                .isFocused())) {
            return true;
        }

        return false;
    }
*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((seekmethod == 0) ? seekMethod1(keyCode) : seekMethod2(keyCode)) {
            return true;
        }

        switch (keyCode) {
            /*
             * // image scale case KeyEvent.KEYCODE_Q: av.adjustParams(-0.05, 0.0,
             * 0.0, 0.0, 0.0,-1.0); break; case KeyEvent.KEYCODE_E: av.adjustParams(
             * 0.05, 0.0, 0.0, 0.0, 0.0, 1.0); break; // image translate case
             * KeyEvent.KEYCODE_W: av.adjustParams( 0.0, 0.0,-1.0, 0.0, 0.0, 0.0);
             * break; case KeyEvent.KEYCODE_X: av.adjustParams( 0.0, 0.0, 1.0, 0.0,
             * 0.0, 0.0); break; case KeyEvent.KEYCODE_A: av.adjustParams( 0.0,-1.0,
             * 0.0, 0.0, 0.0, 0.0); break; case KeyEvent.KEYCODE_D: av.adjustParams(
             * 0.0, 1.0, 0.0, 0.0, 0.0, 0.0); break; // camera rotation case
             * KeyEvent.KEYCODE_R: av.adjustParams( 0.0, 0.0, 0.0, 0.0, 0.0,-1.0);
             * break; case KeyEvent.KEYCODE_U: av.adjustParams( 0.0, 0.0, 0.0, 0.0,
             * 0.0, 1.0); break; // camera translate case KeyEvent.KEYCODE_Y:
             * av.adjustParams( 0.0, 0.0, 0.0, 0.0,-1.0, 0.0); break; case
             * KeyEvent.KEYCODE_N: av.adjustParams( 0.0, 0.0, 0.0, 0.0, 1.0, 0.0);
             * break; case KeyEvent.KEYCODE_G: av.adjustParams( 0.0, 0.0, 0.0,-1.0,
             * 0.0, 0.0); break; case KeyEvent.KEYCODE_J: av.adjustParams( 0.0, 0.0,
             * 0.0, 1.0, 0.0, 0.0); break;
             */
            case KeyEvent.KEYCODE_SLASH:
                seekmethod = 1 - seekmethod;
                return true;
            /*
             * case KeyEvent.KEYCODE_DPAD_LEFT: if (!useDpadMusicControl()) {
             * break; } if (!mPrevButton.hasFocus()) {
             * mPrevButton.requestFocus(); } scanBackward(repcnt,
             * event.getEventTime() - event.getDownTime()); return true; case
             * KeyEvent.KEYCODE_DPAD_RIGHT: if (!useDpadMusicControl()) { break;
             * } if (!mNextButton.hasFocus()) { mNextButton.requestFocus(); }
             * scanForward(repcnt, event.getEventTime() - event.getDownTime());
             * return true;
             */

            case KeyEvent.KEYCODE_S:
                toggleShuffle(1);
                return true;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_SPACE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                doPauseResume();
                return true;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                return setForward();

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                return setRewind();

            case KeyEvent.KEYCODE_PAGE_DOWN:
                if (mService != null) {
                    if (!mSeeking && mStartSeekPos >= 0) {
                        mPauseButton.requestFocus();
                        playNextMusic();
                    } else {
                        scanForward(-1, event.getEventTime() - event.getDownTime());
                        mPauseButton.requestFocus();
                        mStartSeekPos = -1;
                    }
                }

                mSeeking = false;
                mPosOverride = -1;
                return true;

            case KeyEvent.KEYCODE_PAGE_UP:
                if (mService != null) {
                    if (!mSeeking && mStartSeekPos >= 0) {
                        mPauseButton.requestFocus();

                        try {
                            if (mStartSeekPos < 1000) {
                                playPrevMusic();
                            } else {
                                mService.seek(0);
                            }
                        } catch (RemoteException e) {
                            Log.e(TAG, "KEYCODE_PAGE_UP", e);
                        }
                    } else {
                        scanBackward(-1, event.getEventTime() - event.getDownTime());
                        mPauseButton.requestFocus();
                        mStartSeekPos = -1;
                    }
                }

                mSeeking = false;
                mPosOverride = -1;
                return true;

            case KeyEvent.KEYCODE_MEDIA_STOP:
                if (mService != null) {
                    try {
                        mService.stop();
                    } catch (RemoteException e) {
                        Log.e(TAG, "KEYCODE_MEDIA_STOP", e);
                    }

                    finish();
                }

                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private boolean setForward() {
        if (mService == null) {
            return false;
        }

        long now1 = SystemClock.elapsedRealtime();

        if ((now1 - mLastSeekEventTime) > 250) {
            mLastSeekEventTime = now1;
            mPosOverride = mDuration * (mProgress.getProgress() + 50)
                    / 1000;

            try {
                mService.seek(mPosOverride);
            } catch (RemoteException ex) {
                Log.e(TAG, "KEYCODE_FORWARD:RemoteException", ex);
            }

            // trackball event, allow progress updates
            if (!mFromTouch) {
                refreshNow();
                mPosOverride = -1;
            }
        }

        return true;
    }

    private boolean setRewind() {
        if (mService == null) {
            return false;
        }

        long now = SystemClock.elapsedRealtime();

        if ((now - mLastSeekEventTime) > 250) {
            mLastSeekEventTime = now;
            mPosOverride = mDuration * (mProgress.getProgress() - 50)
                    / 1000;

            try {
                if (!mService.isPlaying()) {
                    mService.play();
                }
                mService.seek(mPosOverride);
            } catch (RemoteException ex) {
                Log.e(TAG, "KEYCODE_MEDIA_REWIND:RemoteException", ex);
            }

            // trackball event, allow progress updates
            if (!mFromTouch) {
                refreshNow();
                mPosOverride = -1;
            }
        }

        return true;
    }

    private void scanBackward(int repcnt, long delta) {
        if (mService == null) {
            return;
        }

        try {
            if (repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;

                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10;
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }

                long newpos = mStartSeekPos - delta;

                if (newpos < 0) {
                    // move to previous track
                    mService.prev();
                    long duration = mService.duration();
                    mStartSeekPos += duration;
                    newpos += duration;
                }

                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }

                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }

                refreshNow();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "scanForward:RemoteException", ex);
        }
    }

    private void scanForward(int repcnt, long delta) {
        if (mService == null) {
            return;
        }

        try {
            if (repcnt == 0) {
                mStartSeekPos = mService.position();
                mLastSeekEventTime = 0;
                mSeeking = false;
            } else {
                mSeeking = true;

                if (delta < 5000) {
                    // seek at 10x speed for the first 5 seconds
                    delta = delta * 10;
                } else {
                    // seek at 40x after that
                    delta = 50000 + (delta - 5000) * 40;
                }

                long newpos = mStartSeekPos + delta;
                long duration = mService.duration();

                if (newpos >= duration) {
                    // move to next track
                    mService.next();
                    mStartSeekPos -= duration; // is OK to go negative
                    newpos -= duration;
                }

                if (((delta - mLastSeekEventTime) > 250) || repcnt < 0) {
                    mService.seek(newpos);
                    mLastSeekEventTime = delta;
                }

                if (repcnt >= 0) {
                    mPosOverride = newpos;
                } else {
                    mPosOverride = -1;
                }

                refreshNow();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "scanBackward:RemoteException", ex);
        }
    }

    private void doPauseResume() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    Log.d("MediaPlaybackActivity", "playing");
                    mService.pause();
                } else {
                    Log.d("MediaPlaybackActivity", "pause");
                    mService.play();
                }

                refreshNow();
                setPauseButtonImage();
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "doPauseResume:RemoteException", ex);
        }
    }

    private void toggleShuffle(int i) {
        if (mService == null) {
            return;
        }
        Log.e("MediaPlaybackActivity", "toggleShuffle");
        try {
            int mode = MediaPlaybackService.getPlayMode();
            if (mode < 0) {
                return;
            }
            mode = (mode + i) % 5;
            MediaPlaybackService.setPlayMode(mode);
            mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NONE);
            switch (mode) {
                case MediaPlaybackService.SEQUENCE_PLAYER:
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_NONE);
                    mShuffleButton.setImageResource(R.drawable.hisil_ic_mp_repeat_off_btn);
                    showToast(R.string.repeat_off_notif);
                    break;
                case MediaPlaybackService.CIRCULATE_PLAYER:
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                    mShuffleButton.setImageResource(R.drawable.hisil_ic_mp_repeat_all_btn);
                    showToast(R.string.repeat_all_notif);
                    break;
                case MediaPlaybackService.SINGLE_PLAYER:
                    mShuffleButton.setImageResource(R.drawable.hisil_ic_mp_play_once_btn);
                    showToast(R.string.shuffle_off_notif);
                    break;
                case MediaPlaybackService.SINGLE_CIRCULATE:
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_CURRENT);
                    mShuffleButton.setImageResource(R.drawable.hisil_ic_mp_repeat_once_btn);
                    showToast(R.string.repeat_current_notif);
                    break;
                case MediaPlaybackService.RANDOM_PLAYER:
                    mService.setRepeatMode(MediaPlaybackService.REPEAT_ALL);
                    mService.setShuffleMode(MediaPlaybackService.SHUFFLE_NORMAL);
                    mShuffleButton.setImageResource(R.drawable.hisil_ic_mp_shuffle_on_btn);
                    showToast(R.string.shuffle_on_notif);
                    break;
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "toggleShuffle:RemoteException", ex);
        }
    }

    private void showToast(final int resid) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mToast == null) {
                    mToast = Toast.makeText(getBaseContext(), resid, Toast.LENGTH_SHORT);
                }else {
                    mToast.cancel();
                    mToast = Toast.makeText(getBaseContext(), resid, Toast.LENGTH_SHORT);
                }
//                mToast.setText(resid);
                mToast.show();
            }
        });
    }

    private void startPlayback() {
        if (mService == null) {
            return;
        }

        Intent intent = getIntent();
        String filename = "";
        mUri = intent.getData();

        if (mUri != null && mUri.toString().length() > 0) {
            String scheme = mUri.getScheme();

            if ("file".equals(scheme)) {
                filename = mUri.getPath();
            } else {
                filename = mUri.toString();
            }

            try {
                mService.stop();
                mService.openFile(filename);
                mService.play();
                setIntent(new Intent());
            } catch (RemoteException ex) {
                Log.e(TAG, "couldn't start playback: ", ex);
            }
        }

        updateTrackInfo();
        long next = refreshNow();
        queueNextRefresh(next);
    }

    private ServiceConnection osc = new ServiceConnection() {
        public void onServiceConnected(ComponentName classname, IBinder obj) {
            mService = IMediaPlaybackService.Stub.asInterface(obj);
            startPlayback();
            if (mService == null) {
                return;
            }
            // new Thread(new UIUpdateThread()).start();
            try {
                // Assume something is playing when the service says it is,
                // but also if the audio ID is valid but the service is paused.
                if (mService.getAudioId() >= 0 || mService.isPlaying()
                        || mService.getPath() != null) {
                    // something is playing now, we're done
                    mRepeatButton.setVisibility(View.GONE);
                    mShuffleButton.setVisibility(View.VISIBLE);
                    mQueueButton.setVisibility(View.VISIBLE);
                    toggleShuffle(0);
                    setPauseButtonImage();
                    return;
                }
            } catch (RemoteException ex) {
                Log.e(TAG, "ServiceConnection:RemoteException", ex);
            }

            // Service is dead or not playing anything. If we got here as part
            // of a "play this file" Intent, exit. Otherwise go to the Music
            // app start screen.
            Log.e(TAG, "lint error: exit--can't find MusicBrowserActivity");
/*for lint
            if (getIntent().getData() == null) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setClass(MediaPlaybackActivity.this,
                                MusicBrowserActivity.class);
                startActivity(intent);
            }
*/

            finish();
        }

        public void onServiceDisconnected(ComponentName classname) {
            mService = null;
            mIsWindoDestory = true;
        }
    };


/*for fority
    private void setShuffleButtonImage() {
        if (mService == null)
        { return; }

        try {
            switch (mService.getShuffleMode()) {
                case MediaPlaybackService.SHUFFLE_NONE:
                    mShuffleButton
                    .setImageResource(R.drawable.hisil_ic_mp_shuffle_off_btn);
                    break;

                case MediaPlaybackService.SHUFFLE_AUTO:
                    mShuffleButton
                    .setImageResource(R.drawable.ic_mp_partyshuffle_on_btn);
                    break;
                default:
                    mShuffleButton
                    .setImageResource(R.drawable.hisil_ic_mp_shuffle_on_btn);
                    break;
            }
        } catch (RemoteException ex) {
          Log.i(TAG,"Error setShuffleButtonImage: "+ex);
        }
    }
*/

    private void setPauseButtonImage() {
        try {
            if (mService != null && mService.isPlaying()) {
                mPauseButton
                        .setImageResource(R.drawable.hisil_ic_media_ff);
                if (isUiUpdateThreadStop) {
                    isUiUpdateThreadStop = false;
                    Thread lyricThread = new Thread(new UIUpdateThread());
                    lyricThread.start();
                }
            } else {
                mPauseButton.setImageResource(R.drawable.hisil_ic_appwidget_music_play);
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "setPauseButtonImage:RemoteException", ex);
        }
    }

    private ImageView mAlbum;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private RotateTextView mArtistName;
    private RotateTextView mAlbumName;
    private RotateTextView mTrackName;
    private ImageView mItemImage;
    private TextView mItemTitle;
    private TextView mItemPath;
    private ProgressBar mProgress;
    private long mPosOverride = -1;
    private boolean mFromTouch = false;
    private long mDuration;
    private int seekmethod;
    private boolean paused;

    private static final int REFRESH = 1;
    private static final int QUIT = 2;
    private static final int GET_ALBUM_ART = 3;
    private static final int ALBUM_ART_DECODED = 4;
    private static final int list = 0;

    private void queueNextRefresh(long delay) {
        if (!paused) {
            Message msg = mHandler.obtainMessage(REFRESH);
            mHandler.removeMessages(REFRESH);
            mHandler.sendMessageDelayed(msg, delay);
        }
    }

    private long refreshNow() {
        if (mService == null) {
            return 500;
        }

        try {
            long pos = mPosOverride < 0 ? mService.position() : mPosOverride;
            long remaining = 1000 - (pos % 1000);

            if ((pos >= 0) && (mDuration > 0)) {
                if (pos > mDuration) {
                    pos = mDuration;
                }
                mCurrentTime.setText(MusicUtils
                        .makeTimeString(this, pos / 1000));

                if (mService.isPlaying()) {
                    mCurrentTime.setVisibility(View.VISIBLE);
                } else {
                    // blink the counter
                    int vis = mCurrentTime.getVisibility();
                    mCurrentTime
                            .setVisibility(vis == View.INVISIBLE ? View.VISIBLE
                                    : View.INVISIBLE);
                    remaining = 500;
                }

                mProgress.setProgress((int) (1000 * pos / mDuration));
            } else {
                mCurrentTime.setText("--:--");
                mProgress.setProgress(0);
            }

            // return the number of milliseconds until the next full second, so
            // the counter can be updated at just the right time
            return remaining;
        } catch (RemoteException ex) {
            Log.e(TAG, "refreshNow:RemoteException", ex);
        }

        return 500;
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ALBUM_ART_DECODED:
                    try {
                        long songid = mService.getAudioId();
                        String path = mService.getPath();
                        if (songid < 0 && path.toLowerCase().startsWith("http://")) {
                            // Once we can get album art and meta data from MediaPlayer, we
                            // can show that info again when streaming.
                            ((View) mArtistName).setVisibility(View.INVISIBLE);
                            ((View) mAlbumName).setVisibility(View.INVISIBLE);
                            mAlbum.setVisibility(View.GONE);
                            mTrackName.setText(path);
                        }else {
                            ((View) mArtistName).setVisibility(View.VISIBLE);
                            ((View) mAlbumName).setVisibility(View.VISIBLE);
                            String artistName = mService.getArtistName();
                            if (MediaStore.UNKNOWN_STRING.equals(artistName)
                                    || null == artistName || "".equals(artistName)) {
                                artistName = getString(R.string.unknown_artist_name);
                            }
                            mArtistName.setText(artistName);
                            String albumName = mService.getAlbumName();
                            long albumid = mService.getAlbumId();
                            if (MediaStore.UNKNOWN_STRING.equals(albumName)
                                    || null == albumName || "".equals(albumName)) {
                                albumName = getString(R.string.unknown_album_name);
                                albumid = -1;
                            }
                            mAlbumName.setText(albumName);
                            String titleName = path.substring(path.lastIndexOf('/') + 1);
                            mTrackName.setText(titleName);
                            mAlbum.setVisibility(View.VISIBLE);
                        }
                    }catch (RemoteException ex){
                        Log.e(TAG, "ALBUM_ART_DECODED", ex);
                    }
                    new Thread(new startLyricThread()).start();
                    Bitmap tempBp = null;
                    Bitmap drawBitmap = null;
                    if (null == mBitmap) {
                        mBitmap = BitmapFactory.decodeResource(
                                MediaPlaybackActivity.this.getResources(),
                                R.drawable.hisil_img0333);
                    }

                    WeakReference<Bitmap> weakBp = new WeakReference<Bitmap>(
                            mBitmap);
                    tempBp = createReflectedImages(weakBp.get());
                    WeakReference<Bitmap> weaktempBp = new WeakReference<Bitmap>(
                            tempBp);
                    drawBitmap = transformImageBitmap(weaktempBp.get(), 2);
                    WeakReference<Bitmap> weakdrBp = new WeakReference<Bitmap>(
                            drawBitmap);
                    mAlbum.setImageBitmap(weakdrBp.get());
                    mAlbum.invalidate();
                    weakBp.clear();
                    weaktempBp.clear();
                    weakdrBp.clear();
                    break;
                case REFRESH:
                    if (!isPlaying()) {
                        int vis = mCurrentTime.getVisibility();
                        mCurrentTime.setVisibility(vis == View.INVISIBLE ? View.VISIBLE
                                : View.INVISIBLE);
                        queueNextRefresh(500);
                        break;
                    }
                    long next = refreshNow();
                    queueNextRefresh(next);
                    break;

                case QUIT:
                    // This can be moved back to onCreate once the bug that prevents
                    // Dialogs from being started from onCreate/onResume is fixed.
                    new AlertDialog.Builder(MediaPlaybackActivity.this)
                            .setTitle(R.string.service_start_error_title)
                            .setMessage(R.string.service_start_error_msg)
                            .setPositiveButton(R.string.service_start_error_button,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int whichButton) {
                                            finish();
                                        }
                                    }).setCancelable(false).show();
                    break;

                default:
                    break;
            }
        }
    };

    private int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        //Our height and width will always be the same since all glass has the same resolution, for now...
        if (height > 360 || width > 640) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > 360 && (halfWidth / inSampleSize) > 640) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    //get music album bitmap.
    public void getAlbumArtWork(String filePath) {
        if (filePath.indexOf("http") == 0) {
            return ;
        }
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String titleName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            mService.setAlbumName(albumName);
            mService.setTitleName(titleName);
            mService.setArtistName(artistName);
            mService.setTrackName(titleName);
            byte[] art = retriever.getEmbeddedPicture();
            if (art.length > 0) {
                BitmapFactory.Options opts = new BitmapFactory.Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeByteArray(art, 0, art.length, opts);
                opts.inSampleSize = calculateInSampleSize(opts);
                opts.inJustDecodeBounds = false;
                mBitmap = BitmapFactory.decodeByteArray(art, 0, art.length, opts);
            }
        } catch (IllegalArgumentException ex) {
            mBitmap = null;
        } catch (RuntimeException ex) {
            mBitmap = null;
        }catch (RemoteException ex){
            mBitmap = null;
            Log.e(TAG, "getAlbumArtWork", ex);
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
    }
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action)
                return;

            if (action.equals(MediaPlaybackService.META_CHANGED)) {
                // redraw the artist/title info and
                // set new max for progress bar
                updateTrackInfo();
                setPauseButtonImage();
                queueNextRefresh(1);
            } else if (action.equals(MediaPlaybackService.PLAYSTATE_CHANGED)) {
                setPauseButtonImage();
            }
        }
    };

    private static class AlbumSongIdWrapper {
        public long albumid;
        public long songid;

        AlbumSongIdWrapper(long aid, long sid) {
            albumid = aid;
            songid = sid;
        }
    }

    private void updateTrackInfo() {
        if (mService == null) {
            return;
        }

        try {
            String path = mService.getPath();

            if (path == null) {
                try {
                    if (mService.isPlaying()) {
                        doPauseResume();
                    }
                } catch (RemoteException ex) {
                    Log.e(TAG, "updateTrackInfo path == null:", ex);
                }

                return;
            }

            long songid = mService.getAudioId();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAlbumArtWork(path);
                    Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
                    mHandler.removeMessages(ALBUM_ART_DECODED);
                    mHandler.sendMessage(numsg);
                }
            }).start();
            mDuration = mService.duration();
            mTotalTime.setText(MusicUtils
                    .makeTimeString(this, mDuration / 1000));
        } catch (RemoteException ex) {
            Log.e(TAG, "updateTrackInfo:RemoteException", ex);
            finish();
        }
    }

    private File getLyricFile(String strMusicName) {
        if (null == strMusicName) {
            return null;
        }

        if (1 > strMusicName.lastIndexOf(".")) {
            return null;
        }

        File oldfile = new File(strMusicName);
        if (oldfile.isDirectory()) {
            return null;
        }

        String strFileName = oldfile.getName();
        String type = strFileName.substring(strFileName.lastIndexOf("."));
        String strLyricName = strFileName.replace(type, ".lrc");

        String strParentAbsPath = oldfile.getParentFile().getAbsolutePath();
        if (null == strParentAbsPath) {
            Log.e(TAG, " get the path file error");
            return null;
        }
        File path = new File(strParentAbsPath);

        File[] files = path.listFiles();
        File fileRet = null;
        if (null != files) {
            for (File lyric : files) {
                if (lyric.getName().equalsIgnoreCase(strLyricName)) {
                    fileRet = lyric;
                    break;
                }
            }
        }
        return fileRet;
    }

    class startLyricThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Audio pli = null;
            File f = null;

            try {
                pli = new Audio(mTrackName.getText().toString(),
                        mService.getPath(), 0L, true);
                pli.setTitle(mTrackName.getText().toString());
                pli.setAlbum(mAlbumName.getText().toString());
                pli.setArtist(mArtistName.getText().toString());
                pli.setTrack(mService.getTrackName());
                pli.setPath(mService.getPath());
                f = getLyricFile(mService.getPath());
            } catch (RemoteException ex) {
                Log.e(TAG, "startLyricThread:RemoteException", ex);
            }

            WeakReference<Audio> weakRf_audio = new WeakReference<Audio>(pli);
            WeakReference<File> weakRf_file = new WeakReference<File>(f);
            Lyric lyric;

            if ((null != f) && (f.exists())) {
                lyric = new Lyric(weakRf_file.get(), weakRf_audio.get());
            } else {
                lyric = new Lyric(weakRf_audio.get());
            }

            WeakReference<Lyric> weakRf_lyric = new WeakReference<Lyric>(lyric);
            lyricView.setmLyric(weakRf_lyric.get());
            lyricView.setSentencelist(weakRf_lyric.get().list);
            //jly 20140314
            //            lyricView.setNotCurrentPaintColor(Color.GREEN);
            //            lyricView.setCurrentPaintColor(Color.YELLOW);
            lyricView.setNotCurrentPaintColor(Color.WHITE);
            lyricView.setCurrentPaintColor(Color.WHITE);
            //            lyricView.setCurrentTextSize(24);
            //            lyricView.setLrcTextSize(24);
            lyricView.setCurrentTextSize(40);
            lyricView.setLrcTextSize(22);
            lyricView.setTexttypeface(Typeface.SERIF);
            lyricView.setBrackgroundcolor(Color.TRANSPARENT);
            lyricView.setTextHeight(20);
            if (isUiUpdateThreadStop) {
                isUiUpdateThreadStop = false;
                Thread lyricThread = new Thread(new UIUpdateThread());
                lyricThread.start();
            }
            pli = null;
            f = null;
            lyric = null;
            System.gc();
        }
    }

    class UIUpdateThread implements Runnable {
        long time = 100;

        public void run() {
            boolean isplaying = false;

            try {
                isplaying = mService.isPlaying();
            } catch (RemoteException ex) {
                Log.e(TAG, "UIUpdateThread:RemoteException", ex);
            }

            while (isplaying) {
                try {
                    if (mIsWindoDestory) {
                        return;
                    }
                    lyricView.updateIndex(mService.position());
                    mLyricHandler.post(mUpdateResults);
                    Thread.sleep(time);
                    try {
                        isplaying = mService.isPlaying();
                    } catch (RemoteException ex) {
                        Log.e(TAG, "UIUpdateThread:RemoteException", ex);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "UIUpdateThread", e);
                } catch (RemoteException ex) {
                    Log.e(TAG, "UIUpdateThread", ex);
                } catch (Exception ex) {
                    Log.e(TAG, "UIUpdateThread", ex);
                }
            }
            // UiUpdateThread stop.
            isUiUpdateThreadStop = true;
        }
    }

    Handler mLyricHandler = new Handler();
    Runnable mUpdateResults = new Runnable() {
        public void run() {
            lyricView.invalidate();
        }
    };

    public class AlbumArtHandler extends Handler {
        private long mAlbumId = -1;

        public AlbumArtHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            long albumid = ((AlbumSongIdWrapper) msg.obj).albumid;
            long songid = ((AlbumSongIdWrapper) msg.obj).songid;

            if (msg.what == GET_ALBUM_ART
                    && (mAlbumId != albumid || albumid < 0)) {
                // while decoding the new image, show the default album art
                Message numsg = mHandler.obtainMessage(ALBUM_ART_DECODED, null);
                mHandler.removeMessages(ALBUM_ART_DECODED);
                mHandler.sendMessage(numsg);

            }
        }
    }

    private static class Worker implements Runnable {
        private final Object mLock = new Object();
        private Looper mLooper;

        /**
         * Creates a worker thread with the given name. The thread then runs a
         * {@link android.os.Looper}.
         *
         * @param name A name for the new thread
         */
        Worker(String name) {
            Thread t = new Thread(null, this, name);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();

            synchronized (mLock) {
                while (mLooper == null) {
                    try {
                        mLock.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }

        public Looper getLooper() {
            return mLooper;
        }

        public void run() {
            synchronized (mLock) {
                Looper.prepare();
                mLooper = Looper.myLooper();
                mLock.notifyAll();
            }

            Looper.loop();
        }

        public void quit() {
            mLooper.quit();
        }
    }

    public boolean isOperatSpaceEnough() {
        boolean bRet = (SystemClock.elapsedRealtime() - mCurrentElapseTime) > OPERATING_SPACE_TIME;
        mCurrentElapseTime = SystemClock.elapsedRealtime();
        return bRet;
    }

    public Bitmap createReflectedImages(Bitmap bitmap) {
        final int reflectionGap = 0;
        Bitmap originalImage = bitmap;
        WeakReference<Bitmap> weakImage = new WeakReference<Bitmap>(
                originalImage);
        int width_bitmap = weakImage.get().getWidth();
        int heiget_bitmap = weakImage.get().getHeight();
        int width_view = mAlbum.getWidth();
        int height_view = mAlbum.getHeight();
        int width = 0;
        int height = 0;
        double scaled_index = 0;

        if (width_bitmap >= width_view && heiget_bitmap >= height_view) {
            BigDecimal b1 = new BigDecimal(Integer.toString(width_bitmap));
            BigDecimal b2 = new BigDecimal(Integer.toString(width_view));
            width = width_view;
            scaled_index = b1.divide(b2, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            BigDecimal b3 = new BigDecimal(Integer.toString(heiget_bitmap));
            BigDecimal b4 = new BigDecimal(Double.toString(scaled_index));
            double height_t = b3.divide(b4, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            height = (int) height_t;
        } else if (width_bitmap < width_view && heiget_bitmap < height_view) {
            width = width_bitmap;
            height = heiget_bitmap;
        } else if (width_bitmap >= width_view && heiget_bitmap < height_view) {
            BigDecimal b1 = new BigDecimal(Integer.toString(width_bitmap));
            BigDecimal b2 = new BigDecimal(Integer.toString(width_view));
            width = width_view;
            scaled_index = b1.divide(b2, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            BigDecimal b3 = new BigDecimal(Integer.toString(heiget_bitmap));
            BigDecimal b4 = new BigDecimal(Double.toString(scaled_index));
            double height_t = b3.divide(b4, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            height = (int) height_t;
        } else if (width_bitmap < width_view && heiget_bitmap >= height_view) {
            BigDecimal b1 = new BigDecimal(Integer.toString(heiget_bitmap));
            BigDecimal b2 = new BigDecimal(Integer.toString(height_view));
            height = height_view;
            scaled_index = b1.divide(b2, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            BigDecimal b3 = new BigDecimal(Integer.toString(width_bitmap));
            BigDecimal b4 = new BigDecimal(Double.toString(scaled_index));
            double width_t = b3.divide(b4, 5, BigDecimal.ROUND_HALF_UP)
                    .doubleValue();
            width = (int) width_t;
        }

        Bitmap mBitmap = Bitmap.createScaledBitmap(weakImage.get(), width,
                height, true);
        WeakReference<Bitmap> weakScaked = new WeakReference<Bitmap>(mBitmap);
        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        Bitmap reflectionImage = Bitmap.createBitmap(weakScaked.get(), 0,
                height / 4 * 3, width, height / 4, matrix, false);
        WeakReference<Bitmap> weakbp = new WeakReference<Bitmap>(
                reflectionImage);
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width,
                (height + height / 4), Config.ARGB_8888);
        WeakReference<Bitmap> weakbpwf = new WeakReference<Bitmap>(
                bitmapWithReflection);
        Canvas canvas = new Canvas(weakbpwf.get());
        canvas.drawBitmap(weakScaked.get(), 0, 0, null);
        Paint deafaultPaint = new Paint();
        deafaultPaint.setAntiAlias(true);
        canvas.drawBitmap(weakbp.get(), 0, height + reflectionGap, null);
        weakbp.get().recycle();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        LinearGradient shader = new LinearGradient(0, weakScaked.get()
                .getHeight(), 0, weakbpwf.get().getHeight() + reflectionGap,
                0x70ffffff, 0x00ffffff, TileMode.MIRROR);
        weakScaked.get().recycle();
        paint.setShader(shader);
        paint.setXfermode(new PorterDuffXfermode(
                android.graphics.PorterDuff.Mode.DST_IN));
        canvas.drawRect(0, height, width, weakbpwf.get().getHeight()
                + reflectionGap, paint);
        return weakbpwf.get();
    }

    private Bitmap transformImageBitmap(Bitmap child, int rotationAngle) {
        Transformation t = new Transformation();
        t.clear();
        t.setTransformationType(Transformation.TYPE_MATRIX);
        mCamera.save();
        final Matrix imageMatrix = t.getMatrix();
        final int imageHeight = child.getHeight();
        final int imageWidth = child.getWidth();
        final int rotation = Math.abs(rotationAngle);
        mCamera.translate(0.0f, 0.0f, 100.0f);

        // As the angle of the view gets less, zoom in
        if (rotation < 60) {
            float zoomAmount = (float) (-300 + (rotation * 1.5));
            mCamera.translate(0.0f, 0.0f, zoomAmount);
        }

        mCamera.rotateY(rotationAngle);
        mCamera.getMatrix(imageMatrix);
        imageMatrix.preTranslate(-(imageWidth / 2), -(imageHeight / 2));
        imageMatrix.postTranslate((imageWidth / 2), (imageHeight / 2));
        mCamera.restore();
        Bitmap newBit = Bitmap.createBitmap(child, 0, 0, imageWidth,
                imageHeight, imageMatrix, true);
        WeakReference<Bitmap> weakbp = new WeakReference<Bitmap>(newBit);
        Canvas canvas = new Canvas(weakbp.get());
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawBitmap(weakbp.get(), 0, 0, paint);
        return weakbp.get();
    }

    private boolean isIptvEnable() {
        String iptvEnable = SystemProperties.get("ro.product.target", "aosp");

        if ("telecom".equals(iptvEnable))
            return true;
        else
            return false;
    }

    public static String getHOME() {
        return HOME;
    }

    public static MediaFileListService getmediaFileListService() {
        return mediaFileListService;
    }

    public static MediaFileList getmediaFileList() {
        return mediaFileList;
    }

    public static LyricView getlyricView() {
        return lyricView;
    }

    public boolean isPlaying() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    return true;
                }
            }
        } catch (RemoteException ex) {
            Log.e(TAG, "doPauseResume:RemoteException", ex);
        }
        return false;
    }
}
