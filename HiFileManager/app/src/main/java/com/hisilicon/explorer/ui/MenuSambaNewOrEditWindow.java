package com.hisilicon.explorer.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.explorer.R;
import com.hisilicon.explorer.model.MenuInfo;
import com.hisilicon.explorer.model.SambaItemInfo;
import com.hisilicon.explorer.utils.ToastUtil;

/**
 * nfs或者samba操作窗口
 */

public class MenuSambaNewOrEditWindow {

    private Context mContext;
    private MenuInfo menuInfo;
    private View btn_ok;
    private View btn_cancel;
    private MenuInfo.IServerOperate iServerOperate;
    private AlertDialog alertDialog;
    private SambaItemInfo sambaItemInfo;
    private EditText et_server_address;
    private EditText et_workpath;
    private EditText et_account;
    private EditText et_pwd;
    private EditText et_server_name;
    private CheckBox cb_add_short;

    public MenuSambaNewOrEditWindow(Context context, MenuInfo menuInfo) {
        this.mContext = context;
        this.menuInfo = menuInfo;
        initView();
    }

    public void setSambaItem(SambaItemInfo sambaItemInfo) {
        this.sambaItemInfo = sambaItemInfo;
        if (sambaItemInfo != null) {
            et_server_address.setText(sambaItemInfo.getServerIp());
            et_workpath.setText(sambaItemInfo.getWorkPath());
            et_account.setText(sambaItemInfo.getAccount());
            et_pwd.setText(sambaItemInfo.getPwd());
            et_server_name.setText(sambaItemInfo.getServerName());
        }
    }

    public void setAddressAndWorkEnable(boolean b) {
        et_workpath.setFocusable(b);
        et_server_address.setFocusable(b);
    }

    private void initView() {
        View inflate = LayoutInflater.from(mContext).inflate(R.layout.new_server, null);
        alertDialog = new AlertDialog.Builder(mContext, R.style.MyDialogStyle).create();
        alertDialog.setView(inflate);

        et_server_address = (EditText) inflate.findViewById(R.id.editServer);
        et_workpath = (EditText) inflate.findViewById(R.id.position);
        et_account = (EditText) inflate.findViewById(R.id.editName);
        et_pwd = (EditText) inflate.findViewById(R.id.editpass);
        et_server_name = (EditText) inflate.findViewById(R.id.editdisplay);

        cb_add_short = (CheckBox) inflate.findViewById(R.id.add_shortcut);
        cb_add_short.setFocusable(false);

        btn_ok = (Button) inflate.findViewById(R.id.btn_ok);
        btn_cancel = (Button) inflate.findViewById(R.id.btn_cancel);
        btn_ok.setOnClickListener(onClickListener);
        btn_cancel.setOnClickListener(onClickListener);

    }

    public void show() {
        alertDialog.show();
    }

    public void setOperationListener(MenuInfo.IServerOperate iServerOperate) {
        this.iServerOperate = iServerOperate;
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_ok:
                    Editable server_address = et_server_address.getText();
                    Editable work_path = et_workpath.getText();
                    Editable account = et_account.getText();
                    Editable pwd = et_pwd.getText();
                    Editable server_name = et_server_name.getText();
                    if (TextUtils.isEmpty(server_address) || TextUtils.isEmpty(work_path)) {
                        //TODO 提示有字段为空
                        ToastUtil.showMessage(mContext, mContext.getResources().getString(R.string.name_empty_tips), Toast.LENGTH_SHORT);
                        return;
                    }
                    if (sambaItemInfo == null) {
                        sambaItemInfo = new SambaItemInfo();
                    }
                    sambaItemInfo.setType(SambaItemInfo.TYPE_ITEM);
                    sambaItemInfo.setNickName("\\\\" + server_address + "\\" + work_path);
                    sambaItemInfo.setServerIp(server_address.toString());
                    sambaItemInfo.setWorkPath(work_path.toString());
                    if (!TextUtils.isEmpty(account)) {
                        sambaItemInfo.setAccount(account.toString());
                    }
                    if (!TextUtils.isEmpty(pwd)) {
                        sambaItemInfo.setPwd(pwd.toString());
                    }
                    if (TextUtils.isEmpty(server_name)) {
                        sambaItemInfo.setServerName(server_address.toString());
                    }
                    if (null != iServerOperate) {
                        switch (menuInfo.getmMenuFuctionType()) {
                            case MenuInfo.MenuFuctionTypeForItem.NEW:
                                iServerOperate.serverAdd(sambaItemInfo);
                                break;
                            case MenuInfo.MenuFuctionTypeForItem.EDIT:
                                iServerOperate.serverEdit(sambaItemInfo);
                                break;
                            case MenuInfo.MenuFuctionTypeForItem.ADDSHORTCUT:
                                iServerOperate.serverAddShortCut(sambaItemInfo);
                                break;
                        }
                    }
                    break;
                case R.id.btn_cancel:
                    alertDialog.dismiss();
                    break;
            }
        }
    };

    public void dismiss() {
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
    }

}
