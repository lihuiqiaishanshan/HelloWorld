package com.hisilicon.android.videoplayer.activity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.model.Common;
import com.hisilicon.android.videoplayer.model.VideoModel;
import com.hisilicon.android.videoplayer.model.listmanager.FMMediaFileList;
import com.hisilicon.android.videoplayer.utils.FilterType;
import com.hisilicon.android.videoplayer.utils.LogTool;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class MediaFileListService extends Service {
    private String TAG = "MediaFileListService";
    private IBinder binder = new MediaFileListBinder();

    private VideoModel _currVideoModel = null;
    private static int _currPosition = 0;
    private boolean _haveGetPosition = false;
    private String _currPath = null;
    private static ArrayList<VideoModel> _list = null;
    private GetVideoListThread _getVideoListThread = null;
    private boolean _runFlag = false;
    private static final int SORT_NAME = 1;
    private static final int SORT_SIZE = 2;
    private static final int SORT_MODIFY_TIME = 3;


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

    public ArrayList<VideoModel> getList() {
        synchronized (lock) {
            return this._list;
        }
    }

    public void setList(ArrayList<VideoModel> list) {
        synchronized (lock) {
            this._list = list;
        }
    }

    public GetVideoListThread getThread() {
        synchronized (lock) {
            return this._getVideoListThread;
        }
    }

    public void setThread(GetVideoListThread t) {
        synchronized (lock) {
            this._getVideoListThread = t;
        }
    }

    public void setNewThread(File file) {
        synchronized (lock) {
            setThread(new GetVideoListThread(file));
        }
    }

    public void setThreadStart() {
        synchronized (lock) {
            try {
                if (this._getVideoListThread != null) {
                    this._getVideoListThread.start();
                }
            } catch (IllegalThreadStateException ex) {
                LogTool.e(TAG, "setThreadStart error!" + ex);
            }
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

    public VideoModel getCurrVideoModel() {
        synchronized (lock) {
            return this._currVideoModel;
        }
    }

    public void setCurrVideoModel(VideoModel model) {
        synchronized (lock) {
            this._currVideoModel = model;
        }
    }

    public class MediaFileListBinder extends Binder {
        public MediaFileListService getService() {
            return MediaFileListService.this;
        }
    }

    @Override
    public void onCreate() {
        FilterType.filterType(getApplicationContext());
        super.onCreate();
//        String chipVersion = HiSysManager.getChipVersion();
        String chipVersion = getChipVersion();
        LogTool.i(TAG, "chipVersion:" + chipVersion);
        if (chipVersion.equals("Unknown chip ID")) {
            stopSelf();
        }
    }

    private String getChipVersion() {
        try {
            Class hisysManagerCls = Class.forName("com.hisilicon.android.hisysmanager.HiSysManager");
            Method method = hisysManagerCls.getDeclaredMethod("getChipVersion");
            if (method != null) {
                String ret = (String) method.invoke(hisysManagerCls);
                return ret;
            }
        } catch (ClassNotFoundException e) {
            LogTool.e(TAG, "ChipVersion error : " + e.toString());
        } catch (NoSuchMethodException e) {
            LogTool.e(TAG, "ChipVersion Error:" + e.toString());
        } catch (InvocationTargetException e) {
            LogTool.e(TAG, "ChipVersion Error:" + e.toString());
        } catch (IllegalAccessException e) {
            LogTool.e(TAG, "ChipVersion Error:" + e.toString());
        }
        return "unKnown";
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return binder;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        if (isRunningForeground()) {
            LogTool.i("TAG", "stopSelf");
            return;
        }

        Intent cmd = new Intent("com.android.music.musicservicecommand");
        cmd.putExtra("command", "stop");
        MediaFileListService.this.sendBroadcast(cmd);

        cmd = new Intent("com.hisilicon.android.music.musicservicecommand");
        cmd.putExtra("command", "stop");
        MediaFileListService.this.sendBroadcast(cmd);

        if (getList() != null) {
            getList().clear();
        } else {
            setList(new ArrayList<VideoModel>());
        }
        Common.setLoadSuccess(false);
        setCurrPosition(0);
        setHaveGetPosition(false);

        try {

            String curPath = intent.getData().getPath();

            if (curPath.startsWith("/external/")) {
                LogTool.d("starts with external", "");
                ContentResolver resolver = getContentResolver();
                Cursor cursor = null;
                try {
                    cursor = resolver.query(Uri.parse("content://media" + curPath),
                            new String[]{"_id", "_data", "_display_name", "_size", "duration",
                                    "date_added"},
                            null, null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String getCurrPath = cursor.getString(1);
                            LogTool.d("getCurrPath", "getCurrPath==" + getCurrPath);
                            curPath = getCurrPath;
                        }
                    }
                } catch (Exception e) {
                    LogTool.e(e.toString());
                } finally {
                    if (cursor != null) {
                        cursor.close();
                        cursor = null;
                    }
                }
            }

            setCurrPath(curPath);
            if (!Common.isSecurePath(curPath)) {
                LogTool.e(TAG, "curPath:Path Manipulation");
                throw new Exception("Security Issue. File Path has blacklisted characters");
            }
            File f = new File(curPath);
            VideoModel currvideo = new VideoModel();
            currvideo.setTitle(f.getName());
            currvideo.setAddedTime(f.lastModified());
            currvideo.setPath(f.getPath());
            currvideo.setSize(f.length());
            setCurrVideoModel(currvideo);
            LogTool.i(TAG, "curPath:" + curPath);
            int sortCount = intent.getIntExtra("sortCount", -1);
            Common.setSortCount(sortCount);
            String currPathParent = getCurrPath().substring(0, getCurrPath().lastIndexOf("/"));
            if (!Common.isSecurePath(currPathParent)) {
                LogTool.e(TAG, "currPathParent:Path Manipulation");
                throw new Exception("Security Issue. File Path has blacklisted characters");
            }
            File file = new File(currPathParent);
            if (file.exists() && file.isDirectory()) {
                setStopFlag(true);
                waitThreadToIdle(getThread());
                setThread(new GetVideoListThread(file));
                setStopFlag(false);
                getThread().start();
                /* start MyServiceConnection in DptHighActivity
                getThread().start();
                */
                Intent i = new Intent();
                i.setClassName("com.hisilicon.android.videoplayer", "com.hisilicon.android.videoplayer.activity.VideoActivity");
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setDataAndType(intent.getData(), "video/*");
                i.putExtra("MediaFileList", new FMMediaFileList(getCurrPath(), f.getName()));
                MediaFileListService.this.startActivity(i);

            }
        } catch (Exception e) {
            LogTool.e(TAG, "== onstart error :" + e);
            this.stopSelf(startId);
        }
    }

    private class GetVideoListThread extends Thread {
        public File file = null;
        private ArrayList<VideoModel> videoModels = null;

        public GetVideoListThread(File file) {
            this.file = file;
            videoModels = new ArrayList<VideoModel>();
        }

        public void run() {
            //because the '_list' and '_currPosition' is the static Variable
            //this must be clear before the Thread start everytime
            if (getList() != null) {
                getList().clear();
            } else {
                setList(new ArrayList<VideoModel>());
            }
            setCurrPosition(0);

            Common.setLoadSuccess(false);
            VideoModel model = null;
            File[] files = file.listFiles();
            if (files == null)
                return;
            for (int i = 0; i < files.length; i++) {
                if (!isRunFlag()) {
                    if (files[i].isFile()) {
                        String filename = files[i].getName();
                        String dex = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
                        SharedPreferences share = getSharedPreferences("VIDEO", Context.MODE_PRIVATE);
                        dex = dex.toUpperCase();
                        String videoSuffix = share.getString(dex, "");
                        if (!videoSuffix.equals("")) {
                            model = new VideoModel();
                            model.setPath(files[i].getPath());
                            model.setTitle(filename);
                            model.setSize(files[i].length());
                            model.setAddedTime(files[i].lastModified());
                            if (dex.equals("ISO"))
                                model.setMimeType("video/iso");
                            else
                                model.setMimeType("video/*");
                            LogTool.i(TAG, "Find media:" + files[i].getPath());
                            videoModels.add(model);
                        }
                    }
                } else {
                    break;
                }
            }
            setList(sortFile(videoModels));
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

            Common.setLoadSuccess(true);
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

    public VideoModel getVideoInfo(int flag) {
        VideoModel model = getCurrVideoModel();

        if (!((getList() == null) || (getList().size() == 0))) {
            if (flag == 1) {
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() >= getList().size() - 1) {
                        setCurrPosition(0);
                    } else {
                        setCurrPosition(getCurrPosition() + 1);
                    }
                }
            } else if (flag == 2) {
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() <= 0) {
                        setCurrPosition(getList().size() - 1);
                    } else {
                        setCurrPosition(getCurrPosition() - 1);
                    }
                }
            } else if (flag == 3) {
                Random random = new Random();
                setCurrPosition(random.nextInt(getList().size()));
            } else if (flag == 4) {
                if (Common.isLoadSuccess()) {
                    if (getCurrPosition() >= getList().size() - 1) {
                        setCurrPosition(0);
                        Common.setLastMediaFile(true);
                    } else {
                        setCurrPosition(getCurrPosition() + 1);
                    }
                }
            }

            if (Common.isLoadSuccess()) {
                model = getList().get(getCurrPosition());
                setCurrPath(model.getPath());
            }
            return model;
        } else {
            return null;
        }
    }

    public ArrayList<VideoModel> sortFile(ArrayList<VideoModel> videoList) {
        Collections.sort(videoList, new Comparator<VideoModel>() {
            public int compare(VideoModel object1, VideoModel object2) {
                return compareFile(object1, object2, Common.getSortCount());
            }
        });
        return videoList;
    }

    private static int compareFile(VideoModel o1, VideoModel o2, int sortMethod) {
        File object1 = new File(o1.getPath());
        File object2 = new File(o2.getPath());
        if (sortMethod == SORT_NAME) {
            return compareByName(object1, object2);
        } else if (sortMethod == SORT_SIZE) {
            int len = compareBySize(object1.length(), object2.length());
            return len;
        } else if (sortMethod == SORT_MODIFY_TIME) {
            int len = compareByDate(object1.lastModified(),
                    object2.lastModified());
            return len;
        } else {
            return 0;
        }
    }

    private static int compareByDate(long object1, long object2) {
        long diff = object1 - object2;
        if (diff > 0)
            return 1;
        else if (diff == 0)
            return 0;
        else
            return -1;
    }

    private static int compareBySize(long object1, long object2) {
        long diff = object1 - object2;
        if (diff > 0)
            return 1;
        else if (diff == 0)
            return 0;
        else
            return -1;
    }

    private static int compareByName(String object1, String object2) {
        if (object1.startsWith("d") && object2.startsWith("d"))
            return object1.split("\\|")[1].toLowerCase().compareTo(
                    object2.split("\\|")[1].toLowerCase());
        if (object1.startsWith("f") && object2.startsWith("f"))
            return object1.split("\\|")[1].toLowerCase().compareTo(
                    object2.split("\\|")[1].toLowerCase());
        else
            return 0;
    }

    private static int compareByName(File object1, File object2) {
        if (object1.isDirectory() && !object2.isDirectory()) {
            return -1;
        } else if (!object1.isDirectory() && object2.isDirectory()) {
            return 1;
        }
        String objectName1 = object1.getName();
        String objectName2 = object2.getName();
        int result = objectName1.compareTo(objectName2);
        if (result == 0) {
            return 0;
        } else if (result < 0) {
            return -1;
        } else {
            return 1;
        }
    }

    private static int compareByName(VideoModel object1, VideoModel object2) {
        if (object1 == null || object2 == null)
            return 0;

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

    private static void waitThreadToIdle(Thread thrd) {
        while (threadBusy(thrd)) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                LogTool.e(e.toString());
            }
        }
    }

    protected String tranString(String path) {
        String tranPath = "";
        for (int i = 0; i < path.length(); i++) {
            tranPath += "\\" + path.substring(i, i + 1);
        }

        return tranPath;
    }

    public boolean isRunningForeground() {
        String topActivityClassName = getTopActivityName(this);
        LogTool.i(TAG, "topActivityClassName:" + topActivityClassName);
        if (topActivityClassName != null && topActivityClassName.startsWith("com.hisilicon.android.videoplayer.activity")) {
            if (topActivityClassName.equals("com.hisilicon.android.videoplayer.activity.TransitActivity"))
                return false;
            else {
                LogTool.i(TAG, "topActivityClassName error!");
                return true;
            }
        } else
            return false;
    }

    public String getTopActivityName(Context context) {
        String topActivityClassName = null;
        ActivityManager activityManager =
                (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            topActivityClassName = f.getClassName();
        }
        return topActivityClassName;
    }
}
