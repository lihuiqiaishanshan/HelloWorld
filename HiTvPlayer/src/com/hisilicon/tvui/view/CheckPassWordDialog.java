package com.hisilicon.tvui.view;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.TextView;

import com.hisilicon.dtv.DTV;
import com.hisilicon.dtv.config.DTVConfig;
import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.util.SHA;

/**
 * Dialog used to check input password.
 * @author y00164887
 *
 */

public class CheckPassWordDialog extends Dialog
{
    public CheckPassWordDialog(Context context)
    {
        super(context);
    }

    public CheckPassWordDialog(Context context, int theme)
    {
        super(context, theme);
    }

    public static class Builder
    {
        private Context mContext;
        private CheckPassWordDialogInterface mCheckPassWordsListener;
        private TextView mPasswordTitleView;
        private EditText mPassword1EditText;
        private EditText mPassword2EditText;
        private EditText mPassword3EditText;
        private EditText mPassword4EditText;
        private TextView mPasswordWrongTextView;
        private String mPwdTitle;
        private String mPassword;
        private boolean mIsPasswordRight = false;
        private int mId;
        private TextView mCurrentRate;
        private TextView mCurrentChannel;
        public Builder(Context context, int id)
        {
            mContext = context;
            mId = id;
            mIsPasswordRight = false;
        }

        public boolean getPasswordisRight()
        {
            return mIsPasswordRight;
        }

        public void setmCurrentChannel(String channelInfo) {
            if (TextUtils.isEmpty(channelInfo)) {
                mCurrentChannel.setVisibility(View.GONE);
            } else {
                mCurrentChannel.setVisibility(View.VISIBLE);
                mCurrentChannel.setText(channelInfo);
            }
        }

        public void setPasswordTitle(String passwordTitle)
        {
            mPwdTitle = passwordTitle;
            mPasswordTitleView.setText(passwordTitle);
        }
        public void setCurrentRate(boolean visible, String rate) {
            mCurrentRate.setText(TextUtils.isEmpty(rate) ? "" : rate);
            mCurrentRate.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        public String getPasswordTitle()
        {
            return mPwdTitle;
        }

        public Builder setCheckPassWordsListener(CheckPassWordDialogInterface listener)
        {
            mCheckPassWordsListener = listener;
            return this;
        }

        public Builder setOnKeyListener(View.OnKeyListener listener)
        {
            return this;
        }

        public void addMyTextChangedListener(final EditText editText, final CheckPassWordDialog dialog)
        {
            TextWatcher myTextWatcher = new TextWatcher()
            {
                @Override
                public void afterTextChanged(Editable arg0)
                {
                    if (1 != arg0.length())
                    {
                        return;
                    }

                    if (editText != mPassword4EditText)
                    {
                        View nextView = editText.focusSearch(View.FOCUS_RIGHT);
                        if (null != nextView)
                        {
                            if (!nextView.requestFocus(View.FOCUS_RIGHT))
                            {
                                throw new IllegalStateException("focus search returned a view " + "that wasn't able to take focus!");
                            }
                        }
                    }
                    else
                    {
                        mPassword = mPassword1EditText.getText().toString() + mPassword2EditText.getText().toString() + mPassword3EditText.getText().toString()
                                + mPassword4EditText.getText().toString();

                        //////////
                        DTV dtv = DTV.getInstance(CommonValue.DTV_PLUGIN_NAME);
                        DTVConfig dtvConfig = dtv.getConfig();

                        String userPassword = dtvConfig.getString(CommonValue.USER_PWD_KEY, CommonValue.DEFAULT_USER_PWD);

                        String encryPassword = SHA.toSHA(mPassword);
                        if (null == encryPassword)
                        {
                            LogTool.w(LogTool.MSETTING, "encryPassword password error");
                            return;
                        }

                        if (encryPassword.equals(userPassword))
                        {
                            cleanPassword();
                            mIsPasswordRight = true;
                            mPassword1EditText.requestFocus();
                            mPasswordWrongTextView.setVisibility(View.INVISIBLE);
                            dialog.dismiss();
                            if (mCheckPassWordsListener != null)
                            {
                                mCheckPassWordsListener.onCheck(CheckPassWordDialogInterface.PASSWORD_RIGHT, mPassword);
                            }
                        }
                        else
                        {
                            mPassword1EditText.requestFocus();
                            mIsPasswordRight = false;
                            cleanPassword();
                            mPasswordWrongTextView.setVisibility(View.VISIBLE);
                            if (mCheckPassWordsListener != null)
                            {
                                mCheckPassWordsListener.onCheck(CheckPassWordDialogInterface.PASSWORD_ERROR, mPassword);
                            }
                            return;
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                }

                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
                {
                }
            };

            editText.addTextChangedListener(myTextWatcher);
        }

        public CheckPassWordDialog create()
        {
            LogTool.d(LogTool.MMAIN, "CheckPassWordDialog create()");

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            final CheckPassWordDialog dialog = new CheckPassWordDialog(mContext, mId);

            @SuppressLint("InflateParams") View layout = inflater.inflate(R.layout.check_password_dialog, null);

            dialog.addContentView(layout, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

            //dialog.getWindow().setLayout(WIDTH, HEIGHT);

            mPassword = mContext.getString(R.string.string_null);
            mPwdTitle = "";

            mPasswordTitleView = (TextView) layout.findViewById(R.id.id_password_title);
            mPassword1EditText = (EditText) layout.findViewById(R.id.et_password_1);
            mPassword2EditText = (EditText) layout.findViewById(R.id.et_password_2);
            mPassword3EditText = (EditText) layout.findViewById(R.id.et_password_3);
            mPassword4EditText = (EditText) layout.findViewById(R.id.et_password_4);
            mPasswordWrongTextView = (TextView) layout.findViewById(R.id.tv_password_wrong);
            mCurrentRate = (TextView) layout.findViewById(R.id.id_current_rate);
            mCurrentChannel = (TextView) layout.findViewById(R.id.id_current_channel);
            mPasswordTitleView.setText("");
            mCurrentChannel.setText("");

            addMyTextChangedListener(mPassword1EditText, dialog);
            addMyTextChangedListener(mPassword2EditText, dialog);
            addMyTextChangedListener(mPassword3EditText, dialog);
            addMyTextChangedListener(mPassword4EditText, dialog);

            dialog.setOnDismissListener(new OnDismissListener()
            {
                @Override
                public void onDismiss(DialogInterface arg0)
                {
                    cleanPassword();
                }
            });

            return dialog;
        }

        /**
         * clean password and EdiTexts
         */
        private void cleanPassword()
        {
            mPassword = mContext.getString(R.string.string_null);
            mPassword1EditText.setText("");
            mPassword2EditText.setText("");
            mPassword3EditText.setText("");
            mPassword4EditText.setText("");
            mPassword1EditText.requestFocus();
        }

        public void CleanWrongMsg()
        {
            mPasswordWrongTextView.setVisibility(View.INVISIBLE);
        }

    }

    public interface CheckPassWordDialogInterface
    {
        int PASSWORD_RIGHT = 0;
        int PASSWORD_ERROR = -1;

        void onCheck(int which, String passWord);
    }
}
