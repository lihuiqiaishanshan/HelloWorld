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

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.Metadata;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.hisilicon.android.music.util.HiMediaPlayer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Vector;

//import android.media.Metadata;

/**
 * Provides "background" audio playback capabilities, allowing the user to
 * switch between activities without stopping playback.
 */
public class MediaPlaybackService extends Service {
    /**
     * used to specify whether enqueue() should start playing the new list of
     * files right away, next or once all the currently queued files have been
     * played
     */
    private static final String TAG = "MediaPlaybackService";
    public static final int NOW = 1;
    public static final int NEXT = 2;
    public static final int LAST = 3;
    public static final int PLAYBACKSERVICE_STATUS = 1;

    public static final int SHUFFLE_NONE = 0;
    public static final int SHUFFLE_NORMAL = 1;
    public static final int SHUFFLE_AUTO = 2;

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_CURRENT = 1;
    public static final int REPEAT_ALL = 2;

    public static final int SEQUENCE_PLAYER = 0;
    public static final int CIRCULATE_PLAYER = 1;
    public static final int SINGLE_PLAYER = 2;
    public static final int SINGLE_CIRCULATE = 3;
    public static final int RANDOM_PLAYER = 4;


    public static final String PLAYSTATE_CHANGED = "com.hisilicon.android.music.playstatechanged";
    public static final String META_CHANGED = "com.hisilicon.android.music.metachanged";
    public static final String QUEUE_CHANGED = "com.hisilicon.android.music.queuechanged";

    public static final String SERVICECMD = "com.hisilicon.android.music.musicservicecommand";
    public static final String CMDNAME = "command";
    public static final String CMDTOGGLEPAUSE = "togglepause";
    public static final String CMDSTOP = "stop";
    public static final String CMDPAUSE = "pause";
    public static final String CMDPLAY = "play";
    public static final String CMDPREVIOUS = "previous";
    public static final String CMDNEXT = "next";

    public static final String TOGGLEPAUSE_ACTION = "com.hisilicon.android.music.musicservicecommand.togglepause";
    public static final String PAUSE_ACTION = "com.hisilicon.android.music.musicservicecommand.pause";
    public static final String PREVIOUS_ACTION = "com.hisilicon.android.music.musicservicecommand.previous";
    public static final String NEXT_ACTION = "com.hisilicon.android.music.musicservicecommand.next";

    private static final int TRACK_ENDED = 1;
    private static final int RELEASE_WAKELOCK = 2;
    private static final int SERVER_DIED = 3;
    private static final int FOCUSCHANGE = 4;
    private static final int FADEDOWN = 5;
    private static final int FADEUP = 6;
    private static final int MAX_HISTORY_SIZE = 100;

    private MultiPlayer mPlayer;
    public String mFileToPlay;
    private String mFilePathToPlay;
    private int mShuffleMode = SHUFFLE_NONE;
    private int mRepeatMode = REPEAT_NONE;
    private int mMediaMountedCount = 0;
    private long[] mAutoShuffleList = null;
    private boolean isMusicPlaying = false;
    private long[] mPlayList = null;
    private int mPlayListLen = 0;
    private Vector<Integer> mHistory = new Vector<Integer>(MAX_HISTORY_SIZE);
    private Cursor mCursor;
    private int mPlayPos = -1;
    private static final String LOGTAG = "MediaPlaybackService";
    private final Shuffler mRand = new Shuffler();
    String[] mCursorCols = new String[]{
            "audio._id AS _id", // index must match IDCOLIDX below
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.IS_PODCAST, // index must match PODCASTCOLIDX
            // below
            MediaStore.Audio.Media.BOOKMARK // index must match BOOKMARKCOLIDX
            // below
    };
    private final static int IDCOLIDX = 0;
    private final static int PODCASTCOLIDX = 8;
    private final static int BOOKMARKCOLIDX = 9;
    private BroadcastReceiver mUnmountReceiver = null;
    private WakeLock mWakeLock;
    private int mServiceStartId = -1;
    private boolean mServiceInUse = false;
    private boolean mIsSupposedToBePlaying = false;
    private boolean mQuietMode = false;
    private AudioManager mAudioManager;
    private boolean mQueueIsSaveable = true;
    private static int playMode = 0;
    private Toast mToast = null;
    // used to track what type of audio focus loss caused the playback to pause
    private boolean mPausedByTransientLossOfFocus = false;

    private SharedPreferences mPreferences;
    // We use this to distinguish between different cards when saving/restoring
    // playlists.
    // This will have to change if we want to support multiple simultaneous
    // cards.
    private int mCardId;

/*for lint
    private MediaAppWidgetProvider mAppWidgetProvider = MediaAppWidgetProvider
                                                        .getInstance();
*/

    // interval after which we stop the service when idle
    private static final int IDLE_DELAY = 60000;

    //private RemoteControlClient mRemoteControlClient; for fority

    private Thread lyricThread = null;

    private long saveReleasePosition;
    private boolean isReleaseByCMDStop;

    private Handler mMediaplayerHandler = new Handler() {
        float mCurrentVolume = 1.0f;

        @Override
        public void handleMessage(Message msg) {
            MusicUtils
                    .debugLog("mMediaplayerHandler.handleMessage " + msg.what);

            switch (msg.what) {
                case FADEDOWN:
                    mCurrentVolume -= .05f;

                    if (mCurrentVolume > .2f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEDOWN, 10);
                    } else {
                        mCurrentVolume = .2f;
                    }

                    mPlayer.setVolume(mCurrentVolume);
                    break;

                case FADEUP:
                    mCurrentVolume += .01f;

                    if (mCurrentVolume < 1.0f) {
                        mMediaplayerHandler.sendEmptyMessageDelayed(FADEUP, 10);
                    } else {
                        mCurrentVolume = 1.0f;
                    }

                    mPlayer.setVolume(mCurrentVolume);
                    break;

                case SERVER_DIED:
                    if (mIsSupposedToBePlaying) {
                        next(true);
                    } else {
                        openCurrent();
                    }

                    break;

                case TRACK_ENDED:
                    if (mRepeatMode == REPEAT_CURRENT) {
                        seek(0);
                        play();
                    } else if (isSiglePlayerMode()) {
                        pause();
                        notifyChange(PLAYSTATE_CHANGED);
                        break;
                    } else {
                        next(false);
                    }

                    break;

                case RELEASE_WAKELOCK:
                    mWakeLock.release();
                    break;

                case FOCUSCHANGE:

                    // This code is here so we can better synchronize it with the
                    // code that
                    // handles fade-in
                    switch (msg.arg1) {
                        case AudioManager.AUDIOFOCUS_LOSS:
                            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_LOSS");

                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = false;
                            }

                            pause();
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            mMediaplayerHandler.removeMessages(FADEUP);
                            mMediaplayerHandler.sendEmptyMessage(FADEDOWN);
                            break;

                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            Log.v(LOGTAG,
                                    "AudioFocus: received AUDIOFOCUS_LOSS_TRANSIENT");

                            if (isPlaying()) {
                                mPausedByTransientLossOfFocus = true;
                            }

                            pause();
                            break;

                        case AudioManager.AUDIOFOCUS_GAIN:
                            Log.v(LOGTAG, "AudioFocus: received AUDIOFOCUS_GAIN");

                            if (!isPlaying() && mPausedByTransientLossOfFocus) {
                                mPausedByTransientLossOfFocus = false;
                                mCurrentVolume = 0f;
                                mPlayer.setVolume(mCurrentVolume);
                                play(); // also queues a fade-in
                            } else {
                                mMediaplayerHandler.removeMessages(FADEDOWN);
                                mMediaplayerHandler.sendEmptyMessage(FADEUP);
                            }

                            break;

                        default:
                            Log.e(LOGTAG, "Unknown audio focus change code");
                    }

                    break;

                default:
                    break;
            }
        }
    };

    private boolean isSiglePlayerMode() {
        return (getPlayMode() == SINGLE_PLAYER);
    }

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            MusicUtils.debugLog("mIntentReceiver.onReceive " + action + " / "
                    + cmd);

            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                next(true);
            } else if (CMDPREVIOUS.equals(cmd)
                    || PREVIOUS_ACTION.equals(action)) {
                prev();
            } else if (CMDTOGGLEPAUSE.equals(cmd)
                    || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    mWakeLock.acquire();
                    pause();
                    mPausedByTransientLossOfFocus = false;
                    mWakeLock.release();
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
                play();
            } else if (CMDSTOP.equals(cmd)) {
                saveReleasePosition = mPlayer.position();
                isReleaseByCMDStop = true;
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                release();
            }
