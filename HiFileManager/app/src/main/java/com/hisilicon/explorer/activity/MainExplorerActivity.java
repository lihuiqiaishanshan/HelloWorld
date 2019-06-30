package com.hisilicon.explorer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.MainExplorerExpandableAdapter;
import com.hisilicon.explorer.loader.IProgress;
import com.hisilicon.explorer.loader.LocalGroupInfoLoader;
import com.hisilicon.explorer.model.GroupInfo;
import com.hisilicon.explorer.utils.FileInfoUtils;


import java.util.ArrayList;
import java.util.List;

public class MainExplorerActivity extends BaseFileActivity implements IProgress<List<GroupInfo>> {

    private final static String TAG = MainExplorerActivity.class.getSimpleName();

    private LocalGroupInfoLoader mLocalGroupInfoLoader = new LocalGroupInfoLoader(this, this);
    List<GroupInfo> groupInfos = new ArrayList<GroupInfo>();
    private ExpandableListView elv_group_list;
    private MainExplorerExpandableAdapter mainExplorerExpandableAdapter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main_explorer;
    }

    @Override
    public void initView() {
        elv_group_list = (ExpandableListView) findViewById(R.id.elv_group);
        elv_group_list.setItemsCanFocus(true);
        mainExplorerExpandableAdapter = new MainExplorerExpandableAdapter(this, groupInfos);
        elv_group_list.setOnChildClickListener(onChildClickListener);
        elv_group_list.setAdapter(mainExplorerExpandableAdapter);
    }

    private ExpandableListView.OnChildClickListener onChildClickListener = new ExpandableListView.OnChildClickListener() {
        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            startFileListActivity(groupInfos.get(groupPosition).getRoots().get(childPosition).getPath());
            return false;
        }
    };

    private void startFileListActivity(String path) {
        Intent intent = new Intent(this, FileListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("rootpath", path);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void initData() {
        mLocalGroupInfoLoader.run();
    }

    @Override
    protected void stateChange(int state, boolean bstate, Intent intent) {
        if (USB_STATE_CHANGE == state) {
            initData();
        }
    }

    @Override
    void progressCancelByUserListener(int operate) {

    }

    @Override
    public void onLoading() {
        showProgressDialog(LOCALSDCARDLOADING);
    }

    @Override
    public void loadSuccess(List<GroupInfo> a) {
        elv_group_list.clearFocus();
        groupInfos.clear();
        groupInfos.addAll(FileInfoUtils.sortFileByPath(a));
        mainExplorerExpandableAdapter.notifyDataSetChanged();
        expandGroup();
        dismissProgressDialog();
    }

    /**
     * 获取数据后展开所有的列表
     */
    private void expandGroup() {
        for (int i = 0; i < mainExplorerExpandableAdapter.getGroupCount(); i++) {
            elv_group_list.expandGroup(i);
        }
    }

    @Override
    public void loadFail() {

    }


}
