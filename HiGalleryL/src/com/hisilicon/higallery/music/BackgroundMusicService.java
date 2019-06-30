package com.hisilicon.higallery.music;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;

import android.util.Log;

import java.util.List;
import java.io.IOException;

import java.lang.IllegalStateException;
import java.lang.IllegalArgumentException;
import java.lang.ArrayIndexOutOfBoundsException;

public class BackgroundMusicService extends Service {
    private static final String TAG = "BackgroundMusicService";

    private MediaPlayer mediaPlayer;
    private String musicUrl = "";
    private List<MusicModel> musicLists = null;
    private static int playMusicIndex = 0;
    private static final String ACTION_MUSIC_LIST = "com.hisilicon.higallery.music.background_music_list";
    private static final String PERMISSION_BACKGROUND_MUSIC = "com.hisilicon.higallery.permission.BACKGROUNDMUSIC";
    private static final int PLAY_MUSIC = 12;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG,"onStart");
        //service died may null
        if (null == intent) {
            return;
        }

        registerMusicListReceiver();

        Intent i = new Intent();
        i.setClassName(getPackageName(),getPackageName()+".music.MusicListService");
        i.setData(intent.getData());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(i);


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.setOnErrorListener(errorListener);
        mediaPlayer.setOnInfoListener(infoListener);
        mediaPlayer.setOnPreparedListener(null);

        super.onStart(intent,startId);
    }

    private BroadcastReceiver musicListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null == intent)
                return;
            if (ACTION_MUSIC_LIST.equals(intent.getAction()) && (null != intent)) {
                try {
                    Bundle bundle = intent.getExtras();
                    if(bundle != null){
                        musicLists = bundle.getParcelableArrayList("music_list");
                    }
                    handler.sendEmptyMessage(PLAY_MUSIC);
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e(TAG,"run in ArrayIndexOutOfBoundsException");
                }
            }
            return;
        }
    };

    private void registerMusicListReceiver() {
        IntentFilter intentFilter = new IntentFilter(ACTION_MUSIC_LIST);
        registerReceiver(musicListReceiver,intentFilter, PERMISSION_BACKGROUND_MUSIC, null);
    }

    private void unRegisterMusicListReceiver() {
        unregisterReceiver(musicListReceiver);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PLAY_MUSIC) {
                if (musicLists.size() > 0) {
                    musicUrl = musicLists.get(playMusicIndex).getPath();
                    try {
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(musicUrl);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IllegalStateException e) {
                        Log.e(TAG,"mediaPlayer",e);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG,"mediaPlayer.setDataSource",e);
                    } catch (IOException e) {
                        Log.e(TAG,"mediaPlayer.prepare",e);
                    }
                }
            }
        }

    };

    OnCompletionListener completionListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
            //play the next
            playMusicIndex++;
            if (playMusicIndex >= musicLists.size()) {
                playMusicIndex = 0;
            }
            handler.sendEmptyMessage(PLAY_MUSIC);
        }
    };

    OnErrorListener errorListener = new OnErrorListener() {
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.d(TAG,"onError : what = " + what);
            switch (what) {
                case MediaPlayer.MEDIA_ERROR_UNKNOWN: {
                    break;
                }
                case MediaPlayer.MEDIA_ERROR_SERVER_DIED: {
                    break;
                }
                default: {
                    mediaPlayer.release();
                    mediaPlayer = null;
                    break;
                }
            }
            return false;
        }
    };

    OnInfoListener infoListener = new OnInfoListener() {
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG,"onInfo : what = " + what);
            switch (what) {
                default :
                    break;
            }
            return false;
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        if (null != musicListReceiver) {
            unRegisterMusicListReceiver();
            musicListReceiver = null;
        }
        if (null != mediaPlayer) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}