/*for lint
              else if (MediaAppWidgetProvider.CMDAPPWIDGETUPDATE.equals(cmd)) {
                // Someone asked us to refresh a set of specific widgets,
                // probably
                // because they were just added.

                int[] appWidgetIds = intent
                                     .getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                mAppWidgetProvider.performUpdate(MediaPlaybackService.this,
                                                 appWidgetIds);
            }
*/
        }
    };

    private BroadcastReceiver PowerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (null == action)
                return;

            if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                if (isPlaying()) {
                    pause();
                    isMusicPlaying = true;
                } else {
                    isMusicPlaying = false;
                }
            } else if (action.equals(Intent.ACTION_SCREEN_ON)) {
                if (isMusicPlaying) {
                    play();
                    isMusicPlaying = false;
                }
            }
        }
    };

    private void registerMusicPowerReceiver() {
        IntentFilter powerFilter = new IntentFilter();
        powerFilter.addAction(Intent.ACTION_SCREEN_OFF);
        powerFilter.addAction(Intent.ACTION_SCREEN_ON);
        registerReceiver(PowerReceiver, powerFilter);
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            mMediaplayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0)
                    .sendToTarget();
        }
    };

    public MediaPlaybackService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
/*for lint
        ComponentName rec = new ComponentName(getPackageName(),
                                              MediaButtonIntentReceiver.class.getName());
        mAudioManager.registerMediaButtonEventReceiver(rec);
*/
        mPreferences = getSharedPreferences("Music", MODE_PRIVATE);
        playMode = mPreferences.getInt("playMode",SEQUENCE_PLAYER);
        mRepeatMode = mPreferences.getInt("repeatmode", REPEAT_NONE);
        mShuffleMode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);
        mCardId = MusicUtils.getCardId(this);
        registerExternalStorageListener();
        // Needs to be done in this thread, since otherwise
        // ApplicationContext.getPowerManager() crashes.
        mPlayer = new MultiPlayer();
        mPlayer.setHandler(mMediaplayerHandler);
        // reloadQueue();
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(SERVICECMD);
        commandFilter.addAction(TOGGLEPAUSE_ACTION);
        commandFilter.addAction(PAUSE_ACTION);
        commandFilter.addAction(NEXT_ACTION);
        commandFilter.addAction(PREVIOUS_ACTION);
        registerReceiver(mIntentReceiver, commandFilter);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this
                .getClass().getName());
        mWakeLock.setReferenceCounted(false);
        // If the service was idle, but got killed before it stopped itself, the
        // system will relaunch it. Make sure it gets stopped again in that
        // case.
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        if (SystemProperties.get("persist.suspend.mode").equals("deep_resume")) {
            registerMusicPowerReceiver();
        }
    }

    @Override
    public void onDestroy() {
        // Check that we're not being destroyed while something is still
        // playing.
        if (isPlaying()) {
            Log.e(LOGTAG, "Service being destroyed while still playing.");
        }

        // release all MediaPlayer resources, including the native player and
        // wakelocks
        Intent i = new Intent(
                AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
        i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
        i.setPackage("com.android.musicfx");
        sendBroadcast(i);
        mPlayer.release();
        mPlayer = null;
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        // mAudioManager.unregisterRemoteControlClient(mRemoteControlClient);
        // make sure there aren't any other messages coming
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mMediaplayerHandler.removeCallbacksAndMessages(null);

        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        unregisterReceiver(mIntentReceiver);

        if (mUnmountReceiver != null) {
            unregisterReceiver(mUnmountReceiver);
            mUnmountReceiver = null;
        }

        if (PowerReceiver != null && SystemProperties.get("persist.suspend.mode").equals("deep_resume")) {
            unregisterReceiver(PowerReceiver);
        }

        mWakeLock.release();
        super.onDestroy();
    }

    private final char hexdigits[] = new char[]{'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private void saveQueue(boolean full) {
        if (!mQueueIsSaveable) {
            return;
        }

        Editor ed = mPreferences.edit();

        if (full) {
            StringBuilder q = new StringBuilder();
            int len = mPlayListLen;

            for (int i = 0; i < len; i++) {
                long n = mPlayList[i];

                if (n < 0) {
                    continue;
                } else if (n == 0) {
                    q.append("0;");
                } else {
                    while (n != 0) {
                        int digit = (int) (n & 0xf);
                        n >>>= 4;
                        q.append(hexdigits[digit]);
                    }

                    q.append(";");
                }
            }

            ed.putString("queue", q.toString());
            ed.putInt("cardid", mCardId);

            if (mShuffleMode != SHUFFLE_NONE) {
                // In shuffle mode we need to save the history too
                len = mHistory.size();
                q.setLength(0);

                for (int i = 0; i < len; i++) {
                    int n = mHistory.get(i);

                    if (n == 0) {
                        q.append("0;");
                    } else {
                        while (n != 0) {
                            int digit = (n & 0xf);
                            n >>>= 4;
                            q.append(hexdigits[digit]);
                        }

                        q.append(";");
                    }
                }

                ed.putString("history", q.toString());
            }
        }

        synchronized (this) {
            ed.putInt("curpos", mPlayPos);
        }

        if (mPlayer.isInitialized()) {
            ed.putLong("seekpos", mPlayer.position());
        }
        ed.putInt("playMode",playMode);
        ed.putInt("repeatmode", mRepeatMode);
        ed.putInt("shufflemode", mShuffleMode);
        SharedPreferencesCompat.apply(ed);
    }

    private void reloadQueue() {
        String q = null;
        //boolean newstyle = false; //for fority
        int id = mCardId;

        if (mPreferences.contains("cardid")) {
            //newstyle = true; //for fority
            id = mPreferences.getInt("cardid", ~mCardId);
        }

        if (id == mCardId) {
            // Only restore the saved playlist if the card is still
            // the same one as when the playlist was saved
            q = mPreferences.getString("queue", "");
        }

        int qlen = q != null ? q.length() : 0;

        if (qlen > 1) {
            // Log.i("@@@@ service", "loaded queue: " + q);
            int plen = 0;
            int n = 0;
            int shift = 0;

            for (int i = 0; i < qlen; i++) {
                char c = q.charAt(i);

                if (c == ';') {
                    ensurePlayListCapacity(plen + 1);
                    mPlayList[plen] = n;
                    plen++;
                    n = 0;
                    shift = 0;
                } else {
                    if (c >= '0' && c <= '9') {
                        n += ((c - '0') << shift);
                    } else if (c >= 'a' && c <= 'f') {
                        n += ((10 + c - 'a') << shift);
                    } else {
                        // bogus playlist data
                        plen = 0;
                        break;
                    }

                    shift += 4;
                }
            }

            mPlayListLen = plen;
            int pos = mPreferences.getInt("curpos", 0);

            if (pos < 0 || pos >= mPlayListLen) {
                // The saved playlist is bogus, discard it
                mPlayListLen = 0;
                return;
            }

            synchronized (this) {
                mPlayPos = pos;
            }
            // When reloadQueue is called in response to a card-insertion,
            // we might not be able to query the media provider right away.
            // To deal with this, try querying for the current file, and if
            // that fails, wait a while and try again. If that too fails,
            // assume there is a problem and don't restore the state.
            Cursor crsr = MusicUtils.query(this,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    new String[]{"_id"}, "_id=" + mPlayList[pos], null,
                    null);

            if (crsr == null || crsr.getCount() == 0) {
                if (crsr != null) {
                    crsr.close();
                }
                // wait a bit and try again
                SystemClock.sleep(3000);
                crsr = getContentResolver().query(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursorCols, "_id=" + mPlayList[pos], null, null);
            }

            if (crsr != null) {
                crsr.close();
            }

            // Make sure we don't auto-skip to the next song, since that
            // also starts playback. What could happen in that case is:
            // - music is paused
            // - go to UMS and delete some files, including the currently
            // playing one
            // - come back from UMS
            // (time passes)
            // - music app is killed for some reason (out of memory)
            // - music service is restarted, service restores state, doesn't
            // find
            // the "current" file, goes to the next and: playback starts on its
            // own, potentially at some random inconvenient time.
            mQuietMode = true;
            openCurrent();
            mQuietMode = false;

            if (!mPlayer.isInitialized()) {
                // couldn't restore the saved state
                mPlayListLen = 0;
                return;
            }

            long seekpos = mPreferences.getLong("seekpos", 0);
            seek(seekpos >= 0 && seekpos < duration() ? seekpos : 0);
            Log.d(LOGTAG, "restored queue, currently at position " + position()
                    + "/" + duration() + " (requested " + seekpos + ")");
            int repmode = mPreferences.getInt("repeatmode", REPEAT_NONE);

            if (repmode != REPEAT_ALL && repmode != REPEAT_CURRENT) {
                repmode = REPEAT_NONE;
            }

            mRepeatMode = repmode;
            int shufmode = mPreferences.getInt("shufflemode", SHUFFLE_NONE);

            if (shufmode != SHUFFLE_AUTO && shufmode != SHUFFLE_NORMAL) {
                shufmode = SHUFFLE_NONE;
            }

            if (shufmode != SHUFFLE_NONE) {
                // in shuffle mode we need to restore the history too
                q = mPreferences.getString("history", "");
                qlen = q != null ? q.length() : 0;

                if (qlen > 1) {
                    //plen = 0;
                    n = 0;
                    shift = 0;
                    mHistory.clear();

                    for (int i = 0; i < qlen; i++) {
                        char c = q.charAt(i);

                        if (c == ';') {
                            if (n >= mPlayListLen) {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }

                            mHistory.add(n);
                            n = 0;
                            shift = 0;
                        } else {
                            if (c >= '0' && c <= '9') {
                                n += ((c - '0') << shift);
                            } else if (c >= 'a' && c <= 'f') {
                                n += ((10 + c - 'a') << shift);
                            } else {
                                // bogus history data
                                mHistory.clear();
                                break;
                            }

                            shift += 4;
                        }
                    }
                }
            }

            if (shufmode == SHUFFLE_AUTO) {
                if (!makeAutoShuffleList()) {
                    shufmode = SHUFFLE_NONE;
                }
            }

            mShuffleMode = shufmode;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        mServiceInUse = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        mDelayedStopHandler.removeCallbacksAndMessages(null);

        if (intent != null) {
            String action = intent.getAction();
            String cmd = intent.getStringExtra("command");
            MusicUtils.debugLog("onStartCommand " + action + " / " + cmd);

            if (CMDNEXT.equals(cmd) || NEXT_ACTION.equals(action)) {
                next(true);
            } else if (CMDPREVIOUS.equals(cmd)
                    || PREVIOUS_ACTION.equals(action)) {
                if (position() < 2000) {
                    prev();
                } else {
                    seek(0);
                    play();
                }
            } else if (CMDTOGGLEPAUSE.equals(cmd)
                    || TOGGLEPAUSE_ACTION.equals(action)) {
                if (isPlaying()) {
                    pause();
                    mPausedByTransientLossOfFocus = false;
                } else {
                    play();
                }
            } else if (CMDPAUSE.equals(cmd) || PAUSE_ACTION.equals(action)) {
                pause();
                mPausedByTransientLossOfFocus = false;
            } else if (CMDPLAY.equals(cmd)) {
                play();
            } else if (CMDSTOP.equals(cmd)) {
                pause();
                mPausedByTransientLossOfFocus = false;
                seek(0);
                release();
            }
        }

        // make sure the service will shut down on its own if it was
        // just started but not bound to and nothing is playing
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mServiceInUse = false;
        // Take a snapshot of the current playlist
        saveQueue(true);

        if (isPlaying() || mPausedByTransientLossOfFocus) {
            return true;
        }
        if (mPlayListLen > 0 || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
            Message msg = mDelayedStopHandler.obtainMessage();
            mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
            return true;
        }

        // No active playlist, OK to stop the service right now
        stopSelf(mServiceStartId);
        return super.onUnbind(intent);
    }

    private Handler mDelayedStopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // Check again to make sure nothing is playing right now
            if (isPlaying() || mPausedByTransientLossOfFocus || mServiceInUse
                    || mMediaplayerHandler.hasMessages(TRACK_ENDED)) {
                return;
            }

            // save the queue again, because it might have changed
            // since the user exited the music app (because of
            // party-shuffle or because the play-position changed)
            saveQueue(true);
            stopSelf(mServiceStartId);
        }
    };

    /**
     * Called when we receive a ACTION_MEDIA_EJECT notification.
     *
     * @param storagePath path to mount point for the removed media
     */
    public void closeExternalStorageFiles(String storagePath) {
        // stop playback and clean up if the SD card is going to be unmounted.
        if (MediaPlaybackActivity.getmediaFileList() != null && MediaPlaybackActivity.getmediaFileList()
                .getNextRandomMusicInfo(null) != null) {
            String filePath = MediaPlaybackActivity.getmediaFileList()
                    .getNextRandomMusicInfo(null).getPath();
            if (filePath.startsWith(storagePath)) {
                MediaPlaybackActivity.getmediaFileList().umountPath(storagePath);
                stop(true);
                seek(0);
            }
        }
        notifyChange(QUEUE_CHANGED);
        notifyChange(META_CHANGED);
    }

    /**
     * Registers an intent to listen for ACTION_MEDIA_EJECT notifications. The
     * intent will call closeExternalStorageFiles() if the external media is
     * going to be ejected, so applications can clean up any files they have
     * open.
     */
    public void registerExternalStorageListener() {
        if (mUnmountReceiver == null) {
            mUnmountReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (null == action) {
                        return;
                    }

                    if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                        saveQueue(true);
                        mQueueIsSaveable = false;
                        if (null == intent.getData()) {
                            return;
                        }
                        closeExternalStorageFiles(intent.getData().getPath());
                    } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                        mMediaMountedCount++;
                        mCardId = MusicUtils
                                .getCardId(MediaPlaybackService.this);
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                        mMediaMountedCount--;
                        mCardId = MusicUtils
                                .getCardId(MediaPlaybackService.this);
                        reloadQueue();
                        mQueueIsSaveable = true;
                        notifyChange(QUEUE_CHANGED);
                        notifyChange(META_CHANGED);
                    } else if (action.equals("MEDIA_SCANNER_MEDIA_PATH")) {
                        Uri uri = intent.getData();
                        if (null == uri) {
                            return;
                        }

                        if ("file".equals(uri.getScheme())) {
                            String path = uri.getPath();
                            if (path.equals(mFilePathToPlay))
                                if (isPlaying()) {
                                    Log.i("MediaPlaybackService", "File have be delete");
                                    next(true);
                                }
                        }
                    }
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            iFilter.addAction("MEDIA_SCANNER_MEDIA_PATH");
            iFilter.addDataScheme("file");
            registerReceiver(mUnmountReceiver, iFilter);
        }
    }

    /**
     * Notify the change-receivers that something has changed. The intent that
     * is sent contains the following data for the currently playing track: "id"
     * - Integer: the database row ID "artist" - String: the name of the artist
     * "album" - String: the name of the album "track" - String: the name of the
     * track The intent has an action that is one of
     * "com.hisilicon.android.music.metachanged"
     * "com.hisilicon.android.music.queuechanged",
     * "com.hisilicon.android.music.playbackcomplete"
     * "com.hisilicon.android.music.playstatechanged" respectively indicating
     * that a new track has started playing, that the playback queue has
     * changed, that playback has stopped because the last file in the list has
     * been played, or that the play-state changed (paused/resumed).
     */
    private void notifyChange(String what) {
        Intent i = new Intent(what);
        i.putExtra("id", Long.valueOf(getAudioId()));
        i.putExtra("artist", getArtistName());
        i.putExtra("album", getAlbumName());
        i.putExtra("track", getTrackName());
        i.putExtra("playing", isPlaying());
        sendStickyBroadcast(i);

        if (what.equals(PLAYSTATE_CHANGED)) {
            // mRemoteControlClient.setPlaybackState(isPlaying() ?
            // RemoteControlClient.PLAYSTATE_PLAYING :
            // RemoteControlClient.PLAYSTATE_PAUSED);
        } else if (what.equals(META_CHANGED)) {
        }

        if (what.equals(QUEUE_CHANGED)) {
            saveQueue(true);
        } else {
            saveQueue(false);
        }

        // Share this notification directly with our widgets

        Log.d(LOGTAG, "MediaAppWidgetProvider mAppWidgetProvider doesn't find");
//System.exit(0);
/*for lint
        mAppWidgetProvider.notifyChange(this, what);
*/
    }

    private void ensurePlayListCapacity(int size) {
        if (mPlayList == null || size > mPlayList.length) {
            // reallocate at 2x requested size so we don't
            // need to grow and copy the array for every
            // insert
            long[] newlist = new long[size * 2];
            //int len = mPlayList != null ? mPlayList.length : mPlayListLen; //for fority
            if (mPlayList != null) {
                for (int i = 0; i < mPlayList.length; i++) {
                    newlist[i] = mPlayList[i];
                }
            }
            mPlayList = newlist;
        }

        // FIXME: shrink the array when the needed size is much smaller
        // than the allocated size
    }

    // insert the list of songs at the specified position in the playlist
    private void addToPlayList(long[] list, int position) {
        int addlen = list.length;

        if (position < 0) { // overwrite
            mPlayListLen = 0;
            position = 0;
        }

        ensurePlayListCapacity(mPlayListLen + addlen);

        if (position > mPlayListLen) {
            position = mPlayListLen;
        }

        // move part of list after insertion point
        int tailsize = mPlayListLen - position;

        for (int i = tailsize; i > 0; i--) {
            mPlayList[position + i] = mPlayList[position + i - addlen];
        }

        // copy list into playlist
        for (int i = 0; i < addlen; i++) {
            mPlayList[position + i] = list[i];
        }

        mPlayListLen += addlen;

        if (mPlayListLen == 0) {
            mCursor.close();
            mCursor = null;
            notifyChange(META_CHANGED);
        }
    }

    /**
     * Appends a list of tracks to the current playlist. If nothing is playing
     * currently, playback will be started at the first track. If the action is
     * NOW, playback will switch to the first of the new tracks immediately.
     *
     * @param list   The list of tracks to append.
     * @param action NOW, NEXT or LAST
     */
    public void enqueue(long[] list, int action) {
        synchronized (this) {
            if (action == NEXT && mPlayPos + 1 < mPlayListLen) {
                addToPlayList(list, mPlayPos + 1);
                notifyChange(QUEUE_CHANGED);
            } else {
                // action == LAST || action == NOW || mPlayPos + 1 ==
                // mPlayListLen
                addToPlayList(list, Integer.MAX_VALUE);
                notifyChange(QUEUE_CHANGED);

                if (action == NOW) {
                    mPlayPos = mPlayListLen - list.length;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                }
            }

            if (mPlayPos < 0) {
                mPlayPos = 0;
                openCurrent();
                play();
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Replaces the current playlist with a new list, and prepares for starting
     * playback at the specified position in the list, or a random position if
     * the specified position is 0.
     *
     * @param list The new list of tracks.
     */
    public void open(long[] list, int position) {
        synchronized (this) {
            if (mShuffleMode == SHUFFLE_AUTO) {
                mShuffleMode = SHUFFLE_NORMAL;
            }
            long oldId = getAudioId();
            int listlength = list.length;
            boolean newlist = true;

            if (mPlayListLen == listlength) {
                // possible fast path: list might be the same
                newlist = false;

                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlayList[i]) {
                        newlist = true;
                        break;
                    }
                }
            }

            if (newlist) {
                addToPlayList(list, -1);
                notifyChange(QUEUE_CHANGED);
            }

            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos = mRand.nextInt(mPlayListLen);
            }

            mHistory.clear();
            saveBookmarkIfNeeded();
            openCurrent();

            if (oldId != getAudioId()) {
                notifyChange(META_CHANGED);
            }
        }
    }

    /**
     * Moves the item at index1 to index2.
     *
     * @param index1
     * @param index2
     */
    public void moveQueueItem(int index1, int index2) {
        synchronized (this) {
            if (index1 >= mPlayListLen) {
                index1 = mPlayListLen - 1;
            }

            if (index2 >= mPlayListLen) {
                index2 = mPlayListLen - 1;
            }

            if (index1 < index2) {
                long tmp = mPlayList[index1];

                for (int i = index1; i < index2; i++) {
                    mPlayList[i] = mPlayList[i + 1];
                }

                mPlayList[index2] = tmp;

                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index1 && mPlayPos <= index2) {
                    mPlayPos--;
                }
            } else if (index2 < index1) {
                long tmp = mPlayList[index1];

                for (int i = index1; i > index2; i--) {
                    mPlayList[i] = mPlayList[i - 1];
                }

                mPlayList[index2] = tmp;

                if (mPlayPos == index1) {
                    mPlayPos = index2;
                } else if (mPlayPos >= index2 && mPlayPos <= index1) {
                    mPlayPos++;
                }
            }

            notifyChange(QUEUE_CHANGED);
        }
    }

    /**
     * Returns the current play list
     *
     * @return An array of integers containing the IDs of the tracks in the play
     * list
     */
    public long[] getQueue() {
        synchronized (this) {
            int len = mPlayListLen;
            long[] list = new long[len];

            for (int i = 0; i < len; i++) {
                list[i] = mPlayList[i];
            }

            return list;
        }
    }

    private void openCurrent() {
        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }

            if (mPlayListLen == 0) {
                return;
            }

            stop(false);
            String id = String.valueOf(mPlayList[mPlayPos]);
            mCursor = getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mCursorCols,
                    "_id=" + id, null, null);

            if (mCursor != null) {
                mCursor.moveToFirst();
                open(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/" + id);

                mFilePathToPlay = mCursor.getString(mCursor.getColumnIndex("_data"));

                // go to bookmark if needed
                if (isPodcast()) {
                    long bookmark = getBookmark();
                    // Start playing a little bit before the bookmark,
                    // so it's easier to get back in to the narrative.
                    seek(bookmark - 5000);
                }
            }
        }
    }

    ArrayList<String> mOpenFailedList = new ArrayList<String>();

    /**
     * Opens the specified file and readies it for playback.
     *
     * @param path The full path of the file to be opened.
     */
    public void open(String path) {
        synchronized (this) {
            if (path == null) {
                return;
            }
            File file = new File(path);
            if (!file.exists()) {
                showToast(R.string.playback_file_not_found);
                return;
            }
            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            MediaPlaybackActivity.getmediaFileList().updatePositionInfo();//update isFirstMediaFile and isLastMediaFile

            if (!mPlayer.isInitialized()) {
                if (mOpenFailedList.contains(mFileToPlay)) {
                    // beware: this ends up being recursive because next() calls
                    // open() again.
                    return;
                } else {
                    mOpenFailedList.add(mFileToPlay);
                }

                if (!mPlayer.isInitialized()) {
                    // need to make sure we only shows this once
                    if (!mQuietMode) {
                        showToast(R.string.playback_failed);
                        int mode = MediaPlaybackService.getPlayMode();
                        if (mode == MediaPlaybackService.SEQUENCE_PLAYER && Common.isLastMediaFile()) {
                            showToast(R.string.isthelastfile);
                            prev();
                        } else if (mode == MediaPlaybackService.SEQUENCE_PLAYER && Common.isFirstMediaFile()) {
                            showToast(R.string.isthefirstfile);
                            next(true);
                        } else if (Common.isPrevButton()) {
                            prev();
                            Common.setisPrevButton(false);
                        } else {
                            next(true);
                        }
                    }

                    Log.d(LOGTAG, "Failed to open file for playback");
                }
            }
            if (Common.isFileList()) {
                Intent intent = new Intent(FileListActivity.ACTION_UPDATE_FILELIST);
                intent.setPackage("com.hisilicon.android.music");
                intent.putExtra("filepath", path);
                sendBroadcast(intent, "com.hisilicon.android.music.ACTION_HISI_MUSIC");
            }

            mIsSupposedToBePlaying = true;
        }
    }

    /* no use
    public class startLyricThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Audio pli = new Audio(getTitleName(), mFileToPlay, 0L, true);
            WeakReference<Audio> weakRf_audio = new WeakReference<Audio>(pli);
            File f = new File(mFileToPlay.replace(".mp3", ".lrc"));
            WeakReference<File> weakRf_file = new WeakReference<File>(f);
            Lyric lyric;
            lyric = new Lyric(weakRf_audio.get());
            WeakReference<Lyric> weakRf_lyric = new WeakReference<Lyric>(lyric);
            MediaPlaybackActivity.getlyricView().setmLyric(weakRf_lyric.get());
            MediaPlaybackActivity.getlyricView()
            .setSentencelist(weakRf_lyric.get().list);
            MediaPlaybackActivity.getlyricView()
            .setNotCurrentPaintColor(Color.WHITE);
            MediaPlaybackActivity.getlyricView().setCurrentPaintColor(Color.WHITE);
            // MediaPlaybackActivity.lyricView
            //.setNotCurrentPaintColor(Color.GREEN);
            //MediaPlaybackActivity.lyricView.setCurrentPaintColor(Color.YELLOW);
            MediaPlaybackActivity.getlyricView().setCurrentTextSize(40);
            MediaPlaybackActivity.getlyricView().setLrcTextSize(22);
            MediaPlaybackActivity.getlyricView().setTexttypeface(Typeface.SERIF);
            MediaPlaybackActivity.getlyricView()
            .setBrackgroundcolor(Color.TRANSPARENT);
            MediaPlaybackActivity.getlyricView().setTextHeight(20);
            if (lyricThread == null) {
                lyricThread = new Thread(new UIUpdateThread());
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
            while (isPlaying()) {
                try {
                    MediaPlaybackActivity.getlyricView().updateIndex(position());
                    mLyricHandler.post(mUpdateResults);
                    Thread.sleep(time);
                } catch (Exception e) {
                    Log.e(TAG,"UIUpdateThread",e);
                    break;
                }
            }
        }
    }

    Handler mLyricHandler = new Handler();
    Runnable mUpdateResults = new Runnable() {
        public void run() {
            MediaPlaybackActivity.getlyricView().invalidate();
        }
    };*/

    /**
     * Starts playback of a previously opened file.
     */
    public void play() {
        if (MusicUtils.mIsVideoViewOpened) {
            return;
        }
//        String filePath = MediaPlaybackActivity.mediaFileList
//                .getNextRandomMusicInfo(null).getPath();
        if (null == mFileToPlay) {
            return;
        }

        if (!Common.isSecurePath(mFileToPlay)) {
            Log.e(TAG, "play:Path Manipulation");
            return;
        }
        File file = new File(mFileToPlay);
        if (!file.exists())
            return;



        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
/*for lint
        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(this
                                                                         .getPackageName(), MediaButtonIntentReceiver.class.getName()));
*/

        if (mPlayer.isInitialized()) {
            // if we are at the end of the song, go to the next song first
            long duration = mPlayer.duration();

            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
                    && mPlayer.position() >= duration - 2000) {
                next(true);
            }
            mPlayer.start();
            // make sure we fade in, in case a previous fadein was stopped
            // because
            // of another focus loss
            mMediaplayerHandler.removeMessages(FADEDOWN);
            mMediaplayerHandler.sendEmptyMessage(FADEUP);
            RemoteViews views = new RemoteViews(getPackageName(),
                    R.layout.statusbar);
            views.setImageViewResource(R.id.icon,
                    R.drawable.stat_notify_musicplayer);

            if (getAudioId() < 0) {
                // streaming
                views.setTextViewText(R.id.trackname, getPath());
                views.setTextViewText(R.id.artistalbum, null);
            } else {
                String artist = getArtistName();
                views.setTextViewText(R.id.trackname, getTrackName());

                if (artist == null || artist.equals(MediaStore.UNKNOWN_STRING)) {
                    artist = getString(R.string.unknown_artist_name);
                }

                String album = getAlbumName();

                if (album == null || album.equals(MediaStore.UNKNOWN_STRING)) {
                    album = getString(R.string.unknown_album_name);
                }

                views.setTextViewText(
                        R.id.artistalbum,
                        getString(R.string.notification_artist_album, artist,
                                album));
            }

            Notification status = new Notification();
            status.contentView = views;
            status.flags |= Notification.FLAG_ONGOING_EVENT;
            status.icon = R.drawable.stat_notify_musicplayer;
            status.contentIntent = PendingIntent.getActivity(this, 0,
                    new Intent("com.hisilicon.android.music.PLAYBACK_VIEWER")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK), 0);
            startForeground(PLAYBACKSERVICE_STATUS, status);

            if (!mIsSupposedToBePlaying) {
                mIsSupposedToBePlaying = true;
                notifyChange(PLAYSTATE_CHANGED);
            }
        }else if(isReleaseByCMDStop){
            mPlayer = new MultiPlayer();
            mPlayer.setHandler(mMediaplayerHandler);
            open(mFileToPlay);
            mPlayer.seek(saveReleasePosition);
            play();
            isReleaseByCMDStop = false;
        } else if (mPlayListLen <= 0) {
            // This is mostly so that if you press 'play' on a bluetooth headset
            // without every having played anything before, it will still play
            // something.
//            mPlayer = new MultiPlayer();
//            mPlayer.setHandler(mMediaplayerHandler);
//            open(mFileToPlay);
//            mPlayer.seek(saveReleasePosition);
//            play();
            setShuffleMode(SHUFFLE_AUTO);
        }

        if (!mIsSupposedToBePlaying) {
            mIsSupposedToBePlaying = true;
            notifyChange(PLAYSTATE_CHANGED);
        }
