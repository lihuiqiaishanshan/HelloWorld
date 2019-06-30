package com.hisilicon.tvui.installtion;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hisilicon.tvui.R;
import com.hisilicon.tvui.base.BaseView;
import com.hisilicon.tvui.util.LogTool;

public class SetupAreaCodeView extends BaseView implements IScanSubWnd {
    private IScanMainWnd mMainWnd;
    private AutoScanActivity mParentWnd;
    private EditText mAreaEditView;
    private Button mConfirmButton;
    private String[] mCountryNeedAreaCode = null;

    public SetupAreaCodeView(AutoScanActivity ownerActivity) {
        super(ownerActivity.findViewById(R.id.ly_setup_area));
        mMainWnd = ownerActivity;
        mParentWnd = ownerActivity;
        initView();
        mCountryNeedAreaCode = mParentWnd.getResources().getStringArray(R.array.country_values_need_set_area);
    }

    @Override
    public KeyDoResult keyDispatch(int keyCode, KeyEvent event, View parent) {
        LogTool.d(LogTool.MINSTALL, "SetupAreaCodeView onKeyDown . KeyCode = " + keyCode);
        return KeyDoResult.DO_DONE_NEED_SYSTEM;
    }

    @Override
    public boolean isCanStartScan() {
        return false;
    }

    @Override
    public boolean isNetworkScan() {
        return false;
    }

    @Override
    public void setMainWnd(IScanMainWnd parent) {
        mMainWnd = parent;
    }

    public void initView() {
        mAreaEditView = mParentWnd.findViewById(R.id.edit_content_input);
        mConfirmButton = mParentWnd.findViewById(R.id.modify_btn);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogTool.d(LogTool.MINSTALL, "text :" + mAreaEditView.getText());
                int code = 0;
                try {
                    code = Integer.parseInt(mAreaEditView.getText().toString());
                }catch (NumberFormatException e){
                    LogTool.e(LogTool.MINSTALL, e.getMessage());
                }
                mDTV.setAreaCode(code);

                if (null != mMainWnd) {
                    mMainWnd.sendMessage(IScanMainWnd.MSG_ID_NEXT_STEP, null);
                }
            }
        });
    }

    @Override
    public void show() {
        super.show();
        mAreaEditView.setText(String.valueOf(mDTV.getAreaCode()));
        mAreaEditView.requestFocus();
    }

    public boolean isNeedSetArea() {
        if (mCountryNeedAreaCode != null && mCountryNeedAreaCode.length > 0) {
            String currentCountry = mDTV.getCountry();
            for (String countryCode : mCountryNeedAreaCode) {
                if (countryCode.equals(currentCountry)){
                    return true;
                }
            }
        }
        return false;
    }

    public void restoreDefaultAreaCode() {
        mDTV.setAreaCode(0);
    }
}
