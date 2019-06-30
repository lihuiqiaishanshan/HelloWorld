package com.hisilicon.android.videoplayer.view;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;

import com.hisilicon.android.videoplayer.R;
import com.hisilicon.android.videoplayer.model.VideoModel;
import com.hisilicon.android.videoplayer.utils.LogTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;


public class DptOMultipleListDialog extends Dialog {
    private String TAG = "HiVideoPlayer_" + MultipleClickListener.class.getSimpleName();
    private MultipleClickListener multipleClickListener;
    private ArrayList<VideoModel> list;
    private ListView mPlayListView;
    public SimpleAdapter listItemAdapter;
    private Activity activity;
    ArrayList<HashMap<String, Object>> listItem;
    private final int GET_DATA = 1;
    private final int REFRESH = 2;
    private ProgressDialog progressDialog;
    private boolean isShow = false;

    private boolean isShowDouble = false;
    /**
     * 实现方式
     */
    private int mutilPlayStyle = 0;
    /**
     * 通过TextureView实现
     */
    public static final int STYLE_TEXTUREVIEW = 0;
    /**
     * 通过SurfaceView实现
     */
    public static final int STYLE_SURFACEVIEW = 1;
    private RadioGroup radioGroup;

    public DptOMultipleListDialog(Activity activity, ArrayList list, boolean isShow, MultipleClickListener multipleClickListener) {
        super(activity, R.style.mutipledialog);
        this.activity = activity;
        this.list = list;
        this.isShow = isShow;
        this.multipleClickListener = multipleClickListener;
    }

    public DptOMultipleListDialog(Activity activity, ArrayList list, boolean isShow, MultipleClickListener multipleClickListener, boolean showDouble) {
        super(activity, R.style.mutipledialog);
        this.activity = activity;
        this.list = list;
        this.isShow = isShow;
        this.multipleClickListener = multipleClickListener;
        setShowDouble(showDouble);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mutiple_list_dpt_o);
        initView();
        initData();
    }

    private void initView() {
        Window window = getWindow();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams p = window.getAttributes();
        p.height = (int) (dm.heightPixels * 0.6);
        p.width = (int) (dm.widthPixels * 0.4);
        window.setAttributes(p);
        mPlayListView = (ListView) findViewById(R.id.videoList);
        radioGroup = (RadioGroup) findViewById(R.id.rg_multiplay);
        radioGroup.setOnCheckedChangeListener(checkedChangeListener);
        radioGroup.check(R.id.rb_surface);
        radioGroup.setVisibility(isShowDouble ? View.VISIBLE : View.GONE);
    }

    private RadioGroup.OnCheckedChangeListener checkedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rb_surface) {
                mutilPlayStyle = STYLE_SURFACEVIEW;
            } else if (checkedId == R.id.rb_texture) {
                mutilPlayStyle = STYLE_TEXTUREVIEW;
            }
            LogTool.d(TAG, "VideoClick  mutilPlayStyle ： " + mutilPlayStyle);
        }
    };

    private void initData() {
        listItem = new ArrayList<HashMap<String, Object>>();
        listItemAdapter = new SimpleAdapter(activity, listItem,
                R.layout.list_items, new String[]{"ItemImage",
                "ItemTitle", "ItemText"}, new int[]{R.id.ItemImage,
                R.id.ItemTitle, R.id.ItemText});
        mPlayListView.setAdapter(listItemAdapter);
        updateSubList();
        mPlayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if ((listItem != null) && listItem.get(position) != null) {
                    HashMap<String, Object> tmpItem = listItem.get(position);
                    if (null == tmpItem) {
                        return;
                    }
                    Object tmpObject = tmpItem.get("ItemText");
                    if (null == tmpObject) {
                        return;
                    }
                    String path = tmpObject.toString();
                    multipleClickListener.VideoClick(path, isShowDouble ? mutilPlayStyle : STYLE_SURFACEVIEW);
                }
            }
        });
    }

    public void updateSubList() {
        listItem.clear();
        listItemAdapter.notifyDataSetChanged();
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity);
        }
        progressDialog.show();
        DataThread dataThread = new DataThread();
        dataThread.start();
    }

    private class MyComparator implements Comparator<HashMap<String, Object>> {

        @Override
        public int compare(HashMap<String, Object> o1,
                           HashMap<String, Object> o2) {
            int ret = -1;
            if ((null == o1) || (null == o2)) {
                return ret;
            }
            Object image1 = o1.get("ItemImage");
            Object image2 = o2.get("ItemImage");
            Object oname1 = o1.get("ItemTitle");
            Object oname2 = o2.get("ItemTitle");

            if ((null == image1) || (null == image2) || (null == oname1) || (null == oname2)) {
                return ret;
            }
            int id1 = (Integer) image1;
            int id2 = (Integer) image2;
            String name1 = ((String) oname1).toUpperCase(Locale.getDefault());
            String name2 = ((String) oname2).toUpperCase(Locale.getDefault());
            if (id1 == id2) {
                ret = name1.compareTo(name2);
            } else {
                if (id1 != R.drawable.folder_file) {
                    ret = 1;
                }
            }
            return ret;
        }
    }

    private Handler mDataHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_DATA:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    listItemAdapter.notifyDataSetChanged();
                    mPlayListView.setSelection(0);
                    mPlayListView.requestFocus();
                    break;
                case REFRESH:
                    listItemAdapter.notifyDataSetChanged();
                    break;
            }

        }
    };

    class DataThread extends Thread implements Runnable {
        @Override
        public void run() {
            if (list == null || list.size() == 0) {
                return;
            }
            ArrayList<HashMap<String, Object>> tempList = new ArrayList<HashMap<String, Object>>();
            for (int i = 0; i < list.size(); i++) {
                VideoModel videoModel = list.get(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemImage", R.drawable.iconh);
                //map.put("ItemImage", R.drawable.hisil_ic_tab_albums_unselected);
                map.put("ItemTitle", videoModel.getTitle());
                map.put("ItemText", videoModel.getPath());
                tempList.add(map);
            }
            Collections.sort(tempList, new MyComparator());
            listItem.clear();
            listItem.addAll(tempList);
            mDataHandler.sendEmptyMessage(GET_DATA);
        }
    }

    public interface MultipleClickListener {
        void VideoClick(String path, int style);
    }

    public boolean isShowDouble() {
        return isShowDouble;
    }

    private void setShowDouble(boolean showDouble) {
        isShowDouble = showDouble;
        if (!showDouble) {
            if (radioGroup != null) {
                radioGroup.setVisibility(View.GONE);
            }
        }
    }
}
