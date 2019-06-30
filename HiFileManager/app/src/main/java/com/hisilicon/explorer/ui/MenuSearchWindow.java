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

import com.hisilicon.explorer.R;

/**
 * not use
 */

public class MenuSearchWindow {
    private Context mContext;
    private TextView tv_title;
    private View btn_ok;
    private View btn_cancel;
    private IFileOperateSearch iFileOperateSearch;
    private AlertDialog alertDialog;
    private EditText et_searchname;

    public MenuSearchWindow(Context context) {
        this.mContext = context;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.file_operate_search_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);
        tv_title = (TextView) inflate.findViewById(R.id.tv_title);
        tv_title.setText(mContext.getResources().getString(R.string.search));
        et_searchname = (EditText) inflate.findViewById(R.id.et_searchname);
        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);
    }

    public void show() {
        alertDialog.show();
    }

    public void setSearchOperationListener(IFileOperateSearch iFileOperateSearch) {
        this.iFileOperateSearch = iFileOperateSearch;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    Editable newNameE = et_searchname.getText();
                    if (TextUtils.isEmpty(newNameE)) {
                        //TODO 提示名字不能为空
                        alertDialog.dismiss();
                        return;
                    }
                    if (null != iFileOperateSearch) {
                        iFileOperateSearch.fileOperateSearch(newNameE.toString());
                    }
                    alertDialog.dismiss();
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    public interface IFileOperateSearch {
        void fileOperateSearch(String newName);
    }
}
