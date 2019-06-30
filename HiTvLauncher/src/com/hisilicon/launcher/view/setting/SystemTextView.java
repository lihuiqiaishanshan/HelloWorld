
package com.hisilicon.launcher.view.setting;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.Refreshable;

/**
 * TextView of System
 *
 * @author wang_chuanjian <br>
 */
public class SystemTextView extends LinearLayout implements Refreshable {
    // text of name
    private TextView mNameText;
    // text of system info
    private TextView mSystemInfoText;

    public SystemTextView(Context context, CustomSettingView customSettingView,
            List<WidgetType> li, WidgetType dia) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.system_textview, this);
        mNameText = (TextView) findViewById(R.id.system_info_titletxt);
        mSystemInfoText = (TextView) findViewById(R.id.system_info_txt);
        mNameText.setText(dia.getName());
        mSystemInfoText.setText(dia.getInfo());

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
