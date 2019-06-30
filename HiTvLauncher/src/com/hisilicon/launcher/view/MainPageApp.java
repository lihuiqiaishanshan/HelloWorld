
package com.hisilicon.launcher.view;

import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.graphics.PixelFormat;
import android.os.SystemProperties;
import android.util.AttributeSet;

import com.hisilicon.launcher.hal.halApi;
import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.constant.EnumSystemTvSystem;
import com.hisilicon.android.tvapi.impl.SystemSettingImpl;
import com.hisilicon.dtv.DTV;
import com.hisilicon.launcher.interfaces.SourceManagerInterface;
import com.hisilicon.launcher.util.LogHelper;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.MyAppActivity;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.ShowAbleInterface;
import com.hisilicon.launcher.util.Constant;

/**
 * The first big view
 */
public class MainPageApp extends RelativeLayout implements ShowAbleInterface,
        View.OnClickListener, OnFocusChangeListener {
    private TextView mCurSourceText;
    private static final String TAG = "MainPageApp";
    public final static int PAGENUM = 0;
    private MainActivity mContext;
    public View[] imgView;
    private boolean isOversea = false;
    private int currentSelectAppId;
    // The second interface package name
    private String[] secondPkg = new String[]{
            "",//
            "",// All application，at onClick Marked
            "com.hisilicon.explorer",// Local media
            "com.hisilicon.dlna.dmp",// DLNA-DMP
            "com.google.android.exoplayer.demo", // Exo
            "com.hisilicon.miracast",// Miracast
            "org.chromium.webview_shell"// browser
    };
    // the packages name of App showing in the front
    private String[] secondCls = new String[]{
            "", "",
            "com.hisilicon.explorer.activity.TabBarExample",
            "com.hisilicon.dlna.dmp.HiDMPActivity",
            "com.google.android.exoplayer.demo.SampleChooserActivity",
            "com.hisilicon.miracast.activity.WelcomeActivity",
            "org.chromium.webview_shell.WebViewBrowserActivity"
    };

    public MainPageApp(Context context) {
        super(context);
    }

    public MainPageApp(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogHelper.d(TAG, "MainPageApp init");
        this.mContext = (MainActivity) context;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parent = inflater.inflate(R.layout.main_page_myapp, this);
        readConfig();
        initView(parent);
    }

    private void readConfig() {
        String appSupport = SystemProperties.get("persist.app.support", "all");
        LogHelper.d(TAG, "appSupport = " + appSupport);
        if (appSupport.equals("oversea")) {
            isOversea = true;
        }
    }

    /**
     * Initialize current views
     */
    private void initView(View parent) {
        SurfaceView surface = (SurfaceView) parent.findViewById(R.id.minvideo);
        SurfaceHolder sh = surface.getHolder();
        sh.setFormat(PixelFormat.TRANSPARENT);
        imgView = new View[]{
                parent.findViewById(R.id.tv_item_window),
                parent.findViewById(R.id.app_item_icon),
                parent.findViewById(R.id.app_item_mediacenter),
                parent.findViewById(R.id.app_item_multi_screen),
                parent.findViewById(R.id.app_item_browser),
                parent.findViewById(R.id.app_item_icon_3d),
                parent.findViewById(R.id.app_item_tvqq)
        };
        for (int i = 0; i < imgView.length; i++) {
            imgView[i].setOnClickListener(this);
            imgView[i].getBackground().setAlpha(0);
            imgView[i].setOnFocusChangeListener(this);
        }
        mCurSourceText = (TextView) parent.findViewById(R.id.tv_window_txt);
        ImageView imgBrowser = parent.findViewById(R.id.img_browser);
        TextView appTxtBrowser = parent.findViewById(R.id.app_txt_browser);
        ImageView imgIcon3d = parent.findViewById(R.id.img_icon_3d);
        TextView appTxtIcon3d = parent.findViewById(R.id.app_txt_icon_3d);
        ImageView imgTvqq = parent.findViewById(R.id.img_tvqq);
        TextView appTxtTvqq = parent.findViewById(R.id.app_txt_tvqq);
        imgBrowser.setBackgroundResource(R.drawable.app_icon_exo_player);
        appTxtBrowser.setText(R.string.exo_player_normal);
        imgIcon3d.setBackgroundResource(R.drawable.app_icon_miracast);
        appTxtIcon3d.setText(R.string.miracast_normal);
        imgTvqq.setBackgroundResource(R.drawable.app_icon_browser);
        appTxtTvqq.setText(R.string.browser_normal);
    }

    public void onDestroy() {
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (imgView[Constant.NUMBER_0].hasFocus()
                    || imgView[Constant.NUMBER_2].hasFocus()) {
                mContext.snapToPreScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if ((imgView[Constant.NUMBER_1].hasFocus() || imgView[Constant.NUMBER_6]
                    .hasFocus())) {
                mContext.snapToNextScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (imgView[Constant.NUMBER_1].hasFocus()
                    || imgView[Constant.NUMBER_0].hasFocus()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (imgView[Constant.NUMBER_2].hasFocus()
                    || imgView[Constant.NUMBER_3].hasFocus()
                    || imgView[Constant.NUMBER_4].hasFocus()
                    || imgView[Constant.NUMBER_5].hasFocus()
                    || imgView[Constant.NUMBER_6].hasFocus()) {
                RelativeLayout[] tagList = mContext.getTagView().getTagList();
                tagList[mContext.getFocusedPage()].requestFocus();
                mContext.setFocusedView(0);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onShow() {
        LogHelper.i(TAG, "Now onShow :" + PAGENUM);
        if (MainActivity.isSnapLeftOrRight()) {
            if (!mContext.getTagView().hasFocus()) {
                if (mContext.isSnapLeft()) {
                    if (mContext.isFocusUp()) {
                        imgView[Constant.NUMBER_1].requestFocus();
                    } else {
                        imgView[Constant.NUMBER_6].requestFocus();
                    }
                } else {
                    if (mContext.isFocusUp()) {
                        imgView[Constant.NUMBER_0].requestFocus();
                    } else {
                        imgView[Constant.NUMBER_2].requestFocus();
                    }
                }
            }
        } else {
            requestFocusBySourceID();
        }
        MainActivity.setSnapLeftOrRight(false);
        mContext.getTagView().setViewOnSelectChange(PAGENUM);
    }

    private void requestFocusBySourceID() {
        if (SystemProperties.getBoolean("persist.launcher.tvpage.debug", false)) {
            imgView[Constant.NUMBER_0].requestFocus();
            return;
        }
        imgView[currentSelectAppId].requestFocus();
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
        for (int i = 0; i < imgView.length; i++) {
            if (imgView[i] == v) {
                if (i == 0) {
                    Intent intent = new Intent();
                    intent.setAction(Constant.INTENT_ATV);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    int curId = SourceManagerInterface.getSelectSourceId();
                    switch (curId) {
                        case EnumSourceIndex.SOURCE_DVBC:
                        case EnumSourceIndex.SOURCE_DTMB:
                        case EnumSourceIndex.SOURCE_DVBT:
                        case EnumSourceIndex.SOURCE_ATSC:
                        case EnumSourceIndex.SOURCE_DVBS:
                        case EnumSourceIndex.SOURCE_ISDBT:
                            intent.setAction(Constant.INTENT_DTV);
                            intent.putExtra("SourceName", curId);
                            break;
                        case EnumSourceIndex.SOURCE_ATV:
                        case EnumSourceIndex.SOURCE_CVBS1:
                        case EnumSourceIndex.SOURCE_CVBS2:
                        case EnumSourceIndex.SOURCE_YPBPR1:
                        case EnumSourceIndex.SOURCE_HDMI1:
                        case EnumSourceIndex.SOURCE_HDMI2:
                        case EnumSourceIndex.SOURCE_HDMI3:
                        case EnumSourceIndex.SOURCE_HDMI4:
                        case EnumSourceIndex.SOURCE_VGA:
                        default:
                            intent.putExtra("SourceName", curId);
                            break;
                    }
                    mContext.stopPlayer();
                    mContext.startActivity(intent);
                    currentSelectAppId = 0;
                } else if (i == 1) {
                    currentSelectAppId = 1;
                    mContext.stopPlayer();
                    mContext.releaseResourcePlayer();
                    Intent intent = new Intent(mContext, MyAppActivity.class);
                    mContext.startActivity(intent);
                    DTV.getInstance(Constant.DTV_PLUGIN_NAME).unPrepareDTV();
                } else {
                    try {
                        if (i == 2) {
                            int sourceId = SourceManagerInterface.getCurSourceId();
                            if (!halApi.isDTVSource(sourceId)) {
                                SourceManagerInterface.deselectSource(sourceId, true);
                            }
                        }
                        mContext.stopPlayer();
                        mContext.releaseResourcePlayer();
                        DTV.getInstance(Constant.DTV_PLUGIN_NAME).unPrepareDTV();
                        currentSelectAppId = i;
                        String pkg = secondPkg[i].trim();
                        String cls = secondCls[i].trim();
                        Intent mIntent = new Intent();
                        mIntent.setClassName(pkg, cls);
                        mContext.startActivity(mIntent);
                    } catch (ActivityNotFoundException e) {
                        if (i != 0) {
                            Toast.makeText(mContext, mContext.getText(R.string.failed_to_start).toString() + i,
                                    Toast.LENGTH_SHORT).show();
                        }
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogHelper.d(TAG, "onFocusChange");
        if (hasFocus) {
            v.bringToFront();
            v.getBackground().setAlpha(255);
            if (v.getId() == R.id.tv_item_window) {
            } else {
                v.animate().scaleX(0.913f).scaleY(0.913f)
                        .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            }
            switch (v.getId()) {
                case R.id.tv_item_window:
                    mContext.setFocusedView(Constant.NUMBER_0);
                    mContext.setFocusUp(true);
                    break;
                case R.id.app_item_icon:
                    mContext.setFocusedView(Constant.NUMBER_1);
                    mContext.setFocusUp(true);
                    break;
                case R.id.app_item_mediacenter:
                    mContext.setFocusedView(Constant.NUMBER_2);
                    mContext.setFocusUp(false);
                    break;
                case R.id.app_item_browser:
                    mContext.setFocusedView(Constant.NUMBER_3);
                    mContext.setFocusUp(false);
                    break;
                case R.id.app_item_multi_screen:
                    mContext.setFocusedView(Constant.NUMBER_4);
                    mContext.setFocusUp(false);
                    break;
                case R.id.app_item_icon_3d:
                    mContext.setFocusedView(Constant.NUMBER_5);
                    mContext.setFocusUp(false);
                    break;
                case R.id.app_item_tvqq:
                    mContext.setFocusedView(Constant.NUMBER_6);
                    mContext.setFocusUp(false);
                    break;
                default:
                    break;
            }
        } else {
            v.getBackground().setAlpha(0);
            if (v.getId() == R.id.tv_item_window) {
            } else if (v.getId() == R.id.app_item_icon) {
                v.animate().scaleX(0.836f).scaleY(0.836f)
                        .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            } else {
                v.animate().scaleX(0.83f).scaleY(0.83f)
                        .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            }
        }
    }

    public void setTextValue(int curId) {
        if (null == mCurSourceText) {
            return;
        }
        switch (curId) {
            case EnumSourceIndex.SOURCE_DVBC:
                mCurSourceText.setText(R.string.dvbc);
                break;
            case EnumSourceIndex.SOURCE_DTMB:// DTV
                mCurSourceText.setText(R.string.dtmb);
                break;
            case EnumSourceIndex.SOURCE_ATV:
                mCurSourceText.setText(R.string.atv);
                break;
            case EnumSourceIndex.SOURCE_CVBS1:// AV1
                mCurSourceText.setText(R.string.av1);
                break;
            case EnumSourceIndex.SOURCE_CVBS2:// AV2
                mCurSourceText.setText(R.string.av2);
                break;
            case EnumSourceIndex.SOURCE_YPBPR1:// YPBPR
                mCurSourceText.setText(R.string.ypbpr);
                break;
            case EnumSourceIndex.SOURCE_HDMI1:
                mCurSourceText.setText(R.string.hdmi1);
                break;
            case EnumSourceIndex.SOURCE_HDMI2:
                mCurSourceText.setText(R.string.hdmi2);
                break;
            case EnumSourceIndex.SOURCE_HDMI3:
                mCurSourceText.setText(R.string.hdmi3);
                break;
            case EnumSourceIndex.SOURCE_HDMI4:
                mCurSourceText.setText(R.string.hdmi4);
                break;
            case EnumSourceIndex.SOURCE_VGA:
                mCurSourceText.setText(R.string.vga);
                break;
            case EnumSourceIndex.SOURCE_DVBT:
                mCurSourceText.setText(R.string.dvbt);
                break;
            case EnumSourceIndex.SOURCE_ATSC:
                mCurSourceText.setText(R.string.atsc);
                break;
            case EnumSourceIndex.SOURCE_DVBS:
                mCurSourceText.setText(R.string.dvbs);
                break;
            case EnumSourceIndex.SOURCE_ISDBT:
                mCurSourceText.setText(R.string.isdbt);
                break;
            default:
                mCurSourceText.setText(R.string.atv);
                break;
        }
        hideOrShowText(true);
    }

    public void hideOrShowText(boolean show) {
        if (show) {
            mCurSourceText.setVisibility(View.VISIBLE);
        } else {
            mCurSourceText.setVisibility(View.GONE);
        }
    }
}
