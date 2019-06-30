
package com.hisilicon.launcher.logic.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.R;
import com.hisilicon.launcher.interfaces.PictureInterface;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.AccessProgressInterface;
import com.hisilicon.launcher.util.Constant;

/**
 * picture model
 *
 * @author wangchuanjian
 */

public class PictureModeLogic implements InterfaceLogic {

    private static final String TAG = "PictureModeLogic";
    private Context mContext;
    private Handler mHandler;

    public PictureModeLogic(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        Resources res = mContext.getResources();
        // Brightness
        WidgetType mBrightness = new WidgetType();
        // set name for Brightness
        mBrightness.setName(res.getStringArray(R.array.picture_mode_string)[1]);
        // set type for Brightness
        mBrightness.setType(WidgetType.TYPE_PROGRESS);
        mBrightness.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mBrightness setProgress i = " + i);
                PictureInterface.setBrightness(i);

                refreshPictureSelector();

                return i;
            }

            @Override
            public int getProgress() {
                // int i = 50;
                LogHelper.d(TAG, "mBrightness getProgress i = ");
                return PictureInterface.getBrightness();
            }
        });
        mWidgetList.add(mBrightness);

        // Contrast
        WidgetType mContrast = new WidgetType();
        // set name for Contrast
        mContrast.setName(res.getStringArray(R.array.picture_mode_string)[2]);
        // set type for Contrast
        mContrast.setType(WidgetType.TYPE_PROGRESS);
        mContrast.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mContrast setProgress i = " + i);

                PictureInterface.setContrast(i);
                return i;
            }

            @Override
            public int getProgress() {
                // int i = 50;
                LogHelper.d(TAG, "mContrast getProgress i = ");
                return PictureInterface.getContrast();
            }
        });
        mWidgetList.add(mContrast);

        // mSaturation
        WidgetType mSaturation = new WidgetType();
        // set name for mSaturation
        mSaturation.setName(res.getStringArray(R.array.picture_mode_string)[4]);
        // set type for mSaturation
        mSaturation.setType(WidgetType.TYPE_PROGRESS);
        mSaturation.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mHue setProgress i = " + i);

                PictureInterface.setSaturation(i);
                return i;
            }

            @Override
            public int getProgress() {
                LogHelper.d(TAG, "mHue getProgress i = ");
                return PictureInterface.getSaturation();
            }
        });
        mWidgetList.add(mSaturation);

        // Backlight
        WidgetType mBacklight = new WidgetType();
        // set name for Backlight
        mBacklight.setName(res.getStringArray(R.array.picture_mode_string)[5]);
        // set type for Backlight
        mBacklight.setType(WidgetType.TYPE_PROGRESS);
        mBacklight.setmAccessProgressInterface(new AccessProgressInterface() {

            @Override
            public int setProgress(int i) {
                LogHelper.d(TAG, "mBacklight setProgress i = " + i);
                PictureInterface.setBacklight(i);
                return i;

            }

            @Override
            public int getProgress() {
                // int i = 50;
                LogHelper.d(TAG, "mBacklight getProgress i = ");
                return PictureInterface.getBacklight();
            }
        });
        mWidgetList.add(mBacklight);

        return mWidgetList;
    }

    @Override
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    /**
     * refresh the selector of Picture
     */
    private void refreshPictureSelector() {
        Message msg = new Message();
        msg.what = Constant.SETTING_UI_REFRESH_VIEWS;
        List<String> Stringlist = new ArrayList<String>();
        Stringlist.add(mContext.getResources().getStringArray(
                R.array.picture_mode_string)[0]);
        msg.obj = Stringlist;
        mHandler.sendMessage(msg);
    }

    @Override
    public void dismissDialog() {
        // TODO Auto-generated method stub
    }

}
