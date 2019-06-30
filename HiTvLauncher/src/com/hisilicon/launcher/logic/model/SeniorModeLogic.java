
package com.hisilicon.launcher.logic.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.RemoteException;
import android.os.Message;
import com.hisilicon.launcher.util.LogHelper;

import com.hisilicon.launcher.hal.halApi;
import com.hisilicon.launcher.interfaces.AudioInterface;
import com.hisilicon.launcher.interfaces.CECInterface;
import com.hisilicon.launcher.interfaces.FactoryInterface;
import com.hisilicon.launcher.interfaces.InterfaceValueMaps;
import com.hisilicon.launcher.logic.factory.InterfaceLogic;
import com.hisilicon.launcher.MainActivity;
import com.hisilicon.launcher.model.WidgetType;
import com.hisilicon.launcher.model.WidgetType.AccessSysValueInterface;
import com.hisilicon.launcher.R;
import com.hisilicon.launcher.util.Constant;
import com.hisilicon.launcher.util.Util;
/**
 * SeniorMode
 *
 * @author wangchuanjian
 */
public class SeniorModeLogic implements InterfaceLogic {

    private static final String TAG = "SeniorModeLogic";
    private MainActivity mContext;
    private WidgetType mSeniorMode, mPowerMusic, mMusicNumber;
    private Handler mHandler;

    public SeniorModeLogic(Context mContext) {
        super();
        this.mContext = (MainActivity) mContext;
    }

    @Override
    public List<WidgetType> getWidgetTypeList() {
        List<WidgetType> mWidgetList = new ArrayList<WidgetType>();
        Resources res = mContext.getResources();
        // SeniorMode
        mSeniorMode = new WidgetType();
        // set name for SeniorMode
        mSeniorMode.setName(res.getStringArray(R.array.senior_mode_string)[0]);
        // set type for SeniorMode
        mSeniorMode.setType(WidgetType.TYPE_SELECTOR);
        mSeniorMode.setmAccessSysValueInterface(new AccessSysValueInterface() {

            @Override
            public int setSysValue(int i) {
                LogHelper.d(TAG, "mSeniorMode setSysValue i = " + i);
                CECInterface.setMenuLag(i);
                return setLanguage(i);
            }

            @Override
            public int getSysValue() {
                int mode = getLanguage();
                return Util.getIndexFromArray(mode,
                        InterfaceValueMaps.language_change);
            }
        });
        // set data for SeniorMode
        mSeniorMode.setData(Util
                .createArrayOfParameters(InterfaceValueMaps.language_change));
        mWidgetList.add(mSeniorMode);

        // Power music
        mPowerMusic = new WidgetType();
        // set name for Power music
        mPowerMusic.setName(res.getStringArray(R.array.senior_mode_string)[1]);
        // set type for Power music
        mPowerMusic.setType(WidgetType.TYPE_SELECTOR);
        mPowerMusic.setmAccessSysValueInterface(new AccessSysValueInterface() {

            @Override
            public int setSysValue(int i) {
                LogHelper.d(TAG, "mPowerMusic setSysValue i = " + i);
                int ret;
                ret = FactoryInterface.enablePowerMusic(i == 1);
                mMusicNumber.setEnable(i == 1);
                Message msg = new Message();
                msg.what = Constant.SETTING_UI_REFRESH_VIEWS;
                List<String> Stringlist = new ArrayList<String>();
                Stringlist.add(mContext.getResources().getStringArray(
                        R.array.senior_mode_string)[2]);
                msg.obj = Stringlist;
                mHandler.sendMessage(msg);
                return ret;
            }

            @Override
            public int getSysValue() {
                int ret;
                boolean mode = FactoryInterface.isPowerMusicEnable();
                if (mode == true) {
                    ret = 1;
                    mMusicNumber.setEnable(true);
                } else {
                    ret = 0;
                    mMusicNumber.setEnable(false);
                }
                Message msg = new Message();
                msg.what = Constant.SETTING_UI_REFRESH_VIEWS;
                List<String> Stringlist = new ArrayList<String>();
                Stringlist.add(mContext.getResources().getStringArray(
                        R.array.senior_mode_string)[2]);
                msg.obj = Stringlist;
                mHandler.sendMessage(msg);
                return ret;
            }
        });
        // set data for Power music
        mPowerMusic.setData(Util
                .createArrayOfParameters(InterfaceValueMaps.on_off));
        mWidgetList.add(mPowerMusic);

        // Music Number
        mMusicNumber = new WidgetType();
        // set name for Music Number
        mMusicNumber.setName(res.getStringArray(R.array.senior_mode_string)[2]);
        // set type for Music Number
        mMusicNumber.setType(WidgetType.TYPE_SELECTOR);
        mMusicNumber.setmAccessSysValueInterface(new AccessSysValueInterface() {

            @Override
            public int setSysValue(int i) {
                LogHelper.d(TAG, "mMusicNumber setSysValue i = " + i);
                int ret = FactoryInterface.setPowerMusicNo(InterfaceValueMaps.music_number[i][0]);
                return ret;
            }

            @Override
            public int getSysValue() {
                int mode = FactoryInterface.getPowerMusicNo();
                return Util.getIndexFromArray(mode,
                    InterfaceValueMaps.music_number);
            }
        });
        // set data for Music Number
        mMusicNumber.setData(Util
                .createArrayOfParameters(InterfaceValueMaps.music_number));
        mWidgetList.add(mMusicNumber);

        return mWidgetList;
    }

    /**
     * return 0：Simplified Chinese (default) 1：English
     */
    protected int getLanguage() {
        try {
            IActivityManager am = ActivityManagerNative.getDefault();
            Configuration config = am.getConfiguration();
            LogHelper.d(TAG, "getLanguage config.locale = " + config.locale);
            if (config.locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
                return 0;
            } else if (config.locale.getLanguage().equals(Locale.US.getLanguage())) {
                return 1;
            } else {
                setLanguage(0);
                return 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            setLanguage(0);
            return 0;
        }
    }

    /**
     * i:0 Simplified Chinese 1 English
     */
    protected int setLanguage(int index) {
        halApi.setLanguage(index);
        SharedPreferences preferences = mContext.getSharedPreferences(
                Constant.SET_LOCALE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constant.RESET_LOCALE, true);
        editor.commit();
        return 1;
    }

    @Override
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void dismissDialog() {
        // TODO Auto-generated method stub
    }

}