/*
        if (lyricThread == null) {
            lyricThread = new Thread(new UIUpdateThread());
            lyricThread.start();
        }*/
    }

    private void stop(boolean remove_status_icon) {
        if (mPlayer.isInitialized()) {
            mPlayer.stop();
        }

        //mFileToPlay = null;

        synchronized (this) {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }

        if (remove_status_icon) {
            gotoIdleState();
        } else {
            stopForeground(false);
        }

        if (remove_status_icon) {
            mIsSupposedToBePlaying = false;
        }
    }

    /**
     * Stops playback.
     */
    public void stop() {
        stop(true);
    }

    public void stopService() {
        stopSelf();
    }

    /**
     * Pauses playback (call play() to resume)
     */
    public void pause() {
        synchronized (this) {
            mMediaplayerHandler.removeMessages(FADEUP);

            if (isPlaying()) {
                mPlayer.pause();
                gotoIdleState();
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
                saveBookmarkIfNeeded();
            }
        }
    }

    public void release() {
        synchronized (this) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    /**
     * Returns whether something is currently playing
     *
     * @return true if something is playing (or will be playing shortly, in case
     * we're currently transitioning between tracks), false if not.
     */
    public boolean isPlaying() {
        return mIsSupposedToBePlaying;
    }

    /*
     * Desired behavior for prev/next/shuffle:
     * - NEXT will move to the next track in the list when not shuffling, and to
     * a track randomly picked from the not-yet-played tracks when shuffling. If
     * all tracks have already been played, pick from the full set, but avoid
     * picking the previously played track if possible. - when shuffling, PREV
     * will go to the previously played track. Hitting PREV again will go to the
     * track played before that, etc. When the start of the history has been
     * reached, PREV is a no-op. When not shuffling, PREV will go to the
     * sequentially previous track (the difference with the shuffle-case is
     * mainly that when not shuffling, the user can back up to tracks that are
     * not in the history).
     * Example: When playing an album with 10 tracks from the start, and
     * enabling shuffle while playing track 5, the remaining tracks (6-10) will
     * be shuffled, e.g. the final play order might be 1-2-3-4-5-8-10-6-9-7.
     * When hitting 'prev' 8 times while playing track 7 in this example, the
     * user will go to tracks 9-6-10-8-5-4-3-2. If the user then hits 'next', a
     * random track will be picked again. If at any time user disables shuffling
     * the next/previous track will be picked in sequential order again.
     */

    public void prev() {
        synchronized (this) {
            if (null == MediaPlaybackActivity.getmediaFileList()) {
                return;
            }

            if (getPlayMode() == RANDOM_PLAYER) {
                String filePath = MediaPlaybackActivity.getmediaFileList().getPreRandomMusicInfo(null).getPath();
                if (null == filePath) {
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);

                return;
            } else if (getPlayMode() == CIRCULATE_PLAYER) {
                String filePath = MediaPlaybackActivity.getmediaFileList()
                        .getPreMusicInfo(null).getPath();
                if (null == filePath) {
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);
                return;
            } else {
                Log.v(TAG, "Common.isFirstMediaFile = " + Common.isFirstMediaFile());
                if (Common.isFirstMediaFile())
                    return;
                String filePath = MediaPlaybackActivity.getmediaFileList()
                        .getPreMusicInfo_NoCycle(null).getPath();
                if (null == filePath) {
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);
                return;
            }
        }
    }

    public void next(boolean force) {
        synchronized (this) {
            if (null == MediaPlaybackActivity.getmediaFileList()) {
                mIsSupposedToBePlaying = false;
                notifyChange(PLAYSTATE_CHANGED);
                return;
            }

            if (getPlayMode() == RANDOM_PLAYER) {
                Log.v(TAG, "suiji");
                String filePath = MediaPlaybackActivity.getmediaFileList()
                        .getNextRandomMusicInfo(null).getPath();
                if (null == filePath) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);
                return;
            } else if (getPlayMode() == CIRCULATE_PLAYER) {
                Log.v(TAG, " xunhuan");
                MusicModel model = MediaPlaybackActivity.getmediaFileList().getNextMusicInfo(null);

                if (model == null) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                    return;
                }

                String filePath = model.getPath();

                if (null == filePath) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);
                return;
            } else if (getPlayMode() == SEQUENCE_PLAYER ||
                    getPlayMode() == SINGLE_PLAYER ||
                    getPlayMode() == SINGLE_CIRCULATE) {
                if (Common.isLastMediaFile()) {
                    if (duration() == position()) {//update UI
                        mIsSupposedToBePlaying = false;
                        notifyChange(PLAYSTATE_CHANGED);
                    }
                    return;
                }
                MusicModel model = MediaPlaybackActivity.getmediaFileList().getNextMusicInfo_NoCycle(null);
                Log.v(TAG, " shunxu Common.isLastMediaFile =" + Common.isLastMediaFile());
                if (model == null) {
                    Log.v(TAG, " shunxu model == null");
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                    return;
                }
                String filePath = model.getPath();
                if (null == filePath) {
                    mIsSupposedToBePlaying = false;
                    notifyChange(PLAYSTATE_CHANGED);
                    return;
                }
                open(filePath);
                play();
                notifyChange(META_CHANGED);
                return;
            } else {
                return;
            }
        }
    }

    private void gotoIdleState() {
        mDelayedStopHandler.removeCallbacksAndMessages(null);
        Message msg = mDelayedStopHandler.obtainMessage();
        mDelayedStopHandler.sendMessageDelayed(msg, IDLE_DELAY);
        stopForeground(true);
    }

    private void saveBookmarkIfNeeded() {
        try {
            if (isPodcast()) {
                long pos = position();
                long bookmark = getBookmark();
                long duration = duration();

                if ((pos < bookmark && (pos + 10000) > bookmark)
                        || (pos > bookmark && (pos - 10000) < bookmark)) {
                    // The existing bookmark is close to the current
                    // position, so don't update it.
                    return;
                }

                if (pos < 15000 || (pos + 10000) > duration) {
                    // if we're near the start or end, clear the bookmark
                    pos = 0;
                }

                // write 'pos' to the bookmark field
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.BOOKMARK, pos);
                Uri uri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mCursor.getLong(IDCOLIDX));
                getContentResolver().update(uri, values, null, null);
            }
        } catch (SQLiteException ex) {
            Log.e(TAG, "saveBookmarkIfNeeded", ex);
        }
    }

    // Make sure there are at least 5 items after the currently playing item
    // and no more than 10 items before.
    private void doAutoShuffleUpdate() {
        boolean notify = false;

        // remove old entries
        if (mPlayPos > 10) {
            removeTracks(0, mPlayPos - 9);
            notify = true;
        }

        // add new entries if needed
        int to_add = 7 - (mPlayListLen - (mPlayPos < 0 ? -1 : mPlayPos));

        for (int i = 0; i < to_add; i++) {
            // pick something at random from the list
            int lookback = mHistory.size();
            int idx = -1;

            while (true) {
                idx = mRand.nextInt(mAutoShuffleList.length);

                if (!wasRecentlyUsed(idx, lookback)) {
                    break;
                }

                lookback /= 2;
            }

            mHistory.add(idx);

            if (mHistory.size() > MAX_HISTORY_SIZE) {
                mHistory.remove(0);
            }

            ensurePlayListCapacity(mPlayListLen + 1);
            mPlayList[mPlayListLen++] = mAutoShuffleList[idx];
            notify = true;
        }

        if (notify) {
            notifyChange(QUEUE_CHANGED);
        }
    }

    // check that the specified idx is not in the history (but only look at at
    // most lookbacksize entries in the history)
    private boolean wasRecentlyUsed(int idx, int lookbacksize) {
        // early exit to prevent infinite loops in case idx == mPlayPos
        if (lookbacksize == 0) {
            return false;
        }

        int histsize = mHistory.size();

        if (histsize < lookbacksize) {
            Log.d(LOGTAG, "lookback too big");
            lookbacksize = histsize;
        }

        int maxidx = histsize - 1;

        for (int i = 0; i < lookbacksize; i++) {
            long entry = mHistory.get(maxidx - i);

            if (entry == idx) {
                return true;
            }
        }

        return false;
    }

    // A simple variation of Random that makes sure that the
    // value it returns is not equal to the value it returned
    // previously, unless the interval is 1.
    private static class Shuffler {
        private int mPrevious;
        private SecureRandom mRandom = new SecureRandom();

        public int nextInt(int interval) {
            int ret;

            do {
                ret = mRandom.nextInt(interval);
            } while (ret == mPrevious && interval > 1);

            mPrevious = ret;
            return ret;
        }
    }

    ;

    private boolean makeAutoShuffleList() {
        ContentResolver res = getContentResolver();
        Cursor c = res.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Media._ID},
                MediaStore.Audio.Media.IS_MUSIC + "=1", null, null);

        if (null == c) {
            return false;
        }
        if (0 == c.getCount()) {
            c.close();
            return false;
        }

        int len = c.getCount();
        long[] list = new long[len];

        for (int i = 0; i < len; i++) {
            c.moveToNext();
            list[i] = c.getLong(0);
        }

        mAutoShuffleList = list;
        c.close();
        return true;
    }

    /**
     * Removes the range of tracks specified from the play list. If a file
     * within the range is the file currently being played, playback will move
     * to the next file after the range.
     *
     * @param first The first file to be removed
     * @param last  The last file to be removed
     * @return the number of tracks deleted
     */
    public int removeTracks(int first, int last) {
        int numremoved = removeTracksInternal(first, last);

        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }

        return numremoved;
    }

    private int removeTracksInternal(int first, int last) {
        synchronized (this) {
            if (last < first) {
                return 0;
            }

            if (first < 0) {
                first = 0;
            }

            if (last >= mPlayListLen) {
                last = mPlayListLen - 1;
            }

            boolean gotonext = false;

            if (first <= mPlayPos && mPlayPos <= last) {
                mPlayPos = first;
                gotonext = true;
            } else if (mPlayPos > last) {
                mPlayPos -= (last - first + 1);
            }

            int num = mPlayListLen - last - 1;

            for (int i = 0; i < num; i++) {
                mPlayList[first + i] = mPlayList[last + 1 + i];
            }

            mPlayListLen -= last - first + 1;

            if (gotonext) {
                if (mPlayListLen == 0) {
                    stop(true);
                    mPlayPos = -1;

                    if (mCursor != null) {
                        mCursor.close();
                        mCursor = null;
                    }
                } else {
                    if (mPlayPos >= mPlayListLen) {
                        mPlayPos = 0;
                    }

                    boolean wasPlaying = isPlaying();
                    stop(false);
                    openCurrent();

                    if (wasPlaying) {
                        play();
                    }
                }

                notifyChange(META_CHANGED);
            }

            return last - first + 1;
        }
    }

    /**
     * Removes all instances of the track with the given id from the playlist.
     *
     * @param id The id to be removed
     * @return how many instances of the track were removed
     */
    public int removeTrack(long id) {
        int numremoved = 0;

        synchronized (this) {
            for (int i = 0; i < mPlayListLen; i++) {
                if (mPlayList[i] == id) {
                    numremoved += removeTracksInternal(i, i);
                    i--;
                }
            }
        }

        if (numremoved > 0) {
            notifyChange(QUEUE_CHANGED);
        }

        return numremoved;
    }

    public void setShuffleMode(int shufflemode) {
        synchronized (this) {
            if (mShuffleMode == shufflemode && mPlayListLen > 0) {
                return;
            }

            mShuffleMode = shufflemode;

            if (mShuffleMode == SHUFFLE_AUTO) {
                if (makeAutoShuffleList()) {
                    mPlayListLen = 0;
                    doAutoShuffleUpdate();
                    mPlayPos = 0;
                    openCurrent();
                    play();
                    notifyChange(META_CHANGED);
                    return;
                } else {
                    // failed to build a list of files to shuffle
                    mShuffleMode = SHUFFLE_NONE;
                }
            }

            saveQueue(false);
        }
    }

    public int getShuffleMode() {
        return mShuffleMode;
    }

    public void setRepeatMode(int repeatmode) {
        synchronized (this) {
            mRepeatMode = repeatmode;
            saveQueue(false);
        }
    }

    public int getRepeatMode() {
        return mRepeatMode;
    }

    public int getMediaMountedCount() {
        return mMediaMountedCount;
    }

    /**
     * Returns the path of the currently playing file, or null if no file is
     * currently playing.
     */
    public String getPath() {
        return mFileToPlay;
    }

    /**
     * Returns the rowid of the currently playing file, or -1 if no file is
     * currently playing.
     */
    public long getAudioId() {
        synchronized (this) {
            if (mPlayPos >= 0 && mPlayer.isInitialized()) {
                return mPlayList[mPlayPos];
            }
        }

        return -1;
    }

    /**
     * Returns the position in the queue
     *
     * @return the position in the queue
     */
    public int getQueuePosition() {
        synchronized (this) {
            return mPlayPos;
        }
    }

    /**
     * Starts playing the track at the given position in the queue.
     *
     * @param pos The position in the queue of the track that will be played.
     */
    public void setQueuePosition(int pos) {
        synchronized (this) {
            stop(false);
            mPlayPos = pos;
            openCurrent();
            play();
            notifyChange(META_CHANGED);

            if (mShuffleMode == SHUFFLE_AUTO) {
                doAutoShuffleUpdate();
            }
        }
    }

    /*
     * public String getArtistName() { synchronized(this) { if (mCursor == null)
     * { return null; } return
     * mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore
     * .Audio.Media.ARTIST)); } }
     */

    public long getArtistId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }

            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        }
    }

    /*
     * public String getAlbumName() { synchronized (this) { if (mCursor == null)
     * { return null; } return
     * mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore
     * .Audio.Media.ALBUM)); } }
     */

    public long getAlbumId() {
        synchronized (this) {
            if (mCursor == null) {
                return -1;
            }

            return mCursor.getLong(mCursor
                    .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        }
    }

    public String getTrackName() {
        synchronized (this) {
            return mPlayer.getTrackName();
        }
    }

    private boolean isPodcast() {
        synchronized (this) {
            if (mCursor == null) {
                return false;
            }

            return (mCursor.getInt(PODCASTCOLIDX) > 0);
        }
    }

    private long getBookmark() {
        synchronized (this) {
            if (mCursor == null) {
                return 0;
            }

            return mCursor.getLong(BOOKMARKCOLIDX);
        }
    }

    /**
     * Returns the duration of the file in milliseconds. Currently this method
     * returns -1 for the duration of MIDI files.
     */
    public long duration() {
        if (mPlayer.isInitialized()) {
            return mPlayer.duration();
        }

        return -1;
    }

    /**
     * Returns the current playback position in milliseconds
     */
    public long position() {
        if (mPlayer.isInitialized()) {
            return mPlayer.position();
        }

        return -1;
    }

    /**
     * Seeks to the position specified.
     *
     * @param pos The position to seek to, in milliseconds
     */
    public long seek(long pos) {
        if (mPlayer.isInitialized()) {
            if (pos < 0) {
                pos = 0;
            }

            if (pos > mPlayer.duration()) {
                pos = mPlayer.duration();
            }

            return mPlayer.seek(pos);
        }

        return -1;
    }

    /**
     * Sets the audio session ID.
     *
     * @param sessionId : the audio session ID.
     */
    public void setAudioSessionId(int sessionId) {
        synchronized (this) {
            mPlayer.setAudioSessionId(sessionId);
        }
    }

    /**
     * Returns the audio session ID.
     */
    public int getAudioSessionId() {
        synchronized (this) {
            return mPlayer.getAudioSessionId();
        }
    }

    public void getMusicInfo() {
        synchronized (this) {
            mPlayer.getMusicInfo();
        }
    }

    public String getAlbumName() {
        synchronized (this) {
            return mPlayer.getAlbumName();
        }
    }

    public String getTitleName() {
        synchronized (this) {
            return mPlayer.getTitleName();
        }
    }

    public String getArtistName() {
        synchronized (this) {
            return mPlayer.getArtistName();
        }
    }

    public void setAlbumName(String albumName) {
        synchronized (this) {
            mPlayer.setAlbumName(albumName);
        }
    }

    public void setTitleName(String titleName) {
        synchronized (this) {
            mPlayer.setTitleName(titleName);
        }
    }

    public void setArtistName(String artistName) {
        synchronized (this) {
            mPlayer.setArtistName(artistName);
        }
    }

    public void setTrackName(String titleName) {
        synchronized (this) {
            mPlayer.setTrackName(titleName);
        }
    }

    /**
     * Provides a unified interface for dealing with midi files and other media
     * files.
     */
    private class MultiPlayer {
        //private HiMediaPlayer mHiMediaPlayer = new HiMediaPlayer();
        private MediaPlayer mMediaPlayer = new MediaPlayer();
        private Handler mHandler;
        private boolean mIsInitialized = false;
        //private boolean isHiMediaPlayer = true;
        private boolean isMediaPlayer = true;
        private int CMD_GET_FILE_INFO = 28;
        private int CMD_GET_AUDIO_INFO = 32;
        private String albumName;
        private String titleName;
        private String artistName;
        private String genreName;

        public MultiPlayer() {
            // mHiMediaPlayer.setWakeMode(MediaPlaybackService.this,
            // PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(String path) {
            isMediaPlayer = true;
            Log.i(TAG, "Use mMediaPlayer!");

            try {
                mIsInitialized = false;
                mMediaPlayer.reset();

                if (path.startsWith("content://")) {
                    mMediaPlayer.setDataSource(MediaPlaybackService.this,
                            Uri.parse(path));
                    String scheme = Uri.parse(path).getScheme();
                } else {
                    mMediaPlayer.setDataSource(path);
                }

                // mHiMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.prepare();
            } catch (IOException ex) {
                //  TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            } catch (IllegalArgumentException ex) {
                // TODO: notify the user why the file couldn't be opened
                mIsInitialized = false;
                return;
            } catch (IllegalStateException ex) {
                mIsInitialized = false;
                return;
            } catch (SecurityException ex) {
                mIsInitialized = false;
                return;
            }

            mMediaPlayer.setOnCompletionListener(listener);
            mMediaPlayer.setOnErrorListener(errorListener);
            mMediaPlayer.setOnInfoListener(infoListener);
            Intent i = new Intent(
                    AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
            i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
            i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
            i.setPackage("com.android.musicfx");
            sendBroadcast(i);
            mOpenFailedList.clear();
            mIsInitialized = true;
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }

        public void start() {
            MusicUtils.debugLog(new Exception("MultiPlayer.start called"));

            /*if (isHiMediaPlayer)
            { mHiMediaPlayer.start(); }
            else if (!isHiMediaPlayer)
            { mMediaPlayer.start(); }*/
            mMediaPlayer.start();
        }

        public void stop() {
            /*if (mIsInitialized && isHiMediaPlayer)
            { mHiMediaPlayer.reset(); }
            else if (mIsInitialized && !isHiMediaPlayer)
            { mMediaPlayer.reset(); }*/

            if (mIsInitialized) {
                mMediaPlayer.reset();
            }
            mIsInitialized = false;
        }

        /**
         * You CANNOT use this player anymore after calling release()
         */
        public void release() {
            stop();

            /*if (isHiMediaPlayer)
            { mHiMediaPlayer.release(); }
            else if (!isHiMediaPlayer)
            { mMediaPlayer.release(); }*/
            mMediaPlayer.release();
        }

        public void pause() {
            /*if (isHiMediaPlayer)
            { mHiMediaPlayer.pause(); }
            else if (!isHiMediaPlayer)
            { mMediaPlayer.pause(); }*/
            mMediaPlayer.pause();
        }

        public void setHandler(Handler handler) {
            mHandler = handler;
        }

        MediaPlayer.OnCompletionListener listener = new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                // Acquire a temporary wakelock, since when we return from
                // this callback the MediaPlayer will release its wakelock
                // and allow the device to go to sleep.
                // This temporary wakelock is released when the RELEASE_WAKELOCK
                // message is processed, but just in case, put a timeout on it.
                mWakeLock.acquire(30000);
                mHandler.sendEmptyMessage(TRACK_ENDED);
                mHandler.sendEmptyMessage(RELEASE_WAKELOCK);
            }
        };

        MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch (what) {
                    case HiMediaPlayer.MEDIA_ERROR_UNKNOWN:
                        Log.d(TAG, "MEDIA_ERROR_UNKNOWN");
                        showToast(R.string.service_start_error_msg);
                        //do not add break, do we should do default to release and reset MediaPlayer

                    default:
                        mIsInitialized = false;
                        mMediaPlayer.release();
                        mIsSupposedToBePlaying = true;
                        mMediaPlayer = new MediaPlayer();
                        // mHiMediaPlayer.setWakeMode(MediaPlaybackService.this,
                        // PowerManager.PARTIAL_WAKE_LOCK);
                        mHandler.sendMessageDelayed(
                                mHandler.obtainMessage(SERVER_DIED), 1000);
                        Log.d(TAG, "Error: default what=" + what + ",extra="
                                + extra
                                + "release MediaPlayer and reset MediaPlayer");
                        break;
                }

                return false;
            }
        };

        MediaPlayer.OnInfoListener infoListener = new MediaPlayer.OnInfoListener() {
            public boolean onInfo(MediaPlayer mp, int what, int extra) {

                Log.d(TAG, "onInfo(what=" + what + ", extra=" + extra + ")");
                if (what == HiMediaPlayer.MEDIA_INFO_AUDIO_FAIL) {
                    mIsInitialized = false;
                    mMediaPlayer.release();
                    mIsSupposedToBePlaying = true;
                    mMediaPlayer = new MediaPlayer();
                    // mHiMediaPlayer.setWakeMode(MediaPlaybackService.this,
                    // PowerManager.PARTIAL_WAKE_LOCK);
                    showToast(R.string.playback_failed);
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(SERVER_DIED), 1000);
                } else if (what == HiMediaPlayer.MEDIA_INFO_NOT_SUPPORT) {
                    mIsInitialized = false;
                    mMediaPlayer.release();
                    mIsSupposedToBePlaying = true;
                    mMediaPlayer = new MediaPlayer();
                    showToast(R.string.playback_failed);
                    mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(SERVER_DIED), 1000);
                }
                return false;
            }
        };

        public long duration() {
            /*if (isHiMediaPlayer)
            { return mHiMediaPlayer.getDuration(); }
            else
            { return mMediaPlayer.getDuration(); }*/
            return mMediaPlayer.getDuration();
        }

        public long position() {
            /*if (isHiMediaPlayer)
            { return mHiMediaPlayer.getCurrentPosition(); }
            else
            { return mMediaPlayer.getCurrentPosition(); }*/
            return mMediaPlayer.getCurrentPosition();
        }

        public long seek(long whereto) {
            /*if (isHiMediaPlayer)
            { mHiMediaPlayer.seekTo((int) whereto); }
            else
            { mMediaPlayer.seekTo((int) whereto); }*/
            mMediaPlayer.seekTo((int) whereto);

            return whereto;
        }

        public void setVolume(float vol) {
            /*if (isHiMediaPlayer)
            { mHiMediaPlayer.setVolume(vol, vol); }
            else
            { mMediaPlayer.setVolume(vol, vol); }*/
            mMediaPlayer.setVolume(vol, vol);
        }

        public void setAudioSessionId(int sessionId) {
            // mHiMediaPlayer.setAudioSessionId(sessionId);
        }

        public int getAudioSessionId() {
            return 0;// mHiMediaPlayer.getAudioSessionId();
        }

        public void getMusicInfo() {
            albumName = null;
            titleName = null;
            artistName = null;
            genreName = null;

//            Metadata meta = mMediaPlayer.getMetadata(true, true);
//            if (null != meta) {

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(getPath());
                albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                titleName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                genreName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
            } catch (IllegalArgumentException e) {
                Log.d(TAG, "Retriever Error: " + e.toString());
            } catch (IllegalStateException e) {
                Log.d(TAG, "Retriever Error: " + e.toString());
            } catch (RuntimeException e) {
            } finally {
                try {
                    mmr.release();
                } catch (Exception e) {
                }
            }
