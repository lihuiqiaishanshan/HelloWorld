package com.hisilicon.tvui.setting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.SHA;
import com.hisilicon.tvui.view.MyToast;

/**
 * the Dialog is used to change the password for the user the password length is four.if the user
 * change the password length please modify the setting_password_modify.xml and the
 * check_password_dialog.xml
 *
 */
public class PasswordModifyDialog extends Dialog
{
    private final Context mContext;
    private DTVConfig mDtvConfig;
    private EditText mOldPassword;
    private EditText mNewPassword;
    private EditText mConfirmPassword;

    public PasswordModifyDialog(Context context, int theme)
    {
        super(context, theme);
        mContext = context;
        DTV mDTV = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
        mDtvConfig = mDTV.getConfig();
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_password_modify);
        initView();
    }

    private void initView()
    {
        mOldPassword = (EditText) this.findViewById(R.id.oldpassword);
        mNewPassword = (EditText) this.findViewById(R.id.newpassword);
        mConfirmPassword = (EditText) this.findViewById(R.id.confirmpassword);
        Button mOKModify = (Button) this.findViewById(R.id.password_modify_yes);
        Button mCancelModify = (Button) this.findViewById(R.id.password_modify_no);
        mOKModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String oldpasswordText = mOldPassword.getText().toString();
                String newpasswordText = mNewPassword.getText().toString();
                String confirmpasswordText = mConfirmPassword.getText().toString();

                if (oldpasswordText.length() == 0) {
                    mOldPassword.requestFocus();
                    MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_old_password_null), MyToast.LENGTH_LONG).show();
                } else if ((oldpasswordText.length() != CommonValue.PASSWORD_LENGTH)) {
                    mOldPassword.requestFocus();
                    MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_old_password_length_error), MyToast.LENGTH_LONG).show();
                    return;
                } else {
                    String oldpassword = SHA.toSHA(oldpasswordText);
                    if (null == oldpassword) {
                        LogTool.w(LogTool.MSETTING, "old password is null");
                        return;
                    }
                    String password = mDtvConfig.getString(CommonValue.USER_PWD_KEY, CommonValue.DEFAULT_USER_PWD);
                    if (!password.trim().equals(oldpassword.trim())) {
                        LogTool.d(LogTool.MSETTING, "old password error");
                        mOldPassword.requestFocus();
                        mOldPassword.setText("");
                        MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_old_password_error), MyToast.LENGTH_LONG).show();
                        return;
                    }

                    if (newpasswordText.length() == 0) {
                        LogTool.d(LogTool.MSETTING, "new password length is 0");
                        mNewPassword.requestFocus();
                        MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_null), MyToast.LENGTH_LONG).show();
                        return;
                    } else if (newpasswordText.length() != CommonValue.PASSWORD_LENGTH) {
                        LogTool.d(LogTool.MSETTING, "new password length error");
                        mNewPassword.requestFocus();
                        MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_length_error), MyToast.LENGTH_LONG).show();
                        return;
                    } else {
                        if (confirmpasswordText.length() == 0) {
                            LogTool.d(LogTool.MSETTING, "new password confirm length is 0");
                            mConfirmPassword.setText("");
                            mConfirmPassword.requestFocus();
                            // showDialog(mContext.getString(R.string.dvbnewpasswordconfirmerror));
                            MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_null), MyToast.LENGTH_LONG).show();
                        } else if (!newpasswordText.equals(confirmpasswordText)) {
                            // dialog.dismiss();
                            LogTool.d(LogTool.MSETTING, "new password confirm error");
                            mConfirmPassword.setText("");
                            mConfirmPassword.requestFocus();
                            // showDialog(mContext.getString(R.string.dvbnewpasswordconfirmerror));
                            MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_confirm_error), MyToast.LENGTH_LONG).show();
                        } else if (newpasswordText.equals(oldpasswordText)) {
                            LogTool.d(LogTool.MSETTING, "same not need change");
                            mNewPassword.setText("");
                            mConfirmPassword.setText("");
                            mNewPassword.requestFocus();
                            MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_same_with_old), MyToast.LENGTH_LONG).show();
                        } else {
                            LogTool.d(LogTool.MSETTING, "change password sucess");
                            mDtvConfig.setString(CommonValue.USER_PWD_KEY, SHA.toSHA(newpasswordText));
                            MyToast.makeText(mContext, mContext.getResources().getString(R.string.setting_new_password_change_ok), MyToast.LENGTH_LONG).show();
                            PasswordModifyDialog.this.dismiss();
                        }
                    }
                }
            }
        });
        mCancelModify.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                PasswordModifyDialog.this.cancel();
            }
        });
    }

}
