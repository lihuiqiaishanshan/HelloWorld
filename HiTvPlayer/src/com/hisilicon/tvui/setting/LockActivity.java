package com.hisilicon.tvui.setting;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseActivity;
import com.hisilicon.tvui.base.CommonValue;
import com.hisilicon.tvui.hal.halApi;
import com.hisilicon.tvui.util.KeyValue;
import com.hisilicon.tvui.util.LogTool;
import com.hisilicon.tvui.view.CheckPassWordDialog;
import com.hisilicon.tvui.view.Combox;
import com.hisilicon.tvui.view.Combox.OnComboxSelectChangeListener;


public class LockActivity extends BaseActivity implements OnClickListener,OnKeyListener
{
    private boolean hasInsertPassword = false;
    //private Button mTotalLock;
    private Button mSourceLock;
    private Button mProgramLock;
    private Button mModifyPassword;
    private Button mMenuLock;
    private Button mSourceSelect;
    private Button mKeyLock;
    private Combox mCbxParentalRating = null;
    //private LinearLayout mTotalLockLayout, mMenuLockLayout, mProgramLockLayout, mParentLockLayout;
    private LinearLayout mMenuLockLayout, mProgramLockLayout, mParentLockLayout;

    @Override
    protected void onCreate(Bundle arg0)
    {
        super.onCreate(arg0);
        setContentView(R.layout.tv_lock_setting);

        mProgramLock = (Button)findViewById(R.id.program_lock);
        mModifyPassword = (Button)findViewById(R.id.modify_password);
        //mTotalLock = (Button)findViewById(R.id.total_lock);
        mMenuLock = (Button)findViewById(R.id.menu_lock);
        mSourceLock = (Button)findViewById(R.id.source_lock);
        mSourceSelect = (Button)findViewById(R.id.source_select);
        mKeyLock = (Button)findViewById(R.id.key_lock);
        mCbxParentalRating = (Combox)findViewById(R.id.seting_parentalrating);
        //mTotalLockLayout = (LinearLayout)findViewById(R.id.total_lock_layout);
        mMenuLockLayout = (LinearLayout)findViewById(R.id.menu_lock_layout);
        mProgramLockLayout = (LinearLayout)findViewById(R.id.program_lock_layout);
        mParentLockLayout = (LinearLayout)findViewById(R.id.parental_lock_layout);
        LinearLayout mChangePaswordLayout = (LinearLayout) findViewById(R.id.modify_password_layout);
        LinearLayout mSourceLockLayout = (LinearLayout) findViewById(R.id.source_lock_layout);
        LinearLayout mSourceSelectLayout = (LinearLayout) findViewById(R.id.source_select_layout);
        LinearLayout mKeyLockLayout = (LinearLayout) findViewById(R.id.key_lock_layout);
        //mTotalLock.setOnClickListener(this);
        mProgramLock.setOnClickListener(this);
        mMenuLock.setOnClickListener(this);
        mSourceLock.setOnClickListener(this);
        mSourceSelect.setOnClickListener(this);
        mKeyLock.setOnClickListener(this);
        //mTotalLockLayout.setOnKeyListener(this);
        //mTotalLockLayout.setOnClickListener(this);
        mMenuLockLayout.setOnKeyListener(this);
        mMenuLockLayout.setOnClickListener(this);
        mProgramLockLayout.setOnKeyListener(this);
        mProgramLockLayout.setOnClickListener(this);
        mParentLockLayout.setOnKeyListener(this);
        mChangePaswordLayout.setOnKeyListener(this);
        mChangePaswordLayout.setOnClickListener(this);
        mSourceLockLayout.setOnKeyListener(this);
        mSourceSelectLayout.setOnKeyListener(this);
        mSourceSelectLayout.setOnClickListener(this);
        mKeyLockLayout.setOnClickListener(this);
        mKeyLockLayout.setOnKeyListener(this);

        setOnSelectChangeListener(mCbxParentalRating);
        mModifyPassword.setOnClickListener(this);

        String countrycode = mDtvConfig.getString(CommonValue.COUNTRY_CODE_KEY, "eng");
        if (countrycode.equalsIgnoreCase("SGP"))
        {
            String[] sgp_parental_rating = getResources().getStringArray(R.array.sgp_parental_rating);
            String[] sgp_parental_rating_value = getResources().getStringArray(R.array.sgp_parental_rating_value);
            mCbxParentalRating.setEntries(sgp_parental_rating);
            mCbxParentalRating.setEntriesValue(sgp_parental_rating_value);
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        initView();
    }

    private void initView()
    {
        if (halApi.isTVSource())
        {
            mProgramLockLayout.setVisibility(View.VISIBLE);
            mParentLockLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            mProgramLockLayout.setVisibility(View.GONE);
            mParentLockLayout.setVisibility(View.GONE);
        }

        showPasswordDialog();
        //mTotalLock.setFocusable(false);
        mMenuLock.setFocusable(false);
        mProgramLock.setFocusable(false);
        mCbxParentalRating.setFocusable(false);
        mModifyPassword.setFocusable(false);
        mSourceLock.setFocusable(false);
        mKeyLock.setFocusable(false);
        int programLock = mDtvConfig.getInt(CommonValue.PROGRAM_LOCK, CommonValue.PROGRAM_LOCK_CLOSE);
        int menuLock = mDtvConfig.getInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
        int keyLock = mDtvConfig.getInt(CommonValue.KEY_LOCK, CommonValue.KEY_LOCK_CLOSE);
        boolean sourceLock = halApi.getLockEnable(halApi.EnumLockSwitch.SOURCE_LOCK);
        //boolean totalLock = halApi.getLockEnable(halApi.EnumLockSwitch.TOTAL_LOCK);

        /*if (totalLock)
        {
            mTotalLock.setText(getString(R.string.setting_switch_on));
        }
        else
        {
            mTotalLock.setText(getString(R.string.setting_switch_off));
        }*/

        if (CommonValue.PROGRAM_LOCK_OPEN == programLock)
        {
            mProgramLock.setText(getString(R.string.setting_switch_on));
        }
        else
        {
            mProgramLock.setText(getString(R.string.setting_switch_off));
        }

        if (CommonValue.MENU_LOCK_OPEN == menuLock)
        {
            mMenuLock.setText(getString(R.string.setting_switch_on));
        }
        else
        {
            mMenuLock.setText(getString(R.string.setting_switch_off));
        }

        if (CommonValue.KEY_LOCK_OPEN == keyLock)
        {
            mKeyLock.setText(getString(R.string.setting_switch_on));
        }
        else
        {
            mKeyLock.setText(getString(R.string.setting_switch_off));
        }

        if (sourceLock)
        {
            mSourceLock.setText(getString(R.string.setting_switch_on));
        }
        else
        {
            mSourceLock.setText(getString(R.string.setting_switch_off));
        }

        String parentalRating = "" + mPCManager.getParentLockAge();

        LogTool.d(LogTool.MSETTING, "parentalRating = " + parentalRating + "\n");
        LogTool.d(LogTool.MSETTING, "parentalRating = " + getKey(mCbxParentalRating, parentalRating) + "\n");

        mCbxParentalRating.setText(getKey(mCbxParentalRating, parentalRating));

        String[] parentalRatingEntries = mCbxParentalRating.getEntries();
        if (null != parentalRatingEntries)
        {
            if (parentalRatingEntries.length > CommonValue.DEFAULT_POP_ITEM_NUMBER)
            {
                mCbxParentalRating.setPopupHeight(mCbxParentalRating.getPopupLineHeight() * CommonValue.DEFAULT_POP_ITEM_NUMBER);
            }
            else
            {
                mCbxParentalRating.setPopupHeight(mCbxParentalRating.getPopupLineHeight() * parentalRatingEntries.length);
            }
        }

    }


    private void showPasswordDialog()
    {
        showContent(false);
        final CheckPassWordDialog.Builder passwordBuilder = new CheckPassWordDialog.Builder(this, R.style.DIM_STYLE);
        CheckPassWordDialog passwordDialog = passwordBuilder.setCheckPassWordsListener(new CheckPassWordDialog.CheckPassWordDialogInterface()
        {
            @Override
            public void onCheck(int which, String passWord)
            {
                if (which == CheckPassWordDialog.CheckPassWordDialogInterface.PASSWORD_RIGHT)
                {
                    /*mMenuLock.setFocusable(true);
                    mProgramLock.setFocusable(true);
                    mCbxParentalRating.setFocusable(true);
                    mModifyPassword.setFocusable(true);
                    mMenuLock.requestFocus();*/
                    hasInsertPassword = true;
                    //mTotalLockLayout.requestFocus();
                    mMenuLockLayout.requestFocus();
                    showContent(true);
                }
            }

        }).create();

        passwordDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                if ((passwordBuilder != null) && passwordBuilder.getPasswordisRight())
                {
                    showContent(true);
                }
                else
                {
                    finish();
                }
            }
        });

        passwordBuilder.setPasswordTitle(getString(R.string.play_password_lock_setting));

        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialog.show();
    }

    private void setOnSelectChangeListener(final Combox combox)
    {
        combox.setOnSelectChangeListener(new OnComboxSelectChangeListener()
        {
            @Override
            public void onComboxSelectChange(View arg0, String strText, Object obj, int index)
            {
                combox.setText(strText);
                int age = Integer.parseInt(String.valueOf(obj));
                LogTool.d(LogTool.MSETTING, "setParentLockAge age = " + age);
                mPCManager.setParentLockAge(age);
            }
        });
    }


    @Override
    public void onClick(View arg0)
    {
        if (!hasInsertPassword)
        {
            showPasswordDialog();
            return;
        }

        /*if (R.id.total_lock == arg0.getId())
        {
            String checked = mTotalLock.getText().toString();
            int ret = 0;
            if (checked.equals(getString(R.string.setting_switch_on)))
            {
                ret = halApi.setLockEnable(halApi.EnumLockSwitch.TOTAL_LOCK, false);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set total lock close success");
                    mTotalLock.setText(getString(R.string.setting_switch_off));
                }
            }
            else
            {
                ret = halApi.setLockEnable(halApi.EnumLockSwitch.TOTAL_LOCK, true);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set total lock open success");
                    mTotalLock.setText(getString(R.string.setting_switch_on));
                }
            }
        }*/
        if (R.id.program_lock == arg0.getId())
        {
            String checked = mProgramLock.getText().toString();
            int ret = 0;
            if (checked.equals(getString(R.string.setting_switch_on)))
            {
                ret = mDtvConfig.setInt(CommonValue.PROGRAM_LOCK, CommonValue.PROGRAM_LOCK_CLOSE);
                halApi.setLockEnable(halApi.EnumLockSwitch.PROGRAM_LOCK, false);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set program lock close success");
                    mProgramLock.setText(getString(R.string.setting_switch_off));
                }
            }
            else
            {
                halApi.setLockEnable(halApi.EnumLockSwitch.PROGRAM_LOCK, true);
                ret = mDtvConfig.setInt(CommonValue.PROGRAM_LOCK, CommonValue.PROGRAM_LOCK_OPEN);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set program lock open success");
                    mProgramLock.setText(getString(R.string.setting_switch_on));
                }
            }
        }
        else if (R.id.menu_lock == arg0.getId())
        {
            String checked = mMenuLock.getText().toString();
            int ret = 0;
            if (checked.equals(getString(R.string.setting_switch_on)))
            {
                ret = mDtvConfig.setInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_CLOSE);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set menu lock close success");
                    mMenuLock.setText(getString(R.string.setting_switch_off));
                }
            }
            else
            {
                ret = mDtvConfig.setInt(CommonValue.MENU_LOCK, CommonValue.MENU_LOCK_OPEN);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set menu lock open success");
                    mMenuLock.setText(getString(R.string.setting_switch_on));
                }
            }
        }
        else if (R.id.source_lock == arg0.getId())
        {
            String checked = mSourceLock.getText().toString();
            int ret = 0;
            if (checked.equals(getString(R.string.setting_switch_on)))
            {
                ret = halApi.setLockEnable(halApi.EnumLockSwitch.SOURCE_LOCK, false);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set source lock close success");
                    mSourceLock.setText(getString(R.string.setting_switch_off));
                }
            }
            else
            {
                ret = halApi.setLockEnable(halApi.EnumLockSwitch.SOURCE_LOCK, true);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set source lock open success");
                    mSourceLock.setText(getString(R.string.setting_switch_on));
                }
            }
        }
        else if (R.id.key_lock == arg0.getId())
        {
            String checked = mKeyLock.getText().toString();
            int ret = 0;
            if (checked.equals(getString(R.string.setting_switch_on)))
            {
                halApi.enableLSADCKey(false);
                ret = mDtvConfig.setInt(CommonValue.KEY_LOCK, CommonValue.KEY_LOCK_CLOSE);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set key lock close success");
                    mKeyLock.setText(getString(R.string.setting_switch_off));
                }
            }
            else
            {
                ret = mDtvConfig.setInt(CommonValue.KEY_LOCK, CommonValue.KEY_LOCK_OPEN);
                halApi.enableLSADCKey(true);
                if (ret == 0)
                {
                    LogTool.d(LogTool.MSETTING, "set key lock open success");
                    mKeyLock.setText(getString(R.string.setting_switch_on));
                }
            }
        }
        else if (R.id.source_select == arg0.getId() || R.id.source_select_layout == arg0.getId())
        {
            SourceLockDialog mSourceLockDialog = new SourceLockDialog(this, R.style.DIM_STYLE);
            mSourceLockDialog.setCanceledOnTouchOutside(true);
            mSourceLockDialog.show();
        }
        else if (R.id.modify_password == arg0.getId() || R.id.modify_password_layout == arg0.getId())
        {
            PasswordModifyDialog dialog = new PasswordModifyDialog(this, R.style.DIM_STYLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    }

    private String getKey(Combox combox, String value)
    {
        Object[] entriesValue = combox.getEntriesValue();
        String[] entries = combox.getEntries();

        if ((null == entriesValue) || (null == entries))
        {
            return null;
        }

        int index = 0;

        for (index = 0; index < entriesValue.length; index++)
        {
            if (String.valueOf(entriesValue[index]).equals(value))
            {
                break;
            }
        }

        if (entries.length > index && (index < entriesValue.length))
        {
            return entries[index];
        }

        return entries[0];
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        finish();
    }

    @Override
    public boolean onKey(View view, int keycode, KeyEvent arg2)
    {
        if(arg2.getAction() == KeyEvent.ACTION_DOWN)
        {
            if(keycode == KeyValue.DTV_KEYVALUE_DPAD_LEFT || keycode == KeyValue.DTV_KEYVALUE_DPAD_RIGHT)
            {
                switch(view.getId())
                {
                    /*case R.id.total_lock_layout:
                        mTotalLock.callOnClick();
                        return true;*/
                    case R.id.menu_lock_layout:
                        mMenuLock.callOnClick();
                        return true;
                    case R.id.program_lock_layout:
                        mProgramLock.callOnClick();
                        return true;
                    case R.id.modify_password_layout:
                        mModifyPassword.callOnClick();
                        return true;
                    case R.id.source_lock_layout:
                        mSourceLock.callOnClick();
                        return true;
                    case R.id.key_lock_layout:
                        mKeyLock.callOnClick();
                        return true;
                }
            }
            if(keycode == KeyValue.DTV_KEYVALUE_DPAD_CENTER)
            {
                switch(view.getId())
                {
                    case R.id.parental_lock_layout:
                        mCbxParentalRating.callOnClick();
                        return true;
                    case R.id.modify_password_layout:
                        mModifyPassword.callOnClick();
                        return true;
                    case R.id.source_select_layout:
                        mSourceSelect.callOnClick();
                        return true;
                    case R.id.key_lock_layout:
                        mKeyLock.callOnClick();
                        return true;
                }
            }
        }
        return false;
    }

    private void showContent(boolean flag)
    {
        View view =findViewById(R.id.tv_lock_setting_root);
        view.setVisibility(flag?View.VISIBLE:View.GONE);
    }

}
