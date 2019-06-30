
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
 * @author wang_chuanjian <br>
 */
public class LongSystemTextView extends LinearLayout implements Refreshable {

    private TextView mNameText;
    private TextView mSystemInfoText;

    public LongSystemTextView(Context context, CustomSettingView customSettingView,
            List<WidgetType> li, WidgetType dia) {
        super(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.long_system_textview, this);
        mNameText = (TextView) findViewById(R.id.system_info_title_txt);
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
