package com.hisilicon.android.music;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.security.SecureRandom;

import com.hisilicon.android.music.Common;
import com.hisilicon.android.music.FMMediaFileList;
import com.hisilicon.android.music.FilterType;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.text.TextUtils;
import android.content.ActivityNotFoundException;


/**
 * media file manager
 *
 * @author
 */
public class MediaFileListService extends Service {
    private static final String TAG = "MediaFileListService";
    private IBinder binder = new MyBinder();

    private MusicModel _currMusicModel = null;
    private int _currPosition = 0; // current Music file's position
    private boolean _haveGetPosition = false; // whether have get the position
    // that current Music
    private String _currPath = null;
    private ArrayList<MusicModel> _list = null;
    private GetMusicListThread _getMusicListThread = null;
    private boolean _runFlag = false;
    private Stack<Integer> remberLastFilePosition = new Stack<Integer>();
    private boolean hasSavedCorrentPosition = false;
    private Object lock = new Object();

    public int getCurrPosition() {
        synchronized (lock) {
            return this._currPosition;
        }
    }

    public void setCurrPosition(int vpos) {
        synchronized (lock) {
            this._currPosition = vpos;
        }
    }

    public boolean isHaveGetPosition() {
        synchronized (lock) {
            return this._haveGetPosition;
        }
    }

    public void setHaveGetPosition(boolean haveGetPosition) {
        synchronized (lock) {
            this._haveGetPosition = haveGetPosition;
        }
    }

    public String getCurrPath() {
        synchronized (lock) {
            return this._currPath;
        }
    }

    public void setCurrPath(String currPath) {
        synchronized (lock) {
            this._currPath = currPath;
        }
    }

    public ArrayList<MusicModel> getList() {
        synchronized (lock) {
            return this._list;
        }
    }

    public void setList(ArrayList<MusicModel> list) {
        synchronized (lock) {
            this._list = list;
        }
    }

    public GetMusicListThread getThread() {
        synchronized (lock) {
            return this._getMusicListThread;
        }
    }

    public void setThread(GetMusicListThread t) {
        synchronized (lock) {
            this._getMusicListThread = t;
        }
    }

    public boolean isRunFlag() {
        synchronized (lock) {
            return this._runFlag;
        }
    }

    public void setStopFlag(boolean runFlag) {
        synchronized (lock) {
            this._runFlag = runFlag;
        }
    }

    public MusicModel getCurrMusicModel() {
        synchronized (lock) {
            return this._currMusicModel;
        }
    }

    public void setCurrMusicModel(MusicModel model) {
        synchronized (lock) {
            this._currMusicModel = model;
        }
    }

    /**
     * @author
     */
    public class MyBinder extends Binder {
        public MediaFileListService getService() {
            return MediaFileListService.this;
        }
    }

