
package com.hisilicon.launcher.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.util.AttributeSet;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.InterfaceValueMaps;
import com.hisilicon.launcher.interfaces.ShowAbleInterface;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.logic.factory.LogicFactory;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.Util;
import com.hisilicon.launcher.view.setting.CustomSettingView;
import com.hisilicon.launcher.view.setting.NetSettingDialog;

public class MainPageSetting extends RelativeLayout implements
        ShowAbleInterface, View.OnClickListener, OnFocusChangeListener {

    private static final String TAG = "MainPageSetting";
    public final static int PAGENUM = 1;
    private MainActivity mContext;
    private View[] imgView;
    // This variable is used to solve the pop-up window when
    // the MainActivity focusedView is unable to effectively control the focus
    // problem
    private int mFocusedView = 0;
    private LogicFactory mLogicFactory = null;
    private InterfaceLogic mInterfaceLogic;
    private int[][] settings = InterfaceValueMaps.app_item_values;
    // Network management of two level menu
    private NetSettingDialog mNetSettingDialog;
    // In addition to other settings menu two network management
    private AlertDialog mSettingDialog;

    public MainPageSetting(Context context) {
        super(context);
    }

    public MainPageSetting(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogHelper.d(TAG, "MainPageSetting init");
        this.mContext = (MainActivity) context;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parent = inflater.inflate(R.layout.main_page_setting, this);
        initView(parent);
        mLogicFactory = new LogicFactory(mContext);
    }

    private void initView(View parent) {
        imgView = new View[] {
                parent.findViewById(R.id.set_item_net),
                parent.findViewById(R.id.set_item_pic),
                parent.findViewById(R.id.set_item_sound),
                parent.findViewById(R.id.set_item_sysupgrade),
                parent.findViewById(R.id.set_item_advanced),
                parent.findViewById(R.id.set_item_recover),
                parent.findViewById(R.id.set_item_systeminfo),
                parent.findViewById(R.id.set_item_help)
        };

        for (int i = 0; i < imgView.length; i++) {
            imgView[i].setOnClickListener(this);
            imgView[i].getBackground().setAlpha(0);
            imgView[i].setOnFocusChangeListener(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContext.getRoot().getCurScreen().getId() == PAGENUM && !hasFocus()) {
            if (imgView != null) {
                imgView[Constant.NUMBER_0].requestFocus();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (imgView[Constant.NUMBER_0].hasFocus()
                    || imgView[Constant.NUMBER_5].hasFocus()) {
                mContext.snapToPreScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if ((imgView[Constant.NUMBER_4].hasFocus() || imgView[Constant.NUMBER_7]
                    .hasFocus())) {
                mContext.snapToNextScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (imgView[Constant.NUMBER_5].hasFocus()
                    || imgView[Constant.NUMBER_6].hasFocus()
                    || imgView[Constant.NUMBER_7].hasFocus()) {
                RelativeLayout[] tagList = mContext.getTagView().getTagList();
                tagList[mContext.getFocusedPage()].requestFocus();
                mContext.setFocusedView(0);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (imgView[0].hasFocus() || imgView[1].hasFocus()
                    || imgView[Constant.NUMBER_2].hasFocus()
                    || imgView[Constant.NUMBER_3].hasFocus()
                    || imgView[Constant.NUMBER_4].hasFocus()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onShow() {
        LogHelper.d(TAG, "Now onShow :" + PAGENUM);
        if (MainActivity.isChangeLocale()) {
            imgView[Constant.NUMBER_4].requestFocus();
        } else if (MainActivity.isSnapLeftOrRight()) {
            if (!mContext.getTagView().hasFocus()) {
                if (mContext.isSnapLeft()) {
                    if (mContext.isFocusUp()) {
                        imgView[Constant.NUMBER_4].requestFocus();
                    } else {
                        imgView[Constant.NUMBER_7].requestFocus();
                    }
                } else {
                    if (mContext.isFocusUp()) {
                        imgView[Constant.NUMBER_0].requestFocus();
                    } else {
                        imgView[Constant.NUMBER_5].requestFocus();
                    }
                }
            }
        } else {
            imgView[Constant.NUMBER_0].requestFocus();
        }
        MainActivity.setChangeLocale(false);
        mContext.getTagView().setViewOnSelectChange(PAGENUM);
    }

    public int getId() {
        return PAGENUM;
    }

    @Override
    public View[] getImgViews() {
        return imgView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.set_item_net:
                createNetDialog();
                break;
            case R.id.set_item_pic:
                createDialog(R.id.set_item_pic);
                break;
            case R.id.set_item_sound:
                createDialog(R.id.set_item_sound);
                break;
            case R.id.set_item_sysupgrade:
                createSysUpgradeDialog();
                break;
            case R.id.set_item_advanced:
                createDialog(R.id.set_item_advanced);
                break;
            case R.id.set_item_recover:
                createDialog(R.id.set_item_recover);
                break;
            case R.id.set_item_systeminfo:
                createDialog(R.id.set_item_systeminfo);
                break;
            case R.id.set_item_help:
                // TODO Use the help will not function, SystemInfoLogic may be
                // useless
                break;
            default:
                break;
        }
    }

    /**
     * create net setting Dialog
     */
    private void createNetDialog() {
        setViewVisibility(false);
        mNetSettingDialog = new NetSettingDialog(mContext,
                NetSettingDialog.FLAG_NET);
        mNetSettingDialog.setCanceledOnTouchOutside(false);
        mNetSettingDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                setViewVisibility(true);
            }
        });
        Window window = mNetSettingDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = (int) getResources().getDimension(R.dimen.dimen_600px);
        lp.width = (int) getResources().getDimension(R.dimen.dimen_800px);
        window.setAttributes(lp);
        mNetSettingDialog.show();
    }

    /**
     * create System Upgrade Dialog
     */
    private void createSysUpgradeDialog() {
        setViewVisibility(false);
        mNetSettingDialog = new NetSettingDialog(mContext,
                NetSettingDialog.SYSTEM_UPDATE);
        mNetSettingDialog.setCanceledOnTouchOutside(false);
        mNetSettingDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                setViewVisibility(true);
            }
        });
        Window windows = mNetSettingDialog.getWindow();
        WindowManager.LayoutParams lps = windows.getAttributes();
        lps.height = (int) getResources().getDimension(R.dimen.dimen_600px);
        lps.width = (int) getResources().getDimension(R.dimen.dimen_800px);
        windows.setAttributes(lps);
        mNetSettingDialog.show();
    }

    /**
     * According to the ID value created dialog
     */
    public void createDialog(int Index) {
        mInterfaceLogic = mLogicFactory.createLogic(Index);
        if (mInterfaceLogic != null && mInterfaceLogic.getWidgetTypeList() != null) {
            setViewVisibility(false);
            mSettingDialog = new AlertDialog.Builder(mContext,
                    R.style.Translucent_NoTitle).create();
            mSettingDialog.setCanceledOnTouchOutside(false);
            mSettingDialog.show();
            mSettingDialog.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface arg0) {
                    setViewVisibility(true);
                }
            });

            Window window = mSettingDialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.height = (int) getResources().getDimension(R.dimen.dimen_600px);
            lp.width = (int) getResources().getDimension(R.dimen.dimen_800px);
            window.setAttributes(lp);

            window.setContentView(new CustomSettingView(mContext, mContext
                    .getResources().getString(
                            Util.getValueFromArray(Index, settings)),
                    mInterfaceLogic));
        }
    }

    /**
     * When the pop-up dialog box, hide the main interface
     *
     * @param show
     */
    public void setViewVisibility(boolean show){
        if (!show) {
            for (View img : imgView) {
                img.setVisibility(View.INVISIBLE);
            }
            mContext.getTagView().setVisibility(View.INVISIBLE);
            mContext.getInterImg().setVisibility(View.INVISIBLE);
            mContext.getWifiImg().setVisibility(View.INVISIBLE);
            mContext.getLogoImg().setVisibility(View.INVISIBLE);
        } else {
            for (View img : imgView) {
                img.setVisibility(View.VISIBLE);
            }
            mContext.getTagView().setVisibility(View.VISIBLE);
            mContext.getInterImg().setVisibility(View.VISIBLE);
            mContext.getWifiImg().setVisibility(View.VISIBLE);
            mContext.getLogoImg().setVisibility(View.VISIBLE);
            if (mContext.getRoot().getCurScreen().getId() == PAGENUM) {
                imgView[mFocusedView].requestFocus();
            }
        }
    }

    /**
     * Remove all dialog
     */
    public void dismissDialog() {
        if (mNetSettingDialog != null && mNetSettingDialog.isShowing()) {

            mNetSettingDialog.dismiss();
            mNetSettingDialog = null;
        }
        if (mSettingDialog != null && mSettingDialog.isShowing()) {
            mSettingDialog.dismiss();
            mSettingDialog = null;
        }
        if (null != mInterfaceLogic) {
            mInterfaceLogic.dismissDialog();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogHelper.d(TAG, "onFocusChange");
        if (hasFocus) {
            v.bringToFront();
            v.animate().scaleX(0.907f).scaleY(0.924f)
                    .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            v.getBackground().setAlpha(255);
            // Set the flag and focus related position
            switch (v.getId()) {
                case R.id.set_item_net:
                    mFocusedView = Constant.NUMBER_0;
                    mContext.setFocusedView(Constant.NUMBER_0);
                    mContext.setFocusUp(true);
                    break;
                case R.id.set_item_pic:
                    mFocusedView = Constant.NUMBER_1;
                    mContext.setFocusedView(Constant.NUMBER_1);
                    mContext.setFocusUp(true);
                    break;
                case R.id.set_item_sound:
                    mFocusedView = Constant.NUMBER_2;
                    mContext.setFocusedView(Constant.NUMBER_2);
                    mContext.setFocusUp(true);
                    break;
                case R.id.set_item_sysupgrade:
                    mFocusedView = Constant.NUMBER_3;
                    mContext.setFocusedView(Constant.NUMBER_3);
                    mContext.setFocusUp(true);
                    break;
                case R.id.set_item_advanced:
                    mFocusedView = Constant.NUMBER_4;
                    mContext.setFocusedView(Constant.NUMBER_4);
                    mContext.setFocusUp(true);
                    break;
                case R.id.set_item_recover:
                    mFocusedView = Constant.NUMBER_5;
                    mContext.setFocusedView(Constant.NUMBER_5);
                    mContext.setFocusUp(false);
                    break;
                case R.id.set_item_systeminfo:
                    mFocusedView = Constant.NUMBER_6;
                    mContext.setFocusedView(Constant.NUMBER_6);
                    mContext.setFocusUp(false);
                    break;
                case R.id.set_item_help:
                    mFocusedView = Constant.NUMBER_7;
                    mContext.setFocusedView(Constant.NUMBER_7);
                    mContext.setFocusUp(false);
                    break;
                default:
                    break;
            }
        } else {
            v.getBackground().setAlpha(0);
            v.animate().scaleX(0.825f).scaleY(0.84f)
                    .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
        }
    }
}