//                if (meta.has(Metadata.ALBUM)) {
//                    albumName = meta.getString(Metadata.ALBUM);
//                }
//                if (meta.has(Metadata.TITLE)) {
//                    titleName = meta.getString(Metadata.TITLE);
//                }
//                if (meta.has(Metadata.ARTIST)) {
//                    artistName = meta.getString(Metadata.ARTIST);
//                }
//                if (meta.has(Metadata.GENRE)) {
//                    genreName = meta.getString(Metadata.GENRE);
//                }
//            } else {
//                Log.i(TAG, "Get Music Infomation Failed");
//            }
        }

        public String getAlbumName() {
            return albumName;
        }

        public String getTitleName() {
            return titleName;
        }

        public String getArtistName() {
            return artistName;
        }

        public String getTrackName() {
            return titleName;
        }

        public void setAlbumName(String albumName) {
            this.albumName = albumName;
        }

        public void setTitleName(String titleName) {
            this.titleName = titleName;
        }

        public void setArtistName(String artistName) {
            this.artistName = artistName;
        }

        public void setTrackName(String titleName) {
            this.titleName = titleName;
        }
    }

    private void showToast(final int resid) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), resid, Toast.LENGTH_SHORT);
        mToast.show();
    }

    /*
     * By making this a static class with a WeakReference to the Service, we
     * ensure that the Service can be GCd even when the system process still has
     * a remote reference to the stub.
     */
    static class ServiceStub extends IMediaPlaybackService.Stub {
        WeakReference<MediaPlaybackService> mService;

        ServiceStub(MediaPlaybackService service) {
            mService = new WeakReference<MediaPlaybackService>(service);
        }

        public void openFile(String path) {
            mService.get().open(path);
        }

        public void open(long[] list, int position) {
            mService.get().open(list, position);
        }

        public int getQueuePosition() {
            return mService.get().getQueuePosition();
        }

        public void setQueuePosition(int index) {
            mService.get().setQueuePosition(index);
        }

        public boolean isPlaying() {
            return mService.get().isPlaying();
        }

        public void stop() {
            mService.get().stop();
        }

        public void pause() {
            mService.get().pause();
        }

        public void play() {
            mService.get().play();
        }

        public void prev() {
            mService.get().prev();
        }

        public void next() {
            mService.get().next(true);
        }

        public String getTrackName() {
            return mService.get().getTrackName();
        }

        public String getTitleName() {
            return mService.get().getTitleName();
        }

        public String getAlbumName() {
            return mService.get().getAlbumName();
        }

        public void setTrackName(String trackName){ mService.get().setTrackName(trackName);}

        public void setTitleName(String titleName){mService.get().setTitleName(titleName);}

        public void setAlbumName(String albumName){mService.get().setAlbumName(albumName);}

        public void setArtistName(String artistName){mService.get().setArtistName(artistName);}

        public long getAlbumId() {
            return mService.get().getAlbumId();
        }

        public String getArtistName() {
            return mService.get().getArtistName();
        }

        public long getArtistId() {
            return mService.get().getArtistId();
        }

        public void enqueue(long[] list, int action) {
            mService.get().enqueue(list, action);
        }

        public long[] getQueue() {
            return mService.get().getQueue();
        }

        public void moveQueueItem(int from, int to) {
            mService.get().moveQueueItem(from, to);
        }

        public String getPath() {
            return mService.get().getPath();
        }

        public long getAudioId() {
            return mService.get().getAudioId();
        }

        public long position() {
            return mService.get().position();
        }

        public long duration() {
            return mService.get().duration();
        }

        public long seek(long pos) {
            return mService.get().seek(pos);
        }

        public void setShuffleMode(int shufflemode) {
            mService.get().setShuffleMode(shufflemode);
        }

        public int getShuffleMode() {
            return mService.get().getShuffleMode();
        }

        public int removeTracks(int first, int last) {
            return mService.get().removeTracks(first, last);
        }

        public int removeTrack(long id) {
            return mService.get().removeTrack(id);
        }

        public void setRepeatMode(int repeatmode) {
            mService.get().setRepeatMode(repeatmode);
        }

        public int getRepeatMode() {
            return mService.get().getRepeatMode();
        }

        public int getMediaMountedCount() {
            return mService.get().getMediaMountedCount();
        }

        public int getAudioSessionId() {
            return mService.get().getAudioSessionId();
        }

        public void getMusicInfo() {
            mService.get().getMusicInfo();
        }

        public void stopService() {
            mService.get().stopService();
        }
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter writer, String[] args) {
        synchronized (this) {
            writer.println("" + mPlayListLen
                    + " items in queue, currently at index " + mPlayPos);
        }
        writer.println("Currently loaded:");
        writer.println(getArtistName());
        writer.println(getAlbumName());
        writer.println(getTrackName());
        writer.println(getPath());
        writer.println("playing: " + mIsSupposedToBePlaying);

        /*if (mPlayer.isHiMediaPlayer)
        { writer.println("actual: " + mPlayer.mHiMediaPlayer.isPlaying()); }
        else
        { writer.println("actual: " + mPlayer.mMediaPlayer.isPlaying()); }*/
        writer.println("actual: " + mPlayer.mMediaPlayer.isPlaying());

        writer.println("shuffle mode: " + mShuffleMode);
        MusicUtils.debugDump(writer);
    }

    public static int getPlayMode() {
        return playMode;
    }

    public static void setPlayMode(int playMode) {
        MediaPlaybackService.playMode = playMode;
    }

    private final IBinder mBinder = new ServiceStub(this);
}