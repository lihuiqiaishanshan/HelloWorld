
package com.hisilicon.launcher.logic.factory;

import android.content.Context;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.logic.model.FactoryResetLogic;
import com.hisilicon.launcher.logic.model.NetStateLogic;
import com.hisilicon.launcher.logic.model.PictureModeLogic;
import com.hisilicon.launcher.logic.model.SeniorModeLogic;
import com.hisilicon.launcher.logic.model.SoundModeLogic;
import com.hisilicon.launcher.logic.model.SystemInfoLogic;
import com.hisilicon.launcher.logic.model.SystemLocalUpdateLogic;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.view.setting.NetSettingDialog;

/**
 * the class of control logic
 *
 * @author huyq
 */
public class LogicFactory {

    private static final String TAG = "LogicFactory";
    private Context mContext;

    public LogicFactory(Context mContext) {
        super();
        this.mContext = mContext;
    }

    /**
     * create logic by index
     *
     * @param index
     * @return
     */
    public InterfaceLogic createLogic(int index) {
        LogHelper.d(TAG, "index = " + index);
        InterfaceLogic logic = null;
        switch (index) {
            case R.id.set_item_pic:
                // picture setting
                logic = new PictureModeLogic(mContext);
                break;
            case R.id.set_item_sound:
                // voice setting
                logic = new SoundModeLogic(mContext);
                break;
            case R.id.set_item_advanced:
                // senior setting
                logic = new SeniorModeLogic(mContext);
                break;
            case R.id.set_item_recover:
                // factory reset
                logic = new FactoryResetLogic(mContext);
                break;
            case R.id.set_item_systeminfo:
                // system info
                logic = new SystemInfoLogic(mContext);
                break;
            case NetSettingDialog.NET_STATE:
                // net state
                logic = new NetStateLogic(mContext);
                break;
            default:
                break;
        }
        return logic;
    }
}
