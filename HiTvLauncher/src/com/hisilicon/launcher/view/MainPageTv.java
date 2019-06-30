
package com.hisilicon.launcher.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.util.AttributeSet;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.PixelFormat;
import android.content.ActivityNotFoundException;

import com.hisilicon.android.tvapi.constant.EnumSourceIndex;
import com.hisilicon.android.tvapi.vo.RectInfo;
import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.ShowAbleInterface;
import com.hisilicon.launcher.interfaces.SourceManagerInterface;
import com.hisilicon.launcher.model.SourceObj;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.FullScreenRectInfo;
import com.hisilicon.launcher.util.Util;

/**
 * The first big view
 */
@SuppressLint("ResourceAsColor")
public class MainPageTv extends RelativeLayout implements ShowAbleInterface,
        View.OnClickListener, OnFocusChangeListener {

    private static final String TAG = "MainPageTv";
    public final static int PAGENUM = 0;
    private MainActivity mContext;
    // The actual background
    public View[] mImgView;
    // when the Source is not used,set the background ashing
    public RelativeLayout[] mInnerView;
    // show the current Source
    private TextView mCurSourceText;

    private boolean isOversea = false;

    private List<Integer> availSourceList = SourceManagerInterface.getSourceList();

    public MainPageTv(Context context) {
        super(context);
    }

    public MainPageTv(Context context, AttributeSet attrs) {
        super(context, attrs);
        LogHelper.d(TAG,"MainPageTv init");
        this.mContext = (MainActivity) context;
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View parent = inflater.inflate(R.layout.main_page_tv, this);
        readConfig();
        initView(parent);
    }

    private void readConfig() {
        String appSupport = SystemProperties.get("persist.app.support", "all");
        LogHelper.d(TAG, "appSupport = " + appSupport);
        if(appSupport.equals("oversea")) {
            isOversea = true;
        }
    }

    private void initView(View parent) {
        SurfaceView surface = (SurfaceView)parent.findViewById(R.id.minvideo);
        SurfaceHolder sh = surface.getHolder();
        sh.setFormat(PixelFormat.TRANSPARENT);
        mImgView = new View[] {
                parent.findViewById(R.id.tv_item_window),
                parent.findViewById(R.id.tv_item_dvbc),
                parent.findViewById(R.id.tv_item_av1),
                parent.findViewById(R.id.tv_item_dtmb),
                parent.findViewById(R.id.tv_item_av2),
                parent.findViewById(R.id.tv_item_ypbpr),
                parent.findViewById(R.id.tv_item_vga),
                parent.findViewById(R.id.tv_item_atv),
                parent.findViewById(R.id.tv_item_hdmi1),
                parent.findViewById(R.id.tv_item_hdmi2),
                parent.findViewById(R.id.tv_item_hdmi3),
                parent.findViewById(R.id.tv_item_hdmi4)
        };
        mInnerView = new RelativeLayout[] {
                (RelativeLayout)parent.findViewById(R.id.view_window_pic),
                (RelativeLayout)parent.findViewById(R.id.view_dvbc),
                (RelativeLayout)parent.findViewById(R.id.view_av1),
                (RelativeLayout)parent.findViewById(R.id.view_dtmb),
                (RelativeLayout)parent.findViewById(R.id.view_av2),
                (RelativeLayout)parent.findViewById(R.id.view_ypbpr),
                (RelativeLayout)parent.findViewById(R.id.view_vga),
                (RelativeLayout)parent.findViewById(R.id.view_atv),
                (RelativeLayout)parent.findViewById(R.id.view_hdmi1),
                (RelativeLayout)parent.findViewById(R.id.view_hdmi2),
                (RelativeLayout)parent.findViewById(R.id.view_hdmi3),
                (RelativeLayout)parent.findViewById(R.id.view_hdmi4)
        };

        for (int i = 0; i < mImgView.length; i++) {
            if(i == 3) {
                ((ImageView)mInnerView[i].getChildAt(0)).setBackgroundResource(
                        isOversea ? R.drawable.tv_icon_dvbt:R.drawable.tv_icon_dtv);
            }
            mImgView[i].setOnClickListener(this);
            mImgView[i].getBackground().setAlpha(0);
            mImgView[i].setOnFocusChangeListener(this);
        }
        mCurSourceText = (TextView) parent.findViewById(R.id.tv_window_txt);
        TextView dvb = (TextView) parent.findViewById(R.id.tv_dtmb_txt);
        dvb.setText(isOversea ? R.string.dvbt : R.string.dtmb);
        setSourceBackground();
    }

    /**
     * set Source Background enabled according to the source isavailable
     */
    private void setSourceBackground() {
        List<SourceObj> sourcelist = getSouceList();
        SourceObj model;
        for (int i = 0; i < sourcelist.size(); i++) {
            model = sourcelist.get(i);
            if (!model.isAvail()) {
                if ((model.getSourceId() == Constant.NUMBER_0)
                        || model.getSourceId() == Constant.NUMBER_2
                        || model.getSourceId() == Constant.NUMBER_3
                        || model.getSourceId() == Constant.NUMBER_4) {
                    mInnerView[model.getSourceId()]
                            .setBackgroundResource(R.drawable.tv_grey_small);
                } else if ((model.getSourceId() == Constant.NUMBER_5)
                        || model.getSourceId() == Constant.NUMBER_6
                        || model.getSourceId() == Constant.NUMBER_7
                        || model.getSourceId() == Constant.NUMBER_8
                        || model.getSourceId() == Constant.NUMBER_9) {
                    mInnerView[model.getSourceId()]
                            .setBackgroundResource(R.drawable.tv_grey_middle);
                } else {
                    mInnerView[model.getSourceId()]
                            .setBackgroundResource(R.drawable.tv_grey_high);
                }

                mImgView[model.getSourceId()].setFocusable(false);
                mImgView[model.getSourceId()].setEnabled(false);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mImgView[Constant.NUMBER_0].hasFocus()
                    || mImgView[Constant.NUMBER_7].hasFocus()) {
                mContext.snapToPreScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if ((mImgView[Constant.NUMBER_5].hasFocus() || mImgView[Constant.NUMBER_6]
                    .hasFocus() || mImgView[Constant.NUMBER_11].hasFocus())) {
                mContext.snapToNextScreen();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mImgView[Constant.NUMBER_0].hasFocus()
                    || mImgView[Constant.NUMBER_1].hasFocus()
                    || mImgView[Constant.NUMBER_2].hasFocus()
                    || mImgView[Constant.NUMBER_5].hasFocus()) {
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mImgView[Constant.NUMBER_7].hasFocus()
                    || mImgView[Constant.NUMBER_8].hasFocus()
                    || mImgView[Constant.NUMBER_9].hasFocus()
                    || mImgView[Constant.NUMBER_10].hasFocus()
                    || mImgView[Constant.NUMBER_11].hasFocus()) {
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
        LogHelper.d(TAG, "Now onShow :" + PAGENUM +" MainActivity.isSnapLeftOrRight"
                    + MainActivity.isSnapLeftOrRight());
        if (MainActivity.isSnapLeftOrRight()) {
            if (!mContext.getTagView().hasFocus()) {
                if (mContext.isSnapLeft()) {
                    if (mContext.isFocusUp()) {
                        mImgView[Constant.NUMBER_5].requestFocus();
                    } else {
                        mImgView[Constant.NUMBER_11].requestFocus();
                    }
                } else {
                    if (mContext.isFocusUp()) {
                        mImgView[Constant.NUMBER_0].requestFocus();
                    } else {
                        mImgView[Constant.NUMBER_7].requestFocus();
                    }
                }
            }
        } else {
            requestFocusBySourceID();
        }
        MainActivity.setSnapLeftOrRight(false);
        mContext.getTagView().setViewOnSelectChange(PAGENUM);
    }

    /**
     * On the basis of the reference, or select the default
     */
    private void requestFocusBySourceID() {
        if (SystemProperties.getBoolean("persist.launcher.tvpage.debug", false)) {
            mImgView[Constant.NUMBER_0].requestFocus();
            return;
        }
        int curId = SourceManagerInterface.getSelectSourceId();
        switch (curId) {
            case EnumSourceIndex.SOURCE_DVBC:
                mImgView[Constant.NUMBER_1].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_DTMB:// DTV
                mImgView[Constant.NUMBER_3].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_DVBT:// DVB-T
                mImgView[Constant.NUMBER_0].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_ATV:
                mImgView[Constant.NUMBER_7].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_CVBS1:// AV1
                mImgView[Constant.NUMBER_2].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_CVBS2:// AV2
                mImgView[Constant.NUMBER_4].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_YPBPR1:// YPBPR
                mImgView[Constant.NUMBER_5].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_HDMI1:
                mImgView[Constant.NUMBER_8].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_HDMI2:
                mImgView[Constant.NUMBER_9].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_HDMI3:
                mImgView[Constant.NUMBER_10].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_HDMI4:
                mImgView[Constant.NUMBER_11].requestFocus();
                break;
            case EnumSourceIndex.SOURCE_VGA:
                mImgView[Constant.NUMBER_6].requestFocus();
                break;
            default:
                mImgView[Constant.NUMBER_7].requestFocus();
                break;
        }
    }

    public void hideOrShowText(boolean show){
        if(show) {
            mCurSourceText.setVisibility(View.VISIBLE);
        } else {
            mCurSourceText.setVisibility(View.GONE);
        }
    }

    /**
     * set current source name
     */
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

    public int getId() {
        return PAGENUM;
    }

    @Override
    public View[] getImgViews() {
        return mImgView;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        int destid = 0;
        intent.setAction(Constant.INTENT_ATV);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        switch (v.getId()) {
            case R.id.tv_item_window: {
                int curId = SourceManagerInterface.getSelectSourceId();
                switch (curId) {
                    case EnumSourceIndex.SOURCE_DVBC:
                    case EnumSourceIndex.SOURCE_DTMB:
                    case EnumSourceIndex.SOURCE_DVBT:
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
            }
                return;
            case R.id.tv_item_dvbc:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_DVBC);
                intent.setAction(Constant.INTENT_DTV);
                destid = EnumSourceIndex.SOURCE_DVBC;
                break;
            case R.id.tv_item_dtmb:
                destid = isOversea ? EnumSourceIndex.SOURCE_DVBT:EnumSourceIndex.SOURCE_DTMB;
                intent.putExtra("SourceName", destid);
                intent.setAction(Constant.INTENT_DTV);
                break;
            case R.id.tv_item_atv:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_ATV);
                destid = EnumSourceIndex.SOURCE_ATV;
                break;
            case R.id.tv_item_av1:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_CVBS1);
                destid = EnumSourceIndex.SOURCE_CVBS1;
                break;
            case R.id.tv_item_av2:
                if(!availSourceList.contains(EnumSourceIndex.SOURCE_CVBS2)) {
                    Toast.makeText(getContext(),
                            R.string.toast_av2_not_supported, Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_CVBS2);
                destid = EnumSourceIndex.SOURCE_CVBS2;
                break;
            case R.id.tv_item_ypbpr:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_YPBPR1);
                destid = EnumSourceIndex.SOURCE_YPBPR1;
                break;
            case R.id.tv_item_hdmi1:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_HDMI1);
                destid = EnumSourceIndex.SOURCE_HDMI1;
                break;
            case R.id.tv_item_hdmi2:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_HDMI2);
                destid = EnumSourceIndex.SOURCE_HDMI2;
                break;
            case R.id.tv_item_hdmi3:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_HDMI3);
                destid = EnumSourceIndex.SOURCE_HDMI3;
                break;
            case R.id.tv_item_hdmi4:
                if(!availSourceList.contains(EnumSourceIndex.SOURCE_HDMI4)) {
                    Toast.makeText(getContext(),
                            R.string.toast_hdmi4_not_supported, Toast.LENGTH_SHORT).show();
                    return;
                }
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_HDMI4);
                destid = EnumSourceIndex.SOURCE_HDMI4;
                break;
            case R.id.tv_item_vga:
                intent.putExtra("SourceName", EnumSourceIndex.SOURCE_VGA);
                destid = EnumSourceIndex.SOURCE_VGA;
                break;
            default:
                break;
        }
        LogHelper.d(TAG, "start Full window play, source is " + destid);
        mContext.setIsSelectSource(false);
        MainActivity.setSnapLeftOrRight(false);
        mContext.stopPlayer();
        try{
            mContext.startActivity(intent);
        } catch (ActivityNotFoundException e){
            Toast.makeText(mContext, mContext.getText(R.string.apk_not_found).toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        LogHelper.d(TAG, "onFocusChange");
        if (hasFocus) {
            v.bringToFront();
            v.getBackground().setAlpha(255);
            // Set the flag and focus related position
            switch (v.getId()) {
                case R.id.tv_item_window:
                    mContext.setFocusedView(Constant.NUMBER_0);
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_dvbc:
                    mContext.setFocusedView(Constant.NUMBER_1);
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_av1:
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_2);
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_dtmb:
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_3);
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_av2:
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_4);
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_vga:
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_5);
                    mContext.setFocusUp(false);
                    break;
                case R.id.tv_item_ypbpr:
                    v.animate().scaleX(0.935f).scaleY(0.935f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_6);
                    mContext.setFocusUp(true);
                    break;
                case R.id.tv_item_atv:
                    v.animate().scaleX(0.908f).scaleY(0.924f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_7);
                    mContext.setFocusUp(false);
                    break;
                case R.id.tv_item_hdmi1:
                    v.animate().scaleX(0.908f).scaleY(0.924f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_8);
                    mContext.setFocusUp(false);
                    break;
                case R.id.tv_item_hdmi2:
                    v.animate().scaleX(0.908f).scaleY(0.924f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_9);
                    mContext.setFocusUp(false);
                    break;
                case R.id.tv_item_hdmi3:
                    v.animate().scaleX(0.908f).scaleY(0.924f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_10);
                    mContext.setFocusUp(false);
                    break;
                case R.id.tv_item_hdmi4:
                    v.animate().scaleX(0.908f).scaleY(0.924f)
                            .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
                    mContext.setFocusedView(Constant.NUMBER_11);
                    mContext.setFocusUp(false);
                    break;
                default:
                    break;
            }
        } else {
            v.getBackground().setAlpha(0);
            if (v.getId() == R.id.tv_item_window) {
            } else if (v.getId() == R.id.tv_item_dvbc
                    || v.getId() == R.id.tv_item_av1
                    || v.getId() == R.id.tv_item_dtmb
                    || v.getId() == R.id.tv_item_av2
                    || v.getId() == R.id.tv_item_ypbpr
                    || v.getId() == R.id.tv_item_vga ) {
                v.animate().scaleX(0.85f).scaleY(0.85f)
                        .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            } else {
                v.animate().scaleX(0.825f).scaleY(0.84f)
                        .setDuration(Constant.SCARE_ANIMATION_DURATION).start();
            }
        }
    }

    public List<Integer> getAvailbleIndex() {
        List<Integer> list = new ArrayList<Integer>();
        return list;
    }

    public static List<SourceObj> getSouceList() {
        List<SourceObj> list = new ArrayList<SourceObj>();
        List<Integer> sourcelist = getTestSourceList();
        List<Integer> availablelist = getAvailableList();

        for (int i = 0; i < sourcelist.size(); i++) {
            SourceObj mSourceObj = new SourceObj();

            mSourceObj.setSourceId(i + 1);

            int id = sourcelist.get(i);
            for (int j = 0; j < availablelist.size(); j++) {
                int aviid = availablelist.get(j);
                if (id == aviid) {
                    mSourceObj.setAvail(true);
                    break;
                } else {
                    mSourceObj.setAvail(false);
                }
            }
            list.add(mSourceObj);
        }
        // Put slist into list
        return list;
    }

    /**
     * get all source
     *
     * @return all source list
     */
    public static List<Integer> getTestSourceList() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(Constant.NUMBER_1);
        list.add(Constant.NUMBER_2);
        list.add(Constant.NUMBER_3);
        list.add(Constant.NUMBER_4);
        list.add(Constant.NUMBER_5);
        list.add(Constant.NUMBER_6);
        list.add(Constant.NUMBER_7);
        list.add(Constant.NUMBER_8);
        list.add(Constant.NUMBER_9);
        list.add(Constant.NUMBER_10);
        return list;
    }

    /**
     * get current available source list test data
     *
     * @return list
     */
    public static List<Integer> getAvailableList() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(Constant.NUMBER_1);
        list.add(Constant.NUMBER_2);
        list.add(Constant.NUMBER_3);
        list.add(Constant.NUMBER_4);
        list.add(Constant.NUMBER_5);
        list.add(Constant.NUMBER_6);
        list.add(Constant.NUMBER_7);
        list.add(Constant.NUMBER_8);
        list.add(Constant.NUMBER_9);
        list.add(Constant.NUMBER_10);
        list.add(Constant.NUMBER_11);
        return list;
    }
}
