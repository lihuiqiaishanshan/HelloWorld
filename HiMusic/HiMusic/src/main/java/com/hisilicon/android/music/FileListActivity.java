package com.hisilicon.android.music;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.ActivityNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class FileListActivity extends ListActivity {

    private MediaFileList mediaFileList;
    private ListView mPlayListView;
    public ArrayList<MusicModel> currList;
    public SimpleAdapter listItemAdapter;
    ArrayList<HashMap<String, Object>> listItem;
    private static MediaFileListService mediaFileListService = null;
    public MyServiceConnection conn = null;
    private String currPlayPath;
    private boolean isPlayNext = false;
    public static final String ACTION_UPDATE_FILELIST = "ACTION_UPDATE_FILELIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_picker_activity);
        Intent intent = getIntent();
        mediaFileList = intent.getParcelableExtra("MediaFileList");
        currPlayPath = intent.getStringExtra("path");

        if (mediaFileList != null && mediaFileList.getId() == 1) {
            Intent service = new Intent(Constants.ACTION);
            service.setClassName("com.hisilicon.android.music",
                            "com.hisilicon.android.music.MediaFileListService");
            conn = new MyServiceConnection();
            FileListActivity.this.bindService(service, conn,
                                              Context.BIND_AUTO_CREATE);
        }

        mPlayListView = getListView();
        mPlayListView.setSelector(getResources().getDrawable(
                                      R.drawable.mselector));
        listItem = new ArrayList<HashMap<String, Object>>();
        listItemAdapter = new SimpleAdapter(this, listItem,
                R.layout.list_items, new String[] { "ItemImage", "ItemTitle",
                        "ItemText" }, new int[] { R.id.ItemImage,
                        R.id.ItemTitle, R.id.ItemText });
        mPlayListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
            int position, long id) {
                // TODO Auto-generated method stubif(mService == null)
                String filename = currList.get(position).getPath();
                if (null == filename) {
                    return;
                }

                try {
                    mediaFileListService.setCurrPosition(position);//set the music positon to FileList
                    Intent intent = new Intent();
                    Uri path = Uri.parse(filename);
                    intent.setData(path);
                    intent.setClassName("com.hisilicon.android.music",
                                        "com.hisilicon.android.music.MediaPlaybackActivity");
                    intent.setDataAndType(intent.getData(), "audio/*");
                    intent.putExtra("path", filename);
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    Log.e("MediaPlaybackActivity", "couldn't start: ", ex);
                }

                finish();
            }
        });
        mPlayListView.setAdapter(listItemAdapter);
        mPlayListView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

                if (null != mediaFileListService
                && null != mediaFileListService.getList()) {
                    if (mediaFileListService.getList().size() > currList.size()) {
                        updatePlayList();
                    }
                }
            }
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
            int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
            }
        });
        IntentFilter ListFilter = new IntentFilter();
        ListFilter.addAction(FileListActivity.ACTION_UPDATE_FILELIST);
        registerReceiver(mFileListReceiver, ListFilter,"com.hisilicon.android.music.ACTION_HISI_MUSIC",null);
    }

    public void updatePlayList(String path) {
        for (int i = 0; i < currList.size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();

            if (path != null && path.equals(currList.get(i).getPath())) {
                // jly
                map.put("ItemImage",
                        R.drawable.hisil_indicator_ic_mp_playing_large);
                getListView().setSelection(i);
                Message msg = new Message();
                msg.what = 1;
                msg.arg1 = i;
                mHandler.sendMessage(msg);
            } else {
                map.put("ItemImage", R.drawable.hisil_ic_tab_albums_unselected);
            }

            map.put("ItemTitle", currList.get(i).getTitle());
            map.put("ItemText", currList.get(i).getPath());
            if (isPlayNext) {
                listItem.set(i, map);
            } else {
                listItem.add(map);
            }
        }

        ((SimpleAdapter) getListView().getAdapter()).notifyDataSetChanged();
    }

    public void updatePlayList() {
        for (int i = 0; i < mediaFileListService.getList().size(); i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemTitle", currList.get(i).getTitle());
            map.put("ItemText", currList.get(i).getPath());
            listItem.add(map);
        }

        ((SimpleAdapter) getListView().getAdapter()).notifyDataSetChanged();
    }

    public class MyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder service) {
            mediaFileListService = ((MediaFileListService.MyBinder) service)
                                   .getService();
            currList = mediaFileListService.getList();
            updatePlayList(currPlayPath);
        }

        public void onServiceDisconnected(ComponentName arg0) {
        }
    }

    @Override
    protected void onDestroy() {
        if (conn != null) {
            unbindService(conn);
            Intent service = new Intent(Constants.ACTION);
            service.setClassName("com.hisilicon.android.music",
                            "com.hisilicon.android.music.MediaFileListService");
            stopService(service);
        }
        Common.setisFileList(false);
        isPlayNext = false;
        if (null != mFileListReceiver)
        {
            this.unregisterReceiver(mFileListReceiver);
            mFileListReceiver = null;
        }
        super.onDestroy();

    }

    static class MHandler extends Handler {
        WeakReference<FileListActivity> outerClass;

        MHandler(FileListActivity activity) {
            outerClass = new WeakReference<FileListActivity>(activity);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            FileListActivity theClass = outerClass.get();
            switch (msg.what) {
            case 1:
                int i = msg.arg1;
                theClass.getListView().setSelection(i);
                theClass.getListView().invalidateViews();
                break;
            default: {
                Log.w("MediaPlaybackActivity", "Unknown Handler Message:" + msg.what);
            }
            }

        }
    }
    private final Handler mHandler = new MHandler(this);
/*for lint {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    int i = msg.arg1;
                    getListView().setSelection(i);
                    getListView().invalidateViews();
            }
        };
    };*/

    private BroadcastReceiver mFileListReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
                 String action = intent.getAction();
                 currPlayPath = intent.getStringExtra("filepath");
                 if (FileListActivity.ACTION_UPDATE_FILELIST.equals(action)){
                          isPlayNext = true;
                          updatePlayList(currPlayPath);
                 }
         }
    };
}
