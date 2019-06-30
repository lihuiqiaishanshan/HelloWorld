package com.hisilicon.explorer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.MenuParentAdapter;
import com.hisilicon.explorer.model.MenuInfo;

import java.util.List;

/**
 * Menu主窗口
 */

public class MenuParentWindow extends PopupWindow {

    private Context mContext;
    private List<MenuInfo> list;
    private int aniTabMenu;
    private GridView gv_menu_parent;
    private MenuInfo.IMenuClick iMenuClick;

    public MenuParentWindow(Context mContext, List<MenuInfo> list, int aniTabMenu) {
        super(mContext);
        this.mContext = mContext;
        this.list = list;
        this.aniTabMenu = aniTabMenu;
        initView();
    }

    public void setIMenuClick(MenuInfo.IMenuClick iMenuClick) {
        this.iMenuClick = iMenuClick;
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.menu_parent_layout, null);
        this.setContentView(inflate);
        gv_menu_parent = (GridView)inflate.findViewById(R.id.gv_menu_parent);
        MenuParentAdapter menuParentAdapter = new MenuParentAdapter(mContext, list);
        gv_menu_parent.setAdapter(menuParentAdapter);
        gv_menu_parent.setOnItemClickListener(onItemClickListener);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.hot_text_bg));
        this.setAnimationStyle(aniTabMenu);
        this.setFocusable(true);
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MenuInfo menuInfo = list.get(position);
            if (null != iMenuClick) {
                iMenuClick.menuClick(menuInfo.getmMenuFuctionType(),menuInfo);
            }
        }
    };

    public void selectFirstPosition() {
        gv_menu_parent.setSelection(0);
    }

}
