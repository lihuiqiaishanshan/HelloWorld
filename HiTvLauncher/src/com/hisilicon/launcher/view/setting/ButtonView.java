
package com.hisilicon.launcher.view.setting;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.Refreshable;

/**
 * The view is suitable for launcher settings, restore factory settings, system
 * upgrade.
 *
 * @author wang_chuanjian <br>
 */
public class ButtonView extends LinearLayout implements Refreshable {

    private Context mContext;
    // the button of select
    private Button selectBtn;
    // the layout of button view
    private LinearLayout btnViewLayout;
    // type of widget
    private WidgetType mWidgetType;

    public ButtonView(Context context, CustomSettingView customSettingView,
            WidgetType widgetType, List<WidgetType> li) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.button_view, this);
        this.mContext = context;
        this.mWidgetType = widgetType;
        selectBtn = (Button) findViewById(R.id.select_button);
        btnViewLayout = (LinearLayout) findViewById(R.id.selector_layout);
        selectBtn.setText(mWidgetType.getOnlySelectorName());
        selectBtn.setTextColor(Color.parseColor(mContext.getResources()
                .getStringArray(R.array.text_color)[0]));
        selectBtn.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View arg0, boolean flag) {
                if (flag) {
                    btnViewLayout
                            .setBackgroundResource(R.drawable.launcher_set_focus);
                    selectBtn.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[3]));
                } else {
                    btnViewLayout
                            .setBackgroundResource(R.drawable.button_transparent);
                    selectBtn.setTextColor(Color
                            .parseColor(mContext.getResources().getStringArray(
                                    R.array.text_color)[0]));

                }

            }
        });
        selectBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                v.setTag(getTag());
                mWidgetType.getmAccessOnClickInterface().onClickEvent(v);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void refreshUI() {

    }

    @Override
    public WidgetType getWidgetType() {
        return null;
    }

    @Override
    public boolean getIsFocus() {
        return false;
    }
}
