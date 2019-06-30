
package com.hisilicon.launcher.logic.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Handler;
import com.hisilicon.launcher.util.LogHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.AccessOnClickInterface;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.view.setting.SystemUpdateDialog;

/**
 * local update
 *
 * @author wangchuanjian
 */
public class SystemLocalUpdateLogic extends LinearLayout {

    private final static String TAG = "SystemLocalUpdateLogic";
    private final static String MOUNT_LABLE = "mountLable";
    private final static String MOUNT_TYPE = "mountType";
    private final static String MOUNT_PATH = "mountPath";
    private final static String MOUNT_NAME = "TV_Version_Name";
    private final static String FILE_NAME = "/update.zip";
    private Context mContext;
    private SystemUpdateDialog mSystemUpdateDialog;
    private TextView title;
    private ListView list = null;
    private ArrayList<HashMap<String, Object>> listItem = null;
    private SimpleAdapter listItemAdapter = null;
    public SystemLocalUpdateLogic(Context mContext) {
        super(mContext);
        this.mContext = mContext;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View parent = inflater.inflate(R.layout.select_update_version, this);
        initView(parent, 0);
        installstorageinfo();
    }
    private void initView(View parent, int focus) {  //system_update_local
        title= (TextView) findViewById(R.id.update_title);
        title.setText(R.string.system_update_local);
        list = (ListView) findViewById(R.id.tv_version_listview);
        listItem = new ArrayList<HashMap<String, Object>>();
        list.setSelected(true);
        list.requestFocus();
        list.setOnItemClickListener(new OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            // TODO Auto-generated method stub
            HashMap<String, Object> Item = new HashMap<String, Object>();
            Item = listItem.get(arg2);
            if (isFileExist((String)Item.get(MOUNT_PATH), FILE_NAME)){
                createDialog((int) mContext.getResources().getDimension(R.dimen.dimen_350px),
                        (int) mContext.getResources().getDimension(R.dimen.dimen_400px),
                        SystemUpdateDialog.LOCAL_UPDATE_HAVE,((String)Item.get(MOUNT_PATH)));
            }else{
                createDialog((int) mContext.getResources().getDimension(R.dimen.dimen_350px),
                        (int) mContext.getResources().getDimension(R.dimen.dimen_400px),
                        SystemUpdateDialog.LOCAL_UPDATE_NONE,null);
            }
        }
        });
    }
    private void installstorageinfo(){
        listItem = getMountEquipmentList();
        listItemAdapter = new SimpleAdapter(mContext, listItem, R.layout.list_update_version_item,
                new String[]{"TV_Version_Name"},
                new int[]{R.id.tv_version_name});
        list.setAdapter(listItemAdapter);
        list.deferNotifyDataSetChanged();
        list.requestFocus();
    }
    /**
     * create a dialog
     *
     * @param height
     * @param width
     * @param save
     * @param path
     */
    public void createDialog(int height, int width, int save, String path) {
        mSystemUpdateDialog = new SystemUpdateDialog(mContext, save, path);
        mSystemUpdateDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        Window window = mSystemUpdateDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = height;
        lp.width = width;
        window.setAttributes(lp);
        mSystemUpdateDialog.show();

    }

    /**
     * To determine whether a file exists
     *
     * @param path
     * @param fileName
     * @return
     */
    public boolean isFileExist(String path, String fileName) {
        LogHelper.d(TAG, "path=" + path + "fileName" + fileName);
        File file = new File(path + fileName);
        LogHelper.d(TAG, "file=" + file);
        if (!file.exists()) {
            return false;
        }
        return true;
    }

    /**
     * get the list of mount equipment
     *
     * @return
     */
    private ArrayList<HashMap<String, Object>> getMountEquipmentList() {
        String[] mountType = mContext.getResources().getStringArray(R.array.mount_type);
        MountInfo info = new MountInfo(mContext);
        ArrayList<HashMap<String, Object>> childList = new ArrayList<HashMap<String, Object>>();
        for (int j = 0; j < mountType.length; j++) {
            for (int i = 0; i < info.index; i++) {
                if (info.type[i] == j) {
                    if (info.path[i] != null && (info.path[i].contains("/mnt") || info.path[i].contains("/storage"))) {
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put(MOUNT_TYPE, String.valueOf(info.type[i]));
                        map.put(MOUNT_PATH, info.path[i]);
                        map.put(MOUNT_LABLE, "");
                        map.put(MOUNT_NAME, info.partition[i]);
                        childList.add(map);
                    }
                }
            }
        }
        return childList;
    }

    public void dismissDialog(){
        if (null != mSystemUpdateDialog && mSystemUpdateDialog.isShowing())
            mSystemUpdateDialog.dismiss();
    }

}
