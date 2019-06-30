package com.hisilicon.android.music;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ImageListAcvitity extends ListActivity {

    private static final String TAG = "ImageListAcvitity";
    private ListView mPlayListView;
    private TextView mTitleView;
    public SimpleAdapter listItemAdapter;
    private String[] imageType = { "BMP", "JPG", "DNG", "JFIF", "JPEG", "PNG",
            "WBMP", "GIF", "TIF", "JPE", "TIFF" };
    ArrayList<HashMap<String, Object>> listItem;

    private String currPlayPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.sub_list);

        Window window = getWindow();
        if (null == window)
        {
            return;
        }
        window.setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        LayoutParams p = window.getAttributes();
        p.height = (int) (dm.heightPixels * 0.8);
        p.width = (int) (dm.widthPixels * 0.7);
        window.setAttributes(p);

        Intent intent = getIntent();

        currPlayPath = intent.getStringExtra("path");

        mPlayListView = (ListView) findViewById(android.R.id.list);
        mTitleView = (TextView) findViewById(R.id.left_text);

        listItem = new ArrayList<HashMap<String, Object>>();
        listItemAdapter = new SimpleAdapter(this, listItem,
                R.layout.list_items_image, new String[] { "ItemImage",
                        "ItemTitle", "ItemText" }, new int[] { R.id.ItemImage,
                        R.id.ItemTitle, R.id.ItemText });

        this.setListAdapter(listItemAdapter);
        updateSubList(currPlayPath);
        mPlayListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                if ((listItem != null) && listItem.get(position) != null) {
                     HashMap<String, Object> tmpItem = listItem.get(position);
                     if (null == tmpItem) {
                         return;
                     }
                     Object tmpObject = tmpItem.get("ItemText");
                     if (null == tmpObject){
                         return;
                     }
                    String path = tmpObject.toString();

                    File f = new File(path);
                    if (f.isFile()) {
                        if (isImageFile(f)) {
                            Intent backIntent = new Intent(
                                    ImageListAcvitity.this,
                                    MediaPlaybackActivity.class);
                            backIntent.putExtra("path", path);
                            backIntent.putExtra("fromMusic", true);
                            backIntent.setData(Uri.fromFile(f));
                            setResult(RESULT_OK, backIntent);
                            finish();
                        } else {
                            Toast.makeText(ImageListAcvitity.this,
                                    "Please Choose Image File !",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else if (f.isDirectory()) {
                        updateSubList(path);
                    }
                }
            }
        });
    }

    private boolean isImageFile(File f) {
        String name = f.getName();
        String type = name.substring(name.lastIndexOf(".") + 1);
        for (String str : imageType) {
            if (str.equals(type.toUpperCase(Locale.getDefault()))) {
                return true;
            }
        }
        return false;
    }

    public void updateSubList(String path) {
        if(null == path)
            return;

        listItem.clear();
        mTitleView.setText(path);

        if(!Common.isSecurePath(path)){
            Log.e(TAG,"updateSubList:Path Manipulation");
            return;
        }
        File f = new File(path);
        HashMap<String, Object> temp_map = new HashMap<String, Object>();
        temp_map.put("ItemImage", R.drawable.folder_file);
        temp_map.put("ItemTitle", "..");
        if (path.equals("/mnt"))
            temp_map.put("ItemText", "/mnt");
        else
            temp_map.put("ItemText", f.getParentFile());
        listItem.add(temp_map);

        File[] files = f.listFiles();
        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile() && isImageFile(files[i].getAbsoluteFile())) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemImage", R.drawable.imgfile);
                map.put("ItemTitle", files[i].getName());
                map.put("ItemText", files[i].getPath());
                if (files[i].canRead())
                    listItem.add(map);
            } else if (files[i].isDirectory()) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("ItemImage", R.drawable.folder_file);
                map.put("ItemTitle", files[i].getName());
                map.put("ItemText", files[i].getPath());
                if (files[i].canRead())
                    listItem.add(map);
            }
        }
        Collections.sort(listItem, new MyComparator());
        ((SimpleAdapter) getListView().getAdapter()).notifyDataSetChanged();
        mPlayListView.setSelection(0);
        mPlayListView.requestFocus();
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
}
