
package com.hisilicon.launcher.logic.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.AccessOnClickInterface;
import com.hisilicon.launcher.view.setting.SystemUpdateDialog;

/**
 * factory reset
 *
 * @author wangchuanjian
 */
public class FactoryResetLogic implements InterfaceLogic {
    private static final String PROPERTY_DISABLE_RESET = "persist.sys.monkey.disablereset";

    private Context mContext;
    private SystemUpdateDialog mSystemUpdateDialog;

    public FactoryResetLogic(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        Resources res = mContext.getResources();
        WidgetType mFacReset = null;
        // restore user setting
        mFacReset = new WidgetType();
        // set name for FactoryReset
        mFacReset
                .setOnlySelectorName(res.getStringArray(R.array.factory_reset)[0]);
        // set type for FactoryReset
        mFacReset.setType(WidgetType.TYPE_ONLYSELECTOR);
        // set onClick event
        mFacReset.setmAccessOnClickInterface(new AccessOnClickInterface() {

            @Override
            public void onClickEvent(View v) {
                if (SystemProperties.getBoolean(PROPERTY_DISABLE_RESET, false))
                {
                    return;
                }
                // when click the restore user setting,write in the code here
                createDialog(350, 400, SystemUpdateDialog.USER_BACK);
            }
        });
        mWidgetList.add(mFacReset);
        // system restore
        mFacReset = new WidgetType();
        // set name for FactoryReset
        mFacReset
                .setOnlySelectorName(res.getStringArray(R.array.factory_reset)[1]);
        // set type of only selector
        mFacReset.setType(WidgetType.TYPE_ONLYSELECTOR);
        // set onClick event
        mFacReset.setmAccessOnClickInterface(new AccessOnClickInterface() {

            @Override
            public void onClickEvent(View v) {
                if (SystemProperties.getBoolean(PROPERTY_DISABLE_RESET, false))
                {
                    return;
                }
                // when click the restore system setting,write in the code here
                createDialog(350, 400, SystemUpdateDialog.SYSTEM_BACK);
            }
        });
        mWidgetList.add(mFacReset);
        return mWidgetList;
    }

    @Override
    public void setHandler(Handler handler) {
    }

    /**
     * create update dialog
     *
     * @param height
     * @param width
     * @param save
     */
    public void createDialog(int height, int width, int save) {
        mSystemUpdateDialog = new SystemUpdateDialog(mContext, save, null);
        mSystemUpdateDialog.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        Window window = mSystemUpdateDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.height = height;
        lp.width = width;
        window.setAttributes(lp);
        mSystemUpdateDialog.show();

    }

    @Override
    public void dismissDialog() {
        if (null != mSystemUpdateDialog && mSystemUpdateDialog.isShowing())
            mSystemUpdateDialog.dismiss();
    }

}
