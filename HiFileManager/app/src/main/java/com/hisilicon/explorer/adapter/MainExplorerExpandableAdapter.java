package com.hisilicon.explorer.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.GroupInfo;

import java.util.List;

/**
 */

public class MainExplorerExpandableAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<GroupInfo> mGroupInfos;

    public MainExplorerExpandableAdapter(Context mContext, List<GroupInfo> mGroupInfos) {
        this.mContext = mContext;
        this.mGroupInfos = mGroupInfos;
    }

    @Override
    public int getGroupCount() {
        return mGroupInfos.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroupInfos.get(groupPosition).getRoots().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupInfos.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroupInfos.get(groupPosition).getRoots().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main_explorer_expand_group, null);
            groupHolder = new GroupHolder();
            groupHolder.tv_group_title = (TextView)convertView.findViewById(R.id.tv_group_title);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder)convertView.getTag();
        }

        groupHolder.tv_group_title.setText(mGroupInfos.get(groupPosition).getType());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder itemHolder = null;
        if (convertView == null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_main_explorer_expand_child, null);
            itemHolder = new ItemHolder();
            itemHolder.tv_child_name = (TextView) convertView.findViewById(R.id.tv_child_name);
            convertView.setTag(itemHolder);
        } else {
            itemHolder = (ItemHolder)convertView.getTag();
        }
        String path = mGroupInfos.get(groupPosition).getRoots().get(childPosition).getPath();
        String label = mGroupInfos.get(groupPosition).getRoots().get(childPosition).getmLabel();
        if (TextUtils.isEmpty(label)) {
            itemHolder.tv_child_name.setText(path.split("/")[2]);
        } else {
            itemHolder.tv_child_name.setText(label);
        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
    class GroupHolder
    {
        public TextView tv_group_title;
    }

    class ItemHolder
    {
        public TextView tv_child_name;
    }
}
