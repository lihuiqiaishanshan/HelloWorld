package com.hisilicon.higallery.music;

import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;

import android.os.Binder;
import android.os.IBinder;
//import android.os.SystemProperties;
import android.os.Handler;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.lang.Thread;
import java.lang.SecurityException;
import java.lang.IndexOutOfBoundsException;

public class MusicListService extends Service {
    private static final String TAG = "MusicListService";
    private String curMusicParentPath = "";
    private List<MusicModel> musicList = null;
    private Object lock = new Object();
    private static final String ACTION_MUSIC_LIST = "com.hisilicon.higallery.music.background_music_list";
    private static final String PERMISSION_BACKGROUND_MUSIC = "com.hisilicon.higallery.permission.BACKGROUNDMUSIC";
    private static final int FINISH_SEARCH_MUSIC_LIST = 11;

    public List<MusicModel> getMusicList() {
        synchronized (lock) {
            return musicList;
        }
    }

    public void setMusicList(ArrayList<MusicModel> musicList) {
        synchronized (lock) {
            this.musicList = musicList;
        }
    }

    @Override
    public void onCreate() {
        FilterType.filterTypeMusic(getApplicationContext());
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public static boolean isSecurePath(String filePath) { //for fority:Path Manipulation
        String blackListChars = "..";
        return (filePath.indexOf(blackListChars)< 0);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG,"onStart");
        if ((intent == null) || (intent.getData() == null)) {
            return;
        }
        super.onStart(intent, startId);

        if (getMusicList() != null) {
            getMusicList().clear();
        } else {
            setMusicList(new ArrayList<MusicModel>());
        }

        try {
            String musicPath = intent.getData().getPath();
            if (null == musicPath || musicPath.isEmpty() || !isSecurePath(musicPath))
                return;

            File musicFile = new File(musicPath);

            if (musicFile.exists() && musicFile.isFile()) {
                curMusicParentPath = musicPath.substring(0,musicPath.lastIndexOf("/"));
            } else if (musicFile.isDirectory()) {
                curMusicParentPath = musicPath;
            }

            File musicParentFile = new File(curMusicParentPath);
            if (musicParentFile.exists() && musicParentFile.isDirectory()) {
                GetMusicListThread mThread = new GetMusicListThread(musicParentFile);
                mThread.start();
            }
        } catch (SecurityException e) {
            Log.e(TAG,"musicFile : ",e);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG,"substring : ",e);
        }
    }

    private class GetMusicListThread extends Thread {
        private File file = null;

        public GetMusicListThread(File file) {
            this.file = file;
        }

        @Override
        public void run() {
            Log.d(TAG, "GetMusicListThread.run");
            MusicModel musicModel = null;
            int musicIdIndex = 0;
            File[] files = file.listFiles();

            for (File f : files) {
                if (f.isFile()) {
                    String filename = f.getName();
                    String filedex = filename.substring(filename.lastIndexOf(".")+1,filename.length());
                    filedex = filedex.toUpperCase();

                    SharedPreferences musicShare = getSharedPreferences("AUDIO",Context.MODE_PRIVATE);
                    String musicSuffix = musicShare.getString(filedex,"");

                    if (!musicSuffix.equals("")) {
                        musicModel = new MusicModel();
                        musicModel.setPath(f.getPath());
                        musicModel.setTitle(filename);
                        musicModel.setSize(f.length());
                        musicModel.setAddedTime(f.lastModified());
                        musicModel.setId(musicIdIndex);
                        getMusicList().add(musicModel);
                        musicIdIndex++;
                    }
                }
            }
            handler.sendEmptyMessage(FINISH_SEARCH_MUSIC_LIST);
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case FINISH_SEARCH_MUSIC_LIST:
                    Intent intent = new Intent();
                    intent.setAction(ACTION_MUSIC_LIST);
                    intent.setPackage(getPackageName());
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList("music_list",(ArrayList<? extends Parcelable>)getMusicList());
                    intent.putExtras(bundle);
                    sendBroadcast(intent, PERMISSION_BACKGROUND_MUSIC);
                    stopSelf();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