    @Override
    public void onCreate() {
        FilterType.filterType(getApplicationContext());
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStart(intent, startId);
        if(intent == null){
            Log.e(TAG,"the intent is null",new Exception());
            return START_NOT_STICKY;
        }
        if (getList() != null) {
            getList().clear();
        } else {
            setList(new ArrayList<MusicModel>());
        }

        setCurrPosition(0);
        setHaveGetPosition(false);

        Uri uri = intent.getData();
        if (null == uri) {
            return START_NOT_STICKY;
        }
        String curPath = uri.getPath();
        if (null == curPath) {
            return START_NOT_STICKY;
        }
        try {
            if (!Common.isSecurePath(curPath)) {
                Log.e(TAG, "onStart curPath:Path Manipulation");
                return START_NOT_STICKY;
            }
            File isaudio = new File(curPath);
            if (!isAudioFile(isaudio)) {
                Toast.makeText(this, getResources().getString(R.string.service_start_error_msg),
                        Toast.LENGTH_LONG).show();
                return START_NOT_STICKY;
            }

            if (curPath.startsWith("/external/")) {
                Log.d(TAG, "starts with external");
                ContentResolver resolver = getContentResolver();
                Cursor cursor = null;

                cursor = resolver.query(
                        Uri.parse("content://media" + curPath),
                        new String[]{"_id", "_data", "_display_name",
                                "_size", "duration", "date_added"
                        }, null,
                        null, null);

                while (cursor != null && cursor.moveToNext()) {
                    String getCurrPath = cursor.getString(1);
                    curPath = getCurrPath;
                }

                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            if (!Common.isSecurePath(curPath)) {
                Log.e(TAG, "curPath:Path Manipulation");
                return START_NOT_STICKY;
            }
            setCurrPath(curPath);
            File f = new File(curPath);
            MusicModel currMusic = new MusicModel();
            currMusic.setTitle(f.getName());
            currMusic.setAddedTime(f.lastModified());
            currMusic.setPath(f.getPath());
            currMusic.setSize(f.length());
            setCurrMusicModel(currMusic);
            Common.setsortCount(intent.getIntExtra("sortCount", -1));
            String currPathParent = getCurrPath().substring(0,
                    getCurrPath().lastIndexOf("/"));
            if (!Common.isSecurePath(currPathParent)) {
                Log.e(TAG, "currPathParent:Path Manipulation");
                return START_NOT_STICKY;
            }
            if (TextUtils.isEmpty(currPathParent)) {
                return START_NOT_STICKY;
            }
            File file = new File(currPathParent);

            if (file.exists() && file.isDirectory()) {
                setStopFlag(true);
                waitThreadToIdle(getThread());
                setThread(new GetMusicListThread(file));
                setStopFlag(false);
                getThread().start();
                Intent i = new Intent();
                i.setClassName("com.hisilicon.android.music",
                        "com.hisilicon.android.music.MediaPlaybackActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(intent.getData(), "audio/*");
                i.putExtra("MediaFileList", new FMMediaFileList(getCurrPath()));
                MediaFileListService.this.startActivity(i);
            }
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "onstart2", e);
            this.stopSelf(startId);
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "onstart2", e);
            this.stopSelf(startId);
        }
        return START_NOT_STICKY;
    }

    /**
     * get all Music file from current folder
     *
     * @author
     */
    private class GetMusicListThread extends Thread {
        private File file = null;

        public GetMusicListThread(File file) {
            this.file = file;
        }

        public void run() {
            Common.setisLoadSuccess(false);
            File[] files = file.listFiles();
            if (null == files) {
                Log.d(TAG, " get file list is null ");
                return;
            }

            for (int i = 0; i < files.length; i++) {
                if (!isRunFlag()) {
                    if (files[i].isFile()) {
                        String filename = files[i].getName();
                        String dex = filename.substring(
                                filename.lastIndexOf(".") + 1,
                                filename.length());
                        SharedPreferences share = getSharedPreferences("AUDIO",
                                Context.MODE_PRIVATE);
                        dex = dex.toUpperCase();
                        String musicSuffix = share.getString(dex, "");

                        if (!musicSuffix.isEmpty()) {
                            MusicModel model = new MusicModel();
                            model.setPath(files[i].getPath());
                            model.setTitle(filename);
                            model.setSize(files[i].length());
                            model.setAddedTime(files[i].lastModified());
                            getList().add(model);
                        }
                    }
                } else {
                    break;
                }
            }

            ArrayList<MusicModel> _tmplist = sortFile(getList());
            setList(_tmplist);

            if (!isRunFlag()) {
                for (int i = 0; i < getList().size(); i++) {
                    if (getCurrPath().equals(getList().get(i).getPath())) {
                        setCurrPosition(i);
                        break;
                    } else {
                        setCurrPosition(0);
                    }
                }
            }
            Common.setisLoadSuccess(true);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        setStopFlag(true);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public MusicModel getMusicInfo(int flag) {
        MusicModel model = getCurrMusicModel();

        if (!((getList() == null) || (getList().size() == 0))) {
            if (flag == 1) {
                // next Music
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() >= getList().size() - 1) {
                        setCurrPosition(0);
                    } else {
                        setCurrPosition(getCurrPosition() + 1);
                    }
                }
            } else if (flag == 2) {
                // previous Music
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() <= 0) {
                        setCurrPosition(getList().size() - 1);
                    } else {
                        setCurrPosition(getCurrPosition() - 1);
                    }
                }
            } else if (flag == 3) {
                //  getNextSecureRandomMusicInfo
                SecureRandom random = new SecureRandom();
                int position = random.nextInt(getList().size());
                remberLastFilePosition.push(position);
                hasSavedCorrentPosition = true;
                setCurrPosition(position);
            } else if (flag == 4) {
                // next Music with no cycle mode
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() >= getList().size() - 1) {
                        setCurrPosition(getCurrPosition());
                    } else {
                        setCurrPosition(getCurrPosition() + 1);
                    }
                }
            } else if (flag == 5) {  ///getPreRandomMusicInfo
                if (remberLastFilePosition.isEmpty()) {
                    SecureRandom random = new SecureRandom();
                    int position = random.nextInt(getList().size());
                    setCurrPosition(position);
                } else {
                    if (hasSavedCorrentPosition) {
                        remberLastFilePosition.pop();
                        hasSavedCorrentPosition = false;
                        if (remberLastFilePosition.isEmpty()) {
                            SecureRandom random = new SecureRandom();
                            int position = random.nextInt(getList().size());
                            setCurrPosition(position);
                        } else {
                            int position = remberLastFilePosition.pop();
                            setCurrPosition(position);
                        }
                    } else {
                        int position = remberLastFilePosition.pop();
                        setCurrPosition(position);
                    }
                }
            } else if (flag == 6) {  ///updatePositionInfo
                ;//do nothing,just for set isFirstMediaFile and isLastMediaFile below
            } else if (flag == 0) {
                // previous Music with no cycle mode
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() <= 0) {
                        setCurrPosition(getCurrPosition());
                    } else {
                        setCurrPosition(getCurrPosition() - 1);
                    }
                }
            }

            if (Common.isLoadSuccess()) {
                model = getList().get(getCurrPosition());
                setCurrPath(model.getPath());
                if (getCurrPosition() == 0) {
                    Common.setisFirstMediaFile(true);
                } else {
                    Common.setisFirstMediaFile(false);
                }
                if (getCurrPosition() == getList().size() - 1) {
                    Common.setisLastMediaFile(true);
                } else {
                    Common.setisLastMediaFile(false);
                }
                //Log.d(TAG,"Common.isFirstMediaFile = "+Common.isFirstMediaFile()+"   Common.isLastMediaFile = "+Common.isLastMediaFile()); //for fority
            }
            return model;
        } else {
            return null;
        }
    }

    public static ArrayList<MusicModel> sortFile(ArrayList<MusicModel> musicList) {
        Collections.sort(musicList, new Comparator<MusicModel>() {
            public int compare(MusicModel object1, MusicModel object2) {
                return compareByName(object1, object2);
            }
        });
        return musicList;
    }

    private static int compareByName(MusicModel object1, MusicModel object2) {
        File file1 = new File(object1.getPath());
        File file2 = new File(object2.getPath());
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }
        String objectName1 = object1.getTitle();
        String objectName2 = object2.getTitle();
        int result = objectName1.compareTo(objectName2);
        if (result == 0) {
            return 0;
        } else if (result < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * check if the thread thrd is busy
     *
     * @param thrd
     * @return
     */
    private static boolean threadBusy(Thread thrd) {
        if (thrd == null) {
            return false;
        }

        if ((thrd.getState() != Thread.State.TERMINATED)
                && (thrd.getState() != Thread.State.NEW)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * before recreate a new thread, be sure the old thread is idle
     *
     * @param thrd
     */
    private static void waitThreadToIdle(Thread thrd) {
        while (threadBusy(thrd)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Log.e(TAG, "onStart", e);
            }
        }
    }

    private boolean isAudioFile(File f) {
        String fName = f.getName();
        String end = fName.substring(fName.lastIndexOf(".") + 1, fName.length()).toUpperCase();
        SharedPreferences shareAudio = getSharedPreferences("AUDIO", Context.MODE_PRIVATE);
        String strAudio = shareAudio.getString(end, "");
        if (!strAudio.isEmpty()) {
            return true;
        }

        return false;
    }

    public void umountPath(String path) {
        clearUmountPath(path);
        ArrayList<MusicModel> list = getList();
        if (list == null) {
            return;
        }
        ArrayList<MusicModel> _tmplist = sortFile(list);
        setList(_tmplist);
        initCurrMusic(path);
    }

    private void clearUmountPath(String path) {
        ArrayList<MusicModel> oldList = getList();
        ArrayList<MusicModel> newList = new ArrayList<MusicModel>();
        for (MusicModel musicModel : oldList) {
            String modelPath = musicModel.getPath();
            if (modelPath.startsWith(path)) {
                continue;
            }

            newList.add(musicModel);
        }
        if (newList.size() == 0) {
            newList = null;
        }
        setList(newList);
    }

    private void initCurrMusic(String path) {
        ArrayList<MusicModel> modelList = getList();
        if (modelList == null) {
            return;
        }
        MusicModel currModel = getCurrMusicModel();
        boolean isSetCurr = false;
        for (int i = 0; i < modelList.size(); i++) {
            MusicModel model = getCurrMusicModel();
            if (currModel.getPath().equals(model.getPath())) {
                setCurrMusicModel(model);
                setCurrPosition(i);
                isSetCurr = true;
                break;
            }
        }
        if (!isSetCurr) {
            setCurrMusicModel(modelList.get(0));
            setCurrPosition(0);
        }
    }
}
