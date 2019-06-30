
package com.hisilicon.launcher.view.setting;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import com.hisilicon.launcher.util.LogHelper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.Refreshable;
import com.hisilicon.launcher.util.Constant;

/**
 * The view is suitable for launcher radio interface settings, used to set the
 * language, image pattern etc..
 *
 * @author tang_shengchang <br>
 */
public class SelectorView extends RelativeLayout implements Refreshable {

    private static final String TAG = "SelectorView";
    private Context mContext;
    // listView of menu
    public ListView mListMenu;
    private boolean flag = false;
    // text of menu
    private TextView mMenuText;
    // type of widget
    private WidgetType mWidgetType;
    // button of menu
    private Button mMenuBtn;
    // data of the widget
    private int[] mWigetData;
    private int mSystem = 0;
    // private CustomSettingView mCustomSettingView;
    // list of WidgetType
    private List<WidgetType> mWidgetTypeList = null;

    public SelectorView(Context context, SelectorView mselectorView) {
        super(context);
        mContext = context;
    }

    public SelectorView(CustomSettingView customSettingView, Context context,
            WidgetType widgetType, List<WidgetType> li) {
        super(context);
        mContext = context;
        // this.mCustomSettingView = customSettingView;
        mWidgetType = widgetType;
        if (mWidgetType == null
                || mWidgetType.getmAccessSysValueInterface() == null) {
            throw new NullPointerException(
                    "mWidgetType is null || mWidgetType.getmAccessDataInterface() is null");
        }

        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.selector_view, this);
        mMenuText = (TextView) findViewById(R.id.menu_text);
        mMenuText.setText(widgetType.getName());
        mMenuText.setTextColor(Color.parseColor(mContext.getResources()
                .getStringArray(R.array.text_color)[0]));
        LogHelper.d(TAG, widgetType.getName() + "length="
                + widgetType.getName().length());
        mMenuBtn = (Button) findViewById(R.id.data_btn);
        mMenuBtn.setFocusable(mWidgetType.isEnable());
        findViewById(R.id.button_layout).setBackgroundResource(
                R.drawable.button_transparent);
        mSystem = mWidgetType.getmAccessSysValueInterface().getSysValue();
        mWigetData = widgetType.getData();
        LogHelper.d(TAG, mSystem + "");
        mMenuBtn.setText(mWigetData[mSystem]);

        // When the Selector data for the first time, left arrow. Data is the
        // last right arrow
        // ArrowheadAppearOff();
        mMenuBtn.setTextColor(Color.parseColor(mContext.getResources()
                .getStringArray(R.array.text_color)[0]));
        mWidgetTypeList = li;
        mMenuBtn.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                flag = hasFocus;
                if (flag) {
                    // ArrowheadAppearOff();
                    if (mWidgetTypeList.size() > 1) {
                        findViewById(R.id.button_layout).setBackgroundResource(
                                R.drawable.launcher_set_focus);
                        mMenuBtn.setTextColor(Color.parseColor(mContext
                                .getResources().getStringArray(
                                        R.array.text_color)[3]));
                    } else {
                        findViewById(R.id.data_btn).setBackgroundResource(
                                R.drawable.selector_button_bg);
                        mMenuBtn.setTextColor(Color.parseColor(mContext
                                .getResources().getStringArray(
                                        R.array.text_color)[0]));
                    }
                    mMenuText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[3]));
                } else {
                    // ArrowheadAppearOff();
                    mMenuText.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[0]));
                    mMenuBtn.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[0]));
                    findViewById(R.id.button_layout).setBackgroundResource(
                            R.drawable.button_transparent);
                }
                LogHelper.d(TAG, "onFocusChange flag = " + flag);
            }
        });

        mMenuBtn.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    // The removal of circulating cycle, unlock
                    // if (mCustomSettingView.onKey(v, keyCode, event)) {
                    // return true;
                    // }
                    LogHelper.d(TAG, "keycode = " + keyCode + "; event = "
                            + event);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            if (flag == true) {
                                mSystem--;
                                if (mSystem < 1) {
                                }
                                if (mSystem < 0) {
                                    mSystem = mWigetData.length - 1;
                                }
                                mMenuBtn.setText(mWigetData[mSystem]);
                                mWidgetType.getmAccessSysValueInterface()
                                        .setSysValue(mSystem);
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            if (flag == true) {
                                mSystem++;
                                if (mSystem >= mWigetData.length - 1) {
                                }
                                if (mSystem >= mWigetData.length) {
                                    mSystem = 0;
                                }
                                mMenuBtn.setText(mWigetData[mSystem]);
                                mWidgetType.getmAccessSysValueInterface()
                                        .setSysValue(mSystem);
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_BACK:
                            LogHelper.d(TAG, "on keycode back!");
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void refreshUI() {
        mSystem = mWidgetType.getmAccessSysValueInterface().getSysValue();
        mMenuBtn.setText(mWigetData[mSystem]);
        mMenuBtn.setFocusable(mWidgetType.isEnable());
        ChangeArrowState(mWidgetType.isEnable());
    }

    private void ChangeArrowState(boolean isEnable) {
        if (isEnable) {
            findViewById(R.id.arrow_left).setBackgroundResource(
                    R.drawable.arrow_left_focus);
            findViewById(R.id.arrow_right).setBackgroundResource(
                    R.drawable.arrow_right_focus);
            mMenuText.setTextColor(Color.parseColor(mContext.getResources()
                        .getStringArray(R.array.text_color)[0]));
            mMenuBtn.setTextColor(Color.parseColor(mContext.getResources()
                        .getStringArray(R.array.text_color)[0]));
        } else {
            findViewById(R.id.arrow_left).setBackgroundResource(
                    R.drawable.arrow_left_unfocus);
            findViewById(R.id.arrow_right).setBackgroundResource(
                    R.drawable.arrow_right_unfocus);
            mMenuText.setTextColor(Color.parseColor(mContext.getResources()
                        .getStringArray(R.array.text_color)[1]));
            mMenuBtn.setTextColor(Color.parseColor(mContext.getResources()
                        .getStringArray(R.array.text_color)[1]));
        }
    }

    @Override
    public WidgetType getWidgetType() {
        return mWidgetType;
    }

    @Override
    public boolean getIsFocus() {
        return flag;
    }
}
