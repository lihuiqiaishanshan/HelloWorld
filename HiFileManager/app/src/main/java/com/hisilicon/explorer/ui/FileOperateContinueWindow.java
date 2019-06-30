package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hisilicon.explorer.R;

/**
 * 文件操作提示重复等窗口
 */

public class FileOperateContinueWindow {

    private Context mContext;
    private TextView tv_title;
    private View btn_ok;
    private View btn_cancel;
    private IFileOperateContinue iFileOperateContinue;
    private AlertDialog alertDialog;
    private TextView tv_notice_content;

    public FileOperateContinueWindow(Context context) {
        this.mContext = context;
        initView();
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.file_operate_continue_layout, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);
        tv_title = (TextView) inflate.findViewById(R.id.tv_title);
        tv_title.setText(mContext.getResources().getString(R.string.notice));
        tv_notice_content = (TextView) inflate.findViewById(R.id.tv_notice_content);
        tv_notice_content.setText(mContext.getResources().getString(R.string.override_file));
        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);
    }

    public void show() {
        alertDialog.show();
    }

    public void setContinueOperationListener(IFileOperateContinue iFileOperateContinue) {
        this.iFileOperateContinue = iFileOperateContinue;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    if (null != iFileOperateContinue) {
                        iFileOperateContinue.fileOperateContinue();
                    }
                    alertDialog.dismiss();
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    public interface IFileOperateContinue {
        void fileOperateContinue();
    }

}
