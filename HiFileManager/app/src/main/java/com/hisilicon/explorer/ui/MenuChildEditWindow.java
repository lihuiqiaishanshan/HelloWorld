package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.utils.ToastUtil;

/**
 */

public class MenuChildEditWindow {

    private Context mContext;
    private MenuInfo menuInfo;
    private TextView tv_menu_title;
    private View btn_ok;
    private View btn_cancel;
    private MenuInfo.IFileOperate iFileOperate;
    private EditText et_menu;
    private AlertDialog alertDialog;

    public MenuChildEditWindow(Context context, MenuInfo menuInfo) {
        this.mContext = context;
        this.menuInfo = menuInfo;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.menu_edit_child_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);

        tv_menu_title = (TextView) inflate.findViewById(R.id.tv_menu_child_title);
        tv_menu_title.setText(mContext.getResources().getString(menuInfo.getResTitle()));
        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);
        et_menu = (EditText) inflate.findViewById(R.id.et_menu_child);
    }

    public void show() {
        alertDialog.show();
    }

    public void setOperationListener(MenuInfo.IFileOperate iFileOperate) {
        this.iFileOperate = iFileOperate;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    Editable text = et_menu.getText();
                    if (TextUtils.isEmpty(text)) {
                        ToastUtil.showMessage(mContext, mContext.getResources().getString(R.string.name_empty));
                        return;
                    }
                    if (text.length() >= 128) {
                        ToastUtil.showMessage(mContext, mContext.getResources().getString(R.string.name_long));
                        return;
                    }
                    if (null != iFileOperate) {
                        switch (menuInfo.getmMenuFuctionType()) {
                            case MenuInfo.MenuFuctionType.NEW_FOLDER:
                                iFileOperate.menuNewFolder(text.toString());
                                break;
                            case MenuInfo.MenuFuctionType.SEARCH:
                                iFileOperate.menuSearchFile(text.toString());
                                break;
                        }
                    }
                    alertDialog.dismiss();
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

}
