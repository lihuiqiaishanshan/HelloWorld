package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.explorer.Config;
import com.hisilicon.explorer.R;
import com.hisilicon.explorer.adapter.MenuListAdapter;
import com.hisilicon.explorer.model.MenuInfo;

import static android.widget.AbsListView.CHOICE_MODE_SINGLE;

/**
 * 文件分类排序等窗口
 */

public class MenuChildCheckedWindow {
    private Context mContext;
    private MenuInfo menuInfo;
    private ListView lv_menu_list;

    private MenuInfo.IFileOperate iFileOperate;
    private AlertDialog alertDialog;
    private MenuListAdapter menuListAdapter;

    public MenuChildCheckedWindow(Context context, MenuInfo menuInfo) {
        this.mContext = context;
        this.menuInfo = menuInfo;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.menu_list_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);
        lv_menu_list = (ListView) inflate.findViewById(R.id.lv_menu_list);
        menuListAdapter = new MenuListAdapter(mContext, menuInfo);
        lv_menu_list.setAdapter(menuListAdapter);
        lv_menu_list.setOnItemClickListener(onItemClickListener);
        lv_menu_list.setChoiceMode(CHOICE_MODE_SINGLE);
        setDefaultItemSelected();
    }

    private void setDefaultItemSelected() {
        switch (menuInfo.getmMenuFuctionType()) {
            case MenuInfo.MenuFuctionType.FILE_FILTER:
                lv_menu_list.setItemChecked(Config.getInstance().getFileFilterType(),true);
                lv_menu_list.setSelection(Config.getInstance().getFileFilterType());
                break;
            case MenuInfo.MenuFuctionType.FILE_SORT:
                lv_menu_list.setItemChecked(Config.getInstance().getFileSortType(),true);
                lv_menu_list.setSelection(Config.getInstance().getFileSortType());
                break;
            case MenuInfo.MenuFuctionType.SHOW_STYLE:
                lv_menu_list.setItemChecked(Config.getInstance().getFileShowType(),true);
                lv_menu_list.setSelection(Config.getInstance().getFileShowType());
                break;
        }
    }

    public void show() {
        alertDialog.show();
    }

    public void setOperationListener(MenuInfo.IFileOperate iFileOperate) {
        this.iFileOperate = iFileOperate;
    }

    private void dismiss() {
        alertDialog.dismiss();
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (menuInfo.getmMenuFuctionType()) {
                case MenuInfo.MenuFuctionType.FILE_FILTER:
                    if (null != iFileOperate) {
                        iFileOperate.menuFilterFile(position);
                    }
                    dismiss();
                    break;
                case MenuInfo.MenuFuctionType.FILE_SORT:
                    if (null != iFileOperate) {
                        iFileOperate.menuSortFile(position);
                    }
                    dismiss();
                    break;
                case MenuInfo.MenuFuctionType.SHOW_STYLE:
                    if (null != iFileOperate) {
                        iFileOperate.menuShowStyle(position);
                    }
                    dismiss();
                    break;
                case MenuInfo.MenuFuctionType.OPTIONS:
                    if (null != iFileOperate) {
                        iFileOperate.menuOperationFile(position);
                    }
                    dismiss();
                    break;
            }
        }
    };

}
